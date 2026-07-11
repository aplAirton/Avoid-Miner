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
    private static final Identifier TEXTURE = Identifier.parse("avoidminer:textures/gui/avoid_miner.png");

    private static final int TEXTURE_W = 260;
    private static final int PANEL_W = 64;

    private static final int FUEL_X = 108;
    private static final int FUEL_Y = 18;

    private static final int PROG_X = 128;
    private static final int PROG_Y = 22;
    private static final int PROG_W = 82;
    private static final int PROG_H = 10;

    private static final int ENERGY_X = 236;
    private static final int ENERGY_W = 12;
    private static final int ENERGY_Y = 18;
    private static final int ENERGY_H = 96;

    private static final int INPUT_SX = 72;

    private static final int ACCENT_T1 = 0xFF55AA55;
    private static final int ACCENT_T2 = 0xFFFFAA33;
    private static final int ACCENT_T3 = 0xFFFF5555;

    public ProcessorScreen(ProcessorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE_W, getImageHeight(menu.getMachineTier()));
        this.titleLabelX = 9999;
        this.titleLabelY = 9999;
        this.inventoryLabelX = PANEL_W + 8;
        this.inventoryLabelY = menu.getMachineTier() == 3 ? 127 : 111;
    }

    private static int getImageHeight(int tier) {
        return tier == 3 ? 218 : 200;
    }

    private int accentColor() {
        return switch (menu.getMachineTier()) {
            case 2 -> ACCENT_T2;
            case 3 -> ACCENT_T3;
            default -> ACCENT_T1;
        };
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int h = menu.getMachineTier() == 3 ? 218 : 200;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0f, 0f, TEXTURE_W, Math.min(200, h), TEXTURE_W, Math.min(200, h));

        drawProgressBar(extractor, x, y);
        drawEnergyBar(extractor, x, y);

        Tier t = switch (menu.getMachineTier()) {
            case 2 -> Tier.TIER_2;
            case 3 -> Tier.TIER_3;
            default -> Tier.TIER_1;
        };
        int inputStart = t.getInputStart();
        int inputEnd = t.getInputEnd();
        int inputY = INPUT_SX;
        for (int slot = inputStart; slot <= inputEnd; slot++) {
            int inputIndex = slot - inputStart;
            int ix, iy;
            if (t == Tier.TIER_3 && inputIndex >= 3) {
                int col = inputIndex - 3;
                ix = x + inputY + col * 18;
                iy = y + 114;
            } else {
                ix = x + inputY + inputIndex * 18;
                iy = y + 96;
            }
            drawInputSlotBg(extractor, ix, iy);
        }

        drawInfoPanel(extractor, x, y);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawInputSlotBg(GuiGraphicsExtractor extractor, int sx, int sy) {
        int accent = accentColor();
        int dim = (accent & 0x00FFFFFF) >> 1 | 0xFF000000;
        extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, accent);
        extractor.fill(sx, sy, sx + 17, sy + 1, dim);
        extractor.fill(sx, sy, sx + 1, sy + 17, dim);
    }

    private void drawProgressBar(GuiGraphicsExtractor extractor, int gx, int gy) {
        int barX = gx + PROG_X + 1;
        int barY = gy + PROG_Y + 1;
        int barW = PROG_W - 2;
        int barH = PROG_H - 2;

        int progress = menu.getRawProgress();
        int maxProgress = menu.getMaxProgress();
        boolean burning = menu.isBurning();

        if (burning && maxProgress > 0) {
            int fillW = (int) ((long) progress * barW / Math.max(1, maxProgress));
            if (fillW > barW) fillW = barW;
            if (fillW > 0) {
                extractor.fill(barX, barY, barX + fillW, barY + barH, accentColor());
                extractor.fill(barX, barY, barX + 1, barY + barH, 0xFFFFFFFF);
            }
            int fillWActual = (int) ((long) progress * barW / Math.max(1, maxProgress));
            if (fillWActual < barW) {
                extractor.fill(barX + fillWActual, barY, barX + barW, barY + barH, 0xFF222233);
            }
        } else {
            extractor.fill(barX, barY, barX + barW, barY + barH, 0xFF222233);
            extractor.fill(barX, barY + barH / 2, barX + barW, barY + barH / 2 + 1, 0xFF554444);
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
            int fillH = (int) ((long) energy * barH / maxEnergy);
            if (fillH > barH) fillH = barH;
            if (fillH < 1) fillH = 1;
            int fillY = barY + (barH - fillH);
            extractor.fill(barX, fillY, barX + barW, barY + barH, accentColor());
            extractor.fill(barX, fillY, barX + 1, fillY + Math.min(fillH, 2), 0xFFFFFFFF);
        }
    }

    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy) {
        int textColor = 0xFFFFFFFF;
        int dimColor = 0xFFAAAACC;
        int disabledColor = 0xFF666677;
        int accent = accentColor();

        int tierLevel = menu.getMachineTier();

        String tierLabel = "TIER " + tierLevel;
        int tierLabelW = font.width(tierLabel);
        extractor.text(font, Component.literal(tierLabel), gx + 4 + (60 - tierLabelW) / 2, gy + 6, accent);
        String subtitle = "PROCESSOR";
        int subW = font.width(subtitle);
        extractor.text(font, Component.literal(subtitle), gx + 4 + (60 - subW) / 2, gy + 16, dimColor);

        extractor.text(font, Component.translatable("screen.avoidminer.input"), gx + 4, gy + 32, disabledColor);
        int inputCount = switch (tierLevel) { case 2 -> 3; case 3 -> 6; default -> 1; };
        String inputLabel = inputCount + " " + Component.translatable("screen.avoidminer.input.slots").getString();
        extractor.text(font, Component.literal(inputLabel), gx + 4, gy + 42, accent);

        extractor.text(font, Component.translatable("screen.avoidminer.upgrades"), gx + 4, gy + 56, disabledColor);

        int upgSlotCount = menu.getUpgradeSlotCount();
        int cardX = gx + 3;
        int cardW = 58;
        int cardH = 20;
        int cardGap = 1;
        int cardYStart = gy + 66;
        for (int i = 0; i < 3; i++) {
            int cardY = cardYStart + i * (cardH + cardGap);
            int type = menu.getUpgradeType(i);
            int upgTier = menu.getUpgradeTier(i);

            if (i < upgSlotCount) {
                extractor.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0x88223344);
                extractor.fill(cardX, cardY, cardX + cardW, cardY + 1, 0x88446688);

                if (type == 0) {
                    extractor.text(font, Component.translatable("screen.avoidminer.slot.empty"),
                            cardX + 4, cardY + (cardH - 8) / 2, disabledColor);
                } else {
                    Component typeName = switch (type) {
                        case 1 -> Component.translatable("screen.avoidminer.upgrade.mining");
                        case 2 -> Component.translatable("screen.avoidminer.upgrade.energy");
                        case 3 -> Component.translatable("screen.avoidminer.upgrade.speed");
                        default -> Component.literal("?");
                    };
                    String effect = switch (type) {
                        case 2 -> switch (upgTier) { case 1 -> "x0.8"; case 2 -> "x0.7"; case 3 -> "x0.6"; default -> "?"; };
                        case 3 -> switch (upgTier) { case 1 -> "/1.5"; case 2 -> "/1.7"; case 3 -> "/2.0"; default -> "?"; };
                        default -> "";
                    };
                    extractor.text(font, Component.translatable("screen.avoidminer.upgrade.tier", typeName, upgTier), cardX + 4, cardY + 3, textColor);
                    extractor.text(font, Component.literal(effect), cardX + 4, cardY + 12, accent);
                }
            } else {
                extractor.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0x44222222);
                extractor.text(font, Component.translatable("screen.avoidminer.slot.locked"),
                        cardX + 4, cardY + (cardH - 8) / 2, disabledColor);
            }
        }

        int statusY = cardYStart + 3 * (cardH + cardGap) + 4;
        if (tierLevel == 3 && statusY > gy + 180) statusY = gy + 180;
        extractor.text(font, Component.translatable("screen.avoidminer.status"), gx + 4, statusY, disabledColor);
        boolean burning = menu.isBurning();
        boolean outputFull = menu.isOutputFull();
        Component status = outputFull
                ? Component.translatable("status.avoidminer.full")
                : Component.translatable(burning ? "status.avoidminer.running" : "status.avoidminer.idle");
        int statusColor = outputFull ? 0xFFFF5555 : burning ? 0xFF55FF55 : 0xFFAA5555;
        extractor.text(font, status, gx + 4, statusY + 10, statusColor);
        extractor.fill(gx + 4, statusY + 21, gx + 60, statusY + 22, 0x88444466);

        extractor.text(font, Component.translatable("screen.avoidminer.cost"), gx + 4, statusY + 24, dimColor);
        extractor.text(font, Component.translatable("screen.avoidminer.cost_per_gen", menu.getEnergyCostPerProcess()),
                gx + 4, statusY + 34, 0xFFAAAAFF);
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
            int energy = menu.getEnergyStored();
            int maxEnergy = menu.getEnergyCapacity();
            extractor.setTooltipForNextFrame(
                    Component.translatable("tooltip.avoidminer.energy", energy, maxEnergy),
                    mouseX, mouseY);
        }
    }
}
