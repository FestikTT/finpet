package ru.finpet.app.model

/**
 * Погодные режимы за окном в комнате питомца.
 */
enum class RoomWeather(val title: String, val icon: String) {
    RAIN("Уютный дождь", "🌧️"),
    SNOW("Мягкий снег", "❄️"),
    SUNNY("Солнечный день", "☀️");

    fun next(): RoomWeather {
        val values = entries
        val nextIdx = (ordinal + 1) % values.size
        return values[nextIdx]
    }

    companion object {
        fun fromId(id: String): RoomWeather {
            return entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: RAIN
        }
    }
}
