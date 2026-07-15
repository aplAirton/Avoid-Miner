package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.menu.XpVaultMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class XpVaultBlockEntity extends BlockEntity {
    public static final String STORED_LEVELS_KEY = "StoredLevels";
    public static final int DATA_LEVELS_LOW = 0;
    public static final int DATA_LEVELS_HIGH = 1;
    public static final int DATA_SIZE = 2;

    private int storedLevels;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_LEVELS_LOW -> storedLevels & 0xFFFF;
                case DATA_LEVELS_HIGH -> storedLevels >>> 16;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_LEVELS_LOW -> storedLevels = (storedLevels & 0xFFFF0000) | (value & 0xFFFF);
                case DATA_LEVELS_HIGH -> storedLevels = (storedLevels & 0xFFFF) | ((value & 0xFFFF) << 16);
            }
            storedLevels = Math.max(0, storedLevels);
        }

        @Override
        public int getCount() {
            return DATA_SIZE;
        }
    };

    public XpVaultBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.XP_VAULT.get(), pos, state);
    }

    public int getStoredLevels() {
        return storedLevels;
    }

    public void setStoredLevels(int levels) {
        storedLevels = Math.max(0, levels);
        setChanged();
    }

    public void withdrawLevels(ServerPlayer player, int requestedLevels) {
        int amount = XpVaultRules.withdrawAmount(storedLevels, requestedLevels);
        if (amount <= 0) return;

        int before = player.experienceLevel;
        player.giveExperienceLevels(amount);
        int transferred = Math.min(storedLevels, Math.max(0, player.experienceLevel - before));
        if (transferred <= 0) return;

        storedLevels -= transferred;
        completeTransfer(true);
    }

    public void depositLevels(ServerPlayer player, int requestedLevels) {
        int amount = XpVaultRules.depositAmount(storedLevels, player.experienceLevel, requestedLevels);
        if (amount <= 0) return;

        int before = player.experienceLevel;
        player.giveExperienceLevels(-amount);
        int transferred = Math.min(Integer.MAX_VALUE - storedLevels,
                Math.max(0, before - player.experienceLevel));
        if (transferred <= 0) return;

        storedLevels += transferred;
        completeTransfer(false);
    }

    private void completeTransfer(boolean withdrawing) {
        setChanged();
        if (!(level instanceof ServerLevel serverLevel)) return;

        serverLevel.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS, 0.75f, withdrawing ? 1.25f : 0.75f);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, true, false,
                worldPosition.getX() + 0.5, worldPosition.getY() + 1.05, worldPosition.getZ() + 0.5,
                4, 0.28, 0.12, 0.28, 0.02);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, true, false,
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.7, worldPosition.getZ() + 0.5,
                6, 0.35, 0.25, 0.35, 0.04);
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(getMenuProvider(), worldPosition);
    }

    private MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(
                (id, inventory, player) -> new XpVaultMenu(id, inventory, data, this),
                Component.translatable("container.avoidminer.xp_vault")
        );
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(STORED_LEVELS_KEY, storedLevels);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedLevels = Math.max(0, input.getIntOr(STORED_LEVELS_KEY, 0));
    }
}
