package com.benbenlaw.routers.block.entity;

import com.benbenlaw.routers.block.ExporterBlock;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.networking.packets.SyncChemicalListToClient;
import com.benbenlaw.routers.networking.packets.SyncFluidListToClient;
import com.benbenlaw.routers.screen.RouterExportMenu;
import com.benbenlaw.routers.screen.RouterImportMenu;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Exporter and Importer in one block. The Router is an Importer (so Exporters can link to it as-is)
 * and carries an Exporter inside it that ticks and saves together with it.
 * The inherited filters are the import filters; the inner Exporter has the export filters and upgrades.
 */
public class RouterBlockEntity extends ImporterBlockEntity {

    private final ExporterBlockEntity exporter;

    public RouterBlockEntity(BlockPos pos, BlockState state) {
        super(RoutersBlockEntities.ROUTER_BLOCK_ENTITY.get(), pos, state);
        BlockState exporterState = RoutersBlocks.EXPORTER_BLOCK.get().defaultBlockState()
                .setValue(ExporterBlock.FACING, state.getValue(ExporterBlock.FACING));
        this.exporter = new ExporterBlockEntity(pos, exporterState);
    }

    public ExporterBlockEntity getExporter() {
        return exporter;
    }

    /** For code that expects an Exporter at a position: returns the Router's inner Exporter, or the block entity unchanged. */
    public static BlockEntity exporterOf(BlockEntity blockEntity) {
        return blockEntity instanceof RouterBlockEntity router ? router.getExporter() : blockEntity;
    }

    @Override
    public void setLevel(@NotNull Level level) {
        super.setLevel(level);
        exporter.setLevel(level);
    }

    @Override
    public void tick() {
        super.tick();
        exporter.tick();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.routers.router_block");
    }

    @Override
    public AbstractContainerMenu createMenu(int container, @NotNull Inventory inventory, @NotNull Player player) {
        return new RouterExportMenu(container, inventory, this.getBlockPos(), exporter.data);
    }

    /** Opens the export or import tab of this router and syncs that tab's fluid/chemical filters. */
    public void openMenu(ServerPlayer player, boolean importTab) {
        BlockPos pos = getBlockPos();
        player.openMenu(new SimpleMenuProvider(
                        (windowId, playerInventory, playerEntity) -> importTab
                                ? new RouterImportMenu(windowId, playerInventory, pos, data)
                                : new RouterExportMenu(windowId, playerInventory, pos, exporter.data),
                        getDisplayName()),
                buf -> buf.writeBlockPos(pos));

        PacketDistributor.sendToPlayer(player, new SyncFluidListToClient(pos, importTab ? getFluidFilters() : exporter.getFluidFilters()));
        if (ModList.get().isLoaded("mekanism")) {
            PacketDistributor.sendToPlayer(player, new SyncChemicalListToClient(pos,
                    (List<ChemicalStack>) (importTab ? getChemicalFilters() : exporter.getChemicalFilters())));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(compoundTag, provider);
        compoundTag.put("Export", exporter.saveWithoutMetadata(provider));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(compoundTag, provider);
        exporter.loadWithComponents(compoundTag.getCompound("Export"), provider);
    }
}
