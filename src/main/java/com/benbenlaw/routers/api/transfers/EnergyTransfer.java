package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.routers.api.ImporterPullEngine;
import com.benbenlaw.routers.api.TransferEngine;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class EnergyTransfer {

    public static int transferEnergy(ServerLevel level, ExporterBlockEntity exporter) {

        if (exporter.getEnergyScanState().shouldSkip(level.getGameTime())) return exporter.lastImporterIndex;

        int amount = exporter.getUpgradeValue(RoutersTags.Items.RF_UPGRADES);
        EnergyHandler source = exporter.getConnectedResources().getEnergyHandler();

        if (source == null || source.getAmountAsLong() <= 0) return exporter.lastImporterIndex;

        boolean[] moved = {false};
        exporter.getEnergyScanState().nextScanStart(1);

        int result = TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetGlobalPos) -> {

                    EnergyHandler target = getTargetHandler(srvLevel, entity, targetGlobalPos);
                    if (target == null) return false;

                    try (Transaction tx = Transaction.open(null)) {
                        int extracted = source.extract(amount, tx);
                        if (extracted <= 0) return false;

                        int accepted = target.insert(extracted, tx);
                        if (accepted > 0) {
                            tx.commit();
                            moved[0] = true;
                            return true;
                        }
                    }
                    return false;
                });

        exporter.getEnergyScanState().recordResult(level.getGameTime(), 1, moved[0]);
        return result;
    }

    private static EnergyHandler getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos()) || !(targetLevel.getBlockEntity(pos.pos()) instanceof ImporterBlockEntity)) return null;

        return exporter.getEnergyTargetCache().get(targetLevel, pos);
    }

    public static int pullEnergy(ServerLevel level, ImporterBlockEntity importer) {

        if (importer.getEnergyScanState().shouldSkip(level.getGameTime())) return importer.lastExporterIndex;

        EnergyHandler target = importer.getConnectedResources().getEnergyHandler();

        if (target == null) return importer.lastExporterIndex;

        boolean[] moved = {false};
        importer.getEnergyScanState().nextScanStart(1);

        int result = ImporterPullEngine.run(level, importer, importer.exporterPositions, importer.lastExporterIndex,
                (srvLevel, imp, exporterPos) -> {

                    ExporterBlockEntity exporter = getExporterAt(srvLevel, exporterPos);
                    if (exporter == null || !exporter.hasCorrectUpgrade(RoutersTags.Items.RF_UPGRADES)) return false;

                    EnergyHandler source = getSourceHandler(srvLevel, exporter, imp, exporterPos);
                    if (source == null || source.getAmountAsLong() <= 0) return false;

                    int amount = exporter.getUpgradeValue(RoutersTags.Items.RF_UPGRADES);

                    try (Transaction tx = Transaction.open(null)) {
                        int extracted = source.extract(amount, tx);
                        if (extracted <= 0) return false;

                        int accepted = target.insert(extracted, tx);
                        if (accepted > 0) {
                            tx.commit();
                            moved[0] = true;
                            return true;
                        }
                    }
                    return false;
                });

        importer.getEnergyScanState().recordResult(level.getGameTime(), 1, moved[0]);
        return result;
    }

    @Nullable
    private static ExporterBlockEntity getExporterAt(ServerLevel importerLevel, GlobalPos exporterPos) {
        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;
        return exporterLevel.getBlockEntity(exporterPos.pos()) instanceof ExporterBlockEntity exporter ? exporter : null;
    }

    @Nullable
    private static EnergyHandler getSourceHandler(ServerLevel importerLevel, ExporterBlockEntity exporter, ImporterBlockEntity importer, GlobalPos exporterPos) {
        if (!exporterPos.dimension().equals(importerLevel.dimension()) && !exporter.canDoDimensionalTravel()) return null;

        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;

        return importer.getEnergySourceCache().get(exporterLevel, exporterPos);
    }
}
