package com.airton.avoidminer.item;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GlassSwordRulesTest {
    @Test
    public void chargedWaveRequiresOneSecondAndMatchesTheTierOneCannonRange() {
        assertFalse(GlassSwordRules.isCharged(19));
        assertTrue(GlassSwordRules.isCharged(20));
        assertEquals(15, GlassSwordRules.WAVE_RANGE);
        assertEquals((int) HypersonicCannonRules.TIER_1.range(), GlassSwordRules.WAVE_RANGE);
        assertEquals(16.0F, GlassSwordRules.BASE_DAMAGE, 0.0001F);
    }
}
