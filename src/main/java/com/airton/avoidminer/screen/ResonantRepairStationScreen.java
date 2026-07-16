package com.airton.avoidminer.screen;

import com.airton.avoidminer.menu.ResonantRepairStationMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ResonantRepairStationScreen extends AbstractContainerScreen<ResonantRepairStationMenu> {
    public ResonantRepairStationScreen(ResonantRepairStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        titleLabelX = 8;
        titleLabelY = 7;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = (width - imageWidth) / 2;
        int top = (height - imageHeight) / 2;
        graphics.fill(left, top, left + imageWidth, top + imageHeight, 0xF20A1013);
        frame(graphics, left, top, imageWidth, imageHeight, 0xFF2FAEB8);
        graphics.fill(left + 4, top + 4, left + imageWidth - 4, top + 68, 0xFF121F24);

        slot(graphics, left + 43, top + 34, 0xFF7348A6);
        slot(graphics, left + 115, top + 34, 0xFF32C8CC);
        graphics.text(font, Component.literal(">>"), left + 78, top + 39, 0xFF78E9E5);

        int width = 54;
        int filled = width * menu.progress() / menu.maxProgress();
        graphics.fill(left + 61, top + 58, left + 61 + width, top + 62, 0xFF263940);
        graphics.fill(left + 61, top + 58, left + 61 + filled, top + 62, 0xFF48E0D9);
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    private static void slot(GuiGraphicsExtractor graphics, int x, int y, int color) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF090D10);
        frame(graphics, x, y, 18, 18, color);
    }

    private static void frame(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }
}
