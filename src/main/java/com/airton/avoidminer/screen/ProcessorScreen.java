package com.airton.avoidminer.screen;

import com.airton.avoidminer.block.entity.ProcessorBlockEntity.Tier;
import com.airton.avoidminer.menu.ProcessorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ProcessorScreen extends AbstractContainerScreen<ProcessorMenu> {
    private static final Identifier TEXTURE = Identifier.parse("avoidminer:textures/gui/avoid_processor.png");

    private static final int TEXTURE_W = ProcessorMenu.TEXTURE_W;
    private static final int TEXTURE_H = ProcessorMenu.TEXTURE_H;

    private static final int PANEL_X = 4;
    private static final int PANEL_INNER_W = 80;

    private static final int MAIN_LEFT = 92;
    private static final int MAIN_RIGHT = 258;

    private static final int ENERGY_X = ProcessorMenu.ENERGY_X;
    private static final int ENERGY_Y = ProcessorMenu.ENERGY_Y;
    private static final int ENERGY_W = ProcessorMenu.ENERGY_W;
    private static final int ENERGY_H = ProcessorMenu.ENERGY_H;
    private static final int FUEL_X = ProcessorMenu.FUEL_X;
    private static final int FUEL_Y = ProcessorMenu.FUEL_Y;

    private static final int SEPARATOR_Y = 84;
    private static final int GROUP_LABEL_Y = 81;

    private static final int SLOT_BORDER = 0xFF3A2A10;
    private static final int SLOT_BORDER_LT = 0xFF4A3A20;
    private static final int SLOT_BORDER_DK = 0xFF050301;
    private static final int SLOT_INNER = 0xFF0C0804;
    private static final int ACCENT = 0xFFCC8833;
    private static final int ACCENT_DIM = 0xFF664422;

    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFCCAA88;
    private static final int TEXT_DISABLED = 0xFF554433;

    private int tickCounter = 0;

    public ProcessorScreen(ProcessorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE_W, TEXTURE_H);
        this.titleLabelX = 9999;
        this.titleLabelY = 9999;
        this.inventoryLabelX = ProcessorMenu.MAIN_X;
        this.inventoryLabelY = 111;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        tickCounter++;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0f, 0f, TEXTURE_W, TEXTURE_H, TEXTURE_W, TEXTURE_H);

        Tier t = menu.getTier();

        drawTitleBanner(extractor, x, y);
        drawProcessingArea(extractor, x, y, t);
        drawSectionSeparator(extractor, x, y, SEPARATOR_Y);
        drawUpgradeArea(extractor, x, y);
        drawEnergyBar(extractor, x, y);
        drawFuelGlow(extractor, x, y);
        drawInfoPanel(extractor, x, y);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawTitleBanner(GuiGraphicsExtractor extractor, int gx, int gy) {
        int left = gx + MAIN_LEFT;
        int right = gx + MAIN_RIGHT;
        extractor.fill(left, gy + 4, left + 2, gy + 13, ACCENT);
        extractor.fill(left + 2, gy + 4, right, gy + 13, 0xFF33220E);

        Component label = Component.translatable("screen.avoidminer.processing");
        int labelW = font.width(label);
        extractor.text(font, label, left + 2 + (right - left - 2 - labelW) / 2, gy + 5, TEXT_WHITE);
    }

    private void drawProcessingArea(GuiGraphicsExtractor extractor, int gx, int gy, Tier t) {
        if (t.inputCount == 1) {
            // Tier 1: entrada > seta horizontal > saída
            drawSlotBg(extractor, gx + ProcessorMenu.T1_INPUT_X, gy + ProcessorMenu.T1_ROW_Y);
            drawSlotBg(extractor, gx + ProcessorMenu.T1_OUTPUT_X, gy + ProcessorMenu.T1_ROW_Y);
            drawHorizontalArrow(extractor,
                    gx + ProcessorMenu.T1_ARROW_X,
                    gy + ProcessorMenu.T1_ROW_Y + (18 - ProcessorMenu.T1_ARROW_ROW_H) / 2,
                    menu.getInputProgress(0), menu.getMaxProgress());
        } else {
            // Tiers 2/3: colunas entrada > progresso > saída
            for (int i = 0; i < t.inputCount; i++) {
                int sx = gx + ProcessorMenu.inputSlotX(t, i);
                drawSlotBg(extractor, sx, gy + ProcessorMenu.INPUT_Y);
                drawVerticalArrow(extractor, sx, gy + ProcessorMenu.ARROW_Y,
                        menu.getInputProgress(i), menu.getMaxProgress(), i);
                drawSlotBg(extractor, sx, gy + ProcessorMenu.OUTPUT_Y);
            }
        }
    }

    private void drawSectionSeparator(GuiGraphicsExtractor extractor, int gx, int gy, int y) {
        extractor.fill(gx + MAIN_LEFT, gy + y, gx + MAIN_RIGHT, gy + y + 1, SLOT_BORDER);
        extractor.fill(gx + MAIN_LEFT, gy + y + 1, gx + MAIN_RIGHT, gy + y + 2, 0xFF0A0604);
    }

    // Dois slots de melhoria com lugar marcado: E = Energia, S = Velocidade
    private void drawUpgradeArea(GuiGraphicsExtractor extractor, int gx, int gy) {
        extractor.text(font, Component.translatable("screen.avoidminer.upgrades"),
                gx + ProcessorMenu.MAIN_X, gy + GROUP_LABEL_Y + 6, 0xFF9A7744);

        drawMarkedUpgradeSlot(extractor, gx + ProcessorMenu.UPG_ENERGY_X, gy + ProcessorMenu.UPG_Y, "E",
                menu.getEnergyUpgradeTier() > 0);
        drawMarkedUpgradeSlot(extractor, gx + ProcessorMenu.UPG_SPEED_X, gy + ProcessorMenu.UPG_Y, "S",
                menu.getSpeedUpgradeTier() > 0);
    }

    private void drawMarkedUpgradeSlot(GuiGraphicsExtractor extractor, int sx, int sy, String letter, boolean filled) {
        drawSlotBg(extractor, sx, sy);
        int frame = filled ? ACCENT : ACCENT_DIM;
        extractor.fill(sx - 1, sy - 1, sx + 18, sy, frame);
        extractor.fill(sx - 1, sy + 17, sx + 18, sy + 18, frame);
        extractor.fill(sx - 1, sy, sx, sy + 17, frame);
        extractor.fill(sx + 17, sy, sx + 18, sy + 17, frame);
        if (!filled) {
            int lw = font.width(letter);
            extractor.text(font, Component.literal(letter), sx + 9 - lw / 2, sy + 5, 0xFF6A5230);
        }
    }

    private void drawSlotBg(GuiGraphicsExtractor extractor, int sx, int sy) {
        extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, SLOT_BORDER);
        extractor.fill(sx, sy, sx + 17, sy + 17, SLOT_INNER);
        extractor.fill(sx, sy, sx + 17, sy + 1, SLOT_BORDER_LT);
        extractor.fill(sx, sy, sx + 1, sy + 17, SLOT_BORDER_LT);
        extractor.fill(sx + 17, sy + 1, sx + 18, sy + 17, SLOT_BORDER_DK);
        extractor.fill(sx + 1, sy + 17, sx + 17, sy + 18, SLOT_BORDER_DK);
    }

    private void drawHorizontalArrow(GuiGraphicsExtractor extractor, int sx, int sy, int progress, int maxProgress) {
        int barW = ProcessorMenu.T1_ARROW_W;
        int barH = ProcessorMenu.T1_ARROW_ROW_H;

        extractor.fill(sx, sy, sx + barW, sy + barH, 0xFF120806);
        // ponta da seta
        extractor.fill(sx + barW - 4, sy - 2, sx + barW - 3, sy + barH + 2, 0xFF221208);

        if (maxProgress > 0 && progress > 0) {
            int fillW = Math.clamp((int) ((long) progress * barW / Math.max(1, maxProgress)), 1, barW);
            extractor.fill(sx, sy + 2, sx + fillW, sy + barH - 2, ACCENT);
            extractor.fill(sx, sy + 2, sx + fillW, sy + 3, 0x66FFFFFF);

            int shimmer = (tickCounter * 2) % barW;
            if (shimmer < fillW) {
                extractor.fill(sx + shimmer, sy + 2, sx + shimmer + 3, sy + barH - 2, 0x44FFFFFF);
            }
        } else {
            extractor.fill(sx + 4, sy + barH / 2 - 1, sx + barW - 6, sy + barH / 2 + 1, 0xFF221208);
        }
    }

    private void drawVerticalArrow(GuiGraphicsExtractor extractor, int sx, int sy, int progress, int maxProgress, int slotIndex) {
        int barW = 18;
        int barH = ProcessorMenu.ARROW_H;

        extractor.fill(sx, sy, sx + barW, sy + barH, 0xFF120806);

        if (maxProgress > 0 && progress > 0) {
            int fillH = Math.clamp((int) ((long) progress * barH / Math.max(1, maxProgress)), 1, barH);
            int fillX = sx + 5;
            int fillW = barW - 10;

            extractor.fill(fillX, sy, fillX + fillW, sy + fillH, ACCENT);
            extractor.fill(fillX, sy, fillX + 1, sy + fillH, 0x66FFFFFF);

            int shimmer = (tickCounter + slotIndex * 4) % 8;
            if (shimmer < 3 && fillH > 4) {
                int shimmerY = sy + fillH - 2 - shimmer;
                if (shimmerY >= sy && shimmerY < sy + fillH - 1) {
                    extractor.fill(fillX, shimmerY, fillX + fillW, shimmerY + 1, 0x88FFFFFF);
                }
            }
        } else {
            // seta apagada apontando para baixo
            extractor.fill(sx + 7, sy + 3, sx + 11, sy + 8, 0xFF221208);
            extractor.fill(sx + 5, sy + 8, sx + 13, sy + 10, 0xFF221208);
            extractor.fill(sx + 7, sy + 10, sx + 11, sy + 12, 0xFF221208);
        }
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
            extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, 0x33CC8833);
        }
    }

    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy) {
        int px = gx + PANEL_X;

        String tierLabel = "TIER " + menu.getMachineTier();
        extractor.text(font, Component.literal(tierLabel),
                px + (PANEL_INNER_W - font.width(tierLabel)) / 2, gy + 6, ACCENT);
        String subtitle = "PROCESSOR";
        extractor.text(font, Component.literal(subtitle),
                px + (PANEL_INNER_W - font.width(subtitle)) / 2, gy + 16, TEXT_DIM);

        drawPanelDivider(extractor, px, gy + 26);

        extractor.text(font, Component.translatable("screen.avoidminer.input"), px, gy + 31, TEXT_DISABLED);
        extractor.text(font, Component.translatable("screen.avoidminer.input.count", menu.getInputCount()),
                px, gy + 41, ACCENT);
        extractor.text(font, Component.translatable("screen.avoidminer.ticks_per_item", menu.getMaxProgress()),
                px, gy + 51, TEXT_DIM);

        drawPanelDivider(extractor, px, gy + 61);

        extractor.text(font, Component.translatable("screen.avoidminer.upgrades"), px, gy + 65, TEXT_DISABLED);

        int cardH = 18;
        int cardGap = 2;
        int cardYStart = gy + 75;
        drawUpgradeCard(extractor, px, cardYStart,
                Component.translatable("screen.avoidminer.upgrade.energy"),
                menu.getEnergyUpgradeTier(),
                switch (menu.getEnergyUpgradeTier()) { case 1 -> "x0.8"; case 2 -> "x0.7"; case 3 -> "x0.6"; default -> ""; });
        drawUpgradeCard(extractor, px, cardYStart + cardH + cardGap,
                Component.translatable("screen.avoidminer.upgrade.speed"),
                menu.getSpeedUpgradeTier(),
                switch (menu.getSpeedUpgradeTier()) { case 1 -> "/1.5"; case 2 -> "/1.7"; case 3 -> "/2.0"; default -> ""; });

        int statusY = cardYStart + 2 * (cardH + cardGap) + 4;
        drawPanelDivider(extractor, px, statusY - 2);
        extractor.text(font, Component.translatable("screen.avoidminer.status"), px, statusY + 1, TEXT_DISABLED);
        boolean burning = menu.isBurning();
        boolean blocked = menu.isOutputBlocked();
        Component status = blocked
                ? Component.translatable("status.avoidminer.full")
                : Component.translatable(burning ? "status.avoidminer.running" : "status.avoidminer.idle");
        int statusColor = blocked ? 0xFFFF5555 : burning ? 0xFF55FF55 : 0xFFAA5544;
        extractor.text(font, status, px, statusY + 11, statusColor);

        extractor.text(font, Component.translatable("screen.avoidminer.cost"), px, statusY + 23, TEXT_DIM);
        extractor.text(font, Component.translatable("screen.avoidminer.cost_per_item", energyCostPerItem()),
                px, statusY + 33, 0xFFDDBB88);
    }

    // Custo total por item processado: ticks base x multiplicador de energia
    // (a velocidade encurta o ciclo mas não muda o custo total)
    private int energyCostPerItem() {
        float energyMult = switch (menu.getEnergyUpgradeTier()) {
            case 1 -> 0.8f; case 2 -> 0.7f; case 3 -> 0.6f; default -> 1.0f;
        };
        return Math.max(1, Math.round(menu.getBaseTicksPerProcess() * energyMult));
    }

    private void drawUpgradeCard(GuiGraphicsExtractor extractor, int px, int cardY, Component typeName, int tier, String effect) {
        int cardH = 18;
        extractor.fill(px, cardY, px + PANEL_INNER_W, cardY + cardH, 0x88332211);
        extractor.fill(px, cardY, px + PANEL_INNER_W, cardY + 1, 0x88664422);

        if (tier <= 0) {
            extractor.text(font, Component.translatable("screen.avoidminer.upgrade.tier.none", typeName),
                    px + 4, cardY + (cardH - 8) / 2, TEXT_DISABLED);
        } else {
            extractor.text(font, Component.translatable("screen.avoidminer.upgrade.tier", typeName, tier),
                    px + 4, cardY + 2, TEXT_WHITE);
            extractor.text(font, Component.literal(effect), px + 4, cardY + 10, ACCENT);
        }
    }

    private void drawPanelDivider(GuiGraphicsExtractor extractor, int px, int y) {
        extractor.fill(px - 1, y, px + PANEL_INNER_W + 1, y + 1, 0x44664422);
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
            return;
        }

        if (relY >= ProcessorMenu.UPG_Y - 1 && relY < ProcessorMenu.UPG_Y + 18) {
            if (relX >= ProcessorMenu.UPG_ENERGY_X - 1 && relX < ProcessorMenu.UPG_ENERGY_X + 18
                    && menu.getEnergyUpgradeTier() == 0) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.energy_only"), mouseX, mouseY);
            } else if (relX >= ProcessorMenu.UPG_SPEED_X - 1 && relX < ProcessorMenu.UPG_SPEED_X + 18
                    && menu.getSpeedUpgradeTier() == 0) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.speed_only"), mouseX, mouseY);
            }
        }
    }
}
