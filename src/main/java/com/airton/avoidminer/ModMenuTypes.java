package com.airton.avoidminer;

import com.airton.avoidminer.menu.AvoidMinerMenu;
import com.airton.avoidminer.menu.BatteryMenu;
import com.airton.avoidminer.menu.LootrMenu;
import com.airton.avoidminer.menu.ProcessorMenu;
import com.airton.avoidminer.menu.MagnetiteFurnaceMenu;
import com.airton.avoidminer.menu.XpVaultMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, AvoidMiner.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AvoidMinerMenu>> AVOID_MINER = MENUS.register("avoid_miner",
            () -> new MenuType<>(AvoidMinerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    // O processador tem contagem de slots por tier: o cliente precisa do BlockPos
    // enviado pelo servidor para montar o menu com o tier correto.
    public static final DeferredHolder<MenuType<?>, MenuType<ProcessorMenu>> PROCESSOR = MENUS.register("processor",
            () -> IMenuTypeExtension.create(ProcessorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MagnetiteFurnaceMenu>> MAGNETITE_FURNACE =
            MENUS.register("magnetite_furnace", () -> IMenuTypeExtension.create(MagnetiteFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<LootrMenu>> LOOTR = MENUS.register("lootr",
            () -> new MenuType<>(LootrMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<MenuType<?>, MenuType<BatteryMenu>> BATTERY = MENUS.register("battery",
            () -> new MenuType<>(BatteryMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<MenuType<?>, MenuType<com.airton.avoidminer.menu.MagnetiteBarrelMenu>> MAGNETITE_BARREL =
            MENUS.register("magnetite_barrel",
                    () -> new MenuType<>(com.airton.avoidminer.menu.MagnetiteBarrelMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<MenuType<?>, MenuType<XpVaultMenu>> XP_VAULT = MENUS.register("xp_vault",
            () -> new MenuType<>(XpVaultMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
