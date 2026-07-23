package com.airton.avoidminer.screen;

import com.airton.avoidminer.item.RangeCardItem;
import com.airton.avoidminer.menu.RangeCardMenu;
import com.airton.avoidminer.network.RangeCardUpdatePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class RangeCardScreen extends AbstractContainerScreen<RangeCardMenu> {
    private static final int PANEL_FILL = 0xFFC6C6C6;
    private static final int PANEL_BORDER = 0xFF555555;
    private static final int SLOT_INNER = 0xFF8B8B8B;
    private static final int SLOT_TOP_LEFT = 0xFF373737;
    private static final int SLOT_BOTTOM_RIGHT = 0xFFFFFFFF;
    private static final int TEXT_DARK = 0xFF404040;
    private static final int INPUT_BG = 0xFF000000;
    private static final int BUTTON_BG = 0xFF8B8B8B;
    private static final int BUTTON_HOVER = 0xFFFFFFFF;
    private static final int SECTION_LINE = 0xFF555555;
    private static final int DIM_TEXT = 0xFF707070;

    private static final int LABEL_X = 12;
    private static final int INPUT_W = 42;
    private static final int INPUT_H = 14;
    private static final int INPUT_GAP = 6;

    private static final int POS1_Y = 36;
    private static final int POS1_INPUT_Y = 48;
    private static final int POS2_Y = 68;
    private static final int POS2_INPUT_Y = 80;

    private static final int SAVE_X = 48;
    private static final int CLR_X = 90;
    private static final int BTN_Y = 112;
    private static final int BTN_W = 38;
    private static final int BTN_H = 14;

    private EditBox x1Box, y1Box, z1Box, x2Box, y2Box, z2Box;

    public RangeCardScreen(RangeCardMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, RangeCardMenu.IMAGE_WIDTH, RangeCardMenu.IMAGE_HEIGHT);
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = RangeCardMenu.PLAYER_INV_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;

        int col1 = gx + LABEL_X + 22;
        int col2 = col1 + INPUT_W + INPUT_GAP;
        int col3 = col2 + INPUT_W + INPUT_GAP;

        x1Box = createInput(col1, gy + POS1_INPUT_Y);
        y1Box = createInput(col2, gy + POS1_INPUT_Y);
        z1Box = createInput(col3, gy + POS1_INPUT_Y);
        x2Box = createInput(col1, gy + POS2_INPUT_Y);
        y2Box = createInput(col2, gy + POS2_INPUT_Y);
        z2Box = createInput(col3, gy + POS2_INPUT_Y);

        loadCoords();
    }

    private EditBox createInput(int x, int y) {
        EditBox box = new EditBox(font, x, y, INPUT_W, INPUT_H, Component.empty());
        box.setFilter(s -> {
            if (s.isEmpty()) return true;
            char c = s.charAt(s.length() - 1);
            return Character.isDigit(c) || (c == '-' && s.length() == 1);
        });
        addRenderableWidget(box);
        return box;
    }

    private void loadCoords() {
        ItemStack stack = findRangeCard();
        if (stack.isEmpty()) return;
        x1Box.setValue(String.valueOf(RangeCardItem.getX1(stack)));
        y1Box.setValue(String.valueOf(RangeCardItem.getY1(stack)));
        z1Box.setValue(String.valueOf(RangeCardItem.getZ1(stack)));
        x2Box.setValue(String.valueOf(RangeCardItem.getX2(stack)));
        y2Box.setValue(String.valueOf(RangeCardItem.getY2(stack)));
        z2Box.setValue(String.valueOf(RangeCardItem.getZ2(stack)));
    }

    private ItemStack findRangeCard() {
        if (minecraft.player == null) return ItemStack.EMPTY;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = minecraft.player.getItemInHand(hand);
            if (stack.getItem() instanceof RangeCardItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    private int parseInt(EditBox box) {
        try {
            return Integer.parseInt(box.getValue());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void saveCoords() {
        int x1 = parseInt(x1Box), y1 = parseInt(y1Box), z1 = parseInt(z1Box);
        int x2 = parseInt(x2Box), y2 = parseInt(y2Box), z2 = parseInt(z2Box);
        int vol = RangeCardItem.getVolume(x1, y1, z1, x2, y2, z2);
        if (vol > RangeCardItem.MAX_VOLUME) return;
        ClientPacketDistributor.sendToServer(new RangeCardUpdatePayload(
                RangeCardUpdatePayload.SAVE, x1, y1, z1, x2, y2, z2));
    }

    private void clearCoords() {
        x1Box.setValue("0");
        y1Box.setValue("0");
        z1Box.setValue("0");
        x2Box.setValue("0");
        y2Box.setValue("0");
        z2Box.setValue("0");
        ClientPacketDistributor.sendToServer(new RangeCardUpdatePayload(
                RangeCardUpdatePayload.CLEAR, 0, 0, 0, 0, 0, 0));
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;

        drawPanel(extractor, gx, gy);
        drawSlots(extractor, gx, gy);
        drawSection(extractor, gx, gy, mouseX, mouseY);

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
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawVanillaSlot(extractor,
                        gx + RangeCardMenu.PLAYER_INV_X + col * 18,
                        gy + RangeCardMenu.PLAYER_INV_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawVanillaSlot(extractor,
                    gx + RangeCardMenu.PLAYER_INV_X + col * 18,
                    gy + RangeCardMenu.HOTBAR_Y);
        }
    }

    private void drawSection(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        Component pos1Label = Component.translatable("screen.avoidminer.range_card.pos1");
        extractor.text(font, pos1Label, gx + LABEL_X, gy + POS1_Y, TEXT_DARK, false);

        Component pos2Label = Component.translatable("screen.avoidminer.range_card.pos2");
        extractor.text(font, pos2Label, gx + LABEL_X, gy + POS2_Y, TEXT_DARK, false);

        Component lblX = Component.literal("X");
        Component lblY = Component.literal("Y");
        Component lblZ = Component.literal("Z");

        int col1 = gx + LABEL_X + 10;
        int col2 = col1 + INPUT_W + INPUT_GAP;
        int col3 = col2 + INPUT_W + INPUT_GAP;

        int lblY1 = gy + POS1_INPUT_Y + 4;
        int lblY2 = gy + POS2_INPUT_Y + 4;

        extractor.text(font, lblX, col1 - 10, lblY1, DIM_TEXT, false);
        extractor.text(font, lblY, col2 - 10, lblY1, DIM_TEXT, false);
        extractor.text(font, lblZ, col3 - 10, lblY1, DIM_TEXT, false);
        extractor.text(font, lblX, col1 - 10, lblY2, DIM_TEXT, false);
        extractor.text(font, lblY, col2 - 10, lblY2, DIM_TEXT, false);
        extractor.text(font, lblZ, col3 - 10, lblY2, DIM_TEXT, false);

        drawButton(extractor, gx + SAVE_X, gy + BTN_Y, BTN_W, BTN_H,
                Component.translatable("screen.avoidminer.range_card.save"),
                mouseX, mouseY, false);
        drawButton(extractor, gx + CLR_X, gy + BTN_Y, BTN_W, BTN_H,
                Component.translatable("screen.avoidminer.range_card.clear"),
                mouseX, mouseY, true);

        int vol = RangeCardItem.getVolume(
                parseInt(x1Box), parseInt(y1Box), parseInt(z1Box),
                parseInt(x2Box), parseInt(y2Box), parseInt(z2Box));
        int color = vol > RangeCardItem.MAX_VOLUME ? 0xFFFF5555 : TEXT_DARK;
        Component volText = Component.translatable("screen.avoidminer.range_card.volume", vol, RangeCardItem.MAX_VOLUME);
        extractor.text(font, volText, gx + LABEL_X, gy + POS2_Y + 30, color, false);
    }

    private void drawButton(GuiGraphicsExtractor extractor, int bx, int by, int bw, int bh,
                            Component label, int mouseX, int mouseY, boolean danger) {
        boolean hover = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;
        int fill = hover ? BUTTON_HOVER : BUTTON_BG;
        int border = danger ? 0xFFAA3333 : SLOT_TOP_LEFT;

        extractor.fill(bx, by, bx + bw, by + bh, fill);
        extractor.fill(bx, by, bx + bw, by + 1, border);
        extractor.fill(bx, by + bh - 1, bx + bw, by + bh, border);
        extractor.fill(bx, by, bx + 1, by + bh, border);
        extractor.fill(bx + bw - 1, by, bx + bw, by + bh, border);

        int textW = font.width(label);
        extractor.text(font, label, bx + (bw - textW) / 2, by + 3, TEXT_DARK, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int gx = (width - imageWidth) / 2;
            int gy = (height - imageHeight) / 2;

            if (isOverButton((int) event.x(), (int) event.y(), gx + SAVE_X, gy + BTN_Y, BTN_W, BTN_H)) {
                saveCoords();
                minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
                return true;
            }
            if (isOverButton((int) event.x(), (int) event.y(), gx + CLR_X, gy + BTN_Y, BTN_W, BTN_H)) {
                clearCoords();
                minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private static boolean isOverButton(int mx, int my, int bx, int by, int bw, int bh) {
        return mx >= bx && mx < bx + bw && my >= by && my < by + bh;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (x1Box.isFocused() || y1Box.isFocused() || z1Box.isFocused()
                || x2Box.isFocused() || y2Box.isFocused() || z2Box.isFocused()) {
            return super.keyPressed(event);
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;

        if (isOverButton(mouseX, mouseY, gx + SAVE_X, gy + BTN_Y, BTN_W, BTN_H)) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("screen.avoidminer.range_card.save_tooltip"), mouseX, mouseY);
        }
        if (isOverButton(mouseX, mouseY, gx + CLR_X, gy + BTN_Y, BTN_W, BTN_H)) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("screen.avoidminer.range_card.clear_tooltip"), mouseX, mouseY);
        }
    }
}
