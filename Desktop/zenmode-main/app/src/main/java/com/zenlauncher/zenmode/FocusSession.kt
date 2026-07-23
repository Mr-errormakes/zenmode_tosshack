package com.zenlauncher.zenmode

/**
 * Represents an active deep-focus session.
 *
 * @param startMs   Epoch milliseconds when session started
 * @param durationMs Total session length in milliseconds
 */
data class FocusSession(
    val startMs: Long,
    val durationMs: Long
) {
    val endMs: Long get() = startMs + durationMs

    /** Milliseconds remaining. Zero when session has expired. */
    val remainingMs: Long get() = (endMs - System.currentTimeMillis()).coerceAtLeast(0L)

    /** Remaining time as mm:ss string, e.g. "24:37" */
    val remainingFormatted: String
        get() {
            val totalSec = remainingMs / 1000L
            val mins = totalSec / 60L
            val secs = totalSec % 60L
            return "%02d:%02d".format(mins, secs)
        }

    /** Duration in whole minutes for display */
    val durationMinutes: Int get() = (durationMs / 60_000L).toInt()

    val isActive: Boolean get() = System.currentTimeMillis() < endMs

    /** Progress 0.0 → 1.0 (how much of the session has elapsed) */
    val elapsedFraction: Float
        get() = if (durationMs <= 0L) 1f
        else ((System.currentTimeMillis() - startMs).toFloat() / durationMs).coerceIn(0f, 1f)
}
