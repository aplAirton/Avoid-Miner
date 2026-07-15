package com.airton.avoidminer.item;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GlassSwordRulesTest {
    @Test
    public void chargedWaveRequiresOneSecondAndMatchesTheTierOneCannonRange() {
        assertEquals(20, GlassSwordRules.getChargeTicks(0));
        assertEquals(15, GlassSwordRules.WAVE_RANGE);
        assertEquals((int) HypersonicCannonRules.TIER_1.range(), GlassSwordRules.WAVE_RANGE);
        assertEquals(16.0F, GlassSwordRules.BASE_DAMAGE, 0.0001F);
    }
}
