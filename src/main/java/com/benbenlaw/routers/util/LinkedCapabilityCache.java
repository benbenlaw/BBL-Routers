package com.benbenlaw.routers.util;

import com.benbenlaw.routers.block.custom.RouterBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LinkedCapabilityCache<T> {

    private final BlockCapability<T, Direction> capability;
    private final Map<GlobalPos, BlockCapabilityCache<T, Direction>> caches = new ConcurrentHashMap<>();

    public LinkedCapabilityCache(BlockCapability<T, Direction> capability) {
        this.capability = capability;
    }

    @Nullable
    public T get(ServerLevel level, GlobalPos remotePos) {
        BlockCapabilityCache<T, Direction> cache = caches.get(remotePos);
        if (cache == null) {
            BlockState state = level.getBlockState(remotePos.pos());
            if (!state.hasProperty(RouterBlock.FACING)) return null;

            Direction facing = state.getValue(RouterBlock.FACING);
            cache = BlockCapabilityCache.create(capability, level, remotePos.pos().relative(facing), facing.getOpposite());
            caches.put(remotePos, cache);
        }
        return cache.getCapability();
    }

    public void remove(GlobalPos remotePos) {
        caches.remove(remotePos);
    }
}
