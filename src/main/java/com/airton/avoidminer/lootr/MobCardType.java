package com.airton.avoidminer.lootr;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Define os cartões de mob do Avoid Lootr: mob alvo, abates necessários e as
 * tabelas de drops (normais e excepcionais). Usado pelo item do cartão, pela
 * máquina e pelo JEI — fonte única de verdade.
 */
public enum MobCardType {
    SKELETON("skeleton", EntityType.SKELETON, 10),
    WITHER_SKELETON("wither_skeleton", EntityType.WITHER_SKELETON, 10),
    CREEPER("creeper", EntityType.CREEPER, 10),
    ZOMBIE("zombie", EntityType.ZOMBIE, 10),
    SPIDER("spider", EntityType.SPIDER, 10),
    WITCH("witch", EntityType.WITCH, 10),
    PIGLIN("piglin", EntityType.PIGLIN, 10),
    ENDER_DRAGON("ender_dragon", EntityType.ENDER_DRAGON, 3);

    public final String id;
    public final EntityType<?> entityType;
    public final int requiredKills;

    MobCardType(String id, EntityType<?> entityType, int requiredKills) {
        this.id = id;
        this.entityType = entityType;
        this.requiredKills = requiredKills;
    }

    /** Drop comum: chance por operação, faixa de quantidade, e se escala com Saque. */
    public record LootEntry(Item item, float chance, int min, int max, boolean lootingScales) {}

    /** Drop excepcional: só sai com a Melhoria de Raridade instalada. */
    public record RareEntry(Item item, float chance) {}

    public List<LootEntry> normalDrops() {
        return switch (this) {
            case SKELETON -> List.of(
                new LootEntry(Items.BONE, 1.0f, 1, 2, true),
                new LootEntry(Items.ARROW, 0.8f, 1, 2, true),
                new LootEntry(Items.BOW, 0.085f, 1, 1, false)
            );
            case WITHER_SKELETON -> List.of(
                new LootEntry(Items.BONE, 1.0f, 1, 2, true),
                new LootEntry(Items.COAL, 0.6f, 1, 1, true),
                new LootEntry(Items.STONE_SWORD, 0.08f, 1, 1, false)
            );
            case CREEPER -> List.of(
                new LootEntry(Items.GUNPOWDER, 1.0f, 1, 2, true)
            );
            case ZOMBIE -> List.of(
                new LootEntry(Items.ROTTEN_FLESH, 1.0f, 1, 2, true),
                new LootEntry(Items.IRON_INGOT, 0.025f, 1, 1, false),
                new LootEntry(Items.CARROT, 0.025f, 1, 1, false),
                new LootEntry(Items.POTATO, 0.025f, 1, 1, false)
            );
            case SPIDER -> List.of(
                new LootEntry(Items.STRING, 1.0f, 1, 2, true),
                new LootEntry(Items.SPIDER_EYE, 0.33f, 1, 1, true)
            );
            case WITCH -> List.of(
                new LootEntry(Items.GLOWSTONE_DUST, 0.5f, 1, 2, true),
                new LootEntry(Items.REDSTONE, 0.5f, 1, 2, true),
                new LootEntry(Items.SUGAR, 0.5f, 1, 2, true),
                new LootEntry(Items.GLASS_BOTTLE, 0.5f, 1, 2, true),
                new LootEntry(Items.STICK, 0.5f, 1, 2, true),
                new LootEntry(Items.GLISTERING_MELON_SLICE, 0.1f, 1, 1, false)
            );
            case PIGLIN -> List.of(
                new LootEntry(Items.GOLD_NUGGET, 1.0f, 1, 3, true),
                new LootEntry(Items.GOLD_INGOT, 0.1f, 1, 1, false)
            );
            case ENDER_DRAGON -> List.of(
                new LootEntry(Items.DRAGON_BREATH, 1.0f, 1, 2, true)
            );
        };
    }

    public List<RareEntry> rareDrops() {
        return switch (this) {
            case SKELETON -> List.of(
                new RareEntry(Items.SKELETON_SKULL, 0.025f),
                new RareEntry(Items.SKELETON_SPAWN_EGG, 0.0015f)
            );
            case WITHER_SKELETON -> List.of(
                new RareEntry(Items.WITHER_SKELETON_SKULL, 0.025f),
                new RareEntry(Items.WITHER_SKELETON_SPAWN_EGG, 0.0015f)
            );
            case CREEPER -> List.of(
                new RareEntry(Items.CREEPER_HEAD, 0.025f),
                new RareEntry(Items.MUSIC_DISC_13, 0.015f),
                new RareEntry(Items.MUSIC_DISC_CAT, 0.015f),
                new RareEntry(Items.CREEPER_SPAWN_EGG, 0.0015f)
            );
            case ZOMBIE -> List.of(
                new RareEntry(Items.ZOMBIE_HEAD, 0.025f),
                new RareEntry(Items.ZOMBIE_SPAWN_EGG, 0.0015f)
            );
            case SPIDER -> List.of(
                new RareEntry(Items.COBWEB, 0.05f),
                new RareEntry(Items.SPIDER_SPAWN_EGG, 0.0015f)
            );
            case WITCH -> List.of(
                new RareEntry(Items.WITCH_SPAWN_EGG, 0.0015f)
            );
            case PIGLIN -> List.of(
                new RareEntry(Items.PIGLIN_HEAD, 0.025f),
                new RareEntry(Items.PIGLIN_SPAWN_EGG, 0.0015f)
            );
            case ENDER_DRAGON -> List.of(
                new RareEntry(Items.DRAGON_HEAD, 0.05f),
                new RareEntry(Items.DRAGON_EGG, 0.005f),
                new RareEntry(Items.ENDER_DRAGON_SPAWN_EGG, 0.0005f)
            );
        };
    }
}
