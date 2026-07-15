package com.airton.avoidminer.item;

import com.airton.avoidminer.enchantment.SonicProtectionRules;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HypersonicCannonItemTest {
    @Test
    public void weaponTimingMatchesSpecification() {
        assertEquals(34, HypersonicCannonRules.TIER_1.chargeTicks());
        assertEquals(200, HypersonicCannonRules.TIER_1.cooldownTicks());
        assertEquals(160, HypersonicCannonRules.TIER_2.cooldownTicks());
        assertEquals(120, HypersonicCannonRules.TIER_3.cooldownTicks());
    }

    @Test
    public void cannonDurabilityProgressesByTier() {
        assertEquals(100, HypersonicCannonRules.TIER_1.durability());
        assertEquals(200, HypersonicCannonRules.TIER_2.durability());
        assertEquals(300, HypersonicCannonRules.TIER_3.durability());
    }

    @Test
    public void temporalClockHasBalancedLimits() {
        assertEquals(100, TemporalFreezeRules.DURABILITY);
        assertEquals(160, TemporalFreezeRules.FREEZE_TICKS);
        assertEquals(600, TemporalFreezeRules.COOLDOWN_TICKS);
    }

    @Test
    public void sonicProtectionUsesVanillaProtectionReductionAndCap() {
        assertEquals(8.4, SonicProtectionRules.reduceDamage(10.0F, 4), 0.0001);
        assertEquals(3.6, SonicProtectionRules.reduceDamage(10.0F, 16), 0.0001);
        assertEquals(2.0, SonicProtectionRules.reduceDamage(10.0F, 24), 0.0001);
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
        assertEquals(20.0, HypersonicCannonRules.TIER_3.coneDegrees(), 0.0001);
        assertEquals(8, HypersonicCannonRules.TIER_3.maxTargets());
        assertEquals(2, HypersonicCannonRules.TIER_3.ricochets());
    }

    @Test
    public void tierOneHitsWhenTheShotCrossesAnyPartOfTheTargetHitbox() {
        assertTrue(HypersonicCannonRules.coneIntersectsTarget(10.0, 0.8, 1.0, 15.0, 2.0));
    }

    @Test
    public void targetBehindShooterOrBeyondRangeIsRejected() {
        assertFalse(HypersonicCannonRules.coneIntersectsTarget(-2.0, 0.0, 0.5, 15.0, 2.0));
        assertFalse(HypersonicCannonRules.coneIntersectsTarget(17.0, 0.0, 0.5, 15.0, 2.0));
    }

    @Test
    public void targetOutsideTierOneConeAndHitboxIsRejected() {
        assertFalse(HypersonicCannonRules.coneIntersectsTarget(10.0, 2.0, 0.5, 15.0, 2.0));
    }
}
