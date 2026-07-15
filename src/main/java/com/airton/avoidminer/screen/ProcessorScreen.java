package com.airton.avoidminer.screen;

import com.airton.avoidminer.block.entity.ProcessorBlockEntity;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity.Tier;
import com.airton.avoidminer.menu.ProcessorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GUI do Processador de Minérios no padrão "câmara" da Lootr: painel lateral
 * com título, status e o catálogo de receitas — grade com todos os minérios
 * processáveis; o hover mostra o que cada um vira. Na área principal, as
 * colunas de processamento ganham brasa sob a entrada ativa.
 */
public class ProcessorScreen extends AbstractContainerScreen<ProcessorMenu> {
    private static final Identifier TEXTURE = Identifier.parse("avoidminer:textures/gui/avoid_processor.png");

    private static final int TEXTURE_W = ProcessorMenu.TEXTURE_W;
    private static final int TEXTURE_H = ProcessorMenu.TEXTURE_H;

    private static final int PANEL_X = 4;
    private static final int PANEL_INNER_W = 80;

    private static final int MAIN_LEFT = 92;
    private static final int MAIN_RIGHT = 258;

    private static final int ENERGY_X = ProcessorMenu.ENERGY_X;
    private static final int ENERGY_Y = ProcessorMenu.ENERGY_Y;
    private static final int ENERGY_W = ProcessorMenu.ENERGY_W;
    private static final int ENERGY_H = ProcessorMenu.ENERGY_H;
    private static final int FUEL_X = ProcessorMenu.FUEL_X;
    private static final int FUEL_Y = ProcessorMenu.FUEL_Y;

    // Catálogo de receitas: grade de 4 colunas na janela do painel
    private static final int GRID_X = 8;
    private static final int GRID_Y = 70;
    private static final int GRID_COLS = 4;
    private static final int GRID_CELL = 18;
    private static final int GRID_MAX = 28;

    private static final int SLOT_BORDER = 0xFF3A2A18;
    private static final int SLOT_BORDER_LT = 0xFF6A4E2A;
    private static final int SLOT_BORDER_DK = 0xFF160E06;
    private static final int SLOT_INNER = 0xFF4A3520;
    private static final int ACCENT = 0xFFE09650;
    private static final int ACCENT_DIM = 0xFF8A5426;

    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFC8B090;
    private static final int TEXT_DISABLED = 0xFF6B4F33;

    private int tickCounter = 0;

    private record RecipeSample(ItemStack input, ItemStack output) {}
    private List<RecipeSample> cachedRecipes;

