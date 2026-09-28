package ru.finpet.app.model

data class GamePeriodInfo(
    val periodNumber: Int,
    val title: String,
    val description: String,
    val pocketMoneyAmount: Int,
    val educationalGoal: String,
    val activeQuestId: String
)

object GamePeriodRepository {
    val periods = listOf(
        GamePeriodInfo(
            periodNumber = 1,
            title = "Период 1: Знакомство и первые карманные деньги",
            description = "Ты получил свои первые 50 монет! Распредели их по трем направлениям: еда для питомца, немного радостей и обязательно отложи часть в копилку.",
            pocketMoneyAmount = 50,
            educationalGoal = "Понять, что деньги ограничены и их нужно распределять до совершения трат.",
            activeQuestId = "q_budget_1"
        ),
        GamePeriodInfo(
            periodNumber = 2,
            title = "Период 2: Поход в магазин — Надо или Хочу?",
            description = "Новая порция карманных денег (65 монет)! В магазине появилось много соблазнов. Сможешь ли ты сначала купить полезную еду, а уже потом игрушки?",
            pocketMoneyAmount = 65,
            educationalGoal = "Различать обязательные (еда, гигиена) и необязательные (лакомства, игрушки) покупки.",
            activeQuestId = "q_purchases_1"
        ),
        GamePeriodInfo(
            periodNumber = 3,
            title = "Период 3: Копим на мечту и растем",
            description = "Твой питомец подрос и стал внимательнее! Время сделать регулярный вклад в копилку на финансовую цель (бюджет 80 монет). Чем регулярнее откладываешь, тем ближе мечта.",
            pocketMoneyAmount = 80,
            educationalGoal = "Научиться ставить финансовую цель и регулярно откладывать на неё часть дохода.",
            activeQuestId = "q_savings_1"
        ),
        GamePeriodInfo(
            periodNumber = 4,
            title = "Период 4: Непредвиденные расходы",
            description = "Ой-ой! Неожиданно сломался замочек на домике. Понадобятся дополнительные монеты на починку (бюджет 95 монет). Поможет ли резервный фонд?",
            pocketMoneyAmount = 95,
            educationalGoal = "Узнать, зачем нужна 'подушка безопасности' на случай неожиданных ситуаций.",
            activeQuestId = "q_budget_2"
        ),
        GamePeriodInfo(
            periodNumber = 5,
            title = "Период 5: Финансовая зрелость — Цель достигнута!",
            description = "Твой питомец стал настоящим финансовым экспертом! Подводим итоги большого пути с бюджетом 110 монет, проверяем копилку и празднуем победу над спонтанными тратами.",
            pocketMoneyAmount = 110,
            educationalGoal = "Оценить результаты своих финансовых решений за несколько периодов и насладиться результатом.",
            activeQuestId = "q_savings_2"
        )
    )

    fun getPeriod(index: Int): GamePeriodInfo {
        val safeIndex = (index - 1).coerceIn(0, periods.size - 1)
        return periods[safeIndex]
    }
}
