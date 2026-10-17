package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.block.RoutersBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.jetbrains.annotations.NotNull;

/** Export tab of the Router (server side). Uses the Exporter menu type; only the block check differs. */
public class RouterExportMenu extends ExporterMenu {

    public RouterExportMenu(int containerID, Inventory inventory, BlockPos blockPos, ContainerData data) {
        super(containerID, inventory, blockPos, data);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), blockPos),
                player, RoutersBlocks.ROUTER_BLOCK.get());
    }
}
