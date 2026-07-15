package com.airton.avoidminer.screen;

import com.airton.avoidminer.menu.XpVaultMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

public final class XpVaultScreen extends AbstractContainerScreen<XpVaultMenu> {
    private static final int PANEL = 0xF20B100D;
    private static final int PANEL_INNER = 0xFF131B15;
    private static final int FRAME = 0xFF36523B;
    private static final int FRAME_BRIGHT = 0xFF79D13B;
    private static final int XP_GREEN = 0xFF80E83E;
    private static final int XP_GLOW = 0xFFB8FF72;
    private static final int GOLD = 0xFFE2B84F;
    private static final int TEXT = 0xFFE8F2E5;
    private static final int TEXT_DIM = 0xFF9AAE9C;
    private static final int DISABLED = 0xFF4A594C;

    private static final ButtonDef[] BUTTONS = {
            new ButtonDef(XpVaultMenu.BUTTON_GET_1, 14, 94, 68, 25, "+1", "tooltip.avoidminer.xp_vault.get_1"),
            new ButtonDef(XpVaultMenu.BUTTON_GET_5, 84, 94, 68, 25, "+5", "tooltip.avoidminer.xp_vault.get_5"),
            new ButtonDef(XpVaultMenu.BUTTON_GET_10, 154, 94, 68, 25, "+10", "tooltip.avoidminer.xp_vault.get_10"),
            new ButtonDef(XpVaultMenu.BUTTON_STORE_1, 14, 139, 68, 25, "-1", "tooltip.avoidminer.xp_vault.store_1"),
            new ButtonDef(XpVaultMenu.BUTTON_STORE_5, 84, 139, 68, 25, "-5", "tooltip.avoidminer.xp_vault.store_5"),
            new ButtonDef(XpVaultMenu.BUTTON_STORE_10, 154, 139, 68, 25, "-10", "tooltip.avoidminer.xp_vault.store_10"),
            new ButtonDef(XpVaultMenu.BUTTON_STORE_ALL, 14, 178, 100, 24, null, "tooltip.avoidminer.xp_vault.store_all"),
            new ButtonDef(XpVaultMenu.BUTTON_GET_ALL, 122, 178, 100, 24, null, "tooltip.avoidminer.xp_vault.get_all")
    };

    private int animationTick;

    public XpVaultScreen(XpVaultMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, XpVaultMenu.IMAGE_WIDTH, XpVaultMenu.IMAGE_HEIGHT);
        titleLabelX = 9999;
        titleLabelY = 9999;
        inventoryLabelX = 9999;
        inventoryLabelY = 9999;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;
        animationTick++;

        drawBackground(extractor, gx, gy);
        drawHeader(extractor, gx, gy);
        drawStoredLevels(extractor, gx, gy);
        drawSections(extractor, gx, gy, mouseX, mouseY);
        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawBackground(GuiGraphicsExtractor extractor, int gx, int gy) {
        extractor.fill(gx, gy, gx + imageWidth, gy + imageHeight, PANEL);
        drawFrame(extractor, gx, gy, imageWidth, imageHeight, FRAME);
        extractor.fill(gx + 4, gy + 4, gx + 7, gy + imageHeight - 4, XP_GREEN);
        extractor.fill(gx + imageWidth - 7, gy + 4, gx + imageWidth - 4, gy + imageHeight - 4, GOLD);

        int pulse = (animationTick / 12) % 3;
        for (int i = 0; i < 3; i++) {
            int color = i == pulse ? XP_GLOW : FRAME;
            extractor.fill(gx + 19 + i * 7, gy + 15, gx + 23 + i * 7, gy + 19, color);
            extractor.fill(gx + imageWidth - 41 + i * 7, gy + 15,
                    gx + imageWidth - 37 + i * 7, gy + 19, color);
        }
    }

    private void drawHeader(GuiGraphicsExtractor extractor, int gx, int gy) {
        Component title = Component.translatable("screen.avoidminer.xp_vault.title");
        extractor.text(font, title, gx + (imageWidth - font.width(title)) / 2, gy + 11, TEXT);
    }

