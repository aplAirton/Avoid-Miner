package com.airton.avoidminer.client;

import com.airton.avoidminer.lootr.MobCardItem;
import com.airton.avoidminer.lootr.MobCardType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * HUD overlay mostrada quando o jogador segura um cartão de mob na mão
 * secundária (offhand). Exibe o nome da criatura, quantos já foram mortos
 * e quantos ainda faltam, com uma barra de progresso visual.
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class MobCardHud {

    private static final int BAR_H = 6;

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (mc.options.hideGui) return;

        ItemStack offhand = player.getOffhandItem();
        if (!(offhand.getItem() instanceof MobCardItem cardItem)) return;

        MobCardType type = cardItem.getCardType();
        int kills = MobCardItem.getKills(offhand);
        int max = type.requiredKills;
        boolean completed = kills >= max;

        GuiGraphicsExtractor gg = event.getGuiGraphics();
        Font font = mc.font;

        int boxW = 160;
        int boxH = 50;
        int baseX = gg.guiWidth() - 8 - boxW;
        int baseY = gg.guiHeight() - 8 - boxH;

        // Borda + fundo
        gg.fill(baseX - 2, baseY - 2, baseX + boxW + 2, baseY + boxH + 2, 0x80202020);
        gg.fill(baseX - 1, baseY - 1, baseX + boxW + 1, baseY + boxH + 1, 0xFF050505);
        gg.fill(baseX, baseY, baseX + boxW, baseY + boxH, 0xCC0E0E16);

        // Título
        Component title = Component.translatable("hud.avoidminer.mobcard.title",
                Component.translatable("entity." + type.id));
        gg.text(font, title, baseX + 8, baseY + 6, 0xFFFFFFFF, true);

        // Progresso textual
        Component progress = Component.translatable("hud.avoidminer.mobcard.progress", kills, max);
        int color = completed ? 0xFF55FF55 : 0xFFFFAA00;
        gg.text(font, progress, baseX + 8, baseY + 18, color, true);

        // Barra visual
        int barX = baseX + 8;
        int barY = baseY + 32;
        int barW = boxW - 16;
        gg.fill(barX - 1, barY - 1, barX + barW + 1, barY + BAR_H + 1, 0xFF101010);
        gg.fill(barX, barY, barX + barW, barY + BAR_H, 0xFF333333);

        if (max > 0 && kills > 0) {
            int fillW = Math.min(barW, (int) ((long) kills * barW / max));
            int fillCol = completed ? 0xFF55FF55 : 0xFFFFAA00;
            gg.fill(barX, barY, barX + fillW, barY + BAR_H, fillCol);
            gg.fill(barX, barY, barX + fillW, barY + 1, 0x88FFFFFF);
        }

        // Indicação final
        if (completed) {
            gg.text(font, Component.translatable("hud.avoidminer.mobcard.ready"),
                    baseX + 8, baseY + 38, 0xFF55FF55, true);
        } else {
            gg.text(font, Component.translatable("hud.avoidminer.mobcard.instruction"),
                    baseX + 8, baseY + 38, 0xFFAA8844, true);
        }
    }
}