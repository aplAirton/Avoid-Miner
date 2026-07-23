package com.airton.avoidminer.item;

import com.airton.avoidminer.block.entity.MinerBlockEntity;
import com.airton.avoidminer.menu.RangeCardMenu;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

public class RangeCardItem extends Item {
    private static final String X1_KEY = "x1";
    private static final String Y1_KEY = "y1";
    private static final String Z1_KEY = "z1";
    private static final String X2_KEY = "x2";
    private static final String Y2_KEY = "y2";
    private static final String Z2_KEY = "z2";
    public static final int MAX_VOLUME = 3000000;

    public RangeCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos clicked = context.getClickedPos();

        BlockEntity be = level.getBlockEntity(clicked);
        if (be instanceof MinerBlockEntity miner) {
            if (!hasCompleteData(stack)) {
                if (player != null && !level.isClientSide()) {
                    player.sendSystemMessage(Component.translatable("msg.avoidminer.range_card.incomplete"));
                }
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide()) {
                int x1 = getX1(stack);
                int y1 = getY1(stack);
                int z1 = getZ1(stack);
                int x2 = getX2(stack);
                int y2 = getY2(stack);
                int z2 = getZ2(stack);
                var handler = miner.getItemHandler();
                var existing = handler.getResource(MinerBlockEntity.RANGE_SLOT);
                if (!existing.isEmpty()) {
                    player.sendSystemMessage(Component.translatable("msg.avoidminer.range_card.slot_occupied"));
                    return InteractionResult.SUCCESS;
                }
                try (var tx = Transaction.openRoot()) {
                    int inserted = handler.insert(MinerBlockEntity.RANGE_SLOT,
                            ItemResource.of(stack), 1, tx);
                    if (inserted > 0) {
                        tx.commit();
                        stack.shrink(1);
                        level.playSound(null, clicked, SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 0.8f, 1.0f);
                        int depth = Math.abs(y2 - y1);
                        player.sendSystemMessage(Component.translatable("msg.avoidminer.range_card.applied",
                                Math.min(x1, x2), Math.min(z1, z2), Math.max(x1, x2), Math.max(z1, z2), depth));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!hasData(stack)) {
            setX1(stack, clicked.getX());
            setY1(stack, clicked.getY());
            setZ1(stack, clicked.getZ());
            player.sendSystemMessage(Component.translatable("msg.avoidminer.range_card.pos1", clicked.getX(), clicked.getY(), clicked.getZ()));
        } else {
            int x1 = getX1(stack), y1 = getY1(stack), z1 = getZ1(stack);
            int vol = getVolume(x1, y1, z1, clicked.getX(), clicked.getY(), clicked.getZ());
            if (vol > MAX_VOLUME) {
                player.sendSystemMessage(Component.translatable("msg.avoidminer.range_card.too_large", vol, MAX_VOLUME)
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.SUCCESS;
            }
            setX2(stack, clicked.getX());
            setY2(stack, clicked.getY());
            setZ2(stack, clicked.getZ());
            int x2 = getX2(stack);
            int z2 = getZ2(stack);
            int y2 = getY2(stack);
            int depth = Math.abs(y2 - y1);
            player.sendSystemMessage(Component.translatable("msg.avoidminer.range_card.complete",
                    Math.min(x1, x2), Math.min(z1, z2), Math.max(x1, x2), Math.max(z1, z2), depth));
            level.playSound(null, clicked, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.6f, 1.2f);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean hasData(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(X1_KEY);
    }

    public static int getVolume(int x1, int y1, int z1, int x2, int y2, int z2) {
        long dx = (long) Math.abs(x2 - x1) + 1;
        long dy = (long) Math.abs(y2 - y1) + 1;
        long dz = (long) Math.abs(z2 - z1) + 1;
        long vol = dx * dy * dz;
        return vol > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) vol;
    }

    public static int getVolume(ItemStack stack) {
        if (!hasCompleteData(stack)) return 0;
        return getVolume(getX1(stack), getY1(stack), getZ1(stack),
                getX2(stack), getY2(stack), getZ2(stack));
    }

    public static boolean hasCompleteData(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(X1_KEY) && tag.contains(Y1_KEY) && tag.contains(Z1_KEY)
                && tag.contains(X2_KEY) && tag.contains(Y2_KEY) && tag.contains(Z2_KEY);
    }

    private static void setX1(ItemStack stack, int value) {
        updateTag(stack, tag -> tag.putInt(X1_KEY, value));
    }

    private static void setY1(ItemStack stack, int value) {
        updateTag(stack, tag -> tag.putInt(Y1_KEY, value));
    }

    private static void setZ1(ItemStack stack, int value) {
        updateTag(stack, tag -> tag.putInt(Z1_KEY, value));
    }

    private static void setX2(ItemStack stack, int value) {
        updateTag(stack, tag -> tag.putInt(X2_KEY, value));
    }

    private static void setY2(ItemStack stack, int value) {
        updateTag(stack, tag -> tag.putInt(Y2_KEY, value));
    }

    private static void setZ2(ItemStack stack, int value) {
        updateTag(stack, tag -> tag.putInt(Z2_KEY, value));
    }

    public static int getX1(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(X1_KEY, 0);
    }

    public static int getY1(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(Y1_KEY, 0);
    }

    public static int getZ1(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(Z1_KEY, 0);
    }

    public static int getX2(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(X2_KEY, 0);
    }

    public static int getY2(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(Y2_KEY, 0);
    }

    public static int getZ2(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(Z2_KEY, 0);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                ItemStack stack = player.getItemInHand(hand);
                clearData(stack);
                player.sendSystemMessage(Component.translatable("msg.avoidminer.range_card.cleared"));
                level.playSound(null, player, SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 0.3f, 0.5f);
            }
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new RangeCardMenu(id, inv),
                    Component.translatable("screen.avoidminer.range_card")), buf -> {});
        }
        return InteractionResult.SUCCESS;
    }

