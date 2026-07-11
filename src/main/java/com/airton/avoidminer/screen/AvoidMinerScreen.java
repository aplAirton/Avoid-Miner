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

    private static final int TEXTURE_W = AvoidMinerMenu.TEXTURE_W;
    private static final int TEXTURE_H = AvoidMinerMenu.TEXTURE_H;

    // Painel lateral: x 0..87 (área útil 4..84)
    private static final int PANEL_X = 4;
    private static final int PANEL_INNER_W = 80;

    // Área principal: x 92..258
    private static final int MAIN_LEFT = 92;
    private static final int MAIN_RIGHT = 258;

    private static final int ENERGY_X = AvoidMinerMenu.ENERGY_X;
    private static final int ENERGY_Y = AvoidMinerMenu.ENERGY_Y;
    private static final int ENERGY_W = AvoidMinerMenu.ENERGY_W;
    private static final int ENERGY_H = AvoidMinerMenu.ENERGY_H;
    private static final int FUEL_X = AvoidMinerMenu.FUEL_X;
    private static final int FUEL_Y = AvoidMinerMenu.FUEL_Y;

    private static final int BANNER_Y = 4;
    private static final int BANNER_H = 9;
    private static final int PROG_Y = 15;
    private static final int PROG_H = 3;

    private static final int SEPARATOR_Y = 78;
    private static final int GROUP_LABEL_Y = 81;

    private static final int ACCENT_T1 = 0xFF3388CC;
    private static final int ACCENT_T2 = 0xFF55AAEE;
    private static final int ACCENT_T3 = 0xFF88DDFF;

    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFF8899BB;
    private static final int TEXT_DISABLED = 0xFF455577;

    private int tickCounter = 0;

    public AvoidMinerScreen(AvoidMinerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE_W, TEXTURE_H);
        this.titleLabelX = 9999;
        this.titleLabelY = 9999;
        this.inventoryLabelX = AvoidMinerMenu.MAIN_X;
        this.inventoryLabelY = 111;
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
        tickCounter++;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0f, 0f, TEXTURE_W, TEXTURE_H, TEXTURE_W, TEXTURE_H);

        drawModeBanner(extractor, x, y);
        drawProgressBar(extractor, x, y);
        drawSectionSeparator(extractor, x, y, SEPARATOR_Y);
        drawGroupLabels(extractor, x, y);
        drawUpgradeSlots(extractor, x, y);
        drawSpecialSlotAccents(extractor, x, y);
        drawEnergyBar(extractor, x, y);
        drawFuelGlow(extractor, x, y);
        drawInfoPanel(extractor, x, y);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawSectionSeparator(GuiGraphicsExtractor extractor, int gx, int gy, int y) {
        int accent = accentColor();
        int dim = (accent & 0x00FFFFFF) >> 2 | 0xFF000000;
        extractor.fill(gx + MAIN_LEFT, gy + y, gx + MAIN_RIGHT, gy + y + 1, dim);
        extractor.fill(gx + MAIN_LEFT, gy + y + 1, gx + MAIN_RIGHT, gy + y + 2, 0xFF050810);
    }

    private void drawModeBanner(GuiGraphicsExtractor extractor, int gx, int gy) {
        int accent = accentColor();
        int dim = (accent & 0x00FFFFFF) >> 1 | 0xFF000000;

        int bandTop = gy + BANNER_Y;
        int bandBot = bandTop + BANNER_H;
        int left = gx + MAIN_LEFT;
        int right = gx + MAIN_RIGHT;

        extractor.fill(left, bandTop, left + 2, bandBot, accent);
        extractor.fill(left + 2, bandTop, right, bandBot, dim);

        String mode = switch (menu.getWorldMode()) {
            case 1 -> "BOTANY";
            case 2 -> "NETHER";
            default -> "OVERWORLD";
        };
        String enchant = switch (menu.getEnchantMode()) {
            case 1 -> " +FORTUNE";
            case 2 -> " +SILK";
            default -> "";
        };
        String label = mode + enchant;
        int labelW = font.width(label);
        int tx = left + 2 + (right - left - 2 - labelW) / 2;
        extractor.text(font, Component.literal(label), tx, gy + BANNER_Y + 1, TEXT_WHITE);
    }

    private void drawProgressBar(GuiGraphicsExtractor extractor, int gx, int gy) {
        int barX = gx + MAIN_LEFT + 2;
        int barY = gy + PROG_Y;
        int barW = MAIN_RIGHT - MAIN_LEFT - 4;

        extractor.fill(barX, barY, barX + barW, barY + PROG_H, 0xFF0A0A12);

        int progress = menu.getRawProgress();
        int maxProgress = menu.getMaxProgress();
        boolean burning = menu.isBurning();

        if (burning && maxProgress > 0 && progress > 0) {
            int fillW = (int) ((long) progress * barW / Math.max(1, maxProgress));
            fillW = Math.clamp(fillW, 1, barW);

            int accent = accentColor();
            extractor.fill(barX, barY, barX + fillW, barY + PROG_H, accent);
            extractor.fill(barX, barY, barX + fillW, barY + 1, 0x88FFFFFF);

            int shimmer = (tickCounter * 2) % barW;
            if (shimmer < fillW) {
                extractor.fill(barX + shimmer, barY, barX + shimmer + 4, barY + PROG_H, 0x44FFFFFF);
            }
        }
    }

    private void drawGroupLabels(GuiGraphicsExtractor extractor, int gx, int gy) {
        int labelColor = 0xFF66779A;

        extractor.text(font, Component.translatable("screen.avoidminer.upgrades"),
                gx + AvoidMinerMenu.MAIN_X, gy + GROUP_LABEL_Y, labelColor);

        Component worldLabel = Component.translatable("screen.avoidminer.world");
        int worldW = font.width(worldLabel);
        extractor.text(font, worldLabel, gx + AvoidMinerMenu.WORLD_X + 9 - worldW / 2, gy + GROUP_LABEL_Y, labelColor);

        Component enchLabel = Component.translatable("screen.avoidminer.effect");
        int enchW = font.width(enchLabel);
        extractor.text(font, enchLabel, gx + AvoidMinerMenu.ENCHANT_X + 9 - enchW / 2, gy + GROUP_LABEL_Y, labelColor);
    }

    // Slots de melhoria com "lugar marcado": os desbloqueados ganham borda de destaque,
    // os acima do tier ficam escurecidos com um cadeado.
    private void drawUpgradeSlots(GuiGraphicsExtractor extractor, int gx, int gy) {
        int slotCount = menu.getUpgradeSlotCount();
        int accent = accentColor();

        for (int i = 0; i < 3; i++) {
            int sx = gx + AvoidMinerMenu.MAIN_X + i * AvoidMinerMenu.UPG_SPACING;
            int sy = gy + AvoidMinerMenu.UPG_Y;

            if (i < slotCount) {
                extractor.fill(sx - 1, sy - 1, sx + 18, sy, accent);
                extractor.fill(sx - 1, sy + 17, sx + 18, sy + 18, accent);
                extractor.fill(sx - 1, sy, sx - 1 + 1, sy + 17, accent);
                extractor.fill(sx + 17, sy, sx + 18, sy + 17, accent);
            } else {
                extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, 0xCC10141F);
                // cadeado: corpo + alça
                int cx = sx + 8;
                extractor.fill(cx - 3, sy + 7, cx + 4, sy + 13, 0xFF55617F);
                extractor.fill(cx - 2, sy + 4, cx - 1, sy + 7, 0xFF55617F);
                extractor.fill(cx + 2, sy + 4, cx + 3, sy + 7, 0xFF55617F);
                extractor.fill(cx - 2, sy + 4, cx + 3, sy + 5, 0xFF55617F);
            }
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
            int accent = accentColor();
            extractor.fill(barX, fillY, barX + barW, barY + barH, accent);
            extractor.fill(barX, fillY, barX + 1, fillY + Math.min(fillH, 2), 0xFFFFFFFF);

            int glowOffset = (tickCounter / 4) % 4;
            extractor.fill(barX, fillY + glowOffset, barX + barW, fillY + glowOffset + 1, 0x33FFFFFF);
        }
    }

    private void drawFuelGlow(GuiGraphicsExtractor extractor, int gx, int gy) {
        if (menu.isBurning()) {
            int sx = gx + FUEL_X;
            int sy = gy + FUEL_Y;
            extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, 0x333399CC);
        }
    }

    private void drawSpecialSlotAccents(GuiGraphicsExtractor extractor, int gx, int gy) {
        int worldAccent = switch (menu.getWorldMode()) {
            case 1 -> 0xFF33AA55;
            case 2 -> 0xFFAA3333;
            default -> 0xFF224488;
        };
        int enchantAccent = switch (menu.getEnchantMode()) {
            case 1 -> 0xFFFFDD44;
            case 2 -> 0xFFDDDDFF;
            default -> 0xFF555544;
        };

        drawSlotFrame(extractor, gx + AvoidMinerMenu.WORLD_X, gy + AvoidMinerMenu.WORLD_Y, worldAccent);
        drawSlotFrame(extractor, gx + AvoidMinerMenu.ENCHANT_X, gy + AvoidMinerMenu.ENCHANT_Y, enchantAccent);
    }

    private void drawSlotFrame(GuiGraphicsExtractor extractor, int sx, int sy, int color) {
        extractor.fill(sx - 1, sy - 1, sx + 18, sy, color);
        extractor.fill(sx - 1, sy + 17, sx + 18, sy + 18, color);
        extractor.fill(sx - 1, sy, sx, sy + 17, color);
        extractor.fill(sx + 17, sy, sx + 18, sy + 17, color);
    }

    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy) {
        int accent = accentColor();
        int px = gx + PANEL_X;

        String tierLabel = "TIER " + menu.getMachineTier();
        extractor.text(font, Component.literal(tierLabel),
                px + (PANEL_INNER_W - font.width(tierLabel)) / 2, gy + 6, accent);
        String subtitle = "AVOID MINER";
        extractor.text(font, Component.literal(subtitle),
                px + (PANEL_INNER_W - font.width(subtitle)) / 2, gy + 16, TEXT_DIM);

        drawPanelDivider(extractor, px, gy + 26);

        extractor.text(font, Component.translatable("screen.avoidminer.mode"), px, gy + 31, TEXT_DISABLED);
        Component modeStr = switch (menu.getWorldMode()) {
            case 1 -> Component.translatable("screen.avoidminer.mode.botany");
            case 2 -> Component.translatable("screen.avoidminer.mode.nether");
            default -> Component.translatable("screen.avoidminer.mode.overworld");
        };
        extractor.text(font, modeStr, px, gy + 41, accent);

        Component enchStr = switch (menu.getEnchantMode()) {
            case 1 -> Component.translatable("screen.avoidminer.enchant.fortune");
            case 2 -> Component.translatable("screen.avoidminer.enchant.silk_touch");
            default -> Component.translatable("screen.avoidminer.enchant.none");
        };
        int enchColor = menu.getEnchantMode() > 0 ? 0xFFEEDD44 : TEXT_DISABLED;
        extractor.text(font, enchStr, px, gy + 51, enchColor);

        drawPanelDivider(extractor, px, gy + 61);

        extractor.text(font, Component.translatable("screen.avoidminer.upgrades"), px, gy + 65, TEXT_DISABLED);

        int slotCount = menu.getUpgradeSlotCount();
        int cardH = 18;
        int cardGap = 2;
        int cardYStart = gy + 75;
        for (int i = 0; i < 3; i++) {
            int cardY = cardYStart + i * (cardH + cardGap);
            int type = menu.getUpgradeType(i);
            int tier = menu.getUpgradeTier(i);

            if (i < slotCount) {
                extractor.fill(px, cardY, px + PANEL_INNER_W, cardY + cardH, 0x88224455);
                extractor.fill(px, cardY, px + PANEL_INNER_W, cardY + 1, 0x88446688);

                if (type == 0) {
                    extractor.text(font, Component.translatable("screen.avoidminer.slot.empty"),
                            px + 4, cardY + (cardH - 8) / 2, TEXT_DISABLED);
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
                    extractor.text(font, Component.translatable("screen.avoidminer.upgrade.tier", typeName, tier),
                            px + 4, cardY + 2, TEXT_WHITE);
                    extractor.text(font, Component.literal(effect), px + 4, cardY + 10, accent);
                }
            } else {
                extractor.fill(px, cardY, px + PANEL_INNER_W, cardY + cardH, 0x44110905);
                extractor.text(font, Component.translatable("screen.avoidminer.slot.locked"),
                        px + 4, cardY + (cardH - 8) / 2, TEXT_DISABLED);
            }
        }

        int statusY = cardYStart + 3 * (cardH + cardGap) + 4;
        drawPanelDivider(extractor, px, statusY - 2);
        extractor.text(font, Component.translatable("screen.avoidminer.status"), px, statusY + 1, TEXT_DISABLED);
        boolean burning = menu.isBurning();
        boolean outputFull = menu.isOutputFull();
        Component status = outputFull
                ? Component.translatable("status.avoidminer.full")
                : Component.translatable(burning ? "status.avoidminer.running" : "status.avoidminer.idle");
        int statusColor = outputFull ? 0xFFFF5555 : burning ? 0xFF55FF55 : 0xFFAA5555;
        extractor.text(font, status, px, statusY + 11, statusColor);

        extractor.text(font, Component.translatable("screen.avoidminer.cost"), px, statusY + 23, TEXT_DIM);
        extractor.text(font, Component.translatable("screen.avoidminer.cost_per_gen", menu.getEnergyCostPerGeneration()),
                px, statusY + 33, 0xFFAABBDD);
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
            return;
        }

        // Tooltip nos slots de melhoria bloqueados
        int slotCount = menu.getUpgradeSlotCount();
        for (int i = slotCount; i < 3; i++) {
            int sx = AvoidMinerMenu.MAIN_X + i * AvoidMinerMenu.UPG_SPACING;
            if (relX >= sx - 1 && relX < sx + 18
                    && relY >= AvoidMinerMenu.UPG_Y - 1 && relY < AvoidMinerMenu.UPG_Y + 18) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.locked", i + 1),
                        mouseX, mouseY);
                return;
            }
        }
    }
}
