package com.airton.avoidminer.jei;

import net.minecraft.world.item.ItemStack;

public record ProcessorJeiRecipe(ItemStack input, ItemStack output, String multiplier) {
}
