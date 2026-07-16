package com.airton.avoidminer.worldgen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MagnetiteWorldgenTest {
    @Test
    public void oreUsesTheConfiguredBalancedVeins() {
        String configured = readResource(
                "data/avoidminer/worldgen/configured_feature/magnetite_ore.json");

        assertContains(configured, "\"type\": \"avoidminer:configurable_magnetite_ore\"");
        assertContains(configured, "\"size\": 8");
        assertContains(configured, "\"discard_chance_on_air_exposure\": 0.8");
        assertContains(configured, "minecraft:stone_ore_replaceables");
        assertContains(configured, "minecraft:deepslate_ore_replaceables");
    }

    @Test
    public void oreIsDistributedFromZeroToThirty() {
        String placed = readResource(
                "data/avoidminer/worldgen/placed_feature/magnetite_ore.json");

        assertContains(placed, "\"type\": \"avoidminer:magnetite_count\"");
        assertContains(placed, "\"type\": \"avoidminer:magnetite_height\"");
        assertContains(placed, "\"type\": \"minecraft:biome\"");
    }

    @Test
    public void biomeModifierAddsTheFeatureToOverworldOres() {
        String modifier = readResource(
                "data/avoidminer/neoforge/biome_modifier/add_magnetite_ore.json");

        assertContains(modifier, "\"type\": \"neoforge:add_features\"");
        assertContains(modifier, "\"biomes\": \"#minecraft:is_overworld\"");
        assertContains(modifier, "\"features\": \"avoidminer:magnetite_ore\"");
        assertContains(modifier, "\"step\": \"underground_ores\"");
    }

    private static String readResource(String path) {
        try (var stream = MagnetiteWorldgenTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull("Missing resource: " + path, stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Could not read resource: " + path, exception);
        }
    }

    private static void assertContains(String value, String expected) {
        assertTrue("Expected resource to contain: " + expected, value.contains(expected));
    }
}
