package ru.finpet.app.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Категории реальных расходов человека по правилу 50/30/20
 */
enum class RealCategory(
    val title: String,
    val shortName: String,
    val targetPercent: Int,
    val icon: String,
    val examples: String,
    val isNeeds: Boolean
) {
    NEEDS(
        title = "Обязательные (Нужно 50%)",
        shortName = "Нужно",
        targetPercent = 50,
        icon = "🥕",
        examples = "Еда, продукты, транспорт, связь, лекарства, одежда по сезону",
        isNeeds = true
    ),
    WANTS(
        title = "Желания и радости (Хочу 30%)",
        shortName = "Хочу",
        targetPercent = 30,
        icon = "🎮",
        examples = "Кафе, фастфуд, видеоигры, подписки, сладости, развлечения",
        isNeeds = false
    ),
    SAVINGS(
        title = "Копилка и резерв (Копить 20%)",
        shortName = "Копилка",
        targetPercent = 20,
        icon = "🎯",
        examples = "Вклад, накопительный счет, подушка безопасности, цель мечты",
        isNeeds = false
    )
}

/**
 * Реальная транзакция в рублях
 */
data class RealExpenseItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val amountRub: Int,
    val category: RealCategory,
    val dateFormatted: String = SimpleDateFormat("dd MMMM, HH:mm", Locale("ru")).format(Date()),
    val note: String = ""
)

/**
 * Аналитический результат распределения расходов по правилу 50/30/20
 */
data class Rule503020Analysis(
    val totalSpentRub: Int,
    val needsRub: Int,
    val wantsRub: Int,
    val savingsRub: Int,
    val needsPercent: Int,
    val wantsPercent: Int,
    val savingsPercent: Int,
    val statusTitle: String,
    val statusDescription: String,
    val isHealthy: Boolean
)

object RealFinanceAnalytics {
    fun analyze(items: List<RealExpenseItem>): Rule503020Analysis {
        val total = items.sumOf { it.amountRub }
        if (total == 0) {
            return Rule503020Analysis(
                totalSpentRub = 0,
                needsRub = 0,
                wantsRub = 0,
                savingsRub = 0,
                needsPercent = 0,
                wantsPercent = 0,
                savingsPercent = 0,
                statusTitle = "Пока нет записей трат",
                statusDescription = "Внеси свои первые реальные расходы за день, чтобы увидеть баланс 50/30/20!",
                isHealthy = true
            )
        }

        val needs = items.filter { it.category == RealCategory.NEEDS }.sumOf { it.amountRub }
        val wants = items.filter { it.category == RealCategory.WANTS }.sumOf { it.amountRub }
        val savings = items.filter { it.category == RealCategory.SAVINGS }.sumOf { it.amountRub }

        val needsP = (needs * 100) / total
        val wantsP = (wants * 100) / total
        val savingsP = (savings * 100) / total

        val (title, desc, healthy) = when {
            wantsP > 50 -> Triple(
                "⚠️ Слишком много трат на 'Хочу' ($wantsP%)",
                "Больше половины денег уходит на сиюминутные радости. Попробуй отложить часть этих сумм в копилку — так ты накопишь на большую цель намного быстрее!",
                false
            )
            needsP > 70 -> Triple(
                "Большая нагрузка обязательных трат ($needsP%)",
                "Основные расходы занимают большую часть бюджета. Стоит поискать варианты оптимизации (скидки, проездные, более выгодные тарифы связи).",
                false
            )
            savingsP >= 15 -> Triple(
                "🌟 Отличная финансовая форма!",
                "Твой баланс близок к идеальному правилу 50/30/20. Ты не только покрываешь потребности, но и регулярно откладываешь на будущее!",
                true
            )
            else -> Triple(
                "Хороший баланс трат",
                "Ты держишь расходы под контролем. Постарайся довести сбережения хотя бы до 15-20% от дохода.",
                true
            )
        }

        return Rule503020Analysis(
            totalSpentRub = total,
            needsRub = needs,
            wantsRub = wants,
            savingsRub = savings,
            needsPercent = needsP,
            wantsPercent = wantsP,
            savingsPercent = savingsP,
            statusTitle = title,
            statusDescription = desc,
            isHealthy = healthy
        )
    }
}

/**
 * Расчет сложного процента (Вклады и накопительные счета)
 */
object CompoundInterestCalculator {
    data class Result(
        val totalAccumulatedRub: Long,
        val totalInvestedRub: Long,
        val interestEarnedRub: Long,
        val years: Int,
        val monthlyDepositRub: Int,
        val interestRatePercent: Double
    )

    fun calculate(
        monthlyDepositRub: Int,
        interestRatePercent: Double = 16.0,
        years: Int = 3
    ): Result {
        var balance = 0.0
        val totalInvested = monthlyDepositRub.toLong() * 12 * years
        val monthlyRate = (interestRatePercent / 100.0) / 12.0
        val months = years * 12

        for (m in 1..months) {
            val interestOnBalance = balance * monthlyRate
            val interestOnDeposit = monthlyDepositRub * (monthlyRate / 2.0)
            balance += monthlyDepositRub + interestOnBalance + interestOnDeposit
        }

        val totalAcc = Math.round(balance)
        val earned = (totalAcc - totalInvested).coerceAtLeast(0L)

        return Result(
            totalAccumulatedRub = totalAcc,
            totalInvestedRub = totalInvested,
            interestEarnedRub = earned,
            years = years,
            monthlyDepositRub = monthlyDepositRub,
            interestRatePercent = interestRatePercent
        )
    }
}

