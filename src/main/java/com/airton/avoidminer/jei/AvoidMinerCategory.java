package com.airton.avoidminer.jei;

import com.airton.avoidminer.ModBlocks;
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

public class AvoidMinerCategory extends AbstractRecipeCategory<AvoidMinerJeiRecipe> {
    public static final IRecipeType<AvoidMinerJeiRecipe> TYPE =
            IRecipeType.create(Identifier.parse("avoidminer:avoid_miner_outputs"), AvoidMinerJeiRecipe.class);

    public AvoidMinerCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("avoidminer.jei.category"),
                guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.AVOID_MINER_TIER_3.get())),
                100, 36);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AvoidMinerJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 1, 10)
                .add(recipe.machine());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 70, 10)
                .add(recipe.output())
                .addRichTooltipCallback((slot, tooltip) -> {
                    int combined = (int) Math.round(recipe.dropChance() * recipe.successChance() * 100);
                    tooltip.add(Component.translatable("avoidminer.jei.combined_chance", combined));
                    int dropPct = (int) Math.round(recipe.dropChance() * 100);
                    int succPct = (int) Math.round(recipe.successChance() * 100);
                    tooltip.add(Component.translatable("avoidminer.jei.detail_chance", dropPct, succPct));
                });
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, AvoidMinerJeiRecipe recipe, IFocusGroup focuses) {
        Component modeLabel = Component.translatable(recipe.worldMode());
        builder.addText(Component.translatable("avoidminer.jei.tier_mode_short", recipe.tier(), modeLabel), 90, 9)
                .setPosition(25, 1);
        builder.addAnimatedRecipeArrow(200).setPosition(30, 10);
    }
}