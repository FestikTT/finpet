package ru.finpet.app.model

enum class PetType(
    val title: String,
    val avatarEmoji: String,
    val shortDesc: String,
    val specialBonus: String
) {
    FOX("Лисёнок", "🦊", "Любознательный и сообразительный друг", "+5% к сбережениям"),
    CAT("Котёнок", "🐱", "Аккуратный и бережливый хранитель", "+10% к защите от импульсивных трат"),
    PANDA("Панда", "🐼", "Спокойный и рассудительный накопитель", "+15% опыта за выполнение плана")
}

enum class PetColor(
    val title: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
) {
    ORANGE("Огненно-рыжий", 0xFFFF8C00, 0xFFFFE0B2),
    GOLDEN("Солнечно-золотой", 0xFFF59E0B, 0xFFFEF3C7),
    SNOW("Снежно-белый", 0xFFE0E7FF, 0xFFFFFFFF)
}

enum class PetAccessory(
    val title: String,
    val emoji: String
) {
    NONE("Без аксессуара", ""),
    GLASSES("Очки", "🕶️"),
    CAP("Бейсболка", "🧢"),
    SCARF("Шарф", "🧣"),
    BOW("Бабочка", "🎀"),
    COLLAR("Ошейник", "🏷️"),
    CROWN("Корона", "👑"),
    ROYAL_MANTLE("Мантия", "🧣"),
    ROYAL_SCEPTER("Скипетр", "🪄"),
    VISOR("Смарт-визор", "🥽"),
    HEADBAND("Повязка", "🥷"),
    HEADPHONES("Наушники", "🎧"),
    WIZARD_HAT("Шляпа", "🧙"),
    BADGE("Орден", "⭐"),
    EMERALD_GEM("Изумруд", "💎"),
    FLOWER_WREATH("Венок", "🌸"),
    POCKET_WATCH("Хронометр", "⏱️");

    companion object {
        val starterAccessories = listOf(NONE, GLASSES, CAP, SCARF, BOW)
    }
}

enum class PetMood(
    val title: String,
    val emoji: String,
    val defaultDescription: String
) {
    HAPPY("Счастлив", "😄", "Питомец полон сил и радуется грамотным финансовым решениям!"),
    CONTENT("Доволен", "🙂", "Все идет по плану: питомец накормлен, бюджет сбалансирован"),
    HUNGRY("Проголодался", "🥺", "Пора подкрепиться! Загляни в магазин за полезной едой"),
    TIRED("Устал", "🥱", "Питомец потратил много энергии на игры и обучение, нужен отдых"),
    SLEEPING("Спит", "😴", "Сладко спит и восстанавливает энергию на все 100%")
}

enum class EvolutionStage(
    val title: String,
    val stageIndex: Int,
    val minLevel: Int,
    val description: String
) {
    BABY("Малыш", 1, 1, "Учится отличать обязательные расходы от желаемых"),
    TEEN("Юный финансист", 2, 3, "Уверенно распределяет бюджет по трем конвертам и копит на цель"),
    MASTER("Финансовый эксперт", 3, 5, "Мастерски управляет накоплениями, справляется с форс-мажорами и не поддается скидкам")
}

data class Pet(
    val id: String = "pet_1",
    val name: String = "",
    val type: PetType = PetType.FOX,
    val color: PetColor = PetColor.ORANGE,
    val accessory: PetAccessory = PetAccessory.NONE,
    val level: Int = 1,
    val exp: Int = 20,
    val expToNextLevel: Int = 100,
    val hunger: Float = 0.85f,      // 0.0 .. 1.0 (1.0 = сыт)
    val happiness: Float = 0.80f,   // 0.0 .. 1.0
    val care: Float = 0.75f,        // 0.0 .. 1.0 (уход и здоровье)
    val energy: Float = 0.90f,      // 0.0 .. 1.0
    val finScore: Int = 75,         // 0 .. 100
    val evolutionStage: EvolutionStage = EvolutionStage.BABY,
    val currentMood: PetMood = PetMood.HAPPY,
    val moodExplanation: String = "Твой питомец счастлив! Он вкусно поел, а в копилке появились первые сбережения."
) {
    val isDirty: Boolean get() = care < 0.65f

    fun calculateMood(): PetMood {
        return when {
            energy <= 0.2f -> PetMood.TIRED
            hunger <= 0.3f -> PetMood.HUNGRY
            happiness >= 0.7f && hunger >= 0.6f && care >= 0.5f -> PetMood.HAPPY
            else -> PetMood.CONTENT
        }
    }

    fun getStage(): EvolutionStage {
        return when {
            level >= 5 -> EvolutionStage.MASTER
            level >= 3 -> EvolutionStage.TEEN
            else -> EvolutionStage.BABY
        }
    }
}
