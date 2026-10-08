package com.benbenlaw.routers.screen;

import com.benbenlaw.core.screen.SimpleAbstractContainerMenu;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.manager.ManagerSessions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DistributorMenu extends SimpleAbstractContainerMenu {

    public final DistributorBlockEntity blockEntity;
    protected final BlockPos blockPos;

    public DistributorMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory, extraData.readBlockPos());
    }

    public DistributorMenu(int containerID, Inventory inventory, BlockPos blockPos) {
        super(RoutersMenuTypes.DISTRIBUTOR_MENU.get(), containerID, inventory, blockPos, 0);
        this.blockPos = blockPos;
        this.blockEntity = (DistributorBlockEntity) inventory.player.level().getBlockEntity(blockPos);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return ManagerSessions.allowsRemote(player, blockPos) || super.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
