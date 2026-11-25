package com.friendlybeacons;

import java.util.function.IntSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

/**
 * Tier rules for the creative beacon. Its stored selection is {@link #AUTO} (use the real pyramid)
 * or a fixed tier 1-5; a fixed tier above the highest allowed one (4 with tier 5 off) counts as the highest.
 */
public final class CreativeBeacon {
    private CreativeBeacon() {}

    /** Menu button id: cycle the creative tier. */
    public static final int BUTTON_TIER = 5;

    /** Use the pyramid under the beacon, like a normal beacon. */
    public static final int AUTO = 0;
    /** New creative beacons start at the top tier. */
    public static final int DEFAULT_SELECTION = 5;
    /** Menu data slot value for a normal beacon. */
    public static final int NOT_CREATIVE = -1;

    public static boolean is(BeaconBlockEntity beacon) {
        return beacon.getBlockState().is(FriendlyBeacons.CREATIVE_BEACON.get());
    }

    public static boolean is(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(FriendlyBeacons.CREATIVE_BEACON.get());
    }

    public static int maxTier() {
        return FriendlyBeaconsConfig.tier5Enabled() ? 5 : 4;
    }

    /** The selection with fixed tiers clamped to the highest allowed tier. */
    public static int selection(int stored) {
        return stored <= AUTO ? AUTO : Math.min(stored, maxTier());
    }

    /** Selection after one click: Auto, 1, 2, ... up to the highest tier, then back to Auto. */
    public static int next(int stored) {
        int current = selection(stored);
        return current >= maxTier() ? AUTO : current + 1;
    }

    /** Pyramid levels of a beacon: the creative selection if fixed, otherwise the real pyramid. */
    public static int levels(BeaconBlockEntity beacon, IntSupplier pyramid) {
        if (!is(beacon)) return pyramid.getAsInt();
        int selection = selection(beacon.getData(FriendlyBeacons.BEACON_CREATIVE_TIER));
        return selection == AUTO ? pyramid.getAsInt() : selection;
    }
}
