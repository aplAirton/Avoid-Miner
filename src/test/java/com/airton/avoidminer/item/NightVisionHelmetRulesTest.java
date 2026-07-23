package com.airton.avoidminer.item;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NightVisionHelmetRulesTest {
    @Test
    public void durabilityStaysBetweenDiamondAndNetherite() {
        assertEquals(385, NightVisionHelmetRules.HELMET_DURABILITY);
        assertTrue(NightVisionHelmetRules.HELMET_DURABILITY > 363);
        assertTrue(NightVisionHelmetRules.HELMET_DURABILITY < 407);
        assertEquals(2.5F, NightVisionHelmetRules.TOUGHNESS, 0.0F);
    }

    @Test
    public void effectRefreshesBeforeNightVisionStartsFlickering() {
        assertEquals(260, NightVisionHelmetRules.EFFECT_DURATION_TICKS);
        assertTrue(NightVisionHelmetRules.EFFECT_REFRESH_THRESHOLD > 200);
        assertTrue(NightVisionHelmetRules.EFFECT_REFRESH_THRESHOLD
                < NightVisionHelmetRules.EFFECT_DURATION_TICKS);
    }

    @Test
    public void recipeMatchesTheRequestedSlotLayout() throws IOException {
        try (var stream = getClass().getClassLoader()
                .getResourceAsStream("data/avoidminer/recipe/night_vision_helmet.json")) {
            assertNotNull("Missing night vision helmet recipe", stream);
            String recipe = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(recipe.contains("\"MGM\""));
            assertTrue(recipe.contains("\"GHG\""));
            assertTrue(recipe.contains("\"M\": \"avoidminer:magnetite_ingot\""));
            assertTrue(recipe.contains("\"G\": \"minecraft:glowstone_dust\""));
            assertTrue(recipe.contains("\"H\": \"minecraft:diamond_helmet\""));
        }
    }
}
