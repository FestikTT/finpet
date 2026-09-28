package ru.finpet.app.util

import kotlin.math.abs

object FormatUtils {

    fun pluralize(count: Int, one: String, few: String, many: String): String {
        val n = abs(count)
        val word = when {
            n % 100 in 11..19 -> many
            n % 10 == 1 -> one
            n % 10 in 2..4 -> few
            else -> many
        }
        return "$count $word"
    }

    fun formatCoins(count: Int): String = pluralize(count, "монета", "монеты", "монет")
    fun formatDays(count: Int): String = pluralize(count, "день", "дня", "дней")
    fun formatTasks(count: Int): String = pluralize(count, "задание", "задания", "заданий")
}
