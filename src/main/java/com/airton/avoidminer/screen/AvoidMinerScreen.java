package com.airton.avoidminer.screen;

import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity;
import com.airton.avoidminer.menu.AvoidMinerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * GUI do Avoid Miner no padrão "câmara" da Lootr: painel lateral com título,
 * status e, abaixo, a câmara de amostras — grade com os itens que a
 * configuração atual (tier + modo de mundo + efeito) pode gerar, do mais
 * comum ao mais raro. Passar o mouse numa amostra mostra a chance por ciclo.
 */
public class AvoidMinerScreen extends AbstractContainerScreen<AvoidMinerMenu> {
    private static final Identifier TEXTURE = Identifier.parse("avoidminer:textures/gui/avoid_miner.png");

    private static final int TEXTURE_W = AvoidMinerMenu.TEXTURE_W;
    private static final int TEXTURE_H = AvoidMinerMenu.TEXTURE_H;

    private static final int PANEL_X = 4;
    private static final int PANEL_INNER_W = 80;

    private static final int MAIN_LEFT = 92;
    private static final int MAIN_RIGHT = 258;

    private static final int ENERGY_X = AvoidMinerMenu.ENERGY_X;
    private static final int ENERGY_Y = AvoidMinerMenu.ENERGY_Y;
    private static final int ENERGY_W = AvoidMinerMenu.ENERGY_W;
    private static final int ENERGY_H = AvoidMinerMenu.ENERGY_H;
    private static final int FUEL_X = AvoidMinerMenu.FUEL_X;
    private static final int FUEL_Y = AvoidMinerMenu.FUEL_Y;

    private static final int BANNER_Y = 4;
    private static final int BANNER_H = 9;
    private static final int PROG_Y = 15;
    private static final int PROG_H = 3;

    // Câmara de amostras: grade de 4 colunas dentro da janela do painel
    private static final int GRID_X = 8;
    private static final int GRID_Y = 70;
    private static final int GRID_COLS = 4;
    private static final int GRID_CELL = 18;
    private static final int GRID_MAX = 28;

    private static final int ACCENT_T1 = 0xFF3388CC;
    private static final int ACCENT_T2 = 0xFF55AAEE;
    private static final int ACCENT_T3 = 0xFF88DDFF;

    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFF8899BB;
    private static final int TEXT_DISABLED = 0xFF455577;

    private int tickCounter = 0;

    // Cache da pool exibida na câmara (recalculada quando a configuração muda)
    private record SampleEntry(ItemStack stack, int weight) {}
    private List<SampleEntry> cachedSamples = List.of();
    private int cachedTotalWeight = 1;
    private int cachedMode = -1;
    private int cachedEnchant = -1;
    private int cachedTier = -1;

