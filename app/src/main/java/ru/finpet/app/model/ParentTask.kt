package ru.finpet.app.model

enum class TaskStatus(val title: String) {
    PENDING("Выполняется"),
    WAITING_APPROVAL("Ждет проверки"),
    COMPLETED("Одобрено!")
}

data class ParentTask(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val status: TaskStatus = TaskStatus.PENDING,
    val icon: String = "⭐"
)
