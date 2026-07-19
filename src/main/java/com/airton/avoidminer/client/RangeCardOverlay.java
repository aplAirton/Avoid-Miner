package com.airton.avoidminer.client;

import com.airton.avoidminer.AvoidMiner;
import com.airton.avoidminer.ModItems;
import com.airton.avoidminer.block.entity.MinerBlockEntity;
import com.airton.avoidminer.item.RangeCardItem;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

@EventBusSubscriber(modid = AvoidMiner.MODID, value = Dist.CLIENT)
public class RangeCardOverlay {
    private static final RenderPipeline RANGE_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(AvoidMiner.MODID, "pipeline/range_card_lines"))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();
    private static final RenderType RANGE_LINES = RenderType.create(
            "avoidminer_range_card_lines",
            RenderSetup.builder(RANGE_PIPELINE).bufferSize(1 << 14).createRenderSetup());

    private static int trackedMinX, trackedMaxX, trackedMinY, trackedMaxY, trackedMinZ, trackedMaxZ;
    private static boolean hasTrackedBounds;

    public static void registerPipeline(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(RANGE_PIPELINE);
    }

    public static void setBounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        trackedMinX = minX;
        trackedMaxX = maxX;
        trackedMinY = minY;
        trackedMaxY = maxY;
        trackedMinZ = minZ;
        trackedMaxZ = maxZ;
        hasTrackedBounds = true;
    }

    public static void clearBounds() {
        hasTrackedBounds = false;
    }

    @SubscribeEvent
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        Level level = player.level();
        if (level == null) return;

        var camPos = event.getLevelRenderState().cameraRenderState.pos;

        int rx1 = 0, rz1 = 0, rx2 = 0, rz2 = 0, rMinY = 0, rMaxY = 0;
        boolean shouldRender = false;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack card = mainHand.is(ModItems.RANGE_CARD.get()) ? mainHand
                : offHand.is(ModItems.RANGE_CARD.get()) ? offHand : ItemStack.EMPTY;

        if (!card.isEmpty() && RangeCardItem.hasCompleteData(card)) {
            int cx1 = Math.min(RangeCardItem.getX1(card), RangeCardItem.getX2(card));
            int cz1 = Math.min(RangeCardItem.getZ1(card), RangeCardItem.getZ2(card));
            int cx2 = Math.max(RangeCardItem.getX1(card), RangeCardItem.getX2(card));
            int cz2 = Math.max(RangeCardItem.getZ1(card), RangeCardItem.getZ2(card));
            int cy1 = RangeCardItem.getY1(card);
            int cy2 = RangeCardItem.getY2(card);
            rx1 = cx1; rz1 = cz1; rx2 = cx2; rz2 = cz2;
            rMinY = Math.min(cy1, cy2);
            rMaxY = Math.max(cy1, cy2);
            shouldRender = true;
        }

        if (!shouldRender) {
            HitResult hit = mc.hitResult;
            if (hit instanceof BlockHitResult blockHit) {
                BlockPos pos = blockHit.getBlockPos();
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof MinerBlockEntity miner && miner.hasRange()) {
                    rx1 = miner.getMinX(); rz1 = miner.getMinZ();
                    rx2 = miner.getMaxX(); rz2 = miner.getMaxZ();
                    rMinY = miner.getMinY(); rMaxY = miner.getMaxY();
                    shouldRender = true;
                }
            }
        }

        if (!shouldRender && hasTrackedBounds) {
            rx1 = trackedMinX; rz1 = trackedMinZ;
            rx2 = trackedMaxX; rz2 = trackedMaxZ;
            rMinY = trackedMinY; rMaxY = trackedMaxY;
            shouldRender = true;
        }

        if (!shouldRender) return;

        int finalX1 = rx1;
        int finalZ1 = rz1;
        int finalMinY = rMinY;
        int finalColor = 0x99A0D0FF;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        double w = rx2 - rx1 + 1;
        double h = rMaxY - rMinY + 1;
        double d = rz2 - rz1 + 1;
        VoxelShape box = Shapes.box(0, 0, 0, w, h, d);

        event.getSubmitNodeCollector().submitCustomGeometry(poseStack, RANGE_LINES,
                (pose, consumer) -> {
                    PoseStack localPose = new PoseStack();
                    localPose.last().set(pose);
                    ShapeRenderer.renderShape(localPose, consumer, box,
                            finalX1, finalMinY, finalZ1, finalColor, 1.5F);
                });

        poseStack.popPose();
    }
}
