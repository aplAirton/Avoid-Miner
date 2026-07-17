package com.airton.avoidminer.item;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        assertEquals(3_000, ResonantMiningRules.RESONANT_PICKAXE_DURABILITY);
        assertEquals(20, ResonantMiningRules.CHARGE_TICKS);
        assertEquals(2, ResonantMiningRules.BLOCKS_PER_DURABILITY);
        assertEquals(40, ResonantMiningRules.LAVA_STABILIZATION_TICKS);
        assertEquals(5, ResonantMiningRules.LAVA_RECHECK_INTERVAL);
    }

    @Test
    public void recipeConsumesANetheritePickaxe() throws IOException {
        try (var stream = getClass().getClassLoader()
                .getResourceAsStream("data/avoidminer/recipe/resonant_pickaxe.json")) {
            assertNotNull("Missing resonant pickaxe recipe", stream);
            String recipe = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(recipe.contains("\"D\": \"minecraft:diamond_pickaxe\""));
        }
    }
}
