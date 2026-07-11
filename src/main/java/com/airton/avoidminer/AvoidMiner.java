package com.airton.avoidminer;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import org.slf4j.Logger;

@Mod(AvoidMiner.MODID)
public class AvoidMiner {
    public static final String MODID = "avoidminer";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AvoidMiner(IEventBus modEventBus) {
        LOGGER.info("Avoid Miner inicializado com sucesso!");

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);

        modEventBus.addListener(this::onBlockEntityTypeAddBlocks);
        modEventBus.addListener(this::onRegisterCapabilities);
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
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.AVOID_MINER.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.PROCESSOR.get(),
                (be, side) -> be.getItemHandler(side));
    }
}
