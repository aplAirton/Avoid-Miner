package com.airton.avoidminer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

// Isolado do GuideBookItem: só pode ser carregado no cliente (referencia classes
// que não existem no servidor dedicado)
final class ClientGuideBook {
    private ClientGuideBook() {}

    static void open() {
        Minecraft.getInstance().setScreen(new BookViewScreen(buildBookAccess()));
    }

    private static BookViewScreen.BookAccess buildBookAccess() {
        List<Component> pages = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            pages.add(Component.translatable("guide.avoidminer.page_" + i));
        }
        return new BookViewScreen.BookAccess(pages);
    }
}
