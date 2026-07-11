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
                140, 50);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AvoidMinerJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 1, 16)
                .add(recipe.machine());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 60, 16)
                .add(recipe.output())
                .addRichTooltipCallback((slot, tooltip) -> {
                    int dropPct = (int) Math.round(recipe.dropChance() * 100);
                    int succPct = (int) Math.round(recipe.successChance() * 100);
                    int combined = (int) Math.round(recipe.dropChance() * recipe.successChance() * 100);
                    tooltip.add(Component.translatable("avoidminer.jei.weight", dropPct));
                    tooltip.add(Component.translatable("avoidminer.jei.base_chance", succPct));
                    tooltip.add(Component.translatable("avoidminer.jei.per_cycle", combined));
                });
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, AvoidMinerJeiRecipe recipe, IFocusGroup focuses) {
        int dropPct = (int) Math.round(recipe.dropChance() * 100);
        int succPct = (int) Math.round(recipe.successChance() * 100);
        int combined = (int) Math.round(recipe.dropChance() * recipe.successChance() * 100);

        builder.addText(Component.translatable("avoidminer.jei.tier_mode", recipe.tier(), Component.translatable(recipe.worldMode())), 80, 10)
                .setPosition(20, 2);
        builder.addText(Component.translatable("avoidminer.jei.weight", dropPct), 60, 10)
                .setPosition(80, 8);
        builder.addText(Component.translatable("avoidminer.jei.chance", succPct), 60, 10)
                .setPosition(80, 20);
        builder.addText(Component.translatable("avoidminer.jei.cycle_result", combined), 60, 10)
                .setPosition(80, 32);
        builder.addAnimatedRecipeArrow(200).setPosition(28, 16);
    }
}
