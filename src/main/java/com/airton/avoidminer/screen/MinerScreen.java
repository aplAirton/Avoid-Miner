package com.airton.avoidminer.screen;

import com.airton.avoidminer.block.entity.MinerBlockEntity;
import com.airton.avoidminer.client.RangeCardOverlay;
import com.airton.avoidminer.item.FilterCardItem;
import com.airton.avoidminer.menu.MinerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MinerScreen extends AbstractContainerScreen<MinerMenu> {
    private static final int PANEL_FILL = 0xFFC6C6C6;
    private static final int PANEL_BORDER = 0xFF555555;
    private static final int SLOT_INNER = 0xFF8B8B8B;
    private static final int SLOT_TOP_LEFT = 0xFF373737;
    private static final int SLOT_BOTTOM_RIGHT = 0xFFFFFFFF;
    private static final int TEXT_DARK = 0xFF404040;

    private static final int COLOR_RUN = 0xFF1B7A1B;
    private static final int COLOR_IDLE = 0xFF404040;
    private static final int COLOR_ERR = 0xFFAA2222;

    private static final int TOGGLE_X = 8;
    private static final int TOGGLE_Y = 44;
    private static final int TOGGLE_W = 20;
    private static final int TOGGLE_H = 20;

    private static final int RESET_X = 8;
    private static final int RESET_Y = 22;
    private static final int RESET_W = 20;
    private static final int RESET_H = 20;

    private static final int AUTO_X = 8;
    private static final int AUTO_Y = 66;
    private static final int AUTO_W = 20;
    private static final int AUTO_H = 20;

    private static final int ENERGY_X = 150;
    private static final int ENERGY_Y = 22;
    private static final int ENERGY_W = 18;
    private static final int ENERGY_H = 44;

    private static final int INFO_STATE_Y = 44;
    private static final int INFO_PROGRESS_Y = 54;
    private static final int INFO_BLOCKS_Y = 64;
    private static final int FILTER_LABEL_Y = 74;
    private static final int FILTER_GRID_Y = 84;

    private boolean overlayLocal;
    private List<ItemStack> filterDisplay = new ArrayList<>();

    public MinerScreen(MinerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 210);
        titleLabelX = (176 - font.width(title)) / 2;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = MinerMenu.PLAYER_Y - 11;
        overlayLocal = menu.isOverlayEnabled();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;

        refreshFilterDisplay();

        drawPanel(extractor, gx, gy);
        drawSlots(extractor, gx, gy);
        drawInfoPanel(extractor, gx, gy);
        drawFilterSection(extractor, gx, gy, mouseX, mouseY);
        drawEnergyBar(extractor, gx, gy, mouseX, mouseY);
        drawToggle(extractor, gx, gy, mouseX, mouseY);
        drawResetButton(extractor, gx, gy, mouseX, mouseY);
        drawAutoShutdownButton(extractor, gx, gy, mouseX, mouseY);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawPanel(GuiGraphicsExtractor extractor, int gx, int gy) {
        extractor.fill(gx, gy, gx + imageWidth, gy + imageHeight, PANEL_FILL);
        extractor.fill(gx, gy, gx + imageWidth, gy + 1, PANEL_BORDER);
        extractor.fill(gx, gy + imageHeight - 1, gx + imageWidth, gy + imageHeight, PANEL_BORDER);
        extractor.fill(gx, gy, gx + 1, gy + imageHeight, PANEL_BORDER);
        extractor.fill(gx + imageWidth - 1, gy, gx + imageWidth, gy + imageHeight, PANEL_BORDER);
    }

    private void drawVanillaSlot(GuiGraphicsExtractor extractor, int sx, int sy) {
        extractor.fill(sx, sy, sx + 18, sy + 18, SLOT_INNER);
        extractor.fill(sx, sy, sx + 18, sy + 1, SLOT_TOP_LEFT);
        extractor.fill(sx, sy, sx + 1, sy + 18, SLOT_TOP_LEFT);
        extractor.fill(sx + 17, sy + 1, sx + 18, sy + 18, SLOT_BOTTOM_RIGHT);
        extractor.fill(sx + 1, sy + 17, sx + 18, sy + 18, SLOT_BOTTOM_RIGHT);
    }

    private void drawSlots(GuiGraphicsExtractor extractor, int gx, int gy) {
        drawVanillaSlot(extractor, gx + MinerMenu.RANGE_X, gy + MinerMenu.RANGE_Y);
        drawVanillaSlot(extractor, gx + MinerMenu.FILTER_X, gy + MinerMenu.FILTER_Y);
        drawVanillaSlot(extractor, gx + MinerMenu.UPGRADE_X, gy + MinerMenu.UPGRADE_Y);
        drawVanillaSlot(extractor, gx + MinerMenu.SPEED_UPGRADE_X, gy + MinerMenu.SPEED_UPGRADE_Y);
        drawVanillaSlot(extractor, gx + MinerMenu.ENERGY_UPGRADE_X, gy + MinerMenu.ENERGY_UPGRADE_Y);
        drawVanillaSlot(extractor, gx + MinerMenu.FUEL_X, gy + MinerMenu.FUEL_Y);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawVanillaSlot(extractor,
                        gx + MinerMenu.PLAYER_X + col * MinerMenu.SLOT_SIZE,
                        gy + MinerMenu.PLAYER_Y + row * MinerMenu.SLOT_SIZE);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawVanillaSlot(extractor,
                    gx + MinerMenu.PLAYER_X + col * MinerMenu.SLOT_SIZE,
                    gy + MinerMenu.HOTBAR_Y);
        }
        if (hasFilterCard()) {
            for (int i = 0; i < MinerMenu.FILTER_MAX_DISPLAY; i++) {
                drawVanillaSlot(extractor,
                        gx + MinerMenu.FILTER_GRID_X + (i % MinerMenu.FILTER_GRID_COLS) * MinerMenu.SLOT_SIZE,
                        gy + MinerMenu.FILTER_GRID_Y + (i / MinerMenu.FILTER_GRID_COLS) * MinerMenu.SLOT_SIZE);
            }
        }
    }

    private boolean hasFilterCard() {
        return menu.getSlot(1).hasItem() && menu.getSlot(1).getItem().getItem() instanceof FilterCardItem;
    }

    private void refreshFilterDisplay() {
        filterDisplay.clear();
        if (!hasFilterCard()) return;
        ItemStack filterStack = menu.getSlot(1).getItem();
        for (Identifier id : FilterCardItem.getEntries(filterStack)) {
            var block = BuiltInRegistries.BLOCK.getValue(id);
            var item = block.asItem();
            if (item == null || item.equals(net.minecraft.world.item.Items.AIR)) {
                filterDisplay.add(ItemStack.EMPTY);
            } else {
                filterDisplay.add(new ItemStack(item));
            }
        }
    }

    private Component stateComponent() {
        int status = menu.getStatus();
        return switch (status) {
            case 1 -> Component.translatable("screen.avoidminer.miner.state.running");
            case 3 -> Component.translatable("status.avoidminer.miner.finished");
            case 4 -> Component.translatable("screen.avoidminer.miner.state.no_redstone");
            case 6 -> Component.translatable("screen.avoidminer.miner.state.no_range_card");
            case 7 -> Component.translatable("screen.avoidminer.miner.state.invalid_range_card");
            case 8 -> Component.translatable("screen.avoidminer.miner.state.no_container");
            case 9 -> Component.translatable("screen.avoidminer.miner.state.container_full");
            case 10 -> Component.translatable("screen.avoidminer.miner.state.too_far");
            default -> Component.translatable("screen.avoidminer.miner.state.idle");
        };
    }

    private int stateColor() {
        int status = menu.getStatus();
        return switch (status) {
            case 1 -> COLOR_RUN;
            case 6, 7, 8, 9, 10 -> COLOR_ERR;
            default -> COLOR_IDLE;
        };
    }

    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy) {
        int centerX = gx + imageWidth / 2;
        int totalBlocks = menu.getTotalBlocks();
        int mined = menu.getTotalMined();
        int pct = menu.getProgressPercent();

        Component stateLine = stateComponent();
        extractor.text(font, stateLine,
                centerX - font.width(stateLine) / 2, gy + INFO_STATE_Y, stateColor(), false);

        if (menu.isOperating()) {
            Component progLine = Component.literal(pct + "%");
            extractor.text(font, progLine,
                    centerX - font.width(progLine) / 2, gy + INFO_PROGRESS_Y, COLOR_RUN, false);
        }

        Component blocksLine = Component.translatable(
                "screen.avoidminer.miner.blocks", mined, totalBlocks);
        extractor.text(font, blocksLine,
                centerX - font.width(blocksLine) / 2, gy + INFO_BLOCKS_Y, TEXT_DARK, false);
    }

    private void drawFilterSection(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        ItemStack filterStack = menu.getSlot(1).getItem();
        if (filterStack.isEmpty() || !(filterStack.getItem() instanceof FilterCardItem)) return;

        Component filterLabel = Component.translatable("screen.avoidminer.miner.filter_label");
        extractor.text(font, filterLabel, gx + 8, gy + FILTER_LABEL_Y, TEXT_DARK, false);

        for (int i = 0; i < filterDisplay.size() && i < MinerMenu.FILTER_MAX_DISPLAY; i++) {
            int cx = gx + MinerMenu.FILTER_GRID_X + (i % MinerMenu.FILTER_GRID_COLS) * MinerMenu.SLOT_SIZE;
            int cy = gy + MinerMenu.FILTER_GRID_Y + (i / MinerMenu.FILTER_GRID_COLS) * MinerMenu.SLOT_SIZE;
            ItemStack stack = filterDisplay.get(i);
            if (stack.isEmpty()) continue;
            extractor.item(stack, cx + 1, cy + 1);
            if (mouseX >= cx && mouseX < cx + 18 && mouseY >= cy && mouseY < cy + 18) {
                extractor.fill(cx, cy, cx + 18, cy + 18, 0x44FF5555);
            }
        }
    }

    private void drawEnergyBar(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        int bx = gx + ENERGY_X;
        int by = gy + ENERGY_Y;
        extractor.fill(bx, by, bx + ENERGY_W, by + ENERGY_H, 0xFF555555);
        extractor.fill(bx + 1, by + 1, bx + ENERGY_W - 1, by + ENERGY_H - 1, 0xFF000000);

        int energy = menu.getEnergyStored();
        int maxEnergy = menu.getEnergyCapacity();
        if (maxEnergy > 0 && energy > 0) {
            int fillH = Math.max(1, (int) ((long) energy * (ENERGY_H - 2) / maxEnergy));
            int fillY = by + ENERGY_H - 1 - fillH;
            extractor.fill(bx + 1, fillY, bx + ENERGY_W - 1, by + ENERGY_H - 1, 0xFFCC3333);
        }

        if (mouseX >= bx && mouseX < bx + ENERGY_W && mouseY >= by && mouseY < by + ENERGY_H) {
            Component energyTip = Component.translatable("tooltip.avoidminer.energy", energy, maxEnergy);
            extractor.setTooltipForNextFrame(energyTip, mouseX, mouseY);
        }
    }

    private void drawToggle(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        int bx = gx + TOGGLE_X;
        int by = gy + TOGGLE_Y;
        boolean hovered = mouseX >= bx && mouseX < bx + TOGGLE_W
                && mouseY >= by && mouseY < by + TOGGLE_H;
        boolean on = overlayLocal;

        extractor.fill(bx + 1, by + 1, bx + TOGGLE_W - 1, by + TOGGLE_H - 1, SLOT_INNER);
        extractor.fill(bx, by, bx + TOGGLE_W, by + 1,
                hovered ? SLOT_BOTTOM_RIGHT : SLOT_TOP_LEFT);
        extractor.fill(bx, by, bx + 1, by + TOGGLE_H,
                hovered ? SLOT_BOTTOM_RIGHT : SLOT_TOP_LEFT);
        extractor.fill(bx + TOGGLE_W - 1, by + 1, bx + TOGGLE_W, by + TOGGLE_H,
                hovered ? SLOT_TOP_LEFT : SLOT_BOTTOM_RIGHT);
        extractor.fill(bx + 1, by + TOGGLE_H - 1, bx + TOGGLE_W, by + TOGGLE_H,
                hovered ? SLOT_TOP_LEFT : SLOT_BOTTOM_RIGHT);

        int inner = bx + 5;
        int innerY = by + 5;
        int innerW = TOGGLE_W - 10;
        int innerH = TOGGLE_H - 10;
        extractor.fill(inner, innerY, inner + innerW, innerY + innerH,
                on ? 0x6655FF55 : 0x448B8B8B);
        extractor.fill(inner, innerY, inner + innerW, innerY + 1,
                on ? COLOR_RUN : 0xFF555555);
        extractor.fill(inner, innerY + innerH - 1, inner + innerW, innerY + innerH,
                on ? COLOR_RUN : 0xFF555555);
        extractor.fill(inner, innerY, inner + 1, innerY + innerH,
                on ? COLOR_RUN : 0xFF555555);
        extractor.fill(inner + innerW - 1, innerY, inner + innerW, innerY + innerH,
                on ? COLOR_RUN : 0xFF555555);

        if (!on) {
            extractor.fill(inner + innerW / 2, innerY, inner + innerW / 2 + 1, innerY + innerH, 0xFFFF5555);
            extractor.fill(inner, innerY + innerH / 2, inner + innerW, innerY + innerH / 2 + 1, 0xFFFF5555);
        }
    }

    private void drawResetButton(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        int bx = gx + RESET_X;
        int by = gy + RESET_Y;
        boolean hovered = mouseX >= bx && mouseX < bx + RESET_W
                && mouseY >= by && mouseY < by + RESET_H;

        extractor.fill(bx + 1, by + 1, bx + RESET_W - 1, by + RESET_H - 1, SLOT_INNER);
        extractor.fill(bx, by, bx + RESET_W, by + 1,
                hovered ? SLOT_BOTTOM_RIGHT : SLOT_TOP_LEFT);
        extractor.fill(bx, by, bx + 1, by + RESET_H,
                hovered ? SLOT_BOTTOM_RIGHT : SLOT_TOP_LEFT);
        extractor.fill(bx + RESET_W - 1, by + 1, bx + RESET_W, by + RESET_H,
                hovered ? SLOT_TOP_LEFT : SLOT_BOTTOM_RIGHT);
        extractor.fill(bx + 1, by + RESET_H - 1, bx + RESET_W, by + RESET_H,
                hovered ? SLOT_TOP_LEFT : SLOT_BOTTOM_RIGHT);

        int cx = bx + RESET_W / 2;
        int cy = by + RESET_H / 2;
        extractor.fill(cx - 1, cy - 5, cx + 1, cy - 1, 0xFF222222);
        extractor.fill(cx + 4, cy, cx + 6, cy + 2, 0xFF222222);
        extractor.fill(cx + 4, cy - 4, cx + 6, cy - 1, 0xFF222222);
        extractor.fill(cx + 2, cy - 1, cx + 6, cy + 1, 0xFF222222);
        extractor.fill(cx + 2, cy - 3, cx + 4, cy + 1, 0xFF222222);
    }

    private void drawAutoShutdownButton(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        int bx = gx + AUTO_X;
        int by = gy + AUTO_Y;
        boolean hovered = mouseX >= bx && mouseX < bx + AUTO_W && mouseY >= by && mouseY < by + AUTO_H;
        boolean on = menu.isAutoShutdown();

        extractor.fill(bx + 1, by + 1, bx + AUTO_W - 1, by + AUTO_H - 1, SLOT_INNER);
        extractor.fill(bx, by, bx + AUTO_W, by + 1, hovered ? SLOT_BOTTOM_RIGHT : SLOT_TOP_LEFT);
        extractor.fill(bx, by, bx + 1, by + AUTO_H, hovered ? SLOT_BOTTOM_RIGHT : SLOT_TOP_LEFT);
        extractor.fill(bx + AUTO_W - 1, by + 1, bx + AUTO_W, by + AUTO_H, hovered ? SLOT_TOP_LEFT : SLOT_BOTTOM_RIGHT);
        extractor.fill(bx + 1, by + AUTO_H - 1, bx + AUTO_W, by + AUTO_H, hovered ? SLOT_TOP_LEFT : SLOT_BOTTOM_RIGHT);

        int in = bx + 5, iy = by + 5, iw = AUTO_W - 10, ih = AUTO_H - 10;
        if (on) {
            extractor.fill(in + 1, iy + 3, in + iw - 1, iy + ih - 4, 0xFF55FF55);
        }
        extractor.fill(in, iy, in + iw, iy + 1, on ? 0xFF55FF55 : 0xFF555555);
        extractor.fill(in, iy + ih - 1, in + iw, iy + ih, on ? 0xFF55FF55 : 0xFF555555);
        extractor.fill(in, iy, in + 1, iy + ih, on ? 0xFF55FF55 : 0xFF555555);
        extractor.fill(in + iw - 1, iy, in + iw, iy + ih, on ? 0xFF55FF55 : 0xFF555555);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int gx = (width - imageWidth) / 2;
            int gy = (height - imageHeight) / 2;

            if (event.x() >= gx + TOGGLE_X && event.x() < gx + TOGGLE_X + TOGGLE_W
                    && event.y() >= gy + TOGGLE_Y && event.y() < gy + TOGGLE_Y + TOGGLE_H) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MinerMenu.OVERLAY_BUTTON);
                overlayLocal = !overlayLocal;
                syncOverlayBounds();
                return true;
            }

            if (event.x() >= gx + RESET_X && event.x() < gx + RESET_X + RESET_W
                    && event.y() >= gy + RESET_Y && event.y() < gy + RESET_Y + RESET_H) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MinerMenu.RESET_BUTTON);
                return true;
            }

            if (event.x() >= gx + AUTO_X && event.x() < gx + AUTO_X + AUTO_W
                    && event.y() >= gy + AUTO_Y && event.y() < gy + AUTO_Y + AUTO_H) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MinerMenu.AUTO_SHUTDOWN_BUTTON);
                return true;
            }

            for (int i = 0; i < filterDisplay.size() && i < MinerMenu.FILTER_MAX_DISPLAY; i++) {
                int cx = gx + MinerMenu.FILTER_GRID_X + (i % MinerMenu.FILTER_GRID_COLS) * MinerMenu.SLOT_SIZE;
                int cy = gy + MinerMenu.FILTER_GRID_Y + (i / MinerMenu.FILTER_GRID_COLS) * MinerMenu.SLOT_SIZE;
                if (event.x() >= cx && event.x() < cx + 18
                        && event.y() >= cy && event.y() < cy + 18) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                            MinerMenu.FILTER_REMOVE_BASE + i);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void removed() {
        super.removed();
        syncOverlayBounds();
    }

    private void syncOverlayBounds() {
        if (overlayLocal) {
            var d = menu.getData();
            int mx = d.get(MinerBlockEntity.DATA_MIN_X);
            int Mx = d.get(MinerBlockEntity.DATA_MAX_X);
            int my = d.get(MinerBlockEntity.DATA_MIN_Y);
            int My = d.get(MinerBlockEntity.DATA_MAX_Y);
            int mz = d.get(MinerBlockEntity.DATA_MIN_Z);
            int Mz = d.get(MinerBlockEntity.DATA_MAX_Z);
            RangeCardOverlay.setBounds(mx, Mx, my, My, mz, Mz);
        } else {
            RangeCardOverlay.clearBounds();
        }
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;

        if (mouseX >= gx + TOGGLE_X && mouseX < gx + TOGGLE_X + TOGGLE_W
                && mouseY >= gy + TOGGLE_Y && mouseY < gy + TOGGLE_Y + TOGGLE_H) {
            extractor.setTooltipForNextFrame(Component.translatable(
                    overlayLocal
                            ? "screen.avoidminer.miner.toggle_overlay.on"
                            : "screen.avoidminer.miner.toggle_overlay.off"), mouseX, mouseY);
            return;
        }

        if (mouseX >= gx + RESET_X && mouseX < gx + RESET_X + RESET_W
                && mouseY >= gy + RESET_Y && mouseY < gy + RESET_Y + RESET_H) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("screen.avoidminer.miner.reset"), mouseX, mouseY);
            return;
        }

        if (mouseX >= gx + AUTO_X && mouseX < gx + AUTO_X + AUTO_W
                && mouseY >= gy + AUTO_Y && mouseY < gy + AUTO_Y + AUTO_H) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("screen.avoidminer.miner.auto_shutdown"), mouseX, mouseY);
            return;
        }

        if (mouseX >= gx + MinerMenu.RANGE_X && mouseX < gx + MinerMenu.RANGE_X + 18
                && mouseY >= gy + MinerMenu.RANGE_Y && mouseY < gy + MinerMenu.RANGE_Y + 18
                && menu.getCarried().isEmpty() && !menu.getSlot(0).hasItem()) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("screen.avoidminer.miner.range_card")
                            .withStyle(ChatFormatting.LIGHT_PURPLE), mouseX, mouseY);
            return;
        }

        if (mouseX >= gx + MinerMenu.FILTER_X && mouseX < gx + MinerMenu.FILTER_X + 18
                && mouseY >= gy + MinerMenu.FILTER_Y && mouseY < gy + MinerMenu.FILTER_Y + 18
                && menu.getCarried().isEmpty() && !menu.getSlot(1).hasItem()) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("screen.avoidminer.miner.filter_card"), mouseX, mouseY);
            return;
        }

        if (mouseX >= gx + MinerMenu.UPGRADE_X && mouseX < gx + MinerMenu.UPGRADE_X + 18
                && mouseY >= gy + MinerMenu.UPGRADE_Y && mouseY < gy + MinerMenu.UPGRADE_Y + 18
                && menu.getCarried().isEmpty() && !menu.getSlot(2).hasItem()) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("screen.avoidminer.miner.upgrade"), mouseX, mouseY);
            return;
        }

        if (mouseX >= gx + MinerMenu.SPEED_UPGRADE_X && mouseX < gx + MinerMenu.SPEED_UPGRADE_X + 18
                && mouseY >= gy + MinerMenu.SPEED_UPGRADE_Y && mouseY < gy + MinerMenu.SPEED_UPGRADE_Y + 18
                && menu.getCarried().isEmpty() && !menu.getSlot(4).hasItem()) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("screen.avoidminer.miner.speed_upgrade"), mouseX, mouseY);
            return;
        }

        if (mouseX >= gx + MinerMenu.ENERGY_UPGRADE_X && mouseX < gx + MinerMenu.ENERGY_UPGRADE_X + 18
                && mouseY >= gy + MinerMenu.ENERGY_UPGRADE_Y && mouseY < gy + MinerMenu.ENERGY_UPGRADE_Y + 18
                && menu.getCarried().isEmpty() && !menu.getSlot(5).hasItem()) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("screen.avoidminer.miner.energy_upgrade"), mouseX, mouseY);
            return;
        }

        if (mouseX >= gx + MinerMenu.FUEL_X && mouseX < gx + MinerMenu.FUEL_X + 18
                && mouseY >= gy + MinerMenu.FUEL_Y && mouseY < gy + MinerMenu.FUEL_Y + 18
                && menu.getCarried().isEmpty() && !menu.getSlot(3).hasItem()) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("screen.avoidminer.miner.fuel"), mouseX, mouseY);
            return;
        }

        for (int i = 0; i < filterDisplay.size() && i < MinerMenu.FILTER_MAX_DISPLAY; i++) {
            int cx = gx + MinerMenu.FILTER_GRID_X + (i % MinerMenu.FILTER_GRID_COLS) * MinerMenu.SLOT_SIZE;
            int cy = gy + MinerMenu.FILTER_GRID_Y + (i / MinerMenu.FILTER_GRID_COLS) * MinerMenu.SLOT_SIZE;
            if (mouseX >= cx && mouseX < cx + 18 && mouseY >= cy && mouseY < cy + 18) {
                ItemStack stack = filterDisplay.get(i);
                if (!stack.isEmpty()) {
                    extractor.setTooltipForNextFrame(
                            stack.getHoverName(), mouseX, mouseY);
                }
                return;
            }
        }
    }
}