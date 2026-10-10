package com.friendlybeacons;

import java.util.function.ToIntFunction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

/**
 * Menu data slot for a beacon value. On the server it reads the block entity live; on the
 * client (where access is NULL) it holds the last synced value.
 */
public class SyncedBeaconSlot extends DataSlot {
    private final ContainerLevelAccess access;
    private final ToIntFunction<BeaconBlockEntity> getter;
    private int clientValue;

    public SyncedBeaconSlot(ContainerLevelAccess access, ToIntFunction<BeaconBlockEntity> getter, int clientDefault) {
        this.access = access;
        this.getter = getter;
        this.clientValue = clientDefault;
    }

    @Override
    public int get() {
        return access.evaluate((level, pos) -> level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon
                ? getter.applyAsInt(beacon)
                : clientValue, clientValue);
    }

    @Override
    public void set(int value) {
        clientValue = value;
    }
}
