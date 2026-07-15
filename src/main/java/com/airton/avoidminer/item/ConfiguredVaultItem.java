package com.airton.avoidminer.item;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public final class ConfiguredVaultItem extends Item {
    private final boolean ominous;
    private final BlockItem placer;

    public ConfiguredVaultItem(Properties properties, boolean ominous) {
        super(properties.stacksTo(1));
        this.ominous = ominous;
        this.placer = (BlockItem) Items.VAULT;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPlaceContext placement = new BlockPlaceContext(context);
        BlockPlaceContext updated = placer.updatePlacementContext(placement);
        if (updated == null) {
            return InteractionResult.FAIL;
        }
        BlockPos placedPos = updated.getClickedPos();
        InteractionResult result = placer.place(updated);
        if (placedPos != null && result.consumesAction() && !context.getLevel().isClientSide()) {
            BlockState placedState = context.getLevel().getBlockState(placedPos);
            if (placedState.is(Blocks.VAULT) && placedState.hasProperty(VaultBlock.OMINOUS)) {
                context.getLevel().setBlock(placedPos, placedState.setValue(VaultBlock.OMINOUS, ominous), 3);
            }
        }
        if (placedPos != null && result.consumesAction() && !context.getLevel().isClientSide()
                && context.getLevel().getBlockEntity(placedPos) instanceof VaultBlockEntity vault) {
            VaultConfig oldConfig = vault.getConfig();
            ItemStack key = new ItemStack(ominous ? Items.OMINOUS_TRIAL_KEY : Items.TRIAL_KEY);
            vault.setConfig(new VaultConfig(
                    ominous ? BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS : BuiltInLootTables.TRIAL_CHAMBERS_REWARD,
                    oldConfig.activationRange(),
                    oldConfig.deactivationRange(),
                    key,
                    Optional.empty(),
                    oldConfig.playerDetector(),
                    oldConfig.entitySelector()
            ));
            vault.setChanged();
        }
        return result;
    }
}
