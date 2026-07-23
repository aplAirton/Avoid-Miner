package com.airton.avoidminer;

import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity;
import com.airton.avoidminer.block.entity.BatteryBlockEntity;
import com.airton.avoidminer.block.entity.CreativeBatteryBlockEntity;
import com.airton.avoidminer.block.entity.LootrBlockEntity;
import com.airton.avoidminer.block.entity.MagnetiteFurnaceBlockEntity;
import com.airton.avoidminer.block.entity.MinerBlockEntity;
import com.airton.avoidminer.block.entity.MobGrinderBlockEntity;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity;
import com.airton.avoidminer.block.entity.XpVaultBlockEntity;
import com.airton.avoidminer.block.entity.ResonantRepairStationBlockEntity;
import com.airton.avoidminer.block.entity.VillagerHubBlockEntity;
import com.airton.avoidminer.block.entity.EnchanterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AvoidMiner.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AvoidMinerBlockEntity>> AVOID_MINER = BLOCK_ENTITIES.register("avoid_miner",
            () -> new BlockEntityType<>(AvoidMinerBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessorBlockEntity>> PROCESSOR = BLOCK_ENTITIES.register("processor",
            () -> new BlockEntityType<>(ProcessorBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MagnetiteFurnaceBlockEntity>> MAGNETITE_FURNACE =
            BLOCK_ENTITIES.register("magnetite_furnace",
                    () -> new BlockEntityType<>(MagnetiteFurnaceBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LootrBlockEntity>> LOOTR = BLOCK_ENTITIES.register("lootr",
            () -> new BlockEntityType<>(LootrBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryBlockEntity>> BATTERY = BLOCK_ENTITIES.register("battery",
            () -> new BlockEntityType<>(BatteryBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeBatteryBlockEntity>> CREATIVE_BATTERY = BLOCK_ENTITIES.register("creative_battery",
            () -> new BlockEntityType<>(CreativeBatteryBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.airton.avoidminer.block.entity.MagnetiteBarrelBlockEntity>> MAGNETITE_BARREL =
            BLOCK_ENTITIES.register("magnetite_barrel",
                    () -> new BlockEntityType<>(com.airton.avoidminer.block.entity.MagnetiteBarrelBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<XpVaultBlockEntity>> XP_VAULT =
            BLOCK_ENTITIES.register("xp_vault",
                    () -> new BlockEntityType<>(XpVaultBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonantRepairStationBlockEntity>> RESONANT_REPAIR_STATION =
            BLOCK_ENTITIES.register("resonant_repair_station",
                    () -> new BlockEntityType<>(ResonantRepairStationBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VillagerHubBlockEntity>> VILLAGER_HUB =
            BLOCK_ENTITIES.register("villager_hub",
                    () -> new BlockEntityType<>(VillagerHubBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MinerBlockEntity>> MINER = BLOCK_ENTITIES.register("miner",
            () -> new BlockEntityType<>(MinerBlockEntity::new, Set.of(ModBlocks.MINER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MobGrinderBlockEntity>> MOB_GRINDER = BLOCK_ENTITIES.register("mob_grinder",
            () -> new BlockEntityType<>(MobGrinderBlockEntity::new, Set.of(ModBlocks.MOB_GRINDER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnchanterBlockEntity>> ENCHANTER = BLOCK_ENTITIES.register("enchanter",
            () -> new BlockEntityType<>(EnchanterBlockEntity::new, Set.of(ModBlocks.ENCHANTER.get())));
}
