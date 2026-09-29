package ru.finpet.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.finpet.app.data.db.FinPetDatabase
import ru.finpet.app.model.*
import ru.finpet.app.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.*

object GameRepository {

    private var db: FinPetDatabase? = null
    private var appContext: Context? = null

    private val _gameState = MutableStateFlow(createInitialState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        if (db == null) {
            db = FinPetDatabase(context.applicationContext)
            loadFromDatabase()
        }
    }

    private fun loadFromDatabase() {
        val database = db ?: return
        val profile = database.loadProfile()
        if (profile != null) {
            val (goalsList, selectedGoalId) = database.loadGoals()
            val transactions = database.loadTransactions()
            val completedQuests = database.loadCompletedQuests()
            val parentTasks = database.loadParentTasks()
            val budget = database.loadBudget(profile.currentPeriod) ?: BudgetPeriodPlan(
                periodId = profile.currentPeriod,
                availableAmount = GamePeriodRepository.getPeriod(profile.currentPeriod).pocketMoneyAmount
            )

            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val lastQuestDate = database.getSetting("last_quest_date", "")
            var questsToday = database.getSetting("quests_today_count", "0").toIntOrNull() ?: 0
            val streak = database.getSetting("quest_streak_days", "1").toIntOrNull() ?: 1

            if (lastQuestDate != todayStr) {
                questsToday = 0
                database.setSetting("last_quest_date", todayStr)
                database.setSetting("quests_today_count", "0")
            }

            val baseQuests = getBaseQuests().map { q ->
                val selectedOpt = completedQuests[q.id]
                if (selectedOpt != null) {
                    q.copy(isCompleted = true, selectedOptionIndex = selectedOpt)
                } else q
            }

            var realExpenses = database.loadRealExpenses()
            if (realExpenses.isEmpty()) {
                val seed = listOf(
                    RealExpenseItem(title = "Обед в школьной столовой", amountRub = 180, category = RealCategory.NEEDS, dateFormatted = "Сегодня, 13:15"),
                    RealExpenseItem(title = "Проездной на автобус", amountRub = 54, category = RealCategory.NEEDS, dateFormatted = "Сегодня, 08:30"),
                    RealExpenseItem(title = "Вкусный перекус / чипсы", amountRub = 120, category = RealCategory.WANTS, dateFormatted = "Вчера, 16:40"),
                    RealExpenseItem(title = "Отложено на накопительный счет", amountRub = 250, category = RealCategory.SAVINGS, dateFormatted = "Вчера, 18:00")
                )
                seed.forEach { database.saveRealExpense(it) }
                realExpenses = seed
            }

            val themeId = database.getSetting("app_theme", "cyber_blue")
            val playerAge = database.getSetting("player_age", "10").toIntOrNull() ?: 10
            val playerAvatar = database.getSetting("player_avatar", "fox_hero")
            val currentTheme = ru.finpet.app.ui.theme.AppTheme.fromId(themeId)

            val ownedRoomStr = database.getSetting("owned_room_items", "sh_wp_gray,sh_fl_wood")
            val ownedRoomSet = ownedRoomStr.split(",").filter { it.isNotBlank() }.toSet()

            val purchasedStr = database.getSetting("purchased_items", "sh_food_dry,sh_food_donut,sh_food_ramen,sh_wp_gray,sh_fl_wood")
            val purchasedSet = purchasedStr.split(",").filter { it.isNotBlank() }.toSet()
            val totalKeys = database.getSetting("total_keys", "4").toIntOrNull() ?: 4

            val eqWallpaper = database.getSetting("equipped_wallpaper", "sh_wp_gray")
            val eqFloor = database.getSetting("equipped_floor", "sh_fl_wood")
            val eqToy = database.getSetting("equipped_toy", "").takeIf { it.isNotBlank() }
            val eqWardrobe = database.getSetting("equipped_wardrobe", "").takeIf { it.isNotBlank() }
            val eqDesk = database.getSetting("equipped_desk", "").takeIf { it.isNotBlank() }
            val eqBed = database.getSetting("equipped_bed", "").takeIf { it.isNotBlank() }
            val eqLamp = database.getSetting("equipped_lamp", "").takeIf { it.isNotBlank() }
            val eqPoster = database.getSetting("equipped_poster", "").takeIf { it.isNotBlank() }

            val posWardrobe = parsePos(database.getSetting("pos_WARDROBE", ""), 0.16f, 0.58f)
            val rotWardrobe = database.getSetting("rot_WARDROBE", "0").toIntOrNull() ?: 0
            val posDesk = parsePos(database.getSetting("pos_DESK", ""), 0.64f, 0.68f)
            val rotDesk = database.getSetting("rot_DESK", "0").toIntOrNull() ?: 0
            val posBed = parsePos(database.getSetting("pos_BED", ""), 0.12f, 0.86f)
            val rotBed = database.getSetting("rot_BED", "0").toIntOrNull() ?: 0
            val posLamp = parsePos(database.getSetting("pos_LAMP", ""), 0.82f, 0.62f)
            val rotLamp = database.getSetting("rot_LAMP", "0").toIntOrNull() ?: 0
            val posPoster = parsePos(database.getSetting("pos_POSTER", ""), 0.66f, 0.08f)
            val isTutorialDone = database.getSetting("is_tutorial_completed", "0") == "1"

            val isRoomLightOff = database.getSetting("is_room_light_off", "0") == "1"
            val roomWeather = RoomWeather.fromId(database.getSetting("room_weather", "RAIN"))
            val bankDepositAmount = database.getSetting("bank_deposit_amount", "0").toIntOrNull() ?: 0
            val bankDepositPeriods = database.getSetting("bank_deposit_periods", "0").toIntOrNull() ?: 0
            val totalBankProfit = database.getSetting("total_bank_profit", "0").toIntOrNull() ?: 0
            val isMusicEnabled = database.getSetting("is_music_enabled", "0") == "1"

            if (isMusicEnabled) {
                ru.finpet.app.audio.SoundHapticManager.startAmbientMusic()
            }

            _gameState.value = GameState(
                playerName = profile.playerName,
                playerAge = playerAge,
                playerAvatar = playerAvatar,
                appTheme = currentTheme,
                pet = profile.pet,
                isOnboardingCompleted = profile.isOnboardingCompleted,
                isTutorialCompleted = isTutorialDone,
                currentPeriod = profile.currentPeriod,
                totalCoins = profile.totalCoins,
                totalKeys = totalKeys,
                budgetPlan = budget,
                goals = if (goalsList.isNotEmpty()) goalsList else getBaseGoals(),
                activeGoalId = selectedGoalId ?: goalsList.firstOrNull()?.id ?: "g_scooter",
                quests = baseQuests,
                questsCompletedToday = questsToday,
                dailyQuestLimit = 3,
                questStreakDays = streak,
                shopItems = getBaseShopItems(),
                transactions = transactions,
                realExpenses = realExpenses,
                parentTasks = if (parentTasks.isNotEmpty()) parentTasks else getBaseParentTasks(),
                isDemoMode = database.getSetting("demo_mode", "1") == "1",
                lastAdviceMessage = profile.lastAdvice,
                equippedWallpaper = eqWallpaper,
                equippedFloor = eqFloor,
                equippedToy = eqToy,
                equippedWardrobe = eqWardrobe,
                equippedDesk = eqDesk,
                equippedBed = eqBed,
                equippedLamp = eqLamp,
                equippedPoster = eqPoster,
                wardrobeX = posWardrobe.first,
                wardrobeY = posWardrobe.second,
                wardrobeRotation = rotWardrobe,
                deskX = posDesk.first,
                deskY = posDesk.second,
                deskRotation = rotDesk,
                bedX = posBed.first,
                bedY = posBed.second,
                bedRotation = rotBed,
                lampX = posLamp.first,
                lampY = posLamp.second,
                lampRotation = rotLamp,
                posterX = posPoster.first,
                posterY = posPoster.second,
                ownedRoomItemIds = ownedRoomSet,
                purchasedItemIds = purchasedSet,
                isRoomLightOff = isRoomLightOff,
                roomWeather = roomWeather,
                bankDepositAmount = bankDepositAmount,
                bankDepositPeriodsLeft = bankDepositPeriods,
                totalBankInterestEarned = totalBankProfit,
                isMusicEnabled = isMusicEnabled
            )
        } else {
            // Первая инициализация в базе данных
            persistCurrentState()
        }
    }

    private fun persistCurrentState() {
        val database = db ?: return
        val state = _gameState.value
        database.saveProfile(
            playerName = state.playerName,
            pet = state.pet,
            totalCoins = state.totalCoins,
            currentPeriod = state.currentPeriod,
            isOnboardingCompleted = state.isOnboardingCompleted,
            lastAdvice = state.lastAdviceMessage
        )
        database.saveBudget(state.budgetPlan)
        for (g in state.goals) {
            database.saveGoal(g, g.id == state.activeGoalId)
        }
        database.saveParentTasks(state.parentTasks)
        database.setSetting("demo_mode", if (state.isDemoMode) "1" else "0")
        database.setSetting("app_theme", state.appTheme.id)
        database.setSetting("player_age", state.playerAge.toString())
        database.setSetting("player_avatar", state.playerAvatar)
        database.setSetting("total_keys", state.totalKeys.toString())
        database.setSetting("owned_room_items", state.ownedRoomItemIds.joinToString(","))
        database.setSetting("purchased_items", state.purchasedItemIds.joinToString(","))
        database.setSetting("equipped_wallpaper", state.equippedWallpaper)
        database.setSetting("equipped_floor", state.equippedFloor)
        database.setSetting("equipped_toy", state.equippedToy ?: "")
        database.setSetting("equipped_wardrobe", state.equippedWardrobe ?: "")
        database.setSetting("equipped_desk", state.equippedDesk ?: "")
        database.setSetting("equipped_bed", state.equippedBed ?: "")
        database.setSetting("equipped_lamp", state.equippedLamp ?: "")
        database.setSetting("equipped_poster", state.equippedPoster ?: "")
        database.setSetting("is_tutorial_completed", if (state.isTutorialCompleted) "1" else "0")
        database.setSetting("is_room_light_off", if (state.isRoomLightOff) "1" else "0")
        database.setSetting("room_weather", state.roomWeather.name)
        database.setSetting("bank_deposit_amount", state.bankDepositAmount.toString())
        database.setSetting("bank_deposit_periods", state.bankDepositPeriodsLeft.toString())
        database.setSetting("total_bank_profit", state.totalBankInterestEarned.toString())
        database.setSetting("is_music_enabled", if (state.isMusicEnabled) "1" else "0")
        appContext?.let { ctx ->
            ru.finpet.app.widget.FinPetWidgetProvider.updateAllWidgets(ctx)
        }
    }

