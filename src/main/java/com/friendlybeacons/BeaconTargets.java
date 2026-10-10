package com.friendlybeacons;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/** The four beacon target categories, stored as bits in an int. */
public enum BeaconTargets {
    PASSIVE("passive"),
    FRIENDLY("friendly"),
    HOSTILE("hostile"),
    PLAYER("player");

    /** Vanilla behavior: players only. */
    public static final int DEFAULT = PLAYER.bit();

    private final String key;

    BeaconTargets(String key) {
        this.key = key;
    }

    public int bit() {
        return 1 << ordinal();
    }

    public boolean isIn(int mask) {
        return (mask & bit()) != 0;
    }

    public Component label() {
        return Component.translatable("friendlybeacons.targets." + key);
    }

    public Component description() {
        return Component.translatable("friendlybeacons.targets." + key + ".desc");
    }

    /** Returns the category of an entity, or null if beacons should never affect it (e.g. armor stands). */
    public static BeaconTargets of(LivingEntity entity) {
        if (entity instanceof Player) return PLAYER;
        if (!(entity instanceof Mob mob)) return null;
        if (mob instanceof Enemy) return HOSTILE;
        if (isFriendly(mob)) return FRIENDLY;
        return PASSIVE;
    }

    private static boolean isFriendly(Mob mob) {
        if (mob instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) return true;
        return mob instanceof AbstractGolem || mob instanceof Allay;
    }

    public static boolean matches(LivingEntity entity, int mask) {
        BeaconTargets category = of(entity);
        return category != null && category.isIn(mask);
    }
}
