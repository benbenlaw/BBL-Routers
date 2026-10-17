package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.entity.RouterBlockEntity;
import com.benbenlaw.routers.networking.packets.RouterSwitchTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/** Creative-style Export / Import tabs drawn above the Router screens. */
public final class RouterTabs {

    public static final int EXPORT = 0;
    public static final int IMPORT = 1;

    /** Both Router screens are centred on the export screen's height, so the tabs stay put when switching. */
    public static final int GUI_HEIGHT = 172;

    private static final ResourceLocation[] SELECTED = {
            ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_1"),
            ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_2")
    };
    private static final ResourceLocation[] UNSELECTED = {
            ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1"),
            ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_2")
    };

    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int TAB_SPACING = 27;
    private static final int TAB_OFFSET_Y = 28;

    private RouterTabs() {
    }

    /** Draw the tab that is not selected before the GUI background, and the selected one after it so it overlaps the border. */
    public static void renderTab(GuiGraphics guiGraphics, int leftPos, int topPos, int tab, boolean selected) {
        int x = leftPos + tab * TAB_SPACING;
        int y = topPos - TAB_OFFSET_Y;
        guiGraphics.blitSprite((selected ? SELECTED : UNSELECTED)[tab], x, y, TAB_WIDTH, TAB_HEIGHT);
        guiGraphics.renderItem(icon(tab), x + 5, y + 9);
    }

    public static void renderTooltip(GuiGraphics guiGraphics, Font font, int leftPos, int topPos, int mouseX, int mouseY) {
        int tab = tabAt(leftPos, topPos, mouseX, mouseY);
        if (tab >= 0) {
            guiGraphics.renderTooltip(font, label(tab), mouseX, mouseY);
        }
    }

    /** Client side: the Router reuses the Exporter/Importer menus, so the screen picks the tabbed version by the block entity. */
    public static boolean isRouter(BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        return level != null && level.getBlockEntity(pos) instanceof RouterBlockEntity;
    }

    /** @return the tab under the mouse, or -1 */
    public static int tabAt(int leftPos, int topPos, double mouseX, double mouseY) {
        for (int tab = EXPORT; tab <= IMPORT; tab++) {
            int x = leftPos + tab * TAB_SPACING;
            int y = topPos - TAB_OFFSET_Y;
            if (mouseX >= x && mouseX < x + TAB_WIDTH && mouseY >= y && mouseY < y + TAB_HEIGHT) {
                return tab;
            }
        }
        return -1;
    }

    /** @return true if the click hit a tab and was handled */
    public static boolean mouseClicked(int leftPos, int topPos, double mouseX, double mouseY, int button, int selectedTab) {
        if (button != 0) return false;
        int tab = tabAt(leftPos, topPos, mouseX, mouseY);
        if (tab < 0) return false;

        if (tab != selectedTab) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            PacketDistributor.sendToServer(new RouterSwitchTab(tab == IMPORT));
        }
        return true;
    }

    private static ItemStack icon(int tab) {
        return new ItemStack(tab == EXPORT ? RoutersBlocks.EXPORTER_BLOCK : RoutersBlocks.IMPORTER_BLOCK);
    }

    private static Component label(int tab) {
        return Component.translatable(tab == EXPORT ? "screen.routers.router.export_tab" : "screen.routers.router.import_tab");
    }
}
