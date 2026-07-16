package com.airton.avoidminer.client;

import com.airton.avoidminer.AvoidMiner;
import com.airton.avoidminer.item.ResonantScannerItem;
import com.airton.avoidminer.network.ResonantScanPayload;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

public final class ResonantScannerRenderer {
    private static final RenderPipeline SCANNER_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(
                    AvoidMiner.MODID, "pipeline/resonant_scanner_lines"))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();
    private static final RenderType SCANNER_LINES = RenderType.create(
            "avoidminer_resonant_scanner_lines",
            RenderSetup.builder(SCANNER_PIPELINE).bufferSize(1 << 20).createRenderSetup());

    private static List<ResonantScanPayload.Target> targets = List.of();
    private static long expiresAt;

    private ResonantScannerRenderer() {}

    public static void registerPipeline(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(SCANNER_PIPELINE);
    }

    public static void accept(ResonantScanPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        targets = payload.targets();
        expiresAt = minecraft.level.getGameTime() + payload.durationTicks();
    }

    public static void submit(SubmitCustomGeometryEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || targets.isEmpty()) return;
        if (minecraft.level.getGameTime() >= expiresAt) {
            targets = List.of();
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        var camera = event.getLevelRenderState().cameraRenderState.pos;
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        List<ResonantScanPayload.Target> snapshot = targets;
        event.getSubmitNodeCollector().submitCustomGeometry(poseStack, SCANNER_LINES,
                (pose, consumer) -> {
                    PoseStack linePose = new PoseStack();
                    linePose.last().set(pose);
                    for (ResonantScanPayload.Target target : snapshot) {
                        int color = colorFor(target.category());
                        ShapeRenderer.renderShape(linePose, consumer, Shapes.block(),
                                target.pos().getX(), target.pos().getY(), target.pos().getZ(),
                                color, target.category() == ResonantScannerItem.CAVITY ? 1.0F : 2.0F);
                    }
                });
        poseStack.popPose();
    }

    private static int colorFor(byte category) {
        return switch (category) {
            case ResonantScannerItem.COAL -> 0xF06B6B6B;
            case ResonantScannerItem.COPPER -> 0xF0D77A4A;
            case ResonantScannerItem.LAPIS -> 0xF03F63D8;
            case ResonantScannerItem.GOLD -> 0xF0FFD447;
            case ResonantScannerItem.IRON -> 0xF0E3DDD2;
            case ResonantScannerItem.REDSTONE -> 0xF0F23838;
            case ResonantScannerItem.DIAMOND -> 0xF04DE3E7;
            case ResonantScannerItem.EMERALD -> 0xF043E36F;
            case ResonantScannerItem.LAVA -> 0xEFFF4A22;
            case ResonantScannerItem.WATER -> 0xDD2E9BFF;
            case ResonantScannerItem.CAVITY -> 0x9968E0D5;
            default -> 0xFFFFFFFF;
        };
    }
}
