package ru.finpet.app.model

enum class ShopCategory(val title: String) {
    FOOD("Еда"),
    HYGIENE("Уход"),
    WALLPAPER("Обои и пол"),
    ACCESSORIES("Украшения");

    companion object {
        val TOYS get() = ACCESSORIES
        val FURNITURE get() = WALLPAPER
        val CARE get() = HYGIENE
        val MEDICINE get() = HYGIENE
        val TOY get() = ACCESSORIES
        val OUTFIT get() = ACCESSORIES
        val ENTERTAINMENT get() = ACCESSORIES
    }
}

enum class RoomSlotType(val title: String) {
    WALLPAPER("Обои"),
    FLOOR("Пол"),
    POSTER("Постер на стену");

    companion object {
        val TOY get() = POSTER
        val WARDROBE get() = POSTER
        val DESK get() = POSTER
        val BED get() = POSTER
        val LAMP get() = POSTER
    }
}

data class ShopItem(
    val id: String,
    val name: String,
    val basePrice: Int,
    val category: ShopCategory,
    val icon: String,
    val discountPercent: Int = 0,
    val hungerBoost: Float = 0f,
    val happinessBoost: Float = 0f,
    val careBoost: Float = 0f,
    val energyBoost: Float = 0f,
    val isMandatory: Boolean = true,
    val tip: String = "",
    val roomSlotType: RoomSlotType? = null,
    val iconRes: Int? = null
) {
    val price: Int
        get() = if (discountPercent > 0) {
            (basePrice * (100 - discountPercent) / 100).coerceAtLeast(1)
        } else {
            basePrice
        }
}
