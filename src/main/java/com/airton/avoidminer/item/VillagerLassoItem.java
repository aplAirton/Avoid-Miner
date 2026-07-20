package com.airton.avoidminer.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.function.Consumer;

public class VillagerLassoItem extends Item {
    private static final String KEY_VILLAGER_DATA = "VillagerData";

    public VillagerLassoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Villager villager && !player.level().isClientSide()
                && villager.isAlive() && !hasStoredData(stack)) {
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,
                    player.level().registryAccess());
            villager.saveWithoutId(output);
            var saveTag = output.buildResult();
            saveTag.remove("Pos");
            saveTag.remove("Motion");
            saveTag.remove("Rotation");
            saveTag.remove("FallDistance");
            saveTag.remove("Fire");
            saveTag.remove("Air");
            saveTag.remove("OnGround");
            saveTag.remove("PortalCooldown");
            saveTag.remove("Dimension");
            saveTag.remove("Passengers");
            saveTag.remove("Leash");
            saveTag.remove("Invulnerable");
            saveTag.remove("Brain");
            saveTag.remove("Gossips");
            var wrapper = new CompoundTag();
            wrapper.put(KEY_VILLAGER_DATA, saveTag);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(wrapper));
            stack.hurtAndBreak(1, player, hand);
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
                var saveTagOpt = root.getCompound(KEY_VILLAGER_DATA);
                if (saveTagOpt.isEmpty()) {
                    return InteractionResult.PASS;
                }
                var saveTag = saveTagOpt.get();
                var villager = new Villager(
                        net.minecraft.world.entity.EntityType.VILLAGER, level);
                var input = TagValueInput.create(ProblemReporter.DISCARDING,
                        level.registryAccess(), saveTag);
                villager.load(input);
                var pos = context.getClickedPos().relative(context.getClickedFace());
                villager.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                level.addFreshEntity(villager);
                stack.remove(DataComponents.CUSTOM_DATA);
                stack.hurtAndBreak(1, player, player.getUsedItemHand());
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
