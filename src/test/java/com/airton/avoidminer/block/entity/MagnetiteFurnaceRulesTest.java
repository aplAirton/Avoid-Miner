package com.airton.avoidminer.block.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MagnetiteFurnaceRulesTest {
    @Test
    public void energyUpgradesMatchProcessorEfficiency() {
        assertEquals(100, MagnetiteFurnaceRules.energyPerItem(100, 0));
        assertEquals(80, MagnetiteFurnaceRules.energyPerItem(100, 1));
        assertEquals(70, MagnetiteFurnaceRules.energyPerItem(100, 2));
        assertEquals(60, MagnetiteFurnaceRules.energyPerItem(100, 3));
    }

    @Test
    public void speedUpgradesShortenEachTierCycle() {
        assertEquals(100, MagnetiteFurnaceRules.effectiveTicks(100, 0));
        assertEquals(66, MagnetiteFurnaceRules.effectiveTicks(100, 1));
        assertEquals(47, MagnetiteFurnaceRules.effectiveTicks(80, 2));
        assertEquals(30, MagnetiteFurnaceRules.effectiveTicks(60, 3));
    }

    @Test
    public void unsupportedUpgradeTiersFallBackToBaseValues() {
        assertEquals(80, MagnetiteFurnaceRules.energyPerItem(80, 99));
        assertEquals(60, MagnetiteFurnaceRules.effectiveTicks(60, -1));
    }
}
