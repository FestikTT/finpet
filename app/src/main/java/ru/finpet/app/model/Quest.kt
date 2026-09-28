package ru.finpet.app.model

enum class QuestTopic(
    val unitIndex: Int,
    val unitTitle: String,
    val emoji: String,
    val description: String
) {
    BUDGETING(
        unitIndex = 1,
        unitTitle = "Модуль 1: Личный бюджет и карманные деньги",
        emoji = "📊",
        description = "Правило 50/30/20, подушка безопасности и учет расходов"
    ),
    BANKING(
        unitIndex = 2,
        unitTitle = "Модуль 2: Банковская грамотность и счета",
        emoji = "🏦",
        description = "Карты, кешбэк, СБП, инфляция и сложный процент"
    ),
    SMART_SHOPPING(
        unitIndex = 3,
        unitTitle = "Модуль 3: Осознанное потребление и маркетинг",
        emoji = "🛒",
        description = "Ловушки скидок, бренды, чеки и защита от импульсивных трат"
    ),
    CYBER_SECURITY(
        unitIndex = 4,
        unitTitle = "Модуль 4: Кибербезопасность и антифрод",
        emoji = "🛡️",
        description = "Защита от мошенников, фишинг, 2FA и безопасность паролей"
    ),
    INVESTMENTS(
        unitIndex = 5,
        unitTitle = "Модуль 5: Инвестиции и предпринимательство",
        emoji = "🚀",
        description = "Свой стартап, акции, облигации, диверсификация и капитал"
    ),
    FAMILY_ECONOMICS(
        unitIndex = 6,
        unitTitle = "Модуль 6: Семейная экономика и ресурсы",
        emoji = "🏠",
        description = "ЖКХ, коммунальные платежи, налоги, страхование и экономия"
    );

    val title: String get() = unitTitle
}

enum class StageType {
    DILEMMA,       // Вопрос или практическая финансовая ситуация
    FRAUD_CHECK,   // Проверка подозрительного сообщения/звонка/сайта
    CALCULATION    // Мини-расчет выгоды или сортировка потребностей
}

data class QuestStage(
    val stageType: StageType,
    val title: String,
    val promptText: String,
    val options: List<QuestOption>,
    val senderName: String? = null,
    val senderAvatar: String? = null,
    val messageText: String? = null,
    val redFlags: List<String> = emptyList()
)

data class QuestOption(
    val text: String,
    val isOptimal: Boolean,
    val feedbackExplanation: String, // Обучающее объяснение
    val coinReward: Int,
    val finScoreDelta: Int
)

data class FinancialQuest(
    val id: String,
    val orderIndex: Int, // 1..54 для строгого последовательного порядка
    val title: String,
    val topic: QuestTopic,
    val dilemmaText: String,
    val options: List<QuestOption>,
    val difficulty: Int = 1, // 1: Базовый, 2: Продвинутый, 3: Эксперт
    val periodRequired: Int = 1,
    val isCompleted: Boolean = false,
    val selectedOptionIndex: Int? = null,
    val stages: List<QuestStage> = emptyList()
) {
    val allStages: List<QuestStage>
        get() = if (stages.isNotEmpty()) {
            stages
        } else {
            listOf(
                QuestStage(
                    stageType = StageType.DILEMMA,
                    title = title,
                    promptText = dilemmaText,
                    options = options
                )
            )
        }

    val maxRewardCoins: Int
        get() = if (stages.isNotEmpty()) {
            stages.sumOf { s -> s.options.maxOfOrNull { it.coinReward } ?: 0 }
        } else {
            options.maxOfOrNull { it.coinReward } ?: 10
        }
}
