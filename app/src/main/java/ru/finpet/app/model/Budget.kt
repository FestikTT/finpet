package ru.finpet.app.model

enum class EnvelopeType(
    val title: String,
    val shortName: String,
    val targetPercent: Int,
    val emoji: String,
    val description: String
) {
    NEEDS(
        title = "Обязательные расходы",
        shortName = "Еда и здоровье",
        targetPercent = 50,
        emoji = "🥕",
        description = "Еда, витамины, гигиена и здоровье питомца. От них зависит его самочувствие!"
    ),
    WANTS(
        title = "Необязательные расходы",
        shortName = "Радости и игры",
        targetPercent = 30,
        emoji = "🎮",
        description = "Игрушки, лакомства и развлечения. Поднимают настроение, но можно отложить!"
    ),
    SAVINGS(
        title = "Накопления",
        shortName = "В копилку на мечту",
        targetPercent = 20,
        emoji = "🎯",
        description = "Сбережения на большую финансовую цель. Приближают заветную мечту!"
    )
}

data class BudgetPeriodPlan(
    val periodId: Int = 1,
    val availableAmount: Int = 100,
    val needsPlan: Int = 50,
    val wantsPlan: Int = 30,
    val savingsPlan: Int = 20,
    val needsFact: Int = 0,
    val wantsFact: Int = 0,
    val savingsFact: Int = 0,
    val isConfirmed: Boolean = false
) {
    val totalPlanned: Int get() = needsPlan + wantsPlan + savingsPlan
    val unallocatedAmount: Int get() = availableAmount - totalPlanned
    val isBalanced: Boolean get() = unallocatedAmount == 0

    val totalFactSpent: Int get() = needsFact + wantsFact + savingsFact
    val remainingNeedsBudget: Int get() = (needsPlan - needsFact).coerceAtLeast(0)
    val remainingWantsBudget: Int get() = (wantsPlan - wantsFact).coerceAtLeast(0)
    val remainingSavingsBudget: Int get() = (savingsPlan - savingsFact).coerceAtLeast(0)
    val isSuccessful: Boolean get() = needsFact <= needsPlan && wantsFact <= wantsPlan
}

data class FinTransaction(
    val id: String,
    val title: String,
    val amount: Int,
    val envelope: EnvelopeType,
    val isIncome: Boolean,
    val timestampFormatted: String,
    val periodId: Int = 1,
    val note: String = ""
)
