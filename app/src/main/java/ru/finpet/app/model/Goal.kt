package ru.finpet.app.model

data class FinancialGoal(
    val id: String,
    val title: String,
    val targetAmount: Int,
    val currentAmount: Int = 0,
    val icon: String = "🎯",
    val isCustom: Boolean = false,
    val isAchieved: Boolean = false
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount).coerceIn(0f, 1f) else 0f

    val remainingAmount: Int
        get() = (targetAmount - currentAmount).coerceAtLeast(0)

    fun calculateEstimatedPeriods(avgSavingsPerPeriod: Int): Int {
        if (remainingAmount <= 0) return 0
        val safeSavings = if (avgSavingsPerPeriod <= 0) 25 else avgSavingsPerPeriod
        val periods = (remainingAmount + safeSavings - 1) / safeSavings
        return periods.coerceAtLeast(1)
    }
}
