package com.benbenlaw.routers.screen.util.button;

import com.benbenlaw.core.Core;
import com.benbenlaw.core.block.entity.WhitelistBlockEntity;
import com.benbenlaw.core.network.packets.SyncWhitelistMode;
import com.benbenlaw.core.screen.util.button.WhitelistButton;
import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.networking.packets.OpenMenu;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.client.gui.components.Button;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.awt.*;
import java.util.List;

public class FilterButton extends Button {

    private ButtonType type;

    public FilterButton(int x, int y, int width, int height, OnPress onPress, ButtonType type) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.type = type;
        this.height = 18;
        this.width = 18;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovered();
        Identifier currentTexture = hovered ? Routers.identifier(type.getTextureHover()) : Routers.identifier(type.getTexture());

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, currentTexture, this.getX(), this.getY(), this.width, this.height);

    }

    public static FilterButton create(int x, int y, int width, int height, BlockEntity blockEntity, ButtonType type) {
         if (blockEntity instanceof ConfigurableRouterBlockEntity configurable) {

             if (!configurable.hasUpgrade(type)) return null;

             return new FilterButton(x, y, width, height, button ->
                     ClientPacketDistributor.sendToServer(new OpenMenu(blockEntity.getBlockPos(), type)), type);

         }

        return null;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        super.onPress(input);
    }

    public ButtonType getType() {
        return type;
    }
}
