package com.airton.avoidminer.lootr;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Arrays;

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
    PIGLIN_BRUTE("piglin_brute", EntityType.PIGLIN_BRUTE, 10),
    ENDERMAN("enderman", EntityType.ENDERMAN, 10),
    VILLAGER("villager", EntityType.VILLAGER, 10),
    BLAZE("blaze", EntityType.BLAZE, 10),
    PIG("pig", EntityType.PIG, 5),
    COW("cow", EntityType.COW, 5),
    CHICKEN("chicken", EntityType.CHICKEN, 5),
    SHEEP("sheep", EntityType.SHEEP, 5),
    BREEZE("breeze", EntityType.BREEZE, 10),
    GUARDIAN("guardian", EntityType.GUARDIAN, 10),
    ELDER_GUARDIAN("elder_guardian", EntityType.ELDER_GUARDIAN, 5),
    SLIME("slime", EntityType.SLIME, 10),
    VINDICATOR("vindicator", EntityType.VINDICATOR, 10),
    EVOKER("evoker", EntityType.EVOKER, 5),
    GHAST("ghast", EntityType.GHAST, 10),
    MAGMA_CUBE("magma_cube", EntityType.MAGMA_CUBE, 10),
    SHULKER("shulker", EntityType.SHULKER, 10),
    ENDER_DRAGON("ender_dragon", EntityType.ENDER_DRAGON, 3),
    WITHER("wither", EntityType.WITHER, 3),
    WARDEN("warden", EntityType.WARDEN, 5),
    BOGGED("bogged", EntityType.BOGGED, 10),
    DROWNED("drowned", EntityType.DROWNED, 10),
    HOGLIN("hoglin", EntityType.HOGLIN, 5),
    PHANTOM("phantom", EntityType.PHANTOM, 10),
    PILLAGER("pillager", EntityType.PILLAGER, 10),
    STRAY("stray", EntityType.STRAY, 10);

    public final String id;
    public final EntityType<?> entityType;
    public final int requiredKills;

    MobCardType(String id, EntityType<?> entityType, int requiredKills) {
        this.id = id;
        this.entityType = entityType;
        this.requiredKills = requiredKills;
    }

    public static MobCardType byId(String id) {
        return Arrays.stream(values()).filter(type -> type.id.equals(id)).findFirst().orElse(null);
    }

    /** Drop comum: chance por operação, faixa de quantidade, e se escala com Saque. */
    public record LootEntry(Item item, float chance, int min, int max, boolean lootingScales) {}

    /** Drop excepcional: só sai com a Melhoria de Raridade instalada. */
    public record RareEntry(Item item, float chance) {}

    public List<LootEntry> normalDrops() {
        return switch (this) {
            case SKELETON -> List.of(
                new LootEntry(Items.BONE, 1.0f, 1, 2, true),
                new LootEntry(Items.ARROW, 0.8f, 1, 2, true)
            );
            case WITHER_SKELETON -> List.of(
                new LootEntry(Items.BONE, 1.0f, 1, 2, true),
                new LootEntry(Items.COAL, 0.6f, 1, 1, true),
                new LootEntry(Items.WITHER_SKELETON_SKULL, 0.1f, 1, 1, false)
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
            case PIGLIN_BRUTE -> List.of(
                new LootEntry(Items.GOLD_NUGGET, 1.0f, 1, 3, true),
                new LootEntry(Items.GOLD_INGOT, 0.15f, 1, 1, false)
            );
            case ENDERMAN -> List.of(
                new LootEntry(Items.ENDER_PEARL, 0.5f, 1, 1, true),
                new LootEntry(Items.ENDER_EYE, 0.1f, 1, 1, false)
            );
            case VILLAGER -> List.of(
                new LootEntry(Items.WHEAT_SEEDS, 0.15f, 1, 3, false),
                new LootEntry(Items.WHEAT, 0.1f, 1, 2, false),
                new LootEntry(Items.POTATO, 0.1f, 1, 1, false),
                new LootEntry(Items.CARROT, 0.1f, 1, 1, false),
                new LootEntry(Items.BOOK, 0.05f, 1, 1, false)
            );
            case BLAZE -> List.of(
                new LootEntry(Items.BLAZE_ROD, 1.0f, 1, 2, true)
            );
            case PIG -> List.of(
                new LootEntry(Items.PORKCHOP, 1.0f, 1, 2, true)
            );
            case COW -> List.of(
                new LootEntry(Items.BEEF, 1.0f, 1, 2, true),
                new LootEntry(Items.LEATHER, 0.5f, 1, 1, true)
            );
            case CHICKEN -> List.of(
                new LootEntry(Items.CHICKEN, 1.0f, 1, 2, true),
                new LootEntry(Items.FEATHER, 0.5f, 1, 2, true),
                new LootEntry(Items.EGG, 0.2f, 1, 1, false)
            );
            case SHEEP -> List.of(
                new LootEntry(Items.MUTTON, 1.0f, 1, 2, true),
                new LootEntry(Items.WHITE_WOOL, 0.5f, 1, 1, false)
            );
            case BREEZE -> List.of(
                new LootEntry(Items.BREEZE_ROD, 1.0f, 1, 2, true),
                new LootEntry(Items.WIND_CHARGE, 0.5f, 1, 2, true)
            );
            case GUARDIAN -> List.of(
                new LootEntry(Items.PRISMARINE_SHARD, 0.5f, 1, 2, true),
                new LootEntry(Items.COD, 0.5f, 1, 1, false)
            );
            case ELDER_GUARDIAN -> List.of(
                new LootEntry(Items.PRISMARINE_SHARD, 1.0f, 1, 2, true),
                new LootEntry(Items.PRISMARINE_CRYSTALS, 0.5f, 1, 1, false),
                new LootEntry(Items.WET_SPONGE, 0.1f, 1, 1, false)
            );
            case SLIME -> List.of(
                new LootEntry(Items.SLIME_BALL, 1.0f, 1, 2, true)
            );
            case VINDICATOR -> List.of(
                new LootEntry(Items.EMERALD, 0.5f, 1, 1, false),
                new LootEntry(Items.IRON_AXE, 0.1f, 1, 1, false)
            );
            case EVOKER -> List.of(
                new LootEntry(Items.TOTEM_OF_UNDYING, 0.25f, 1, 1, false),
                new LootEntry(Items.EMERALD, 1.0f, 1, 2, false)
            );
            case GHAST -> List.of(
                new LootEntry(Items.GHAST_TEAR, 0.5f, 1, 1, false),
                new LootEntry(Items.GUNPOWDER, 0.5f, 1, 2, true)
            );
            case MAGMA_CUBE -> List.of(
                new LootEntry(Items.MAGMA_CREAM, 1.0f, 1, 2, true)
            );
            case SHULKER -> List.of(
                new LootEntry(Items.SHULKER_SHELL, 0.5f, 1, 1, false)
            );
            case ENDER_DRAGON -> List.of(
                new LootEntry(Items.DRAGON_BREATH, 1.0f, 1, 2, true)
            );
            case WITHER -> List.of(
                new LootEntry(Items.NETHER_STAR, 1.0f, 1, 1, false)
            );
            case WARDEN -> List.of(
                new LootEntry(Items.SCULK, 1.0f, 1, 3, true),
                new LootEntry(Items.SCULK_SENSOR, 0.3f, 1, 1, false),
                new LootEntry(Items.ECHO_SHARD, 0.02f, 1, 1, false),
                new LootEntry(Items.DISC_FRAGMENT_5, 0.01f, 1, 1, false)
            );
            case BOGGED -> List.of(
                new LootEntry(Items.ARROW, 1.0f, 1, 2, true),
                new LootEntry(Items.BONE, 0.8f, 1, 2, true),
                new LootEntry(Items.TIPPED_ARROW, 0.15f, 1, 1, false)
            );
            case DROWNED -> List.of(
                new LootEntry(Items.ROTTEN_FLESH, 1.0f, 1, 2, true),
                new LootEntry(Items.COPPER_INGOT, 0.3f, 1, 1, false)
            );
            case HOGLIN -> List.of(
                new LootEntry(Items.LEATHER, 1.0f, 1, 2, true),
                new LootEntry(Items.PORKCHOP, 1.0f, 1, 2, true)
            );
            case PHANTOM -> List.of(
                new LootEntry(Items.PHANTOM_MEMBRANE, 1.0f, 1, 1, true)
            );
            case PILLAGER -> List.of(
                new LootEntry(Items.ARROW, 0.8f, 1, 2, true),
                new LootEntry(Items.EMERALD, 0.3f, 1, 1, false)
            );
            case STRAY -> List.of(
                new LootEntry(Items.BONE, 1.0f, 1, 2, true),
                new LootEntry(Items.ARROW, 0.8f, 1, 2, true)
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
            case PIGLIN_BRUTE -> List.of(
                new RareEntry(Items.PIGLIN_BRUTE_SPAWN_EGG, 0.0015f)
            );
            case ENDERMAN -> List.of(
                new RareEntry(Items.ENDERMAN_SPAWN_EGG, 0.0015f)
            );
            case VILLAGER -> List.of(
                new RareEntry(Items.VILLAGER_SPAWN_EGG, 0.0015f)
            );
            case BLAZE -> List.of(
                new RareEntry(Items.BLAZE_SPAWN_EGG, 0.0015f)
            );
            case PIG -> List.of(
                new RareEntry(Items.PIG_SPAWN_EGG, 0.0015f)
            );
            case COW -> List.of(
                new RareEntry(Items.COW_SPAWN_EGG, 0.0015f)
            );
            case CHICKEN -> List.of(
                new RareEntry(Items.CHICKEN_SPAWN_EGG, 0.0015f)
            );
            case SHEEP -> List.of(
                new RareEntry(Items.SHEEP_SPAWN_EGG, 0.0015f)
            );
            case BREEZE -> List.of(
                new RareEntry(Items.BREEZE_SPAWN_EGG, 0.0015f),
                new RareEntry(Items.HEAVY_CORE, 0.0005f)
            );
            case GUARDIAN -> List.of(
                new RareEntry(Items.GUARDIAN_SPAWN_EGG, 0.0015f)
            );
            case ELDER_GUARDIAN -> List.of(
                new RareEntry(Items.ELDER_GUARDIAN_SPAWN_EGG, 0.0015f)
            );
            case SLIME -> List.of(
                new RareEntry(Items.SLIME_SPAWN_EGG, 0.0015f)
            );
            case VINDICATOR -> List.of(
                new RareEntry(Items.VINDICATOR_SPAWN_EGG, 0.0015f)
            );
            case EVOKER -> List.of(
                new RareEntry(Items.EVOKER_SPAWN_EGG, 0.0015f)
            );
            case GHAST -> List.of(
                new RareEntry(Items.GHAST_SPAWN_EGG, 0.0015f)
            );
            case MAGMA_CUBE -> List.of(
                new RareEntry(Items.MAGMA_CUBE_SPAWN_EGG, 0.0015f)
            );
            case SHULKER -> List.of(
                new RareEntry(Items.SHULKER_SPAWN_EGG, 0.0015f)
            );
            case ENDER_DRAGON -> List.of(
                new RareEntry(Items.DRAGON_HEAD, 0.05f),
                new RareEntry(Items.DRAGON_EGG, 0.005f),
                new RareEntry(Items.ELYTRA, 0.0005f)
            );
            case WITHER -> List.of(
                new RareEntry(Items.WITHER_SKELETON_SKULL, 0.025f),
                new RareEntry(Items.WITHER_SPAWN_EGG, 0.0015f)
            );
            case WARDEN -> List.of(
                new RareEntry(BuiltInRegistries.ITEM.get(Identifier.parse("avoidminer:rarity_upgrade")).orElseThrow().value(), 0.01f),
                new RareEntry(Items.WARDEN_SPAWN_EGG, 0.0015f)
            );
            case BOGGED -> List.of(
                new RareEntry(Items.BOGGED_SPAWN_EGG, 0.0015f)
            );
            case DROWNED -> List.of(
                new RareEntry(Items.TRIDENT, 0.02f),
                new RareEntry(Items.DROWNED_SPAWN_EGG, 0.0015f)
            );
            case HOGLIN -> List.of(
                new RareEntry(Items.HOGLIN_SPAWN_EGG, 0.0015f)
            );
            case PHANTOM -> List.of(
                new RareEntry(Items.PHANTOM_SPAWN_EGG, 0.0015f)
            );
            case PILLAGER -> List.of(
                new RareEntry(Items.PILLAGER_SPAWN_EGG, 0.0015f)
            );
            case STRAY -> List.of(
                new RareEntry(Items.STRAY_SPAWN_EGG, 0.0015f)
            );
        };
    }
}
