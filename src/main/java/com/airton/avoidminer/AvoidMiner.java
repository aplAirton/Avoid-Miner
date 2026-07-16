package com.airton.avoidminer;

import com.mojang.logging.LogUtils;
import com.airton.avoidminer.config.AvoidMinerServerConfig;
import com.airton.avoidminer.worldgen.ModWorldgenRegistries;
import com.airton.avoidminer.network.ModNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import org.slf4j.Logger;

@Mod(AvoidMiner.MODID)
public class AvoidMiner {
    public static final String MODID = "avoidminer";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AvoidMiner(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Avoid Miner inicializado com sucesso!");

        ModBlocks.BLOCKS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModParticleTypes.PARTICLE_TYPES.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModWorldgenRegistries.FEATURES.register(modEventBus);
        ModWorldgenRegistries.PLACEMENT_MODIFIERS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, AvoidMinerServerConfig.SPEC, "avoidminer-server.toml");

        modEventBus.addListener(this::onBlockEntityTypeAddBlocks);
        modEventBus.addListener(this::onRegisterCapabilities);
        modEventBus.addListener(ModNetworking::register);
    }

    private void onBlockEntityTypeAddBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(ModBlockEntities.AVOID_MINER.getKey(),
                ModBlocks.AVOID_MINER_TIER_1.get(),
                ModBlocks.AVOID_MINER_TIER_2.get(),
                ModBlocks.AVOID_MINER_TIER_3.get());
        event.modify(ModBlockEntities.PROCESSOR.getKey(),
                ModBlocks.AVOID_PROCESSOR_TIER_1.get(),
                ModBlocks.AVOID_PROCESSOR_TIER_2.get(),
                ModBlocks.AVOID_PROCESSOR_TIER_3.get());
        event.modify(ModBlockEntities.MAGNETITE_FURNACE.getKey(),
                ModBlocks.MAGNETITE_FURNACE_TIER_1.get(),
                ModBlocks.MAGNETITE_FURNACE_TIER_2.get(),
                ModBlocks.MAGNETITE_FURNACE_TIER_3.get());
        event.modify(ModBlockEntities.LOOTR.getKey(),
                ModBlocks.AVOID_LOOTR.get());
        event.modify(ModBlockEntities.BATTERY.getKey(),
                ModBlocks.BATTERY.get());
        event.modify(ModBlockEntities.CREATIVE_BATTERY.getKey(),
                ModBlocks.CREATIVE_BATTERY.get());
        event.modify(ModBlockEntities.XP_VAULT.getKey(),
                ModBlocks.XP_VAULT.get());
        event.modify(ModBlockEntities.MAGNETITE_BARREL.getKey(),
                ModBlocks.MAGNETITE_BARREL.get());
        event.modify(ModBlockEntities.RESONANT_REPAIR_STATION.getKey(),
                ModBlocks.RESONANT_REPAIR_STATION.get());
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.AVOID_MINER.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.PROCESSOR.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.MAGNETITE_FURNACE.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.LOOTR.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.BATTERY.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.MAGNETITE_BARREL.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.RESONANT_REPAIR_STATION.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.CREATIVE_BATTERY.get(),
                (be, side) -> null);
    }
}
