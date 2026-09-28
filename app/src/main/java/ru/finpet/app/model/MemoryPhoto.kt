package ru.finpet.app.model

import ru.finpet.app.data.GameState

/**
 * Памятная ретро-фотография для альбома воспоминаний FinPet.
 */
data class MemoryPhoto(
    val id: String,
    val title: String,
    val dateFormatted: String,
    val caption: String,
    val emoji: String,
    val category: String, // "Достижение", "Обучение", "Комната", "Супермаркет", "Банк"
    val petReactionEmoji: String = "🦊✨",
    val tiltDegrees: Float = 0f,
    val cardColorHex: Long = 0xFFFFFFFF
)

object MemoryAlbumRepository {

    fun getDefaultMemories(state: GameState): List<MemoryPhoto> {
        val petName = state.pet.name.ifBlank { "Питомец" }
        return listOf(
            MemoryPhoto(
                id = "mem_first_meet",
                title = "Знакомство с $petName",
                dateFormatted = "День 1",
                caption = "Первый день в уютном доме! Выбрали имя и составили первые планы на будущее.",
                emoji = "🐾",
                category = "Дружба",
                petReactionEmoji = "💖",
                tiltDegrees = -2f,
                cardColorHex = 0xFFFFFDF5
            ),
            MemoryPhoto(
                id = "mem_first_budget",
                title = "Правило 50/30/20",
                dateFormatted = "Период 1",
                caption = "Наш первый бюджет разложен по конвертам: на нужды, желания и надежную копилку.",
                emoji = "📊",
                category = "Финансы",
                petReactionEmoji = "🎯",
                tiltDegrees = 1.8f,
                cardColorHex = 0xFFF0FDF4
            ),
            MemoryPhoto(
                id = "mem_first_savings",
                title = "Первые монеты в копилке",
                dateFormatted = "Период ${state.currentPeriod}",
                caption = "Цель «${state.activeGoal?.title ?: "Мечта"}» становится ближе с каждой сохранённой монеткой!",
                emoji = "🪙",
                category = "Копилка",
                petReactionEmoji = "🚀",
                tiltDegrees = -1.5f,
                cardColorHex = 0xFFFEF3C7
            ),
            MemoryPhoto(
                id = "mem_supermarket",
                title = "Умный шопинг",
                dateFormatted = "Супермаркет",
                caption = "Купили всё строго по списку и не поддались на уловки маркетологов у кассы!",
                emoji = "🛒",
                category = "Супермаркет",
                petReactionEmoji = "🥦",
                tiltDegrees = 2.2f,
                cardColorHex = 0xFFECFEFF
            ),
            MemoryPhoto(
                id = "mem_fraud_detective",
                title = "Детектив безопасности",
                dateFormatted = "Антифрод-лаб",
                caption = "Разоблачили подозрительный фишинговый сайт и сохранили все семейные сбережения.",
                emoji = "🛡️",
                category = "Безопасность",
                petReactionEmoji = "🕵️",
                tiltDegrees = -2.0f,
                cardColorHex = 0xFFEEF2FF
            ),
            MemoryPhoto(
                id = "mem_bank_vault",
                title = "Сила сложного процента",
                dateFormatted = "Банк FinPet",
                caption = "Открыли вклад в банке. Деньги делают деньги, когда работают на нас!",
                emoji = "🏦",
                category = "Инвестиции",
                petReactionEmoji = "📈",
                tiltDegrees = 1.2f,
                cardColorHex = 0xFFFAF5FF
            ),
            MemoryPhoto(
                id = "mem_cozy_room",
                title = "Уютное гнёздышко",
                dateFormatted = "Комната",
                caption = "Обустроили мягкий коврик, теплый торшер и слушаем шум дождя за окном.",
                emoji = "🛋️",
                category = "Уют",
                petReactionEmoji = "✨",
                tiltDegrees = -1.8f,
                cardColorHex = 0xFFFFF1F2
            )
        )
    }
}
