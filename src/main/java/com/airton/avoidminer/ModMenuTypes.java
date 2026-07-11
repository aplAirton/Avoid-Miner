package com.airton.avoidminer;

import com.airton.avoidminer.menu.AvoidMinerMenu;
import com.airton.avoidminer.menu.ProcessorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, AvoidMiner.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AvoidMinerMenu>> AVOID_MINER = MENUS.register("avoid_miner",
            () -> new MenuType<>(AvoidMinerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<MenuType<?>, MenuType<ProcessorMenu>> PROCESSOR = MENUS.register("processor",
            () -> new MenuType<>(ProcessorMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
