package ru.finpet.app

import android.app.Application
import ru.finpet.app.audio.SoundHapticManager
import ru.finpet.app.data.GameRepository
import ru.finpet.app.rustore.RuStoreManager

class FinPetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация базы данных и репозитория
        GameRepository.init(this)
        // Инициализация RuStore менеджера
        RuStoreManager.init(this)
        // Инициализация звуков и виброотклика
        SoundHapticManager.init(this)
    }
}
