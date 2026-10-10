package com.friendlybeacons;

/** Implemented on BeaconMenu by mixin to expose the synced target mask. */
public interface BeaconTargetsHolder {
    int friendlybeacons$getTargets();

    /** Client-side optimistic update; the server value overwrites it on the next sync. */
    void friendlybeacons$setTargets(int mask);
}
