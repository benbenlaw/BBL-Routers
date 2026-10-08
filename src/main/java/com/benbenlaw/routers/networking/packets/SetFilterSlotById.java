package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.screen.IdFilterSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record SetFilterSlotById(int slot, Identifier id) implements CustomPacketPayload {

    public static final Type<SetFilterSlotById> TYPE = new Type<>(Routers.identifier("set_filter_slot_by_id"));

    public static final IPayloadHandler<SetFilterSlotById> HANDLER = (packet, context) -> context.enqueueWork(() -> {
        Player player = context.player();
        if (packet.slot < 0 || packet.slot >= player.containerMenu.slots.size()) return;

        Slot slot = player.containerMenu.getSlot(packet.slot);
        if (slot instanceof IdFilterSlot filterSlot) filterSlot.setById(packet.id, player.level());
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, SetFilterSlotById> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SetFilterSlotById::slot,
            Identifier.STREAM_CODEC, SetFilterSlotById::id,
            SetFilterSlotById::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