    private fun createInitialState(): GameState {
        val initialGoals = getBaseGoals()
        val initialQuests = getBaseQuests()
        val initialShop = getBaseShopItems()
        val initialTasks = getBaseParentTasks()

        val initialTx = listOf(
            FinTransaction(
                id = "tx_init_1",
                title = "Карманные деньги на период 1",
                amount = 40,
                envelope = EnvelopeType.NEEDS,
                isIncome = true,
                timestampFormatted = "Сегодня, 10:00",
                periodId = 1,
                note = "Стартовый бюджет от родителей для обучения"
            )
        )

        return GameState(
            playerName = "Юный финансист",
            pet = Pet(
                id = "pet_finny",
                name = "",
                type = PetType.FOX,
                color = PetColor.ORANGE,
                accessory = PetAccessory.NONE,
                level = 1,
                exp = 25,
                expToNextLevel = 100,
                hunger = 0.85f,
                happiness = 0.85f,
                care = 0.80f,
                energy = 0.95f,
                finScore = 75,
                evolutionStage = EvolutionStage.BABY,
                currentMood = PetMood.HAPPY,
                moodExplanation = "Твой питомец радостно виляет хвостиком! Бюджет на неделю открыт."
            ),
            isOnboardingCompleted = false,
            currentPeriod = 1,
            totalCoins = 40,
            budgetPlan = BudgetPeriodPlan(
                periodId = 1,
                availableAmount = 40,
                needsPlan = 20,
                wantsPlan = 12,
                savingsPlan = 8,
                needsFact = 0,
                wantsFact = 0,
                savingsFact = 0,
                isConfirmed = false
            ),
            goals = initialGoals,
            activeGoalId = "g_scooter",
            quests = initialQuests,
            shopItems = initialShop,
            transactions = initialTx,
            parentTasks = initialTasks,
            isDemoMode = true,
            lastAdviceMessage = "Привет! Давай составим наш первый план бюджета!"
        )
    }

    private fun getBaseGoals() = listOf(
        FinancialGoal("g_scooter", "Городской самокат", 150, 0, "🛴"),
        FinancialGoal("g_boardgame", "Развивающая настольная игра", 80, 0, "🎲"),
        FinancialGoal("g_aquapark", "Семейный визит в аквапарк", 200, 0, "🏊‍♂️")
    )

    private fun getBaseShopItems() = listOf(
        // ==========================================
        // 1. ЕДА (FOOD) — 15 предметов (дает энергию питомцу для квестов)
        // ==========================================
        ShopItem("sh_food_dry", "Хрустящий корм", 18, ShopCategory.FOOD, "🥣", hungerBoost = 0.40f, energyBoost = 0.45f, careBoost = 0.10f, isMandatory = true, tip = "Полезный сухой корм для сытости и заряда энергии ⚡"),
        ShopItem("sh_food_carrot", "Свежая морковка", 12, ShopCategory.FOOD, "🥕", hungerBoost = 0.25f, energyBoost = 0.30f, careBoost = 0.15f, isMandatory = true, tip = "Хрустящая сладкая морковка с витаминами 🥕", iconRes = ru.finpet.app.R.drawable.ic_item_carrot),
        ShopItem("sh_food_meat", "Мясное лакомство", 26, ShopCategory.FOOD, "🍖", hungerBoost = 0.35f, energyBoost = 0.40f, happinessBoost = 0.25f, isMandatory = false, tip = "Вкусные мясные палочки для бодрости 🍖"),
        ShopItem("sh_food_donut", "Ягодный пончик", 15, ShopCategory.FOOD, "🍩", happinessBoost = 0.30f, hungerBoost = 0.15f, energyBoost = 0.25f, isMandatory = false, tip = "Сладкий пончик с глазурью для отличного настроения 🍩"),
        ShopItem("sh_food_fish", "Рыбка лосось", 30, ShopCategory.FOOD, "🐟", hungerBoost = 0.45f, energyBoost = 0.50f, happinessBoost = 0.20f, isMandatory = false, tip = "Аппетитная рыбка — лучший источник сил и энергии 🐟", iconRes = ru.finpet.app.R.drawable.ic_item_fish),
        ShopItem("sh_food_milk", "Парное молочко", 16, ShopCategory.FOOD, "🥛", hungerBoost = 0.25f, energyBoost = 0.35f, careBoost = 0.20f, isMandatory = true, tip = "Свежее фермерское молочко для здоровья 🥛"),
        ShopItem("sh_food_ramen", "Сытный суп рамен", 32, ShopCategory.FOOD, "🍜", hungerBoost = 0.50f, energyBoost = 0.55f, happinessBoost = 0.30f, isMandatory = false, tip = "Горячий супчик с лапшой для супер-энергии 🍜"),
        ShopItem("sh_food_mochi", "Сладкие моти", 19, ShopCategory.FOOD, "🍡", hungerBoost = 0.25f, energyBoost = 0.30f, happinessBoost = 0.35f, isMandatory = false, tip = "Нежное японское пирожное 🍡"),
        ShopItem("sh_food_bento", "Обед бенто", 36, ShopCategory.FOOD, "🍱", hungerBoost = 0.60f, energyBoost = 0.60f, happinessBoost = 0.25f, isMandatory = true, tip = "Большой праздничный обед в коробочке 🍱"),
        ShopItem("sh_food_cheese", "Вкусный сыр", 20, ShopCategory.FOOD, "🧀", hungerBoost = 0.30f, energyBoost = 0.35f, careBoost = 0.15f, isMandatory = false, tip = "Ароматный кусочек сыра 🧀"),
        ShopItem("sh_food_strawberry", "Сладкая клубника", 14, ShopCategory.FOOD, "🍓", hungerBoost = 0.20f, energyBoost = 0.25f, happinessBoost = 0.25f, isMandatory = false, tip = "Спелые садовые ягодки 🍓"),
        ShopItem("sh_food_apple", "Сочное яблоко", 10, ShopCategory.FOOD, "🍏", hungerBoost = 0.20f, energyBoost = 0.25f, careBoost = 0.10f, isMandatory = true, tip = "Хрустящее зеленое яблочко 🍏"),
        ShopItem("sh_food_onigiri", "Рисовый онигири", 22, ShopCategory.FOOD, "🍙", hungerBoost = 0.35f, energyBoost = 0.40f, happinessBoost = 0.20f, isMandatory = true, tip = "Сытный треугольник из риса с рыбкой 🍙"),
        ShopItem("sh_food_bubble_tea", "Чай бабл-ти", 24, ShopCategory.FOOD, "🧋", happinessBoost = 0.40f, hungerBoost = 0.15f, energyBoost = 0.30f, isMandatory = false, tip = "Освежающий чай с шариками тапиоки 🧋"),
        ShopItem("sh_food_waffles", "Медовые вафли", 17, ShopCategory.FOOD, "🧇", hungerBoost = 0.25f, energyBoost = 0.30f, happinessBoost = 0.25f, isMandatory = false, tip = "Хрустящие вафельки с цветочным медом 🧇"),

        // ==========================================
        // 2. ГИГИЕНА (HYGIENE) — 13 предметов
        // ==========================================
        ShopItem("sh_hyg_shampoo", "Нежный шампунь", 22, ShopCategory.HYGIENE, "🧼", careBoost = 0.45f, happinessBoost = 0.15f, isMandatory = true, tip = "Ароматная пенка для чистой и блестящей шерстки", iconRes = ru.finpet.app.R.drawable.ic_item_soap),
        ShopItem("sh_hyg_brush", "Мягкая расческа", 18, ShopCategory.HYGIENE, "🪮", careBoost = 0.35f, happinessBoost = 0.20f, isMandatory = true, tip = "Удобная щеточка для приятного расчесывания", iconRes = ru.finpet.app.R.drawable.ic_item_brush),
        ShopItem("sh_hyg_vitamins", "Витаминки", 32, ShopCategory.HYGIENE, "💊", careBoost = 0.50f, energyBoost = 0.30f, isMandatory = true, tip = "Комплекс витаминов для крепкого иммунитета"),
        ShopItem("sh_hyg_wipes", "Влажные салфетки", 14, ShopCategory.HYGIENE, "🧻", careBoost = 0.30f, isMandatory = true, tip = "Салфетки для чистых лапок после прогулки"),
        ShopItem("sh_hyg_towel", "Пушистое полотенце", 20, ShopCategory.HYGIENE, "🧖", careBoost = 0.35f, happinessBoost = 0.10f, isMandatory = true, tip = "Мягкое полотенце после купания"),
        ShopItem("sh_hyg_bath", "Ванна с пеной", 38, ShopCategory.HYGIENE, "🛁", careBoost = 0.60f, happinessBoost = 0.40f, isMandatory = false, tip = "Теплая ванна с пузырьками для полного релакса"),
        ShopItem("sh_hyg_sonic_brush", "Зубная щеточка", 34, ShopCategory.HYGIENE, "🪥", careBoost = 0.45f, energyBoost = 0.15f, isMandatory = true, tip = "Щеточка для белоснежной улыбки"),
        ShopItem("sh_hyg_golden_comb", "Золотой гребешок", 42, ShopCategory.HYGIENE, "✨", careBoost = 0.55f, happinessBoost = 0.35f, isMandatory = false, tip = "Красивый гребень для королевской шерстки", iconRes = ru.finpet.app.R.drawable.ic_item_brush),
        ShopItem("sh_hyg_spray", "Спрей с алоэ", 16, ShopCategory.HYGIENE, "🩹", careBoost = 0.30f, isMandatory = true, tip = "Освежающий спрей для ухода"),
        ShopItem("sh_hyg_powder", "Лавандовая пудра", 24, ShopCategory.HYGIENE, "🌸", careBoost = 0.35f, happinessBoost = 0.25f, isMandatory = false, tip = "Нежная пудра с запахом цветов"),
        ShopItem("sh_hyg_clipper", "Когтерезка", 22, ShopCategory.HYGIENE, "✂️", careBoost = 0.35f, isMandatory = true, tip = "Аккуратный уход за коготками"),
        ShopItem("sh_hyg_perfume", "Детский парфюм", 36, ShopCategory.HYGIENE, "🧴", happinessBoost = 0.45f, careBoost = 0.25f, isMandatory = false, tip = "Сладкий аромат весенней вишни"),
        ShopItem("sh_hyg_drops", "Капли для глазок", 25, ShopCategory.HYGIENE, "💧", careBoost = 0.40f, isMandatory = true, tip = "Увлажняющие капли для ясного взгляда"),

        // ==========================================
        // 3. ОБОИ И ПОЛ (WALLPAPER) — 16 предметов
        // ==========================================
        ShopItem("sh_wp_gray", "Светлые обои", 0, ShopCategory.WALLPAPER, "🧱", roomSlotType = RoomSlotType.WALLPAPER, tip = "Базовые стильные обои для комнаты"),
        ShopItem("sh_wp_brick", "Кирпичная стена", 35, ShopCategory.WALLPAPER, "🧱", roomSlotType = RoomSlotType.WALLPAPER, happinessBoost = 0.25f, tip = "Модная кирпичная кладка в стиле лофт"),
        ShopItem("sh_wp_space", "Звездный космос", 45, ShopCategory.WALLPAPER, "🌌", roomSlotType = RoomSlotType.WALLPAPER, happinessBoost = 0.35f, tip = "Мерцающие звезды и далекие галактики"),
        ShopItem("sh_wp_clouds", "Нежные облака", 35, ShopCategory.WALLPAPER, "☁️", roomSlotType = RoomSlotType.WALLPAPER, happinessBoost = 0.25f, tip = "Воздушные пастельные облака"),
        ShopItem("sh_wp_cyber", "Неоновые волны", 50, ShopCategory.WALLPAPER, "🌐", roomSlotType = RoomSlotType.WALLPAPER, happinessBoost = 0.40f, tip = "Светящиеся линии в стиле киберпанк"),
        ShopItem("sh_wp_sunflower", "Поле подсолнухов", 40, ShopCategory.WALLPAPER, "🌻", roomSlotType = RoomSlotType.WALLPAPER, happinessBoost = 0.30f, tip = "Солнечные яркие цветы для хорошего настроения"),
        ShopItem("sh_wp_sakura", "Цветущая сакура", 48, ShopCategory.WALLPAPER, "🌸", roomSlotType = RoomSlotType.WALLPAPER, happinessBoost = 0.40f, tip = "Нежные розовые лепестки сакуры"),
        ShopItem("sh_wp_retro", "Горные вершины", 42, ShopCategory.WALLPAPER, "🏔️", roomSlotType = RoomSlotType.WALLPAPER, happinessBoost = 0.35f, tip = "Красивый закат над горным хребтом"),
        ShopItem("sh_wp_forest", "Зеленый лес", 46, ShopCategory.WALLPAPER, "🌲", roomSlotType = RoomSlotType.WALLPAPER, happinessBoost = 0.35f, tip = "Уютные лесные мотивы и свежесть"),
        ShopItem("sh_wp_city", "Ночной город", 52, ShopCategory.WALLPAPER, "🌆", roomSlotType = RoomSlotType.WALLPAPER, happinessBoost = 0.45f, tip = "Огни вечернего мегаполиса"),
        ShopItem("sh_fl_wood", "Дубовый паркет", 0, ShopCategory.WALLPAPER, "🪵", roomSlotType = RoomSlotType.FLOOR, tip = "Классический теплый деревянный пол"),
        ShopItem("sh_fl_dark", "Темный ламинат", 28, ShopCategory.WALLPAPER, "🪵", roomSlotType = RoomSlotType.FLOOR, happinessBoost = 0.20f, tip = "Благородное темное дерево"),
        ShopItem("sh_fl_tatami", "Бамбуковый коврик", 36, ShopCategory.WALLPAPER, "🎋", roomSlotType = RoomSlotType.FLOOR, happinessBoost = 0.25f, tip = "Натуральное плетение из бамбука"),
        ShopItem("sh_fl_marble", "Белый мрамор", 42, ShopCategory.WALLPAPER, "🏛️", roomSlotType = RoomSlotType.FLOOR, happinessBoost = 0.30f, tip = "Гладкий и сияющий мраморный пол"),
        ShopItem("sh_fl_neon", "Неоновый пол", 48, ShopCategory.WALLPAPER, "🔮", roomSlotType = RoomSlotType.FLOOR, happinessBoost = 0.35f, tip = "Светящиеся плиты с мягким свечением"),
        ShopItem("sh_fl_carpet", "Мягкий ковер", 38, ShopCategory.WALLPAPER, "🧶", roomSlotType = RoomSlotType.FLOOR, happinessBoost = 0.30f, tip = "Пушистый теплый ковер для уюта лапок"),

        // ==========================================
        // 4. ДЕКОР И ПОСТЕРЫ НА СТЕНУ
        // ==========================================
        ShopItem("sh_dec_cert", "Диплом финансиста", 30, ShopCategory.WALLPAPER, "📜", roomSlotType = RoomSlotType.POSTER, happinessBoost = 0.30f, isMandatory = false, tip = "Красивый диплом в рамке на стену"),
        ShopItem("sh_dec_poster", "Арт-картина", 25, ShopCategory.WALLPAPER, "🖼️", roomSlotType = RoomSlotType.POSTER, happinessBoost = 0.35f, isMandatory = false, tip = "Яркий постер с любимым героем"),
        ShopItem("sh_dec_gold_medal", "Золотая медаль", 35, ShopCategory.WALLPAPER, "🥇", roomSlotType = RoomSlotType.POSTER, happinessBoost = 0.40f, isMandatory = false, tip = "Награда за успехи в накоплениях"),
        ShopItem("sh_dec_star_award", "Звезда почета", 40, ShopCategory.WALLPAPER, "⭐", roomSlotType = RoomSlotType.POSTER, happinessBoost = 0.45f, isMandatory = false, tip = "Сверкающая звездочка для стены"),

        // ==========================================
        // 5. УКРАШЕНИЯ (ACCESSORIES) — 16 предметов
        // ==========================================
        ShopItem("sh_acc_collar", "Ошейник с кулоном", 28, ShopCategory.ACCESSORIES, "🏷️", happinessBoost = 0.30f, isMandatory = false, tip = "Стильный ошейник с именным кулоном"),
        ShopItem("sh_acc_glasses", "Солнечные очки", 34, ShopCategory.ACCESSORIES, "🕶️", happinessBoost = 0.40f, isMandatory = false, tip = "Модные темные очки от солнца"),
        ShopItem("sh_acc_cap", "Крутая кепка", 32, ShopCategory.ACCESSORIES, "🧢", happinessBoost = 0.35f, isMandatory = false, tip = "Яркая бейсболка с козырьком"),
        ShopItem("sh_acc_scarf", "Теплый шарфик", 26, ShopCategory.ACCESSORIES, "🧣", happinessBoost = 0.30f, isMandatory = false, tip = "Вязаный уютный шарфик"),
        ShopItem("sh_acc_badge", "Орден чемпиона", 50, ShopCategory.ACCESSORIES, "⭐", happinessBoost = 0.60f, isMandatory = false, tip = "Блестящий значок за финансовые победы"),
        ShopItem("sh_acc_crown", "Золотая корона", 80, ShopCategory.ACCESSORIES, "👑", happinessBoost = 0.80f, isMandatory = false, tip = "Настоящая корона для короля накоплений"),
        ShopItem("sh_royal_mantle", "Королевская мантия", 80, ShopCategory.ACCESSORIES, "🧣", happinessBoost = 0.80f, isMandatory = false, tip = "Бархатная мантия благородного цвета"),
        ShopItem("sh_royal_scepter", "Золотой скипетр", 80, ShopCategory.ACCESSORIES, "🪄", happinessBoost = 0.80f, isMandatory = false, tip = "Символ мастерства и мудрости"),
        ShopItem("sh_acc_visor", "Кибер-очки", 58, ShopCategory.ACCESSORIES, "🥽", happinessBoost = 0.65f, isMandatory = false, tip = "Футуристические светящиеся очки"),
        ShopItem("sh_acc_headband", "Спортивная повязка", 30, ShopCategory.ACCESSORIES, "🥷", happinessBoost = 0.35f, isMandatory = false, tip = "Удобная повязка для тренировок"),
        ShopItem("sh_acc_cat_headphones", "Музыкальные наушники", 52, ShopCategory.ACCESSORIES, "🎧", happinessBoost = 0.55f, isMandatory = false, tip = "Наушники с мягкими амбушюрами"),
        ShopItem("sh_acc_wizard_hat", "Шляпа магистра", 48, ShopCategory.ACCESSORIES, "🧙", happinessBoost = 0.50f, isMandatory = false, tip = "Шляпа знатока финансов"),
        ShopItem("sh_acc_bowtie", "Галстук-бабочка", 24, ShopCategory.ACCESSORIES, "🎀", happinessBoost = 0.30f, isMandatory = false, tip = "Праздничная нарядная бабочка"),
        ShopItem("sh_acc_emerald_gem", "Изумрудный кулон", 65, ShopCategory.ACCESSORIES, "💎", happinessBoost = 0.70f, isMandatory = false, tip = "Красивый сверкающий кулон на цепочке"),
        ShopItem("sh_acc_flower_wreath", "Цветочный венок", 36, ShopCategory.ACCESSORIES, "🌸", happinessBoost = 0.40f, isMandatory = false, tip = "Весенний венок из свежих цветов"),
        ShopItem("sh_acc_pocket_watch", "Карманные часики", 60, ShopCategory.ACCESSORIES, "⏱️", happinessBoost = 0.60f, isMandatory = false, tip = "Золотые карманные часы на цепочке")
    )

