package com.airton.avoidminer.loot;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArchaeologyLootRulesTest {
    @Test
    public void successfulCardRollReplacesVanillaArchaeologyResult() {
        List<String> generatedLoot = new ArrayList<>(List.of("minecraft:tnt"));

        ArchaeologyLootRules.replaceWithFirst(
                generatedLoot,
                List.of("avoidminer:warden_card")
        );

        assertEquals(List.of("avoidminer:warden_card"), generatedLoot);
    }

    @Test
    public void emptyReplacementKeepsVanillaArchaeologyResult() {
        List<String> generatedLoot = new ArrayList<>(List.of("minecraft:tnt"));

        ArchaeologyLootRules.replaceWithFirst(generatedLoot, List.of());

        assertEquals(List.of("minecraft:tnt"), generatedLoot);
    }

    @Test
    public void replacementAlwaysLeavesExactlyOneArchaeologyResult() {
        List<String> generatedLoot = new ArrayList<>(List.of("minecraft:tnt"));

        ArchaeologyLootRules.replaceWithFirst(
                generatedLoot,
                List.of("avoidminer:warden_card", "avoidminer:wither_card")
        );

        assertEquals(1, generatedLoot.size());
        assertEquals("avoidminer:warden_card", generatedLoot.getFirst());
    }
}
