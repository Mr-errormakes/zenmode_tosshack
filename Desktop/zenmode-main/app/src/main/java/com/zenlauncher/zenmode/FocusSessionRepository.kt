package com.zenlauncher.zenmode

import android.content.Context

/**
 * Persists focus session state to SharedPreferences.
 * Both the FocusTimerService and Activity read from the same prefs key
 * so the countdown survives screen-off / process lifecycle.
 */
object FocusSessionRepository {

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(AppConstants.FOCUS_PREFS, Context.MODE_PRIVATE)

    /** Begin a session. Replaces any existing session. */
    fun startSession(ctx: Context, durationMinutes: Int) {
        prefs(ctx).edit()
            .putLong(AppConstants.FOCUS_KEY_START_MS, System.currentTimeMillis())
            .putLong(AppConstants.FOCUS_KEY_DURATION_MS, durationMinutes * 60_000L)
            .apply()
    }

    /** Clear the persisted session (called on end or expiry). */
    fun endSession(ctx: Context) {
        prefs(ctx).edit()
            .remove(AppConstants.FOCUS_KEY_START_MS)
            .remove(AppConstants.FOCUS_KEY_DURATION_MS)
            .apply()
    }

    /**
     * Returns the active [FocusSession] or null if none exists or it has expired.
     */
    fun getActiveSession(ctx: Context): FocusSession? {
        val p = prefs(ctx)
        val startMs = p.getLong(AppConstants.FOCUS_KEY_START_MS, -1L)
        val durationMs = p.getLong(AppConstants.FOCUS_KEY_DURATION_MS, 0L)
        if (startMs < 0L || durationMs <= 0L) return null
        val session = FocusSession(startMs, durationMs)
        if (!session.isActive) {
            endSession(ctx) // auto-clean
            return null
        }
        return session
    }

    fun isSessionActive(ctx: Context): Boolean = getActiveSession(ctx) != null
}