    private fun getBaseQuests(): List<FinancialQuest> = QuestsBank.getAll54Quests()

    private fun getBaseParentTasks() = listOf(
        ParentTask("pt_1", "Убрать свою комнату и книги", "Разложить учебники и игрушки по местам", 35, TaskStatus.PENDING, "🧹"),
        ParentTask("pt_2", "Прочитать главу книги", "Рассказать родителям, о чем прочитанная глава", 30, TaskStatus.PENDING, "📖"),
        ParentTask("pt_3", "Помочь разобрать покупки", "Разложить продукты по правилам и проверить чек", 40, TaskStatus.COMPLETED, "🛒")
    )

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd.MM, HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    // --- Сквозной сценарий Приложения А: шаги 1–3 (Онбординг и создание питомца) ---

    fun completeOnboardingAndCreatePet(
        playerName: String,
        playerAge: Int = 10,
        playerAvatar: String = "fox_hero",
        appTheme: ru.finpet.app.ui.theme.AppTheme = ru.finpet.app.ui.theme.AppTheme.CYBER_BLUE,
        petName: String,
        petType: PetType,
        petColor: PetColor,
        petAccessory: PetAccessory
    ) {
        _gameState.update { state ->
            val updatedPet = state.pet.copy(
                name = petName.ifBlank { "Питомец" },
                type = petType,
                color = petColor,
                accessory = petAccessory,
                currentMood = PetMood.HAPPY,
                moodExplanation = "Ура! ${petName.ifBlank { "Питомец" }} готов к финансовым приключениям!"
            )
            state.copy(
                playerName = playerName.ifBlank { "Юный финансист" },
                playerAge = playerAge,
                playerAvatar = playerAvatar,
                appTheme = appTheme,
                pet = updatedPet,
                isOnboardingCompleted = true,
                lastAdviceMessage = "План бюджета составлен. Начнем знакомство с правилом 50/30/20!"
            )
        }
        persistCurrentState()
    }

    fun updateAppTheme(theme: ru.finpet.app.ui.theme.AppTheme) {
        _gameState.update { it.copy(appTheme = theme) }
        persistCurrentState()
    }

    fun updatePlayerProfile(name: String, age: Int, avatar: String) {
        _gameState.update { it.copy(playerName = name, playerAge = age, playerAvatar = avatar) }
        persistCurrentState()
    }

