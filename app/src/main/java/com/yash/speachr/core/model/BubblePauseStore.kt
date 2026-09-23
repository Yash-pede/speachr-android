package com.yash.speachr.core.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls whether the floating bubble (and the foreground service that hosts it) is allowed
 * to run.
 *
 * States:
 *  - `pausedUntil == 0`        → bubble is active
 *  - `pausedUntil == INDEFINITE` → paused until the user turns it back on (no auto-resume)
 *  - `pausedUntil > 0`         → paused until that epoch timestamp (auto-resumes)
 *
 * Everything is persisted so a timed pause survives process death. The accessibility service
 * checks [isPaused] before starting the foreground service, so while paused the service is
 * neither kept alive nor restarted.
 */
object BubblePauseStore {

    /** Sentinel timestamp: paused with no automatic resume. */
    const val INDEFINITE = -1L

    internal const val PAUSE_PREF_KEY = "bubble_paused_until"

    private var prefs: SharedPreferences? = null

    private val _pausedUntil = MutableStateFlow(0L)

    /**
     * Epoch millis when the pause lifts, [INDEFINITE] when paused with no auto-resume,
     * or `0` while the bubble is active.
     */
    val pausedUntil: StateFlow<Long> = _pausedUntil.asStateFlow()

    private fun prefs(context: Context): SharedPreferences =
        prefs ?: context.applicationContext
            .getSharedPreferences(USER_SETTINGS_PREFS, Context.MODE_PRIVATE)
            .also { prefs = it }

    /** Seeds the in-memory value from disk. Call once from Application.onCreate(). */
    fun init(context: Context) {
        _pausedUntil.value = prefs(context).getLong(PAUSE_PREF_KEY, 0L)
    }

    /**
     * Suppresses the bubble. Pass a [durationMillis] to auto-resume later, or `null` to keep
     * it paused until the user manually re-enables it.
     */
    fun pause(context: Context, durationMillis: Long?) {
        val until = if (durationMillis == null) {
            INDEFINITE
        } else {
            System.currentTimeMillis() + durationMillis
        }
        _pausedUntil.value = until
        prefs(context).edit { putLong(PAUSE_PREF_KEY, until) }
    }

    /** Lifts the pause immediately. */
    fun resume(context: Context) {
        _pausedUntil.value = 0L
        prefs(context).edit { putLong(PAUSE_PREF_KEY, 0L) }
    }

    /**
     * Whether the bubble is currently suppressed. Timed pauses expire lazily here, so every
     * caller (settings UI, accessibility service) sees a consistent, self-healing answer.
     */
    fun isPaused(): Boolean {
        val until = _pausedUntil.value
        if (until == 0L) return false
        if (until == INDEFINITE) return true
        if (System.currentTimeMillis() >= until) {
            _pausedUntil.value = 0L
            return false
        }
        return true
    }

    /** Millis left in a timed pause, or null when active / paused indefinitely / expired. */
    fun remainingMillis(): Long? {
        val until = _pausedUntil.value
        if (until <= 0L) return null
        val remaining = until - System.currentTimeMillis()
        return if (remaining > 0) remaining else null
    }
}
