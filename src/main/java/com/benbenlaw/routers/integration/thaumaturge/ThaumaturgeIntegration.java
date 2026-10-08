package com.benbenlaw.routers.integration.thaumaturge;

import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.api.screen.RouterUIRegistries;
import com.benbenlaw.routers.api.screen.ScreenModule;
import com.benbenlaw.routers.api.screen.client.RouterUIRenderers;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.item.UpgradeItem;
import com.benbenlaw.routers.screen.upgrade.FilterScreen;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.transfers.RoutersTransfers;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThaumaturgeIntegration {

    public static final Identifier ESSENTIA = Routers.identifier("essentia");
    private static final Identifier SLOTS_9 = Routers.identifier("inventory_slots_9");

    private static final DeferredRegister<TransferModule<?>> TRANSFER_MODULES =
            DeferredRegister.create(RoutersTransfers.TRANSFER_MODULE_KEY, Routers.MOD_ID);

    private static final DeferredRegister<ScreenModule> SCREEN_MODULES =
            DeferredRegister.create(RouterUIRegistries.SCREEN_MODULE_KEY, Routers.MOD_ID);

    public static final DeferredHolder<TransferModule<?>, EssentiaRouterTransfer> ESSENTIA_TRANSFER =
            TRANSFER_MODULES.register("essentia", EssentiaRouterTransfer::new);

    public static final DeferredHolder<ScreenModule, ScreenModule> ESSENTIA_FILTER =
            SCREEN_MODULES.register("essentia_filter", () -> new ScreenModule(ESSENTIA, (menu, entity, x) -> {
                FilterItemHandler filter = entity.getResourceFilter(ESSENTIA);
                for (int i = 0; i < 18; i++) {
                    menu.addSlotPublic(new AspectFilterSlot(filter, filter::set, i, 8 + (i % 9) * 18, 36 + (i / 9) * 18));
                }
            }));

    public static final List<DeferredItem<Item>> ESSENTIA_UPGRADES = List.of(
            essentiaUpgrade(1, StartupConfig.essentiaPerOperation1),
            essentiaUpgrade(2, StartupConfig.essentiaPerOperation2),
            essentiaUpgrade(3, StartupConfig.essentiaPerOperation3),
            essentiaUpgrade(4, StartupConfig.essentiaPerOperation4),
            essentiaUpgrade(5, StartupConfig.essentiaPerOperation5)
    );

    public static void register(IEventBus eventBus) {
        TRANSFER_MODULES.register(eventBus);
        SCREEN_MODULES.register(eventBus);
    }

    public static void clientInit() {
        RouterUIRenderers.register(ESSENTIA, new RouterUIRenderers.Renderer() {
            @Override
            public float[] color() {
                return ButtonType.hex("40C8B8");
            }

            @Override
            public void renderBackground(GuiGraphicsExtractor gui, FilterScreen screen) {
                gui.blitSprite(RenderPipelines.GUI_TEXTURED, SLOTS_9, screen.getGuiLeft() + 7, screen.getGuiTop() + 35, 162, 18);
                gui.blitSprite(RenderPipelines.GUI_TEXTURED, SLOTS_9, screen.getGuiLeft() + 7, screen.getGuiTop() + 53, 162, 18);
            }

            @Override
            public void renderExtra(GuiGraphicsExtractor gui, FilterScreen screen, ConfigurableRouterBlockEntity entity, int mouseX, int mouseY) {
                Level level = Minecraft.getInstance().level;
                if (level == null) return;

                for (Slot slot : screen.getMenu().slots) {
                    if (!(slot instanceof AspectFilterSlot aspectSlot)) continue;

                    ResourceKey<IAspect> key = aspectSlot.aspect();
                    if (key == null) continue;

                    Holder<IAspect> aspect = Aspects.resolve(level, key);
                    int x = screen.getGuiLeft() + slot.x;
                    int y = screen.getGuiTop() + slot.y;
                    AspectKnowledge knowledge = AspectKnowledgeAccess.of(aspect);

                    gui.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
                    if (knowledge.isKnown()) {
                        AspectTagRenderer.render(gui, x, y, aspect);
                    } else {
                        AspectTagRenderer.renderMaskedChip(gui, x, y, aspect, knowledge);
                    }

                    if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                        int color = aspect.value().color() | 0xFF000000;
                        List<Component> lines = new ArrayList<>();
                        lines.add(AspectComponents.name(aspect).withStyle(style -> style.withColor(color)));
                        lines.add(AspectComponents.description(aspect).withStyle(ChatFormatting.GRAY));
                        if (knowledge == AspectKnowledge.DEDUCIBLE) {
                            lines.add(AspectComponents.composition(aspect).withStyle(ChatFormatting.DARK_GRAY));
                        }
                        lines.add(Component.literal("Thaumaturge").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                        gui.setTooltipForNextFrame(Minecraft.getInstance().font, lines, Optional.empty(), mouseX, mouseY);
                    }
                }
            }
        });
    }

    private static DeferredItem<Item> essentiaUpgrade(int tier, ModConfigSpec.ConfigValue<Integer> amount) {
        String name = "essentia_upgrade_" + tier;
        return RoutersItems.ITEMS.registerItem(name,
                properties -> new UpgradeItem(new Item.Properties().setId(RoutersItems.createID(name)), amount.get()));
    }
}
