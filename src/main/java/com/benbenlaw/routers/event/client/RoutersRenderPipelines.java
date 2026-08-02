package com.benbenlaw.routers.event.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class RoutersRenderPipelines {

    public static final RenderPipeline LINES_NO_DEPTH = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("routers", "pipeline/lines_no_depth"))
            .withDepthStencilState(Optional.empty())
            .build();
}