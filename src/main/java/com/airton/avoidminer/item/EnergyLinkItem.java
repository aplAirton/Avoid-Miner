package com.airton.avoidminer.item;

import com.airton.avoidminer.block.entity.BatteryBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Link de Energia — vincula-se a uma Bateria (agachado + clique direito nela)
 * e, colocado no slot de combustível de qualquer máquina do mod, alimenta a
 * máquina puxando energia já transformada da bateria vinculada, sem ser
 * consumido. Várias máquinas podem usar links para a mesma bateria.
 */
public class EnergyLinkItem extends Item {
    /** Energia máxima puxada da bateria por máquina, por tick. */
    public static final int TRANSFER_PER_TICK = 100;

    private static final String POS_KEY = "BatteryPos";
    private static final String DIM_KEY = "BatteryDim";

    public EnergyLinkItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockEntity be = level.getBlockEntity(context.getClickedPos());

        if (be instanceof BatteryBlockEntity) {
            if (player == null || !player.isSecondaryUseActive()) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            bind(context.getItemInHand(), context.getClickedPos(), level);
            level.playSound(null, context.getClickedPos(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS, 0.8f, 1.4f);
            player.sendOverlayMessage(Component.translatable("msg.avoidminer.energy_link.bound",
                    context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ()));
            return InteractionResult.SUCCESS;
        }

        // Agachado numa máquina: insere o link direto no slot de combustível
        return UpgradeApplier.tryApply(context);
    }

    private static void bind(ItemStack stack, BlockPos pos, Level level) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CustomData updated = data.update(tag -> {
            tag.putLong(POS_KEY, pos.asLong());
            tag.putString(DIM_KEY, level.dimension().identifier().toString());
        });
        stack.set(DataComponents.CUSTOM_DATA, updated);
    }

    @Nullable
    public static BlockPos boundPos(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(POS_KEY)) return null;
        return BlockPos.of(tag.getLongOr(POS_KEY, 0L));
    }

    @Nullable
    public static String boundDimension(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String dim = tag.getStringOr(DIM_KEY, "");
        return dim.isEmpty() ? null : dim;
    }

    /**
     * Puxa até {@code amount} de energia da bateria vinculada ao link.
     * Retorna quanto foi de fato transferido (0 se não vinculada, dimensão
     * diferente, chunk descarregado ou bateria vazia). Só chamar no servidor.
     */
    public static int drawEnergy(Level level, ItemStack link, int amount) {
        if (amount <= 0) return 0;
        BlockPos pos = boundPos(link);
        String dim = boundDimension(link);
        if (pos == null || dim == null) return 0;
        if (!level.dimension().identifier().toString().equals(dim)) return 0;
        if (!level.isLoaded(pos)) return 0;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BatteryBlockEntity battery) {
            return battery.extractEnergy(amount);
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        BlockPos pos = boundPos(stack);
        if (pos != null) {
            builder.accept(Component.translatable("tooltip.avoidminer.energy_link.bound",
                    pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.AQUA));
        } else {
            builder.accept(Component.translatable("tooltip.avoidminer.energy_link.unbound")
                    .withStyle(ChatFormatting.GRAY));
        }
        builder.accept(Component.translatable("tooltip.avoidminer.energy_link.usage")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return boundPos(stack) != null;
    }
}