    public ProcessorScreen(ProcessorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE_W, TEXTURE_H);
        this.titleLabelX = 9999;
        this.titleLabelY = 9999;
        this.inventoryLabelX = ProcessorMenu.MAIN_X;
        this.inventoryLabelY = 111;
    }

    private List<RecipeSample> recipeCatalog() {
        if (cachedRecipes == null) {
            cachedRecipes = new ArrayList<>();
            for (Map.Entry<Item, ItemStack> e : ProcessorBlockEntity.getRecipeMap().entrySet()) {
                cachedRecipes.add(new RecipeSample(new ItemStack(e.getKey()), e.getValue()));
            }
        }
        return cachedRecipes;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        tickCounter++;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0f, 0f, TEXTURE_W, TEXTURE_H, TEXTURE_W, TEXTURE_H);

        Tier t = menu.getTier();

        drawTitleBanner(extractor, x, y);
        drawProcessingArea(extractor, x, y, t);
        drawUpgradeArea(extractor, x, y);
        drawEnergyBar(extractor, x, y);
        drawFuelGlow(extractor, x, y);
        drawAutoBalanceButton(extractor, x, y);
        drawInfoPanel(extractor, x, y, t);
        drawRecipeChamber(extractor, x, y, mouseX, mouseY);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawTitleBanner(GuiGraphicsExtractor extractor, int gx, int gy) {
        int left = gx + MAIN_LEFT;
        int right = gx + MAIN_RIGHT;
        extractor.fill(left, gy + 4, left + 2, gy + 13, ACCENT);
        extractor.fill(left + 2, gy + 4, right, gy + 13, 0xFF32220F);

        Component label = Component.translatable("screen.avoidminer.processing");
        int labelW = font.width(label);
        extractor.text(font, label, left + 2 + (right - left - 2 - labelW) / 2, gy + 5, TEXT_WHITE);
    }

    private void drawProcessingArea(GuiGraphicsExtractor extractor, int gx, int gy, Tier t) {
        if (t.inputCount == 1) {
            // Tier 1: entrada > seta horizontal > saída
            boolean active = menu.getInputProgress(0) > 0;
            drawSlotBg(extractor, gx + ProcessorMenu.T1_INPUT_X, gy + ProcessorMenu.T1_ROW_Y, active);
            drawSlotBg(extractor, gx + ProcessorMenu.T1_OUTPUT_X, gy + ProcessorMenu.T1_ROW_Y, false);
            drawHorizontalArrow(extractor,
                    gx + ProcessorMenu.T1_ARROW_X,
                    gy + ProcessorMenu.T1_ROW_Y + (18 - ProcessorMenu.T1_ARROW_ROW_H) / 2,
                    menu.getInputProgress(0), menu.getMaxProgress());
        } else {
            // Tiers 2/3: colunas entrada > progresso > saída
            for (int i = 0; i < t.inputCount; i++) {
                int sx = gx + ProcessorMenu.inputSlotX(t, i);
                boolean active = menu.getInputProgress(i) > 0;
                drawSlotBg(extractor, sx, gy + ProcessorMenu.INPUT_Y, active);
                drawVerticalArrow(extractor, sx, gy + ProcessorMenu.ARROW_Y,
                        menu.getInputProgress(i), menu.getMaxProgress(), i);
                drawSlotBg(extractor, sx, gy + ProcessorMenu.OUTPUT_Y, false);
            }
        }
    }

    // Dois slots de melhoria com lugar marcado (letra do tipo dedicado quando vazios)
    private void drawUpgradeArea(GuiGraphicsExtractor extractor, int gx, int gy) {
        drawMarkedUpgradeSlot(extractor, gx + ProcessorMenu.UPG_ENERGY_X, gy + ProcessorMenu.UPG_Y,
                "screen.avoidminer.slot.letter.energy", menu.getEnergyUpgradeTier() > 0);
        drawMarkedUpgradeSlot(extractor, gx + ProcessorMenu.UPG_SPEED_X, gy + ProcessorMenu.UPG_Y,
                "screen.avoidminer.slot.letter.speed", menu.getSpeedUpgradeTier() > 0);
    }

    private void drawMarkedUpgradeSlot(GuiGraphicsExtractor extractor, int sx, int sy, String letterKey, boolean filled) {
        drawSlotBg(extractor, sx, sy, false);
        int frame = filled ? ACCENT : ACCENT_DIM;
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

    // Slot escavado em tons de bronze; entradas ativas ganham brasa pulsante
    private void drawSlotBg(GuiGraphicsExtractor extractor, int sx, int sy, boolean heat) {
        extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, SLOT_BORDER);
        extractor.fill(sx, sy, sx + 17, sy + 17, SLOT_INNER);
        extractor.fill(sx, sy, sx + 17, sy + 1, SLOT_BORDER_DK);
        extractor.fill(sx, sy, sx + 1, sy + 17, SLOT_BORDER_DK);
        extractor.fill(sx + 17, sy + 1, sx + 18, sy + 17, SLOT_BORDER_LT);
        extractor.fill(sx + 1, sy + 17, sx + 17, sy + 18, SLOT_BORDER_LT);
        if (heat) {
            int alpha = (int) (56 + 40 * Math.sin(tickCounter * 0.12));
            extractor.fill(sx, sy, sx + 17, sy + 17, (alpha << 24) | 0x00FF7722);
        }
    }

    private void drawHorizontalArrow(GuiGraphicsExtractor extractor, int sx, int sy, int progress, int maxProgress) {
        int barW = ProcessorMenu.T1_ARROW_W;
        int barH = ProcessorMenu.T1_ARROW_ROW_H;

        extractor.fill(sx, sy, sx + barW, sy + barH, 0xFF2A1C0E);
        extractor.fill(sx + barW - 4, sy - 2, sx + barW - 3, sy + barH + 2, ACCENT_DIM);

        if (maxProgress > 0 && progress > 0) {
            int fillW = Math.clamp((int) ((long) progress * barW / Math.max(1, maxProgress)), 1, barW);
            extractor.fill(sx, sy + 2, sx + fillW, sy + barH - 2, ACCENT);
            extractor.fill(sx, sy + 2, sx + fillW, sy + 3, 0x66FFFFFF);

            int shimmer = (tickCounter * 2) % barW;
            if (shimmer < fillW) {
                extractor.fill(sx + shimmer, sy + 2, sx + shimmer + 3, sy + barH - 2, 0x44FFFFFF);
            }
        } else {
            extractor.fill(sx + 4, sy + barH / 2 - 1, sx + barW - 6, sy + barH / 2 + 1, ACCENT_DIM);
        }
    }

    private void drawVerticalArrow(GuiGraphicsExtractor extractor, int sx, int sy, int progress, int maxProgress, int slotIndex) {
        int barW = 18;
        int barH = ProcessorMenu.ARROW_H;

        extractor.fill(sx, sy, sx + barW, sy + barH, 0xFF2A1C0E);

        if (maxProgress > 0 && progress > 0) {
            int fillH = Math.clamp((int) ((long) progress * barH / Math.max(1, maxProgress)), 1, barH);
            int fillX = sx + 5;
            int fillW = barW - 10;

            extractor.fill(fillX, sy, fillX + fillW, sy + fillH, ACCENT);
            extractor.fill(fillX, sy, fillX + 1, sy + fillH, 0x66FFFFFF);

            int shimmer = (tickCounter + slotIndex * 4) % 8;
            if (shimmer < 3 && fillH > 4) {
                int shimmerY = sy + fillH - 2 - shimmer;
                if (shimmerY >= sy && shimmerY < sy + fillH - 1) {
                    extractor.fill(fillX, shimmerY, fillX + fillW, shimmerY + 1, 0x88FFFFFF);
                }
            }
        } else {
            // seta apagada apontando para baixo
            extractor.fill(sx + 7, sy + 3, sx + 11, sy + 8, ACCENT_DIM);
            extractor.fill(sx + 5, sy + 8, sx + 13, sy + 10, ACCENT_DIM);
            extractor.fill(sx + 7, sy + 10, sx + 11, sy + 12, ACCENT_DIM);
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
            extractor.fill(barX, fillY, barX + barW, barY + barH, ACCENT);
            extractor.fill(barX, fillY, barX + 1, fillY + Math.min(fillH, 2), 0xFFFFFFFF);

            int glowOffset = (tickCounter / 4) % 4;
            extractor.fill(barX, fillY + glowOffset, barX + barW, fillY + glowOffset + 1, 0x33FFFFFF);
        }
    }

    private void drawFuelGlow(GuiGraphicsExtractor extractor, int gx, int gy) {
        if (menu.isBurning()) {
            int sx = gx + FUEL_X;
            int sy = gy + FUEL_Y;
            extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, 0x33E09650);
        }
    }

    private void drawAutoBalanceButton(GuiGraphicsExtractor extractor, int gx, int gy) {
        if (menu.getTier().inputCount <= 1) return;
        int x = gx + ProcessorMenu.AUTO_BALANCE_X;
        int y = gy + ProcessorMenu.AUTO_BALANCE_Y;
        int size = ProcessorMenu.AUTO_BALANCE_SIZE;
        boolean enabled = menu.isAutoBalanceEnabled();
        int frame = enabled ? ACCENT : SLOT_BORDER_LT;
        int inner = enabled ? 0xFF5A3A18 : 0xFF261A0D;
        extractor.fill(x, y, x + size, y + size, frame);
        extractor.fill(x + 1, y + 1, x + size - 1, y + size - 1, inner);
        extractor.fill(x + 4, y + 4, x + 11, y + 6, frame);
        extractor.fill(x + 9, y + 3, x + 12, y + 7, frame);
        extractor.fill(x + 5, y + 10, x + 12, y + 12, frame);
        extractor.fill(x + 4, y + 9, x + 7, y + 13, frame);
    }

    private boolean isAutoBalanceButton(double mouseX, double mouseY) {
        if (menu.getTier().inputCount <= 1) return false;
        int x = (width - imageWidth) / 2 + ProcessorMenu.AUTO_BALANCE_X;
        int y = (height - imageHeight) / 2 + ProcessorMenu.AUTO_BALANCE_Y;
        return mouseX >= x && mouseX < x + ProcessorMenu.AUTO_BALANCE_SIZE
                && mouseY >= y && mouseY < y + ProcessorMenu.AUTO_BALANCE_SIZE;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && isAutoBalanceButton(event.x(), event.y()) && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ProcessorMenu.AUTO_BALANCE_BUTTON);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    // Painel lateral enxuto: título + tier, status com linhas ativas e custo
    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy, Tier t) {
        int px = gx + PANEL_X;

        String title = "ORE PROCESSOR";
        extractor.text(font, Component.literal(title),
                px + (PANEL_INNER_W - font.width(title)) / 2, gy + 6, ACCENT);

        extractor.text(font, Component.translatable("screen.avoidminer.status"), px, gy + 22, TEXT_DISABLED);
        String tierLabel = "T" + menu.getMachineTier();
        extractor.text(font, Component.literal(tierLabel),
                px + PANEL_INNER_W - font.width(tierLabel), gy + 22, ACCENT);

        boolean burning = menu.isBurning();
        boolean blocked = menu.isOutputBlocked();
        Component status = blocked
                ? Component.translatable("status.avoidminer.full")
                : Component.translatable(burning ? "status.avoidminer.running" : "status.avoidminer.idle");
        int statusColor = blocked ? 0xFFFF5555 : burning ? 0xFF55FF55 : TEXT_DIM;
        extractor.text(font, status, px, gy + 32, statusColor);

        int activeLines = 0;
        for (int i = 0; i < t.inputCount; i++) {
            if (menu.getInputProgress(i) > 0) activeLines++;
        }
        String lines = activeLines + "/" + t.inputCount;
        extractor.text(font, Component.literal(lines),
                px + PANEL_INNER_W - font.width(lines), gy + 32, activeLines > 0 ? ACCENT : TEXT_DISABLED);

        extractor.text(font, Component.translatable("screen.avoidminer.cost"), px, gy + 44, TEXT_DIM);
        Component cost = Component.translatable("screen.avoidminer.cost_per_item", energyCostPerItem());
        extractor.text(font, cost, px + PANEL_INNER_W - font.width(cost), gy + 44, 0xFFDDBB88);
    }

    // Custo total por item processado: ticks base x multiplicador de energia
    // (a velocidade encurta o ciclo mas não muda o custo total)
    private int energyCostPerItem() {
        float energyMult = switch (menu.getEnergyUpgradeTier()) {
            case 1 -> 0.8f; case 2 -> 0.7f; case 3 -> 0.6f; default -> 1.0f;
        };
        return Math.max(1, Math.round(menu.getBaseTicksPerProcess() * energyMult));
    }

    // Catálogo de receitas: tudo que a máquina aceita; hover mostra o resultado
    private void drawRecipeChamber(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        List<RecipeSample> recipes = recipeCatalog();
        int hovered = hoveredRecipeIndex(mouseX - gx, mouseY - gy);

        for (int i = 0; i < recipes.size() && i < GRID_MAX; i++) {
            int cx = gx + GRID_X + (i % GRID_COLS) * GRID_CELL;
            int cy = gy + GRID_Y + (i / GRID_COLS) * GRID_CELL;
            if (i == hovered) {
                extractor.fill(cx - 1, cy - 1, cx + 17, cy + 17, 0x33FFFFFF);
            }
            extractor.item(recipes.get(i).input(), cx, cy);
        }
    }

    private int hoveredRecipeIndex(int relX, int relY) {
        int col = Math.floorDiv(relX - GRID_X, GRID_CELL);
        int row = Math.floorDiv(relY - GRID_Y, GRID_CELL);
        if (col < 0 || col >= GRID_COLS || row < 0) return -1;
        int idx = row * GRID_COLS + col;
        List<RecipeSample> recipes = recipeCatalog();
        if (idx >= recipes.size() || idx >= GRID_MAX) return -1;
        int inX = relX - GRID_X - col * GRID_CELL;
        int inY = relY - GRID_Y - row * GRID_CELL;
        return (inX < 17 && inY < 17) ? idx : -1;
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int relX = mouseX - x;
        int relY = mouseY - y;

        if (isAutoBalanceButton(mouseX, mouseY)) {
            extractor.setTooltipForNextFrame(Component.translatable(
                    menu.isAutoBalanceEnabled()
                            ? "tooltip.avoidminer.auto_balance.on"
                            : "tooltip.avoidminer.auto_balance.off"), mouseX, mouseY);
            return;
        }

        if (relX >= ENERGY_X && relX < ENERGY_X + ENERGY_W
                && relY >= ENERGY_Y && relY < ENERGY_Y + ENERGY_H) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("tooltip.avoidminer.energy", menu.getEnergyStored(), menu.getEnergyCapacity()),
                    mouseX, mouseY);
            return;
        }

        int recipe = hoveredRecipeIndex(relX, relY);
        if (recipe >= 0) {
            RecipeSample sample = recipeCatalog().get(recipe);
            extractor.setTooltipForNextFrame(font, List.of(
                    sample.input().getHoverName(),
                    Component.translatable("screen.avoidminer.recipe.gives",
                            sample.output().getCount(), sample.output().getHoverName())
            ), Optional.empty(), ItemStack.EMPTY, mouseX, mouseY);
            return;
        }

        if (relY >= ProcessorMenu.UPG_Y - 1 && relY < ProcessorMenu.UPG_Y + 18) {
            if (relX >= ProcessorMenu.UPG_ENERGY_X - 1 && relX < ProcessorMenu.UPG_ENERGY_X + 18
                    && menu.getEnergyUpgradeTier() == 0) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.energy_only"), mouseX, mouseY);
            } else if (relX >= ProcessorMenu.UPG_SPEED_X - 1 && relX < ProcessorMenu.UPG_SPEED_X + 18
                    && menu.getSpeedUpgradeTier() == 0) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.speed_only"), mouseX, mouseY);
            }
        }
    }
}
