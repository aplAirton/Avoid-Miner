package com.airton.avoidminer.screen;

import com.airton.avoidminer.item.FilterCardItem;
import com.airton.avoidminer.menu.FilterCardMenu;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterCardScreen extends AbstractContainerScreen<FilterCardMenu> {
    private static final int PANEL_FILL = 0xFFC6C6C6;
    private static final int PANEL_BORDER = 0xFF555555;
    private static final int SLOT_INNER = 0xFF8B8B8B;
    private static final int SLOT_TOP_LEFT = 0xFF373737;
    private static final int SLOT_BOTTOM_RIGHT = 0xFFFFFFFF;
    private static final int TEXT_DARK = 0xFF404040;
    private static final int RESULTS_BG = 0xFFA0A0A0;
    private static final int HOVER = 0x44FFFF55;
    private static final int SEARCH_BG = 0xFF000000;
    private static final int SEARCH_TEXT = 0xFFE0E0E0;
    private static final int SEARCH_HINT = 0xFF707070;

    private EditBox searchBox;
    private List<Identifier> filterEntries = new ArrayList<>();
    private List<Block> searchResults = new ArrayList<>();
    private static List<SearchableBlock> allBlocks;

    public FilterCardScreen(FilterCardMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, FilterCardMenu.IMAGE_WIDTH, FilterCardMenu.IMAGE_HEIGHT);
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = FilterCardMenu.PLAYER_INV_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;
        searchBox = new EditBox(font, gx + FilterCardMenu.SEARCH_X, gy + FilterCardMenu.SEARCH_Y,
                FilterCardMenu.SEARCH_W, FilterCardMenu.SEARCH_H,
                Component.translatable("screen.avoidminer.filter_card.search"));
        searchBox.setHint(Component.translatable("screen.avoidminer.filter_card.search_hint"));
        searchBox.setResponder(this::onSearchChanged);
        addRenderableWidget(searchBox);
        refreshFilterEntries();
    }

    private void refreshFilterEntries() {
        filterEntries.clear();
        ItemStack filterCard = findFilterCard();
        if (!filterCard.isEmpty()) {
            filterEntries = FilterCardItem.getEntries(filterCard);
        }
    }

    private ItemStack findFilterCard() {
        if (minecraft.player == null) return ItemStack.EMPTY;
        for (var hand : net.minecraft.world.InteractionHand.values()) {
            ItemStack stack = minecraft.player.getItemInHand(hand);
            if (stack.getItem() instanceof FilterCardItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    private void onSearchChanged(String text) {
        searchResults.clear();
        if (text.isEmpty()) return;

        String query = text.toLowerCase(Locale.ROOT).trim();
        if (query.isEmpty()) return;

        List<SearchableBlock> blocks = getSearchableBlocks();
        for (SearchableBlock sb : blocks) {
            if (sb.name.contains(query) || sb.key.contains(query)) {
                Identifier id = BuiltInRegistries.BLOCK.getKey(sb.block);
                if (!filterEntries.contains(id)) {
                    searchResults.add(sb.block);
                    if (searchResults.size() >= FilterCardItem.MAX_ENTRIES) break;
                }
            }
        }
    }

    private static List<SearchableBlock> getSearchableBlocks() {
        if (allBlocks != null) return allBlocks;
        allBlocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block == Blocks.AIR) continue;
            ItemStack display = new ItemStack(block.asItem());
            if (display.isEmpty()) continue;
            String name = display.getHoverName().getString().toLowerCase(Locale.ROOT);
            String key = BuiltInRegistries.BLOCK.getKey(block).toString().toLowerCase(Locale.ROOT);
            allBlocks.add(new SearchableBlock(block, name, key));
        }
        allBlocks.sort((a, b) -> a.name.compareTo(b.name));
        return allBlocks;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;

        drawPanel(extractor, gx, gy);
        drawGridSlots(extractor, gx, gy);
        drawGridContents(extractor, gx, gy, mouseX, mouseY);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawPanel(GuiGraphicsExtractor extractor, int gx, int gy) {
        extractor.fill(gx, gy, gx + imageWidth, gy + imageHeight, PANEL_FILL);
        extractor.fill(gx, gy, gx + imageWidth, gy + 1, PANEL_BORDER);
        extractor.fill(gx, gy + imageHeight - 1, gx + imageWidth, gy + imageHeight, PANEL_BORDER);
        extractor.fill(gx, gy, gx + 1, gy + imageHeight, PANEL_BORDER);
        extractor.fill(gx + imageWidth - 1, gy, gx + imageWidth, gy + imageHeight, PANEL_BORDER);

        int sy = gy + FilterCardMenu.SEARCH_Y;
        extractor.fill(gx + FilterCardMenu.SEARCH_X, sy,
                gx + FilterCardMenu.SEARCH_X + FilterCardMenu.SEARCH_W,
                sy + FilterCardMenu.SEARCH_H, SEARCH_BG);
        extractor.fill(gx + FilterCardMenu.SEARCH_X, sy,
                gx + FilterCardMenu.SEARCH_X + FilterCardMenu.SEARCH_W, sy + 1, SLOT_BOTTOM_RIGHT);
        extractor.fill(gx + FilterCardMenu.SEARCH_X, sy + FilterCardMenu.SEARCH_H - 1,
                gx + FilterCardMenu.SEARCH_X + FilterCardMenu.SEARCH_W, sy + FilterCardMenu.SEARCH_H, SLOT_TOP_LEFT);
    }

    private void drawVanillaSlot(GuiGraphicsExtractor extractor, int sx, int sy) {
        extractor.fill(sx, sy, sx + 18, sy + 18, SLOT_INNER);
        extractor.fill(sx, sy, sx + 18, sy + 1, SLOT_TOP_LEFT);
        extractor.fill(sx, sy, sx + 1, sy + 18, SLOT_TOP_LEFT);
        extractor.fill(sx + 17, sy + 1, sx + 18, sy + 18, SLOT_BOTTOM_RIGHT);
        extractor.fill(sx + 1, sy + 17, sx + 18, sy + 18, SLOT_BOTTOM_RIGHT);
    }

    private void drawGridSlots(GuiGraphicsExtractor extractor, int gx, int gy) {
        for (int i = 0; i < FilterCardItem.MAX_ENTRIES; i++) {
            int col = i % FilterCardMenu.GRID_COLS;
            int row = i / FilterCardMenu.GRID_COLS;
            drawVanillaSlot(extractor,
                    gx + FilterCardMenu.GRID_X + col * FilterCardMenu.CELL_SIZE,
                    gy + FilterCardMenu.GRID_Y + row * FilterCardMenu.CELL_SIZE);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawVanillaSlot(extractor,
                        gx + FilterCardMenu.PLAYER_INV_X + col * 18,
                        gy + FilterCardMenu.PLAYER_INV_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawVanillaSlot(extractor,
                    gx + FilterCardMenu.PLAYER_INV_X + col * 18,
                    gy + FilterCardMenu.HOTBAR_Y);
        }
    }

    private void drawGridContents(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        String searchText = searchBox.getValue();
        if (searchText.isEmpty()) {
            Component filterLabel = Component.translatable("screen.avoidminer.filter_card.filter_label");
            int labelY = gy + FilterCardMenu.GRID_Y - 11;
            extractor.text(font, filterLabel, gx + FilterCardMenu.GRID_X, labelY, TEXT_DARK, false);

            if (filterEntries.isEmpty()) {
                Component empty = Component.translatable("tooltip.avoidminer.filter_card.empty");
                extractor.text(font, empty,
                        gx + FilterCardMenu.GRID_X + FilterCardMenu.GRID_COLS * FilterCardMenu.CELL_SIZE
                                - font.width(empty) - 2, labelY, 0xFF707070, false);
            }

            for (int i = 0; i < filterEntries.size() && i < FilterCardItem.MAX_ENTRIES; i++) {
                int col = i % FilterCardMenu.GRID_COLS;
                int row = i / FilterCardMenu.GRID_COLS;
                int cx = gx + FilterCardMenu.GRID_X + col * FilterCardMenu.CELL_SIZE;
                int cy = gy + FilterCardMenu.GRID_Y + row * FilterCardMenu.CELL_SIZE;
                var block = BuiltInRegistries.BLOCK.getValue(filterEntries.get(i));
                ItemStack icon = new ItemStack(block.asItem());
                if (!icon.isEmpty()) {
                    extractor.item(icon, cx + 1, cy + 1);
                }
                if (mouseX >= cx && mouseX < cx + 18 && mouseY >= cy && mouseY < cy + 18) {
                    extractor.fill(cx, cy, cx + 18, cy + 18, HOVER);
                }
            }
        } else {
            Component resultsLabel = Component.translatable("screen.avoidminer.filter_card.results_label");
            int labelY = gy + FilterCardMenu.GRID_Y - 11;
            extractor.text(font, resultsLabel, gx + FilterCardMenu.GRID_X, labelY, TEXT_DARK, false);

            if (searchResults.isEmpty()) {
                Component none = Component.translatable("screen.avoidminer.filter_card.no_results");
                extractor.text(font, none,
                        gx + FilterCardMenu.GRID_X + FilterCardMenu.GRID_COLS * FilterCardMenu.CELL_SIZE
                                - font.width(none) - 2, labelY, 0xFF707070, false);
            }

            for (int i = 0; i < searchResults.size() && i < FilterCardItem.MAX_ENTRIES; i++) {
                int col = i % FilterCardMenu.GRID_COLS;
                int row = i / FilterCardMenu.GRID_COLS;
                int cx = gx + FilterCardMenu.GRID_X + col * FilterCardMenu.CELL_SIZE;
                int cy = gy + FilterCardMenu.GRID_Y + row * FilterCardMenu.CELL_SIZE;
                ItemStack icon = new ItemStack(searchResults.get(i).asItem());
                if (!icon.isEmpty()) {
                    extractor.item(icon, cx + 1, cy + 1);
                }
                if (mouseX >= cx && mouseX < cx + 18 && mouseY >= cy && mouseY < cy + 18) {
                    extractor.fill(cx, cy, cx + 18, cy + 18, HOVER);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int gx = (width - imageWidth) / 2;
            int gy = (height - imageHeight) / 2;

            if (searchBox.isMouseOver(event.x(), event.y())) {
                return super.mouseClicked(event, doubleClick);
            }

            for (int i = 0; i < FilterCardItem.MAX_ENTRIES; i++) {
                int col = i % FilterCardMenu.GRID_COLS;
                int row = i / FilterCardMenu.GRID_COLS;
                int cx = gx + FilterCardMenu.GRID_X + col * FilterCardMenu.CELL_SIZE;
                int cy = gy + FilterCardMenu.GRID_Y + row * FilterCardMenu.CELL_SIZE;
                if (event.x() >= cx && event.x() < cx + 18
                        && event.y() >= cy && event.y() < cy + 18) {
                    String searchText = searchBox.getValue();
                    if (searchText.isEmpty()) {
                        if (i < filterEntries.size()) {
                            minecraft.gameMode.handleInventoryButtonClick(
                                    menu.containerId, FilterCardMenu.BUTTON_REMOVE_BASE + i);
                            filterEntries.remove(i);
                        }
                    } else {
                        if (i < searchResults.size()) {
                            int blockId = BuiltInRegistries.BLOCK.getId(searchResults.get(i));
                            Identifier addedId = BuiltInRegistries.BLOCK.getKey(searchResults.get(i));
                            minecraft.gameMode.handleInventoryButtonClick(
                                    menu.containerId, FilterCardMenu.BUTTON_ADD_BASE + blockId);
                            if (!filterEntries.contains(addedId)) {
                                filterEntries.add(addedId);
                            }
                            onSearchChanged(searchText);
                        }
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchBox.isFocused()) {
            if (searchBox.keyPressed(event)) {
                return true;
            }
            if (minecraft.options.keyInventory.getKey().getValue() == event.key()
                    && minecraft.options.keyInventory.getKey().getType() == InputConstants.Type.KEYSYM) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public void removed() {
        super.removed();
        refreshFilterEntries();
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;

        String searchText = searchBox.getValue();
        for (int i = 0; i < FilterCardItem.MAX_ENTRIES; i++) {
            int col = i % FilterCardMenu.GRID_COLS;
            int row = i / FilterCardMenu.GRID_COLS;
            int cx = gx + FilterCardMenu.GRID_X + col * FilterCardMenu.CELL_SIZE;
            int cy = gy + FilterCardMenu.GRID_Y + row * FilterCardMenu.CELL_SIZE;
            if (mouseX >= cx && mouseX < cx + 18 && mouseY >= cy && mouseY < cy + 18) {
                if (searchText.isEmpty()) {
                    if (i < filterEntries.size()) {
                        var block = BuiltInRegistries.BLOCK.getValue(filterEntries.get(i));
                        extractor.setTooltipForNextFrame(
                                new ItemStack(block.asItem()).getHoverName(), mouseX, mouseY);
                    }
                } else {
                    if (i < searchResults.size()) {
                        extractor.setTooltipForNextFrame(
                                new ItemStack(searchResults.get(i).asItem()).getHoverName(), mouseX, mouseY);
                    }
                }
                return;
            }
        }
    }

    private record SearchableBlock(Block block, String name, String key) {}
}
