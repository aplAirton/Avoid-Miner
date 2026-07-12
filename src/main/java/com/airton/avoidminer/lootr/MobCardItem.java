package com.airton.avoidminer.lootr;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;

import com.airton.avoidminer.item.UpgradeApplier;

import java.util.function.Consumer;

/**
 * Cartão de mob — item não empilhável que armazena o progresso de abates
 * (via {@link DataComponents#CUSTOM_DATA}). Quando o contador atinge
 * {@link MobCardType#requiredKills} o cartão fica "carregado" e pode ser
 * inserido na máquina Avoid Lootr.
 *
 * <p>O {@code MobCardType} (skeleton, wither_skeleton, etc) é determinado no
 * registro do item via {@link #MobCardItem(MobCardType, Properties)}.</p>
 */
public class MobCardItem extends Item {

    private static final String KILLS_KEY = "Kills";

    private final MobCardType cardType;

    public MobCardItem(MobCardType cardType, Properties properties) {
        super(properties.stacksTo(1));
        this.cardType = cardType;
    }

    public MobCardType getCardType() {
        return cardType;
    }

    /* ----------------------- progress helpers ----------------------- */

    public static int getKills(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.getIntOr(KILLS_KEY, 0);
    }

    public static void setKills(ItemStack stack, int kills) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CustomData updated = data.update(tag -> tag.putInt(KILLS_KEY, Math.max(0, kills)));
        stack.set(DataComponents.CUSTOM_DATA, updated);
    }

    public static boolean isCompleted(ItemStack stack, MobCardType type) {
        return getKills(stack) >= type.requiredKills;
    }

    /* ----------------------- info visual ----------------------------- */

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        int kills = getKills(stack);
        boolean done = kills >= cardType.requiredKills;
        builder.accept(Component.translatable("tooltip.avoidminer.mobcard.target",
                Component.translatable("entity." + cardType.id)).withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.avoidminer.mobcard.progress", kills, cardType.requiredKills)
                .withStyle(done ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        if (done) {
            builder.accept(Component.translatable("tooltip.avoidminer.mobcard.ready")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        } else {
            builder.accept(Component.translatable("tooltip.avoidminer.mobcard.instructions")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isCompleted(stack, cardType);
    }

    /** Agachado + clique direito na Avoid Lootr insere o cartão carregado direto no slot. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return UpgradeApplier.tryApply(context);
    }

    /* ----------------------- helper de registro ---------------------- */

    /**
     * Cria o primeiro item do cartão "vazio" (0 kills) — útil para receitas.
     */
    public static ItemStack freshCard(MobCardType type, Item item) {
        ItemStack stack = new ItemStack(item);
        setKills(stack, 0);
        return stack;
    }
}