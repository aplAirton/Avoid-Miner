package com.airton.avoidminer.block.entity;

import com.airton.avoidminer.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class VillagerHubBlockEntity extends BlockEntity {
    private static final double SCAN_RADIUS = 32.0;
    private static final int SCAN_INTERVAL = 100;
    private int scanTimer = 0;
    private List<Villager> cachedVillagers = List.of();

    public VillagerHubBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VILLAGER_HUB.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VillagerHubBlockEntity be) {
        if (level.isClientSide()) return;
        be.scanTimer++;
        if (be.scanTimer >= SCAN_INTERVAL) {
            be.scanTimer = 0;
            var box = new AABB(pos).inflate(SCAN_RADIUS);
            be.cachedVillagers = level.getEntitiesOfClass(Villager.class, box,
                    v -> v.isAlive() && !v.isBaby());
        }
    }

    public void openMenu(ServerPlayer player) {
        if (cachedVillagers.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatable("message.avoidminer.villager_hub.no_villagers"));
            return;
        }
        var activeVillagers = cachedVillagers.stream()
                .filter(v -> v.isAlive() && !v.getOffers().isEmpty())
                .toList();
        if (activeVillagers.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatable("message.avoidminer.villager_hub.no_villagers"));
            return;
        }
        var merchant = new ConsolidatedMerchant(activeVillagers);
        merchant.setTradingPlayer(player);
        merchant.openTradingScreen(player,
                Component.translatable("block.avoidminer.villager_hub"),
                1);
    }

    private record ScoredOffer(Villager villager, int index, MerchantOffer offer) {}

    private record VillagerTrade(Villager villager, int index) {}

    private static class ConsolidatedMerchant implements Merchant {
        private final Map<MerchantOffer, VillagerTrade> offerToVillager = new IdentityHashMap<>();
        private final MerchantOffers combinedOffers;
        private Player tradingPlayer;

        ConsolidatedMerchant(List<Villager> villagers) {
            var scored = new ArrayList<ScoredOffer>();
            for (var v : villagers) {
                var offers = v.getOffers();
                for (int i = 0; i < offers.size(); i++) {
                    scored.add(new ScoredOffer(v, i, offers.get(i)));
                }
            }
            scored.sort(Comparator.comparing(
                    so -> BuiltInRegistries.ITEM.getKey(so.offer().getResult().getItem())));
            combinedOffers = new MerchantOffers();
            for (var so : scored) {
                combinedOffers.add(so.offer());
                offerToVillager.put(so.offer(), new VillagerTrade(so.villager(), so.index()));
            }
        }

        @Override
        public void setTradingPlayer(Player player) {
            this.tradingPlayer = player;
        }

        @Override
        public Player getTradingPlayer() {
            return tradingPlayer;
        }

        @Override
        public MerchantOffers getOffers() {
            return combinedOffers;
        }

        @Override
        public void overrideOffers(MerchantOffers offers) {
        }

        @Override
        public void notifyTrade(MerchantOffer offer) {
            var trade = offerToVillager.get(offer);
            if (trade != null) {
                trade.villager().notifyTrade(offer);
            }
        }

        @Override
        public void notifyTradeUpdated(ItemStack stack) {
        }

        @Override
        public int getVillagerXp() {
            return 0;
        }

        @Override
        public void overrideXp(int xp) {
        }

        @Override
        public boolean showProgressBar() {
            return false;
        }

        @Override
        public SoundEvent getNotifyTradeSound() {
            return null;
        }

        @Override
        public boolean isClientSide() {
            return false;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public boolean canRestock() {
            return false;
        }
    }
}
