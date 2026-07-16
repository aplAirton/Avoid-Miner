package com.airton.avoidminer.worldgen;

import com.airton.avoidminer.config.AvoidMinerServerConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

public final class ConfigCountPlacement extends RepeatingPlacement {
    public static final ConfigCountPlacement INSTANCE = new ConfigCountPlacement();
    public static final MapCodec<ConfigCountPlacement> CODEC = MapCodec.unit(INSTANCE);

    private ConfigCountPlacement() {}

    @Override
    protected int count(RandomSource random, BlockPos pos) {
        return AvoidMinerServerConfig.magnetiteVeinsPerChunk();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModWorldgenRegistries.MAGNETITE_COUNT.get();
    }
}