    public AvoidMinerScreen(AvoidMinerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE_W, TEXTURE_H);
        this.titleLabelX = 9999;
        this.titleLabelY = 9999;
        this.inventoryLabelX = AvoidMinerMenu.MAIN_X;
        this.inventoryLabelY = 111;
    }

    private int accentColor() {
        return switch (menu.getMachineTier()) {
            case 2 -> ACCENT_T2;
            case 3 -> ACCENT_T3;
            default -> ACCENT_T1;
        };
    }

    private AvoidMinerBlockEntity.Tier machineTier() {
        int t = Math.clamp(menu.getMachineTier(), 1, 3);
        return AvoidMinerBlockEntity.Tier.values()[t - 1];
    }

    private List<SampleEntry> samplePool() {
        int mode = menu.getWorldMode();
        int enchant = menu.getEnchantMode();
        int tier = menu.getMachineTier();
        if (mode == cachedMode && enchant == cachedEnchant && tier == cachedTier) return cachedSamples;

        AvoidMinerBlockEntity.Tier t = machineTier();
        List<AvoidMinerBlockEntity.WeightedResource> pool = switch (mode) {
            case 1 -> t.botanyResources.get();
            case 2 -> t.netherResources.get();
            case 3 -> t.endResources.get();
            default -> t.resources.get();
        };

        List<SampleEntry> samples = new ArrayList<>(pool.size());
        int total = 0;
        for (AvoidMinerBlockEntity.WeightedResource r : pool) {
            ItemStack display = r.stack().copy();
            if (enchant == 2 && mode != 1) {
                display = AvoidMinerBlockEntity.applySilkTouch(display, mode == 2, false);
            }
            samples.add(new SampleEntry(display, r.weight()));
            total += r.weight();
        }
        samples.sort((a, b) -> Integer.compare(b.weight(), a.weight()));

        cachedSamples = samples;
        cachedTotalWeight = Math.max(1, total);
        cachedMode = mode;
        cachedEnchant = enchant;
        cachedTier = tier;
        return samples;
    }

    private float effectiveSuccessChance() {
        float mult = 1.0f;
        for (int i = 0; i < menu.getUpgradeSlotCount(); i++) {
            if (menu.getUpgradeType(i) == 1) {
                mult = switch (menu.getUpgradeTier(i)) {
                    case 1 -> 1.5f; case 2 -> 1.7f; case 3 -> 2.0f; default -> 1.0f;
                };
            }
        }
        return Math.min(1.0f, machineTier().successChance * mult);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        tickCounter++;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0f, 0f, TEXTURE_W, TEXTURE_H, TEXTURE_W, TEXTURE_H);

        drawModeBanner(extractor, x, y);
        drawProgressBar(extractor, x, y);
        drawUpgradeSlots(extractor, x, y);
        drawSpecialSlotAccents(extractor, x, y);
        drawOutputFullAlert(extractor, x, y);
        drawEnergyBar(extractor, x, y);
        drawFuelGlow(extractor, x, y);
        drawInfoPanel(extractor, x, y);
        drawSampleChamber(extractor, x, y, mouseX, mouseY);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    private void drawModeBanner(GuiGraphicsExtractor extractor, int gx, int gy) {
        int accent = accentColor();
        int dim = (accent & 0x00FFFFFF) >> 1 | 0xFF000000;

        int bandTop = gy + BANNER_Y;
        int bandBot = bandTop + BANNER_H;
        int left = gx + MAIN_LEFT;
        int right = gx + MAIN_RIGHT;

        extractor.fill(left, bandTop, left + 2, bandBot, accent);
        extractor.fill(left + 2, bandTop, right, bandBot, dim);

        Component label = switch (menu.getWorldMode()) {
            case 1 -> Component.translatable("screen.avoidminer.mode.botany");
            case 2 -> Component.translatable("screen.avoidminer.mode.nether");
            case 3 -> Component.translatable("screen.avoidminer.mode.end");
            default -> Component.translatable("screen.avoidminer.mode.overworld");
        };
        if (menu.getEnchantMode() > 0) {
            Component ench = menu.getEnchantMode() == 1
                    ? Component.translatable("screen.avoidminer.enchant.fortune")
                    : Component.translatable("screen.avoidminer.enchant.silk_touch");
            label = label.copy().append(" + ").append(ench);
        }
        int labelW = font.width(label);
        int tx = left + 2 + (right - left - 2 - labelW) / 2;
        extractor.text(font, label, tx, gy + BANNER_Y + 1, TEXT_WHITE);
    }

    private void drawProgressBar(GuiGraphicsExtractor extractor, int gx, int gy) {
        int barX = gx + MAIN_LEFT + 2;
        int barY = gy + PROG_Y;
        int barW = MAIN_RIGHT - MAIN_LEFT - 4;

        extractor.fill(barX, barY, barX + barW, barY + PROG_H, 0xFF0A0A12);

        int progress = menu.getRawProgress();
        int maxProgress = menu.getMaxProgress();

        if (menu.isBurning() && maxProgress > 0 && progress > 0) {
            int fillW = (int) ((long) progress * barW / Math.max(1, maxProgress));
            fillW = Math.clamp(fillW, 1, barW);

            int accent = accentColor();
            extractor.fill(barX, barY, barX + fillW, barY + PROG_H, accent);
            extractor.fill(barX, barY, barX + fillW, barY + 1, 0x88FFFFFF);

            int shimmer = (tickCounter * 2) % barW;
            if (shimmer < fillW) {
                extractor.fill(barX + shimmer, barY, barX + shimmer + 4, barY + PROG_H, 0x44FFFFFF);
            }
        }
    }

    // Slots de melhoria dedicados (M/E/S): borda acesa nos desbloqueados,
    // letra do tipo quando vazios, cadeado nos bloqueados pelo tier
    private void drawUpgradeSlots(GuiGraphicsExtractor extractor, int gx, int gy) {
        int slotCount = menu.getUpgradeSlotCount();
        int accent = accentColor();

        for (int i = 0; i < 3; i++) {
            int sx = gx + AvoidMinerMenu.MAIN_X + i * AvoidMinerMenu.UPG_SPACING;
            int sy = gy + AvoidMinerMenu.UPG_Y;

            if (i < slotCount) {
                extractor.fill(sx - 1, sy - 1, sx + 18, sy, accent);
                extractor.fill(sx - 1, sy + 17, sx + 18, sy + 18, accent);
                extractor.fill(sx - 1, sy, sx - 1 + 1, sy + 17, accent);
                extractor.fill(sx + 17, sy, sx + 18, sy + 17, accent);

                if (menu.getUpgradeType(i) == 0) {
                    Component letter = Component.translatable(switch (i) {
                        case 0 -> "screen.avoidminer.slot.letter.mining";
                        case 1 -> "screen.avoidminer.slot.letter.energy";
                        default -> "screen.avoidminer.slot.letter.speed";
                    });
                    int lw = font.width(letter);
                    extractor.text(font, letter, sx + 9 - lw / 2, sy + 5, TEXT_DISABLED);
                }
            } else {
                extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, 0xCC10141F);
                // cadeado: corpo + alça
                int cx = sx + 8;
                extractor.fill(cx - 3, sy + 7, cx + 4, sy + 13, 0xFF55617F);
                extractor.fill(cx - 2, sy + 4, cx - 1, sy + 7, 0xFF55617F);
                extractor.fill(cx + 2, sy + 4, cx + 3, sy + 7, 0xFF55617F);
                extractor.fill(cx - 2, sy + 4, cx + 3, sy + 5, 0xFF55617F);
            }
        }
    }

    private void drawSpecialSlotAccents(GuiGraphicsExtractor extractor, int gx, int gy) {
        int worldAccent = switch (menu.getWorldMode()) {
            case 1 -> 0xFF33AA55;
            case 2 -> 0xFFAA3333;
            case 3 -> 0xFFBB66EE;
            default -> 0xFF224488;
        };
        int enchantAccent = switch (menu.getEnchantMode()) {
            case 1 -> 0xFFFFDD44;
            case 2 -> 0xFFDDDDFF;
            default -> 0xFF555544;
        };

        drawSlotFrame(extractor, gx + AvoidMinerMenu.WORLD_X, gy + AvoidMinerMenu.WORLD_Y, worldAccent);
        drawSlotFrame(extractor, gx + AvoidMinerMenu.ENCHANT_X, gy + AvoidMinerMenu.ENCHANT_Y, enchantAccent);

        if (menu.getWorldMode() == 0) {
            drawSlotLetter(extractor, gx + AvoidMinerMenu.WORLD_X, gy + AvoidMinerMenu.WORLD_Y,
                    "screen.avoidminer.slot.letter.world");
        }
        if (menu.getEnchantMode() == 0) {
            drawSlotLetter(extractor, gx + AvoidMinerMenu.ENCHANT_X, gy + AvoidMinerMenu.ENCHANT_Y,
                    "screen.avoidminer.slot.letter.effect");
        }
    }

    private void drawSlotLetter(GuiGraphicsExtractor extractor, int sx, int sy, String key) {
        Component letter = Component.translatable(key);
        int lw = font.width(letter);
        extractor.text(font, letter, sx + 9 - lw / 2, sy + 5, TEXT_DISABLED);
    }

    private void drawSlotFrame(GuiGraphicsExtractor extractor, int sx, int sy, int color) {
        extractor.fill(sx - 1, sy - 1, sx + 18, sy, color);
        extractor.fill(sx - 1, sy + 17, sx + 18, sy + 18, color);
        extractor.fill(sx - 1, sy, sx, sy + 17, color);
        extractor.fill(sx + 17, sy, sx + 18, sy + 17, color);
    }

    // Saída cheia: moldura vermelha pulsando em volta da grade de saída
    private void drawOutputFullAlert(GuiGraphicsExtractor extractor, int gx, int gy) {
        if (!menu.isOutputFull()) return;
        int alpha = (int) (120 + 100 * Math.sin(tickCounter * 0.15));
        int color = (alpha << 24) | 0x00FF3333;
        int x0 = gx + AvoidMinerMenu.MAIN_X - 2;
        int y0 = gy + AvoidMinerMenu.OUT_Y - 2;
        int x1 = gx + AvoidMinerMenu.MAIN_X + 9 * 18 + 1;
        int y1 = gy + AvoidMinerMenu.OUT_Y + 3 * 18 + 1;
        extractor.fill(x0, y0, x1, y0 + 1, color);
        extractor.fill(x0, y1 - 1, x1, y1, color);
        extractor.fill(x0, y0, x0 + 1, y1, color);
        extractor.fill(x1 - 1, y0, x1, y1, color);
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
            int accent = accentColor();
            extractor.fill(barX, fillY, barX + barW, barY + barH, accent);
            extractor.fill(barX, fillY, barX + 1, fillY + Math.min(fillH, 2), 0xFFFFFFFF);

            int glowOffset = (tickCounter / 4) % 4;
            extractor.fill(barX, fillY + glowOffset, barX + barW, fillY + glowOffset + 1, 0x33FFFFFF);
        }
    }

    private void drawFuelGlow(GuiGraphicsExtractor extractor, int gx, int gy) {
        if (menu.isBurning()) {
            int sx = gx + FUEL_X;
            int sy = gy + FUEL_Y;
            extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, 0x333399CC);
        }
    }

    // Painel lateral enxuto: título + tier, status e custo; câmara logo abaixo
    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy) {
        int accent = accentColor();
        int px = gx + PANEL_X;

        String title = "AVOID MINER";
        extractor.text(font, Component.literal(title),
                px + (PANEL_INNER_W - font.width(title)) / 2, gy + 6, accent);

        extractor.text(font, Component.translatable("screen.avoidminer.status"), px, gy + 22, TEXT_DISABLED);
        String tierLabel = "T" + menu.getMachineTier();
        extractor.text(font, Component.literal(tierLabel),
                px + PANEL_INNER_W - font.width(tierLabel), gy + 22, accent);

        boolean burning = menu.isBurning();
        boolean outputFull = menu.isOutputFull();
        Component status = outputFull
                ? Component.translatable("status.avoidminer.full")
                : Component.translatable(burning ? "status.avoidminer.running" : "status.avoidminer.idle");
        int statusColor = outputFull ? 0xFFFF5555 : burning ? 0xFF55FF55 : 0xFFAA5555;
        extractor.text(font, status, px, gy + 32, statusColor);

        int maxProgress = menu.getMaxProgress();
        int pct = maxProgress > 0 ? Math.min(100, menu.getRawProgress() * 100 / maxProgress) : 0;
        String pctStr = pct + "%";
        extractor.text(font, Component.literal(pctStr),
                px + PANEL_INNER_W - font.width(pctStr), gy + 32, burning ? accent : TEXT_DISABLED);

        extractor.text(font, Component.translatable("screen.avoidminer.cost"), px, gy + 44, TEXT_DIM);
        Component cost = Component.translatable("screen.avoidminer.cost_per_gen", menu.getEnergyCostPerGeneration());
        extractor.text(font, cost, px + PANEL_INNER_W - font.width(cost), gy + 44, 0xFFAABBDD);
    }

    // Câmara de amostras: itens que esta configuração pode gerar, do mais
    // comum ao mais raro; célula sob o mouse ganha realce
    private void drawSampleChamber(GuiGraphicsExtractor extractor, int gx, int gy, int mouseX, int mouseY) {
        List<SampleEntry> samples = samplePool();
        int hovered = hoveredSampleIndex(mouseX - gx, mouseY - gy);

        for (int i = 0; i < samples.size() && i < GRID_MAX; i++) {
            int cx = gx + GRID_X + (i % GRID_COLS) * GRID_CELL;
            int cy = gy + GRID_Y + (i / GRID_COLS) * GRID_CELL;
            if (i == hovered) {
                extractor.fill(cx - 1, cy - 1, cx + 17, cy + 17, 0x33FFFFFF);
            }
            extractor.item(samples.get(i).stack(), cx, cy);
        }
    }

    private int hoveredSampleIndex(int relX, int relY) {
        int col = Math.floorDiv(relX - GRID_X, GRID_CELL);
        int row = Math.floorDiv(relY - GRID_Y, GRID_CELL);
        if (col < 0 || col >= GRID_COLS || row < 0) return -1;
        int idx = row * GRID_COLS + col;
        List<SampleEntry> samples = samplePool();
        if (idx >= samples.size() || idx >= GRID_MAX) return -1;
        // dentro da célula de 18px, o item ocupa 16px a partir da origem
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

        if (relX >= ENERGY_X && relX < ENERGY_X + ENERGY_W
                && relY >= ENERGY_Y && relY < ENERGY_Y + ENERGY_H) {
            extractor.setTooltipForNextFrame(
                    Component.translatable("tooltip.avoidminer.energy", menu.getEnergyStored(), menu.getEnergyCapacity()),
                    mouseX, mouseY);
            return;
        }

        // Amostras da câmara: chance por ciclo + detalhe peso/sucesso
        int sample = hoveredSampleIndex(relX, relY);
        if (sample >= 0) {
            SampleEntry entry = samplePool().get(sample);
            float success = effectiveSuccessChance();
            float weightPct = 100.0f * entry.weight() / cachedTotalWeight;
            float combined = weightPct * success;
            extractor.setTooltipForNextFrame(font, List.of(
                    entry.stack().getHoverName(),
                    Component.translatable("avoidminer.jei.combined_chance",
                            String.format(Locale.ROOT, "%.2f", combined)),
                    Component.translatable("avoidminer.jei.detail_chance",
                            String.format(Locale.ROOT, "%.1f", weightPct),
                            String.format(Locale.ROOT, "%.0f", success * 100))
            ), Optional.empty(), ItemStack.EMPTY, mouseX, mouseY);
            return;
        }

        // Slots de melhoria: bloqueados mostram o tier exigido,
        // desbloqueados vazios mostram o tipo dedicado que aceitam
        int slotCount = menu.getUpgradeSlotCount();
        for (int i = 0; i < 3; i++) {
            int sx = AvoidMinerMenu.MAIN_X + i * AvoidMinerMenu.UPG_SPACING;
            if (relX >= sx - 1 && relX < sx + 18
                    && relY >= AvoidMinerMenu.UPG_Y - 1 && relY < AvoidMinerMenu.UPG_Y + 18) {
                if (i >= slotCount) {
                    extractor.setTooltipForNextFrame(
                            Component.translatable("tooltip.avoidminer.slot.locked", i + 1),
                            mouseX, mouseY);
                } else if (menu.getUpgradeType(i) == 0) {
                    String key = switch (i) {
                        case 0 -> "tooltip.avoidminer.slot.mining_only";
                        case 1 -> "tooltip.avoidminer.slot.energy_only";
                        default -> "tooltip.avoidminer.slot.speed_only";
                    };
                    extractor.setTooltipForNextFrame(Component.translatable(key), mouseX, mouseY);
                }
                return;
            }
        }

        // Slots de mundo/efeito vazios
        if (relY >= AvoidMinerMenu.WORLD_Y - 1 && relY < AvoidMinerMenu.WORLD_Y + 18) {
            if (relX >= AvoidMinerMenu.WORLD_X - 1 && relX < AvoidMinerMenu.WORLD_X + 18
                    && menu.getWorldMode() == 0) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.world_only"), mouseX, mouseY);
            } else if (relX >= AvoidMinerMenu.ENCHANT_X - 1 && relX < AvoidMinerMenu.ENCHANT_X + 18
                    && menu.getEnchantMode() == 0) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.effect_only"), mouseX, mouseY);
            }
        }
    }
}
