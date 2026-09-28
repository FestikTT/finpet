package ru.finpet.app.data

import ru.finpet.app.model.*

sealed class GameDialog {
    data class InsufficientFunds(val itemName: String, val price: Int, val currentCoins: Int, val needed: Int) : GameDialog()
    data class ConfirmPurchase(val item: ShopItem) : GameDialog()
    data class DeliveryUnboxing(val item: ShopItem) : GameDialog()
    data class ConfirmWithdrawal(val goal: FinancialGoal, val withdrawAmount: Int, val newRemaining: Int, val delayPeriods: Int) : GameDialog()
    data class QuestResult(val quest: FinancialQuest, val option: QuestOption) : GameDialog()
    data class PeriodCompleted(val periodNumber: Int, val summary: String, val earnedExp: Int, val nextPocketMoney: Int) : GameDialog()
    data class GoalAchieved(val goal: FinancialGoal) : GameDialog()
    data class DailyLimitReached(val completedCount: Int, val streakDays: Int) : GameDialog()
    object HelpAdvice : GameDialog()
}

data class GameState(
    val playerName: String = "Юный финансист",
    val playerAge: Int = 10,
    val playerAvatar: String = "fox_hero",
    val customAvatarPath: String? = null,
    val appTheme: ru.finpet.app.ui.theme.AppTheme = ru.finpet.app.ui.theme.AppTheme.CYBER_BLUE,
    val pet: Pet = Pet(),
    val isOnboardingCompleted: Boolean = false,
    val currentPeriod: Int = 1,
    val totalCoins: Int = 100,
    val budgetPlan: BudgetPeriodPlan = BudgetPeriodPlan(),
    val goals: List<FinancialGoal> = emptyList(),
    val activeGoalId: String = "g_scooter",
    val quests: List<FinancialQuest> = emptyList(),
    val questsCompletedToday: Int = 0,
    val dailyQuestLimit: Int = 3,
    val questStreakDays: Int = 1,
    val shopItems: List<ShopItem> = emptyList(),
    val transactions: List<FinTransaction> = emptyList(),
    val parentTasks: List<ParentTask> = emptyList(),
    val isParentUnlocked: Boolean = false,
    val isDemoMode: Boolean = true,
    val lastAdviceMessage: String = "Привет! Давай спланируем наш бюджет на неделю!",
    val realExpenses: List<RealExpenseItem> = emptyList(),
    val todayPracticalTip: PracticalMoneyTip = PracticalMoneyKnowledge.tips.first(),
    val activeDialog: GameDialog? = null,

    // Экипировка и предметы комнаты
    val equippedWallpaper: String = "sh_wp_gray",
    val equippedFloor: String = "sh_fl_wood",
    val equippedToy: String? = null,
    val equippedWardrobe: String? = null,
    val equippedDesk: String? = null,
    val equippedBed: String? = null,
    val equippedLamp: String? = null,
    val equippedPoster: String? = null,
    val wardrobeX: Float = 0.16f,
    val wardrobeY: Float = 0.58f,
    val wardrobeRotation: Int = 0,
    val deskX: Float = 0.64f,
    val deskY: Float = 0.68f,
    val deskRotation: Int = 0,
    val bedX: Float = 0.12f,
    val bedY: Float = 0.86f,
    val bedRotation: Int = 0,
    val lampX: Float = 0.82f,
    val lampY: Float = 0.62f,
    val lampRotation: Int = 0,
    val posterX: Float = 0.66f,
    val posterY: Float = 0.08f,
    val ownedRoomItemIds: Set<String> = setOf("sh_wp_gray", "sh_fl_wood"),
    val purchasedItemIds: Set<String> = setOf("sh_food_dry", "sh_food_donut", "sh_food_ramen", "sh_wp_gray", "sh_fl_wood"),
    val carrotCount: Int = 4,
    val totalKeys: Int = 4,
    val isTutorialCompleted: Boolean = false,
    val activeTutorialStep: ru.finpet.app.ui.components.TutorialStep? = null,

    // Новые фичи: свет в комнате, погода за окном, банковский вклад и фоновая музыка
    val isRoomLightOff: Boolean = false,
    val roomWeather: RoomWeather = RoomWeather.RAIN,
    val bankDepositAmount: Int = 0,
    val bankDepositPeriodsLeft: Int = 0,
    val bankDepositRate: Float = 0.20f,
    val totalBankInterestEarned: Int = 0,
    val isMusicEnabled: Boolean = false
) {
    val hasActiveBankDeposit: Boolean
        get() = bankDepositAmount > 0 && bankDepositPeriodsLeft > 0

    val activeGoal: FinancialGoal?
        get() = goals.find { it.id == activeGoalId } ?: goals.firstOrNull()

    val totalSavingsAmount: Int
        get() = goals.sumOf { it.currentAmount }

    val isDailyLimitReached: Boolean
        get() = questsCompletedToday >= dailyQuestLimit

    val nextActiveQuestOrder: Int
        get() = quests.firstOrNull { !it.isCompleted }?.orderIndex ?: (quests.size + 1)

    val royalSkinIds: List<String>
        get() = listOf("sh_acc_crown", "sh_royal_mantle", "sh_royal_scepter")

    val royalSkinsUnlockedCount: Int
        get() = royalSkinIds.count { it in purchasedItemIds }

    val hasAllRoyalSkins: Boolean
        get() = royalSkinsUnlockedCount >= 3
}
