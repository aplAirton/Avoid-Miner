package com.airton.avoidminer.event;

import com.airton.avoidminer.AvoidMiner;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.IdentityHashMap;
import java.util.Map;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class TemporalFreezeManager {
    private static final Map<MinecraftServer, Integer> ACTIVE_FREEZES = new IdentityHashMap<>();
    private static final int PERMANENT = -1;

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

    public static boolean freezePermanent(MinecraftServer server) {
        if (server.tickRateManager().isFrozen() || ACTIVE_FREEZES.containsKey(server)) {
            return false;
        }
        ACTIVE_FREEZES.put(server, PERMANENT);
        server.tickRateManager().setFrozen(true);
        return true;
    }

    public static void unfreeze(MinecraftServer server) {
        if (ACTIVE_FREEZES.remove(server) != null && server.tickRateManager().isFrozen()) {
            server.tickRateManager().setFrozen(false);
        }
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
        if (remaining < 0) {
            return;
        }
        if (remaining <= 1) {
            ACTIVE_FREEZES.remove(server);
            server.tickRateManager().setFrozen(false);
        } else {
            if (remaining <= 30 && remaining % 10 == 0) {
                ServerLevel overworld = server.overworld();
                if (overworld != null) {
                    for (var player : server.getPlayerList().getPlayers()) {
                        overworld.playSound(null, player.blockPosition(), SoundEvents.LEVER_CLICK,
                                SoundSource.PLAYERS, 0.3F, 1.5F);
                    }
                }
            }
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
