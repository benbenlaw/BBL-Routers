package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.api.ImporterPullEngine;
import com.benbenlaw.routers.api.TransferEngine;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.item.FilterItem;
import com.benbenlaw.routers.item.FilterType;
import com.benbenlaw.routers.item.RoutersDataComponents;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class ItemTransfer {

    public static int transferItems(ServerLevel level, ExporterBlockEntity exporter) {

        if (exporter.getItemScanState().shouldSkip(level.getGameTime())) return exporter.lastImporterIndex;

        ResourceHandler<ItemResource> source = exporter.getConnectedResources().getItemHandler();

        if (source == null || source.size() == 0) return exporter.lastImporterIndex;

        boolean[] moved = {false};

        int result = TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetPos) -> {

                    ResourceHandler<ItemResource> target = getTargetHandler(srvLevel, entity, targetPos);
                    if (target == null) return false;

                    ImporterBlockEntity importer = srvLevel.getBlockEntity(targetPos.pos()) instanceof ImporterBlockEntity imp ? imp : null;

                    int amount = entity.getUpgradeValue(RoutersTags.Items.ITEM_UPGRADES);
                    int start = entity.getItemScanState().nextScanStart(source.size());

                    boolean success = moveFirstBounded(source, target, start, amount, resource -> {
                        boolean isWhitelist = !entity.isBlacklist();

                        if (!ResourceHandlerUtil.isEmpty(entity.getFilterItemHandler())) {
                            if (!checkFilter(entity.getFilterItemHandler(), resource, isWhitelist, entity.isIgnoreNbt())) {
                                return false;
                            }
                        }

                        if (importer != null && !ResourceHandlerUtil.isEmpty(importer.getFilterItemHandler())) {
                            boolean importerIsWhitelist = !importer.isBlacklist();
                            if (!checkImporterFilter(importer.getFilterItemHandler(), resource, importerIsWhitelist, importer.isIgnoreNbt(), target)) {
                                return false;
                            }
                        }

                        return true;
                    });
                    moved[0] |= success;
                    return success;
                });

        exporter.getItemScanState().recordResult(level.getGameTime(), source.size(), moved[0]);
        return result;
    }

    public static int pullItems(ServerLevel level, ImporterBlockEntity importer) {

        if (importer.getItemScanState().shouldSkip(level.getGameTime())) return importer.lastExporterIndex;

        ResourceHandler<ItemResource> target = importer.getConnectedResources().getItemHandler();

        if (target == null || target.size() == 0) return importer.lastExporterIndex;

        boolean[] moved = {false};
        int[] lastSourceSize = {1};

        int result = ImporterPullEngine.run(level, importer, importer.exporterPositions, importer.lastExporterIndex,
                (srvLevel, imp, exporterPos) -> {

                    ExporterBlockEntity exporter = getExporterAt(srvLevel, exporterPos);
                    if (exporter == null || !exporter.hasCorrectUpgrade(RoutersTags.Items.ITEM_UPGRADES)) return false;

                    ResourceHandler<ItemResource> source = getSourceHandler(srvLevel, exporter, imp, exporterPos);
                    if (source == null) return false;

                    int amount = exporter.getUpgradeValue(RoutersTags.Items.ITEM_UPGRADES);
                    lastSourceSize[0] = Math.max(source.size(), 1);
                    int start = imp.getItemScanState().nextScanStart(lastSourceSize[0]);

                    boolean success = moveFirstBounded(source, target, start, amount, resource -> {
                        boolean isWhitelist = !exporter.isBlacklist();

                        if (!ResourceHandlerUtil.isEmpty(exporter.getFilterItemHandler())) {
                            if (!checkFilter(exporter.getFilterItemHandler(), resource, isWhitelist, exporter.isIgnoreNbt())) {
                                return false;
                            }
                        }

                        boolean importerIsWhitelist = !imp.isBlacklist();
                        if (!ResourceHandlerUtil.isEmpty(imp.getFilterItemHandler())) {
                            if (!checkImporterFilter(imp.getFilterItemHandler(), resource, importerIsWhitelist, imp.isIgnoreNbt(), target)) {
                                return false;
                            }
                        }

                        return true;
                    });
                    moved[0] |= success;
                    return success;
                });

        importer.getItemScanState().recordResult(level.getGameTime(), lastSourceSize[0], moved[0]);
        return result;
    }

    private interface ItemPredicate {
        boolean test(ItemResource resource);
    }

    private static boolean moveFirstBounded(ResourceHandler<ItemResource> source, ResourceHandler<ItemResource> target,
                                             int start, int amount, ItemPredicate predicate) {
        if (amount <= 0) return false;

        int size = source.size();
        int scanLimit = Math.min(size, StartupConfig.maxInventoryScanPerOperation.get());

        for (int offset = 0; offset < scanLimit; offset++) {
            int slot = (start + offset) % size;
            ItemResource resource = source.getResource(slot);
            if (resource.isEmpty()) continue;
            if (!predicate.test(resource)) continue;

            try (Transaction tx = Transaction.open(null)) {
                int available = (int) Math.min(source.getAmountAsLong(slot), amount);
                int accepted = target.insert(resource, available, tx);

                if (accepted > 0 && source.extract(slot, resource, accepted, tx) > 0) {
                    tx.commit();
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    private static ExporterBlockEntity getExporterAt(ServerLevel importerLevel, GlobalPos exporterPos) {
        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;
        return exporterLevel.getBlockEntity(exporterPos.pos()) instanceof ExporterBlockEntity exporter ? exporter : null;
    }

    @Nullable
    private static ResourceHandler<ItemResource> getSourceHandler(ServerLevel importerLevel, ExporterBlockEntity exporter, ImporterBlockEntity importer, GlobalPos exporterPos) {
        if (!exporterPos.dimension().equals(importerLevel.dimension()) && !exporter.canDoDimensionalTravel()) return null;

        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;

        return importer.getItemSourceCache().get(exporterLevel, exporterPos);
    }

    private static boolean checkFilter(FilterItemHandler filterHandler, ItemResource resource, boolean isWhitelist, boolean ignoreNbt) {
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

        if (!hasAnyFilter) return true;
        return isWhitelist == foundMatch;
    }

    private static boolean checkImporterFilter(FilterItemHandler filterHandler, ItemResource resource, boolean isWhitelist, boolean ignoreNbt, @Nullable ResourceHandler<ItemResource> adjacentHandler) {
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
                    if (currentCount >= stock.amount()) return false;

                    return true;
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

        if (!hasAnyFilter) return true;
        return isWhitelist ? foundMatch : !foundMatch;
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

    private static ResourceHandler<ItemResource> getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos())) return null;

        return exporter.getItemTargetCache().get(targetLevel, pos);
    }
}
