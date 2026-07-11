package com.airton.avoidminer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class GuideBookItem extends Item {
    public GuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            Minecraft.getInstance().setScreen(new BookViewScreen(buildBookAccess()));
        }
        return InteractionResult.SUCCESS;
    }

    private BookViewScreen.BookAccess buildBookAccess() {
        List<Component> pages = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {
            pages.add(Component.translatable("guide.avoidminer.page_" + i));
        }

        return new BookViewScreen.BookAccess(pages);
    }
}
