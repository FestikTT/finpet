package ru.finpet.app.model

import ru.finpet.app.data.GameState

data class WallAward(
    val id: String,
    val title: String,
    val diplomaName: String,
    val description: String,
    val emoji: String,
    val ribbonColorHex: Long = 0xFFD97706,
    val sealColorHex: Long = 0xFFDC2626,
    val conditionDescription: String,
    val rewardCoins: Int = 20
) {
    fun isUnlocked(state: GameState): Boolean {
        return when (id) {
            "award_first_expense" -> state.realExpenses.isNotEmpty()
            "award_first_savings" -> state.goals.any { it.currentAmount > 0 }
            "award_quests_5" -> state.quests.count { it.isCompleted } >= 5
            "award_streak_3" -> state.questStreakDays >= 3
            "award_budget_master" -> state.budgetPlan.isConfirmed
            "award_parent_hero" -> state.parentTasks.any { it.status == TaskStatus.COMPLETED }
            else -> false
        }
    }
}

object WallAwardsRepository {
    val allAwards = listOf(
        WallAward(
            id = "award_first_expense",
            title = "Первый шаг",
            diplomaName = "Грамота юного финансиста",
            description = "За запись первой реальной траты и старт осознанного бюджета!",
            emoji = "📜",
            ribbonColorHex = 0xFF2563EB,
            sealColorHex = 0xFFDC2626,
            conditionDescription = "Запишите расход в разделе «Бюджет»"
        ),
        WallAward(
            id = "award_first_savings",
            title = "Копилка мечты",
            diplomaName = "Сертификат инвестора",
            description = "За первое пополнение копилки и шаг навстречу финансовой мечте!",
            emoji = "🪙",
            ribbonColorHex = 0xFFD97706,
            sealColorHex = 0xFFB45309,
            conditionDescription = "Отложите первые монеты в копилку мечты"
        ),
        WallAward(
            id = "award_quests_5",
            title = "Знаток правил",
            diplomaName = "Диплом финансовой грамотности",
            description = "За успешное решение первых 5 финансовых квестов и ситуаций!",
            emoji = "🎓",
            ribbonColorHex = 0xFF7C3AED,
            sealColorHex = 0xFF4338CA,
            conditionDescription = "Решите 5 обучающих квестов"
        ),
        WallAward(
            id = "award_streak_3",
            title = "Дисциплина",
            diplomaName = "Наградной лист за упорство",
            description = "За ежедневную заботу о бюджете и серию квестов от 3 дней!",
            emoji = "🔥",
            ribbonColorHex = 0xFFEA580C,
            sealColorHex = 0xFFDC2626,
            conditionDescription = "Держите серию квестов не менее 3 дней подряд"
        ),
        WallAward(
            id = "award_budget_master",
            title = "Магистр 50/30/20",
            diplomaName = "Почётный диплом распределения",
            description = "За идеальное разделение карманных денег на нужды, желания и копилку!",
            emoji = "⭐",
            ribbonColorHex = 0xFF16A34A,
            sealColorHex = 0xFF15803D,
            conditionDescription = "Утвердите план бюджета периода"
        ),
        WallAward(
            id = "award_parent_hero",
            title = "Семейный герой",
            diplomaName = "Свидетельство о полезных делах",
            description = "За помощь семье и выполнение полезного дела от родителей!",
            emoji = "🏆",
            ribbonColorHex = 0xFFDB2777,
            sealColorHex = 0xFFBE185D,
            conditionDescription = "Выполните и получите одобрение задания от родителей"
        )
    )
}
