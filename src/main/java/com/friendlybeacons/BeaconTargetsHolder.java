package com.friendlybeacons;

/** Implemented on BeaconMenu by mixin to expose the synced friendlybeacons settings. */
public interface BeaconTargetsHolder {
    int friendlybeacons$getTargets();

    /** Client-side optimistic update; the server value overwrites it on the next sync. */
    void friendlybeacons$setTargets(int mask);

    /** Effective radius in blocks, 0 if the beacon has no pyramid. */
    int friendlybeacons$getRadius();

    int friendlybeacons$getBonus();
}
