package com.airton.avoidminer.item;

import com.airton.avoidminer.menu.FilterCardMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FilterCardItem extends Item {
    public static final int MAX_ENTRIES = 18;
    public static final int GRID_COLS = 9;
    public static final int GRID_ROWS = 2;
    private static final String ENTRIES_KEY = "FilterEntries";

    public FilterCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos clicked = context.getClickedPos();

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockState state = level.getBlockState(clicked);
        Block block = state.getBlock();
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);

        if (addEntry(stack, id)) {
            player.sendSystemMessage(Component.translatable("msg.avoidminer.filter_card.added",
                    Component.translatable(block.getDescriptionId())));
            level.playSound(null, player, SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.PLAYERS, 0.5f, 1.3f);
        } else {
            int count = getEntries(stack).size();
            if (count >= MAX_ENTRIES) {
                player.sendSystemMessage(Component.translatable("msg.avoidminer.filter_card.full"));
            } else {
                player.sendSystemMessage(Component.translatable("msg.avoidminer.filter_card.duplicate"));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new FilterCardMenu(id, inv),
                Component.translatable("screen.avoidminer.filter_card")), buf -> {});
        return InteractionResult.SUCCESS;
    }

    public static List<Identifier> getEntries(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(ENTRIES_KEY)) return List.of();
        ListTag list = tag.getListOrEmpty(ENTRIES_KEY);
        List<Identifier> out = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            String s = list.getStringOr(i, "");
            if (s.isEmpty()) continue;
            Identifier id = Identifier.tryParse(s);
            if (id != null) out.add(id);
        }
        return out;
    }

    public static boolean addEntry(ItemStack stack, Identifier id) {
        List<Identifier> current = getEntries(stack);
        for (Identifier e : current) {
            if (e.equals(id)) return false;
        }
        if (current.size() >= MAX_ENTRIES) return false;
        updateEntries(stack, list -> list.add(StringTag.valueOf(id.toString())));
        return true;
    }

    public static void removeEntry(ItemStack stack, int index) {
        List<Identifier> current = getEntries(stack);
        if (index < 0 || index >= current.size()) return;
        updateEntries(stack, list -> list.remove(index));
    }

    public static void clearEntries(ItemStack stack) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    }

    private static void updateEntries(ItemStack stack, java.util.function.Consumer<ListTag> mutator) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        stack.set(DataComponents.CUSTOM_DATA, data.update(tag -> {
            ListTag list = new ListTag();
            ListTag existing = tag.getListOrEmpty(ENTRIES_KEY);
            for (int i = 0; i < existing.size(); i++) {
                list.add(existing.get(i));
            }
            mutator.accept(list);
            tag.put(ENTRIES_KEY, list);
        }));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        List<Identifier> entries = getEntries(stack);
        builder.accept(Component.translatable("tooltip.avoidminer.filter_card.usage")
                .withStyle(ChatFormatting.DARK_GRAY));
        if (entries.isEmpty()) {
            builder.accept(Component.translatable("tooltip.avoidminer.filter_card.empty")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            builder.accept(Component.translatable("tooltip.avoidminer.filter_card.count", entries.size())
                    .withStyle(ChatFormatting.AQUA));
            int show = Math.min(5, entries.size());
            for (int i = 0; i < show; i++) {
                Block b = BuiltInRegistries.BLOCK.getValue(entries.get(i));
                builder.accept(Component.literal("  ")
                        .append(Component.translatable(b.getDescriptionId()))
                        .withStyle(ChatFormatting.GRAY));
            }
            if (entries.size() > show) {
                builder.accept(Component.translatable("tooltip.avoidminer.filter_card.more",
                        entries.size() - show).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return !getEntries(stack).isEmpty();
    }
}