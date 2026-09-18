package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.routers.api.ImporterPullEngine;
import com.benbenlaw.routers.api.TransferEngine;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class FluidTransfer {

    public static int transferFluids(ServerLevel level, ExporterBlockEntity exporter) {

        var state = level.getBlockState(exporter.getBlockPos());
        Direction facing = state.getValue(RouterBlock.FACING);

        ResourceHandler<FluidResource> source = exporter.getConnectedResources().getFluidHandler(
                level, exporter.getBlockPos().relative(facing), facing.getOpposite()
        ).orElse(null);

        if (source == null || source.size() == 0) return exporter.lastImporterIndex;

        return TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetPos) -> {

                    ResourceHandler<FluidResource> target = getTargetHandler(srvLevel, entity, targetPos);
                    if (target == null) return false;

                    for (int slot = 0; slot < source.size(); slot++) {
                        FluidResource res = source.getResource(slot);
                        if (res.isEmpty()) continue;

                        boolean isWhitelist = !entity.isBlacklist();
                        boolean matches = true;

                        if (!ResourceHandlerUtil.isEmpty(entity.getFilterFluidHandler())) {
                            matches = entity.getFilterFluidHandler().matchesFluid(FluidResource.of(res.toStack(1000)), isWhitelist, entity.isIgnoreNbt());
                        }

                        if (matches && srvLevel.getBlockEntity(targetPos.pos()) instanceof ImporterBlockEntity importer) {
                            if (!ResourceHandlerUtil.isEmpty(importer.getFilterFluidHandler())) {
                                matches = importer.getFilterFluidHandler().matchesFluid(
                                        FluidResource.of(res.toStack(1000)), !importer.isBlacklist(), importer.isIgnoreNbt());
                            }
                        }

                        if (!matches) continue;

                        try (Transaction tx = Transaction.open(null)) {
                            int amount = Math.min(source.getAmountAsInt(slot), entity.getUpgradeValue(RoutersTags.Items.FLUID_UPGRADES));
                            int accepted = target.insert(res, amount, tx);

                            if (accepted > 0 && source.extract(slot, res, accepted, tx) > 0) {
                                tx.commit();
                                return true;
                            }
                        }
                    }
                    return false;
                });
    }

    private static ResourceHandler<FluidResource> getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos())) return null;

        var state = targetLevel.getBlockState(pos.pos());
        Direction facing = state.getValue(RouterBlock.FACING).getOpposite();
        return targetLevel.getCapability(Capabilities.Fluid.BLOCK, pos.pos().relative(state.getValue(RouterBlock.FACING)), facing);
    }

    // Driven by the importer's own tick when it has a Round Robin upgrade - it actively pulls
    // from its linked exporters instead of waiting for them to push (see TransferEngine.pullsOwnResources).
    public static int pullFluids(ServerLevel level, ImporterBlockEntity importer) {

        var state = level.getBlockState(importer.getBlockPos());
        Direction facing = state.getValue(RouterBlock.FACING);

        ResourceHandler<FluidResource> target = level.getCapability(Capabilities.Fluid.BLOCK,
                importer.getBlockPos().relative(facing), facing.getOpposite());

        if (target == null) return importer.lastExporterIndex;

        return ImporterPullEngine.run(level, importer, importer.exporterPositions, importer.lastExporterIndex,
                (srvLevel, imp, exporterPos) -> {

                    ExporterBlockEntity exporter = getExporterAt(srvLevel, exporterPos);
                    if (exporter == null || !exporter.hasCorrectUpgrade(RoutersTags.Items.FLUID_UPGRADES)) return false;

                    ResourceHandler<FluidResource> source = getSourceHandler(srvLevel, exporter, exporterPos);
                    if (source == null) return false;

                    for (int slot = 0; slot < source.size(); slot++) {
                        FluidResource res = source.getResource(slot);
                        if (res.isEmpty()) continue;

                        boolean isWhitelist = !exporter.isBlacklist();
                        boolean matches = true;

                        if (!ResourceHandlerUtil.isEmpty(exporter.getFilterFluidHandler())) {
                            matches = exporter.getFilterFluidHandler().matchesFluid(FluidResource.of(res.toStack(1000)), isWhitelist, exporter.isIgnoreNbt());
                        }

                        if (matches && !ResourceHandlerUtil.isEmpty(imp.getFilterFluidHandler())) {
                            matches = imp.getFilterFluidHandler().matchesFluid(
                                    FluidResource.of(res.toStack(1000)), !imp.isBlacklist(), imp.isIgnoreNbt());
                        }

                        if (!matches) continue;

                        try (Transaction tx = Transaction.open(null)) {
                            int amount = Math.min(source.getAmountAsInt(slot), exporter.getUpgradeValue(RoutersTags.Items.FLUID_UPGRADES));
                            int accepted = target.insert(res, amount, tx);

                            if (accepted > 0 && source.extract(slot, res, accepted, tx) > 0) {
                                tx.commit();
                                return true;
                            }
                        }
                    }
                    return false;
                });
    }

    @Nullable
    private static ExporterBlockEntity getExporterAt(ServerLevel importerLevel, GlobalPos exporterPos) {
        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;
        return exporterLevel.getBlockEntity(exporterPos.pos()) instanceof ExporterBlockEntity exporter ? exporter : null;
    }

    @Nullable
    private static ResourceHandler<FluidResource> getSourceHandler(ServerLevel importerLevel, ExporterBlockEntity exporter, GlobalPos exporterPos) {
        if (!exporterPos.dimension().equals(importerLevel.dimension()) && !exporter.canDoDimensionalTravel()) return null;

        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null) return null;

        var state = exporterLevel.getBlockState(exporterPos.pos());
        Direction facing = state.getValue(RouterBlock.FACING);
        return exporterLevel.getCapability(Capabilities.Fluid.BLOCK, exporterPos.pos().relative(facing), facing.getOpposite());
    }
}