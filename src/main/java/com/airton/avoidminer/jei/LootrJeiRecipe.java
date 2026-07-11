package com.airton.avoidminer.jei;

import com.airton.avoidminer.lootr.MobCardType;
import net.minecraft.world.item.ItemStack;

public record LootrJeiRecipe(MobCardType cardType, ItemStack cardItem) {
}