    // --- Шаг 5: Планирование бюджета ---

    fun updateBudgetPlan(needs: Int, wants: Int, savings: Int) {
        _gameState.update { state ->
            val currentPlan = state.budgetPlan
            val newPlan = currentPlan.copy(
                needsPlan = needs,
                wantsPlan = wants,
                savingsPlan = savings
            )
            state.copy(budgetPlan = newPlan)
        }
    }

    fun confirmBudgetPlan() {
        _gameState.update { state ->
            val plan = state.budgetPlan
            if (plan.unallocatedAmount < 0) {
                return@update state.copy(lastAdviceMessage = "Сумма расходов превышает бюджет! Уменьши одну из категорий.")
            }
            val confirmedPlan = plan.copy(isConfirmed = true)
            state.copy(
                budgetPlan = confirmedPlan,
                lastAdviceMessage = "План бюджета на период утвержден! Теперь совершай покупки с умом."
            )
        }
        persistCurrentState()
    }

    fun requestBuyItem(item: ShopItem) {
        val state = _gameState.value
        if (state.totalCoins < item.price) {
            val needed = item.price - state.totalCoins
            _gameState.update {
                it.copy(activeDialog = GameDialog.InsufficientFunds(item.name, item.price, it.totalCoins, needed))
            }
            return
        }
        _gameState.update {
            it.copy(activeDialog = GameDialog.ConfirmPurchase(item))
        }
    }

    fun confirmPurchase(item: ShopItem) {
        _gameState.update { state ->
            val cost = item.price
            if (state.totalCoins < cost) return@update state

            val isNeed = item.isMandatory
            val newTotalCoins = state.totalCoins - cost

            // Обновляем факт расходов в плане бюджета
            val currentPlan = state.budgetPlan
            val updatedPlan = if (isNeed) {
                currentPlan.copy(needsFact = currentPlan.needsFact + cost)
            } else {
                currentPlan.copy(wantsFact = currentPlan.wantsFact + cost)
            }

            // Влияние на питомца
            val newHunger = (state.pet.hunger + item.hungerBoost).coerceIn(0f, 1f)
            val newHappiness = (state.pet.happiness + item.happinessBoost).coerceIn(0f, 1f)
            val newCare = (state.pet.care + item.careBoost).coerceIn(0f, 1f)
            val newEnergy = (state.pet.energy + item.energyBoost).coerceIn(0f, 1f)
            val gainedExp = if (isNeed) 20 else 10
            val newExp = state.pet.exp + gainedExp
            val leveledUp = newExp >= state.pet.expToNextLevel
            val finalLevel = if (leveledUp) state.pet.level + 1 else state.pet.level
            val finalExp = if (leveledUp) newExp - state.pet.expToNextLevel else newExp

            val updatedPet = state.pet.copy(
                hunger = newHunger,
                happiness = newHappiness,
                care = newCare,
                energy = newEnergy,
                level = finalLevel,
                exp = finalExp,
                currentMood = PetMood.HAPPY,
                moodExplanation = if (isNeed) "${state.pet.name.ifBlank { "Питомец" }} очень благодарен за заботу и сытный обед!" else "${state.pet.name.ifBlank { "Питомец" }} радуется полезной обновке!"
            )

            val tx = FinTransaction(
                id = UUID.randomUUID().toString(),
                title = "Покупка: ${item.name}",
                amount = cost,
                envelope = if (isNeed) EnvelopeType.NEEDS else EnvelopeType.WANTS,
                isIncome = false,
                timestampFormatted = getCurrentDateString(),
                periodId = state.currentPeriod,
                note = if (isNeed) "Обязательный расход" else "Необязательный расход"
            )

            db?.addTransaction(tx)

            // Если куплен предмет интерьера комнаты, добавляем в инвентарь комнаты и сразу экипируем
            val newOwnedRoom = if (item.roomSlotType != null) state.ownedRoomItemIds + item.id else state.ownedRoomItemIds
            var s = state.copy(
                totalCoins = newTotalCoins,
                budgetPlan = updatedPlan,
                pet = updatedPet,
                transactions = listOf(tx) + state.transactions,
                ownedRoomItemIds = newOwnedRoom,
                purchasedItemIds = state.purchasedItemIds + item.id,
                activeDialog = GameDialog.DeliveryUnboxing(item),
                lastAdviceMessage = "Куплено: ${item.name}! Доставка спешит в комнату. +$gainedExp опыта."
            )

            // Авто-экипировка для наглядности
            when (item.roomSlotType) {
                RoomSlotType.WALLPAPER -> s = s.copy(equippedWallpaper = item.id)
                RoomSlotType.FLOOR -> s = s.copy(equippedFloor = item.id)
                RoomSlotType.TOY -> s = s.copy(equippedToy = item.id)
                RoomSlotType.WARDROBE -> s = s.copy(equippedWardrobe = item.id)
                RoomSlotType.DESK -> s = s.copy(equippedDesk = item.id)
                RoomSlotType.BED -> s = s.copy(equippedBed = item.id)
                RoomSlotType.LAMP -> s = s.copy(equippedLamp = item.id)
                RoomSlotType.POSTER -> s = s.copy(equippedPoster = item.id)
                null -> {
                    val acc = getAccessoryForItemId(item.id)
                    if (acc != null) {
                        s = s.copy(pet = s.pet.copy(accessory = acc))
                    }
                }
            }
            s
        }
        persistCurrentState()
    }

    fun equipPetAccessory(accessory: PetAccessory) {
        _gameState.update { s ->
            val newAcc = if (s.pet.accessory == accessory) PetAccessory.NONE else accessory
            s.copy(
                pet = s.pet.copy(accessory = newAcc),
                lastAdviceMessage = if (newAcc != PetAccessory.NONE) {
                    "${s.pet.name.ifBlank { "Питомец" }} надел: ${newAcc.title}!"
                } else {
                    "${s.pet.name.ifBlank { "Питомец" }} снял аксессуар."
                }
            )
        }
        persistCurrentState()
    }

    fun getAccessoryForItemId(itemId: String): PetAccessory? {
        return when (itemId) {
            "sh_acc_glasses" -> PetAccessory.GLASSES
            "sh_acc_cap" -> PetAccessory.CAP
            "sh_acc_scarf" -> PetAccessory.SCARF
            "sh_acc_bowtie", "sh_acc_bow" -> PetAccessory.BOW
            "sh_acc_collar" -> PetAccessory.COLLAR
            "sh_acc_crown" -> PetAccessory.CROWN
            "sh_royal_mantle" -> PetAccessory.ROYAL_MANTLE
            "sh_royal_scepter" -> PetAccessory.ROYAL_SCEPTER
            "sh_acc_visor" -> PetAccessory.VISOR
            "sh_acc_headband" -> PetAccessory.HEADBAND
            "sh_acc_cat_headphones" -> PetAccessory.HEADPHONES
            "sh_acc_wizard_hat" -> PetAccessory.WIZARD_HAT
            "sh_acc_badge" -> PetAccessory.BADGE
            "sh_acc_emerald_gem" -> PetAccessory.EMERALD_GEM
            "sh_acc_flower_wreath" -> PetAccessory.FLOWER_WREATH
            "sh_acc_pocket_watch" -> PetAccessory.POCKET_WATCH
            else -> null
        }
    }

    fun openRarityChest(): Boolean {
        val state = _gameState.value
        val cost = 40
        if (state.totalCoins < cost) return false

        val nextRoyalId = state.royalSkinIds.find { it !in state.purchasedItemIds } ?: return false
        val rareItem = getBaseShopItems().find { it.id == nextRoyalId } ?: ShopItem(
            id = nextRoyalId,
            name = "Королевский предмет",
            basePrice = cost,
            category = ShopCategory.ACCESSORIES,
            icon = "👑",
            happinessBoost = 0.80f,
            isMandatory = false,
            tip = "Эксклюзивная королевская награда из сундука!"
        )

        _gameState.update { s ->
            val updatedPurchased = s.purchasedItemIds + rareItem.id
            val allCollected = s.royalSkinIds.all { it in updatedPurchased }
            val acc = getAccessoryForItemId(rareItem.id)
            val updatedPet = if (acc != null) s.pet.copy(accessory = acc) else s.pet
            s.copy(
                totalCoins = (s.totalCoins - cost).coerceAtLeast(0),
                purchasedItemIds = updatedPurchased,
                pet = updatedPet,
                activeDialog = GameDialog.DeliveryUnboxing(rareItem),
                lastAdviceMessage = if (allCollected) {
                    "👑 Ура! Все три королевских скина получены! Твой питомец — истинный монарх!"
                } else {
                    "🎉 Из Королевского сундука получена ${rareItem.name}!"
                }
            )
        }
        persistCurrentState()
        return true
    }

    fun selectGoal(goalId: String) {
        _gameState.update { it.copy(activeGoalId = goalId) }
        persistCurrentState()
    }

    fun createNewGoal(title: String, targetAmount: Int, icon: String) {
        val newGoal = FinancialGoal(
            id = "g_custom_${System.currentTimeMillis()}",
            title = title.ifBlank { "Моя мечта" },
            targetAmount = targetAmount.coerceAtLeast(10),
            currentAmount = 0,
            icon = icon.ifBlank { "🎯" },
            isCustom = true
        )
        _gameState.update { state ->
            state.copy(
                goals = state.goals + newGoal,
                activeGoalId = newGoal.id,
                lastAdviceMessage = "Создана новая финансовая цель: ${newGoal.title}!"
            )
        }
        persistCurrentState()
    }

    fun depositToActiveGoal(amount: Int) {
        _gameState.update { state ->
            if (amount <= 0 || state.totalCoins < amount) {
                return@update state.copy(lastAdviceMessage = "Недостаточно монет для пополнения копилки!")
            }
            val activeGoal = state.activeGoal ?: return@update state
            val newGoalAmount = activeGoal.currentAmount + amount
            val isGoalAchieved = newGoalAmount >= activeGoal.targetAmount
            val updatedGoal = activeGoal.copy(
                currentAmount = newGoalAmount,
                isAchieved = isGoalAchieved
            )

            val updatedGoals = state.goals.map { if (it.id == activeGoal.id) updatedGoal else it }
            val newTotalCoins = state.totalCoins - amount
            val updatedBudget = state.budgetPlan.copy(savingsFact = state.budgetPlan.savingsFact + amount)

            val tx = FinTransaction(
                id = UUID.randomUUID().toString(),
                title = "В копилку: ${activeGoal.title}",
                amount = amount,
                envelope = EnvelopeType.SAVINGS,
                isIncome = false,
                timestampFormatted = getCurrentDateString(),
                periodId = state.currentPeriod,
                note = "Пополнение финансовой цели"
            )
            db?.addTransaction(tx)

            val updatedPet = state.pet.copy(
                happiness = (state.pet.happiness + 0.15f).coerceIn(0f, 1f),
                exp = state.pet.exp + 25,
                moodExplanation = "Твой питомец видит, как растет копилка, и гордится твоей дисциплиной!"
            )

            state.copy(
                totalCoins = newTotalCoins,
                goals = updatedGoals,
                budgetPlan = updatedBudget,
                pet = updatedPet,
                transactions = listOf(tx) + state.transactions,
                lastAdviceMessage = "В копилку отложено $amount монет! Мечта всё ближе.",
                activeDialog = if (isGoalAchieved) GameDialog.GoalAchieved(updatedGoal) else null
            )
        }
        persistCurrentState()
    }

