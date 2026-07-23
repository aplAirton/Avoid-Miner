package com.airton.avoidminer.screen;

import com.airton.avoidminer.client.RangeCardOverlay;
import com.airton.avoidminer.item.MobGrinderRangeCardItem;
import com.airton.avoidminer.menu.MobGrinderMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MobGrinderScreen extends AbstractContainerScreen<MobGrinderMenu> {
    private static final int PANEL_FILL = 0xFFC6C6C6;
    private static final int PANEL_BORDER = 0xFF555555;
    private static final int SLOT_INNER = 0xFF8B8B8B;
    private static final int SLOT_TOP_LEFT = 0xFF373737;
    private static final int SLOT_BOTTOM_RIGHT = 0xFFFFFFFF;
    private static final int TEXT_DARK = 0xFF404040;

    private static final int TOGGLE_X = 8;
    private static final int TOGGLE_Y = 22;
    private static final int TOGGLE_W = 20;
    private static final int TOGGLE_H = 20;

    private static final int PACIFIST_X = 8;
    private static final int PACIFIST_Y = 44;
    private static final int PACIFIST_W = 20;
    private static final int PACIFIST_H = 20;

    private static final int ENERGY_X = 150;
    private static final int ENERGY_Y = 22;
    private static final int ENERGY_W = 18;
    private static final int ENERGY_H = 42;

    private boolean overlayLocal;
    private boolean pacifistLocal;

    public MobGrinderScreen(MobGrinderMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 166);
        titleLabelX = (176 - font.width(title)) / 2;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = MobGrinderMenu.PLAYER_Y - 11;
        overlayLocal = menu.isOverlayEnabled();
        pacifistLocal = menu.isPacifist();
    }

    @Override
    protected void init() {
        super.init();
        if (overlayLocal) syncOverlay();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor ex, int mx, int my, float pt) {
        int gx = (width - imageWidth) / 2, gy = (height - imageHeight) / 2;
        drawPanel(ex, gx, gy);
        drawSlots(ex, gx, gy);
        drawEnergyBar(ex, gx, gy, mx, my);
        drawToggle(ex, gx, gy, mx, my);
        drawPacifistToggle(ex, gx, gy, mx, my);
        super.extractContents(ex, mx, my, pt);
    }

    private void drawPanel(GuiGraphicsExtractor ex, int gx, int gy) {
        ex.fill(gx, gy, gx + imageWidth, gy + imageHeight, PANEL_FILL);
        ex.fill(gx, gy, gx + imageWidth, gy + 1, PANEL_BORDER);
        ex.fill(gx + imageWidth - 1, gy, gx + imageWidth, gy + imageHeight, PANEL_BORDER);
        ex.fill(gx, gy + imageHeight - 1, gx + imageWidth, gy + imageHeight, PANEL_BORDER);
        ex.fill(gx, gy, gx + 1, gy + imageHeight, PANEL_BORDER);
    }

    private void drawSlot(GuiGraphicsExtractor ex, int sx, int sy) {
        ex.fill(sx, sy, sx + 18, sy + 18, SLOT_INNER);
        ex.fill(sx, sy, sx + 18, sy + 1, SLOT_TOP_LEFT);
        ex.fill(sx, sy, sx + 1, sy + 18, SLOT_TOP_LEFT);
        ex.fill(sx + 17, sy + 1, sx + 18, sy + 18, SLOT_BOTTOM_RIGHT);
        ex.fill(sx + 1, sy + 17, sx + 18, sy + 18, SLOT_BOTTOM_RIGHT);
    }

    private void drawSlots(GuiGraphicsExtractor ex, int gx, int gy) {
        drawSlot(ex, gx + MobGrinderMenu.RANGE_X, gy + MobGrinderMenu.RANGE_Y);
        drawSlot(ex, gx + MobGrinderMenu.DAMAGE_X, gy + MobGrinderMenu.DAMAGE_Y);
        drawSlot(ex, gx + MobGrinderMenu.DAMAGE_CARD_X, gy + MobGrinderMenu.DAMAGE_CARD_Y);
        drawSlot(ex, gx + MobGrinderMenu.LOOT_X, gy + MobGrinderMenu.LOOT_Y);
        drawSlot(ex, gx + MobGrinderMenu.ATTRACTION_X, gy + MobGrinderMenu.ATTRACTION_Y);
        drawSlot(ex, gx + MobGrinderMenu.FUEL_X, gy + MobGrinderMenu.FUEL_Y);
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++)
            drawSlot(ex, gx + MobGrinderMenu.PLAYER_X + c * 18, gy + MobGrinderMenu.PLAYER_Y + r * 18);
        for (int c = 0; c < 9; c++)
            drawSlot(ex, gx + MobGrinderMenu.PLAYER_X + c * 18, gy + MobGrinderMenu.HOTBAR_Y);
    }

    private void drawInfo(GuiGraphicsExtractor ex, int gx, int gy) {
    }

    private void drawEnergyBar(GuiGraphicsExtractor ex, int gx, int gy, int mx, int my) {
        int bx = gx + ENERGY_X, by = gy + ENERGY_Y;
        ex.fill(bx, by, bx + ENERGY_W, by + ENERGY_H, 0xFF555555);
        ex.fill(bx + 1, by + 1, bx + ENERGY_W - 1, by + ENERGY_H - 1, 0xFF000000);
        int e = menu.getEnergyStored(), max = menu.getEnergyCapacity();
        if (max > 0 && e > 0) {
            int fh = Math.max(1, (int)((long)e * (ENERGY_H - 2) / max));
            ex.fill(bx + 1, by + ENERGY_H - 1 - fh, bx + ENERGY_W - 1, by + ENERGY_H - 1, 0xFFCC3333);
        }
        if (mx >= bx && mx < bx + ENERGY_W && my >= by && my < by + ENERGY_H)
            ex.setTooltipForNextFrame(Component.translatable("tooltip.avoidminer.energy", e, max), mx, my);
    }

    private void drawToggle(GuiGraphicsExtractor ex, int gx, int gy, int mx, int my) {
        int bx = gx + TOGGLE_X, by = gy + TOGGLE_Y;
        boolean h = mx >= bx && mx < bx + TOGGLE_W && my >= by && my < by + TOGGLE_H, on = overlayLocal;
        ex.fill(bx + 1, by + 1, bx + TOGGLE_W - 1, by + TOGGLE_H - 1, SLOT_INNER);
        ex.fill(bx, by, bx + TOGGLE_W, by + 1, h ? SLOT_BOTTOM_RIGHT : SLOT_TOP_LEFT);
        ex.fill(bx, by, bx + 1, by + TOGGLE_H, h ? SLOT_BOTTOM_RIGHT : SLOT_TOP_LEFT);
        ex.fill(bx + TOGGLE_W - 1, by + 1, bx + TOGGLE_W, by + TOGGLE_H, h ? SLOT_TOP_LEFT : SLOT_BOTTOM_RIGHT);
        ex.fill(bx + 1, by + TOGGLE_H - 1, bx + TOGGLE_W, by + TOGGLE_H, h ? SLOT_TOP_LEFT : SLOT_BOTTOM_RIGHT);
        int in = bx + 5, iy = by + 5, iw = TOGGLE_W - 10, ih = TOGGLE_H - 10;
        ex.fill(in, iy, in + iw, iy + ih, on ? 0x6655FF55 : 0x448B8B8B);
        ex.fill(in, iy, in + iw, iy + 1, on ? 0xFF55FF55 : 0xFF555555);
        ex.fill(in, iy + ih - 1, in + iw, iy + ih, on ? 0xFF55FF55 : 0xFF555555);
        ex.fill(in, iy, in + 1, iy + ih, on ? 0xFF55FF55 : 0xFF555555);
        ex.fill(in + iw - 1, iy, in + iw, iy + ih, on ? 0xFF55FF55 : 0xFF555555);
        if (!on) { ex.fill(in + iw / 2, iy, in + iw / 2 + 1, iy + ih, 0xFFFF5555);
            ex.fill(in, iy + ih / 2, in + iw, iy + ih / 2 + 1, 0xFFFF5555); }
    }

    private void drawPacifistToggle(GuiGraphicsExtractor ex, int gx, int gy, int mx, int my) {
        int bx = gx + PACIFIST_X, by = gy + PACIFIST_Y;
        boolean h = mx >= bx && mx < bx + PACIFIST_W && my >= by && my < by + PACIFIST_H;
        boolean on = pacifistLocal;
        ex.fill(bx + 1, by + 1, bx + PACIFIST_W - 1, by + PACIFIST_H - 1, SLOT_INNER);
        ex.fill(bx, by, bx + PACIFIST_W, by + 1, h ? SLOT_BOTTOM_RIGHT : SLOT_TOP_LEFT);
        ex.fill(bx, by, bx + 1, by + PACIFIST_H, h ? SLOT_BOTTOM_RIGHT : SLOT_TOP_LEFT);
        ex.fill(bx + PACIFIST_W - 1, by + 1, bx + PACIFIST_W, by + PACIFIST_H, h ? SLOT_TOP_LEFT : SLOT_BOTTOM_RIGHT);
        ex.fill(bx + 1, by + PACIFIST_H - 1, bx + PACIFIST_W, by + PACIFIST_H, h ? SLOT_TOP_LEFT : SLOT_BOTTOM_RIGHT);
        int in = bx + 5, iy = by + 5, iw = PACIFIST_W - 10, ih = PACIFIST_H - 10;
        if (on) {
            ex.fill(in + 2, iy + 2, in + iw - 2, iy + ih - 2, 0x44FF8844);
        }
        ex.fill(in, iy, in + iw, iy + 1, on ? 0xFFFF8844 : 0xFF555555);
        ex.fill(in, iy + ih - 1, in + iw, iy + ih, on ? 0xFFFF8844 : 0xFF555555);
    }

    @Override public boolean mouseClicked(MouseButtonEvent ev, boolean dbl) {
        if (ev.button() == 0) { int gx = (width - imageWidth) / 2, gy = (height - imageHeight) / 2;
            if (ev.x() >= gx + TOGGLE_X && ev.x() < gx + TOGGLE_X + TOGGLE_W
                    && ev.y() >= gy + TOGGLE_Y && ev.y() < gy + TOGGLE_Y + TOGGLE_H) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MobGrinderMenu.OVERLAY_BUTTON);
                overlayLocal = !overlayLocal;
                syncOverlay();
                return true; }
            if (ev.x() >= gx + PACIFIST_X && ev.x() < gx + PACIFIST_X + PACIFIST_W
                    && ev.y() >= gy + PACIFIST_Y && ev.y() < gy + PACIFIST_Y + PACIFIST_H) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MobGrinderMenu.PACIFIST_BUTTON);
                pacifistLocal = !pacifistLocal;
                return true; } }
        return super.mouseClicked(ev, dbl);
    }

    private void syncOverlay() {
        if (overlayLocal) {
            var d = menu.getData();
            int r = d.get(0); // DATA_RANGE
            if (r > 0) {
                r = r / 2;
                int px = d.get(5), py = d.get(6), pz = d.get(7); // DATA_POS_X/Y/Z
                RangeCardOverlay.setBounds(px - r, px + r, py - r, py + r, pz - r, pz + r);
            } else {
                RangeCardOverlay.clearBounds();
            }
        } else {
            RangeCardOverlay.clearBounds();
        }
    }

    @Override public void removed() {
        super.removed();
        syncOverlay();
    }

    @Override public void extractTooltip(GuiGraphicsExtractor ex, int mx, int my) {
        super.extractTooltip(ex, mx, my); int gx = (width - imageWidth) / 2, gy = (height - imageHeight) / 2;
        if (mx >= gx + TOGGLE_X && mx < gx + TOGGLE_X + TOGGLE_W && my >= gy + TOGGLE_Y && my < gy + TOGGLE_Y + TOGGLE_H)
            ex.setTooltipForNextFrame(Component.translatable(overlayLocal ? "screen.avoidminer.miner.toggle_overlay.on" : "screen.avoidminer.miner.toggle_overlay.off"), mx, my);
        if (mx >= gx + PACIFIST_X && mx < gx + PACIFIST_X + PACIFIST_W && my >= gy + PACIFIST_Y && my < gy + PACIFIST_Y + PACIFIST_H)
            ex.setTooltipForNextFrame(Component.translatable("screen.avoidminer.mob_grinder.pacifist"), mx, my);
        if (isEmptySlot(mx, my, gx, gy, MobGrinderMenu.RANGE_X, MobGrinderMenu.RANGE_Y, 0))
            ex.setTooltipForNextFrame(Component.translatable("screen.avoidminer.mob_grinder.range_slot"), mx, my);
        if (isEmptySlot(mx, my, gx, gy, MobGrinderMenu.DAMAGE_X, MobGrinderMenu.DAMAGE_Y, 1))
            ex.setTooltipForNextFrame(Component.translatable("screen.avoidminer.mob_grinder.damage_slot"), mx, my);
        if (isEmptySlot(mx, my, gx, gy, MobGrinderMenu.DAMAGE_CARD_X, MobGrinderMenu.DAMAGE_CARD_Y, 5))
            ex.setTooltipForNextFrame(Component.translatable("screen.avoidminer.mob_grinder.damage_card_slot"), mx, my);
        if (isEmptySlot(mx, my, gx, gy, MobGrinderMenu.LOOT_X, MobGrinderMenu.LOOT_Y, 2))
            ex.setTooltipForNextFrame(Component.translatable("screen.avoidminer.mob_grinder.loot_slot"), mx, my);
        if (isEmptySlot(mx, my, gx, gy, MobGrinderMenu.ATTRACTION_X, MobGrinderMenu.ATTRACTION_Y, 4))
            ex.setTooltipForNextFrame(Component.translatable("screen.avoidminer.mob_grinder.attraction_slot"), mx, my);
        if (isEmptySlot(mx, my, gx, gy, MobGrinderMenu.FUEL_X, MobGrinderMenu.FUEL_Y, 3))
            ex.setTooltipForNextFrame(Component.translatable("screen.avoidminer.miner.fuel"), mx, my);
    }

    private boolean isEmptySlot(int mx, int my, int gx, int gy, int sx, int sy, int slot) {
        return mx >= gx + sx && mx < gx + sx + 18 && my >= gy + sy && my < gy + sy + 18
                && menu.getCarried().isEmpty() && !menu.getSlot(slot).hasItem();
    }
}
