package com.airton.avoidminer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SeismicBootsRulesTest {
    @Test
    public void impactOnlyArmsDuringAConsiderableSneakingFall() {
        assertTrue(SeismicBootsRules.shouldArm(true, false, -0.5, 4.0));
        assertFalse(SeismicBootsRules.shouldArm(false, false, -0.5, 8.0));
        assertFalse(SeismicBootsRules.shouldArm(true, false, -0.2, 8.0));
        assertFalse(SeismicBootsRules.shouldArm(true, true, -0.5, 8.0));
    }

    @Test
    public void impactAcceptsTrackedSneakOrShiftHeldAtLanding() {
        assertTrue(SeismicBootsRules.shouldTriggerImpact(true, false, 10.0));
        assertTrue(SeismicBootsRules.shouldTriggerImpact(false, true, 10.0));
        assertFalse(SeismicBootsRules.shouldTriggerImpact(false, false, 10.0));
        assertFalse(SeismicBootsRules.shouldTriggerImpact(true, true, 3.9));
    }

    @Test
    public void shockwaveScalesWithFallEnergyAndHasCaps() {
        assertEquals(8.0, SeismicBootsRules.shockwaveRadius(4.0), 0.0001);
        assertEquals(16.0, SeismicBootsRules.shockwaveRadius(100.0), 0.0001);
        assertEquals(6.0, SeismicBootsRules.shockwaveDamage(4.0), 0.0001);
        assertEquals(18.0, SeismicBootsRules.shockwaveDamage(100.0), 0.0001);
        assertEquals(12.0, SeismicBootsRules.damageAtDistance(12.0F, 0.0, 8.0), 0.0001);
        assertEquals(6.0, SeismicBootsRules.damageAtDistance(12.0F, 8.0, 8.0), 0.0001);
    }
}
