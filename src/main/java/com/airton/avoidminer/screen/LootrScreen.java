package com.airton.avoidminer.screen;

import com.airton.avoidminer.menu.LootrMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class LootrScreen extends AbstractContainerScreen<LootrMenu> {
    private static final Identifier TEXTURE = Identifier.parse("avoidminer:textures/gui/avoid_lootr.png");

    private static final int TEXTURE_W = LootrMenu.TEXTURE_W;
    private static final int TEXTURE_H = LootrMenu.TEXTURE_H;

    private static final int PANEL_X = 4;
    private static final int PANEL_INNER_W = 80;

    private static final int MAIN_LEFT = 92;
    private static final int MAIN_RIGHT = 256;

    private static final int ENERGY_X = LootrMenu.ENERGY_X;
    private static final int ENERGY_Y = LootrMenu.ENERGY_Y;
    private static final int ENERGY_W = LootrMenu.ENERGY_W;
    private static final int ENERGY_H = LootrMenu.ENERGY_H;
    private static final int FUEL_X = LootrMenu.FUEL_X;
    private static final int FUEL_Y = LootrMenu.FUEL_Y;

    private static final int BANNER_Y = 4;
    private static final int BANNER_H = 9;
    private static final int PROG_Y = 15;
    private static final int PROG_H = 3;

    private static final int SEPARATOR_Y = 78;

    private static final int ACCENT = 0xFF8844CC;
    private static final int ACCENT_DIM = 0xFF663399;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFBB99DD;
    private static final int TEXT_DISABLED = 0xFF554477;

    private int tickCounter = 0;

    public LootrScreen(LootrMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE_W, TEXTURE_H);
        this.titleLabelX = 9999;
        this.titleLabelY = 9999;
        this.inventoryLabelX = LootrMenu.MAIN_X;
        this.inventoryLabelY = 111;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        tickCounter++;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0f, 0f, TEXTURE_W, TEXTURE_H, TEXTURE_W, TEXTURE_H);

        drawModeBanner(extractor, x, y);
        drawProgressBar(extractor, x, y);
        drawSectionSeparator(extractor, x, y, SEPARATOR_Y);
        drawSpecialSlotFrames(extractor, x, y);
        drawEnergyBar(extractor, x, y);
        drawFuelGlow(extractor, x, y);
        drawInfoPanel(extractor, x, y);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawSectionSeparator(GuiGraphicsExtractor extractor, int gx, int gy, int y) {
        int dim = (ACCENT & 0x00FFFFFF) >> 2 | 0xFF000000;
        extractor.fill(gx + MAIN_LEFT, gy + y, gx + MAIN_RIGHT, gy + y + 1, dim);
        extractor.fill(gx + MAIN_LEFT, gy + y + 1, gx + MAIN_RIGHT, gy + y + 2, 0xFF0A060A);
    }

    private void drawModeBanner(GuiGraphicsExtractor extractor, int gx, int gy) {
        int dim = (ACCENT & 0x00FFFFFF) >> 1 | 0xFF000000;

        int bandTop = gy + BANNER_Y;
        int bandBot = bandTop + BANNER_H;
        int left = gx + MAIN_LEFT;
        int right = gx + MAIN_RIGHT;

        extractor.fill(left, bandTop, left + 2, bandBot, ACCENT);
        extractor.fill(left + 2, bandTop, right, bandBot, dim);

        String label = "LOOTR";
        int labelW = font.width(label);
        int tx = left + 2 + (right - left - 2 - labelW) / 2;
        extractor.text(font, Component.literal(label), tx, gy + BANNER_Y + 1, TEXT_WHITE);
    }

    private void drawProgressBar(GuiGraphicsExtractor extractor, int gx, int gy) {
        int barX = gx + MAIN_LEFT + 2;
        int barY = gy + PROG_Y;
        int barW = MAIN_RIGHT - MAIN_LEFT - 4;

        extractor.fill(barX, barY, barX + barW, barY + PROG_H, 0xFF0A0A12);

        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        boolean burning = menu.isBurning();

        if (burning && maxProgress > 0 && progress > 0) {
            int fillW = (int) ((long) progress * barW / Math.max(1, maxProgress));
            fillW = Math.clamp(fillW, 1, barW);

            extractor.fill(barX, barY, barX + fillW, barY + PROG_H, ACCENT);
            extractor.fill(barX, barY, barX + fillW, barY + 1, 0x88FFFFFF);

            int shimmer = (tickCounter * 2) % barW;
            if (shimmer < fillW) {
                extractor.fill(barX + shimmer, barY, barX + shimmer + 4, barY + PROG_H, 0x44FFFFFF);
            }
        }
    }

    private void drawSpecialSlotFrames(GuiGraphicsExtractor extractor, int gx, int gy) {
        drawSlotFrame(extractor, gx + LootrMenu.MAIN_X + 0 * LootrMenu.UPG_SPACING, gy + LootrMenu.UPG_Y, ACCENT);
        drawSlotFrame(extractor, gx + LootrMenu.MAIN_X + 1 * LootrMenu.UPG_SPACING, gy + LootrMenu.UPG_Y, ACCENT);
        drawSlotFrame(extractor, gx + LootrMenu.MAIN_X + 2 * LootrMenu.UPG_SPACING, gy + LootrMenu.UPG_Y, ACCENT);

        int cardAccent = menu.hasValidCard() ? ACCENT : ACCENT_DIM;
        drawSlotFrame(extractor, gx + LootrMenu.CARD_X, gy + LootrMenu.CARD_Y, cardAccent);
    }

    private void drawSlotFrame(GuiGraphicsExtractor extractor, int sx, int sy, int color) {
        extractor.fill(sx - 1, sy - 1, sx + 18, sy, color);
        extractor.fill(sx - 1, sy + 17, sx + 18, sy + 18, color);
        extractor.fill(sx - 1, sy, sx, sy + 17, color);
        extractor.fill(sx + 17, sy, sx + 18, sy + 17, color);
    }

    private void drawEnergyBar(GuiGraphicsExtractor extractor, int gx, int gy) {
        int barX = gx + ENERGY_X + 2;
        int barY = gy + ENERGY_Y + 2;
        int barW = ENERGY_W - 4;
        int barH = ENERGY_H - 4;

        int energy = menu.getEnergyStored();
        int maxEnergy = menu.getEnergyCapacity();
        if (maxEnergy > 0 && energy > 0) {
            int fillH = Math.clamp((int) ((long) energy * barH / maxEnergy), 1, barH);
            int fillY = barY + (barH - fillH);
            extractor.fill(barX, fillY, barX + barW, barY + barH, ACCENT);
            extractor.fill(barX, fillY, barX + 1, fillY + Math.min(fillH, 2), 0xFFFFFFFF);

            int glowOffset = (tickCounter / 4) % 4;
            extractor.fill(barX, fillY + glowOffset, barX + barW, fillY + glowOffset + 1, 0x33FFFFFF);
        }
    }

    private void drawFuelGlow(GuiGraphicsExtractor extractor, int gx, int gy) {
        if (menu.isBurning()) {
            int sx = gx + FUEL_X;
            int sy = gy + FUEL_Y;
            extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, 0x338844CC);
        }
    }

    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy) {
        int px = gx + PANEL_X;

        String tierLabel = "LOOTR";
        extractor.text(font, Component.literal(tierLabel),
                px + (PANEL_INNER_W - font.width(tierLabel)) / 2, gy + 6, ACCENT);
        String subtitle = "AVOID LOOTR";
        extractor.text(font, Component.literal(subtitle),
                px + (PANEL_INNER_W - font.width(subtitle)) / 2, gy + 16, TEXT_DIM);

        drawPanelDivider(extractor, px, gy + 26);

        extractor.text(font, Component.translatable("screen.avoidminer.lootr.card"), px, gy + 31, TEXT_DISABLED);
        Component cardStatus = menu.hasValidCard()
                ? Component.translatable("screen.avoidminer.lootr.card.ready")
                : Component.translatable("screen.avoidminer.lootr.card.empty");
        extractor.text(font, cardStatus, px, gy + 41, menu.hasValidCard() ? 0xFF55FF55 : ACCENT);

        drawPanelDivider(extractor, px, gy + 51);

        extractor.text(font, Component.translatable("screen.avoidminer.lootr.progress"), px, gy + 55, TEXT_DISABLED);
        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        int pct = maxProgress > 0 ? Math.min(100, progress * 100 / maxProgress) : 0;
        String pctStr = pct + "%";
        extractor.text(font, Component.literal(pctStr), px, gy + 65, menu.isBurning() ? ACCENT : TEXT_DISABLED);

        extractor.text(font, Component.translatable("screen.avoidminer.upgrades"), px, gy + 78, TEXT_DISABLED);

        int cardH = 18;
        int cardGap = 2;
        int cardYStart = gy + 88;

        drawUpgradeCard(extractor, px, cardYStart,
                Component.translatable("screen.avoidminer.upgrade.speed"),
                menu.getSpeedUpgradeTier(),
                switch (menu.getSpeedUpgradeTier()) { case 1 -> "/1.5"; case 2 -> "/1.7"; case 3 -> "/2.0"; default -> ""; });

        drawUpgradeCard(extractor, px, cardYStart + cardH + cardGap,
                Component.translatable("screen.avoidminer.lootr.loot"),
                menu.getLootingLevel(),
                menu.getLootingLevel() > 0 ? "Lv" + menu.getLootingLevel() : "");

        drawUpgradeCard(extractor, px, cardYStart + 2 * (cardH + cardGap),
                Component.translatable("screen.avoidminer.lootr.rarity"),
                menu.hasRarityUpgrade() ? 1 : 0,
                menu.hasRarityUpgrade() ? "rare" : "");

        int statusY = cardYStart + 3 * (cardH + cardGap) + 4;
        drawPanelDivider(extractor, px, statusY - 2);
        extractor.text(font, Component.translatable("screen.avoidminer.status"), px, statusY + 1, TEXT_DISABLED);
        boolean burning = menu.isBurning();
        boolean outputFull = menu.isOutputFull();
        Component status = outputFull
                ? Component.translatable("status.avoidminer.full")
                : Component.translatable(burning ? "status.avoidminer.running" : "status.avoidminer.idle");
        int statusColor = outputFull ? 0xFFFF5555 : burning ? 0xFF55FF55 : 0xFFAA5544;
        extractor.text(font, status, px, statusY + 11, statusColor);

        String opLabel = menu.lastOperationSucceeded()
                ? Component.translatable("status.avoidminer.success").getString()
                : Component.translatable("status.avoidminer.fail").getString();
        int opColor = menu.lastOperationSucceeded() ? 0xFF55FF55 : 0xFFFF5555;
        extractor.text(font, Component.literal(opLabel), px, statusY + 22, opColor);
    }

    private void drawUpgradeCard(GuiGraphicsExtractor extractor, int px, int cardY, Component typeName, int tier, String effect) {
        int cardH = 18;
        boolean active = tier > 0;
        extractor.fill(px, cardY, px + PANEL_INNER_W, cardY + cardH, active ? 0x88332255 : 0x44221133);
        extractor.fill(px, cardY, px + PANEL_INNER_W, cardY + 1, active ? 0x884466AA : 0x44223344);

        if (!active) {
            extractor.text(font, Component.translatable("screen.avoidminer.slot.empty"),
                    px + 4, cardY + (cardH - 8) / 2, TEXT_DISABLED);
        } else {
            extractor.text(font, typeName, px + 4, cardY + 2, TEXT_WHITE);
            if (!effect.isEmpty()) {
                extractor.text(font, Component.literal(effect), px + 4, cardY + 10, ACCENT);
            }
        }
    }

    private void drawPanelDivider(GuiGraphicsExtractor extractor, int px, int y) {
        extractor.fill(px - 1, y, px + PANEL_INNER_W + 1, y + 1, 0x44556688);
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int relX = mouseX - x;
        int relY = mouseY - y;

        if (relX >= ENERGY_X && relX < ENERGY_X + ENERGY_W
                && relY >= ENERGY_Y && relY < ENERGY_Y + ENERGY_H) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("tooltip.avoidminer.energy", menu.getEnergyStored(), menu.getEnergyCapacity()),
                    mouseX, mouseY);
        }
    }
}
