package ru.finpet.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Менеджер виброотклика (Haptics) и чистого процедурного звукового дизайна (SoundPool / AudioTrack).
 * Процедурно синтезирует хрустальный звон монет, победный аккорд и мягкие клики без тяжелых медиа-файлов.
 */
object SoundHapticManager {

    private var vibrator: Vibrator? = null
    var isHapticsEnabled: Boolean = true
    var isSoundEnabled: Boolean = true

    private var coinTrack: AudioTrack? = null
    private var successTrack: AudioTrack? = null
    private var clickTrack: AudioTrack? = null
    private var petHappyTrack: AudioTrack? = null
    private var ambientTrack: AudioTrack? = null
    @Volatile
    var isAmbientPlaying: Boolean = false
        private set

    fun init(context: Context) {
        val appContext = context.applicationContext
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        // Процедурный синтез звуков в фоновом потоке при запуске (0 ms задержки при воспроизведении)
        thread(start = true, isDaemon = true, name = "ProceduralAudioInit") {
            try {
                coinTrack = createTrack(generateDualChime(1760.0, 2637.0, 110))
                successTrack = createTrack(generateArpeggio(listOf(1046.5, 1318.5, 1567.9, 2093.0), 65))
                clickTrack = createTrack(generateClickTone(520.0, 16))
                petHappyTrack = createTrack(generateWarmChirp(600.0, 950.0, 120))
            } catch (_: Exception) {}
        }
    }

    // ==========================================
    // УЮТНАЯ ФОНОВАЯ LO-FI МУЗЫКА
    // ==========================================

    fun startAmbientMusic() {
        if (!isSoundEnabled) return
        thread(start = true, isDaemon = true, name = "AmbientMusicPlayer") {
            try {
                if (ambientTrack == null) {
                    val pcm = generateLofiAmbientMusic(6000)
                    ambientTrack = createTrack(pcm, sampleRate = 22050)
                }
                val track = ambientTrack ?: return@thread
                track.setVolume(0.40f)
                track.setLoopPoints(0, track.bufferSizeInFrames, -1)
                track.play()
                isAmbientPlaying = true
            } catch (_: Exception) {
                isAmbientPlaying = false
            }
        }
    }

    fun stopAmbientMusic() {
        try {
            ambientTrack?.pause()
            ambientTrack?.flush()
            isAmbientPlaying = false
        } catch (_: Exception) {}
    }

    fun toggleAmbientMusic(): Boolean {
        return if (isAmbientPlaying) {
            stopAmbientMusic()
            false
        } else {
            startAmbientMusic()
            true
        }
    }

    // ==========================================
    // ТАКТИЛЬНЫЙ ВИБРООТКЛИК (Haptics Taxonomy)
    // ==========================================

