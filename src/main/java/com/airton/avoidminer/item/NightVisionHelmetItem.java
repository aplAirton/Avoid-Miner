package com.airton.avoidminer.item;

import com.airton.avoidminer.AvoidMiner;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class NightVisionHelmetItem extends Item {
    private static final TagKey<Item> REPAIR_INGREDIENTS = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "repairs_night_vision_helmet")
    );
    private static final ArmorMaterial MATERIAL = new ArmorMaterial(
            NightVisionHelmetRules.DURABILITY_MULTIPLIER,
            ArmorMaterials.DIAMOND.defense(),
            10,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            NightVisionHelmetRules.TOUGHNESS,
            0.0F,
            REPAIR_INGREDIENTS,
            EquipmentAssets.DIAMOND
    );

    public NightVisionHelmetItem(Properties properties) {
        super(properties.humanoidArmor(MATERIAL, ArmorType.HELMET)
                .rarity(Rarity.UNCOMMON));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (slot != EquipmentSlot.HEAD || !(entity instanceof LivingEntity living)) {
            return;
        }

        MobEffectInstance current = living.getEffect(MobEffects.NIGHT_VISION);
        if (current != null && !isHelmetEffect(current)) {
            return;
        }
        if (current == null || current.getDuration() <= NightVisionHelmetRules.EFFECT_REFRESH_THRESHOLD) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    NightVisionHelmetRules.EFFECT_DURATION_TICKS,
                    0,
                    true,
                    false,
                    true
            ));
        }
    }

    public static boolean isHelmetEffect(MobEffectInstance effect) {
        return effect.is(MobEffects.NIGHT_VISION)
                && effect.isAmbient()
                && effect.getAmplifier() == 0
                && effect.getDuration() <= NightVisionHelmetRules.EFFECT_DURATION_TICKS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.night_vision_helmet.effect")
                .withStyle(ChatFormatting.GOLD));
        builder.accept(Component.translatable("tooltip.avoidminer.night_vision_helmet.stats",
                        NightVisionHelmetRules.HELMET_DURABILITY)
                .withStyle(ChatFormatting.GRAY));
    }
}
