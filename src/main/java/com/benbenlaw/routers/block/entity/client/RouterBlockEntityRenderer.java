package com.benbenlaw.routers.block.entity.client;

import com.benbenlaw.routers.block.entity.RouterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Draws the connection beams of the Router's inner Exporter. */
public class RouterBlockEntityRenderer implements BlockEntityRenderer<RouterBlockEntity> {

    private final ExporterBlockEntityRenderer exporterRenderer;

    public RouterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.exporterRenderer = new ExporterBlockEntityRenderer(context);
    }

    @Override
    public void render(RouterBlockEntity router, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        exporterRenderer.render(router.getExporter(), partialTicks, poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRender(RouterBlockEntity router, Vec3 cameraPos) {
        return exporterRenderer.shouldRender(router.getExporter(), cameraPos);
    }

    @Override
    public boolean shouldRenderOffScreen(RouterBlockEntity router) {
        return exporterRenderer.shouldRenderOffScreen(router.getExporter());
    }

    @Override
    public int getViewDistance() {
        return exporterRenderer.getViewDistance();
    }

    @Override
    public AABB getRenderBoundingBox(RouterBlockEntity router) {
        return exporterRenderer.getRenderBoundingBox(router.getExporter());
    }
}
