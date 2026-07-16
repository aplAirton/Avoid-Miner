package com.airton.avoidminer;

import com.airton.avoidminer.event.ResonantMiningManager;
import com.airton.avoidminer.screen.AvoidMinerScreen;
import com.airton.avoidminer.screen.BatteryScreen;
import com.airton.avoidminer.screen.LootrScreen;
import com.airton.avoidminer.screen.ProcessorScreen;
import com.airton.avoidminer.screen.MagnetiteFurnaceScreen;
import com.airton.avoidminer.screen.XpVaultScreen;
import net.minecraft.client.particle.SonicBoomParticle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.GOLDEN_SONIC_BOOM.get(), sprites ->
                (options, level, x, y, z, xSpeed, ySpeed, zSpeed, random) -> {
                    SonicBoomParticle particle = new SonicBoomParticle(level, x, y, z, 0.0, sprites);
                    particle.setColor(1.0F, 0.68F, 0.08F);
                    return particle;
                });
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.AVOID_MINER.get(), AvoidMinerScreen::new);
        event.register(ModMenuTypes.PROCESSOR.get(), ProcessorScreen::new);
        event.register(ModMenuTypes.MAGNETITE_FURNACE.get(), MagnetiteFurnaceScreen::new);
        event.register(ModMenuTypes.LOOTR.get(), LootrScreen::new);
        event.register(ModMenuTypes.BATTERY.get(), BatteryScreen::new);
        event.register(ModMenuTypes.MAGNETITE_BARREL.get(), com.airton.avoidminer.screen.MagnetiteBarrelScreen::new);
        event.register(ModMenuTypes.XP_VAULT.get(), XpVaultScreen::new);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) return;

        var tooltip = event.getToolTip();
        var lookup = event.getEntity().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack stack = event.getItemStack();

        var allComponents = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        var storedComponents = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        for (var entry : allComponents.entrySet()) {
            var holder = entry.getKey();
            Identifier id = Identifier.parse(holder.getRegisteredName());
            if (!id.getNamespace().equals(AvoidMiner.MODID)) continue;
            String descKey = id.toLanguageKey("enchantment") + ".desc";
            tooltip.add(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GREEN));
        }
        for (var entry : storedComponents.entrySet()) {
            var holder = entry.getKey();
            Identifier id = Identifier.parse(holder.getRegisteredName());
            if (!id.getNamespace().equals(AvoidMiner.MODID)) continue;
            String descKey = id.toLanguageKey("enchantment") + ".desc";
            tooltip.add(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GREEN));
        }

        if (stack.is(ModItems.RESONANT_PICKAXE.get())) return;
        var enchantment = lookup.get(ResonantMiningManager.RESONANT_MINER)
                .orElse(null);
        if (enchantment == null) return;
        if (allComponents.getLevel(enchantment) > 0 || storedComponents.getLevel(enchantment) > 0) {
            tooltip.add(Component.translatable("tooltip.avoidminer.resonant_mining.lava")
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
