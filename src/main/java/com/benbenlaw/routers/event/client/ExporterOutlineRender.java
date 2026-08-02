package com.benbenlaw.routers.event.client;

import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.item.RoutersItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.common.Tags;

public class ExporterOutlineRender implements CustomBlockOutlineRenderer {

    private final Camera camera;

    public ExporterOutlineRender(Camera camera) {
        this.camera = camera;
    }

    @Override
    public boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource buffer, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState) {

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Player player = mc.player;
        if (level == null || player == null) return false;

        if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(RoutersItems.CONNECTOR.get())
                && !player.getItemInHand(InteractionHand.MAIN_HAND).is(Tags.Items.TOOLS_WRENCH)) return false;

        BlockPos exporterPos = renderState.pos();
        BlockEntity be = level.getBlockEntity(exporterPos);
        if (!(be instanceof ExporterBlockEntity exporter)) return false;
        if (exporter.importerPositions == null || exporter.importerPositions.isEmpty()) return false;

        double camX = camera.position().x;
        double camY = camera.position().y;
        double camZ = camera.position().z;

        VertexConsumer lineBuilder = buffer.getBuffer(RenderTypes.lines());

        // exporter box (green)
        drawBox(poseStack, lineBuilder, exporterPos, camX, camY, camZ, 0F, 1F, 0.4F);

        // connected importer boxes (orange)
        for (GlobalPos importerGlobalPos : exporter.importerPositions) {
            if (!importerGlobalPos.dimension().equals(level.dimension())) continue;
            drawBox(poseStack, lineBuilder, importerGlobalPos.pos(), camX, camY, camZ, 1F, 0.6F, 0F);
        }

        return true;
    }

    private static void drawBox(PoseStack poseStack, VertexConsumer lineBuilder, BlockPos pos, double camX, double camY, double camZ, float r, float g, float b) {

        AABB shifted = new AABB(pos).inflate(0.002).move(-camX, -camY, -camZ);

        ShapeRenderer.renderShape(
                poseStack,
                lineBuilder,
                Shapes.create(shifted),
                0, 1, 0,
                ARGB.colorFromFloat(0.4F, r, g, b),
                2F
        );
    }
}