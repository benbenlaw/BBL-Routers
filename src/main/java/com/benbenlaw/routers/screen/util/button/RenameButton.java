package com.benbenlaw.routers.screen.util.button;

import com.benbenlaw.routers.Routers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

// A small icon beside the title, with no frame: click it to name the router.
public class RenameButton extends Button {

    public static final int SIZE = 10;

    private static final Identifier ICON = Routers.identifier("rename_icon");

    public RenameButton(int x, int y, Runnable onRename) {
        super(x, y, SIZE, SIZE, Component.empty(), button -> onRename.run(), DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovered();

        if (hovered) guiGraphics.fill(getX() - 1, getY() - 1, getX() + SIZE + 1, getY() + SIZE + 1, 0x40FFFFFF);

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ICON, getX(), getY(), SIZE, SIZE);

        if (hovered) {
            Component text = Component.translatable("tooltip.routers.rename");

            guiGraphics.tooltip(
                    Minecraft.getInstance().font,
                    List.of(ClientTooltipComponent.create(text.getVisualOrderText())),
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }
    }
}
