package com.airton.avoidminer.menu;

import com.airton.avoidminer.ModMenuTypes;
import com.airton.avoidminer.block.entity.XpVaultBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class XpVaultMenu extends AbstractContainerMenu {
    public static final int BUTTON_GET_1 = 0;
    public static final int BUTTON_GET_5 = 1;
    public static final int BUTTON_GET_10 = 2;
    public static final int BUTTON_STORE_1 = 3;
    public static final int BUTTON_STORE_5 = 4;
    public static final int BUTTON_STORE_10 = 5;
    public static final int BUTTON_STORE_ALL = 6;
    public static final int BUTTON_GET_ALL = 7;

    public static final int IMAGE_WIDTH = 236;
    public static final int IMAGE_HEIGHT = 214;

    private final ContainerData data;
    @Nullable
    private final XpVaultBlockEntity vault;

    public XpVaultMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainerData(XpVaultBlockEntity.DATA_SIZE), null);
    }

    public XpVaultMenu(int containerId, Inventory inventory, ContainerData data,
                       @Nullable XpVaultBlockEntity vault) {
        super(ModMenuTypes.XP_VAULT.get(), containerId);
        this.data = data;
        this.vault = vault;
        addDataSlots(data);
    }

    public int getStoredLevels() {
        return (data.get(XpVaultBlockEntity.DATA_LEVELS_HIGH) << 16)
                | (data.get(XpVaultBlockEntity.DATA_LEVELS_LOW) & 0xFFFF);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (vault == null || !(player instanceof ServerPlayer serverPlayer)) return false;

        switch (id) {
            case BUTTON_GET_1 -> vault.withdrawLevels(serverPlayer, 1);
            case BUTTON_GET_5 -> vault.withdrawLevels(serverPlayer, 5);
            case BUTTON_GET_10 -> vault.withdrawLevels(serverPlayer, 10);
            case BUTTON_STORE_1 -> vault.depositLevels(serverPlayer, 1);
            case BUTTON_STORE_5 -> vault.depositLevels(serverPlayer, 5);
            case BUTTON_STORE_10 -> vault.depositLevels(serverPlayer, 10);
            case BUTTON_STORE_ALL -> vault.depositLevels(serverPlayer, -1);
            case BUTTON_GET_ALL -> vault.withdrawLevels(serverPlayer, -1);
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (vault == null || vault.isRemoved() || player.level().getBlockEntity(vault.getBlockPos()) != vault) {
            return false;
        }
        return player.distanceToSqr(Vec3.atCenterOf(vault.getBlockPos())) <= 64.0;
    }
}
