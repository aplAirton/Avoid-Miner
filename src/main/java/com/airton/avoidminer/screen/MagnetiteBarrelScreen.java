package com.airton.avoidminer.screen;

import com.airton.avoidminer.menu.MagnetiteBarrelMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

/**
 * GUI do Barril de Magnetita: grade de 81 slots na área principal e painel
 * lateral com o cofre de experiência — pontos acumulados, botões "+100" e
 * "TUDO" para resgatar, contador de ocupação e os dois slots de melhoria
 * (Absorção de XP e Empilhamento).
 */
public class MagnetiteBarrelScreen extends AbstractContainerScreen<MagnetiteBarrelMenu> {
    private static final Identifier TEXTURE = Identifier.parse("avoidminer:textures/gui/magnetite_barrel.png");

    private static final int TEXTURE_W = MagnetiteBarrelMenu.TEXTURE_W;
    private static final int TEXTURE_H = MagnetiteBarrelMenu.TEXTURE_H;

    private static final int PANEL_X = 4;
    private static final int PANEL_INNER_W = 80;

    private static final int ACCENT = 0xFF7EE64E;        // verde XP
    private static final int ACCENT_DIM = 0xFF3E7A2A;
    private static final int UPGRADE_ACCENT = 0xFF55C8C0;
    private static final int UPGRADE_DIM = 0xFF2A6660;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFA9C89B;
    private static final int TEXT_DISABLED = 0xFF4E6644;

    private int tickCounter = 0;

