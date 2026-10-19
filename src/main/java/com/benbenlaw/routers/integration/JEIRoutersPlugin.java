package com.benbenlaw.routers.integration;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.screen.ExporterScreen;
import com.benbenlaw.routers.screen.ImporterMenu;
import com.benbenlaw.routers.screen.ImporterScreen;
import com.benbenlaw.routers.screen.RouterExportScreen;
import com.benbenlaw.routers.screen.RouterImportScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

@JeiPlugin
public class JEIRoutersPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Routers.MOD_ID, "jei_plugin");
    }


    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(ExporterScreen.class, new GhostIngredientExporterHandler());
        registration.addGhostIngredientHandler(ImporterScreen.class, new GhostIngredientImporterHandler());
        registration.addGhostIngredientHandler((Class) RouterExportScreen.class, new GhostIngredientExporterHandler());
        registration.addGhostIngredientHandler((Class) RouterImportScreen.class, new GhostIngredientImporterHandler());
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        registration.addAliases(VanillaTypes.ITEM_STACK, List.of(new ItemStack(RoutersItems.ROUTER_CONNECTOR.get())), "wrench");
    }
}
