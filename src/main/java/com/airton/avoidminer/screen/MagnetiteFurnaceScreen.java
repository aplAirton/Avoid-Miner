package com.airton.avoidminer.screen;

import com.airton.avoidminer.block.entity.MagnetiteFurnaceBlockEntity.Tier;
import com.airton.avoidminer.block.entity.MagnetiteFurnaceRules;
import com.airton.avoidminer.menu.MagnetiteFurnaceMenu;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MagnetiteFurnaceScreen extends AbstractContainerScreen<MagnetiteFurnaceMenu> {
    private static final Identifier TEXTURE = Identifier.parse("avoidminer:textures/gui/avoid_processor.png");
    private static final int PANEL_X = 4;
    private static final int PANEL_INNER_W = 80;
    private static final int MAIN_LEFT = 92;
    private static final int MAIN_RIGHT = 258;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 70;
    private static final int GRID_COLS = 4;
    private static final int GRID_CELL = 18;

    private static final int SLOT_BORDER = 0xFF34383C;
    private static final int SLOT_BORDER_LIGHT = 0xFF666C70;
    private static final int SLOT_BORDER_DARK = 0xFF131518;
    private static final int SLOT_INNER = 0xFF282B2E;
    private static final int PANEL_DARK = 0xFF202326;
    private static final int ACCENT = 0xFFB7C2C8;
    private static final int ACCENT_DIM = 0xFF657079;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFC3C8CB;
    private static final int TEXT_DISABLED = 0xFF747A7E;

    private static final List<RecipeSample> RECIPE_SAMPLES = List.of(
            sample(Items.RAW_IRON, Items.IRON_INGOT),
            sample(Items.RAW_COPPER, Items.COPPER_INGOT),
            sample(Items.RAW_GOLD, Items.GOLD_INGOT),
            sample(Items.IRON_ORE, Items.IRON_INGOT),
            sample(Items.COPPER_ORE, Items.COPPER_INGOT),
            sample(Items.GOLD_ORE, Items.GOLD_INGOT),
            sample(Items.SAND, Items.GLASS),
            sample(Items.COBBLESTONE, Items.STONE),
            sample(Items.CLAY_BALL, Items.BRICK),
            sample(Items.CLAY, Items.TERRACOTTA),
            sample(Items.OAK_LOG, Items.CHARCOAL),
            sample(Items.CACTUS, Items.GREEN_DYE),
            sample(Items.KELP, Items.DRIED_KELP),
            sample(Items.NETHERRACK, Items.NETHER_BRICK),
            sample(Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP),
            sample(Items.WET_SPONGE, Items.SPONGE),
            sample(Items.BEEF, Items.COOKED_BEEF),
            sample(Items.PORKCHOP, Items.COOKED_PORKCHOP),
            sample(Items.CHICKEN, Items.COOKED_CHICKEN),
            sample(Items.MUTTON, Items.COOKED_MUTTON),
            sample(Items.POTATO, Items.BAKED_POTATO),
            sample(Items.CHORUS_FRUIT, Items.POPPED_CHORUS_FRUIT)
    );

    private int tickCounter;

    private record RecipeSample(ItemStack input, ItemStack output) {
    }

    private static RecipeSample sample(net.minecraft.world.level.ItemLike input,
                                       net.minecraft.world.level.ItemLike output) {
        return new RecipeSample(new ItemStack(input), new ItemStack(output));
    }

    public MagnetiteFurnaceScreen(MagnetiteFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, MagnetiteFurnaceMenu.TEXTURE_W, MagnetiteFurnaceMenu.TEXTURE_H);
        titleLabelX = 9999;
        titleLabelY = 9999;
        inventoryLabelX = MagnetiteFurnaceMenu.MAIN_X;
        inventoryLabelY = 111;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        tickCounter++;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0.0F, 0.0F, imageWidth, imageHeight, imageWidth, imageHeight);
        tintMachineSurface(extractor, x, y);
        drawTitle(extractor, x, y);
        drawProcessingArea(extractor, x, y, menu.getTier());
        drawUpgradeArea(extractor, x, y);
        drawEnergyBar(extractor, x, y);
        drawFuelGlow(extractor, x, y);
        drawAutoBalanceButton(extractor, x, y);
        drawInfoPanel(extractor, x, y, menu.getTier());
        drawRecipeSamples(extractor, x, y, mouseX, mouseY);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void tintMachineSurface(GuiGraphicsExtractor extractor, int x, int y) {
        extractor.fill(x + MAIN_LEFT, y + 4, x + MAIN_RIGHT, y + 13, PANEL_DARK);
        extractor.fill(x + 4, y + 4, x + 84, y + 62, 0xCC202326);
        extractor.fill(x + 4, y + 66, x + 84, y + 198, 0xB8202326);
    }

    private void drawTitle(GuiGraphicsExtractor extractor, int x, int y) {
        int left = x + MAIN_LEFT;
        int right = x + MAIN_RIGHT;
        extractor.fill(left, y + 4, left + 2, y + 13, ACCENT);
        Component label = Component.translatable("screen.avoidminer.smelting");
        extractor.text(font, label,
                left + 2 + (right - left - 2 - font.width(label)) / 2, y + 5, TEXT_WHITE);
    }

    private void drawProcessingArea(GuiGraphicsExtractor extractor, int x, int y, Tier tier) {
        if (tier.inputCount == 1) {
            drawSlot(extractor, x + MagnetiteFurnaceMenu.T1_INPUT_X,
                    y + MagnetiteFurnaceMenu.T1_ROW_Y, menu.getInputProgress(0) > 0);
            drawSlot(extractor, x + MagnetiteFurnaceMenu.T1_OUTPUT_X,
                    y + MagnetiteFurnaceMenu.T1_ROW_Y, false);
            drawHorizontalProgress(extractor,
                    x + MagnetiteFurnaceMenu.T1_ARROW_X,
                    y + MagnetiteFurnaceMenu.T1_ROW_Y
                            + (18 - MagnetiteFurnaceMenu.T1_ARROW_ROW_H) / 2,
                    menu.getInputProgress(0), menu.getMaxProgress());
            return;
        }

        for (int i = 0; i < tier.inputCount; i++) {
            int slotX = x + MagnetiteFurnaceMenu.inputSlotX(tier, i);
            drawSlot(extractor, slotX, y + MagnetiteFurnaceMenu.INPUT_Y,
                    menu.getInputProgress(i) > 0);
            drawVerticalProgress(extractor, slotX, y + MagnetiteFurnaceMenu.ARROW_Y,
                    menu.getInputProgress(i), menu.getMaxProgress(), i);
            drawSlot(extractor, slotX, y + MagnetiteFurnaceMenu.OUTPUT_Y, false);
        }
    }

    private void drawUpgradeArea(GuiGraphicsExtractor extractor, int x, int y) {
        drawUpgradeSlot(extractor, x + MagnetiteFurnaceMenu.UPG_ENERGY_X,
                y + MagnetiteFurnaceMenu.UPG_Y, "screen.avoidminer.slot.letter.energy",
                menu.getEnergyUpgradeTier() > 0);
        drawUpgradeSlot(extractor, x + MagnetiteFurnaceMenu.UPG_SPEED_X,
                y + MagnetiteFurnaceMenu.UPG_Y, "screen.avoidminer.slot.letter.speed",
                menu.getSpeedUpgradeTier() > 0);
    }

    private void drawUpgradeSlot(GuiGraphicsExtractor extractor, int x, int y,
                                 String letterKey, boolean filled) {
        drawSlot(extractor, x, y, false);
        int frame = filled ? ACCENT : ACCENT_DIM;
        extractor.fill(x - 1, y - 1, x + 18, y, frame);
        extractor.fill(x - 1, y + 17, x + 18, y + 18, frame);
        extractor.fill(x - 1, y, x, y + 17, frame);
        extractor.fill(x + 17, y, x + 18, y + 17, frame);
        if (!filled) {
            Component letter = Component.translatable(letterKey);
            extractor.text(font, letter, x + 9 - font.width(letter) / 2, y + 5, TEXT_DISABLED);
        }
    }

    private void drawSlot(GuiGraphicsExtractor extractor, int x, int y, boolean active) {
        extractor.fill(x - 1, y - 1, x + 18, y + 18, SLOT_BORDER);
        extractor.fill(x, y, x + 17, y + 17, SLOT_INNER);
        extractor.fill(x, y, x + 17, y + 1, SLOT_BORDER_DARK);
        extractor.fill(x, y, x + 1, y + 17, SLOT_BORDER_DARK);
        extractor.fill(x + 17, y + 1, x + 18, y + 17, SLOT_BORDER_LIGHT);
        extractor.fill(x + 1, y + 17, x + 17, y + 18, SLOT_BORDER_LIGHT);
        if (active) {
            int alpha = (int) (42 + 28 * Math.sin(tickCounter * 0.12));
            extractor.fill(x, y, x + 17, y + 17, (alpha << 24) | 0x00D34A36);
        }
    }

    private void drawHorizontalProgress(GuiGraphicsExtractor extractor, int x, int y,
                                        int progress, int maximum) {
        int width = MagnetiteFurnaceMenu.T1_ARROW_W;
        int height = MagnetiteFurnaceMenu.T1_ARROW_ROW_H;
        extractor.fill(x, y, x + width, y + height, SLOT_BORDER_DARK);
        if (maximum > 0 && progress > 0) {
            int fill = Math.clamp((int) ((long) progress * width / maximum), 1, width);
            extractor.fill(x, y + 2, x + fill, y + height - 2, ACCENT);
            int shimmer = tickCounter * 2 % width;
            if (shimmer < fill) extractor.fill(x + shimmer, y + 2, x + shimmer + 2,
                    y + height - 2, 0x66FFFFFF);
        } else {
            extractor.fill(x + 4, y + height / 2 - 1, x + width - 6,
                    y + height / 2 + 1, ACCENT_DIM);
        }
    }

    private void drawVerticalProgress(GuiGraphicsExtractor extractor, int x, int y,
                                      int progress, int maximum, int index) {
        int height = MagnetiteFurnaceMenu.ARROW_H;
        extractor.fill(x, y, x + 18, y + height, SLOT_BORDER_DARK);
        if (maximum > 0 && progress > 0) {
            int fill = Math.clamp((int) ((long) progress * height / maximum), 1, height);
            extractor.fill(x + 5, y, x + 13, y + fill, ACCENT);
            if ((tickCounter + index * 4) % 8 < 3 && fill > 4) {
                extractor.fill(x + 5, y + fill - 2, x + 13, y + fill - 1, 0x88FFFFFF);
            }
        } else {
            extractor.fill(x + 7, y + 3, x + 11, y + 8, ACCENT_DIM);
            extractor.fill(x + 5, y + 8, x + 13, y + 10, ACCENT_DIM);
            extractor.fill(x + 7, y + 10, x + 11, y + 12, ACCENT_DIM);
        }
    }

    private void drawEnergyBar(GuiGraphicsExtractor extractor, int x, int y) {
        int barX = x + MagnetiteFurnaceMenu.ENERGY_X + 2;
        int barY = y + MagnetiteFurnaceMenu.ENERGY_Y + 2;
        int barHeight = MagnetiteFurnaceMenu.ENERGY_H - 4;
        int energy = menu.getEnergyStored();
        int capacity = menu.getEnergyCapacity();
        if (capacity <= 0 || energy <= 0) return;
        int fill = Math.clamp((int) ((long) energy * barHeight / capacity), 1, barHeight);
        int top = barY + barHeight - fill;
        extractor.fill(barX, top, barX + MagnetiteFurnaceMenu.ENERGY_W - 4,
                barY + barHeight, ACCENT);
        extractor.fill(barX, top, barX + 1, top + Math.min(fill, 2), TEXT_WHITE);
    }

    private void drawFuelGlow(GuiGraphicsExtractor extractor, int x, int y) {
        if (menu.isBurning()) {
            int slotX = x + MagnetiteFurnaceMenu.FUEL_X;
            int slotY = y + MagnetiteFurnaceMenu.FUEL_Y;
            extractor.fill(slotX - 1, slotY - 1, slotX + 18, slotY + 18, 0x44B7C2C8);
        }
    }

    private void drawAutoBalanceButton(GuiGraphicsExtractor extractor, int gx, int gy) {
        if (menu.getTier().inputCount <= 1) return;
        int x = gx + MagnetiteFurnaceMenu.AUTO_BALANCE_X;
        int y = gy + MagnetiteFurnaceMenu.AUTO_BALANCE_Y;
        int size = MagnetiteFurnaceMenu.AUTO_BALANCE_SIZE;
        boolean enabled = menu.isAutoBalanceEnabled();
        int frame = enabled ? ACCENT : SLOT_BORDER_LIGHT;
        int inner = enabled ? 0xFF3A4044 : 0xFF1D2022;
        extractor.fill(x, y, x + size, y + size, frame);
        extractor.fill(x + 1, y + 1, x + size - 1, y + size - 1, inner);
        extractor.fill(x + 4, y + 4, x + 11, y + 6, frame);
        extractor.fill(x + 9, y + 3, x + 12, y + 7, frame);
        extractor.fill(x + 5, y + 10, x + 12, y + 12, frame);
        extractor.fill(x + 4, y + 9, x + 7, y + 13, frame);
    }

    private boolean isAutoBalanceButton(double mouseX, double mouseY) {
        if (menu.getTier().inputCount <= 1) return false;
        int x = (width - imageWidth) / 2 + MagnetiteFurnaceMenu.AUTO_BALANCE_X;
        int y = (height - imageHeight) / 2 + MagnetiteFurnaceMenu.AUTO_BALANCE_Y;
        return mouseX >= x && mouseX < x + MagnetiteFurnaceMenu.AUTO_BALANCE_SIZE
                && mouseY >= y && mouseY < y + MagnetiteFurnaceMenu.AUTO_BALANCE_SIZE;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && isAutoBalanceButton(event.x(), event.y()) && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId, MagnetiteFurnaceMenu.AUTO_BALANCE_BUTTON);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void drawInfoPanel(GuiGraphicsExtractor extractor, int x, int y, Tier tier) {
        int panelX = x + PANEL_X;
        Component title = Component.translatable("screen.avoidminer.magnetite_furnace.name");
        extractor.text(font, title, panelX + (PANEL_INNER_W - font.width(title)) / 2, y + 6, ACCENT);
        extractor.text(font, Component.translatable("screen.avoidminer.status"),
                panelX, y + 22, TEXT_DISABLED);

        String tierLabel = "T" + menu.getMachineTier();
        extractor.text(font, Component.literal(tierLabel),
                panelX + PANEL_INNER_W - font.width(tierLabel), y + 22, ACCENT);

        boolean blocked = menu.isOutputBlocked();
        boolean running = menu.isBurning();
        Component status = blocked ? Component.translatable("status.avoidminer.full")
                : Component.translatable(running ? "status.avoidminer.running" : "status.avoidminer.idle");
        extractor.text(font, status, panelX, y + 32,
                blocked ? 0xFFFF5555 : running ? 0xFF55FF55 : TEXT_DIM);

        int active = 0;
        for (int i = 0; i < tier.inputCount; i++) if (menu.getInputProgress(i) > 0) active++;
        String lines = active + "/" + tier.inputCount;
        extractor.text(font, Component.literal(lines),
                panelX + PANEL_INNER_W - font.width(lines), y + 32,
                active > 0 ? ACCENT : TEXT_DISABLED);

        extractor.text(font, Component.translatable("screen.avoidminer.cost"),
                panelX, y + 44, TEXT_DIM);
        Component cost = Component.translatable("screen.avoidminer.cost_per_item", energyCostPerItem());
        extractor.text(font, cost, panelX + PANEL_INNER_W - font.width(cost), y + 44, ACCENT);
    }

    private int energyCostPerItem() {
        return MagnetiteFurnaceRules.energyPerItem(
                menu.getBaseTicksPerProcess(), menu.getEnergyUpgradeTier());
    }

    private void drawRecipeSamples(GuiGraphicsExtractor extractor, int x, int y,
                                   int mouseX, int mouseY) {
        int hovered = hoveredRecipe(mouseX - x, mouseY - y);
        for (int i = 0; i < RECIPE_SAMPLES.size(); i++) {
            int cellX = x + GRID_X + i % GRID_COLS * GRID_CELL;
            int cellY = y + GRID_Y + i / GRID_COLS * GRID_CELL;
            if (i == hovered) extractor.fill(cellX - 1, cellY - 1, cellX + 17, cellY + 17, 0x33FFFFFF);
            extractor.item(RECIPE_SAMPLES.get(i).input(), cellX, cellY);
        }
    }

    private int hoveredRecipe(int relativeX, int relativeY) {
        int column = Math.floorDiv(relativeX - GRID_X, GRID_CELL);
        int row = Math.floorDiv(relativeY - GRID_Y, GRID_CELL);
        if (column < 0 || column >= GRID_COLS || row < 0) return -1;
        int index = row * GRID_COLS + column;
        if (index >= RECIPE_SAMPLES.size()) return -1;
        int insideX = relativeX - GRID_X - column * GRID_CELL;
        int insideY = relativeY - GRID_Y - row * GRID_CELL;
        return insideX < 17 && insideY < 17 ? index : -1;
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int relativeX = mouseX - x;
        int relativeY = mouseY - y;

        if (isAutoBalanceButton(mouseX, mouseY)) {
            extractor.setTooltipForNextFrame(Component.translatable(
                    menu.isAutoBalanceEnabled()
                            ? "tooltip.avoidminer.auto_balance.on"
                            : "tooltip.avoidminer.auto_balance.off"), mouseX, mouseY);
            return;
        }

        if (relativeX >= MagnetiteFurnaceMenu.ENERGY_X
                && relativeX < MagnetiteFurnaceMenu.ENERGY_X + MagnetiteFurnaceMenu.ENERGY_W
                && relativeY >= MagnetiteFurnaceMenu.ENERGY_Y
                && relativeY < MagnetiteFurnaceMenu.ENERGY_Y + MagnetiteFurnaceMenu.ENERGY_H) {
            extractor.setTooltipForNextFrame(Component.translatable("tooltip.avoidminer.energy",
                    menu.getEnergyStored(), menu.getEnergyCapacity()), mouseX, mouseY);
            return;
        }

        int recipe = hoveredRecipe(relativeX, relativeY);
        if (recipe >= 0) {
            RecipeSample sample = RECIPE_SAMPLES.get(recipe);
            extractor.setTooltipForNextFrame(font, List.of(
                    sample.input().getHoverName(),
                    Component.translatable("screen.avoidminer.recipe.gives", 1, sample.output().getHoverName())
            ), Optional.empty(), ItemStack.EMPTY, mouseX, mouseY);
            return;
        }

        if (relativeY >= MagnetiteFurnaceMenu.UPG_Y - 1
                && relativeY < MagnetiteFurnaceMenu.UPG_Y + 18) {
            if (relativeX >= MagnetiteFurnaceMenu.UPG_ENERGY_X - 1
                    && relativeX < MagnetiteFurnaceMenu.UPG_ENERGY_X + 18
                    && menu.getEnergyUpgradeTier() == 0) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.energy_only"), mouseX, mouseY);
            } else if (relativeX >= MagnetiteFurnaceMenu.UPG_SPEED_X - 1
                    && relativeX < MagnetiteFurnaceMenu.UPG_SPEED_X + 18
                    && menu.getSpeedUpgradeTier() == 0) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.speed_only"), mouseX, mouseY);
            }
        }
    }
}
