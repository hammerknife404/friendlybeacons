package com.friendlybeacons;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Creative-only beacon for testing. Uses the vanilla beacon block entity; the tier override and free
 * power changes are handled by the beacon mixins (see {@link CreativeBeacon}). Unbreakable outside
 * creative and drops nothing.
 */
public class CreativeBeaconBlock extends BeaconBlock {
    public CreativeBeaconBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isCreative()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("friendlybeacons.creative.locked"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("friendlybeacons.creative.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
