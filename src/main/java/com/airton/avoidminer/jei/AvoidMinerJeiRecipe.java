package com.airton.avoidminer.jei;

import net.minecraft.world.item.ItemStack;

public record AvoidMinerJeiRecipe(ItemStack machine, int tier, ItemStack output, double dropChance, double successChance, String worldMode) {
}