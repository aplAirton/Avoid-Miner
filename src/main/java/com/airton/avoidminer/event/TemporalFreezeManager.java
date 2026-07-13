package com.airton.avoidminer.event;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.IdentityHashMap;
import java.util.Map;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class TemporalFreezeManager {
    private static final Map<MinecraftServer, Integer> ACTIVE_FREEZES = new IdentityHashMap<>();

    private TemporalFreezeManager() {
    }

    public static boolean tryFreeze(MinecraftServer server, int durationTicks) {
        if (durationTicks <= 0 || server.tickRateManager().isFrozen() || ACTIVE_FREEZES.containsKey(server)) {
            return false;
        }
        ACTIVE_FREEZES.put(server, durationTicks);
        server.tickRateManager().setFrozen(true);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        Integer remaining = ACTIVE_FREEZES.get(server);
        if (remaining == null) {
            return;
        }
        if (!server.tickRateManager().isFrozen()) {
            ACTIVE_FREEZES.remove(server);
            return;
        }
        if (remaining <= 1) {
            ACTIVE_FREEZES.remove(server);
            server.tickRateManager().setFrozen(false);
        } else {
            ACTIVE_FREEZES.put(server, remaining - 1);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        if (ACTIVE_FREEZES.remove(server) != null && server.tickRateManager().isFrozen()) {
            server.tickRateManager().setFrozen(false);
        }
    }
}