    fun requestWithdrawFromGoal(amount: Int) {
        val state = _gameState.value
        val goal = state.activeGoal ?: return
        if (amount <= 0 || goal.currentAmount < amount) return

        val newRemaining = goal.remainingAmount + amount
        val avgSavings = 25
        val delayPeriods = (amount + avgSavings - 1) / avgSavings

        _gameState.update {
            it.copy(
                activeDialog = GameDialog.ConfirmWithdrawal(
                    goal = goal,
                    withdrawAmount = amount,
                    newRemaining = newRemaining,
                    delayPeriods = delayPeriods.coerceAtLeast(1)
                )
            )
        }
    }

    fun confirmWithdrawal(goalId: String, amount: Int) {
        _gameState.update { state ->
            val goal = state.goals.find { it.id == goalId } ?: return@update state
            val newAmount = (goal.currentAmount - amount).coerceAtLeast(0)
            val updatedGoal = goal.copy(currentAmount = newAmount, isAchieved = false)
            val updatedGoals = state.goals.map { if (it.id == goalId) updatedGoal else it }
            val newTotalCoins = state.totalCoins + amount

            val tx = FinTransaction(
                id = UUID.randomUUID().toString(),
                title = "Снятие из копилки: ${goal.title}",
                amount = amount,
                envelope = EnvelopeType.SAVINGS,
                isIncome = true,
                timestampFormatted = getCurrentDateString(),
                periodId = state.currentPeriod,
                note = "Снятие средств на текущие нужды"
            )
            db?.addTransaction(tx)

            state.copy(
                totalCoins = newTotalCoins,
                goals = updatedGoals,
                transactions = listOf(tx) + state.transactions,
                activeDialog = null,
                lastAdviceMessage = "Монеты вернулись на баланс. Но помни: регулярность приближает мечту!"
            )
        }
        persistCurrentState()
    }

    fun addMiniGameReward(coinsEarned: Int) {
        if (coinsEarned <= 0) return
        _gameState.update { state ->
            val newTotalCoins = state.totalCoins + coinsEarned
            val newExp = state.pet.exp + (coinsEarned * 2)
            val newHappiness = (state.pet.happiness + 0.15f).coerceIn(0f, 1f)
            val updatedPet = state.pet.copy(
                exp = newExp,
                happiness = newHappiness,
                moodExplanation = "Питомец в восторге от твоих успехов в мини-игре! Заработано ${FormatUtils.formatCoins(coinsEarned)}."
            )

            val tx = FinTransaction(
                id = UUID.randomUUID().toString(),
                title = "Награда: Мини-игра 'Нужно vs Хочу'",
                amount = coinsEarned,
                envelope = EnvelopeType.SAVINGS,
                isIncome = true,
                timestampFormatted = getCurrentDateString(),
                periodId = state.currentPeriod,
                note = "Победа в обучающей мини-игре"
            )
            db?.addTransaction(tx)

            state.copy(
                totalCoins = newTotalCoins,
                pet = updatedPet,
                transactions = listOf(tx) + state.transactions,
                lastAdviceMessage = "Отличная игра! Заработано $coinsEarned монет за правильную сортировку трат."
            )
        }
        persistCurrentState()
    }

    fun answerQuest(questId: String, optionIndex: Int) {
        val currentState = _gameState.value
        val quest = currentState.quests.find { it.id == questId } ?: return

        // 1. Проверка последовательного открытия: квест N доступен, только если квест N-1 уже решен
        if (quest.orderIndex > 1) {
            val prevQuest = currentState.quests.find { it.orderIndex == quest.orderIndex - 1 }
            if (prevQuest != null && !prevQuest.isCompleted) {
                ru.finpet.app.audio.SoundHapticManager.performLockHaptic()
                _gameState.update {
                    it.copy(lastAdviceMessage = "Сначала пройди квест #${prevQuest.orderIndex}: ${prevQuest.title}!")
                }
                return
            }
        }

        // 2. Если квест уже пройден - открываем режим повторения и разбора
        if (quest.isCompleted) {
            val opt = quest.options.getOrNull(quest.selectedOptionIndex ?: optionIndex) ?: quest.options.first()
            _gameState.update {
                it.copy(activeDialog = GameDialog.QuestResult(quest, opt))
            }
            return
        }

        if (optionIndex !in quest.options.indices) return
        val selectedOption = quest.options[optionIndex]

        val updatedQuest = quest.copy(
            isCompleted = true,
            selectedOptionIndex = optionIndex
        )
        val updatedQuests = currentState.quests.map { if (it.id == questId) updatedQuest else it }

        val newQuestsToday = currentState.questsCompletedToday + 1
        val isDailyTargetAchieved = newQuestsToday >= currentState.dailyQuestLimit
        val newStreak = if (isDailyTargetAchieved) currentState.questStreakDays + 1 else currentState.questStreakDays

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db?.setSetting("last_quest_date", todayStr)
        db?.setSetting("quests_today_count", newQuestsToday.toString())
        db?.setSetting("quest_streak_days", newStreak.toString())

        if (isDailyTargetAchieved) {
            ru.finpet.app.audio.SoundHapticManager.performStreakHaptic()
        } else {
            ru.finpet.app.audio.SoundHapticManager.performSuccessHaptic()
        }

        val reward = selectedOption.coinReward
        val newTotalCoins = currentState.totalCoins + reward
        val newFinScore = (currentState.pet.finScore + selectedOption.finScoreDelta).coerceIn(0, 100)

        val tx = FinTransaction(
            id = UUID.randomUUID().toString(),
            title = "Награда за квест #${quest.orderIndex}: ${quest.title}",
            amount = reward,
            envelope = EnvelopeType.NEEDS,
            isIncome = true,
            timestampFormatted = getCurrentDateString(),
            periodId = currentState.currentPeriod,
            note = "Доход за финансовое решение"
        )
        db?.addTransaction(tx)
        db?.saveQuestCompletion(questId, optionIndex, reward, getCurrentDateString())

        val newEnergy = (currentState.pet.energy - 0.20f).coerceIn(0.05f, 1f)
        val newHunger = (currentState.pet.hunger - 0.15f).coerceIn(0.05f, 1f)
        val newMood = when {
            newEnergy <= 0.2f -> PetMood.TIRED
            newHunger <= 0.3f -> PetMood.HUNGRY
            else -> PetMood.HAPPY
        }

        val updatedPet = currentState.pet.copy(
            finScore = newFinScore,
            exp = currentState.pet.exp + 30,
            energy = newEnergy,
            hunger = newHunger,
            currentMood = newMood,
            moodExplanation = if (newEnergy <= 0.25f || newHunger <= 0.3f) {
                "${currentState.pet.name} устал и хочет кушать после решения заданий. Покорми его 🥣"
            } else {
                "Твой питомец узнал новое финансовое правило!"
            }
        )

        _gameState.update { state ->
            state.copy(
                totalCoins = newTotalCoins,
                quests = updatedQuests,
                questsCompletedToday = newQuestsToday,
                questStreakDays = newStreak,
                pet = updatedPet,
                transactions = listOf(tx) + state.transactions,
                activeDialog = null,
                lastAdviceMessage = selectedOption.feedbackExplanation
            )
        }
        persistCurrentState()
    }

