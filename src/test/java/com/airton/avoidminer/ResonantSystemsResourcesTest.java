package com.airton.avoidminer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ResonantSystemsResourcesTest {
    @Test
    public void projectileDeflectionIsChestOnlyAndDescribedInBothLanguages() {
        String enchantment = read("data/avoidminer/enchantment/projectile_deflection.json");
        contains(enchantment, "\"slots\":");
        contains(enchantment, "\"chest\"");
        contains(enchantment, "#avoidminer:enchantable/projectile_deflection");
        contains(read("assets/avoidminer/lang/en_us.json"), "enchantment.avoidminer.projectile_deflection.desc");
        contains(read("assets/avoidminer/lang/pt_br.json"), "enchantment.avoidminer.projectile_deflection.desc");
    }

    @Test
    public void scannerAndRepairStationHaveRecipesAndModels() {
        String tier1 = read("data/avoidminer/recipe/resonant_scanner.json");
        String tier2 = read("data/avoidminer/recipe/resonant_scanner_tier_2.json");
        String tier3 = read("data/avoidminer/recipe/resonant_scanner_tier_3.json");
        contains(tier1, "avoidminer:resonant_scanner");
        contains(tier1, "minecraft:sculk_shrieker");
        contains(tier1, "avoidminer:mining_core");
        assertTrue(!tier1.contains("minecraft:echo_shard"));
        contains(tier2, "avoidminer:resonant_scanner");
        contains(tier2, "avoidminer:resonant_scanner_tier_2");
        contains(tier3, "avoidminer:resonant_scanner_tier_2");
        contains(tier3, "avoidminer:resonant_scanner_tier_3");
        contains(read("data/avoidminer/recipe/resonant_repair_station.json"), "avoidminer:resonant_repair_station");
        contains(read("assets/avoidminer/items/resonant_scanner.json"),
                "avoidminer:item/resonant_scanner");
        contains(read("assets/avoidminer/items/resonant_scanner_tier_2.json"),
                "avoidminer:item/resonant_scanner_tier_2");
        contains(read("assets/avoidminer/items/resonant_scanner_tier_3.json"),
                "avoidminer:item/resonant_scanner_tier_3");
        contains(read("assets/avoidminer/items/resonant_repair_station.json"),
                "avoidminer:item/resonant_repair_station");
    }

    private static String read(String path) {
        try (var stream = ResonantSystemsResourcesTest.class.getClassLoader().getResourceAsStream(path)) {
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
