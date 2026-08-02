package com.benbenlaw.routers.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.fluid.FilterFluidHandler;
import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.screen.ImporterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImporterBlockEntity extends SyncableBlockEntity implements MenuProvider {

    public final ContainerData data;
    public final GlobalPos importerPos;
    public List<GlobalPos> exporterPositions;
    private final FilterItemHandler filterItemHandler = new FilterItemHandler(this, 9);
    private final FilterFluidHandler filterFluidHandler = new FilterFluidHandler(this, 9);

    public ImporterBlockEntity(BlockPos pos, BlockState state) {
        super(RoutersBlockEntities.IMPORTER_BLOCK_ENTITY.get(), pos, state);
        this.importerPos = null;
        this.exporterPositions = new ArrayList<>();

        this.data = new ContainerData() {;
            @Override
            public int get(int index) {
                return 0;
            }

            @Override
            public void set(int index, int value) {

            }

            @Override
            public int getCount() {
                return 0;
            }
        };
    }

    public void tick() {
        assert level != null;
        if (level.isClientSide()) return;

        if (level.getGameTime() % 100 == 0) {
            validateExporterPositions();
        }
    }

    public void validateExporterPositions() {
        if (exporterPositions == null || exporterPositions.isEmpty()) return;
        assert level != null;
        exporterPositions.removeIf(pos -> {
            ServerLevel exporterLevel = Objects.requireNonNull(level.getServer()).getLevel(pos.dimension());
            return exporterLevel == null || exporterLevel.getBlockEntity(pos.pos()) == null;
        });
        sync();
    }

    public boolean addExporterPosition(GlobalPos exporterGlobalPos) {
        for (GlobalPos pos : exporterPositions) {
            if (pos.dimension().equals(exporterGlobalPos.dimension()) && pos.pos().equals(exporterGlobalPos.pos())) {
                return false;
            }
        }
        exporterPositions.add(exporterGlobalPos);
        setChanged();
        notifyClient();
        return true;
    }

    public boolean removeExporterPosition(GlobalPos exporterGlobalPos) {
        boolean removed = exporterPositions.removeIf(pos ->
                pos.dimension().equals(exporterGlobalPos.dimension()) && pos.pos().equals(exporterGlobalPos.pos()));
        if (removed) {
            setChanged();
            notifyClient();
        }
        return removed;
    }

    private void notifyClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public FilterItemHandler getFilterItemHandler() {
        return filterItemHandler;
    }

    public FilterFluidHandler getFilterFluidHandler() {
        return filterFluidHandler;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.routers.importer");
    }

    @Override
    public AbstractContainerMenu createMenu(int container, @NotNull Inventory inventory, @NotNull Player player) {
        return new ImporterMenu(container, inventory, this.getBlockPos(), data);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {

        filterItemHandler.serialize(output.child("itemFilter"));
        filterFluidHandler.serialize(output.child("fluidFilter"));

        if (exporterPositions != null && !exporterPositions.isEmpty()) {
            var list = output.list("exporterPositions", GlobalPos.CODEC);
            for (GlobalPos pos : exporterPositions) {
                list.add(pos);
            }
        }

        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {

        filterItemHandler.deserialize(input.childOrEmpty("itemFilter"));
        filterFluidHandler.deserialize(input.childOrEmpty("fluidFilter"));

        exporterPositions = new ArrayList<>();
        input.listOrEmpty("exporterPositions", GlobalPos.CODEC)
                .forEach(exporterPositions::add);

        super.loadAdditional(input);
    }

    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        if (level == null || level.isClientSide() || exporterPositions == null || exporterPositions.isEmpty()) return;

        GlobalPos thisImporterPos = GlobalPos.of(level.dimension(), pos);

        for (GlobalPos exporterGlobalPos : new ArrayList<>(exporterPositions)) {
            ServerLevel exporterLevel = level.getServer() != null ? level.getServer().getLevel(exporterGlobalPos.dimension()) : null;
            if (exporterLevel == null) continue;

            if (exporterLevel.getBlockEntity(exporterGlobalPos.pos()) instanceof ExporterBlockEntity exporter) {
                exporter.removeImporterPosition(thisImporterPos);
            }
        }
    }
}