package com.benbenlaw.routers.event.client;

import net.minecraft.core.BlockPos;

public class ClientRenderState {

    private static BlockPos current;

    public static void toggleCollector(BlockPos pos) {
        if (pos.equals(current)) {
            current = null;
        } else {
            current = pos;
        }
    }

    public static BlockPos get() {
        return current;
    }
}