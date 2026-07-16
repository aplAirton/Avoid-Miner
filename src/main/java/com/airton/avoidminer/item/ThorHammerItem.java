package com.airton.avoidminer.item;

import com.airton.avoidminer.AvoidMiner;
import com.airton.avoidminer.config.AvoidMinerServerConfig;
import com.airton.avoidminer.enchantment.ThorThunderRules;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ThorHammerItem extends MaceItem {
    private static final ResourceKey<Enchantment> THUNDER = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "thor_thunder")
    );

    public ThorHammerItem(Item.Properties properties) {
        super(properties
                .rarity(Rarity.EPIC)
                .durability(500)
                .component(DataComponents.TOOL, MaceItem.createToolProperties())
                .repairable(Items.BREEZE_ROD)
                .attributes(MaceItem.createAttributes())
                .enchantable(15)
                .component(DataComponents.WEAPON, new Weapon(1)));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72_000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) {
            return;
        }

        int heldTicks = getUseDuration(stack, entity) - ticksRemaining;
        if (heldTicks == ThorThunderRules.CHARGE_TICKS) {
            // pronto para disparar: clarão + trovão distante como confirmação sonora
            emitReadyBurst(serverLevel, player);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.COPPER_BULB_TURN_ON,
                    SoundSource.PLAYERS, 0.55F, 1.45F);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.PLAYERS, 0.4F, 1.9F);
        } else if (heldTicks < ThorThunderRules.CHARGE_TICKS) {
            emitChargingSparks(serverLevel, player, heldTicks);
            if (heldTicks % 3 == 0) {
                float progress = heldTicks / (float) ThorThunderRules.CHARGE_TICKS;
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT,
                        SoundSource.PLAYERS, 0.35F, 0.7F + progress);
            }
        } else if (heldTicks > ThorThunderRules.CHARGE_TICKS) {
            if (heldTicks % 4 == 0) {
                emitChargedSparks(serverLevel, player, heldTicks);
            }
            emitStormCloud(serverLevel, player, heldTicks);
            if (heldTicks % 36 == 0) {
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.PLAYERS, 0.12F, 0.55F);
            }
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        int heldTicks = getUseDuration(stack, entity) - remainingTime;
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        if (heldTicks < ThorThunderRules.CHARGE_TICKS) {
            return false;
        }

        fire(serverLevel, player, stack);
        player.getCooldowns().addCooldown(stack,
                AvoidMinerServerConfig.weaponCooldown(ThorThunderRules.COOLDOWN_TICKS));
        stack.hurtAndBreak(1, player, player.getUsedItemHand());
        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }

    private static void fire(ServerLevel level, Player player, ItemStack stack) {
        HitResult hit = ProjectileUtil.getHitResultOnViewVector(
                player,
                entity -> entity instanceof LivingEntity living && living.isAlive() && player.canAttack(living),
                AvoidMinerServerConfig.weaponRange(ThorThunderRules.RANGE)
        );
        Vec3 origin = chargeFocus(player);
        Vec3 endpoint = hit.getLocation();

        // Proteções do portador: o raio incendeia e explode perto — 10s de
        // Resistência ao Fogo e Resistência IV. Não cumulativas: enquanto o
        // efeito estiver ativo, novos disparos não renovam a duração.
        if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, false, true));
        }
        if (!player.hasEffect(MobEffects.RESISTANCE)) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 3, false, false, true));
        }

        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.0F, 0.7F);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(),
                SoundSource.PLAYERS, 0.9F, 1.1F);

        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity primary) {
            endpoint = primary.getEyePosition();
            emitArc(level, origin, endpoint);
            strikeTarget(level, player, primary);
            chainLightning(level, player, primary, getEffectiveThunderLevel(level, stack));
        } else {
            emitArc(level, origin, endpoint);
            if (hit.getType() == HitResult.Type.BLOCK) {
                summonLightning(level, player, endpoint);
                emitImpact(level, endpoint);
            }
        }
    }

    // Raio + repulsão (nível 2) + onda de choque visual num alvo vivo
    private static void strikeTarget(ServerLevel level, Player player, LivingEntity target) {
        summonLightning(level, player, target.position());
        emitImpact(level, target.position());
        target.knockback(1.2, player.getX() - target.getX(), player.getZ() - target.getZ());
        target.hurtMarked = true;
    }

    private static void chainLightning(ServerLevel level, Player player, LivingEntity primary, int effectiveLevel) {
        int additionalTargets = ThorThunderRules.additionalTargets(effectiveLevel);
        if (additionalTargets == 0) {
            return;
        }

        AABB area = primary.getBoundingBox().inflate(
                AvoidMinerServerConfig.weaponRange(ThorThunderRules.CHAIN_RADIUS));
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area,
                        target -> target != primary
                                && target instanceof Enemy
                                && target.isAlive()
                                && player.canAttack(target))
                .stream()
                .sorted(Comparator.comparingDouble(primary::distanceToSqr))
                .limit(additionalTargets)
                .toList();

        Vec3 previous = primary.getEyePosition();
        for (LivingEntity target : nearby) {
            Vec3 targetPoint = target.getEyePosition();
            emitArc(level, previous, targetPoint);
            strikeTarget(level, player, target);
            previous = targetPoint;
        }
    }

    private static void summonLightning(ServerLevel level, Player player, Vec3 position) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (lightning == null) {
            return;
        }
        lightning.setPos(position.x, position.y, position.z);
        lightning.setDamage(AvoidMinerServerConfig.weaponDamage(ThorThunderRules.DAMAGE));
        if (player instanceof ServerPlayer serverPlayer) {
            lightning.setCause(serverPlayer);
        }
        level.addFreshEntity(lightning);
    }

    // Caminho de eletricidade estática até o alvo: núcleo reto brilhante +
    // dois filamentos serrilhados com jitter aleatório e ramificações curtas
    private static void emitArc(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        int steps = Math.clamp((int) Math.ceil(delta.length() * 2.5), 2, 80);
        Vec3 direction = delta.normalize();
        Vec3 side = new Vec3(-direction.z, 0.0, direction.x);
        if (side.lengthSqr() < 1.0E-6) {
            side = new Vec3(1.0, 0.0, 0.0);
        } else {
            side = side.normalize();
        }
        Vec3 up = side.cross(direction).normalize();
        RandomSource random = level.getRandom();

        for (int strand = 0; strand < 2; strand++) {
            double phase = strand * Math.PI;
            for (int step = 0; step <= steps; step++) {
                double progress = step / (double) steps;
                // serrilhado: seno base + ruído aleatório que zera nas pontas
                double taper = Math.sin(progress * Math.PI);
                double sway = Math.sin(progress * Math.PI * 8.0 + phase) * 0.06;
                double jitterS = (random.nextDouble() - 0.5) * 0.22 * taper;
                double jitterU = (random.nextDouble() - 0.5) * 0.22 * taper;
                Vec3 point = start.add(delta.scale(progress))
                        .add(side.scale(sway + jitterS))
                        .add(up.scale(jitterU));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, true, true,
                        point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.004);

                // núcleo luminoso reto (só no primeiro filamento, mais espaçado)
                if (strand == 0 && step % 2 == 0) {
                    Vec3 core = start.add(delta.scale(progress));
                    level.sendParticles(ParticleTypes.END_ROD, true, true,
                            core.x, core.y, core.z, 1, 0.0, 0.0, 0.0, 0.0);
                }

                // ramificações curtas saltando do arco
                if (strand == 0 && taper > 0.4 && random.nextFloat() < 0.18f) {
                    Vec3 branchDir = side.scale(random.nextDouble() - 0.5)
                            .add(up.scale(random.nextDouble() - 0.5))
                            .add(direction.scale(0.2))
                            .normalize();
                    for (int b = 1; b <= 3; b++) {
                        Vec3 branchPoint = point.add(branchDir.scale(b * 0.18));
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, true, true,
                                branchPoint.x, branchPoint.y, branchPoint.z, 1, 0.01, 0.01, 0.01, 0.002);
                    }
                }
            }
        }
    }

    // Ponto de queda do raio: clarão, rajada de repulsão e anel de faíscas
    private static void emitImpact(ServerLevel level, Vec3 position) {
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFCCEEFF), true, true,
                position.x, position.y + 0.5, position.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, true, true,
                position.x, position.y + 0.1, position.z, 1, 0.0, 0.0, 0.0, 0.0);
        for (int i = 0; i < 16; i++) {
            double angle = Math.PI * 2.0 * i / 16.0;
            double px = position.x + Math.cos(angle) * 1.5;
            double pz = position.z + Math.sin(angle) * 1.5;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, true, true,
                    px, position.y + 0.2, pz, 1, 0.05, 0.15, 0.05, 0.02);
        }
    }

    // Nuvem de tempestade se formando acima do portador enquanto segura a carga
    private static void emitStormCloud(ServerLevel level, Player player, int heldTicks) {
        double cx = player.getX();
        double cy = player.getY() + 5.5;
        double cz = player.getZ();
        level.sendParticles(ParticleTypes.CLOUD, true, true,
                cx, cy, cz, 2, 1.4, 0.25, 1.4, 0.0);
        if (heldTicks % 12 == 0) {
            RandomSource random = level.getRandom();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, true, true,
                    cx + (random.nextDouble() - 0.5) * 2.4, cy - 0.4,
                    cz + (random.nextDouble() - 0.5) * 2.4, 4, 0.3, 0.2, 0.3, 0.03);
        }
    }

    // Carga: hélice dupla convergindo no martelo + estática subindo do chão
    private static void emitChargingSparks(ServerLevel level, Player player, int heldTicks) {
        Vec3 focus = chargeFocus(player);
        Vec3 side = horizontalSide(player);
        double progress = heldTicks / (double) ThorThunderRules.CHARGE_TICKS;
        double radius = 0.32 - progress * 0.16;
        for (int i = 0; i < 2; i++) {
            double angle = heldTicks * 0.55 + i * Math.PI;
            Vec3 point = focus.add(side.scale(Math.cos(angle) * radius))
                    .add(0.0, Math.sin(angle) * radius, 0.0);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, true, true,
                    point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        // hélice subindo em volta do corpo, fechando conforme carrega
        double helixAngle = heldTicks * 0.9;
        double helixRadius = 1.1 - progress * 0.5;
        double helixY = player.getY() + progress * 2.0;
        for (int i = 0; i < 2; i++) {
            double a = helixAngle + i * Math.PI;
            level.sendParticles(ParticleTypes.END_ROD, true, true,
                    player.getX() + Math.cos(a) * helixRadius,
                    helixY,
                    player.getZ() + Math.sin(a) * helixRadius,
                    1, 0.0, 0.02, 0.0, 0.0);
        }

        // estática no chão ao redor dos pés
        if (heldTicks % 2 == 0) {
            RandomSource random = level.getRandom();
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = 0.8 + random.nextDouble() * 0.6;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, true, true,
                    player.getX() + Math.cos(a) * r, player.getY() + 0.05,
                    player.getZ() + Math.sin(a) * r, 1, 0.02, 0.1, 0.02, 0.015);
        }
    }

    private static void emitReadyBurst(ServerLevel level, Player player) {
        Vec3 focus = chargeFocus(player);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, true, true,
                focus.x, focus.y, focus.z, 18, 0.28, 0.28, 0.28, 0.055);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFCCEEFF), true, true,
                focus.x, focus.y, focus.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    private static void emitChargedSparks(ServerLevel level, Player player, int heldTicks) {
        Vec3 focus = chargeFocus(player);
        Vec3 side = horizontalSide(player);
        for (int i = 0; i < 3; i++) {
            double angle = -heldTicks * 0.22 + Math.PI * 2.0 * i / 3.0;
            Vec3 point = focus.add(side.scale(Math.cos(angle) * 0.2))
                    .add(0.0, Math.sin(angle) * 0.2, 0.0);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, true, true,
                    point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static Vec3 chargeFocus(Player player) {
        return player.getEyePosition().add(player.getLookAngle().normalize().scale(0.9)).add(0.0, -0.35, 0.0);
    }

    private static Vec3 horizontalSide(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0.0, look.x);
        return side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
    }

    private static int getEffectiveThunderLevel(Level level, ItemStack stack) {
        int enchantmentLevel = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(THUNDER)
                .map(holder -> stack.getEnchantments().getLevel(holder))
                .orElse(0);
        return ThorThunderRules.effectiveLevel(enchantmentLevel);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.avoidminer.thor_hammer.innate")
                .withStyle(ChatFormatting.GOLD));
        builder.accept(Component.translatable("tooltip.avoidminer.thor_hammer.charge")
                .withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.avoidminer.thor_hammer.chain")
                .withStyle(ChatFormatting.DARK_GRAY));
        builder.accept(Component.translatable("tooltip.avoidminer.thor_hammer.protection")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
