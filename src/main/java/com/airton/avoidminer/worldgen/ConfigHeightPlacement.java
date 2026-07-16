package com.airton.avoidminer.worldgen;

import com.airton.avoidminer.config.AvoidMinerServerConfig;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class ConfigHeightPlacement extends PlacementModifier {
    public static final ConfigHeightPlacement INSTANCE = new ConfigHeightPlacement();
    public static final MapCodec<ConfigHeightPlacement> CODEC = MapCodec.unit(INSTANCE);

    private ConfigHeightPlacement() {}

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        int worldMin = context.getMinGenY();
        int worldMax = worldMin + context.getGenDepth() - 1;
        int min = Math.clamp(AvoidMinerServerConfig.magnetiteMinY(), worldMin, worldMax);
        int max = Math.clamp(AvoidMinerServerConfig.magnetiteMaxY(), min, worldMax);
        int y = (random.nextIntBetweenInclusive(min, max) + random.nextIntBetweenInclusive(min, max)) / 2;
        return Stream.of(new BlockPos(pos.getX(), y, pos.getZ()));
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModWorldgenRegistries.MAGNETITE_HEIGHT.get();
    }
}
