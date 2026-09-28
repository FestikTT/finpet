# RuStore & Jetpack Compose Proguard Rules for ФинПет (ru.finpet.app)

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Jetpack Compose
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }
-keepclassmembers class androidx.compose.material3.** { *; }

# RuStore SDK
-keep class ru.rustore.sdk.** { *; }
-keep interface ru.rustore.sdk.** { *; }
-keep class ru.finpet.app.rustore.** { *; }

# Data Models & Room Entities
-keep class ru.finpet.app.model.** { *; }
-keep class ru.finpet.app.data.** { *; }
-keepclassmembers class ru.finpet.app.data.db.** { *; }

# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
