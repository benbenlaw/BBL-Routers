package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.item.RoutersDataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record FilterItemUpdate(String value) implements CustomPacketPayload {

    public static final Type<FilterItemUpdate> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Routers.MOD_ID, "filter_item_update"));

    private static final int MAX_VALUE_LENGTH = 256;

    public static final IPayloadHandler<FilterItemUpdate> HANDLER = (packet, context) -> {
        context.player().level().getServer().execute(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            String value = packet.value();
            if (value == null || value.length() > MAX_VALUE_LENGTH) return;

            ItemStack heldItem = player.getItemBySlot(EquipmentSlot.MAINHAND);

            if (heldItem.is(RoutersItems.TAG_FILTER)) {
                if (value.isEmpty()) {
                    heldItem.remove(RoutersDataComponents.TAG_FILTER.get());
                    return;
                }
                ResourceLocation tagLocation = ResourceLocation.tryParse(value);
                if (tagLocation == null) return;

                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLocation);
                if (!serverTagNotEmpty(player, tagKey)) return;

                heldItem.set(RoutersDataComponents.TAG_FILTER.get(), tagLocation);

            } else if (heldItem.is(RoutersItems.MOD_FILTER)) {
                if (value.isEmpty()) {
                    heldItem.remove(RoutersDataComponents.MOD_FILTER.get());
                    return;
                }
                if (!ModList.get().isLoaded(value)) return;

                heldItem.set(RoutersDataComponents.MOD_FILTER.get(), value);
            }
        });
    };

    private static boolean serverTagNotEmpty(ServerPlayer player, TagKey<Item> tagKey) {
        return !player.level().registryAccess()
                .registryOrThrow(Registries.ITEM)
                .getTag(tagKey)
                .map(named -> named.size() == 0)
                .orElse(true);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, FilterItemUpdate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_VALUE_LENGTH), FilterItemUpdate::value,
            FilterItemUpdate::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}