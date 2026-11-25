package com.friendlybeacons.mixin;

import com.friendlybeacons.BeaconSettings;
import com.friendlybeacons.BeaconTargets;
import com.friendlybeacons.BeaconTargetsHolder;
import com.friendlybeacons.CreativeBeacon;
import com.friendlybeacons.FriendlyBeacons;
import com.friendlybeacons.FriendlyBeaconsConfig;
import com.friendlybeacons.SyncedBeaconSlot;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
    @Shadow
    @Final
    private ContainerData beaconData;

    @Unique
    private DataSlot friendlybeacons$targets;
    @Unique
    private DataSlot friendlybeacons$radius;
    @Unique
    private DataSlot friendlybeacons$bonus;
    @Unique
    private DataSlot friendlybeacons$glow; // Glowing targets mask
    @Unique
    private DataSlot friendlybeacons$disabledEffects;
    @Unique
    private DataSlot friendlybeacons$creativeTier;

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
        friendlybeacons$glow = addDataSlot(new SyncedBeaconSlot(access,
                FriendlyBeacons::glowTargets, BeaconSettings.GLOW_TARGETS_DEFAULT));
        // Server-wide, but synced per menu so /friendlybeacons effect changes reach open screens.
        friendlybeacons$disabledEffects = addDataSlot(new SyncedBeaconSlot(access,
                beacon -> FriendlyBeaconsConfig.disabledEffects(), 0));
        friendlybeacons$creativeTier = addDataSlot(new SyncedBeaconSlot(access,
                beacon -> CreativeBeacon.is(beacon) ? CreativeBeacon.selection(beacon.getData(FriendlyBeacons.BEACON_CREATIVE_TIER))
                        : CreativeBeacon.NOT_CREATIVE, CreativeBeacon.NOT_CREATIVE));
    }

    @Unique
    private boolean friendlybeacons$isCreativeServer() {
        return access.evaluate(CreativeBeacon::is, false);
    }

    /** Vanilla only accepts the vanilla beacon block here; keep creative beacon menus open too. */
    @ModifyArg(method = "stillValid", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/BeaconMenu;stillValid(Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/Block;)Z"),
            index = 2)
    private Block friendlybeacons$creativeStillValid(Block block) {
        return friendlybeacons$isCreativeServer() ? FriendlyBeacons.CREATIVE_BEACON.get() : block;
    }

    /** Creative beacons change powers for free. With a payment item in the slot, vanilla consumes it as usual. */
    @Inject(method = "updateEffects", at = @At("HEAD"), cancellable = true)
    private void friendlybeacons$freeEffects(Optional<Holder<MobEffect>> primary, Optional<Holder<MobEffect>> secondary, CallbackInfo ci) {
        if (((BeaconMenu) (Object) this).hasPayment() || !friendlybeacons$isCreativeServer()) return;
        beaconData.set(1, BeaconMenu.encodeEffect(primary.orElse(null)));
        beaconData.set(2, BeaconMenu.encodeEffect(secondary.orElse(null)));
        access.execute(Level::blockEntityChanged);
        ci.cancel();
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
            } else if (id >= BeaconSettings.BUTTON_GLOW_TARGETS && id < BeaconSettings.BUTTON_GLOW_TARGETS + BeaconTargets.values().length) {
                // Free and instant like the target checkboxes, so it can be flipped on for a raid.
                int bit = BeaconTargets.values()[id - BeaconSettings.BUTTON_GLOW_TARGETS].bit();
                beacon.setData(FriendlyBeacons.BEACON_GLOW_TARGETS, FriendlyBeacons.glowTargets(beacon) ^ bit);
            } else if (id == CreativeBeacon.BUTTON_TIER && CreativeBeacon.is(beacon) && player.isCreative()) {
                beacon.setData(FriendlyBeacons.BEACON_CREATIVE_TIER, CreativeBeacon.next(beacon.getData(FriendlyBeacons.BEACON_CREATIVE_TIER)));
                // Apply now rather than at the next 80-tick check; Auto falls back to the pyramid at that check.
                int selection = CreativeBeacon.selection(beacon.getData(FriendlyBeacons.BEACON_CREATIVE_TIER));
                if (selection != CreativeBeacon.AUTO) ((BeaconBlockEntityAccessor) beacon).friendlybeacons$setLevels(selection);
                // The client beacon needs the selection for the beam.
                level.sendBlockUpdated(pos, beacon.getBlockState(), beacon.getBlockState(), Block.UPDATE_CLIENTS);
            } else if (BeaconSettings.isValidBonus(id - BeaconSettings.BUTTON_SET_BONUS) && levels >= 5
                    && (self.hasPayment() || CreativeBeacon.is(beacon))) {
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

    @Override
    public int friendlybeacons$getGlowTargets() {
        return friendlybeacons$glow.get();
    }

    @Override
    public void friendlybeacons$setGlowTargets(int mask) {
        friendlybeacons$glow.set(mask);
    }

    @Override
    public int friendlybeacons$getDisabledEffects() {
        return friendlybeacons$disabledEffects.get();
    }

    @Override
    public int friendlybeacons$getCreativeTier() {
        return friendlybeacons$creativeTier.get();
    }
}
