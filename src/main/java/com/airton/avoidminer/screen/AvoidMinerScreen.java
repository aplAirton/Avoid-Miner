package com.airton.avoidminer.screen;

import com.airton.avoidminer.menu.AvoidMinerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class AvoidMinerScreen extends AbstractContainerScreen<AvoidMinerMenu> {
    private static final Identifier TEXTURE = Identifier.parse("avoidminer:textures/gui/avoid_miner.png");

    private static final int TEXTURE_W = 260;
    private static final int TEXTURE_H = 200;
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

    private static final int UPG_X = 72;
    private static final int UPG_Y = 96;
    private static final int WORLD_SX = 130;
    private static final int ENCHANT_SX = 152;
    private static final int SLOT_SY = 96;

    private static final int ACCENT_T1 = 0xFF55AA55;
    private static final int ACCENT_T2 = 0xFFFFAA33;
    private static final int ACCENT_T3 = 0xFFFF5555;

    public AvoidMinerScreen(AvoidMinerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE_W, TEXTURE_H);
        this.titleLabelX = 9999;
        this.titleLabelY = 9999;
        this.inventoryLabelX = PANEL_W + 8;
        this.inventoryLabelY = 111;
    }

    private int accentColor() {
        return switch (menu.getMachineTier()) {
            case 1 -> ACCENT_T1;
            case 2 -> ACCENT_T2;
            case 3 -> ACCENT_T3;
            default -> ACCENT_T1;
        };
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0f, 0f, TEXTURE_W, TEXTURE_H, TEXTURE_W, TEXTURE_H);

        drawModeBanner(extractor, x, y);
        drawProgressBar(extractor, x, y);
        drawEnergyBar(extractor, x, y);
        drawSpecialSlotBg(extractor, x + WORLD_SX, y + SLOT_SY, 0xFF225522, 0xFF338833);
        drawSpecialSlotBg(extractor, x + ENCHANT_SX, y + SLOT_SY, 0xFF555020, 0xFF887733);

        int slotCount = menu.getUpgradeSlotCount();
        drawInfoPanel(extractor, x, y, slotCount);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawSpecialSlotBg(GuiGraphicsExtractor extractor, int sx, int sy, int borderCol, int lightCol) {
        extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, borderCol);
        extractor.fill(sx, sy, sx + 17, sy + 1, lightCol);
        extractor.fill(sx, sy, sx + 1, sy + 17, lightCol);
    }

    private void drawModeBanner(GuiGraphicsExtractor extractor, int gx, int gy) {
        int accent = accentColor();
        int dim = (accent & 0x00FFFFFF) >> 1 | 0xFF000000;
        int x0 = gx + PANEL_W + 2;
        int y0 = gy + 2;
        int x1 = gx + TEXTURE_W - 3;
        int y1 = gy + 14;
        extractor.fill(x0, y0, x0 + 2, y1, accent);
        extractor.fill(x0 + 2, y0, x1, y1, dim);

        String mode = switch (menu.getWorldMode()) {
            case 1 -> "BOTANY";
            case 2 -> "NETHER";
            default -> "OVERWORLD";
        };
        String enchant = switch (menu.getEnchantMode()) {
            case 1 -> "  + Fortune";
            case 2 -> "  + Silk";
            default -> "";
        };
        String label = mode + enchant;
        int labelW = font.width(label);
        int tx = x0 + 4 + (x1 - x0 - 4 - labelW) / 2;
        int ty = y0 + (12 - 8) / 2 + 1;
        extractor.text(font, Component.literal(label), tx, ty, 0xFFFFFFFF);
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

    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy, int slotCount) {
        int textColor = 0xFFFFFFFF;
        int dimColor = 0xFFAAAACC;
        int disabledColor = 0xFF666677;
        int accent = accentColor();

        // Tier label
        String tierLabel = "TIER " + menu.getMachineTier();
        int tierLabelW = font.width(tierLabel);
        extractor.text(font, Component.literal(tierLabel), gx + 4 + (60 - tierLabelW) / 2, gy + 6, accent);
        String subtitle = "AVOID MINER";
        int subW = font.width(subtitle);
        extractor.text(font, Component.literal(subtitle), gx + 4 + (60 - subW) / 2, gy + 16, dimColor);

        // Mode
        extractor.text(font, Component.translatable("screen.avoidminer.mode"), gx + 4, gy + 32, disabledColor);
        Component modeStr = switch (menu.getWorldMode()) {
            case 1 -> Component.translatable("screen.avoidminer.mode.botany");
            case 2 -> Component.translatable("screen.avoidminer.mode.nether");
            default -> Component.translatable("screen.avoidminer.mode.overworld");
        };
        extractor.text(font, modeStr, gx + 4, gy + 42, accent);

        // Enchant
        Component enchStr = switch (menu.getEnchantMode()) {
            case 1 -> Component.translatable("screen.avoidminer.enchant.fortune");
            case 2 -> Component.translatable("screen.avoidminer.enchant.silk_touch");
            default -> Component.translatable("screen.avoidminer.enchant.none");
        };
        int enchColor = menu.getEnchantMode() > 0 ? 0xFFFFDD44 : disabledColor;
        extractor.text(font, enchStr, gx + 4, gy + 53, enchColor);

        // Upgrades section
        extractor.text(font, Component.translatable("screen.avoidminer.upgrades"), gx + 4, gy + 68, disabledColor);

        int cardX = gx + 3;
        int cardW = 58;
        int cardH = 20;
        int cardGap = 1;
        int cardYStart = gy + 78;
        for (int i = 0; i < 3; i++) {
            int cardY = cardYStart + i * (cardH + cardGap);
            int type = menu.getUpgradeType(i);
            int tier = menu.getUpgradeTier(i);

            if (i < slotCount) {
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
                        case 1 -> switch (tier) { case 1 -> "x1.5"; case 2 -> "x1.7"; case 3 -> "x2.0"; default -> "?"; };
                        case 2 -> switch (tier) { case 1 -> "x0.8"; case 2 -> "x0.7"; case 3 -> "x0.6"; default -> "?"; };
                        case 3 -> switch (tier) { case 1 -> "/1.5"; case 2 -> "/1.7"; case 3 -> "/2.0"; default -> "?"; };
                        default -> "";
                    };
                    extractor.text(font, Component.translatable("screen.avoidminer.upgrade.tier", typeName, tier), cardX + 4, cardY + 3, textColor);
                    extractor.text(font, Component.literal(effect), cardX + 4, cardY + 12, accent);
                }
            } else {
                extractor.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0x44222222);
                extractor.text(font, Component.translatable("screen.avoidminer.slot.locked"),
                        cardX + 4, cardY + (cardH - 8) / 2, disabledColor);
            }
        }

        // Status footer
        int statusY = cardYStart + 3 * (cardH + cardGap) + 4;
        if (statusY < gy + TEXTURE_H - 30) {
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
            extractor.text(font, Component.translatable("screen.avoidminer.cost_per_gen", menu.getEnergyCostPerGeneration()),
                    gx + 4, statusY + 34, 0xFFAAAAFF);
        }
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