    public static void clearData(ItemStack stack) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    }

    public static void setAllCoords(ItemStack stack, int x1, int y1, int z1, int x2, int y2, int z2) {
        updateTag(stack, tag -> {
            tag.putInt(X1_KEY, x1);
            tag.putInt(Y1_KEY, y1);
            tag.putInt(Z1_KEY, z1);
            tag.putInt(X2_KEY, x2);
            tag.putInt(Y2_KEY, y2);
            tag.putInt(Z2_KEY, z2);
        });
    }

    private static void updateTag(ItemStack stack, Consumer<CompoundTag> updater) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        stack.set(DataComponents.CUSTOM_DATA, data.update(tag -> {
            updater.accept(tag);
        }));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        if (hasCompleteData(stack)) {
            int x1 = getX1(stack);
            int z1 = getZ1(stack);
            int x2 = getX2(stack);
            int z2 = getZ2(stack);
            int y1 = getY1(stack);
            int y2 = getY2(stack);
            int depth = Math.abs(y2 - y1);
            int vol = getVolume(stack);
            builder.accept(Component.translatable("tooltip.avoidminer.range_card.set",
                    Math.min(x1, x2), Math.min(z1, z2), Math.max(x1, x2), Math.max(z1, z2), depth)
                    .withStyle(ChatFormatting.GREEN));
            builder.accept(Component.translatable("tooltip.avoidminer.range_card.volume", vol, MAX_VOLUME)
                    .withStyle(vol > MAX_VOLUME ? ChatFormatting.RED : ChatFormatting.GRAY));
        } else if (hasData(stack)) {
            builder.accept(Component.translatable("tooltip.avoidminer.range_card.pos1_only",
                    getX1(stack), getY1(stack), getZ1(stack)).withStyle(ChatFormatting.YELLOW));
        } else {
            builder.accept(Component.translatable("tooltip.avoidminer.range_card.empty")
                    .withStyle(ChatFormatting.GRAY));
        }
        builder.accept(Component.translatable("tooltip.avoidminer.range_card.usage")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasCompleteData(stack);
    }
}
