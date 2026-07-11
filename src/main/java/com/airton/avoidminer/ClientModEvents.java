package com.airton.avoidminer;

import com.airton.avoidminer.screen.AvoidMinerScreen;
import com.airton.avoidminer.screen.ProcessorScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.AVOID_MINER.get(), AvoidMinerScreen::new);
        event.register(ModMenuTypes.PROCESSOR.get(), ProcessorScreen::new);
    }
}
