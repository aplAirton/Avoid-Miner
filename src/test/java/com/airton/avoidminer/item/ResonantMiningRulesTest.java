package com.airton.avoidminer.item;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResonantMiningRulesTest {
    @Test
    public void vanillaPickaxesUseTheEnchantmentRanges() {
        assertEquals(0, ResonantMiningRules.forwardRange(false, 0));
        assertEquals(4, ResonantMiningRules.forwardRange(false, 1));
        assertEquals(8, ResonantMiningRules.forwardRange(false, 2));
        assertEquals(16, ResonantMiningRules.forwardRange(false, 3));
    }

    @Test
    public void resonantPickaxeAddsItsNativeRange() {
        assertEquals(4, ResonantMiningRules.forwardRange(true, 0));
        assertEquals(8, ResonantMiningRules.forwardRange(true, 1));
        assertEquals(12, ResonantMiningRules.forwardRange(true, 2));
        assertEquals(20, ResonantMiningRules.forwardRange(true, 3));
    }

    @Test
    public void rangeAndDurabilityMatchTheSpecification() {
        assertEquals(2, ResonantMiningRules.SIDE_RADIUS);
        assertEquals(1_800, ResonantMiningRules.RESONANT_PICKAXE_DURABILITY);
    }
}
