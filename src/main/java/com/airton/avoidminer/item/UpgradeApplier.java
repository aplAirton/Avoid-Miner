package com.airton.avoidminer.item;

import com.airton.avoidminer.block.entity.AvoidMinerBlockEntity;
import com.airton.avoidminer.block.entity.LootrBlockEntity;
import com.airton.avoidminer.block.entity.ProcessorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/**
 * Aplicação direta de melhorias/cartões: agachado + clique direito numa
 * máquina do mod move 1 item da mão para o primeiro slot dedicado válido e
 * vazio, sem abrir a GUI. Toda a mutação acontece no servidor — o stack da
 * mão só encolhe depois de a inserção ser confirmada, evitando duplicação.
 */
public final class UpgradeApplier {
    private UpgradeApplier() {}

    public static InteractionResult tryApply(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isSecondaryUseActive()) return InteractionResult.PASS;

        Level level = context.getLevel();
        ResourceHandler<ItemResource> handler = machineHandler(level.getBlockEntity(context.getClickedPos()));
        if (handler == null) return InteractionResult.PASS;

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack stack = context.getItemInHand();
        ItemResource resource = ItemResource.of(stack);
        for (int slot = 0; slot < handler.size(); slot++) {
            if (!handler.getResource(slot).isEmpty()) continue;
            if (!handler.isValid(slot, resource)) continue;
            try (Transaction tx = Transaction.openRoot()) {
                if (handler.insert(slot, resource, 1, tx) == 1) {
                    tx.commit();
                    stack.shrink(1);
                    level.playSound(null, context.getClickedPos(), SoundEvents.ITEM_FRAME_ADD_ITEM,
                            SoundSource.BLOCKS, 0.7f, 1.1f);
                    player.sendOverlayMessage(
                            Component.translatable("msg.avoidminer.upgrade.applied", resource.toStack(1).getHoverName()));
                    return InteractionResult.SUCCESS;
                }
            }
        }

        player.sendOverlayMessage(Component.translatable("msg.avoidminer.upgrade.no_slot"));
        return InteractionResult.SUCCESS;
    }

    @Nullable
    private static ResourceHandler<ItemResource> machineHandler(@Nullable BlockEntity be) {
        if (be instanceof AvoidMinerBlockEntity miner) return miner.getItemHandler();
        if (be instanceof ProcessorBlockEntity processor) return processor.getItemHandler();
        if (be instanceof LootrBlockEntity lootr) return lootr.getItemHandler();
        return null;
    }
}
