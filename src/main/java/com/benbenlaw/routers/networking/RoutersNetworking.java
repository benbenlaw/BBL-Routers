package com.benbenlaw.routers.networking;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.networking.packets.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RoutersNetworking {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Routers.MOD_ID);

        registrar.playToServer(OpenMenu.TYPE, OpenMenu.STREAM_CODEC, OpenMenu.HANDLER);
        registrar.playToServer(BackMenu.TYPE, BackMenu.STREAM_CODEC, BackMenu.HANDLER);

        registrar.playToServer(SyncFilterValue.TYPE, SyncFilterValue.STREAM_CODEC, SyncFilterValue.HANDLER);
        registrar.playToServer(SyncStockFilter.TYPE, SyncStockFilter.STREAM_CODEC, SyncStockFilter.HANDLER);

    }
}
