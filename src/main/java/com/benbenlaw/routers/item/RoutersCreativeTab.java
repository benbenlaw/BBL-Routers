package com.benbenlaw.routers.item;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RoutersCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Routers.MOD_ID);

    public static final Supplier<CreativeModeTab> CASTING_TAB = CREATIVE_MODE_TABS.register("routers", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> RoutersItems.ROUTER_CONNECTOR.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.routers"))
            .displayItems((parameters, output) -> {

                output.accept(RoutersBlocks.EXPORTER_BLOCK);
                output.accept(RoutersBlocks.IMPORTER_BLOCK);
                output.accept(RoutersBlocks.ROUTER_BLOCK);
                output.accept(RoutersItems.ROUTER_CONNECTOR);

                output.accept(RoutersItems.RF_UPGRADE_1);
                output.accept(RoutersItems.RF_UPGRADE_2);
                output.accept(RoutersItems.RF_UPGRADE_3);
                output.accept(RoutersItems.RF_UPGRADE_4);

                output.accept(RoutersItems.ITEM_UPGRADE_1);
                output.accept(RoutersItems.ITEM_UPGRADE_2);
                output.accept(RoutersItems.ITEM_UPGRADE_3);
                output.accept(RoutersItems.ITEM_UPGRADE_4);

                output.accept(RoutersItems.FLUID_UPGRADE_1);
                output.accept(RoutersItems.FLUID_UPGRADE_2);
                output.accept(RoutersItems.FLUID_UPGRADE_3);
                output.accept(RoutersItems.FLUID_UPGRADE_4);

                output.accept(RoutersItems.SPEED_UPGRADE_1);
                output.accept(RoutersItems.SPEED_UPGRADE_2);
                output.accept(RoutersItems.SPEED_UPGRADE_3);
                output.accept(RoutersItems.SPEED_UPGRADE_4);

                output.accept(RoutersItems.ROUND_ROBIN_UPGRADE);
                output.accept(RoutersItems.MOD_FILTER);
                output.accept(RoutersItems.TAG_FILTER);
                output.accept(RoutersItems.DIMENSIONAL_UPGRADE);

                if (ModList.get().isLoaded("pneumaticcraft")) {
                    output.accept(RoutersItems.PRESSURE_UPGRADE);
                    output.accept(RoutersItems.REINFORCED_PRESSURE_UPGRADE);
                    output.accept(RoutersItems.ADVANCED_PRESSURE_UPGRADE);
                }

                if (ModList.get().isLoaded("mekanism")){
                    output.accept(RoutersItems.CHEMICAL_UPGRADE_1);
                    output.accept(RoutersItems.CHEMICAL_UPGRADE_2);
                    output.accept(RoutersItems.CHEMICAL_UPGRADE_3);
                    output.accept(RoutersItems.CHEMICAL_UPGRADE_4);
                }

                if (ModList.get().isLoaded("ars_nouveau")){
                    output.accept(RoutersItems.SOURCE_UPGRADE_1);
                    output.accept(RoutersItems.SOURCE_UPGRADE_2);
                    output.accept(RoutersItems.SOURCE_UPGRADE_3);
                    output.accept(RoutersItems.SOURCE_UPGRADE_4);
                }

                if (ModList.get().isLoaded("industrialforegoingsouls")){
                    output.accept(RoutersItems.SOUL_UPGRADE);
                }

                //DISABLE FOR NOW, NEEDS TESTING
                /*
                if (ModList.get().isLoaded("pneumaticcraft")){
                    output.accept(RoutersItems.PRESSURE_UPGRADE);
                    output.accept(RoutersItems.ADVANCED_PRESSURE_UPGRADE);
                    output.accept(RoutersItems.HEAT_UPGRADE_PC);
                }
                 */




            }).build());
}
