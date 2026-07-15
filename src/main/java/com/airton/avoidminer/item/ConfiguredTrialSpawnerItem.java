package com.airton.avoidminer.item;

import com.airton.avoidminer.lootr.MobCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import java.util.function.Consumer;

public final class ConfiguredTrialSpawnerItem extends Item {
    public static final String MOB_KEY = "AvoidMinerTrialMob";
    private final BlockItem placer = (BlockItem) Items.TRIAL_SPAWNER;

    public ConfiguredTrialSpawnerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        MobCardType cardType = MobCardType.byId(customData.copyTag().getStringOr(MOB_KEY, ""));
        if (cardType != null) {
            builder.accept(Component.translatable("tooltip.avoidminer.configured_trial_spawner.mob",
                    cardType.entityType.getDescription()));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        String mobId = customData.copyTag().getStringOr(MOB_KEY, "");
        MobCardType cardType = MobCardType.byId(mobId);

        BlockPlaceContext placement = new BlockPlaceContext(context);
        BlockPlaceContext updated = placer.updatePlacementContext(placement);
        if (updated == null) {
            return InteractionResult.FAIL;
        }
        BlockPos placedPos = updated.getClickedPos();
        InteractionResult result = placer.place(updated);
        if (placedPos != null && result.consumesAction() && !context.getLevel().isClientSide()
                && cardType != null
                && context.getLevel().getBlockEntity(placedPos) instanceof TrialSpawnerBlockEntity spawner) {
            spawner.getTrialSpawner().overrideEntityToSpawn(cardType.entityType, context.getLevel());
            spawner.setChanged();
        }
        return result;
    }
}
