package com.friendlybeacons.mixin;

import com.friendlybeacons.BeaconSettings;
import com.friendlybeacons.BeaconTargets;
import com.friendlybeacons.BeaconTargetsHolder;
import com.friendlybeacons.FriendlyBeacons;
import com.friendlybeacons.SyncedBeaconSlot;
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
 * Syncs the beacon's friendlybeacons settings to the client through extra data slots, and
 * accepts changes from the client through the vanilla container button click packet
 * (see {@link BeaconSettings} for button ids).
 */
@Mixin(BeaconMenu.class)
public abstract class BeaconMenuMixin extends AbstractContainerMenu implements BeaconTargetsHolder {
    @Shadow
    @Final
    private ContainerLevelAccess access;

    @Unique
    private DataSlot friendlybeacons$targets;
    @Unique
    private DataSlot friendlybeacons$radius;
    @Unique
    private DataSlot friendlybeacons$bonus;

    protected BeaconMenuMixin(MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL"))
    private void friendlybeacons$addSlots(int containerId, Container container, ContainerData data,
            ContainerLevelAccess access, CallbackInfo ci) {
        friendlybeacons$targets = addDataSlot(new SyncedBeaconSlot(access,
                beacon -> beacon.getData(FriendlyBeacons.BEACON_TARGETS), BeaconTargets.DEFAULT));
        friendlybeacons$radius = addDataSlot(new SyncedBeaconSlot(access,
                beacon -> BeaconSettings.radiusBlocks(((BeaconBlockEntityAccessor) beacon).friendlybeacons$getLevels(),
                        beacon.getData(FriendlyBeacons.BEACON_RADIUS)), 0));
        friendlybeacons$bonus = addDataSlot(new SyncedBeaconSlot(access,
                beacon -> beacon.getData(FriendlyBeacons.BEACON_BONUS), BeaconSettings.BONUS_BOTH_II));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        BeaconMenu self = (BeaconMenu) (Object) this;
        return access.evaluate((level, pos) -> {
            if (!(level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon)) return false;
            int levels = ((BeaconBlockEntityAccessor) beacon).friendlybeacons$getLevels();
            if (id >= 0 && id < BeaconTargets.values().length) {
                int bit = BeaconTargets.values()[id].bit();
                beacon.setData(FriendlyBeacons.BEACON_TARGETS, beacon.getData(FriendlyBeacons.BEACON_TARGETS) ^ bit);
            } else if (id == BeaconSettings.BUTTON_RADIUS && levels > 0) {
                beacon.setData(FriendlyBeacons.BEACON_RADIUS,
                        BeaconSettings.nextRadiusSelection(levels, beacon.getData(FriendlyBeacons.BEACON_RADIUS)));
            } else if (BeaconSettings.isValidBonus(id - BeaconSettings.BUTTON_SET_BONUS) && levels >= 5 && self.hasPayment()) {
                // Like vanilla powers this needs a payment item; the Done packet that follows consumes it.
                beacon.setData(FriendlyBeacons.BEACON_BONUS, id - BeaconSettings.BUTTON_SET_BONUS);
            } else {
                return false;
            }
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

    @Override
    public int friendlybeacons$getRadius() {
        return friendlybeacons$radius.get();
    }

    @Override
    public int friendlybeacons$getBonus() {
        return friendlybeacons$bonus.get();
    }
}
