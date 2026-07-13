package com.airton.avoidminer;

import com.airton.avoidminer.loot.ReplaceWithTableLootModifier;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, AvoidMiner.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ReplaceWithTableLootModifier>>
            REPLACE_WITH_TABLE = LOOT_MODIFIER_SERIALIZERS.register(
                    "replace_with_table", () -> ReplaceWithTableLootModifier.CODEC);

    private ModLootModifiers() {
    }
}
