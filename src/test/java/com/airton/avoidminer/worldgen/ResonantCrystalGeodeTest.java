package com.airton.avoidminer.worldgen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ResonantCrystalGeodeTest {
    @Test
    public void geodeCopiesEveryAmethystGrowthStage() {
        String configured = read("data/avoidminer/worldgen/configured_feature/resonant_crystal_geode.json");
        contains(configured, "avoidminer:resonant_crystal_block");
        contains(configured, "avoidminer:budding_resonant_crystal");
        contains(configured, "avoidminer:small_resonant_crystal_bud");
        contains(configured, "avoidminer:medium_resonant_crystal_bud");
        contains(configured, "avoidminer:large_resonant_crystal_bud");
        contains(configured, "avoidminer:resonant_crystal_cluster");
    }

    @Test
    public void geodeIsHalfAsCommonAsVanillaAmethyst() {
        String placed = read("data/avoidminer/worldgen/placed_feature/resonant_crystal_geode.json");
        contains(placed, "\"chance\": 48");
        String modifier = read("data/avoidminer/neoforge/biome_modifier/add_resonant_crystal_geode.json");
        contains(modifier, "\"step\": \"local_modifications\"");
    }

    @Test
    public void clusterSupportsSilkTouchAndFortuneDrops() {
        String loot = read("data/avoidminer/loot_table/blocks/resonant_crystal_cluster.json");
        contains(loot, "minecraft:silk_touch");
        contains(loot, "minecraft:fortune");
        contains(loot, "avoidminer:resonant_crystal");
    }

    private static String read(String path) {
        try (var stream = ResonantCrystalGeodeTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull("Missing resource: " + path, stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void contains(String value, String expected) {
        assertTrue("Expected resource to contain: " + expected, value.contains(expected));
    }
}