    private void drawStoredLevels(GuiGraphicsExtractor extractor, int gx, int gy) {
        int x = gx + 14;
        int y = gy + 30;
        int w = imageWidth - 28;
        int h = 44;
        extractor.fill(x, y, x + w, y + h, PANEL_INNER);
        drawFrame(extractor, x, y, w, h, FRAME_BRIGHT);
        extractor.item(new ItemStack(Items.EXPERIENCE_BOTTLE), x + 10, y + 14);

        Component label = Component.translatable("screen.avoidminer.xp_vault.stored");
        extractor.text(font, label, x + 34, y + 8, TEXT_DIM);

        String levels = String.format(Locale.ROOT, "%,d", menu.getStoredLevels());
        int color = menu.getStoredLevels() > 0 && (animationTick / 18) % 2 == 0 ? XP_GLOW : XP_GREEN;
        extractor.text(font, Component.literal(levels),
                x + 34 + (w - 44 - font.width(levels)) / 2, y + 23, color);

        Component unit = Component.translatable("screen.avoidminer.xp_vault.levels");
        extractor.text(font, unit, x + w - font.width(unit) - 9, y + 23, GOLD);
    }

    private void drawSections(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        Component get = Component.translatable("screen.avoidminer.xp_vault.get_section");
        Component store = Component.translatable("screen.avoidminer.xp_vault.store_section");
        extractor.text(font, get, gx + 14, gy + 81, XP_GREEN);
        extractor.text(font, store, gx + 14, gy + 126, GOLD);

        for (ButtonDef button : BUTTONS) {
            Component label = button.label == null
                    ? Component.translatable(button.id == XpVaultMenu.BUTTON_STORE_ALL
                    ? "screen.avoidminer.xp_vault.store_all" : "screen.avoidminer.xp_vault.get_all")
                    : Component.literal(button.label);
            drawButton(extractor, gx, gy, button, label, isEnabled(button.id), mouseX, mouseY);
        }
    }

    private void drawButton(GuiGraphicsExtractor extractor, int gx, int gy, ButtonDef button,
                            Component label, boolean enabled, int mouseX, int mouseY) {
        int x = gx + button.x;
        int y = gy + button.y;
        boolean hovered = enabled && button.contains(gx, gy, mouseX, mouseY);
        boolean store = button.id >= XpVaultMenu.BUTTON_STORE_1
                && button.id <= XpVaultMenu.BUTTON_STORE_ALL;

        int fill = !enabled ? 0xFF111612 : hovered ? 0xFF293D27 : 0xFF1B281B;
        int frame = !enabled ? 0xFF303A31 : hovered ? (store ? GOLD : XP_GLOW) : (store ? 0xFF846D32 : FRAME_BRIGHT);
        extractor.fill(x, y, x + button.width, y + button.height, fill);
        drawFrame(extractor, x, y, button.width, button.height, frame);

        int color = !enabled ? DISABLED : hovered ? TEXT : store ? GOLD : XP_GREEN;
        extractor.text(font, label, x + (button.width - font.width(label)) / 2,
                y + (button.height - 8) / 2, color);
    }

    private boolean isEnabled(int buttonId) {
        if (buttonId == XpVaultMenu.BUTTON_GET_1 || buttonId == XpVaultMenu.BUTTON_GET_5
                || buttonId == XpVaultMenu.BUTTON_GET_10 || buttonId == XpVaultMenu.BUTTON_GET_ALL) {
            return menu.getStoredLevels() > 0;
        }
        return minecraft != null && minecraft.player != null && minecraft.player.experienceLevel > 0;
    }

    private static void drawFrame(GuiGraphicsExtractor extractor, int x, int y, int width, int height, int color) {
        extractor.fill(x, y, x + width, y + 1, color);
        extractor.fill(x, y + height - 1, x + width, y + height, color);
        extractor.fill(x, y, x + 1, y + height, color);
        extractor.fill(x + width - 1, y, x + width, y + height, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (minecraft != null && minecraft.gameMode != null) {
            int gx = (width - imageWidth) / 2;
            int gy = (height - imageHeight) / 2;
            for (ButtonDef button : BUTTONS) {
                if (isEnabled(button.id) && button.contains(gx, gy, event.x(), event.y())) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button.id);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;
        for (ButtonDef button : BUTTONS) {
            if (button.contains(gx, gy, mouseX, mouseY)) {
                extractor.setTooltipForNextFrame(Component.translatable(button.tooltipKey), mouseX, mouseY);
                return;
            }
        }
    }

    private record ButtonDef(int id, int x, int y, int width, int height,
                             String label, String tooltipKey) {
        private boolean contains(int gx, int gy, double mouseX, double mouseY) {
            int left = gx + x;
            int top = gy + y;
            return mouseX >= left && mouseX < left + width
                    && mouseY >= top && mouseY < top + height;
        }
    }
}
