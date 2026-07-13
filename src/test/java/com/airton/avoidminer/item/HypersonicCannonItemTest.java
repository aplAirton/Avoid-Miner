package com.airton.avoidminer.item;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HypersonicCannonItemTest {
    @Test
    public void weaponTimingMatchesSpecification() {
        assertEquals(34, HypersonicCannonRules.CHARGE_TICKS);
        assertEquals(200, HypersonicCannonRules.COOLDOWN_TICKS);
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
}
