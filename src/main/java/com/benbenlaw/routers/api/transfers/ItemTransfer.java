package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.api.ImporterPullEngine;
import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.api.TransferEngine;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterCore;
import com.benbenlaw.routers.item.FilterItem;
import com.benbenlaw.routers.item.FilterType;
import com.benbenlaw.routers.item.RoutersDataComponents;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.util.ResourceScanState;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class ItemTransfer implements TransferModule<ResourceHandler<ItemResource>> {

    @Override
    public TagKey<Item> upgradeTag() {
        return RoutersTags.Items.ITEM_UPGRADES;
    }

    @Override
    public BlockCapability<ResourceHandler<ItemResource>, Direction> capability() {
        return Capabilities.Item.BLOCK;
    }

    @Override
    public ResourceHandler<ItemResource> createDistributor(DistributorBlockEntity distributor) {
        return new DistributorHandlers.Items(distributor);
    }

    @Override
    public int push(ServerLevel level, ExporterBlockEntity exporter) {

        if (exporter.getItemScanState().shouldSkip(level.getGameTime())) return exporter.lastImporterIndex;

        ResourceHandler<ItemResource> source = exporter.getConnectedResources().getItemHandler();

        if (source == null || source.size() == 0) return exporter.lastImporterIndex;

        boolean[] moved = {false};

        boolean stacks = exporter.isStackRoundRobin();

        int result = TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin && !stacks, exporter.lastImporterIndex,
                (srvLevel, entity, targetPos) -> {

                    ResourceHandler<ItemResource> target = getTargetHandler(srvLevel, entity, targetPos);
                    if (target == null) return false;

                    ImporterCore importer = ImporterCore.at(srvLevel, targetPos.pos());

                    int amount = entity.getUpgradeValue(RoutersTags.Items.ITEM_UPGRADES);
                    ResourceScanState scan = entity.getItemScanState();
                    int start = scan.nextScanStart(targetPos, source.size());
                    if (stacks) start = scan.stackStart(source.size());

                    int slot = moveFirstBounded(source, target, start, amount, resource -> {
                        boolean isWhitelist = !entity.isBlacklist();
                        int allowed = Integer.MAX_VALUE;

                        if (!ResourceHandlerUtil.isEmpty(entity.getFilterItemHandler())) {
                            int exporterAllowed = checkFilter(entity.getFilterItemHandler(), resource, isWhitelist, entity.isIgnoreNbt());
                            if (exporterAllowed <= 0) return 0;
                            allowed = Math.min(allowed, exporterAllowed);
                        }

                        if (importer != null && !ResourceHandlerUtil.isEmpty(importer.getFilterItemHandler())) {
                            boolean importerIsWhitelist = !importer.isBlacklist();
                            int importerAllowed = checkImporterFilter(importer.getFilterItemHandler(), resource, importerIsWhitelist, importer.isIgnoreNbt(), target);
                            if (importerAllowed <= 0) return 0;
                            allowed = Math.min(allowed, importerAllowed);
                        }

                        return allowed;
                    });
                    if (slot >= 0 && stacks) scan.stackMoved(slot);
                    moved[0] |= slot >= 0;
                    return slot >= 0;
                });

        exporter.getItemScanState().recordResult(level.getGameTime(), source.size(), moved[0]);
        return result;
    }

    @Override
    public int pull(ServerLevel level, ImporterCore importer) {

        if (importer.getItemScanState().shouldSkip(level.getGameTime())) return importer.lastExporterIndex;

        ResourceHandler<ItemResource> target = importer.getConnectedResources().getItemHandler();

        if (target == null || target.size() == 0) return importer.lastExporterIndex;

        boolean[] moved = {false};
        int[] lastSourceSize = {1};

        boolean stacks = importer.isStackRoundRobin();

        int result = ImporterPullEngine.run(level, importer, importer.exporterPositions, importer.lastExporterIndex, !stacks,
                (srvLevel, imp, exporterPos) -> {

                    ExporterBlockEntity exporter = getExporterAt(srvLevel, exporterPos);
                    if (exporter == null || !exporter.hasCorrectUpgrade(RoutersTags.Items.ITEM_UPGRADES)) return false;

                    ResourceHandler<ItemResource> source = getSourceHandler(srvLevel, exporter, imp, exporterPos);
                    if (source == null) return false;

                    int amount = exporter.getUpgradeValue(RoutersTags.Items.ITEM_UPGRADES);
                    lastSourceSize[0] = Math.max(source.size(), 1);
                    ResourceScanState scan = imp.getItemScanState();
                    int start = scan.nextScanStart(exporterPos, lastSourceSize[0]);
                    if (stacks) start = scan.stackStart(exporterPos, lastSourceSize[0]);

                    int slot = moveFirstBounded(source, target, start, amount, resource -> {
                        boolean isWhitelist = !exporter.isBlacklist();
                        int allowed = Integer.MAX_VALUE;

                        if (!ResourceHandlerUtil.isEmpty(exporter.getFilterItemHandler())) {
                            int exporterAllowed = checkFilter(exporter.getFilterItemHandler(), resource, isWhitelist, exporter.isIgnoreNbt());
                            if (exporterAllowed <= 0) return 0;
                            allowed = Math.min(allowed, exporterAllowed);
                        }

                        boolean importerIsWhitelist = !imp.isBlacklist();
                        if (!ResourceHandlerUtil.isEmpty(imp.getFilterItemHandler())) {
                            int importerAllowed = checkImporterFilter(imp.getFilterItemHandler(), resource, importerIsWhitelist, imp.isIgnoreNbt(), target);
                            if (importerAllowed <= 0) return 0;
                            allowed = Math.min(allowed, importerAllowed);
                        }

                        return allowed;
                    });
                    if (slot >= 0 && stacks) scan.stackMoved(exporterPos, slot);
                    moved[0] |= slot >= 0;
                    return slot >= 0;
                });

        importer.getItemScanState().recordResult(level.getGameTime(), lastSourceSize[0], moved[0]);
        return result;
    }

    private interface ItemPredicate {
        int allowedAmount(ItemResource resource);
    }

    // The slot something was moved out of, or -1.
    private static int moveFirstBounded(ResourceHandler<ItemResource> source, ResourceHandler<ItemResource> target,
                                             int start, int amount, ItemPredicate predicate) {
        if (amount <= 0) return -1;

        int size = source.size();
        int scanLimit = Math.min(size, StartupConfig.maxInventoryScanPerOperation.get());

        for (int offset = 0; offset < scanLimit; offset++) {
            int slot = (start + offset) % size;
            ItemResource resource = source.getResource(slot);
            if (resource.isEmpty()) continue;

            int allowed = predicate.allowedAmount(resource);
            if (allowed <= 0) continue;

            try (Transaction tx = Transaction.open(null)) {
                int available = (int) Math.min(Math.min(source.getAmountAsLong(slot), amount), allowed);
                int accepted = target.insert(resource, available, tx);

                if (accepted > 0 && source.extract(slot, resource, accepted, tx) > 0) {
                    tx.commit();
                    return slot;
                }
            }
        }
        return -1;
    }

    @Nullable
    private static ExporterBlockEntity getExporterAt(ServerLevel importerLevel, GlobalPos exporterPos) {
        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;
        return exporterLevel.getBlockEntity(exporterPos.pos()) instanceof ExporterBlockEntity exporter ? exporter : null;
    }

    @Nullable
    private static ResourceHandler<ItemResource> getSourceHandler(ServerLevel importerLevel, ExporterBlockEntity exporter, ImporterCore importer, GlobalPos exporterPos) {
        if (!exporterPos.dimension().equals(importerLevel.dimension()) && !exporter.canDoDimensionalTravel()) return null;

        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;

        return importer.getItemSourceCache().get(exporterLevel, exporterPos);
    }

    static int checkFilter(FilterItemHandler filterHandler, ItemResource resource, boolean isWhitelist, boolean ignoreNbt) {
        ItemStack incoming = resource.toStack();
        boolean hasAnyFilter = false;
        boolean foundMatch = false;

        for (int i = 0; i < filterHandler.size(); i++) {
            ItemStack filterStack = filterHandler.getResource(i).toStack();
            if (filterStack.isEmpty()) continue;

            hasAnyFilter = true;

            if (filterStack.getItem() instanceof FilterItem filterItem) {
                if (filterItem.matches(filterStack, incoming)) {
                    foundMatch = true;
                    break;
                }
            } else {
                boolean matches = ignoreNbt
                        ? resource.getItem() == filterStack.getItem()
                        : ItemStack.isSameItemSameComponents(incoming, filterStack);
                if (matches) {
                    foundMatch = true;
                    break;
                }
            }
        }

        if (!hasAnyFilter) return Integer.MAX_VALUE;
        return (isWhitelist == foundMatch) ? Integer.MAX_VALUE : 0;
    }

    private static int checkImporterFilter(FilterItemHandler filterHandler, ItemResource resource, boolean isWhitelist, boolean ignoreNbt, @Nullable ResourceHandler<ItemResource> adjacentHandler) {
        ItemStack incoming = resource.toStack();
        boolean hasAnyFilter = false;
        boolean foundMatch = false;

        for (int i = 0; i < filterHandler.size(); i++) {
            ItemStack filterStack = filterHandler.getResource(i).toStack();
            if (filterStack.isEmpty()) continue;

            hasAnyFilter = true;

            if (filterStack.getItem() instanceof FilterItem filterItem) {
                if (filterItem.filterType == FilterType.STOCK) {
                    var stock = filterStack.get(RoutersDataComponents.STOCK_FILTER.get());
                    if (stock == null) continue;
                    if (!ItemStack.isSameItemSameComponents(incoming, stock.stack())) continue;

                    int currentCount = countMatchingItems(adjacentHandler, stock.stack());
                    return Math.max(stock.amount() - currentCount, 0);
                }

                if (filterItem.matches(filterStack, incoming)) {
                    foundMatch = true;
                    break;
                }
            } else {
                boolean matches = ignoreNbt
                        ? resource.getItem() == filterStack.getItem()
                        : ItemStack.isSameItemSameComponents(incoming, filterStack);
                if (matches) {
                    foundMatch = true;
                    break;
                }
            }
        }

        if (!hasAnyFilter) return Integer.MAX_VALUE;
        boolean allowed = isWhitelist ? foundMatch : !foundMatch;
        return allowed ? Integer.MAX_VALUE : 0;
    }

    private static int countMatchingItems(@Nullable ResourceHandler<ItemResource> handler, ItemStack target) {
        if (handler == null) return 0;
        int count = 0;
        for (int i = 0; i < handler.size(); i++) {
            ItemResource res = handler.getResource(i);
            if (!res.isEmpty() && ItemStack.isSameItemSameComponents(res.toStack(), target)) {
                count += (int) handler.getAmountAsInt(i);
            }
        }
        return count;
    }

    private ResourceHandler<ItemResource> getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos())) return null;

        if (targetLevel.getBlockEntity(pos.pos()) instanceof DistributorBlockEntity distributor) {
            return distributor.getDistributor(this);
        }

        return exporter.getItemTargetCache().get(targetLevel, pos);
    }
}
