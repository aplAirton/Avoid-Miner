package com.airton.avoidminer.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class EnchanterMenu extends AnvilMenu {
    public EnchanterMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(id, inv, access);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
