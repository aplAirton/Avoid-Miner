package com.airton.avoidminer.loot;

import java.util.List;

public final class ArchaeologyLootRules {
    private ArchaeologyLootRules() {
    }

    public static <T> void replaceWithFirst(List<T> generatedLoot, List<T> replacementLoot) {
        if (replacementLoot.isEmpty()) {
            return;
        }

        T replacement = replacementLoot.getFirst();
        generatedLoot.clear();
        generatedLoot.add(replacement);
    }
}
