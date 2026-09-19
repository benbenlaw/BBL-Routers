package com.benbenlaw.routers.util;

import com.benbenlaw.routers.config.StartupConfig;

public class ResourceScanState {

    private int cursor;
    private int slotsCheckedThisLap;

    private long resumeAtTick = Long.MIN_VALUE;
    private int missStreak;

    public boolean shouldSkip(long currentTick) {
        return currentTick < resumeAtTick;
    }

    public int nextScanStart(int size) {
        int start = Math.floorMod(cursor, size);
        int windowSize = Math.min(size, StartupConfig.maxInventoryScanPerOperation.get());
        cursor = start + windowSize;
        slotsCheckedThisLap = Math.min(slotsCheckedThisLap + windowSize, size);
        return start;
    }

    public void recordResult(long currentTick, int size, boolean movedSomething) {
        if (movedSomething) {
            missStreak = 0;
            resumeAtTick = Long.MIN_VALUE;
            slotsCheckedThisLap = 0;
            return;
        }

        if (slotsCheckedThisLap < size) {
            return;
        }

        slotsCheckedThisLap = 0;

        int minTicks = StartupConfig.minBackoffTicks.get();
        if (minTicks <= 0) {
            return;
        }

        missStreak++;
        long waitTicks = Math.min((long) missStreak * minTicks, StartupConfig.maxBackoffTicks.get());
        resumeAtTick = currentTick + Math.max(waitTicks, minTicks);
    }
}
