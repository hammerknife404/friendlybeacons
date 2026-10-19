package com.friendlybeacons;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * /friendlybeacons effects lists the server-wide effect toggles; /friendlybeacons effect &lt;name&gt; shows one and
 * /friendlybeacons effect &lt;name&gt; &lt;true|false&gt; changes it and saves it to the server config. Needs op (permission level 2).
 */
final class FriendlyBeaconsCommand {
    private static final DynamicCommandExceptionType UNKNOWN_EFFECT =
            new DynamicCommandExceptionType(name -> Component.translatable("friendlybeacons.command.unknown_effect", name));

    private FriendlyBeaconsCommand() {}

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(FriendlyBeacons.MOD_ID)
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("effects").executes(FriendlyBeaconsCommand::list))
                .then(Commands.literal("effect")
                        .then(Commands.argument("effect", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        BeaconSettings.TOGGLEABLE_EFFECTS.stream().map(BeaconSettings::effectName), builder))
                                .executes(FriendlyBeaconsCommand::show)
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(FriendlyBeaconsCommand::set)))));
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        MutableComponent message = Component.translatable("friendlybeacons.command.list");
        for (Holder<MobEffect> effect : BeaconSettings.TOGGLEABLE_EFFECTS) {
            message.append("\n ").append(status(effect));
        }
        context.getSource().sendSuccess(() -> message, false);
        return BeaconSettings.TOGGLEABLE_EFFECTS.size();
    }

    private static int show(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder<MobEffect> effect = effect(context);
        context.getSource().sendSuccess(() -> status(effect), false);
        return FriendlyBeaconsConfig.effectEnabled(effect) ? 1 : 0;
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Holder<MobEffect> effect = effect(context);
        FriendlyBeaconsConfig.setEffectEnabled(effect, BoolArgumentType.getBool(context, "enabled"));
        context.getSource().sendSuccess(() -> status(effect), true);
        return 1;
    }

    private static Holder<MobEffect> effect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "effect");
        return BeaconSettings.TOGGLEABLE_EFFECTS.stream()
                .filter(effect -> BeaconSettings.effectName(effect).equals(name))
                .findFirst()
                .orElseThrow(() -> UNKNOWN_EFFECT.create(name));
    }

    private static Component status(Holder<MobEffect> effect) {
        boolean enabled = FriendlyBeaconsConfig.effectEnabled(effect);
        return Component.literal(BeaconSettings.effectName(effect) + ": ")
                .append(Component.translatable(enabled ? "friendlybeacons.command.enabled" : "friendlybeacons.command.disabled")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
    }
}
