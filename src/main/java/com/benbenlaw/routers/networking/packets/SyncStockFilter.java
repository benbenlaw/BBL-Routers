package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.item.FilterItem;
import com.benbenlaw.routers.item.RoutersDataComponents;
import com.benbenlaw.routers.item.StockFilter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record SyncStockFilter(ItemStack stockStack, int amount) implements CustomPacketPayload {

    public static final Type<SyncStockFilter> TYPE = new Type<>(Routers.identifier("sync_stock_filter"));

    public static final IPayloadHandler<SyncStockFilter> HANDLER = (packet, context) -> {
        context.player().level().getServer().execute(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack held = player.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!(held.getItem() instanceof FilterItem)) return;

            ItemStack clientStock = packet.stockStack();
            int amount = Math.max(0, packet.amount());

            ItemStack sanitizedStock = clientStock.isEmpty()
                    ? ItemStack.EMPTY
                    : new ItemStack(clientStock.getItem(), 1);

            held.set(RoutersDataComponents.STOCK_FILTER.get(), new StockFilter(sanitizedStock, amount));
        });
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStockFilter> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, SyncStockFilter::stockStack,
            ByteBufCodecs.VAR_INT, SyncStockFilter::amount,
            SyncStockFilter::new
    );

    @Override
    public Type<SyncStockFilter> type() {
        return TYPE;
    }
}