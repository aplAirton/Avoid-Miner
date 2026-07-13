package com.airton.avoidminer.screen;

import com.airton.avoidminer.lootr.MobCardItem;
import com.airton.avoidminer.menu.LootrMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * GUI exclusiva da Avoid Lootr — "câmara de invocação": o painel lateral
 * exibe o mob do cartão inserido como entidade 3D girando dentro de uma
 * câmara, abaixo da seção de status. Na área principal, o cartão (dourado,
 * input inerente à máquina) alimenta os três slots dedicados de melhoria
 * (roxos: Velocidade, Saque, Raridade) e a grade de saída.
 */
public class LootrScreen extends AbstractContainerScreen<LootrMenu> {
    private static final Identifier TEXTURE = Identifier.parse("avoidminer:textures/gui/avoid_lootr.png");

    private static final int TEXTURE_W = LootrMenu.TEXTURE_W;
    private static final int TEXTURE_H = LootrMenu.TEXTURE_H;

    private static final int PANEL_X = 4;
    private static final int PANEL_INNER_W = 80;

    private static final int MAIN_LEFT = 92;
    private static final int MAIN_RIGHT = 256;

    private static final int ENERGY_X = LootrMenu.ENERGY_X;
    private static final int ENERGY_Y = LootrMenu.ENERGY_Y;
    private static final int ENERGY_W = LootrMenu.ENERGY_W;
    private static final int ENERGY_H = LootrMenu.ENERGY_H;
    private static final int FUEL_X = LootrMenu.FUEL_X;
    private static final int FUEL_Y = LootrMenu.FUEL_Y;

    private static final int BANNER_Y = 4;
    private static final int BANNER_H = 9;
    private static final int PROG_Y = 15;
    private static final int PROG_H = 3;

    // Câmara do mob no painel lateral (abaixo da seção de status)
    private static final int CHAMBER_X0 = 6;
    private static final int CHAMBER_Y0 = 66;
    private static final int CHAMBER_X1 = 82;
    private static final int CHAMBER_Y1 = 196;

    private static final int ACCENT = 0xFF8844CC;       // melhorias / identidade da máquina
    private static final int ACCENT_DIM = 0xFF663399;
    private static final int CARD_GOLD = 0xFFDDAA33;    // cartão (input inerente)
    private static final int CARD_GOLD_DIM = 0xFF6A5220;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFBB99DD;
    private static final int TEXT_DISABLED = 0xFF554477;

    private int tickCounter = 0;

    // Entidade dummy só para exibição na câmara, cacheada por tipo de cartão
    @Nullable private EntityType<?> cachedEntityType;
    @Nullable private LivingEntity cachedEntity;

