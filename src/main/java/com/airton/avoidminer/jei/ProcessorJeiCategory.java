package com.airton.avoidminer.jei;

import com.airton.avoidminer.ModBlocks;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessorJeiCategory extends AbstractRecipeCategory<ProcessorJeiRecipe> {
    public static final IRecipeType<ProcessorJeiRecipe> TYPE =
            IRecipeType.create(Identifier.parse("avoidminer:processor"), ProcessorJeiRecipe.class);

    public ProcessorJeiCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("avoidminers.jei.processor.category"),
                guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.AVOID_PROCESSOR_TIER_3.get())),
                100, 40);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ProcessorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 12)
                .add(recipe.input());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 70, 12)
                .add(recipe.output());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, ProcessorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(200).setPosition(32, 12);
    }

    public static List<ProcessorJeiRecipe> generateRecipes() {
        List<ProcessorJeiRecipe> recipes = new ArrayList<>();
        for (Map.Entry<net.minecraft.world.item.Item, ItemStack> entry : ProcessorBlockEntity.getRecipeMap().entrySet()) {
            ItemStack input = new ItemStack(entry.getKey());
            ItemStack output = entry.getValue().copy();
            recipes.add(new ProcessorJeiRecipe(input, output));
        }
        return recipes;
    }
}