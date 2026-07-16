package com.airton.avoidminer.event;

import com.airton.avoidminer.AvoidMiner;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.item.NightVisionHelmetItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID)
public final class NightVisionHelmetHandler {
    private NightVisionHelmetHandler() {
    }

    @SubscribeEvent
    public static void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.HEAD
                || !event.getFrom().is(ModItems.NIGHT_VISION_HELMET.get())
                || event.getTo().is(ModItems.NIGHT_VISION_HELMET.get())) {
            return;
        }

        MobEffectInstance effect = event.getEntity().getEffect(MobEffects.NIGHT_VISION);
        if (effect != null && NightVisionHelmetItem.isHelmetEffect(effect)) {
            event.getEntity().removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}