    fun resetDailyQuestLimit() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db?.setSetting("last_quest_date", todayStr)
        db?.setSetting("quests_today_count", "0")
        _gameState.update { it.copy(questsCompletedToday = 0) }
    }

    fun advanceToNextPeriod() {
        _gameState.update { state ->
            val nextPeriodNumber = (state.currentPeriod % 5) + 1
            val nextPeriodInfo = GamePeriodRepository.getPeriod(nextPeriodNumber)
            val pocketMoney = nextPeriodInfo.pocketMoneyAmount

            // Оценка выполнения плана бюджета
            val plan = state.budgetPlan
            val isBudgetRespected = plan.isSuccessful

            val earnedExp = if (isBudgetRespected) 60 else 30
            val newExp = state.pet.exp + earnedExp
            val newLevel = state.pet.level + 1

            // Эволюция питомца
            val newStage = when {
                nextPeriodNumber >= 5 || newLevel >= 5 -> EvolutionStage.MASTER
                nextPeriodNumber >= 3 || newLevel >= 3 -> EvolutionStage.TEEN
                else -> EvolutionStage.BABY
            }

            val updatedPet = state.pet.copy(
                level = newLevel,
                exp = newExp % 100,
                evolutionStage = newStage,
                hunger = 0.85f,
                happiness = if (isBudgetRespected) 0.95f else 0.75f,
                care = 0.85f,
                energy = 1.0f,
                currentMood = PetMood.HAPPY,
                moodExplanation = if (isBudgetRespected) {
                    "Ты отлично выполнил план прошлого периода! Твой питомец вырос до стадии: ${newStage.title}!"
                } else {
                    "Мы готовы к новому периоду! Давай постараемся точнее следовать плану."
                }
            )

            val newPlan = BudgetPeriodPlan(
                periodId = nextPeriodNumber,
                availableAmount = pocketMoney,
                needsPlan = (pocketMoney * 0.5).toInt(),
                wantsPlan = (pocketMoney * 0.3).toInt(),
                savingsPlan = (pocketMoney * 0.2).toInt(),
                needsFact = 0,
                wantsFact = 0,
                savingsFact = 0,
                isConfirmed = false
            )

            // Обработка банковского вклада под сложный процент (+20% в период)
            var newDepositAmount = state.bankDepositAmount
            var newDepositPeriods = state.bankDepositPeriodsLeft
            var bonusCoinsFromDeposit = 0
            var interestEarnedThisPeriod = 0
            var depositTx: FinTransaction? = null
            var depositMessageExtra = ""

            if (newDepositAmount > 0 && newDepositPeriods > 0) {
                val interest = (newDepositAmount * state.bankDepositRate).toInt().coerceAtLeast(1)
                interestEarnedThisPeriod = interest
                val accumulated = newDepositAmount + interest
                newDepositPeriods -= 1

                if (newDepositPeriods <= 0) {
                    bonusCoinsFromDeposit = accumulated
                    newDepositAmount = 0
                    depositTx = FinTransaction(
                        id = UUID.randomUUID().toString(),
                        title = "Выплата вклада с процентами 🏦",
                        amount = accumulated,
                        envelope = EnvelopeType.SAVINGS,
                        isIncome = true,
                        timestampFormatted = getCurrentDateString(),
                        periodId = nextPeriodNumber,
                        note = "Успешное закрытие депозита: тело + сложный процент (+$interest 🪙 прибыли)"
                    )
                    db?.addTransaction(depositTx)
                    depositMessageExtra = "\n\n🏦 Банковский вклад успешно закрыт! Выплачено $accumulated монет (включая чистый доход +$interest 🪙 от сложного процента)!"
                } else {
                    newDepositAmount = accumulated
                    depositMessageExtra = "\n\n📈 Банковский вклад вырос на +$interest монет за счет сложного процента (теперь на вкладе: $newDepositAmount 🪙, остался 1 период)."
                }
            }

            val summaryText = if (isBudgetRespected) {
                "Ура! План периода ${state.currentPeriod} успешно выполнен! Обязательные расходы покрыты, копилка пополнена. Питомец подрос и стал мудрее!$depositMessageExtra"
            } else {
                "Период ${state.currentPeriod} завершен. Появились непредвиденные траты, но это отличный урок для планирования следующего бюджета!$depositMessageExtra"
            }

            val updatedTransactions = buildList {
                if (depositTx != null) add(depositTx)
                addAll(state.transactions)
            }

            state.copy(
                currentPeriod = nextPeriodNumber,
                totalCoins = state.totalCoins + bonusCoinsFromDeposit,
                bankDepositAmount = newDepositAmount,
                bankDepositPeriodsLeft = newDepositPeriods,
                totalBankInterestEarned = state.totalBankInterestEarned + interestEarnedThisPeriod,
                budgetPlan = newPlan,
                pet = updatedPet,
                isRoomLightOff = false, // с наступлением нового периода наступает утро
                transactions = updatedTransactions,
                activeDialog = GameDialog.PeriodCompleted(state.currentPeriod, summaryText, earnedExp, bonusCoinsFromDeposit),
                lastAdviceMessage = "Начался период $nextPeriodNumber: ${nextPeriodInfo.title}!" +
                        if (bonusCoinsFromDeposit > 0) " Выплата по вкладу: +$bonusCoinsFromDeposit 🪙!" else ""
            )
        }
        persistCurrentState()
    }

    fun setPeriod(periodNumber: Int) {
        val targetPeriod = periodNumber.coerceIn(1, 5)
        val periodInfo = GamePeriodRepository.getPeriod(targetPeriod)
        val pocketMoney = periodInfo.pocketMoneyAmount

        val newStage = when {
            targetPeriod >= 5 -> EvolutionStage.MASTER
            targetPeriod >= 3 -> EvolutionStage.TEEN
            else -> EvolutionStage.BABY
        }

        val completedQuestsThreshold = (targetPeriod - 1) * 2

        _gameState.update { state ->
            val updatedQuests = state.quests.map { q ->
                if (q.orderIndex <= completedQuestsThreshold) {
                    q.copy(isCompleted = true, selectedOptionIndex = 0)
                } else {
                    q.copy(isCompleted = false, selectedOptionIndex = null)
                }
            }

            val updatedPet = state.pet.copy(
                level = targetPeriod,
                evolutionStage = newStage,
                energy = 1.0f,
                hunger = 0.85f,
                happiness = 0.90f,
                currentMood = PetMood.HAPPY,
                finScore = (50 + targetPeriod * 8).coerceIn(50, 95),
                moodExplanation = "Период $targetPeriod: ${periodInfo.title} (${newStage.title})."
            )

            val newPlan = BudgetPeriodPlan(
                periodId = targetPeriod,
                availableAmount = pocketMoney,
                needsPlan = (pocketMoney * 0.5).toInt(),
                wantsPlan = (pocketMoney * 0.3).toInt(),
                savingsPlan = (pocketMoney * 0.2).toInt(),
                needsFact = 0,
                wantsFact = 0,
                savingsFact = 0,
                isConfirmed = false
            )

            val summaryText = "🎓 Активирован Период $targetPeriod: ${periodInfo.title}!\n\n" +
                    "🎯 Образовательная цель: ${periodInfo.educationalGoal}\n" +
                    "🐾 Статус питомца: ${newStage.title} (Уровень $targetPeriod)\n\n" +
                    when (targetPeriod) {
                        1 -> "🌱 Знакомство с питомцем, базовые потребности и ведение бюджета."
                        2 -> "📊 Доступно планирование бюджета по правилу 50/30/20 и учет реальных трат."
                        3 -> "🏦 Разблокирован Банковский Сейф: вклады под 20% сложного процента!"
                        4 -> "🛡️ Формирование финансовой подушки безопасности и защита от спонтанных покупок."
                        else -> "👑 Высший статус: Мастер финансовой грамотности! Награды и золотой диплом на стене."
                    }

            state.copy(
                currentPeriod = targetPeriod,
                totalCoins = state.totalCoins,
                budgetPlan = newPlan,
                pet = updatedPet,
                quests = updatedQuests,
                isRoomLightOff = false,
                activeDialog = GameDialog.PeriodCompleted(
                    periodNumber = targetPeriod,
                    summary = summaryText,
                    earnedExp = 50 * targetPeriod,
                    nextPocketMoney = 0
                ),
                lastAdviceMessage = "Начался период $targetPeriod: ${periodInfo.title}!"
            )
        }
        persistCurrentState()
    }

    fun applyInteriorSet(wallpaperId: String, floorId: String) {
        _gameState.update { state ->
            state.copy(
                equippedWallpaper = wallpaperId,
                equippedFloor = floorId
            )
        }
        persistCurrentState()
    }

    // ==========================================
    // БАНКОВСКИЙ ВКЛАД ПОД СЛОЖНЫЙ ПРОЦЕНТ
    // ==========================================

    fun openBankDeposit(amount: Int, periods: Int = 1): Boolean {
        val state = _gameState.value
        if (amount <= 0 || state.totalCoins < amount) {
            ru.finpet.app.audio.SoundHapticManager.performErrorHaptic()
            _gameState.update {
                it.copy(lastAdviceMessage = "Недостаточно монет для открытия вклада (нужно $amount 🪙)!")
            }
            return false
        }
        val tx = FinTransaction(
            id = UUID.randomUUID().toString(),
            title = "Открытие банковского вклада 🏦",
            amount = amount,
            envelope = EnvelopeType.SAVINGS,
            isIncome = false,
            timestampFormatted = getCurrentDateString(),
            periodId = state.currentPeriod,
            note = "Вклад $amount монет под 20% сложного процента на $periods период(а)"
        )
        db?.addTransaction(tx)
        ru.finpet.app.audio.SoundHapticManager.playCoinSound()
        ru.finpet.app.audio.SoundHapticManager.performSuccessHaptic()

        _gameState.update { s ->
            s.copy(
                totalCoins = s.totalCoins - amount,
                bankDepositAmount = s.bankDepositAmount + amount,
                bankDepositPeriodsLeft = periods,
                transactions = listOf(tx) + s.transactions,
                lastAdviceMessage = "Банковский вклад на $amount 🪙 открыт под 20% в период! Сложный процент начнет приумножать твои монеты."
            )
        }
        persistCurrentState()
        return true
    }

    fun withdrawBankDepositEarly(): Boolean {
        val state = _gameState.value
        val amount = state.bankDepositAmount
        if (amount <= 0) return false
        val tx = FinTransaction(
            id = UUID.randomUUID().toString(),
            title = "Досрочный возврат вклада 🏦",
            amount = amount,
            envelope = EnvelopeType.SAVINGS,
            isIncome = true,
            timestampFormatted = getCurrentDateString(),
            periodId = state.currentPeriod,
            note = "Возврат тела вклада без начисленных процентов"
        )
        db?.addTransaction(tx)
        ru.finpet.app.audio.SoundHapticManager.playCoinSound()

        _gameState.update { s ->
            s.copy(
                totalCoins = s.totalCoins + amount,
                bankDepositAmount = 0,
                bankDepositPeriodsLeft = 0,
                transactions = listOf(tx) + s.transactions,
                lastAdviceMessage = "Вклад досрочно возвращен в кошелек ($amount 🪙)."
            )
        }
        persistCurrentState()
        return true
    }

    // ==========================================
    // СВЕТ, ПОГОДА И МУЗЫКА В КОМНАТЕ
    // ==========================================

    fun toggleRoomLight() {
        val state = _gameState.value
        val newLightOff = !state.isRoomLightOff
        ru.finpet.app.audio.SoundHapticManager.playClickSound()
        ru.finpet.app.audio.SoundHapticManager.performClickHaptic()

        _gameState.update { s ->
            val updatedPet = if (newLightOff) {
                s.pet.copy(
                    energy = (s.pet.energy + 0.30f).coerceIn(0f, 1f),
                    currentMood = PetMood.SLEEPING,
                    moodExplanation = "${s.pet.name} сладко спит при мягком свете лампы..."
                )
            } else {
                s.pet.copy(
                    currentMood = PetMood.HAPPY,
                    moodExplanation = "${s.pet.name} проснулся, полон сил и энергии!"
                )
            }
            s.copy(
                isRoomLightOff = newLightOff,
                pet = updatedPet,
                lastAdviceMessage = if (newLightOff) {
                    "Свет выключен. ${s.pet.name} сладко спит под мягкий свет лампы 💤 Нажми на лампу, чтобы разбудить его."
                } else {
                    "Свет включен! Доброе утро, ${s.pet.name} готов к новым финансовым открытиям!"
                }
            )
        }
        persistCurrentState()
    }

    fun cycleRoomWeather() {
        val state = _gameState.value
        val nextWeather = state.roomWeather.next()
        ru.finpet.app.audio.SoundHapticManager.performTickHaptic()
        _gameState.update { s ->
            s.copy(
                roomWeather = nextWeather,
                lastAdviceMessage = "Погода за окном: ${nextWeather.icon} ${nextWeather.title}"
            )
        }
        persistCurrentState()
    }

    fun setRoomWeather(weather: RoomWeather) {
        ru.finpet.app.audio.SoundHapticManager.performTickHaptic()
        _gameState.update { s ->
            s.copy(roomWeather = weather)
        }
        persistCurrentState()
    }

    fun toggleMusic(): Boolean {
        val isNowPlaying = ru.finpet.app.audio.SoundHapticManager.toggleAmbientMusic()
        _gameState.update { it.copy(isMusicEnabled = isNowPlaying) }
        persistCurrentState()
        return isNowPlaying
    }

    // --- Интерактивные действия с питомцем ---

    fun playWithPet() {
        val state = _gameState.value
        if (state.pet.energy < 0.15f) {
            ru.finpet.app.audio.SoundHapticManager.performErrorHaptic()
            _gameState.update {
                it.copy(lastAdviceMessage = "${it.pet.name} слишком устал для активных игр. Сначала уложи его отдохнуть!")
            }
            return
        }
        _gameState.update { s ->
            val updatedPet = s.pet.copy(
                happiness = (s.pet.happiness + 0.20f).coerceIn(0f, 1f),
                energy = (s.pet.energy - 0.25f).coerceIn(0.05f, 1f),
                currentMood = PetMood.HAPPY,
                moodExplanation = "${s.pet.name} весело играет и радуется!"
            )
            s.copy(pet = updatedPet, lastAdviceMessage = "${s.pet.name} прыгает от счастья после веселой игры!")
        }
        persistCurrentState()
    }

    fun sleepPet() {
        _gameState.update { state ->
            val updatedPet = state.pet.copy(
                energy = 1.0f,
                currentMood = PetMood.SLEEPING,
                moodExplanation = "${state.pet.name} крепко спит и восстанавливает силы."
            )
            state.copy(pet = updatedPet, lastAdviceMessage = "${state.pet.name} сладко спит. Энергия 100%!")
        }
        persistCurrentState()
    }

    fun feedPetQuick() {
        val state = _gameState.value
        val cost = 10
        if (state.totalCoins < cost) {
            ru.finpet.app.audio.SoundHapticManager.performErrorHaptic()
            _gameState.update {
                it.copy(lastAdviceMessage = "Недостаточно монет для покупки корма (нужно $cost 🪙)! Заработай в квестах или мини-играх.")
            }
            return
        }
        _gameState.update { s ->
            val cur = s.pet
            val newHunger = (cur.hunger + 0.35f).coerceIn(0f, 1f)
            val newEnergy = (cur.energy + 0.35f).coerceIn(0f, 1f)
            val newHapp = (cur.happiness + 0.15f).coerceIn(0f, 1f)
            val (newLevel, newExp, newExpNext) = calculateNewExp(cur.level, cur.exp + 15, cur.expToNextLevel)
            val updatedPet = cur.copy(
                hunger = newHunger,
                energy = newEnergy,
                happiness = newHapp,
                level = newLevel,
                exp = newExp,
                expToNextLevel = newExpNext,
                currentMood = if (newHunger > 0.6f) PetMood.HAPPY else PetMood.CONTENT,
                moodExplanation = "${cur.name} вкусно покушал полезного корма (+15 XP, +35% энергии)!"
            )
            val newTotalCoins = s.totalCoins - cost
            val currentPlan = s.budgetPlan
            val updatedBudget = currentPlan.copy(needsFact = currentPlan.needsFact + cost)
            val tx = FinTransaction(
                id = UUID.randomUUID().toString(),
                title = "Покупка корма для ${cur.name}",
                amount = cost,
                envelope = EnvelopeType.NEEDS,
                isIncome = false,
                timestampFormatted = getCurrentDateString(),
                periodId = s.currentPeriod,
                note = "Базовое питание питомца"
            )
            db?.addTransaction(tx)
            s.copy(
                totalCoins = newTotalCoins,
                budgetPlan = updatedBudget,
                pet = updatedPet,
                transactions = listOf(tx) + s.transactions,
                lastAdviceMessage = "${cur.name} сыт и доволен! Потрачено $cost 🪙 из обязательных трат."
            )
        }
        persistCurrentState()
    }

    fun applyFraudPenalty(coinsPenalty: Int) {
        _gameState.update { state ->
            val actualPenalty = coinsPenalty.coerceAtMost(state.totalCoins)
            val newTotal = (state.totalCoins - actualPenalty).coerceAtLeast(0)
            val newFinScore = (state.pet.finScore - 10).coerceAtLeast(0)
            val updatedPet = state.pet.copy(
                finScore = newFinScore,
                moodExplanation = "Ой-ой! Мы попались на уловку интернет-мошенников..."
            )
            state.copy(
                totalCoins = newTotal,
                pet = updatedPet,
                lastAdviceMessage = "Осторожно! Доверие сомнительным ссылкам привело к потере $actualPenalty 🪙 и падению финансового рейтинга."
            )
        }
        persistCurrentState()
    }

    fun showerPetQuick() {
        _gameState.update { state ->
            val cur = state.pet
            val newCare = (cur.care + 0.35f).coerceIn(0f, 1f)
            val newHapp = (cur.happiness + 0.20f).coerceIn(0f, 1f)
            val (newLevel, newExp, newExpNext) = calculateNewExp(cur.level, cur.exp + 20, cur.expToNextLevel)
            val updatedPet = cur.copy(
                care = newCare,
                happiness = newHapp,
                level = newLevel,
                exp = newExp,
                expToNextLevel = newExpNext,
                currentMood = PetMood.HAPPY,
                moodExplanation = "${cur.name} сияет от чистоты и свежести (+20 XP)!"
            )
            state.copy(
                pet = updatedPet,
                lastAdviceMessage = "${cur.name} чистый и опрятный! Гигиена и забота о себе — это важно."
            )
        }
        persistCurrentState()
    }

    fun addPetExp(amount: Int) {
        _gameState.update { state ->
            val cur = state.pet
            val (newLevel, newExp, newExpNext) = calculateNewExp(cur.level, cur.exp + amount, cur.expToNextLevel)
            val updatedPet = cur.copy(
                level = newLevel,
                exp = newExp,
                expToNextLevel = newExpNext,
                evolutionStage = if (newLevel >= 5) EvolutionStage.MASTER else if (newLevel >= 3) EvolutionStage.TEEN else EvolutionStage.BABY
            )
            state.copy(pet = updatedPet)
        }
        persistCurrentState()
    }

    private fun calculateNewExp(level: Int, totalExp: Int, expToNext: Int): Triple<Int, Int, Int> {
        var l = level
        var e = totalExp
        var req = expToNext
        while (e >= req) {
            e -= req
            l++
            req += 50
        }
        return Triple(l, e, req)
    }

    // --- Реальный учет трат в рублях (Правило 50/30/20) ---

    fun addRealExpense(title: String, amountRub: Int, category: RealCategory, note: String = "") {
        val item = RealExpenseItem(
            title = title,
            amountRub = amountRub,
            category = category,
            note = note
        )
        db?.saveRealExpense(item)

        _gameState.update { state ->
            val curPet = state.pet
            val (newLevel, newExp, newExpNext) = calculateNewExp(curPet.level, curPet.exp + 35, curPet.expToNextLevel)
            val updatedPet = curPet.copy(
                level = newLevel,
                exp = newExp,
                expToNextLevel = newExpNext,
                happiness = (curPet.happiness + 0.10f).coerceIn(0f, 1f),
                finScore = (curPet.finScore + 1).coerceIn(0, 100),
                moodExplanation = "${curPet.name} гордится тобой: учет реальных трат дает +35 XP!"
            )
            state.copy(
                totalCoins = state.totalCoins + 10,
                realExpenses = listOf(item) + state.realExpenses,
                pet = updatedPet,
                lastAdviceMessage = "Записан расход $amountRub ₽ в категорию '${category.shortName}'. Получено +10 монет и +35 XP!"
            )
        }
        persistCurrentState()
    }

    fun deleteRealExpense(id: String) {
        db?.deleteRealExpense(id)
        _gameState.update { state ->
            state.copy(realExpenses = state.realExpenses.filter { it.id != id })
        }
    }

    fun getParentPin(): String {
        return db?.getSetting("parent_pin", "2026") ?: "2026"
    }

    fun setParentPin(pin: String) {
        db?.setSetting("parent_pin", pin)
    }

    fun setParentUnlocked(unlocked: Boolean) {
        _gameState.update { it.copy(isParentUnlocked = unlocked) }
    }

    fun approveParentTask(taskId: String) {
        _gameState.update { state ->
            val task = state.parentTasks.find { it.id == taskId } ?: return@update state
            if (task.status == TaskStatus.COMPLETED) return@update state

            val updatedTasks = state.parentTasks.map {
                if (it.id == taskId) it.copy(status = TaskStatus.COMPLETED) else it
            }
            val reward = task.rewardCoins
            val newTotalCoins = state.totalCoins + reward

            val tx = FinTransaction(
                id = UUID.randomUUID().toString(),
                title = "Награда от родителей: ${task.title}",
                amount = reward,
                envelope = EnvelopeType.NEEDS,
                isIncome = true,
                timestampFormatted = getCurrentDateString(),
                periodId = state.currentPeriod,
                note = "Поощрение за полезное дело"
            )
            db?.addTransaction(tx)

            state.copy(
                totalCoins = newTotalCoins,
                parentTasks = updatedTasks,
                transactions = listOf(tx) + state.transactions,
                lastAdviceMessage = "Родители одобрили дело '${task.title}' и начислили +$reward монет!"
            )
        }
        persistCurrentState()
    }

    fun addParentTask(title: String, reward: Int) {
        val newTask = ParentTask(
            id = "pt_${System.currentTimeMillis()}",
            title = title.ifBlank { "Новое полезное дело" },
            description = "Выполни и получи монетки от родителей",
            rewardCoins = reward.coerceAtLeast(10),
            status = TaskStatus.PENDING,
            icon = "⭐"
        )
        _gameState.update { it.copy(parentTasks = it.parentTasks + newTask) }
        persistCurrentState()
    }

    fun awardParentCoins(amount: Int, reason: String) {
        _gameState.update { state ->
            val reward = amount.coerceAtLeast(5)
            val tx = FinTransaction(
                id = UUID.randomUUID().toString(),
                title = "Бонус от родителей: $reason",
                amount = reward,
                envelope = EnvelopeType.NEEDS,
                isIncome = true,
                timestampFormatted = getCurrentDateString(),
                periodId = state.currentPeriod,
                note = reason
            )
            db?.addTransaction(tx)
            state.copy(
                totalCoins = state.totalCoins + reward,
                transactions = listOf(tx) + state.transactions,
                lastAdviceMessage = "Родители начислили +$reward монет ($reason)!"
            )
        }
        persistCurrentState()
    }

    fun resetTestProfile() {
        db?.resetToDefault()
        db?.setSetting("quests_today_count", "0")
        db?.setSetting("quest_streak_days", "1")
        db?.setSetting("pos_WARDROBE", "")
        db?.setSetting("rot_WARDROBE", "0")
        db?.setSetting("pos_DESK", "")
        db?.setSetting("rot_DESK", "0")
        db?.setSetting("pos_BED", "")
        db?.setSetting("rot_BED", "0")
        db?.setSetting("pos_LAMP", "")
        db?.setSetting("rot_LAMP", "0")
        db?.setSetting("pos_POSTER", "")
        db?.setSetting("is_tutorial_completed", "0")
        _gameState.value = createInitialState()
        persistCurrentState()
    }

    fun resetGameProgress() {
        resetTestProfile()
    }

    fun toggleDemoMode(enabled: Boolean) {
        _gameState.update { it.copy(isDemoMode = enabled) }
        db?.setSetting("demo_mode", if (enabled) "1" else "0")
    }

    
    fun equipRoomItem(itemId: String, slotType: RoomSlotType) {
        _gameState.update { state ->
            when (slotType) {
                RoomSlotType.WALLPAPER -> state.copy(equippedWallpaper = itemId)
                RoomSlotType.FLOOR -> state.copy(equippedFloor = itemId)
                RoomSlotType.TOY -> state.copy(equippedToy = itemId)
                RoomSlotType.WARDROBE -> state.copy(equippedWardrobe = itemId)
                RoomSlotType.DESK -> state.copy(equippedDesk = itemId)
                RoomSlotType.BED -> state.copy(equippedBed = itemId)
                RoomSlotType.LAMP -> state.copy(equippedLamp = itemId)
                RoomSlotType.POSTER -> state.copy(equippedPoster = itemId)
            }
        }
        persistCurrentState()
    }

    fun unequipRoomItem(slotType: RoomSlotType) {
        _gameState.update { state ->
            when (slotType) {
                RoomSlotType.WALLPAPER -> state.copy(equippedWallpaper = "sh_wp_gray")
                RoomSlotType.FLOOR -> state.copy(equippedFloor = "sh_fl_wood")
                RoomSlotType.TOY -> state.copy(equippedToy = null)
                RoomSlotType.WARDROBE -> state.copy(equippedWardrobe = null)
                RoomSlotType.DESK -> state.copy(equippedDesk = null)
                RoomSlotType.BED -> state.copy(equippedBed = null)
                RoomSlotType.LAMP -> state.copy(equippedLamp = null)
                RoomSlotType.POSTER -> state.copy(equippedPoster = null)
            }
        }
        persistCurrentState()
    }

    fun petThePet(): String {
        var message = ""
        _gameState.update { s ->
            val cur = s.pet
            val petName = cur.name.ifBlank { "Питомец" }
            val (newLevel, newExp, newExpNext) = calculateNewExp(cur.level, cur.exp + 15, cur.expToNextLevel)
            val updatedPet = cur.copy(
                happiness = (cur.happiness + 0.25f).coerceIn(0f, 1f),
                exp = newExp,
                level = newLevel,
                expToNextLevel = newExpNext,
                currentMood = PetMood.HAPPY,
                moodExplanation = "$petName мурлычет от заботы и ласки (+15 XP, +25% радости)!"
            )
            message = "❤️ $petName счастлив: +15 XP, +25% радости!"
            s.copy(
                pet = updatedPet,
                lastAdviceMessage = "$petName радуется вашей заботе!"
            )
        }
        persistCurrentState()
        return message
    }

    fun playWithPetToy(): String {
        return petThePet()
    }

    fun updateFurniturePosition(slotType: RoomSlotType, xRatio: Float, yRatio: Float) {
        _gameState.update { state ->
            when (slotType) {
                RoomSlotType.WARDROBE -> state.copy(wardrobeX = xRatio, wardrobeY = yRatio)
                RoomSlotType.DESK -> state.copy(deskX = xRatio, deskY = yRatio)
                RoomSlotType.BED -> state.copy(bedX = xRatio, bedY = yRatio)
                RoomSlotType.LAMP -> state.copy(lampX = xRatio, lampY = yRatio)
                RoomSlotType.POSTER -> state.copy(posterX = xRatio, posterY = yRatio)
                else -> state
            }
        }
        db?.setSetting("pos_${slotType.name}", "$xRatio,$yRatio")
    }

    fun rotateFurniture(slotType: RoomSlotType) {
        _gameState.update { state ->
            when (slotType) {
                RoomSlotType.BED -> {
                    val nextRotation = (state.bedRotation + 45) % 360
                    db?.setSetting("rot_BED", nextRotation.toString())
                    state.copy(bedRotation = nextRotation)
                }
                RoomSlotType.WARDROBE -> {
                    val nextRotation = (state.wardrobeRotation + 90) % 360
                    db?.setSetting("rot_WARDROBE", nextRotation.toString())
                    state.copy(wardrobeRotation = nextRotation)
                }
                RoomSlotType.DESK -> {
                    val nextRotation = (state.deskRotation + 45) % 360
                    db?.setSetting("rot_DESK", nextRotation.toString())
                    state.copy(deskRotation = nextRotation)
                }
                RoomSlotType.LAMP -> {
                    val nextRotation = (state.lampRotation + 90) % 360
                    db?.setSetting("rot_LAMP", nextRotation.toString())
                    state.copy(lampRotation = nextRotation)
                }
                else -> state
            }
        }
    }

    fun rotateBed() = rotateFurniture(RoomSlotType.BED)

    private fun parsePos(str: String, defX: Float, defY: Float): Pair<Float, Float> {
        if (str.isBlank()) return Pair(defX, defY)
        val parts = str.split(",")
        if (parts.size == 2) {
            val x = parts[0].toFloatOrNull() ?: defX
            val y = parts[1].toFloatOrNull() ?: defY
            return Pair(x, y)
        }
        return Pair(defX, defY)
    }

    fun interactWithRoomFurniture(slotType: RoomSlotType): String {
        var message = ""
        _gameState.update { s ->
            val cur = s.pet
            when (slotType) {
                RoomSlotType.BED -> {
                    val item = s.shopItems.find { it.id == s.equippedBed }
                    val name = item?.name ?: "Лежанка"
                    val updatedPet = cur.copy(
                        energy = (cur.energy + 0.35f).coerceIn(0f, 1f),
                        happiness = (cur.happiness + 0.15f).coerceIn(0f, 1f),
                        currentMood = PetMood.SLEEPING,
                        moodExplanation = "${cur.name} сладко вздремнул на $name и полон сил!"
                    )
                    message = "😴 $name: +35% энергии, +15% радости!"
                    s.copy(pet = updatedPet, lastAdviceMessage = "${cur.name} прекрасно отдохнул на $name!")
                }
                RoomSlotType.DESK -> {
                    val item = s.shopItems.find { it.id == s.equippedDesk }
                    val name = item?.name ?: "Рабочий стол"
                    val (newLevel, newExp, newExpNext) = calculateNewExp(cur.level, cur.exp + 20, cur.expToNextLevel)
                    val updatedPet = cur.copy(
                        exp = newExp,
                        level = newLevel,
                        expToNextLevel = newExpNext,
                        happiness = (cur.happiness + 0.12f).coerceIn(0f, 1f),
                        energy = (cur.energy - 0.08f).coerceIn(0.05f, 1f),
                        currentMood = PetMood.CONTENT,
                        moodExplanation = if (s.equippedDesk == "sh_furn_arcade") {
                            "${cur.name} установил новый рекорд на аркаде (+20 XP, +15 монет)!"
                        } else {
                            "${cur.name} позанимался финансовой грамотностью за $name (+20 XP)!"
                        }
                    )
                    val coinBonus = if (s.equippedDesk == "sh_furn_arcade") 15 else 5
                    message = if (s.equippedDesk == "sh_furn_arcade") {
                        "🕹️ Игра на аркаде: +20 XP, +$coinBonus 🪙!"
                    } else {
                        "💻 Занятия за столом: +20 XP опыта, +$coinBonus 🪙!"
                    }
                    s.copy(
                        totalCoins = s.totalCoins + coinBonus,
                        pet = updatedPet,
                        lastAdviceMessage = "${cur.name} развивает интеллект за $name!"
                    )
                }
                RoomSlotType.WARDROBE -> {
                    val item = s.shopItems.find { it.id == s.equippedWardrobe }
                    val name = item?.name ?: "Шкаф"
                    val updatedPet = cur.copy(
                        happiness = (cur.happiness + 0.20f).coerceIn(0f, 1f),
                        care = (cur.care + 0.25f).coerceIn(0f, 1f),
                        currentMood = PetMood.HAPPY,
                        moodExplanation = if (s.equippedWardrobe == "sh_furn_aquarium") {
                            "${cur.name} покормил рыбок в аквариуме и любуется ими!"
                        } else {
                            "${cur.name} навел идеальный порядок в комнате!"
                        }
                    )
                    message = if (s.equippedWardrobe == "sh_furn_aquarium") {
                        "🐠 Аквариум: рыбки сыты, +25% ухода, +20% радости!"
                    } else {
                        "✨ Порядок: +25% ухода, +20% радости!"
                    }
                    s.copy(pet = updatedPet, lastAdviceMessage = "В комнате уют и гармония благодаря $name!")
                }
                RoomSlotType.LAMP -> {
                    val item = s.shopItems.find { it.id == s.equippedLamp }
                    val name = item?.name ?: "Светильник"
                    val updatedPet = cur.copy(
                        happiness = (cur.happiness + 0.15f).coerceIn(0f, 1f),
                        currentMood = PetMood.HAPPY,
                        moodExplanation = "${cur.name} наслаждается мягким светом $name!"
                    )
                    message = if (s.equippedLamp == "sh_furn_star_projector") {
                        "✨ Проектор звёзд: комната наполнилась созвездиями! (+15% радости)"
                    } else {
                        "💡 $name: атмосфера тепла и уюта (+15% радости)!"
                    }
                    s.copy(pet = updatedPet, lastAdviceMessage = "Свет $name наполняет комнату теплом!")
                }
                else -> s
            }
        }
        persistCurrentState()
        return message
    }

    fun setCustomAvatar(path: String) {
        _gameState.update { it.copy(customAvatarPath = path, playerAvatar = "custom") }
        persistCurrentState()
    }

    fun dismissDialog() {
        _gameState.update { it.copy(activeDialog = null) }
    }

    fun showHelpAdvice() {
        _gameState.update { it.copy(activeDialog = GameDialog.HelpAdvice) }
    }

    fun startInteractiveTutorial() {
        _gameState.update { it.copy(activeTutorialStep = ru.finpet.app.ui.components.TutorialStep.PET) }
    }

    fun setTutorialStep(step: ru.finpet.app.ui.components.TutorialStep) {
        _gameState.update { it.copy(activeTutorialStep = step) }
    }

    fun advanceTutorialStep(completedStep: ru.finpet.app.ui.components.TutorialStep) {
        val nextStep = when (completedStep) {
            ru.finpet.app.ui.components.TutorialStep.PET -> ru.finpet.app.ui.components.TutorialStep.BUDGET
            ru.finpet.app.ui.components.TutorialStep.BUDGET -> ru.finpet.app.ui.components.TutorialStep.QUESTS
            ru.finpet.app.ui.components.TutorialStep.QUESTS -> ru.finpet.app.ui.components.TutorialStep.SHOP
            ru.finpet.app.ui.components.TutorialStep.SHOP -> ru.finpet.app.ui.components.TutorialStep.PROFILE
            ru.finpet.app.ui.components.TutorialStep.PROFILE -> null
        }
        _gameState.update {
            it.copy(
                activeTutorialStep = nextStep,
                isTutorialCompleted = if (nextStep == null) true else it.isTutorialCompleted
            )
        }
        persistCurrentState()
    }

    fun skipInteractiveTutorial() {
        _gameState.update { it.copy(activeTutorialStep = null, isTutorialCompleted = true) }
        persistCurrentState()
    }
}
