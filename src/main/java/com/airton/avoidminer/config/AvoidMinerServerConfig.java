package com.airton.avoidminer.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AvoidMinerServerConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue MAGNETITE_VEINS_PER_CHUNK;
    private static final ModConfigSpec.IntValue MAGNETITE_VEIN_SIZE;
    private static final ModConfigSpec.IntValue MAGNETITE_MIN_Y;
    private static final ModConfigSpec.IntValue MAGNETITE_MAX_Y;
    private static final ModConfigSpec.DoubleValue MAGNETITE_AIR_DISCARD_CHANCE;
    private static final ModConfigSpec.DoubleValue MACHINE_ENERGY_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue MACHINE_SPEED_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue LOOT_CHANCE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue WEAPON_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue WEAPON_COOLDOWN_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue WEAPON_RANGE_MULTIPLIER;
    private static final ModConfigSpec.IntValue SCANNER_RADIUS;
    private static final ModConfigSpec.IntValue SCANNER_DURATION_TICKS;
    private static final ModConfigSpec.IntValue SCANNER_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue REPAIR_PER_CRYSTAL;
    private static final ModConfigSpec.IntValue REPAIR_INTERVAL_TICKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Magnetite ore generation. These values apply to newly generated chunks.")
                .push("magnetiteGeneration");
        MAGNETITE_VEINS_PER_CHUNK = builder.comment("Placement attempts per chunk.")
                .worldRestart().defineInRange("veinsPerChunk", 8, 0, 128);
        MAGNETITE_VEIN_SIZE = builder.comment("Maximum blocks attempted in each vein.")
                .worldRestart().defineInRange("veinSize", 8, 1, 64);
        MAGNETITE_MIN_Y = builder.worldRestart().defineInRange("minY", 0, -64, 320);
        MAGNETITE_MAX_Y = builder.worldRestart().defineInRange("maxY", 30, -64, 320);
        MAGNETITE_AIR_DISCARD_CHANCE = builder.comment("Chance that exposed ore is discarded.")
                .worldRestart().defineInRange("airDiscardChance", 0.8D, 0.0D, 1.0D);
        builder.pop();

        builder.comment("Global machine balance multipliers.").push("machines");
        MACHINE_ENERGY_MULTIPLIER = builder.comment("Multiplies all machine energy costs.")
                .defineInRange("energyConsumptionMultiplier", 1.0D, 0.0D, 100.0D);
        MACHINE_SPEED_MULTIPLIER = builder.comment("Values above 1 make machine operations faster.")
                .defineInRange("speedMultiplier", 1.0D, 0.05D, 100.0D);
        LOOT_CHANCE_MULTIPLIER = builder.comment("Multiplies random upgrade and Lootr chances.")
                .defineInRange("lootChanceMultiplier", 1.0D, 0.0D, 100.0D);
        builder.pop();

        builder.comment("Global weapon balance multipliers.").push("weapons");
        WEAPON_DAMAGE_MULTIPLIER = builder.defineInRange("damageMultiplier", 1.0D, 0.0D, 100.0D);
        WEAPON_COOLDOWN_MULTIPLIER = builder.comment("Values above 1 increase cooldowns.")
                .defineInRange("cooldownMultiplier", 1.0D, 0.0D, 100.0D);
        WEAPON_RANGE_MULTIPLIER = builder.defineInRange("rangeMultiplier", 1.0D, 0.1D, 10.0D);
        builder.pop();

        builder.push("resonantScanner");
        SCANNER_RADIUS = builder.defineInRange("radius", 16, 4, 48);
        SCANNER_DURATION_TICKS = builder.defineInRange("outlineDurationTicks", 200, 20, 2400);
        SCANNER_COOLDOWN_TICKS = builder.defineInRange("cooldownTicks", 600, 0, 72000);
        builder.pop();

        builder.push("resonantRepairStation");
        REPAIR_PER_CRYSTAL = builder.defineInRange("durabilityPerCrystal", 100, 1, 10000);
        REPAIR_INTERVAL_TICKS = builder.defineInRange("operationTicks", 20, 1, 1200);
        builder.pop();

        SPEC = builder.build();
    }

    private AvoidMinerServerConfig() {}

    public static int magnetiteVeinsPerChunk() { return MAGNETITE_VEINS_PER_CHUNK.get(); }
    public static int magnetiteVeinSize() { return MAGNETITE_VEIN_SIZE.get(); }
    public static int magnetiteMinY() { return Math.min(MAGNETITE_MIN_Y.get(), MAGNETITE_MAX_Y.get()); }
    public static int magnetiteMaxY() { return Math.max(MAGNETITE_MIN_Y.get(), MAGNETITE_MAX_Y.get()); }
    public static float magnetiteAirDiscardChance() { return MAGNETITE_AIR_DISCARD_CHANCE.get().floatValue(); }
    public static double machineEnergyMultiplier() { return MACHINE_ENERGY_MULTIPLIER.get(); }
    public static double machineSpeedMultiplier() { return MACHINE_SPEED_MULTIPLIER.get(); }
    public static double lootChanceMultiplier() { return LOOT_CHANCE_MULTIPLIER.get(); }
    public static double weaponDamageMultiplier() { return WEAPON_DAMAGE_MULTIPLIER.get(); }
    public static double weaponCooldownMultiplier() { return WEAPON_COOLDOWN_MULTIPLIER.get(); }
    public static double weaponRangeMultiplier() { return WEAPON_RANGE_MULTIPLIER.get(); }
    public static int scannerRadius() { return SCANNER_RADIUS.get(); }
    public static int scannerDurationTicks() { return SCANNER_DURATION_TICKS.get(); }
    public static int scannerCooldownTicks() { return SCANNER_COOLDOWN_TICKS.get(); }
    public static int repairPerCrystal() { return REPAIR_PER_CRYSTAL.get(); }
    public static int repairIntervalTicks() { return REPAIR_INTERVAL_TICKS.get(); }

    public static int machineTicks(int baseTicks) {
        return Math.max(1, (int) Math.ceil(baseTicks / machineSpeedMultiplier()));
    }

    public static int energyCost(double baseCost) {
        return Math.max(0, (int) Math.ceil(baseCost * machineEnergyMultiplier()));
    }

    public static float lootChance(float baseChance) {
        return (float) Math.min(1.0D, baseChance * lootChanceMultiplier());
    }

    public static float weaponDamage(float baseDamage) {
        return (float) (baseDamage * weaponDamageMultiplier());
    }

    public static int weaponCooldown(int baseTicks) {
        return Math.max(0, (int) Math.ceil(baseTicks * weaponCooldownMultiplier()));
    }

    public static double weaponRange(double baseRange) {
        return baseRange * weaponRangeMultiplier();
    }
}
