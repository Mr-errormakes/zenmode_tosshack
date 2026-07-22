package com.zenlauncher.zenmode

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLogicTest {

    @Test
    fun `getMoodState returns HAPPY when minutes are low (high mindfulness)`() {
        // 30 mins → percentage = (120-30)/120*100 = 75% → >= 70% HAPPY threshold
        val mood = AppLogic.getMoodState(30L)
        assertEquals(MoodState.HAPPY, mood)
    }

    @Test
    fun `getMoodState returns NEUTRAL when minutes are moderate`() {
        // 150 mins → between 120 and 210 minutes
        val mood = AppLogic.getMoodState(150L)
        assertEquals(MoodState.NEUTRAL, mood)
    }

    @Test
    fun `getMoodState returns ANNOYED when minutes above neutral threshold`() {
        val mood = AppLogic.getMoodState(AppConstants.THRESHOLD_NEUTRAL_MINUTES.toLong() + 10)
        assertEquals(MoodState.ANNOYED, mood)
    }

    @Test
    fun `getMindfulnessPercentage calcultes correctly`() {
        // 0 minutes -> 100%
        assertEquals(100, AppLogic.getMindfulnessPercentage(0L))
        
        // 105 minutes -> 50% (under 210 minutes max)
        assertEquals(50, AppLogic.getMindfulnessPercentage(105L))
        
        // 210 minutes -> 0%
        assertEquals(0, AppLogic.getMindfulnessPercentage(210L))
        
        // > 210 minutes -> 0%
        assertEquals(0, AppLogic.getMindfulnessPercentage(250L))
    }

    @Test
    fun `getMindfulnessColor returns correct color resource`() {
        // High percentage -> Happy color
        assertEquals(R.color.zen_mindfulness_happy, AppLogic.getMindfulnessColor(0L))
        
        // Low percentage -> Annoyed color
        assertEquals(R.color.zen_mindfulness_annoyed, AppLogic.getMindfulnessColor(220L))
    }
}
