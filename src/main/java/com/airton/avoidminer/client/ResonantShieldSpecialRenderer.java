package com.airton.avoidminer.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class ResonantShieldSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {

    private static final int GLINT_COLOR = 0xFF7073FF;

    private final SpriteGetter sprites;
    private final ShieldModel model;
    private final SpriteId baseTexture;

    public ResonantShieldSpecialRenderer(SpriteGetter sprites, ShieldModel model, SpriteId baseTexture) {
        this.sprites = sprites;
        this.model = model;
        this.baseTexture = baseTexture;
    }

    @Override
    public DataComponentMap extractArgument(net.minecraft.world.item.ItemStack stack) {
        return stack.immutableComponents();
    }

    @Override
    public void submit(DataComponentMap components, PoseStack poseStack,
                       SubmitNodeCollector collector, int packedLight, int packedOverlay,
                       boolean hasGlint, int tint) {
        BannerPatternLayers patterns = components != null
                ? components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
                : BannerPatternLayers.EMPTY;
        DyeColor baseColor = components != null
                ? components.get(DataComponents.BASE_COLOR)
                : null;

        boolean usePatterns = !patterns.layers().isEmpty() || baseColor != null;
        SpriteId spriteId = usePatterns ? Sheets.SHIELD_BASE : baseTexture;

        collector.submitModel(
                model, Unit.INSTANCE, poseStack,
                packedLight, packedOverlay, -1,
                spriteId, sprites, 0, null
        );

        if (usePatterns) {
            BannerRenderer.submitPatterns(
                    sprites, poseStack, collector,
                    packedLight, packedOverlay,
                    model, Unit.INSTANCE, false,
                    baseColor != null ? baseColor : DyeColor.WHITE,
                    patterns, null
            );
        }

        if (hasGlint) {
            TextureAtlasSprite sprite = sprites.get(spriteId);
            collector.submitModel(
                    model, Unit.INSTANCE, poseStack,
                    RenderTypes.entityGlint(),
                    packedLight, packedOverlay, GLINT_COLOR,
                    sprite, 0, null
            );
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        PoseStack poseStack = new PoseStack();
        model.root().getExtentsForGui(poseStack, extents);
    }

    public record Unbaked(Identifier texture) implements SpecialModelRenderer.Unbaked<DataComponentMap> {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture)
                ).apply(instance, Unbaked::new)
        );

        @Override
        public SpecialModelRenderer<DataComponentMap> bake(BakingContext context) {
            ShieldModel shieldModel = new ShieldModel(
                    context.entityModelSet().bakeLayer(ModelLayers.SHIELD)
            );
            SpriteId spriteId = Sheets.SHIELD_MAPPER.apply(texture);
            return new ResonantShieldSpecialRenderer(context.sprites(), shieldModel, spriteId);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<DataComponentMap>> type() {
            return MAP_CODEC;
        }
    }
}
