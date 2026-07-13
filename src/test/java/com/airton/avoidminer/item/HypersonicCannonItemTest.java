package com.airton.avoidminer.item;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HypersonicCannonItemTest {
    @Test
    public void weaponTimingMatchesSpecification() {
        assertEquals(34, HypersonicCannonRules.TIER_1.chargeTicks());
        assertEquals(200, HypersonicCannonRules.TIER_1.cooldownTicks());
        assertEquals(160, HypersonicCannonRules.TIER_2.cooldownTicks());
        assertEquals(120, HypersonicCannonRules.TIER_3.cooldownTicks());
    }

    @Test
    public void sonicTrailUsesWardenSpacing() {
        assertEquals(21, HypersonicCannonRules.particleCount(15.0));
    }

    @Test
    public void knockbackRespectsTargetResistance() {
        assertEquals(2.5, HypersonicCannonRules.adjustedKnockback(2.5, 0.0), 0.0001);
        assertEquals(1.25, HypersonicCannonRules.adjustedKnockback(2.5, 0.5), 0.0001);
        assertEquals(0.0, HypersonicCannonRules.adjustedKnockback(2.5, 1.0), 0.0001);
    }

    @Test
    public void upgradedDamageFallsWithDistance() {
        assertEquals(14.0, HypersonicCannonRules.damageAtDistance(HypersonicCannonRules.TIER_2, 0.0), 0.0001);
        assertEquals(10.5, HypersonicCannonRules.damageAtDistance(HypersonicCannonRules.TIER_2, 10.0), 0.0001);
        assertEquals(7.0, HypersonicCannonRules.damageAtDistance(HypersonicCannonRules.TIER_2, 20.0), 0.0001);
    }

    @Test
    public void tierThreeAddsWideConePenetrationAndRicochets() {
        assertEquals(12.0, HypersonicCannonRules.TIER_3.coneDegrees(), 0.0001);
        assertEquals(8, HypersonicCannonRules.TIER_3.maxTargets());
        assertEquals(2, HypersonicCannonRules.TIER_3.ricochets());
    }
}
