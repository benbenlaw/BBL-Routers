package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.entity.RouterBlockEntity;
import com.benbenlaw.routers.screen.RouterExportMenu;
import com.benbenlaw.routers.screen.RouterImportMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/** Sent when a Router tab is clicked: reopens the Router on the export or import tab. */
public record RouterSwitchTab(boolean importTab) implements CustomPacketPayload {

    public static final Type<RouterSwitchTab> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Routers.MOD_ID, "router_switch_tab"));

    public static final IPayloadHandler<RouterSwitchTab> HANDLER = (packet, context) -> {
        context.player().level().getServer().execute(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            BlockPos pos;
            if (player.containerMenu instanceof RouterExportMenu menu) {
                pos = menu.getBlockPos();
            } else if (player.containerMenu instanceof RouterImportMenu menu) {
                pos = menu.getBlockPos();
            } else {
                return;
            }

            if (!player.containerMenu.stillValid(player)) return;
            if (!(player.level().getBlockEntity(pos) instanceof RouterBlockEntity router)) return;

            // Close server side only, so the client swaps screens directly and keeps the cursor where it is
            player.doCloseContainer();
            router.openMenu(player, packet.importTab());
        });
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, RouterSwitchTab> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, RouterSwitchTab::importTab,
            RouterSwitchTab::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