    /**
     * Короткий четкий щелчок при нажатии на кнопки и приземлении питомца (iOS Taptic Tap)
     */
    fun performTapHaptic() {
        if (!isHapticsEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(12, 160))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    /**
     * Псевдоним для стандартного клика
     */
    fun performClickHaptic() = performTapHaptic()

    /**
     * Легкая дискретная механическая трещотка (для ползунков и пролетающих монеток)
     */
    fun performTickHaptic() {
        if (!isHapticsEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(8, 80))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(8)
            }
        } catch (_: Exception) {}
    }

    /**
     * Плотный увесистый тактильный щелчок (удар коробки о пол, фиксация покупки, разблокировка)
     */
    fun performHeavyClickHaptic() {
        if (!isHapticsEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(28, 240))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(28)
            }
        } catch (_: Exception) {}
    }

    /**
     * Мягкое приятное мурлыканье при поглаживании питомца
     */
    fun performPetHaptic() {
        vibratePattern(
            timings = longArrayOf(0, 18, 45, 22, 45, 28),
            amplitudes = intArrayOf(0, 120, 0, 150, 0, 190)
        )
    }

    /**
     * Торжественная двойная победная виброотдача (Duolingo double bounce success)
     */
    fun performSuccessHaptic() {
        vibratePattern(
            timings = longArrayOf(0, 25, 45, 40),
            amplitudes = intArrayOf(0, 190, 0, 255)
        )
    }

    /**
     * Предупреждающий двойной глухой стук («ту-тум» — ошибка, нехватка монет, превышение лимита)
     */
    fun performErrorHaptic() {
        vibratePattern(
            timings = longArrayOf(0, 35, 60, 35),
            amplitudes = intArrayOf(0, 230, 0, 210)
        )
    }

    /**
     * Праздничная победная вибрация при закрытии дневной нормы / 100% цели
     */
    fun performStreakHaptic() {
        vibratePattern(
            timings = longArrayOf(0, 35, 40, 35, 40, 60, 50, 90),
            amplitudes = intArrayOf(0, 160, 0, 180, 0, 220, 0, 255)
        )
    }

    /**
     * Глухой тактильный отклик при касании заблокированного элемента
     */
    fun performLockHaptic() {
        performTickHaptic()
    }

    private fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        if (!isHapticsEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }

    // ==========================================
    // ПРОЦЕДУРНЫЕ ЗВУКОВЫЕ ЭФФЕКТЫ
    // ==========================================

    /** Звон золотых монеток (хрустальный перезвон A6-E7) */
    fun playCoinSound() {
        playTrack(coinTrack)
    }

    /** Довольный звук радости питомца (теплый дружелюбный чирп) */
    fun playPetHappySound() {
        playTrack(petHappyTrack)
    }

    /** Деликатный легкий клик */
    fun playClickSound() {
        playTrack(clickTrack)
    }

    /** Торжественный победный аккорд (мажорное арпеджио C-E-G-C) */
    fun playSuccessSound() {
        playTrack(successTrack)
    }

    private fun playTrack(track: AudioTrack?) {
        if (!isSoundEnabled || track == null) return
        try {
            track.stop()
            track.reloadStaticData()
            track.play()
        } catch (_: Exception) {}
    }

    private fun createTrack(pcmData: ShortArray, sampleRate: Int = 44100): AudioTrack? {
        return try {
            val byteCount = pcmData.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(byteCount)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(pcmData, 0, pcmData.size)
            track
        } catch (_: Exception) {
            null
        }
    }

    private fun generateLofiAmbientMusic(durationMs: Int): ShortArray {
        val sampleRate = 22050
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)

        // 4 такта умиротворяющей гармонии (Cmaj9 -> Am9 -> Fmaj7 -> G6)
        val barCount = 4
        val barSamples = count / barCount

        val chords = listOf(
            // Cmaj9
            listOf(130.81, 196.00, 246.94, 329.63, 587.33),
            // Am9
            listOf(110.00, 164.81, 196.00, 261.63, 493.88),
            // Fmaj7
            listOf(87.31, 130.81, 164.81, 220.00, 329.63),
            // G6
            listOf(98.00, 146.83, 196.00, 246.94, 392.00)
        )

        for (b in 0 until barCount) {
            val barStart = b * barSamples
            val chordNotes = chords[b]

            for (i in 0 until barSamples) {
                val globalIdx = barStart + i
                if (globalIdx >= count) break
                val t = i.toDouble() / sampleRate
                val barProgress = i.toDouble() / barSamples

                // Мягкое затухание аккорда в такте
                val chordEnv = exp(-1.4 * barProgress) * (1.0 - 0.15 * sin(PI * barProgress))

                var sampleVal = 0.0

                // Бас и гармонический пад (тёплый электропиано звук)
                for ((noteIdx, freq) in chordNotes.withIndex()) {
                    val weight = when (noteIdx) {
                        0 -> 0.30 // Бас
                        1, 2 -> 0.22 // Средний голос
                        else -> 0.18 // Верхний колокольчик
                    }
                    val sine1 = sin(2.0 * PI * freq * t)
                    val sine2 = 0.35 * sin(4.0 * PI * freq * t)
                    val sine3 = 0.12 * sin(6.0 * PI * freq * t)
                    sampleVal += (sine1 + sine2 + sine3) * weight * chordEnv
                }

                // Арпеджио-нотки (нежные переливы в такте на 0%, 35%, 70%)
                val pluckTimes = listOf(0.0, 0.35, 0.70)
                val pluckNotes = listOf(chordNotes[2], chordNotes[3], chordNotes[4])
                for (p in pluckTimes.indices) {
                    val pluckStartSec = pluckTimes[p] * (barSamples.toDouble() / sampleRate)
                    val pluckT = t - pluckStartSec
                    if (pluckT >= 0) {
                        val pluckEnv = exp(-4.5 * pluckT)
                        val pluckFreq = pluckNotes[p]
                        val pluckSine = sin(2.0 * PI * pluckFreq * pluckT) + 0.25 * sin(4.0 * PI * pluckFreq * pluckT)
                        sampleVal += pluckSine * 0.20 * pluckEnv
                    }
                }

                val finalAmpl = (sampleVal * 0.24 * Short.MAX_VALUE).toInt()
                out[globalIdx] = finalAmpl.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }

        // Мягкий фейдинг в начале и конце для исключения щелчков при бесконечном зацикливании
        val fadeSamples = (sampleRate * 0.05).toInt()
        for (i in 0 until fadeSamples) {
            val fade = i.toDouble() / fadeSamples
            out[i] = (out[i] * fade).toInt().toShort()
            out[count - 1 - i] = (out[count - 1 - i] * fade).toInt().toShort()
        }

        return out
    }

    private fun generateDualChime(f1: Double, f2: Double, durationMs: Int): ShortArray {
        val sampleRate = 44100
        val count = (sampleRate * durationMs) / 1000
        val half = count / 2
        val out = ShortArray(count)
        for (i in 0 until count) {
            val freq = if (i < half) f1 else f2
            val t = (i % half).toDouble() / sampleRate
            val progress = (i % half).toDouble() / half
            val envelope = (1.0 - progress) * (1.0 - progress)
            val v = sin(2.0 * PI * freq * t) * envelope * 0.45 * Short.MAX_VALUE
            out[i] = v.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun generateArpeggio(notes: List<Double>, noteDurationMs: Int): ShortArray {
        val sampleRate = 44100
        val noteSamples = (sampleRate * noteDurationMs) / 1000
        val totalSamples = noteSamples * notes.size
        val out = ShortArray(totalSamples)
        for ((idx, freq) in notes.withIndex()) {
            val start = idx * noteSamples
            for (i in 0 until noteSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / noteSamples
                val envelope = exp(-3.2 * progress)
                val v = sin(2.0 * PI * freq * t) * envelope * 0.38 * Short.MAX_VALUE
                out[start + i] = v.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return out
    }

    private fun generateClickTone(freq: Double, durationMs: Int): ShortArray {
        val sampleRate = 44100
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / count
            val envelope = (1.0 - progress) * (1.0 - progress)
            val v = sin(2.0 * PI * freq * t) * envelope * 0.30 * Short.MAX_VALUE
            out[i] = v.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun generateWarmChirp(startFreq: Double, endFreq: Double, durationMs: Int): ShortArray {
        val sampleRate = 44100
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val freq = startFreq + (endFreq - startFreq) * progress
            val t = i.toDouble() / sampleRate
            val envelope = sin(PI * progress)
            val v = sin(2.0 * PI * freq * t) * envelope * 0.35 * Short.MAX_VALUE
            out[i] = v.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }
}
