package com.airton.avoidminer;

import com.airton.avoidminer.screen.AvoidMinerScreen;
import com.airton.avoidminer.screen.BatteryScreen;
import com.airton.avoidminer.screen.LootrScreen;
import com.airton.avoidminer.screen.ProcessorScreen;
import com.airton.avoidminer.screen.MagnetiteFurnaceScreen;
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
        event.register(ModMenuTypes.MAGNETITE_FURNACE.get(), MagnetiteFurnaceScreen::new);
        event.register(ModMenuTypes.LOOTR.get(), LootrScreen::new);
        event.register(ModMenuTypes.BATTERY.get(), BatteryScreen::new);
        event.register(ModMenuTypes.MAGNETITE_BARREL.get(), com.airton.avoidminer.screen.MagnetiteBarrelScreen::new);
    }
}
