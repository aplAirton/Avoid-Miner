package com.airton.avoidminer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResonantShieldRulesTest {
    @Test
    public void chargeStopsAtFiveAndEnablesRetaliation() {
        assertEquals(1, ResonantShieldRules.addCharge(0));
        assertEquals(5, ResonantShieldRules.addCharge(4));
        assertEquals(5, ResonantShieldRules.addCharge(5));
        assertFalse(ResonantShieldRules.isReady(4));
        assertTrue(ResonantShieldRules.isReady(5));
    }

    @Test
    public void retaliationRequiresTwoUsesInsideTheWindow() {
        assertTrue(ResonantShieldRules.isDoubleUse(100, 108));
        assertFalse(ResonantShieldRules.isDoubleUse(100, 109));
        assertFalse(ResonantShieldRules.isDoubleUse(-1, 1));
    }
}
