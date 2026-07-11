package com.airton.avoidminer;

import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity;
import com.airton.avoidminer.block.entity.LootrBlockEntity;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AvoidMiner.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AvoidMinerBlockEntity>> AVOID_MINER = BLOCK_ENTITIES.register("avoid_miner",
            () -> new BlockEntityType<>(AvoidMinerBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessorBlockEntity>> PROCESSOR = BLOCK_ENTITIES.register("processor",
            () -> new BlockEntityType<>(ProcessorBlockEntity::new, Collections.emptySet()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LootrBlockEntity>> LOOTR = BLOCK_ENTITIES.register("lootr",
            () -> new BlockEntityType<>(LootrBlockEntity::new, Collections.emptySet()));
}
