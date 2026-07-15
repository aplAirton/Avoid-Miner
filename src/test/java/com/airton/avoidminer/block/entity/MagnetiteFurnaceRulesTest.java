package com.airton.avoidminer.block.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MagnetiteFurnaceRulesTest {
    @Test
    public void rawOreBlocksConsumeEightTimesTheNormalEnergy() {
        assertEquals(40, MagnetiteFurnaceRules.energyPerItem(200, 0.2F, 0, false));
        assertEquals(320, MagnetiteFurnaceRules.energyPerItem(200, 0.2F, 0, true));
        assertEquals(640, MagnetiteFurnaceRules.energyPerItem(100, 0.8F, 0, true));
        assertEquals(1_280, MagnetiteFurnaceRules.energyPerItem(50, 3.2F, 0, true));
    }

    @Test
    public void energyUpgradesAlsoApplyToRawOreBlocks() {
        assertEquals(256, MagnetiteFurnaceRules.energyPerItem(200, 0.2F, 1, true));
        assertEquals(224, MagnetiteFurnaceRules.energyPerItem(200, 0.2F, 2, true));
        assertEquals(192, MagnetiteFurnaceRules.energyPerItem(200, 0.2F, 3, true));
    }

    @Test
    public void speedUpgradesShortenEachTierCycle() {
        assertEquals(200, MagnetiteFurnaceRules.effectiveTicks(200, 0));
        assertEquals(133, MagnetiteFurnaceRules.effectiveTicks(200, 1));
        assertEquals(58, MagnetiteFurnaceRules.effectiveTicks(100, 2));
        assertEquals(25, MagnetiteFurnaceRules.effectiveTicks(50, 3));
    }

    @Test
    public void unsupportedUpgradeTiersFallBackToBaseValues() {
        assertEquals(80, MagnetiteFurnaceRules.energyPerItem(100, 0.8F, 99, false));
        assertEquals(50, MagnetiteFurnaceRules.effectiveTicks(50, -1));
    }
}
