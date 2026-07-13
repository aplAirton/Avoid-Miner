package com.airton.avoidminer.screen;

import com.airton.avoidminer.block.entity.BatteryBlockEntity;
import com.airton.avoidminer.menu.BatteryMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class BatteryScreen extends AbstractContainerScreen<BatteryMenu> {
    private static final Identifier TEXTURE = Identifier.parse("avoidminer:textures/gui/battery.png");

    private static final int TEXTURE_W = BatteryMenu.TEXTURE_W;
    private static final int TEXTURE_H = BatteryMenu.TEXTURE_H;

    private static final int PANEL_X = 4;
    private static final int PANEL_INNER_W = 76;

    private static final int MAIN_LEFT = 92;
    private static final int MAIN_RIGHT = 256;

    private static final int CHAMBER_X0 = 6;
    private static final int CHAMBER_Y0 = 66;
    private static final int CHAMBER_X1 = 82;
    private static final int CHAMBER_Y1 = 196;

    private static final int ACCENT = 0xFF33DDCC;
    private static final int ACCENT_DIM = 0xFF1A7A72;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFF9BC9C4;
    private static final int TEXT_DISABLED = 0xFF3E6B66;

    private static final int SLOT_BORDER = 0xFF1A5550;
    private static final int SLOT_BORDER_LT = 0xFF339988;
    private static final int SLOT_BORDER_DK = 0xFF0A2A28;
    private static final int SLOT_INNER = 0xFF0E3E3A;

    private int tickCounter = 0;

    public BatteryScreen(BatteryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE_W, TEXTURE_H);
        this.titleLabelX = 9999;
        this.titleLabelY = 9999;
        this.inventoryLabelX = BatteryMenu.MAIN_X;
        this.inventoryLabelY = 111;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        tickCounter++;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0f, 0f, TEXTURE_W, TEXTURE_H, TEXTURE_W, TEXTURE_H);

        drawBanner(extractor, x, y);
        drawInfoPanel(extractor, x, y);
        drawEnergyCore(extractor, x, y);
        drawEnergyText(extractor, x, y);
        drawCapacityGrid(extractor, x, y);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawCapacityGrid(GuiGraphicsExtractor extractor, int gx, int gy) {
        for (int row = 0; row < BatteryMenu.CAPACITY_ROWS; row++) {
            for (int col = 0; col < BatteryMenu.CAPACITY_COLS; col++) {
                int sx = gx + BatteryMenu.CAPACITY_GRID_X + col * 18;
                int sy = gy + BatteryMenu.CAPACITY_GRID_Y + row * 18;
                drawSlotBg(extractor, sx, sy);
            }
        }
    }

    private void drawBanner(GuiGraphicsExtractor extractor, int gx, int gy) {
        int dim = (ACCENT & 0x00FFFFFF) >> 1 | 0xFF000000;
        int left = gx + MAIN_LEFT;
        int right = gx + MAIN_RIGHT;
        extractor.fill(left, gy + 4, left + 2, gy + 13, ACCENT);
        extractor.fill(left + 2, gy + 4, right, gy + 13, dim);

        Component label = Component.translatable("screen.avoidminer.battery.banner");
        int labelW = font.width(label);
        extractor.text(font, label, left + 2 + (right - left - 2 - labelW) / 2, gy + 5, TEXT_WHITE);
    }

    private void drawEnergyText(GuiGraphicsExtractor extractor, int gx, int gy) {
        String amount = String.format(Locale.ROOT, "%,d / %,d",
                menu.getEnergyStored(), menu.getEffectiveCapacity());
        extractor.text(font, Component.literal(amount),
                gx + (MAIN_LEFT + MAIN_RIGHT) / 2 - font.width(amount) / 2,
                gy + 22, TEXT_DIM);
    }

    private void drawEnergyCore(GuiGraphicsExtractor extractor, int gx, int gy) {
        int cx = gx + (CHAMBER_X0 + CHAMBER_X1) / 2;
        int y0 = gy + CHAMBER_Y0 + 10;
        int y1 = gy + CHAMBER_Y1 - 12;

        int cells = 8;
        int gap = 3;
        int cellH = (y1 - y0 - (cells - 1) * gap) / cells;
        int cellW = 36;

        long energy = menu.getEnergyStored();
        float fill = (float) energy / menu.getEffectiveCapacity();
        int litCells = Math.round(fill * cells);

        for (int i = 0; i < cells; i++) {
            int cy = y1 - (i + 1) * cellH - i * gap;
            int x0 = cx - cellW / 2;
            boolean lit = i < litCells;
            extractor.fill(x0, cy, x0 + cellW, cy + cellH, lit ? ACCENT : 0xFF10201E);
            if (lit) {
                extractor.fill(x0, cy, x0 + cellW, cy + 1, 0x88FFFFFF);
                if (i == litCells - 1) {
                    int alpha = (int) (60 + 50 * Math.sin(tickCounter * 0.15));
                    extractor.fill(x0 - 2, cy - 2, x0 + cellW + 2, cy + cellH + 2,
                            (alpha << 24) | (ACCENT & 0x00FFFFFF));
                }
            }
        }

        if (menu.getActiveLinks() > 0) {
            int sparkY = y0 + (tickCounter * 3) % Math.max(1, y1 - y0);
            extractor.fill(gx + CHAMBER_X0 + 4, sparkY, gx + CHAMBER_X0 + 7, sparkY + 2, 0xAAAAFFEE);
            extractor.fill(gx + CHAMBER_X1 - 7, (y1 - (sparkY - y0)), gx + CHAMBER_X1 - 4, (y1 - (sparkY - y0)) + 2, 0xAAAAFFEE);
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

    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy) {
        int px = gx + PANEL_X;

        Component title = Component.translatable("screen.avoidminer.battery.name");
        extractor.text(font, title,
                px + (PANEL_INNER_W - font.width(title)) / 2, gy + 6, ACCENT);

        extractor.text(font, Component.translatable("screen.avoidminer.status"), px, gy + 22, TEXT_DISABLED);

        long energy = menu.getEnergyStored();
        boolean full = energy >= menu.getEffectiveCapacity();
        Component status = full
                ? Component.translatable("status.avoidminer.full")
                : Component.translatable(menu.isCharging()
                        ? "status.avoidminer.battery.charging" : "status.avoidminer.idle");
        int statusColor = full ? ACCENT : menu.isCharging() ? 0xFF55FF55 : TEXT_DIM;
        extractor.text(font, status, px, gy + 32, statusColor);

        int pct = (int) (energy * 100L / menu.getEffectiveCapacity());
        String pctStr = pct + "%";
        extractor.text(font, Component.literal(pctStr),
                px + PANEL_INNER_W - font.width(pctStr), gy + 32, energy > 0 ? ACCENT : TEXT_DISABLED);

        drawPanelDivider(extractor, px, gy + 44);

        extractor.text(font, Component.translatable("screen.avoidminer.battery.links"), px, gy + 48, TEXT_DISABLED);
        int active = menu.getActiveLinks();
        int maxLinks = switch (menu.getRangeUpgradeTier()) {
            case 1 -> 2; case 2 -> 4; case 3 -> 8; default -> 2;
        };
        String links = active + " / " + maxLinks;
        extractor.text(font, Component.literal(links),
                px + PANEL_INNER_W - font.width(links), gy + 48,
                active > 0 ? ACCENT : TEXT_DISABLED);

        extractor.text(font, Component.translatable("screen.avoidminer.battery.capacity"), px, gy + 58, TEXT_DISABLED);
        String slotsText = menu.getEffectiveCapacity() / 1_000_000 + "m";
        extractor.text(font, Component.literal(slotsText),
                px + PANEL_INNER_W - font.width(slotsText), gy + 58, ACCENT);

        extractor.text(font, Component.translatable("screen.avoidminer.battery.range"), px, gy + 68, TEXT_DISABLED);
        String rangeTier = "T" + menu.getRangeUpgradeTier();
        extractor.text(font, Component.literal(rangeTier),
                px + PANEL_INNER_W - font.width(rangeTier), gy + 68,
                menu.getRangeUpgradeTier() > 0 ? ACCENT : TEXT_DISABLED);
    }

    private void drawPanelDivider(GuiGraphicsExtractor extractor, int px, int y) {
        extractor.fill(px - 1, y, px + PANEL_INNER_W + 1, y + 1, 0x4433DDCC);
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int relX = mouseX - x;
        int relY = mouseY - y;

        if (relX >= BatteryMenu.FUEL_X - 1 && relX < BatteryMenu.FUEL_X + 18
                && relY >= BatteryMenu.FUEL_Y - 1 && relY < BatteryMenu.FUEL_Y + 18
                && menu.getEnergyStored() < menu.getEffectiveCapacity()) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("tooltip.avoidminer.slot.fuel"), mouseX, mouseY);
            return;
        }

        if (relX >= BatteryMenu.RANGE_UPG_X - 1 && relX < BatteryMenu.RANGE_UPG_X + 18
                && relY >= BatteryMenu.RANGE_UPG_Y - 1 && relY < BatteryMenu.RANGE_UPG_Y + 18
                && menu.getRangeUpgradeTier() == 0) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("tooltip.avoidminer.slot.range_only"), mouseX, mouseY);
            return;
        }

        if (relY >= BatteryMenu.CAPACITY_GRID_Y - 1 && relY < BatteryMenu.CAPACITY_GRID_Y + BatteryMenu.CAPACITY_ROWS * 18
                && relX >= BatteryMenu.CAPACITY_GRID_X - 1 && relX < BatteryMenu.CAPACITY_GRID_X + BatteryMenu.CAPACITY_COLS * 18) {
            int col = (relX - BatteryMenu.CAPACITY_GRID_X) / 18;
            int row = (relY - BatteryMenu.CAPACITY_GRID_Y) / 18;
            int slotIndex = BatteryBlockEntity.CAPACITY_SLOT_START + row * BatteryMenu.CAPACITY_COLS + col;
            if (slotIndex < BatteryBlockEntity.CAPACITY_SLOT_END && menu.getSlot(slotIndex).getItem().isEmpty()) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.capacity_only"), mouseX, mouseY);
                return;
            }
        }

        boolean overChamber = relX >= CHAMBER_X0 && relX < CHAMBER_X1
                && relY >= CHAMBER_Y0 && relY < CHAMBER_Y1;
        if (overChamber) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("tooltip.avoidminer.energy", menu.getEnergyStored(), menu.getEffectiveCapacity()),
                    mouseX, mouseY);
        }
    }
}
