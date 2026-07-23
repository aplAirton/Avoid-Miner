package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnchanterBlockEntity extends BlockEntity {

    public EnchanterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTER.get(), pos, state);
    }

    public void openMenu(Player player) {
        if (level == null) return;
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (id, inv, p) -> new AnvilMenu(id, inv, ContainerLevelAccess.create(level, worldPosition)),
                Component.translatable("container.avoidminer.enchanter")));
    }
}
