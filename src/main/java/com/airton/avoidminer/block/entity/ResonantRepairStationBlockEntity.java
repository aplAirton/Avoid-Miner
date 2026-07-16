package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.config.AvoidMinerServerConfig;
import com.airton.avoidminer.menu.ResonantRepairStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.Nullable;

public final class ResonantRepairStationBlockEntity extends BlockEntity {
    public static final int CRYSTAL_SLOT = 0;
    public static final int EQUIPMENT_SLOT = 1;
    public static final int TOTAL_SLOTS = 2;
    private int progress;

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            setChanged();
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            if (resource.isEmpty()) return false;
            ItemStack stack = resource.toStack(1);
            return slot == CRYSTAL_SLOT ? stack.is(ModItems.RESONANT_CRYSTAL.get())
                    : slot == EQUIPMENT_SLOT && isSupportedEquipment(stack);
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            return slot == EQUIPMENT_SLOT ? 1L : super.getCapacityAsLong(slot, resource);
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return index == 0 ? progress : index == 1 ? AvoidMinerServerConfig.repairIntervalTicks() : 0;
        }
        @Override public void set(int index, int value) { if (index == 0) progress = value; }
        @Override public int getCount() { return 2; }
    };

    public ResonantRepairStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_REPAIR_STATION.get(), pos, state);
    }

    public static boolean isSupportedEquipment(ItemStack stack) {
        return stack.is(ModItems.RESONANT_PICKAXE.get())
                || stack.is(ModItems.GLASS_SWORD.get())
                || stack.is(ModItems.THOR_HAMMER.get());
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            ResonantRepairStationBlockEntity be) {
        if (level.isClientSide()) return;
        ItemResource crystal = be.inventory.getResource(CRYSTAL_SLOT);
        ItemResource equipmentResource = be.inventory.getResource(EQUIPMENT_SLOT);
        if (crystal.isEmpty() || be.inventory.getAmountAsInt(CRYSTAL_SLOT) <= 0
                || equipmentResource.isEmpty()) {
            if (be.progress != 0) { be.progress = 0; be.setChanged(); }
            return;
        }

        ItemStack equipment = equipmentResource.toStack(1);
        if (!isSupportedEquipment(equipment) || !equipment.isDamaged()) {
            if (be.progress != 0) { be.progress = 0; be.setChanged(); }
            return;
        }

        be.progress++;
        if (be.progress >= AvoidMinerServerConfig.repairIntervalTicks()) {
            be.progress = 0;
            equipment.setDamageValue(Math.max(0,
                    equipment.getDamageValue() - AvoidMinerServerConfig.repairPerCrystal()));
            be.inventory.set(EQUIPMENT_SLOT, ItemResource.of(equipment), 1);
            int remaining = be.inventory.getAmountAsInt(CRYSTAL_SLOT) - 1;
            be.inventory.set(CRYSTAL_SLOT,
                    remaining > 0 ? crystal : ItemResource.EMPTY, Math.max(0, remaining));
        }
        be.setChanged();
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(getMenuProvider());
    }

    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(
                (id, inv, player) -> new ResonantRepairStationMenu(id, inv, inventory, data),
                Component.translatable("container.avoidminer.resonant_repair_station"));
    }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return inventory;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", progress);
        inventory.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr("Progress", 0);
        inventory.deserialize(input.childOrEmpty("Inventory"));
    }
}
