package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class VillagerLassoItem extends Item {
    private static final String KEY_TYPE = "VillagerType";
    private static final String KEY_PROFESSION = "Profession";
    private static final String KEY_LEVEL = "Level";
    private static final String KEY_XP = "Xp";
    private static final String KEY_CUSTOM_NAME = "CustomName";

    public VillagerLassoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Villager villager && !player.level().isClientSide()
                && villager.isAlive() && !hasStoredData(stack)) {
            var data = villager.getVillagerData();
            var tag = new CompoundTag();
            tag.putString(KEY_TYPE, data.type().unwrapKey().orElseThrow().identifier().toString());
            tag.putString(KEY_PROFESSION, data.profession().unwrapKey().orElseThrow().identifier().toString());
            tag.putInt(KEY_LEVEL, data.level());
            tag.putInt(KEY_XP, villager.getVillagerXp());
            var customName = villager.getCustomName();
            if (customName != null) {
                tag.putString(KEY_CUSTOM_NAME, customName.getString());
            }
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            villager.discard();
            player.level().playSound(null, villager.blockPosition(), SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var stack = context.getItemInHand();
        var level = context.getLevel();
        if (player != null && hasStoredData(stack) && !level.isClientSide()) {
            var tag = stack.get(DataComponents.CUSTOM_DATA);
            if (tag != null && !tag.isEmpty()) {
                var root = tag.copyTag();
                var typeStr = root.getStringOr(KEY_TYPE, "");
                var profStr = root.getStringOr(KEY_PROFESSION, "");
                if (typeStr.isEmpty() || profStr.isEmpty()) return InteractionResult.PASS;
                var villager = new Villager(
                        net.minecraft.world.entity.EntityType.VILLAGER, level);
                var typeKey = ResourceKey.create(
                        Registries.VILLAGER_TYPE,
                        Identifier.parse(typeStr));
                var profKey = ResourceKey.create(
                        Registries.VILLAGER_PROFESSION,
                        Identifier.parse(profStr));
                var registryAccess = level.registryAccess();
                var type = registryAccess.lookupOrThrow(
                        Registries.VILLAGER_TYPE).getOrThrow(typeKey);
                var prof = registryAccess.lookupOrThrow(
                        Registries.VILLAGER_PROFESSION).getOrThrow(profKey);
                villager.setVillagerData(new VillagerData(type, prof, root.getIntOr(KEY_LEVEL, 1)));
                villager.setVillagerXp(root.getIntOr(KEY_XP, 0));
                if (root.contains(KEY_CUSTOM_NAME)) {
                    villager.setCustomName(Component.literal(root.getStringOr(KEY_CUSTOM_NAME, "")));
                }
                var pos = context.getClickedPos().relative(context.getClickedFace());
                villager.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                level.addFreshEntity(villager);
                stack.shrink(1);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean hasStoredData(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA) && !stack.get(DataComponents.CUSTOM_DATA).isEmpty();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        if (hasStoredData(stack)) {
            builder.accept(Component.translatable("tooltip.avoidminer.villager_lasso.filled")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            builder.accept(Component.translatable("tooltip.avoidminer.villager_lasso.empty")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
