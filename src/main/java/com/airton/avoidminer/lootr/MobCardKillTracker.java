package com.airton.avoidminer.lootr;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Conta abates quando o jogador tem um cartão de mob na mão secundária (offhand).
 * Somente o tipo certo de criatura incrementa o contador do cartão correspondente.
 *
 * <p>Registrado no bus do mod via {@link EventBusSubscriber}.</p>
 */
@EventBusSubscriber
public class MobCardKillTracker {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        // A fonte deve ser um player (morto pelo jogador diretamente).
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(player instanceof ServerPlayer)) return;

        ItemStack offhand = player.getOffhandItem();
        if (!(offhand.getItem() instanceof MobCardItem cardItem)) return;

        MobCardType cardType = cardItem.getCardType();
        // Compara pelo EntityType; criaturas filhas (ex: zumbi gigante) não contam.
        if (victim.getType() != cardType.entityType) return;

        int current = MobCardItem.getKills(offhand);
        if (current >= cardType.requiredKills) return;

        int next = current + 1;
        MobCardItem.setKills(offhand, next);

        if (next >= cardType.requiredKills) {
            player.sendSystemMessage(
                    Component.translatable("msg.avoidminer.mobcard.completed",
                            Component.translatable("entity." + cardType.id))
                            .withStyle(net.minecraft.ChatFormatting.GREEN));
        } else {
            player.sendSystemMessage(
                    Component.translatable("msg.avoidminer.mobcard.kill_counted",
                            Component.translatable("entity." + cardType.id),
                            next, cardType.requiredKills)
                            .withStyle(net.minecraft.ChatFormatting.YELLOW));
        }
    }
}