package com.benbenlaw.routers.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RouterImportScreen extends ImporterScreen {

    public RouterImportScreen(ImporterMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.topPos = (this.height - RouterTabs.GUI_HEIGHT) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RouterTabs.renderTab(guiGraphics, leftPos, topPos, RouterTabs.EXPORT, false);
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        RouterTabs.renderTab(guiGraphics, leftPos, topPos, RouterTabs.IMPORT, true);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        RouterTabs.renderTooltip(guiGraphics, font, leftPos, topPos, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return RouterTabs.mouseClicked(leftPos, topPos, mouseX, mouseY, button, RouterTabs.IMPORT)
                || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return RouterTabs.tabAt(guiLeft, guiTop, mouseX, mouseY) < 0
                && super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
    }
}
