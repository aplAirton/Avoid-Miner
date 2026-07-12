package com.airton.avoidminer.jei;

import com.airton.avoidminer.ModBlocks;
import com.airton.avoidminer.lootr.MobCardType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class LootrJeiCategory extends AbstractRecipeCategory<LootrJeiRecipe> {
    public static final IRecipeType<LootrJeiRecipe> TYPE =
            IRecipeType.create(Identifier.parse("avoidminer:lootr"), LootrJeiRecipe.class);

    private static final int SLOT_SIZE = 18;
    private static final int GAP = 4;
    private static final int ROW_SIZE = 4;
    private static final int CARD_X = 1;
    private static final int CARD_Y = 16;
    private static final int ARROW_X = 20;
    private static final int ARROW_Y = 18;
    private static final int OUTPUT_X = 45;
    private static final int OUTPUT_Y = 3;

    public LootrJeiCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("avoidminer.jei.lootr.category"),
                guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.AVOID_LOOTR.get())),
                140, 90);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LootrJeiRecipe recipe, IFocusGroup focuses) {
        MobCardType cardType = recipe.cardType();

        builder.addSlot(RecipeIngredientRole.INPUT, CARD_X, CARD_Y)
                .add(recipe.cardItem())
                .addRichTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.translatable("tooltip.avoidminer.mobcard.target",
                            Component.translatable("entity." + cardType.id)));
                    tooltip.add(Component.translatable("tooltip.avoidminer.mobcard.progress",
                            0, cardType.requiredKills));
                });

        List<MobCardType.LootEntry> normalDrops = cardType.normalDrops();
        List<MobCardType.RareEntry> rareDrops = cardType.rareDrops();

        for (int i = 0; i < normalDrops.size(); i++) {
            var entry = normalDrops.get(i);
            int x = OUTPUT_X + (i % ROW_SIZE) * (SLOT_SIZE + GAP);
            int y = OUTPUT_Y + (i / ROW_SIZE) * (SLOT_SIZE + GAP);
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .add(new ItemStack(entry.item()))
                    .addRichTooltipCallback((slot, tooltip) -> {
                        int pct = (int) Math.round(entry.chance() * 100);
                        tooltip.add(Component.translatable("avoidminer.jei.lootr.chance", pct));
                        tooltip.add(Component.translatable("avoidminer.jei.lootr.quantity",
                                entry.min(), entry.max()));
                        if (entry.lootingScales()) {
                            tooltip.add(Component.translatable("avoidminer.jei.lootr.looting"));
                        }
                    });
        }

        if (!rareDrops.isEmpty()) {
            int normalRows = (normalDrops.size() + ROW_SIZE - 1) / ROW_SIZE;
            int rareY = OUTPUT_Y + normalRows * (SLOT_SIZE + GAP) + 10;

            for (int i = 0; i < rareDrops.size(); i++) {
                var rare = rareDrops.get(i);
                int x = OUTPUT_X + i * (SLOT_SIZE + GAP);
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, rareY)
                        .add(new ItemStack(rare.item()))
                        .addRichTooltipCallback((slot, tooltip) -> {
                            double pct = rare.chance() * 100;
                            tooltip.add(Component.translatable(
                                    "avoidminer.jei.lootr.chance_dec", String.format("%.2f", pct)));
                        });
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, LootrJeiRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(200).setPosition(ARROW_X, ARROW_Y);
    }
}
