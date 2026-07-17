package com.airton.avoidminer.enchantment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NewEnchantmentsResourcesTest {
    @Test
    public void lumberjackFortuneMatchesFortuneAvailabilityAndLevels() {
        String enchantment = read("data/avoidminer/enchantment/lumberjack_fortune.json");
        contains(enchantment, "\"max_level\": 3");
        contains(enchantment, "\"weight\": 2");
        contains(enchantment, "\"base\": 15");
        contains(enchantment, "\"base\": 65");
        contains(enchantment, "\"supported_items\": \"#minecraft:axes\"");

        contains(read("data/minecraft/tags/enchantment/non_treasure.json"),
                "avoidminer:lumberjack_fortune");
        contains(read("data/minecraft/tags/enchantment/tradeable.json"),
                "avoidminer:lumberjack_fortune");
        contains(read("data/minecraft/tags/enchantment/in_enchanting_table.json"),
                "avoidminer:lumberjack_fortune");
    }

    @Test
    public void sonicChargeIsSingleLevelAndExcludesGlassSword() {
        String enchantment = read("data/avoidminer/enchantment/sonic_charge.json");
        String supportedItems = read("data/avoidminer/tags/item/enchantable/sonic_charge.json");
        contains(enchantment, "\"max_level\": 1");
        contains(supportedItems, "minecraft:netherite_sword");
        assertFalse(supportedItems.contains("avoidminer:glass_sword"));
        contains(read("data/minecraft/tags/enchantment/tradeable.json"), "avoidminer:sonic_charge");
        contains(read("data/minecraft/tags/enchantment/in_enchanting_table.json"), "avoidminer:sonic_charge");
        contains(read("data/avoidminer/loot_table/items/trial_chamber_enchantments.json"),
                "avoidminer:sonic_charge");
    }

    @Test
    public void enderUsesProgressiveCooldownAndConflictsWithSonicCharge() {
        String enchantment = read("data/avoidminer/enchantment/ender.json");
        String exclusiveSet = read(
                "data/avoidminer/tags/enchantment/exclusive_set/sword_active_ability.json");

        contains(enchantment, "\"max_level\": 3");
        contains(enchantment, "\"supported_items\": \"#minecraft:swords\"");
        contains(enchantment, "#avoidminer:exclusive_set/sword_active_ability");
        contains(exclusiveSet, "avoidminer:ender");
        contains(exclusiveSet, "avoidminer:sonic_charge");
        contains(read("data/avoidminer/enchantment/sonic_charge.json"),
                "#avoidminer:exclusive_set/sword_active_ability");

        org.junit.Assert.assertEquals(60, EnderCooldownRules.cooldownTicks(1));
        org.junit.Assert.assertEquals(30, EnderCooldownRules.cooldownTicks(2));
        org.junit.Assert.assertEquals(20, EnderCooldownRules.cooldownTicks(3));
    }

    @Test
    public void ghastBombIsSingleLevelAndBowOnly() {
        String enchantment = read("data/avoidminer/enchantment/ghast_bomb.json");
        contains(enchantment, "\"max_level\": 1");
        contains(enchantment, "\"supported_items\": \"#minecraft:enchantable/bow\"");
        contains(read("data/minecraft/tags/enchantment/tradeable.json"), "avoidminer:ghast_bomb");
        contains(read("data/minecraft/tags/enchantment/in_enchanting_table.json"),
                "avoidminer:ghast_bomb");
        contains(read("data/avoidminer/loot_table/items/trial_chamber_enchantments.json"),
                "avoidminer:ghast_bomb");
    }

    @Test
    public void descriptionsExistInBothLanguages() {
        String portuguese = read("assets/avoidminer/lang/pt_br.json");
        String english = read("assets/avoidminer/lang/en_us.json");
        contains(portuguese, "enchantment.avoidminer.lumberjack_fortune.desc");
        contains(portuguese, "enchantment.avoidminer.sonic_charge.desc");
        contains(portuguese, "enchantment.avoidminer.ender.desc");
        contains(portuguese, "enchantment.avoidminer.ghast_bomb.desc");
        contains(english, "enchantment.avoidminer.lumberjack_fortune.desc");
        contains(english, "enchantment.avoidminer.sonic_charge.desc");
        contains(english, "enchantment.avoidminer.ender.desc");
        contains(english, "enchantment.avoidminer.ghast_bomb.desc");
    }

    private static String read(String path) {
        try (var stream = NewEnchantmentsResourcesTest.class.getClassLoader().getResourceAsStream(path)) {
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
