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
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class EnergyTransfer {

    public static int transferEnergy(ServerLevel level, ExporterBlockEntity exporter) {

        int amount = exporter.getUpgradeValue(RoutersTags.Items.RF_UPGRADES);
        EnergyHandler source = exporter.getConnectedResources().getEnergyHandler(
                level, exporter.getBlockPos().relative(level.getBlockState(exporter.getBlockPos()).getValue(RouterBlock.FACING)),
                level.getBlockState(exporter.getBlockPos()).getValue(RouterBlock.FACING).getOpposite()
        ).orElse(null);

        if (source == null || source.getAmountAsLong() <= 0) return exporter.lastImporterIndex;

        return TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetGlobalPos) -> {

                    EnergyHandler target = getTargetHandler(srvLevel, entity, targetGlobalPos);
                    if (target == null) return false;

                    try (Transaction tx = Transaction.open(null)) {
                        int extracted = source.extract(amount, tx);
                        if (extracted <= 0) return false;

                        int accepted = target.insert(extracted, tx);
                        if (accepted > 0) {
                            tx.commit();
                            return true;
                        }
                    }
                    return false;
                });
    }

    private static EnergyHandler getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos()) || !(targetLevel.getBlockEntity(pos.pos()) instanceof ImporterBlockEntity)) return null;

        var state = targetLevel.getBlockState(pos.pos());
        Direction facing = state.getValue(RouterBlock.FACING).getOpposite();
        return targetLevel.getCapability(Capabilities.Energy.BLOCK, pos.pos().relative(state.getValue(RouterBlock.FACING)), facing);
    }

    // Driven by the importer's own tick when it has a Round Robin upgrade - it actively pulls
    // from its linked exporters instead of waiting for them to push (see TransferEngine.pullsOwnResources).
    public static int pullEnergy(ServerLevel level, ImporterBlockEntity importer) {

        var state = level.getBlockState(importer.getBlockPos());
        Direction facing = state.getValue(RouterBlock.FACING);

        EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK,
                importer.getBlockPos().relative(facing), facing.getOpposite());

        if (target == null) return importer.lastExporterIndex;

        return ImporterPullEngine.run(level, importer, importer.exporterPositions, importer.lastExporterIndex,
                (srvLevel, imp, exporterPos) -> {

                    ExporterBlockEntity exporter = getExporterAt(srvLevel, exporterPos);
                    if (exporter == null || !exporter.hasCorrectUpgrade(RoutersTags.Items.RF_UPGRADES)) return false;

                    EnergyHandler source = getSourceHandler(srvLevel, exporter, exporterPos);
                    if (source == null || source.getAmountAsLong() <= 0) return false;

                    int amount = exporter.getUpgradeValue(RoutersTags.Items.RF_UPGRADES);

                    try (Transaction tx = Transaction.open(null)) {
                        int extracted = source.extract(amount, tx);
                        if (extracted <= 0) return false;

                        int accepted = target.insert(extracted, tx);
                        if (accepted > 0) {
                            tx.commit();
                            return true;
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
    private static EnergyHandler getSourceHandler(ServerLevel importerLevel, ExporterBlockEntity exporter, GlobalPos exporterPos) {
        if (!exporterPos.dimension().equals(importerLevel.dimension()) && !exporter.canDoDimensionalTravel()) return null;

        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null) return null;

        var state = exporterLevel.getBlockState(exporterPos.pos());
        Direction facing = state.getValue(RouterBlock.FACING);
        return exporterLevel.getCapability(Capabilities.Energy.BLOCK, exporterPos.pos().relative(facing), facing.getOpposite());
    }
}