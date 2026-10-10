package com.friendlybeacons.mixin;

import com.friendlybeacons.BeaconTargets;
import com.friendlybeacons.BeaconTargetsHolder;
import com.friendlybeacons.FriendlyBeacons;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Syncs the beacon's target mask to the client through an extra data slot, and accepts
 * toggles from the client through the vanilla container button click packet
 * (button id = {@link BeaconTargets} ordinal).
 */
@Mixin(BeaconMenu.class)
public abstract class BeaconMenuMixin extends AbstractContainerMenu implements BeaconTargetsHolder {
    @Shadow
    @Final
    private ContainerLevelAccess access;

    @Unique
    private DataSlot friendlybeacons$targets;

    protected BeaconMenuMixin(MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL"))
    private void friendlybeacons$addTargetsSlot(int containerId, Container container, ContainerData data,
            ContainerLevelAccess access, CallbackInfo ci) {
        // Server: reads the block entity live. Client: access is NULL, so it holds the synced value.
        friendlybeacons$targets = addDataSlot(new DataSlot() {
            private int clientValue = BeaconTargets.DEFAULT;

            @Override
            public int get() {
                return access.evaluate((level, pos) -> level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon
                        ? beacon.getData(FriendlyBeacons.BEACON_TARGETS)
                        : clientValue, clientValue);
            }

            @Override
            public void set(int value) {
                clientValue = value;
            }
        });
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= BeaconTargets.values().length) return false;
        int bit = BeaconTargets.values()[id].bit();
        return access.evaluate((level, pos) -> {
            if (!(level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon)) return false;
            beacon.setData(FriendlyBeacons.BEACON_TARGETS, beacon.getData(FriendlyBeacons.BEACON_TARGETS) ^ bit);
            beacon.setChanged();
            return true;
        }, false);
    }

    @Override
    public int friendlybeacons$getTargets() {
        return friendlybeacons$targets.get();
    }

    @Override
    public void friendlybeacons$setTargets(int mask) {
        friendlybeacons$targets.set(mask);
    }
}
