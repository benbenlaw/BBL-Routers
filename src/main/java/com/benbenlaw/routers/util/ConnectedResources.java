package com.benbenlaw.routers.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;

public class ConnectedResources {

    private final BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> itemCache;
    private final BlockCapabilityCache<ResourceHandler<FluidResource>, Direction> fluidCache;
    private final BlockCapabilityCache<EnergyHandler, Direction> energyCache;

    public ConnectedResources(ServerLevel level, BlockPos pos, Direction side) {
        this.itemCache = BlockCapabilityCache.create(Capabilities.Item.BLOCK, level, pos, side);
        this.fluidCache = BlockCapabilityCache.create(Capabilities.Fluid.BLOCK, level, pos, side);
        this.energyCache = BlockCapabilityCache.create(Capabilities.Energy.BLOCK, level, pos, side);
    }

    @Nullable
    public ResourceHandler<ItemResource> getItemHandler() {
        return itemCache.getCapability();
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler() {
        return fluidCache.getCapability();
    }

    @Nullable
    public EnergyHandler getEnergyHandler() {
        return energyCache.getCapability();
    }
}
