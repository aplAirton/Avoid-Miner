package com.airton.avoidminer.item;

import com.airton.avoidminer.config.AvoidMinerServerConfig;
import com.airton.avoidminer.network.ResonantScanPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public final class ResonantScannerItem extends Item {
    public static final byte COAL = 0;
    public static final byte COPPER = 1;
    public static final byte LAPIS = 2;
    public static final byte GOLD = 3;
    public static final byte IRON = 4;
    public static final byte REDSTONE = 5;
    public static final byte DIAMOND = 6;
    public static final byte EMERALD = 7;
    public static final byte LAVA = 8;
    public static final byte WATER = 9;
    public static final byte CAVITY = 10;

    private final int tier;

    public ResonantScannerItem(Properties properties, int tier) {
        super(properties.stacksTo(1).durability(256));
        this.tier = Math.clamp(tier, 1, 3);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;

        List<ResonantScanPayload.Target> targets = scan(serverLevel, player.blockPosition(),
                AvoidMinerServerConfig.scannerRadius(), tier);
        PacketDistributor.sendToPlayer(serverPlayer,
                new ResonantScanPayload(targets, AvoidMinerServerConfig.scannerDurationTicks()));
        player.getCooldowns().addCooldown(stack, AvoidMinerServerConfig.scannerCooldownTicks());
        stack.hurtAndBreak(1, player, hand);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.PLAYERS, 0.8F, 1.7F);
        return InteractionResult.SUCCESS;
    }

    static List<ResonantScanPayload.Target> scan(ServerLevel level, BlockPos origin, int radius, int tier) {
        List<ResonantScanPayload.Target> important = new ArrayList<>();
        List<ResonantScanPayload.Target> cavities = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = Math.max(level.getMinY(), origin.getY() - radius);
        int maxY = Math.min(level.getMaxY() - 1, origin.getY() + radius);
        int radiusSquared = radius * radius;

        for (int y = minY; y <= maxY; y++) {
            for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
                for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                    int dx = x - origin.getX();
                    int dy = y - origin.getY();
                    int dz = z - origin.getZ();
                    if (dx * dx + dy * dy + dz * dz > radiusSquared) continue;
                    cursor.set(x, y, z);
                    if (!level.hasChunkAt(cursor)) continue;

                    BlockState state = level.getBlockState(cursor);
                    byte category = oreCategory(state, tier);
                    if (category < 0 && state.getFluidState().is(FluidTags.LAVA)) category = LAVA;
                    else if (state.getFluidState().is(FluidTags.WATER)) category = WATER;

                    if (category >= 0) {
                        if (important.size() < 1536) {
                            important.add(new ResonantScanPayload.Target(cursor.immutable(), category));
                        }
                    } else if (cavities.size() < 512 && state.isAir()
                            && Math.floorMod(dx, 3) == 0 && Math.floorMod(dy, 3) == 0
                            && Math.floorMod(dz, 3) == 0 && !level.canSeeSky(cursor)
                            && touchesSolid(level, cursor)) {
                        cavities.add(new ResonantScanPayload.Target(cursor.immutable(), CAVITY));
                    }
                }
            }
        }
        important.addAll(cavities);
        return List.copyOf(important);
    }

    static byte oreCategory(BlockState state, int tier) {
        return switch (tier) {
            case 1 -> {
                if (state.is(BlockTags.COAL_ORES)) yield COAL;
                if (state.is(BlockTags.COPPER_ORES)) yield COPPER;
                if (state.is(BlockTags.LAPIS_ORES)) yield LAPIS;
                yield -1;
            }
            case 2 -> {
                if (state.is(BlockTags.GOLD_ORES)) yield GOLD;
                if (state.is(BlockTags.IRON_ORES)) yield IRON;
                if (state.is(BlockTags.REDSTONE_ORES)) yield REDSTONE;
                yield -1;
            }
            case 3 -> {
                if (state.is(BlockTags.DIAMOND_ORES)) yield DIAMOND;
                if (state.is(BlockTags.EMERALD_ORES)) yield EMERALD;
                yield -1;
            }
            default -> -1;
        };
    }

    private static boolean touchesSolid(ServerLevel level, BlockPos pos) {
        int solidFaces = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).isSolid()) solidFaces++;
        }
        return solidFaces >= 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.resonant_scanner.desc")
                .withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("tooltip.avoidminer.resonant_scanner.tier_" + tier + ".targets")
                .withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.avoidminer.resonant_scanner.environment")
                .withStyle(ChatFormatting.GRAY));
    }
}
