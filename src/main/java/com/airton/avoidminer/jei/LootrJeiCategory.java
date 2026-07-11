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
    private static final int ROW_SIZE = 5;

    public LootrJeiCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("avoidminer.jei.lootr.category"),
                guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.AVOID_LOOTR.get())),
                170, 85);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LootrJeiRecipe recipe, IFocusGroup focuses) {
        MobCardType cardType = recipe.cardType();

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 20)
                .add(recipe.cardItem());

        List<MobCardType.LootEntry> normalDrops = cardType.normalDrops();
        List<MobCardType.RareEntry> rareDrops = cardType.rareDrops();

        for (int i = 0; i < normalDrops.size(); i++) {
            var entry = normalDrops.get(i);
            int x = 43 + (i % ROW_SIZE) * (SLOT_SIZE + GAP);
            int y = 3 + (i / ROW_SIZE) * (SLOT_SIZE + GAP);
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

        int rareRow = (normalDrops.size() + ROW_SIZE - 1) / ROW_SIZE;
        int rareY = 3 + rareRow * (SLOT_SIZE + GAP) + 6;

        for (int i = 0; i < rareDrops.size(); i++) {
            var rare = rareDrops.get(i);
            int x = 43 + i * (SLOT_SIZE + GAP);
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, rareY)
                    .add(new ItemStack(rare.item()))
                    .addRichTooltipCallback((slot, tooltip) -> {
                        double pct = rare.chance() * 100;
                        tooltip.add(Component.translatable("avoidminer.jei.lootr.chance_dec", String.format("%.2f", pct)));
                        tooltip.add(Component.translatable("avoidminer.jei.lootr.requires_rarity"));
                    });
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, LootrJeiRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(200).setPosition(20, 20);

        if (!recipe.cardType().rareDrops().isEmpty()) {
            List<MobCardType.LootEntry> normalDrops = recipe.cardType().normalDrops();
            int rareRow = (normalDrops.size() + ROW_SIZE - 1) / ROW_SIZE;
            int labelY = 1 + rareRow * (SLOT_SIZE + GAP) + 6;
            builder.addText(Component.translatable("avoidminer.jei.lootr.rare_section"), 170, 85)
                    .setPosition(43, labelY);
        }
    }
}