/**
 * Калькулятор «Кофе-эффект» (влияние мелких ежедневных трат)
 */
data class HabitExpensePreset(
    val title: String,
    val icon: String,
    val defaultPriceRub: Int,
    val equivalent1Year: String,
    val equivalent3Years: String
)

object CoffeeEffectCalculator {
    val presets = listOf(
        HabitExpensePreset(
            title = "Кофе",
            icon = "☕",
            defaultPriceRub = 180,
            equivalent1Year = "Новый смартфон или планшет",
            equivalent3Years = "Поездка на море или мощный ноутбук"
        ),
        HabitExpensePreset(
            title = "Чипсы",
            icon = "🍟",
            defaultPriceRub = 120,
            equivalent1Year = "Беспроводные наушники + кроссовки",
            equivalent3Years = "Игровая консоль или электросамокат"
        ),
        HabitExpensePreset(
            title = "Такси",
            icon = "🚕",
            defaultPriceRub = 350,
            equivalent1Year = "Курсы обучения + путешествие",
            equivalent3Years = "Первый взнос за автомобиль или ремонт"
        ),
        HabitExpensePreset(
            title = "Донат",
            icon = "🎮",
            defaultPriceRub = 100,
            equivalent1Year = "Умные часы или новая одежда",
            equivalent3Years = "Топовый игровой компьютер"
        )
    )

    data class HabitImpact(
        val dailyRub: Int,
        val monthlyRub: Long,
        val yearRub: Long,
        val threeYearsWithInterestRub: Long
    )

    fun calculate(dailyRub: Int): HabitImpact {
        val month = dailyRub.toLong() * 30
        val year = dailyRub.toLong() * 365
        // Капитализация накоплений за 3 года по ставке 16% годовых
        val rate = 0.16
        val threeYears = (year * (Math.pow(1.0 + rate, 3.0) - 1.0) / rate * (1.0 + rate)).toLong()

        return HabitImpact(
            dailyRub = dailyRub,
            monthlyRub = month,
            yearRub = year,
            threeYearsWithInterestRub = threeYears
        )
    }
}

/**
 * Жизненные советы и разборы реальных финансовых ситуаций
 */
data class PracticalMoneyTip(
    val id: String,
    val topic: String,
    val icon: String,
    val title: String,
    val shortSummary: String,
    val actionableRule: String
)

object PracticalMoneyKnowledge {
    val tips = listOf(
        PracticalMoneyTip(
            id = "t1",
            topic = "Импульсивные покупки",
            icon = "⏳",
            title = "Правило 24 часов",
            shortSummary = "Маркетологи используют таймеры и яркие ценники, чтобы вызвать всплеск эмоций и заставить тебя купить вещь прямо сейчас.",
            actionableRule = "Если захотелось купить что-то незапланированное дороже 1 000 ₽ — подожди ровно 24 часа. В 80% случаев желание пропадает!"
        ),
        PracticalMoneyTip(
            id = "t2",
            topic = "Банки и карты",
            icon = "💳",
            title = "Ловушка 'Минимального платежа'",
            shortSummary = "По кредиткам банки предлагают платить всего 3-5% в месяц. Так кажется, что долг пустяковый, но проценты начисляются на весь остаток.",
            actionableRule = "Всегда гаси кредитку полностью в льготный (грейс) период. Никогда не плати только минимальный платеж — это переплата в 2-3 раза."
        ),
        PracticalMoneyTip(
            id = "t3",
            topic = "Подушка безопасности",
            icon = "🛡️",
            title = "Запас на черный день",
            shortSummary = "Сломался телефон, заболел зуб или задержали зарплату? Без запаса придется влезать в долги под бешеные проценты.",
            actionableRule = "Держи в резерве сумму твоих обычных расходов за 3 месяца. Храни ее на накопительном счете с возможностью снять в любой день."
        ),
        PracticalMoneyTip(
            id = "t4",
            topic = "Маркетплейсы",
            icon = "🏷️",
            title = "Фальшивые скидки",
            shortSummary = "Перед 'распродажами' продавцы часто поднимают цену на 50%, а затем рисуют заманчивую перечеркнутую цену.",
            actionableRule = "Сравнивай историю цен через расширения браузера или проверяй этот же товар у 3-4 других продавцов."
        ),
        PracticalMoneyTip(
            id = "t5",
            topic = "Пассивный доход",
            icon = "📈",
            title = "Время важнее суммы",
            shortSummary = "Благодаря сложному проценту, если начать откладывать даже по 1 000 ₽ в 16 лет, к 30 годам у тебя будет в 3 раза больше денег, чем если начать откладывать по 3 000 ₽ в 25 лет.",
            actionableRule = "Начни с любой мелкой суммы уже сегодня. Регулярность бьет размер капитала!"
        )
    )
}