    public MagnetiteBarrelScreen(MagnetiteBarrelMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE_W, TEXTURE_H);
        this.titleLabelX = 9999;
        this.titleLabelY = 9999;
        this.inventoryLabelX = MagnetiteBarrelMenu.MAIN_X;
        this.inventoryLabelY = 183;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        tickCounter++;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0f, 0f, TEXTURE_W, TEXTURE_H, TEXTURE_W, TEXTURE_H);

        drawInfoPanel(extractor, x, y, mouseX, mouseY);
        drawUpgradeSlots(extractor, x, y);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        int px = gx + PANEL_X;

        Component title = Component.translatable("screen.avoidminer.barrel.name");
        extractor.text(font, title,
                px + (PANEL_INNER_W - font.width(title)) / 2, gy + 6, ACCENT);

        // Cofre de experiência
        extractor.text(font, Component.translatable("screen.avoidminer.barrel.xp"), px, gy + 24, TEXT_DISABLED);
        int xp = menu.getStoredXp();
        String points = String.format(Locale.ROOT, "%,d", xp);
        int glow = xp > 0 && menu.hasXpUpgrade() && (tickCounter / 16) % 2 == 0 ? 0xFFA8FF7A : ACCENT;
        extractor.text(font, Component.literal(points),
                px + (PANEL_INNER_W - font.width(points)) / 2, gy + 36, xp > 0 ? glow : TEXT_DISABLED);

        drawButton(extractor, gx, gy, MagnetiteBarrelMenu.BTN_100_Y,
                Component.translatable("screen.avoidminer.barrel.btn_100"), xp > 0, mouseX, mouseY);
        drawButton(extractor, gx, gy, MagnetiteBarrelMenu.BTN_ALL_Y,
                Component.translatable("screen.avoidminer.barrel.btn_all"), xp > 0, mouseX, mouseY);

        // Ocupação do armazenamento
        extractor.text(font, Component.translatable("screen.avoidminer.status"), px, gy + 96, TEXT_DISABLED);
        String used = menu.getUsedSlots() + "/81";
        int usedColor = menu.getUsedSlots() >= 81 ? 0xFFFF5555 : TEXT_DIM;
        extractor.text(font, Component.literal(used),
                px + PANEL_INNER_W - font.width(used), gy + 96, usedColor);

        // O que está sendo absorvido (cada absorção exige sua melhoria)
        boolean items = menu.hasItemUpgrade();
        boolean xpOn = menu.hasXpUpgrade();
        String absorbKey = items && xpOn ? "screen.avoidminer.barrel.absorb_both"
                : items ? "screen.avoidminer.barrel.absorb_items"
                : xpOn ? "screen.avoidminer.barrel.absorbing"
                : "screen.avoidminer.barrel.absorb_none";
        extractor.text(font, Component.translatable(absorbKey), px, gy + 106,
                items || xpOn ? ACCENT : TEXT_DISABLED);
    }

    private void drawButton(GuiGraphicsExtractor extractor, int gx, int gy, int btnY,
                            Component label, boolean enabled, int mouseX, int mouseY) {
        int x0 = gx + MagnetiteBarrelMenu.BTN_X;
        int y0 = gy + btnY;
        int x1 = x0 + MagnetiteBarrelMenu.BTN_W;
        int y1 = y0 + MagnetiteBarrelMenu.BTN_H;
        boolean hovered = enabled && mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;

        int fill = !enabled ? 0xFF17220F : hovered ? 0xFF2E5220 : 0xFF223A17;
        int frame = !enabled ? 0xFF2A3A20 : hovered ? ACCENT : ACCENT_DIM;
        extractor.fill(x0, y0, x1, y1, fill);
        extractor.fill(x0, y0, x1, y0 + 1, frame);
        extractor.fill(x0, y1 - 1, x1, y1, frame);
        extractor.fill(x0, y0, x0 + 1, y1, frame);
        extractor.fill(x1 - 1, y0, x1, y1, frame);

        int labelColor = !enabled ? TEXT_DISABLED : hovered ? TEXT_WHITE : TEXT_DIM;
        extractor.text(font, label,
                x0 + (MagnetiteBarrelMenu.BTN_W - font.width(label)) / 2,
                y0 + (MagnetiteBarrelMenu.BTN_H - 8) / 2 + 1, labelColor);
    }

    private void drawUpgradeSlots(GuiGraphicsExtractor extractor, int gx, int gy) {
        drawUpgradeSlot(extractor, gx + MagnetiteBarrelMenu.UPG_ITEM_X, gy + MagnetiteBarrelMenu.UPG_Y,
                menu.hasItemUpgrade(), "screen.avoidminer.slot.letter.item");
        drawUpgradeSlot(extractor, gx + MagnetiteBarrelMenu.UPG_XP_X, gy + MagnetiteBarrelMenu.UPG_Y,
                menu.hasXpUpgrade(), "screen.avoidminer.slot.letter.xp");
        drawUpgradeSlot(extractor, gx + MagnetiteBarrelMenu.UPG_STACK_X, gy + MagnetiteBarrelMenu.UPG_Y,
                menu.hasStackUpgrade(), "screen.avoidminer.slot.letter.stack");
    }

    private void drawUpgradeSlot(GuiGraphicsExtractor extractor, int sx, int sy, boolean filled, String letterKey) {
        int frame = filled ? UPGRADE_ACCENT : UPGRADE_DIM;
        extractor.fill(sx - 1, sy - 1, sx + 18, sy, frame);
        extractor.fill(sx - 1, sy + 17, sx + 18, sy + 18, frame);
        extractor.fill(sx - 1, sy, sx, sy + 17, frame);
        extractor.fill(sx + 17, sy, sx + 18, sy + 17, frame);
        if (!filled) {
            Component letter = Component.translatable(letterKey);
            int lw = font.width(letter);
            extractor.text(font, letter, sx + 9 - lw / 2, sy + 5, TEXT_DISABLED);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;
        double mx = event.x();
        double my = event.y();

        if (menu.getStoredXp() > 0 && minecraft != null && minecraft.gameMode != null) {
            if (isOverButton(gx, gy, MagnetiteBarrelMenu.BTN_100_Y, mx, my)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MagnetiteBarrelMenu.BUTTON_XP_100);
                return true;
            }
            if (isOverButton(gx, gy, MagnetiteBarrelMenu.BTN_ALL_Y, mx, my)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MagnetiteBarrelMenu.BUTTON_XP_ALL);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private static boolean isOverButton(int gx, int gy, int btnY, double mx, double my) {
        int x0 = gx + MagnetiteBarrelMenu.BTN_X;
        int y0 = gy + btnY;
        return mx >= x0 && mx < x0 + MagnetiteBarrelMenu.BTN_W
                && my >= y0 && my < y0 + MagnetiteBarrelMenu.BTN_H;
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);

        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;
        int relX = mouseX - gx;
        int relY = mouseY - gy;

        if (relY >= MagnetiteBarrelMenu.UPG_Y - 1 && relY < MagnetiteBarrelMenu.UPG_Y + 18) {
            if (relX >= MagnetiteBarrelMenu.UPG_ITEM_X - 1 && relX < MagnetiteBarrelMenu.UPG_ITEM_X + 18
                    && !menu.hasItemUpgrade()) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.item_only"), mouseX, mouseY);
                return;
            }
            if (relX >= MagnetiteBarrelMenu.UPG_XP_X - 1 && relX < MagnetiteBarrelMenu.UPG_XP_X + 18
                    && !menu.hasXpUpgrade()) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.xp_only"), mouseX, mouseY);
                return;
            }
            if (relX >= MagnetiteBarrelMenu.UPG_STACK_X - 1 && relX < MagnetiteBarrelMenu.UPG_STACK_X + 18
                    && !menu.hasStackUpgrade()) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.stack_only"), mouseX, mouseY);
                return;
            }
        }

        if (isOverButton(gx, gy, MagnetiteBarrelMenu.BTN_100_Y, mouseX, mouseY)) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("tooltip.avoidminer.barrel.btn_100"), mouseX, mouseY);
        } else if (isOverButton(gx, gy, MagnetiteBarrelMenu.BTN_ALL_Y, mouseX, mouseY)) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("tooltip.avoidminer.barrel.btn_all"), mouseX, mouseY);
        }
    }
}
