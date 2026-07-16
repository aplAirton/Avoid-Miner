package com.airton.avoidminer.worldgen;

import com.airton.avoidminer.config.AvoidMinerServerConfig;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public final class ConfigurableMagnetiteOreFeature extends OreFeature {
    public ConfigurableMagnetiteOreFeature() {
        super(OreConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        OreConfiguration original = context.config();
        OreConfiguration configured = new OreConfiguration(
                original.targetStates,
                AvoidMinerServerConfig.magnetiteVeinSize(),
                AvoidMinerServerConfig.magnetiteAirDiscardChance());
        return super.place(new FeaturePlaceContext<>(context.topFeature(), context.level(),
                context.chunkGenerator(), context.random(), context.origin(), configured));
    }
}
