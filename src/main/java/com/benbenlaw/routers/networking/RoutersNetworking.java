package com.benbenlaw.routers.networking;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.networking.packets.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RoutersNetworking {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Routers.MOD_ID);

        registrar.playToServer(JEISyncToMenu.TYPE, JEISyncToMenu.STREAM_CODEC, JEISyncToMenu.HANDLER);
        registrar.playToServer(JEISyncToMenuFluid.TYPE, JEISyncToMenuFluid.STREAM_CODEC, JEISyncToMenuFluid.HANDLER);

        if (ModList.get().isLoaded("mekanism")) {
            registrar.playToServer(JEISyncToMenuChemical.TYPE, JEISyncToMenuChemical.STREAM_CODEC, JEISyncToMenuChemical.HANDLER);
            registrar.playToClient(SyncChemicalListToClient.TYPE, SyncChemicalListToClient.STREAM_CODEC, SyncChemicalListToClient.HANDLER);

        }

        registrar.playToServer(FilterItemUpdate.TYPE, FilterItemUpdate.STREAM_CODEC, FilterItemUpdate.HANDLER);
        registrar.playToServer(RouterSwitchTab.TYPE, RouterSwitchTab.STREAM_CODEC, RouterSwitchTab.HANDLER);

        registrar.playToClient(SyncFluidListToClient.TYPE, SyncFluidListToClient.STREAM_CODEC, SyncFluidListToClient.HANDLER);

    }
}
