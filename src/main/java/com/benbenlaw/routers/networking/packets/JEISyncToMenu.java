package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.screen.ExporterMenu;
import com.benbenlaw.routers.screen.ImporterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record JEISyncToMenu(int slot, ItemStack stack) implements CustomPacketPayload {

    public static final Type<JEISyncToMenu> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Routers.MOD_ID, "jei_sync_to_menu"));

    public static final IPayloadHandler<JEISyncToMenu> HANDLER = (packet, context) -> {
        context.player().level().getServer().execute(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            int slot = packet.slot();

            ItemStack sanitized = packet.stack().isEmpty()
                    ? ItemStack.EMPTY
                    : new ItemStack(packet.stack().getItem(), 1);

            if (player.containerMenu instanceof ExporterMenu menu) {
                if (slot < 0 || slot >= menu.filterInventory.getContainerSize()) return;
                menu.filterInventory.setItem(slot, sanitized);
            }
            if (player.containerMenu instanceof ImporterMenu menu) {
                if (slot < 0 || slot >= menu.filterInventory.getContainerSize()) return;
                menu.filterInventory.setItem(slot, sanitized);
            }
        });
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, JEISyncToMenu> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT , JEISyncToMenu::slot,
            ItemStack.STREAM_CODEC, JEISyncToMenu::stack,
            JEISyncToMenu::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
