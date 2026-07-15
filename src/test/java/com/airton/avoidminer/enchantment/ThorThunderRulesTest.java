package com.airton.avoidminer.enchantment;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThorThunderRulesTest {
    @Test
    public void chargeCooldownAndDamageMatchSpecification() {
        assertEquals(10, ThorThunderRules.CHARGE_TICKS);
        assertEquals(40, ThorThunderRules.COOLDOWN_TICKS);
        assertEquals(10.0F, ThorThunderRules.DAMAGE, 0.0F);
        assertEquals(0.15, ThorThunderRules.ELECTRIC_CORE_DROP_CHANCE, 0.0);
    }

    @Test
    public void chainTargetsIncreaseAtLevelsTwoAndThree() {
        assertEquals(0, ThorThunderRules.additionalTargets(1));
        assertEquals(2, ThorThunderRules.additionalTargets(2));
        assertEquals(4, ThorThunderRules.additionalTargets(3));
    }

    @Test
    public void hammerProvidesLevelOneAndOnlyTwoAndThreeImproveIt() {
        assertEquals(1, ThorThunderRules.effectiveLevel(0));
        assertEquals(1, ThorThunderRules.effectiveLevel(1));
        assertEquals(2, ThorThunderRules.effectiveLevel(2));
        assertEquals(3, ThorThunderRules.effectiveLevel(3));
        assertEquals(3, ThorThunderRules.effectiveLevel(99));
    }
}
