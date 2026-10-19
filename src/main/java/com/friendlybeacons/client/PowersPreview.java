package com.friendlybeacons.client;

import com.friendlybeacons.BeaconSettings;
import com.friendlybeacons.BeaconTargetsHolder;
import com.friendlybeacons.mixin.client.BeaconPowerButtonAccessor;
import com.friendlybeacons.mixin.client.BeaconScreenAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.BeaconMenu;
import org.jetbrains.annotations.Nullable;

/**
 * "Powers" section of the side panel: the effects the beacon will give. Shows the picks on the
 * screen (or what hovering a power button would pick), with effects the beacon doesn't give yet
 * in green and effects it would stop giving struck through.
 */
class PowersPreview {
    private static final int TITLE_HEIGHT = 13;
    private static final int ICON = 18;
    private static final int ROW_HEIGHT = 20;
    private static final int TEXT_COLOR = 0x404040;
    private static final int ADDED_COLOR = 0x207020;
    private static final int GRAY_COLOR = 0x8B8B8B;

    private final BeaconScreen screen;
    private final BeaconMenu menu;
    private final BeaconTargetsHolder settings;
    @Nullable
    private final Tier5Row tier5;

    private record Row(BeaconSettings.Power power, int color, boolean removed) {}

    private final List<Row> rows = new ArrayList<>();
    private List<FormattedCharSequence> message = List.of();
    private List<FormattedCharSequence> footer = List.of();

    PowersPreview(BeaconScreen screen, @Nullable Tier5Row tier5) {
        this.screen = screen;
        this.menu = screen.getMenu();
        this.settings = (BeaconTargetsHolder) menu;
        this.tier5 = tier5;
    }

    /** Recomputes the rows for this frame and returns the section's height. */
    int update(int mouseX, int mouseY, int width) {
        Font font = Minecraft.getInstance().font;
        int levels = menu.getLevels();
        boolean glow = settings.friendlybeacons$getGlow();
        int disabled = settings.friendlybeacons$getDisabledEffects();
        List<BeaconSettings.Power> applied = BeaconSettings.powers(levels, menu.getPrimaryEffect(), menu.getSecondaryEffect(),
                settings.friendlybeacons$getBonus(), glow, disabled);

        // Picks on the screen, replaced by whatever the hovered button would pick.
        BeaconScreenAccessor picks = (BeaconScreenAccessor) screen;
        Holder<MobEffect> primary = picks.friendlybeacons$getPrimary(), secondary = picks.friendlybeacons$getSecondary();
        int bonus = tier5 != null ? tier5.selectedBonus() : settings.friendlybeacons$getBonus();
        List<BeaconSettings.Power> pending = BeaconSettings.powers(levels, primary, secondary, bonus, glow, disabled);
        for (GuiEventListener child : screen.children()) {
            if (child instanceof BeaconPowerButtonAccessor button && child.isMouseOver(mouseX, mouseY)) {
                if (button.friendlybeacons$isPrimary()) primary = button.friendlybeacons$getEffect();
                else secondary = button.friendlybeacons$getEffect();
            }
        }
        Integer hoveredBonus = tier5 != null ? tier5.bonusAt(mouseX, mouseY) : null;
        if (hoveredBonus != null) bonus = hoveredBonus;
        List<BeaconSettings.Power> shown = BeaconSettings.powers(levels, primary, secondary, bonus, glow, disabled);

        rows.clear();
        for (BeaconSettings.Power power : shown) {
            rows.add(new Row(power, applied.contains(power) ? TEXT_COLOR : ADDED_COLOR, false));
        }
        for (BeaconSettings.Power power : applied) {
            if (!shown.contains(power)) rows.add(new Row(power, GRAY_COLOR, true));
        }
        String messageKey = levels <= 0 ? "friendlybeacons.powers.no_pyramid"
                : primary == null ? "friendlybeacons.powers.none"
                : "friendlybeacons.powers.disabled";
        message = rows.isEmpty() ? font.split(Component.translatable(messageKey), width) : List.of();
        footer = pending.equals(applied) ? List.of()
                : font.split(Component.translatable("friendlybeacons.powers.pending").withStyle(ChatFormatting.ITALIC), width);
        return TITLE_HEIGHT + rows.size() * ROW_HEIGHT + (message.size() + footer.size()) * font.lineHeight;
    }

    /** Draws the section computed by the last {@link #update}. */
    void render(GuiGraphics graphics, int x, int y, int width) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("friendlybeacons.powers.title"), x, y, TEXT_COLOR, false);
        y += TITLE_HEIGHT;
        for (FormattedCharSequence line : message) {
            graphics.drawString(font, line, x, y, GRAY_COLOR, false);
            y += font.lineHeight;
        }
        int textX = x + ICON + 3;
        for (Row row : rows) {
            if (row.removed()) graphics.setColor(0.5F, 0.5F, 0.5F, 1.0F);
            graphics.blit(x, y, 0, ICON, ICON, Minecraft.getInstance().getMobEffectTextures().get(row.power().effect()));
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            MutableComponent name = Component.translatable(row.power().effect().value().getDescriptionId());
            if (row.power().amplifier() > 0) {
                name.append(" ").append(Component.translatable("potion.potency." + row.power().amplifier()));
            }
            if (row.removed()) name.withStyle(ChatFormatting.STRIKETHROUGH);
            FormattedCharSequence line = font.split(name, x + width - textX).get(0);
            graphics.drawString(font, line, textX, y + (ICON - font.lineHeight) / 2 + 1, row.color(), false);
            y += ROW_HEIGHT;
        }
        for (FormattedCharSequence line : footer) {
            graphics.drawString(font, line, x, y, GRAY_COLOR, false);
            y += font.lineHeight;
        }
    }
}
