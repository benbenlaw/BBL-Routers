package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.routers.api.ImporterPullEngine;
import com.benbenlaw.routers.api.RouteFilters;
import com.benbenlaw.routers.api.TransferEngine;
import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterCore;
import com.benbenlaw.routers.util.ResourceScanState;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleCapabilityTransfer<H> implements TransferModule<H> {

    protected abstract boolean isEmpty(H source, Direction side);

    protected abstract boolean move(H source, Direction sourceSide, H target, Direction targetSide, int amount, RouteFilters filters);

    private record Resolved<H>(H handler, Direction side, boolean distributed) {}

    private static Direction faceOf(BlockState routerState) {
        return routerState.hasProperty(RouterBlock.FACING) ? routerState.getValue(RouterBlock.FACING).getOpposite() : Direction.UP;
    }

    @Override
    public int push(ServerLevel level, ExporterBlockEntity exporter) {
        ResourceScanState scan = exporter.getScanState(capability().name());
        if (scan.shouldSkip(level.getGameTime())) return exporter.lastImporterIndex;

        int amount = exporter.getUpgradeValue(upgradeTag());
        Direction sourceSide = faceOf(exporter.getBlockState());
        H source = exporter.getConnectedResources().get(capability());
        if (source == null || isEmpty(source, sourceSide)) return exporter.lastImporterIndex;

        boolean[] moved = {false};
        scan.nextScanStart(1);

        int result = TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetPos) -> {
                    Resolved<H> target = getTarget(srvLevel, entity, targetPos);
                    if (target == null) return false;

                    // a distributor applies its own filters as it shares things out
                    ImporterCore importer = target.distributed() ? null : ImporterCore.at(srvLevel, targetPos.pos());
                    RouteFilters filters = new RouteFilters(entity, entity.isBlacklist(), importer, importer != null && importer.isBlacklist());

                    if (move(source, sourceSide, target.handler(), target.side(), amount, filters)) {
                        moved[0] = true;
                        return true;
                    }
                    return false;
                });

        scan.recordResult(level.getGameTime(), 1, moved[0]);
        return result;
    }

    @Override
    public int pull(ServerLevel level, ImporterCore importer) {
        ResourceScanState scan = importer.getScanState(capability().name());
        if (scan.shouldSkip(level.getGameTime())) return importer.lastExporterIndex;

        Direction targetSide = faceOf(importer.getHost().getBlockState());
        H target = importer.getConnectedResources().get(capability());
        if (target == null) return importer.lastExporterIndex;

        boolean[] moved = {false};
        scan.nextScanStart(1);

        int result = ImporterPullEngine.run(level, importer, importer.exporterPositions, importer.lastExporterIndex,
                (srvLevel, imp, exporterPos) -> {
                    ExporterBlockEntity exporter = getExporterAt(srvLevel, exporterPos);
                    if (exporter == null || !exporter.hasCorrectUpgrade(upgradeTag())) return false;

                    Resolved<H> source = getSource(srvLevel, exporter, imp, exporterPos);
                    if (source == null || isEmpty(source.handler(), source.side())) return false;

                    RouteFilters filters = new RouteFilters(exporter, exporter.isBlacklist(), imp, imp.isBlacklist());

                    if (move(source.handler(), source.side(), target, targetSide, exporter.getUpgradeValue(upgradeTag()), filters)) {
                        moved[0] = true;
                        return true;
                    }
                    return false;
                });

        scan.recordResult(level.getGameTime(), 1, moved[0]);
        return result;
    }

    @Nullable
    private Resolved<H> getTarget(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos())) return null;

        if (targetLevel.getBlockEntity(pos.pos()) instanceof DistributorBlockEntity distributor) {
            H handler = distributor.getDistributor(this);
            return handler == null ? null : new Resolved<>(handler, Direction.UP, true);
        }

        if (ImporterCore.at(targetLevel, pos.pos()) == null) return null;

        H handler = exporter.getTargetCache(capability()).get(targetLevel, pos);
        return handler == null ? null : new Resolved<>(handler, faceOf(targetLevel.getBlockState(pos.pos())), false);
    }

    @Nullable
    private static ExporterBlockEntity getExporterAt(ServerLevel importerLevel, GlobalPos exporterPos) {
        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;
        return exporterLevel.getBlockEntity(exporterPos.pos()) instanceof ExporterBlockEntity exporter ? exporter : null;
    }

    @Nullable
    private Resolved<H> getSource(ServerLevel importerLevel, ExporterBlockEntity exporter, ImporterCore importer, GlobalPos exporterPos) {
        if (!exporterPos.dimension().equals(importerLevel.dimension()) && !exporter.canDoDimensionalTravel()) return null;

        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;

        H handler = importer.getSourceCache(capability()).get(exporterLevel, exporterPos);
        return handler == null ? null : new Resolved<>(handler, faceOf(exporterLevel.getBlockState(exporterPos.pos())), false);
    }
}
