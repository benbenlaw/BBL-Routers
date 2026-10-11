package com.benbenlaw.routers.util;

import com.benbenlaw.routers.config.StartupConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResourceScanState {

    private static final Object DEFAULT = new Object();

    // {where the next window starts, slots checked this lap, size of the inventory}. Each inventory gets its own, because a
    // router with several links would otherwise move the one cursor along for all of them and never see the stacks it skipped.
    private final Map<Object, int[]> lanes = new HashMap<>();
    private final Map<Object, Integer> stackCursors = new HashMap<>();
    private final Set<Object> touched = new HashSet<>();

    private long resumeAtTick = Long.MIN_VALUE;
    private int missStreak;

    public boolean shouldSkip(long currentTick) {
        return currentTick < resumeAtTick;
    }

    public int nextScanStart(int size) {
        return nextScanStart(DEFAULT, size);
    }

    public int nextScanStart(Object inventory, int size) {
        int[] lane = lanes.computeIfAbsent(inventory, key -> new int[3]);
        int start = Math.floorMod(lane[0], size);
        int windowSize = Math.min(size, StartupConfig.maxInventoryScanPerOperation.get());

        lane[0] = start + windowSize;
        lane[1] = Math.min(lane[1] + windowSize, size);
        lane[2] = size;
        touched.add(inventory);
        return start;
    }

    public int stackStart(int size) {
        return stackStart(DEFAULT, size);
    }

    public int stackStart(Object inventory, int size) {
        return Math.floorMod(stackCursors.getOrDefault(inventory, 0), size);
    }

    public void stackMoved(int slot) {
        stackMoved(DEFAULT, slot);
    }

    public void stackMoved(Object inventory, int slot) {
        stackCursors.put(inventory, slot + 1);
    }

    public void recordResult(long currentTick, int size, boolean movedSomething) {
        if (movedSomething) {
            missStreak = 0;
            resumeAtTick = Long.MIN_VALUE;
            for (int[] lane : lanes.values()) lane[1] = 0;
            touched.clear();
            return;
        }

        boolean lapDone = !touched.isEmpty();
        for (Object inventory : touched) {
            int[] lane = lanes.get(inventory);
            if (lane[1] < lane[2]) lapDone = false;
        }

        if (!lapDone) {
            touched.clear();
            return;
        }

        for (Object inventory : touched) lanes.get(inventory)[1] = 0;
        touched.clear();

        int minTicks = StartupConfig.minBackoffTicks.get();
        if (minTicks <= 0) {
            return;
        }

        missStreak++;
        long waitTicks = Math.min((long) missStreak * minTicks, StartupConfig.maxBackoffTicks.get());
        resumeAtTick = currentTick + Math.max(waitTicks, minTicks);
    }
}
