package com.airton.avoidminer;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, AvoidMiner.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GOLDEN_SONIC_BOOM =
            PARTICLE_TYPES.register("golden_sonic_boom", () -> new SimpleParticleType(true));

    private ModParticleTypes() {
    }
}
