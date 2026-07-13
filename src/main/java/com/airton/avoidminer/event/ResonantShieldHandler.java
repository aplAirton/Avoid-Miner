package com.airton.avoidminer.event;

import com.airton.avoidminer.AvoidMiner;
import com.airton.avoidminer.ResonantShieldRules;
import com.airton.avoidminer.item.ResonantRetaliationShieldItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class ResonantShieldHandler {
    private ResonantShieldHandler() {
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || !(player.level() instanceof ServerLevel level)
                || !event.getBlocked()
                || event.getBlockedDamage() <= 0.0F
                || event.getDamageSource().getDirectEntity() == null) {
            return;
        }

        ItemStack shield = player.getUseItem();
        if (!(shield.getItem() instanceof ResonantRetaliationShieldItem)) {
            return;
        }

        int previous = ResonantRetaliationShieldItem.getCharge(shield);
        int charge = ResonantShieldRules.addCharge(previous);
        ResonantRetaliationShieldItem.setCharge(shield, charge);
        if (previous < ResonantShieldRules.MAX_CHARGE && charge == ResonantShieldRules.MAX_CHARGE) {
            ResonantRetaliationShieldItem.emitReadyFlash(level, player);
        }
    }
}
