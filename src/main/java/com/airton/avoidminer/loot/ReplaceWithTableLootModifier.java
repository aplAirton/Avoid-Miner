package com.airton.avoidminer.loot;

import com.airton.avoidminer.ModLootModifiers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class ReplaceWithTableLootModifier extends LootModifier {
    public static final MapCodec<ReplaceWithTableLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance)
                    .and(ResourceKey.codec(Registries.LOOT_TABLE)
                            .fieldOf("table")
                            .forGetter(ReplaceWithTableLootModifier::table))
                    .apply(instance, ReplaceWithTableLootModifier::new));

    private final ResourceKey<LootTable> table;

    public ReplaceWithTableLootModifier(
            LootItemCondition[] conditions,
            int priority,
            ResourceKey<LootTable> table
    ) {
        super(conditions, priority);
        this.table = table;
    }

    public ResourceKey<LootTable> table() {
        return table;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ObjectArrayList<ItemStack> replacementLoot = new ObjectArrayList<>();
        context.getResolver().lookupOrThrow(Registries.LOOT_TABLE).get(table).ifPresent(replacementTable ->
                replacementTable.value().getRandomItemsRaw(
                        context,
                        LootTable.createStackSplitter(context.getLevel(), replacementLoot::add)
                ));

        ArchaeologyLootRules.replaceWithFirst(generatedLoot, replacementLoot);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.REPLACE_WITH_TABLE.get();
    }
}
