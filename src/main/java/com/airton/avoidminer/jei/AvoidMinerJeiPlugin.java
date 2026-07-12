package com.airton.avoidminer.jei;

import com.airton.avoidminer.ModBlocks;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity;
import com.airton.avoidminer.lootr.MobCardType;
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
        registration.addRecipeCategories(new LootrJeiCategory(registration.getJeiHelpers().getGuiHelper()));
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

            // End
            int totalEndWeight = tier.endResources.get().stream().mapToInt(AvoidMinerBlockEntity.WeightedResource::weight).sum();
            for (var wr : tier.endResources.get()) {
                double dropPct = (double) wr.weight() / totalEndWeight;
                minerRecipes.add(new AvoidMinerJeiRecipe(machines[t], t + 1, wr.stack().copy(), dropPct, chances[t], "screen.avoidminer.mode.end"));
            }
        }

        // Upgrade drops (nether mode, ~0.5% per cycle)
        minerRecipes.add(new AvoidMinerJeiRecipe(machines[0], 1,
                new ItemStack(ModItems.UPGRADE_TIER_2.get()), 0.005, 1.0, "screen.avoidminer.mode.nether"));
        minerRecipes.add(new AvoidMinerJeiRecipe(machines[1], 2,
                new ItemStack(ModItems.UPGRADE_TIER_3.get()), 0.005, 1.0, "screen.avoidminer.mode.nether"));

        registration.addRecipes(AvoidMinerCategory.TYPE, minerRecipes);

        registration.addRecipes(ProcessorJeiCategory.TYPE, ProcessorJeiCategory.generateRecipes());

        List<LootrJeiRecipe> lootrRecipes = new ArrayList<>();
        for (MobCardType type : MobCardType.values()) {
            ItemStack card = switch (type) {
                case SKELETON -> new ItemStack(ModItems.SKELETON_CARD.get());
                case WITHER_SKELETON -> new ItemStack(ModItems.WITHER_SKELETON_CARD.get());
                case CREEPER -> new ItemStack(ModItems.CREEPER_CARD.get());
                case ZOMBIE -> new ItemStack(ModItems.ZOMBIE_CARD.get());
                case SPIDER -> new ItemStack(ModItems.SPIDER_CARD.get());
                case WITCH -> new ItemStack(ModItems.WITCH_CARD.get());
                case PIGLIN -> new ItemStack(ModItems.PIGLIN_CARD.get());
                case PIGLIN_BRUTE -> new ItemStack(ModItems.PIGLIN_BRUTE_CARD.get());
                case ENDERMAN -> new ItemStack(ModItems.ENDERMAN_CARD.get());
                case VILLAGER -> new ItemStack(ModItems.VILLAGER_CARD.get());
                case BLAZE -> new ItemStack(ModItems.BLAZE_CARD.get());
                case PIG -> new ItemStack(ModItems.PIG_CARD.get());
                case COW -> new ItemStack(ModItems.COW_CARD.get());
                case CHICKEN -> new ItemStack(ModItems.CHICKEN_CARD.get());
                case SHEEP -> new ItemStack(ModItems.SHEEP_CARD.get());
                case VEX -> new ItemStack(ModItems.VEX_CARD.get());
                case GUARDIAN -> new ItemStack(ModItems.GUARDIAN_CARD.get());
                case ELDER_GUARDIAN -> new ItemStack(ModItems.ELDER_GUARDIAN_CARD.get());
                case SLIME -> new ItemStack(ModItems.SLIME_CARD.get());
                case VINDICATOR -> new ItemStack(ModItems.VINDICATOR_CARD.get());
                case EVOKER -> new ItemStack(ModItems.EVOKER_CARD.get());
                case GHAST -> new ItemStack(ModItems.GHAST_CARD.get());
                case MAGMA_CUBE -> new ItemStack(ModItems.MAGMA_CUBE_CARD.get());
                case SHULKER -> new ItemStack(ModItems.SHULKER_CARD.get());
                case ENDER_DRAGON -> new ItemStack(ModItems.ENDER_DRAGON_CARD.get());
            };
            lootrRecipes.add(new LootrJeiRecipe(type, card));
        }
        registration.addRecipes(LootrJeiCategory.TYPE, lootrRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(AvoidMinerCategory.TYPE, new ItemStack(ModBlocks.AVOID_MINER_TIER_1.get()));
        registration.addCraftingStation(AvoidMinerCategory.TYPE, new ItemStack(ModBlocks.AVOID_MINER_TIER_2.get()));
        registration.addCraftingStation(AvoidMinerCategory.TYPE, new ItemStack(ModBlocks.AVOID_MINER_TIER_3.get()));

        registration.addCraftingStation(ProcessorJeiCategory.TYPE, new ItemStack(ModBlocks.AVOID_PROCESSOR_TIER_1.get()));
        registration.addCraftingStation(ProcessorJeiCategory.TYPE, new ItemStack(ModBlocks.AVOID_PROCESSOR_TIER_2.get()));
        registration.addCraftingStation(ProcessorJeiCategory.TYPE, new ItemStack(ModBlocks.AVOID_PROCESSOR_TIER_3.get()));

        registration.addCraftingStation(LootrJeiCategory.TYPE, new ItemStack(ModBlocks.AVOID_LOOTR.get()));
    }
}
