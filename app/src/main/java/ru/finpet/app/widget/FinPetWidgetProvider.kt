package ru.finpet.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.finpet.app.MainActivity
import ru.finpet.app.R
import ru.finpet.app.data.GameRepository

class FinPetWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, FinPetWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                updateWidgets(context, appWidgetManager, appWidgetIds)
            }
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                val state = GameRepository.gameState.value
                val pet = state.pet

                for (widgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_finpet)

                    // Данные питомца
                    views.setTextViewText(R.id.tv_pet_emoji, pet.type.avatarEmoji)
                    views.setTextViewText(R.id.tv_pet_name, pet.name)
                    views.setTextViewText(R.id.tv_pet_level, "Ур. ${pet.level}")
                    views.setTextViewText(R.id.tv_pet_mood, "${pet.currentMood.emoji} ${pet.currentMood.title}")

                    // Балансы
                    views.setTextViewText(R.id.tv_widget_coins, "🪙 ${state.totalCoins}")
                    views.setTextViewText(R.id.tv_widget_savings, "🎯 ${state.totalSavingsAmount}")

                    // Сытость
                    val hungerPercent = (pet.hunger * 100).toInt().coerceIn(0, 100)
                    views.setTextViewText(R.id.tv_hunger_label, "Сытость: $hungerPercent%")
                    views.setProgressBar(R.id.pb_hunger, 100, hungerPercent, false)

                    // Нажатие на виджет открывает приложение
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            }
        }

        /**
         * Запрос на закрепление виджета на рабочем столе Android (API 26+)
         */
        fun requestPinWidget(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = context.getSystemService(AppWidgetManager::class.java) ?: return false
                val myProvider = ComponentName(context, FinPetWidgetProvider::class.java)

                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val successIntent = Intent(context, MainActivity::class.java)
                    val successPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        successIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    return appWidgetManager.requestPinAppWidget(myProvider, null, successPendingIntent)
                }
            }
            return false
        }
    }
}
