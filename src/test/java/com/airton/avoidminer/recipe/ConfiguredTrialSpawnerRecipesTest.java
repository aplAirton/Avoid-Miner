package com.airton.avoidminer.recipe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfiguredTrialSpawnerRecipesTest {
    private static final List<String> MOB_IDS = List.of(
            "skeleton", "wither_skeleton", "creeper", "zombie", "spider", "witch",
            "piglin", "piglin_brute", "enderman", "villager", "blaze", "pig", "cow",
            "chicken", "sheep", "breeze", "guardian", "elder_guardian", "slime",
            "vindicator", "evoker", "ghast", "magma_cube", "shulker", "ender_dragon",
            "warden", "wither", "bogged", "drowned", "hoglin", "phantom", "pillager", "stray"
    );

    @Test
    public void everyMobCardHasANativeConfiguredSpawnerRecipe() {
        assertEquals(33, MOB_IDS.size());
        String cardTag = readResource("data/avoidminer/tags/item/mob_cards.json");

        for (String mobId : MOB_IDS) {
            String cardItem = "avoidminer:" + mobId + "_card";
            assertTrue("Card missing from mob_cards tag: " + cardItem, cardTag.contains(quoted(cardItem)));

            String recipe = readResource(
                    "data/avoidminer/recipe/configured_trial_spawner_from_" + mobId + "_card.json");
            assertContains(recipe, "\"type\": \"minecraft:crafting_shaped\"");
            assertContains(recipe, "\"MNM\"");
            assertContains(recipe, "\"MCM\"");
            assertContains(recipe, "\"C\": " + quoted(cardItem));
            assertContains(recipe, "\"N\": \"avoidminer:trial_spawner_core\"");
            assertContains(recipe, "\"AvoidMinerTrialMob\": " + quoted(mobId));
        }
    }

    @Test
    public void vaultRecipesUseTheirMatchingCores() {
        assertVaultRecipe("configured_vault", "N", "avoidminer:vault_core", "MNM");
        assertVaultRecipe("configured_ominous_vault", "S", "avoidminer:ominous_vault_core", "MSM");
    }

    private static void assertVaultRecipe(String name, String symbol, String core, String middleRow) {
        String recipe = readResource("data/avoidminer/recipe/" + name + ".json");
        assertContains(recipe, "\"type\": \"minecraft:crafting_shaped\"");
        assertContains(recipe, quoted(middleRow));
        assertContains(recipe, quoted(symbol) + ": " + quoted(core));
        assertContains(recipe, "\"id\": \"avoidminer:" + name + "\"");
    }

    private static String readResource(String path) {
        try (var stream = ConfiguredTrialSpawnerRecipesTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull("Missing resource: " + path, stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Could not read resource: " + path, exception);
        }
    }

    private static void assertContains(String value, String expected) {
        assertTrue("Expected resource to contain: " + expected, value.contains(expected));
    }

    private static String quoted(String value) {
        return "\"" + value + "\"";
    }
}
