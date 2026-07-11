package com.airton.avoidminer.jei;

import com.airton.avoidminer.ModBlocks;
import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class AvoidMinerJeiPlugin implements IModPlugin {
    public static final Identifier PLUGIN_ID = Identifier.parse("avoidminer:jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new AvoidMinerCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ProcessorJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<AvoidMinerJeiRecipe> minerRecipes = new ArrayList<>();
        ItemStack[] machines = {
                new ItemStack(ModBlocks.AVOID_MINER_TIER_1.get()),
                new ItemStack(ModBlocks.AVOID_MINER_TIER_2.get()),
                new ItemStack(ModBlocks.AVOID_MINER_TIER_3.get())
        };
        float[] chances = {0.30f, 0.40f, 0.60f};
        for (int t = 0; t < 3; t++) {
            AvoidMinerBlockEntity.Tier tier = AvoidMinerBlockEntity.Tier.values()[t];

            // Overworld
            int totalWeight = tier.resources.get().stream().mapToInt(AvoidMinerBlockEntity.WeightedResource::weight).sum();
            for (var wr : tier.resources.get()) {
                double dropPct = (double) wr.weight() / totalWeight;
                minerRecipes.add(new AvoidMinerJeiRecipe(machines[t], t + 1, wr.stack().copy(), dropPct, chances[t], "screen.avoidminer.mode.overworld"));
            }

            // Botany
            int totalBotWeight = tier.botanyResources.get().stream().mapToInt(AvoidMinerBlockEntity.WeightedResource::weight).sum();
            for (var wr : tier.botanyResources.get()) {
                double dropPct = (double) wr.weight() / totalBotWeight;
                minerRecipes.add(new AvoidMinerJeiRecipe(machines[t], t + 1, wr.stack().copy(), dropPct, chances[t], "screen.avoidminer.mode.botany"));
            }

            // Nether
            int totalNetherWeight = tier.netherResources.get().stream().mapToInt(AvoidMinerBlockEntity.WeightedResource::weight).sum();
            for (var wr : tier.netherResources.get()) {
                double dropPct = (double) wr.weight() / totalNetherWeight;
                minerRecipes.add(new AvoidMinerJeiRecipe(machines[t], t + 1, wr.stack().copy(), dropPct, chances[t], "screen.avoidminer.mode.nether"));
            }
        }
        registration.addRecipes(AvoidMinerCategory.TYPE, minerRecipes);

        registration.addRecipes(ProcessorJeiCategory.TYPE, ProcessorJeiCategory.generateRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(AvoidMinerCategory.TYPE, new ItemStack(ModBlocks.AVOID_MINER_TIER_1.get()));
        registration.addCraftingStation(AvoidMinerCategory.TYPE, new ItemStack(ModBlocks.AVOID_MINER_TIER_2.get()));
        registration.addCraftingStation(AvoidMinerCategory.TYPE, new ItemStack(ModBlocks.AVOID_MINER_TIER_3.get()));

        registration.addCraftingStation(ProcessorJeiCategory.TYPE, new ItemStack(ModBlocks.AVOID_PROCESSOR_TIER_1.get()));
        registration.addCraftingStation(ProcessorJeiCategory.TYPE, new ItemStack(ModBlocks.AVOID_PROCESSOR_TIER_2.get()));
        registration.addCraftingStation(ProcessorJeiCategory.TYPE, new ItemStack(ModBlocks.AVOID_PROCESSOR_TIER_3.get()));
    }
}
