package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.ImporterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.block.entity.RouterBlockEntity;
import com.benbenlaw.routers.screen.ExporterMenu;
import com.benbenlaw.routers.screen.ImporterMenu;
import com.benbenlaw.routers.screen.util.GhostSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record JEISyncToMenuFluid(int slot, FluidStack stack) implements CustomPacketPayload {

    public static final Type<JEISyncToMenuFluid> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Routers.MOD_ID, "jei_sync_to_menu_fluid"));

    public static final IPayloadHandler<JEISyncToMenuFluid> HANDLER = (packet, context) -> {

        ServerPlayer player = (ServerPlayer) context.player();
        Level level = player.level();

        if (player.containerMenu instanceof ExporterMenu menu) {
            Slot slot = menu.getSlot(packet.slot);
            if (slot instanceof GhostSlot ghostSlot) {
                ghostSlot.setFluid(packet.stack);

                if (RouterBlockEntity.exporterOf(level.getBlockEntity(menu.getBlockPos())) instanceof ExporterBlockEntity blockEntity) {
                    int slotIndex = packet.slot();
                    if (slotIndex < blockEntity.getFluidFilters().size()) {
                        blockEntity.getFluidFilters().set(slotIndex, packet.stack());
                        blockEntity.getFilters().set(slotIndex, ItemStack.EMPTY);
                        blockEntity.setChanged();
                    }
                }
            }
        }

        if (player.containerMenu instanceof ImporterMenu menu) {
            Slot slot = menu.getSlot(packet.slot);
            if (slot instanceof GhostSlot ghostSlot) {
                ghostSlot.setFluid(packet.stack);

                if (level.getBlockEntity(menu.getBlockPos()) instanceof ImporterBlockEntity blockEntity) {
                    int slotIndex = packet.slot();
                    if (slotIndex < blockEntity.getFluidFilters().size()) {
                        blockEntity.getFluidFilters().set(slotIndex, packet.stack());
                        blockEntity.getFilters().set(slotIndex, ItemStack.EMPTY);
                        blockEntity.setChanged();
                    }
                }
            }
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, JEISyncToMenuFluid> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT , JEISyncToMenuFluid::slot,
            FluidStack.STREAM_CODEC, JEISyncToMenuFluid::stack,
            JEISyncToMenuFluid::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
