package com.benbenlaw.routers.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.fluid.FilterFluidHandler;
import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.core.block.entity.handler.item.InputItemHandler;
import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.item.UpgradeItem;
import com.benbenlaw.routers.screen.ExporterMenu;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.transfers.RoutersTransfers;
import com.benbenlaw.routers.util.ConnectedResources;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExporterBlockEntity extends SyncableBlockEntity implements MenuProvider {

    public List<GlobalPos> importerPositions;
    public final ContainerData data;
    public GlobalPos exporterPos;

    public boolean isRoundRobin;
    private boolean canDoDimensionalTravel;
    private boolean ignoreNbt;
    private boolean isBlacklist;

    private final InputItemHandler upgradeItemHandler = new InputItemHandler(this, 9, (i, stack) ->
            stack.is(RoutersTags.Items.UPGRADES) && !hasUpgradeTypeAlready(stack)) {
        @Override
        protected int getCapacity(int index, ItemResource resource) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            isRoundRobin = false;
            canDoDimensionalTravel = false;
            ignoreNbt = false;
            isBlacklist = false;

            for (int i = 0; i < upgradeItemHandler.size(); i++) {
                ItemResource resource = upgradeItemHandler.getResource(i);
                ItemStack stack = resource.toStack();
                if (stack.isEmpty()) continue;

                if (stack.is(RoutersItems.ROUND_ROBIN_UPGRADE)) {
                    isRoundRobin = true;
                }
                if (stack.is(RoutersItems.DIMENSIONAL_UPGRADE)) {
                    canDoDimensionalTravel = true;
                }
                if (stack.is(RoutersItems.IGNORE_NBT_UPGRADE)) {
                    ignoreNbt = true;
                }
                if (stack.is(RoutersItems.BLACKLIST_UPGRADE)) {
                    isBlacklist = true;
                }
            }

            super.onContentsChanged(index, previousContents);
        }

    };

    private final FilterItemHandler filterItemHandler = new FilterItemHandler(this, 18);
    private final FilterFluidHandler filterFluidHandler = new FilterFluidHandler(this, 18);

    private ConnectedResources connectedResources;
    public int lastImporterIndex = 0;

    public ExporterBlockEntity(BlockPos pos, BlockState state) {
        super(RoutersBlockEntities.EXPORTER_BLOCK_ENTITY.get(), pos, state);
        assert level != null;
        this.exporterPos = null;
        this.importerPositions = new ArrayList<>();

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

    public boolean canDoDimensionalTravel() {
        return canDoDimensionalTravel;
    }

    public boolean isIgnoreNbt() {
        return ignoreNbt;
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }

    public boolean hasUpgradeTypeAlready(ItemStack stack) {
        for (int i = 0; i < upgradeItemHandler.size(); i++) {
            ItemStack existing = upgradeItemHandler.getResource(i).toStack();
            if (existing.isEmpty()) continue;

            for (TagKey<Item> tag : getUpgradeTypeTags()) {
                if (stack.is(tag) && existing.is(tag)) {
                    return true;
                }

            }
        }
        return false;
    }

    public static List<TagKey<Item>> getUpgradeTypeTags() {
        return List.of(
                RoutersTags.Items.SPEED_UPGRADES,
                RoutersTags.Items.ITEM_UPGRADES,
                RoutersTags.Items.FLUID_UPGRADES,
                RoutersTags.Items.RF_UPGRADES
        );
    }

    public void tick() {

        assert level != null;
        if (level.isClientSide()) return;
        BlockState currentState = level.getBlockState(worldPosition);
        if (!currentState.hasProperty(RouterBlock.WORKING) || !currentState.getValue(RouterBlock.WORKING)) return;

        if (level.getGameTime() % 100 == 0) {
            validateImporterPositions(importerPositions);
        }

        if (exporterPos == null) {
            assert level != null;
            exporterPos = GlobalPos.of(level.dimension(), this.worldPosition);
        }

        if (connectedResources == null) {
            connectedResources = getConnectedResources();
        }

        if (connectedResources != null) {

            if (level.getGameTime() % getSpeedPerOperation() == 0) {
                moveResources();
            }
        }
    }

    public int getUpgradeValue(TagKey<Item> tag) {
        for (int i = 0; i < upgradeItemHandler.size(); i++) {
            ItemStack stack = upgradeItemHandler.getResource(i).toStack();
            if (stack.is(tag) && stack.getItem() instanceof UpgradeItem upgrade) {
                return upgrade.getExtractAmount();
            }
        }
        return 0;
    }

    public void moveResources() {
        assert level != null;
        Registry<TransferModule> registry = level.registryAccess().lookupOrThrow(RoutersTransfers.TRANSFER_MODULE_KEY);

        for (TransferModule module : registry) {
            if (hasCorrectUpgrade(module.upgradeTag())) {
                this.lastImporterIndex = module.logic().apply((ServerLevel) level, this);
            }
        }
    }

    public void validateImporterPositions(List<GlobalPos> importerPositions) {
        importerPositions.removeIf(pos -> {
            assert level != null;
            ServerLevel importerLevel = Objects.requireNonNull(level.getServer()).getLevel(pos.dimension());
            return importerLevel == null || importerLevel.getBlockEntity(pos.pos()) == null;
        });
        sync();
    }


    public boolean hasCorrectUpgrade(TagKey<Item> tag) {
        for (int i = 0; i < upgradeItemHandler.size(); i++) {
            ItemResource resource = upgradeItemHandler.getResource(i);
            if (resource.toStack().is(tag)) {
                return true;
            }
        }
        return false;
    }

    public int getSpeedPerOperation() {
        int speed = StartupConfig.defaultSpeedPerOperation.get();

        for (int i = 0; i < upgradeItemHandler.size(); i++) {
            ItemStack stack = upgradeItemHandler.getResource(i).toStack();

            if (stack.is(RoutersTags.Items.SPEED_UPGRADES) && stack.getItem() instanceof UpgradeItem upgradeItem) {
                speed = upgradeItem.getExtractAmount();
            }
        }
        return speed;
    }

    public BlockPos getTargetBlockPos(BlockPos startPos) {
        Direction facing = level.getBlockState(startPos).getValue(RouterBlock.FACING);
        return startPos.relative(facing);
    }

    public ConnectedResources getConnectedResources() {
        return connectedResources = new ConnectedResources(new GlobalPos(exporterPos.dimension(), getTargetBlockPos(worldPosition)));
    }

    public InputItemHandler getUpgradeItemHandler() {
        return upgradeItemHandler;
    }

    public FilterItemHandler getFilterItemHandler() {
        return filterItemHandler;
    }

    public FilterFluidHandler getFilterFluidHandler() {
        return filterFluidHandler;
    }

    public boolean toggleImporterPosition(GlobalPos clickedPos) {
        GlobalPos existing = null;

        for (GlobalPos pos : importerPositions) {
            if (pos.dimension().equals(clickedPos.dimension()) &&
                    pos.pos().equals(clickedPos.pos())) {
                existing = pos;
                break;
            }
        }

        boolean added = existing == null;

        if (existing != null) {
            importerPositions.remove(existing);
            setChanged();
        } else {
            importerPositions.add(clickedPos);
            setChanged();
        }

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

            ServerLevel importerLevel = level.getServer() != null ? level.getServer().getLevel(clickedPos.dimension()) : null;
            if (importerLevel != null && importerLevel.getBlockEntity(clickedPos.pos()) instanceof ImporterBlockEntity importer) {
                GlobalPos thisExporterPos = GlobalPos.of(level.dimension(), this.worldPosition);
                if (added) {
                    importer.addExporterPosition(thisExporterPos);
                } else {
                    importer.removeExporterPosition(thisExporterPos);
                }
            }
        }

        return added;
    }


    public void removeImporterPosition(GlobalPos importerGlobalPos) {
        boolean removed = importerPositions.removeIf(pos ->
                pos.dimension().equals(importerGlobalPos.dimension()) && pos.pos().equals(importerGlobalPos.pos()));
        if (removed) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public boolean hasUpgrade(ButtonType type) {
        for (int i = 0; i < upgradeItemHandler.size(); i++) {
            if (upgradeItemHandler.getResource(i).toStack().is(type.getUnlockedBy())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.routers.exporter");
    }

    @Override
    public AbstractContainerMenu createMenu(int container, @NotNull Inventory inventory, @NotNull Player player) {
        return new ExporterMenu(container, inventory, this.getBlockPos(), data);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {

        upgradeItemHandler.serialize(output.child("upgradeItems"));
        filterItemHandler.serialize(output.child("itemFilter"));
        filterFluidHandler.serialize(output.child("fluidFilter"));

        output.putBoolean("isRoundRobin", isRoundRobin);
        output.putBoolean("canDoDimensionalTravel", canDoDimensionalTravel);
        output.putBoolean("ignoreNbt", ignoreNbt);
        output.putBoolean("isBlacklist", isBlacklist);

        if (importerPositions != null && !importerPositions.isEmpty()) {
            var list = output.list("importerPositions", GlobalPos.CODEC);
            for (GlobalPos pos : importerPositions) {
                list.add(pos);
            }
        }

        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {

        upgradeItemHandler.deserialize(input.childOrEmpty("upgradeItems"));
        filterItemHandler.deserialize(input.childOrEmpty("itemFilter"));
        filterFluidHandler.deserialize(input.childOrEmpty("fluidFilter"));

        isRoundRobin = input.getBooleanOr("isRoundRobin", false);
        canDoDimensionalTravel = input.getBooleanOr("canDoDimensionalTravel", false);
        ignoreNbt = input.getBooleanOr("ignoreNbt", false);
        isBlacklist = input.getBooleanOr("isBlacklist", false);

        importerPositions = new ArrayList<>();

        input.listOrEmpty("importerPositions", GlobalPos.CODEC)
                .forEach(importerPositions::add);

        super.loadAdditional(input);
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        dropInventoryContents(upgradeItemHandler);

        if (level == null || level.isClientSide() || importerPositions == null || importerPositions.isEmpty()) return;

        GlobalPos thisExporterPos = GlobalPos.of(level.dimension(), pos);

        for (GlobalPos importerGlobalPos : new ArrayList<>(importerPositions)) {
            ServerLevel importerLevel = level.getServer() != null ? level.getServer().getLevel(importerGlobalPos.dimension()) : null;
            if (importerLevel == null) continue;

            if (importerLevel.getBlockEntity(importerGlobalPos.pos()) instanceof ImporterBlockEntity importer) {
                importer.removeExporterPosition(thisExporterPos);
            }
        }
    }


}