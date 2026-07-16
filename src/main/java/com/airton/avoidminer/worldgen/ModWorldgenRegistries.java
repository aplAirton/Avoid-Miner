package com.airton.avoidminer.worldgen;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModWorldgenRegistries {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, AvoidMiner.MODID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, AvoidMiner.MODID);

    public static final DeferredHolder<Feature<?>, ConfigurableMagnetiteOreFeature> CONFIGURABLE_MAGNETITE_ORE =
            FEATURES.register("configurable_magnetite_ore", ConfigurableMagnetiteOreFeature::new);
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigCountPlacement>> MAGNETITE_COUNT =
            PLACEMENT_MODIFIERS.register("magnetite_count", () -> () -> ConfigCountPlacement.CODEC);
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigHeightPlacement>> MAGNETITE_HEIGHT =
            PLACEMENT_MODIFIERS.register("magnetite_height", () -> () -> ConfigHeightPlacement.CODEC);

    private ModWorldgenRegistries() {}
}
