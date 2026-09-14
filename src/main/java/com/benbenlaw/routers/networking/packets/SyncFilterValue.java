package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.item.FilterItem;
import com.benbenlaw.routers.item.RoutersDataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record SyncFilterValue(FilterField field, String value) implements CustomPacketPayload {

    public enum FilterField { TAG, MOD }

    public static final Type<SyncFilterValue> TYPE = new Type<>(Routers.identifier("sync_filter_value"));

    private static final int MAX_VALUE_LENGTH = 256;

    public static final IPayloadHandler<SyncFilterValue> HANDLER = (packet, context) -> {
        context.player().level().getServer().execute(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack held = player.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!(held.getItem() instanceof FilterItem)) return;

            String value = packet.value();
            if (value == null || value.length() > MAX_VALUE_LENGTH) return;

            switch (packet.field()) {
                case TAG -> {
                    if (value.isEmpty()) {
                        held.remove(RoutersDataComponents.TAG_FILTER.get());
                        return;
                    }
                    Identifier tagLoc = Identifier.tryParse(value);
                    if (tagLoc == null) return;

                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLoc);
                    if (!serverTagNotEmpty(player, tagKey)) return;

                    held.set(RoutersDataComponents.TAG_FILTER.get(), tagLoc);
                }
                case MOD -> {
                    if (value.isEmpty()) {
                        held.remove(RoutersDataComponents.MOD_FILTER.get());
                        return;
                    }
                    if (!ModList.get().isLoaded(value)) return;

                    held.set(RoutersDataComponents.MOD_FILTER.get(), value);
                }
            }
        });
    };

    private static boolean serverTagNotEmpty(ServerPlayer player, TagKey<Item> tagKey) {
        return !player.level().registryAccess()
                .lookupOrThrow(Registries.ITEM)
                .get(tagKey)
                .map(named -> named.size() == 0)
                .orElse(true);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFilterValue> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(FilterField::valueOf, Enum::name), SyncFilterValue::field,
            ByteBufCodecs.stringUtf8(MAX_VALUE_LENGTH), SyncFilterValue::value,
            SyncFilterValue::new
    );

    @Override
    public Type<SyncFilterValue> type() {
        return TYPE;
    }
}