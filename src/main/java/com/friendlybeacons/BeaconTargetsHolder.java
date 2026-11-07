package com.friendlybeacons;

/** Implemented on BeaconMenu by mixin to expose the synced friendlybeacons settings. */
public interface BeaconTargetsHolder {
    int friendlybeacons$getTargets();

    /** Client-side optimistic update; the server value overwrites it on the next sync. */
    void friendlybeacons$setTargets(int mask);

    /** Effective radius in blocks, 0 if the beacon has no pyramid. */
    int friendlybeacons$getRadius();

    int friendlybeacons$getBonus();

    /** Categories the beacon gives tier 5 Glowing to, as a {@link BeaconTargets} mask. */
    int friendlybeacons$getGlowTargets();

    /** Client-side optimistic update; the server value overwrites it on the next sync. */
    void friendlybeacons$setGlowTargets(int mask);

    /** Effects disabled server-wide, as a mask of {@link BeaconSettings#effectBit} bits. */
    int friendlybeacons$getDisabledEffects();
}
