package com.airton.avoidminer.enchantment;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResonanceEnchantmentRulesTest {
    @Test
    public void resonantEchoLastsThreeSeconds() {
        assertEquals(60, ResonanceEnchantmentRules.ECHO_DURATION_TICKS);
    }

    @Test
    public void dissonanceLastsTwoAndAHalfSecondsPerLevel() {
        assertEquals(50, ResonanceEnchantmentRules.dissonanceDurationTicks(1));
        assertEquals(100, ResonanceEnchantmentRules.dissonanceDurationTicks(2));
    }

    @Test
    public void dissonanceDurationIsCappedAtLevelTwo() {
        assertEquals(100, ResonanceEnchantmentRules.dissonanceDurationTicks(3));
    }
}