    public LootrScreen(LootrMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE_W, TEXTURE_H);
        this.titleLabelX = 9999;
        this.titleLabelY = 9999;
        this.inventoryLabelX = LootrMenu.MAIN_X;
        this.inventoryLabelY = 111;
    }

    @Nullable
    private EntityType<?> cardEntityType() {
        ItemStack card = menu.slots.get(LootrMenu.SLOT_INDEX_CARD).getItem();
        if (card.getItem() instanceof MobCardItem mobCard) {
            return mobCard.getCardType().entityType;
        }
        return null;
    }

    @Nullable
    private LivingEntity previewEntity() {
        EntityType<?> type = cardEntityType();
        if (type == null || minecraft == null || minecraft.level == null) {
            cachedEntityType = null;
            cachedEntity = null;
            return null;
        }
        if (type != cachedEntityType) {
            Entity created = type.create(minecraft.level, EntitySpawnReason.LOAD);
            cachedEntity = created instanceof LivingEntity living ? living : null;
            cachedEntityType = type;
        }
        return cachedEntity;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        tickCounter++;

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                0f, 0f, TEXTURE_W, TEXTURE_H, TEXTURE_W, TEXTURE_H);

        drawMobBanner(extractor, x, y);
        drawProgressBar(extractor, x, y);
        drawSlotFrames(extractor, x, y);
        drawEnergyBar(extractor, x, y);
        drawFuelGlow(extractor, x, y);
        drawInfoPanel(extractor, x, y);
        drawMobChamber(extractor, x, y);

        super.extractContents(extractor, mouseX, mouseY, partialTick);
    }

    // Faixa superior: nome do mob do cartão inserido (dourada) ou aviso para inserir cartão
    private void drawMobBanner(GuiGraphicsExtractor extractor, int gx, int gy) {
        EntityType<?> type = cardEntityType();
        boolean hasCard = type != null;
        int accent = hasCard ? CARD_GOLD : ACCENT_DIM;
        int dim = (accent & 0x00FFFFFF) >> 1 | 0xFF000000;

        int bandTop = gy + BANNER_Y;
        int bandBot = bandTop + BANNER_H;
        int left = gx + MAIN_LEFT;
        int right = gx + MAIN_RIGHT;

        extractor.fill(left, bandTop, left + 2, bandBot, accent);
        extractor.fill(left + 2, bandTop, right, bandBot, dim);

        Component label = hasCard
                ? type.getDescription()
                : Component.translatable("screen.avoidminer.lootr.insert_card");
        int labelW = font.width(label);
        int tx = left + 2 + (right - left - 2 - labelW) / 2;
        extractor.text(font, label, tx, gy + BANNER_Y + 1, hasCard ? TEXT_WHITE : TEXT_DIM);
    }

    private void drawProgressBar(GuiGraphicsExtractor extractor, int gx, int gy) {
        int barX = gx + MAIN_LEFT + 2;
        int barY = gy + PROG_Y;
        int barW = MAIN_RIGHT - MAIN_LEFT - 4;

        extractor.fill(barX, barY, barX + barW, barY + PROG_H, 0xFF0A0A12);

        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();

        if (menu.isBurning() && maxProgress > 0 && progress > 0) {
            int fillW = (int) ((long) progress * barW / Math.max(1, maxProgress));
            fillW = Math.clamp(fillW, 1, barW);

            extractor.fill(barX, barY, barX + fillW, barY + PROG_H, ACCENT);
            extractor.fill(barX, barY, barX + fillW, barY + 1, 0x88FFFFFF);

            int shimmer = (tickCounter * 2) % barW;
            if (shimmer < fillW) {
                extractor.fill(barX + shimmer, barY, barX + shimmer + 4, barY + PROG_H, 0x44FFFFFF);
            }
        }
    }

    // Cartão em dourado (input inerente) e melhorias em roxo (uma cor para todas),
    // com letra do tipo dedicado quando vazias
    private void drawSlotFrames(GuiGraphicsExtractor extractor, int gx, int gy) {
        boolean cardIn = menu.slots.get(LootrMenu.SLOT_INDEX_CARD).hasItem();
        drawSlotFrame(extractor, gx + LootrMenu.CARD_X, gy + LootrMenu.CARD_Y,
                menu.hasValidCard() ? CARD_GOLD : CARD_GOLD_DIM);
        if (!cardIn) {
            Component letter = Component.translatable("screen.avoidminer.slot.letter.card");
            int lw = font.width(letter);
            extractor.text(font, letter, gx + LootrMenu.CARD_X + 9 - lw / 2, gy + LootrMenu.CARD_Y + 5, CARD_GOLD_DIM);
        }

        String[] letterKeys = {
                "screen.avoidminer.slot.letter.speed",
                "screen.avoidminer.slot.letter.loot",
                "screen.avoidminer.slot.letter.rarity"
        };
        int[] slotIndexes = { LootrMenu.SLOT_INDEX_UPG_SPEED, LootrMenu.SLOT_INDEX_UPG_LOOT, LootrMenu.SLOT_INDEX_UPG_RARITY };
        for (int i = 0; i < 3; i++) {
            int sx = gx + LootrMenu.UPG_X + i * LootrMenu.UPG_SPACING;
            int sy = gy + LootrMenu.UPG_Y;
            boolean filled = menu.slots.get(slotIndexes[i]).hasItem();
            drawSlotFrame(extractor, sx, sy, filled ? ACCENT : ACCENT_DIM);
            if (!filled) {
                Component letter = Component.translatable(letterKeys[i]);
                int lw = font.width(letter);
                extractor.text(font, letter, sx + 9 - lw / 2, sy + 5, TEXT_DISABLED);
            }
        }
    }

    private void drawSlotFrame(GuiGraphicsExtractor extractor, int sx, int sy, int color) {
        extractor.fill(sx - 1, sy - 1, sx + 18, sy, color);
        extractor.fill(sx - 1, sy + 17, sx + 18, sy + 18, color);
        extractor.fill(sx - 1, sy, sx, sy + 17, color);
        extractor.fill(sx + 17, sy, sx + 18, sy + 17, color);
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
            extractor.fill(sx - 1, sy - 1, sx + 18, sy + 18, 0x338844CC);
        }
    }

    // Painel lateral enxuto: título, status e a câmara do mob logo abaixo
    private void drawInfoPanel(GuiGraphicsExtractor extractor, int gx, int gy) {
        int px = gx + PANEL_X;

        String title = "AVOID LOOTR";
        extractor.text(font, Component.literal(title),
                px + (PANEL_INNER_W - font.width(title)) / 2, gy + 6, ACCENT);

        extractor.text(font, Component.translatable("screen.avoidminer.status"), px, gy + 22, TEXT_DISABLED);

        boolean burning = menu.isBurning();
        boolean outputFull = menu.isOutputFull();
        Component status = outputFull
                ? Component.translatable("status.avoidminer.full")
                : Component.translatable(burning ? "status.avoidminer.running" : "status.avoidminer.idle");
        int statusColor = outputFull ? 0xFFFF5555 : burning ? 0xFF55FF55 : 0xFFAA5544;
        extractor.text(font, status, px, gy + 32, statusColor);

        int maxProgress = menu.getMaxProgress();
        int pct = maxProgress > 0 ? Math.min(100, menu.getProgress() * 100 / maxProgress) : 0;
        String pctStr = pct + "%";
        extractor.text(font, Component.literal(pctStr),
                px + PANEL_INNER_W - font.width(pctStr), gy + 32, burning ? ACCENT : TEXT_DISABLED);

        boolean incomplete = menu.hasCard() && !menu.hasValidCard();
        Component opLabel = incomplete
                ? Component.translatable("status.avoidminer.incomplete")
                : menu.lastOperationSucceeded()
                        ? Component.translatable("status.avoidminer.success")
                        : Component.translatable("status.avoidminer.fail");
        int opColor = incomplete ? 0xFFDDAA33 : menu.lastOperationSucceeded() ? 0xFF55FF55 : 0xFFFF5555;
        extractor.text(font, opLabel, px, gy + 44, opColor);
    }

    // Entidade 3D do mob do cartão girando em pedestal; "?" quando não há cartão
    private void drawMobChamber(GuiGraphicsExtractor extractor, int gx, int gy) {
        int x0 = gx + CHAMBER_X0;
        int y0 = gy + CHAMBER_Y0;
        int x1 = gx + CHAMBER_X1;
        int y1 = gy + CHAMBER_Y1;

        LivingEntity entity = previewEntity();
        if (entity == null) {
            String q = "?";
            int qw = font.width(q);
            extractor.text(font, Component.literal(q),
                    (x0 + x1) / 2 - qw / 2, (y0 + y1) / 2 - 4, TEXT_DISABLED);
            return;
        }

        if (menu.isBurning()) {
            int pulse = (int) (32 + 24 * Math.sin(tickCounter * 0.08));
            extractor.fill(x0, y1 - 6, x1, y1, (pulse << 24) | (ACCENT & 0x00FFFFFF));
        }

        float bbW = Math.max(0.4f, entity.getBbWidth());
        float bbH = Math.max(0.4f, entity.getBbHeight());
        int size = (int) Math.min((y1 - y0) * 0.68f / bbH, (x1 - x0) * 0.72f / bbW);
        size = Math.clamp(size, 4, 50);

        // renderEntityInInventoryFollowsAngle usa xAngle*20 graus: /20 dá giro contínuo
        float spin = (tickCounter % 480) * 0.75f / 20.0f;
        InventoryScreen.renderEntityInInventoryFollowsAngle(
                extractor, x0, y0, x1, y1, size, 0.0625f, spin, 0f, entity);
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

        if (relY >= LootrMenu.UPG_Y - 1 && relY < LootrMenu.UPG_Y + 18) {
            if (relX >= LootrMenu.CARD_X - 1 && relX < LootrMenu.CARD_X + 18
                    && !menu.slots.get(LootrMenu.SLOT_INDEX_CARD).hasItem()) {
                extractor.setTooltipForNextFrame(
                        Component.translatable("tooltip.avoidminer.slot.card_only"), mouseX, mouseY);
                return;
            }
            String[] keys = {
                    "tooltip.avoidminer.slot.speed_only",
                    "tooltip.avoidminer.slot.loot_only",
                    "tooltip.avoidminer.slot.rarity_only"
            };
            int[] slotIndexes = { LootrMenu.SLOT_INDEX_UPG_SPEED, LootrMenu.SLOT_INDEX_UPG_LOOT, LootrMenu.SLOT_INDEX_UPG_RARITY };
            for (int i = 0; i < 3; i++) {
                int sx = LootrMenu.UPG_X + i * LootrMenu.UPG_SPACING;
                if (relX >= sx - 1 && relX < sx + 18 && !menu.slots.get(slotIndexes[i]).hasItem()) {
                    extractor.setTooltipForNextFrame(Component.translatable(keys[i]), mouseX, mouseY);
                    return;
                }
            }
        }
    }
}
