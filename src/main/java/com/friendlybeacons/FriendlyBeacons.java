package com.friendlybeacons;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(FriendlyBeacons.MOD_ID)
public class FriendlyBeacons {
    public static final String MOD_ID = "friendlybeacons";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    /**
     * Creative-only testing beacon: pick any tier without a pyramid, change powers without a payment
     * item. Uses the vanilla beacon block entity, so everything else behaves like a normal beacon.
     */
    public static final DeferredBlock<CreativeBeaconBlock> CREATIVE_BEACON = BLOCKS.register("creative_beacon",
            () -> new CreativeBeaconBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEACON)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .sound(SoundType.GLASS)));
    public static final DeferredItem<BlockItem> CREATIVE_BEACON_ITEM =
            ITEMS.registerSimpleBlockItem(CREATIVE_BEACON, new Item.Properties().rarity(Rarity.EPIC));

    /** Bitmask of {@link BeaconTargets} flags, stored on each beacon block entity. */
    public static final Supplier<AttachmentType<Integer>> BEACON_TARGETS = intAttachment("beacon_targets", BeaconTargets.DEFAULT);

    /** Selected radius tier, or {@link BeaconSettings#RADIUS_MAX}. */
    public static final Supplier<AttachmentType<Integer>> BEACON_RADIUS = intAttachment("beacon_radius", BeaconSettings.RADIUS_MAX);

    /** Tier 5 bonus, see {@link BeaconSettings}. */
    public static final Supplier<AttachmentType<Integer>> BEACON_BONUS = intAttachment("beacon_bonus", BeaconSettings.BONUS_BOTH_II);

    /** Bitmask of {@link BeaconTargets} flags that tier 5 Glowing applies to, independent of {@link #BEACON_TARGETS}. */
    public static final Supplier<AttachmentType<Integer>> BEACON_GLOW_TARGETS =
            intAttachment("beacon_glow_targets", BeaconSettings.GLOW_TARGETS_UNSET);

    /**
     * 1.3.0's single Glowing checkbox (1 = on, following the Affects checkboxes). Still registered
     * so existing beacons load it; {@link #glowTargets} converts it once.
     */
    /** Creative beacon tier selection, see {@link CreativeBeacon}. Synced to the client in the block update tag. */
    public static final Supplier<AttachmentType<Integer>> BEACON_CREATIVE_TIER =
            intAttachment("beacon_creative_tier", CreativeBeacon.DEFAULT_SELECTION);

    private static final Supplier<AttachmentType<Integer>> LEGACY_BEACON_GLOW = intAttachment("beacon_glow", 1);

    private static Supplier<AttachmentType<Integer>> intAttachment(String name, int defaultValue) {
        return ATTACHMENTS.register(name, () -> AttachmentType.builder(() -> defaultValue).serialize(Codec.INT).build());
    }

    /** The beacon's Glowing targets. Server side only. */
    public static int glowTargets(BeaconBlockEntity beacon) {
        int mask = beacon.getData(BEACON_GLOW_TARGETS);
        if (mask != BeaconSettings.GLOW_TARGETS_UNSET) return mask;
        // Beacons from 1.3.0 keep glowing exactly what they did: their Affects categories, or nothing.
        if (beacon.hasData(LEGACY_BEACON_GLOW.get())) {
            mask = beacon.getData(LEGACY_BEACON_GLOW) != 0 ? beacon.getData(BEACON_TARGETS) : 0;
            beacon.removeData(LEGACY_BEACON_GLOW.get());
        } else {
            mask = BeaconSettings.GLOW_TARGETS_DEFAULT;
        }
        beacon.setData(BEACON_GLOW_TARGETS, mask);
        beacon.setChanged();
        return mask;
    }

    public FriendlyBeacons(IEventBus modBus, ModContainer container) {
        ATTACHMENTS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(FriendlyBeacons::addBeaconBlocks);
        modBus.addListener(FriendlyBeacons::addCreativeTabItems);
        container.registerConfig(ModConfig.Type.SERVER, FriendlyBeaconsConfig.SPEC);
        modBus.addListener(FriendlyBeacons::registerPayloads);
        NeoForge.EVENT_BUS.addListener(FriendlyBeaconsCommand::register);
    }

    /** Lets the creative beacon use the vanilla beacon block entity (and so its ticker, menu and beam renderer). */
    private static void addBeaconBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.BEACON, CREATIVE_BEACON.get());
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.insertAfter(Items.BEACON.getDefaultInstance(), CREATIVE_BEACON_ITEM.toStack(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // Bumped in 1.4.0 (Glowing checkboxes) and 1.5.0 (creative beacon menu slot), so older clients are turned away.
        event.registrar("3").playToClient(HandshakePayload.TYPE, HandshakePayload.STREAM_CODEC, (payload, context) -> {});
    }
}
