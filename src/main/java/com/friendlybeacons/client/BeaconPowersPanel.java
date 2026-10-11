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
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.BeaconMenu;
import org.jetbrains.annotations.Nullable;

/**
 * Side panel to the right of the beacon screen listing the effects the beacon will give.
 * Shows the picks on the screen (or what hovering a power button would pick), with effects
 * the beacon doesn't give yet in green and effects it would stop giving struck through.
 */
public class BeaconPowersPanel extends AbstractWidget {
    private static final int PADDING = 6;
    private static final int TITLE_HEIGHT = 13;
    private static final int ICON = 18;
    private static final int ROW_HEIGHT = 20;
    private static final int TEXT_COLOR = 0x404040;
    private static final int ADDED_COLOR = 0x207020;
    private static final int REMOVED_COLOR = 0x8B8B8B;
    /** Matches the Affects panel so both sides of the beacon screen line up. */
    private static final int MIN_HEIGHT = 123;

    public static final int WIDTH = 120;

    private final BeaconScreen screen;
    private final BeaconMenu menu;
    private final BeaconTargetsHolder settings;
    @Nullable
    private final Tier5Row tier5;

    public BeaconPowersPanel(int x, int y, BeaconScreen screen, @Nullable Tier5Row tier5) {
        super(x, y, WIDTH, MIN_HEIGHT, Component.translatable("friendlybeacons.powers.title"));
        this.screen = screen;
        this.menu = screen.getMenu();
        this.settings = (BeaconTargetsHolder) menu;
        this.tier5 = tier5;
    }

    private record Row(BeaconSettings.Power power, int color, boolean removed) {}

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int levels = menu.getLevels();
        List<BeaconSettings.Power> applied = BeaconSettings.powers(levels, menu.getPrimaryEffect(), menu.getSecondaryEffect(),
                settings.friendlybeacons$getBonus());

        // Picks on the screen, replaced by whatever the hovered button would pick.
        BeaconScreenAccessor picks = (BeaconScreenAccessor) screen;
        Holder<MobEffect> primary = picks.friendlybeacons$getPrimary(), secondary = picks.friendlybeacons$getSecondary();
        int bonus = tier5 != null ? tier5.selectedBonus() : settings.friendlybeacons$getBonus();
        List<BeaconSettings.Power> pending = BeaconSettings.powers(levels, primary, secondary, bonus);
        for (GuiEventListener child : screen.children()) {
            if (child instanceof BeaconPowerButtonAccessor button && child.isMouseOver(mouseX, mouseY)) {
                if (button.friendlybeacons$isPrimary()) primary = button.friendlybeacons$getEffect();
                else secondary = button.friendlybeacons$getEffect();
            }
        }
        Integer hoveredBonus = tier5 != null ? tier5.bonusAt(mouseX, mouseY) : null;
        if (hoveredBonus != null) bonus = hoveredBonus;
        List<BeaconSettings.Power> shown = BeaconSettings.powers(levels, primary, secondary, bonus);

        List<Row> rows = new ArrayList<>();
        for (BeaconSettings.Power power : shown) {
            rows.add(new Row(power, applied.contains(power) ? TEXT_COLOR : ADDED_COLOR, false));
        }
        for (BeaconSettings.Power power : applied) {
            if (!shown.contains(power)) rows.add(new Row(power, REMOVED_COLOR, true));
        }

        List<FormattedCharSequence> footer = pending.equals(applied) ? List.of()
                : font.split(Component.translatable("friendlybeacons.powers.pending").withStyle(ChatFormatting.ITALIC), width - 2 * PADDING);
        int rowsHeight = Math.max(rows.size(), 1) * ROW_HEIGHT;
        height = Math.max(MIN_HEIGHT, PADDING + TITLE_HEIGHT + rowsHeight + footer.size() * font.lineHeight + PADDING);

        int x = getX(), y = getY(), right = x + width, bottom = y + height;
        // Vanilla container-style bevelled panel, same as the Affects panel.
        graphics.fill(x, y, right, bottom, 0xFF000000);
        graphics.fill(x + 1, y + 1, right - 1, bottom - 1, 0xFFFFFFFF);
        graphics.fill(x + 2, y + 2, right - 1, bottom - 1, 0xFF555555);
        graphics.fill(x + 2, y + 2, right - 2, bottom - 2, 0xFFC6C6C6);

        graphics.drawString(font, getMessage(), x + PADDING, y + PADDING, TEXT_COLOR, false);

        int rowY = y + PADDING + TITLE_HEIGHT;
        if (rows.isEmpty()) {
            Component none = Component.translatable(levels > 0 ? "friendlybeacons.powers.none" : "friendlybeacons.powers.no_pyramid");
            for (FormattedCharSequence line : font.split(none, width - 2 * PADDING)) {
                graphics.drawString(font, line, x + PADDING, rowY + (ICON - font.lineHeight) / 2 + 1, REMOVED_COLOR, false);
                rowY += font.lineHeight;
            }
        }
        int textX = x + PADDING + ICON + 3;
        for (Row row : rows) {
            if (row.removed()) graphics.setColor(0.5F, 0.5F, 0.5F, 1.0F);
            graphics.blit(x + PADDING, rowY, 0, ICON, ICON, Minecraft.getInstance().getMobEffectTextures().get(row.power().effect()));
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            MutableComponent name = Component.translatable(row.power().effect().value().getDescriptionId());
            if (row.power().amplifier() > 0) {
                name.append(" ").append(Component.translatable("potion.potency." + row.power().amplifier()));
            }
            if (row.removed()) name.withStyle(ChatFormatting.STRIKETHROUGH);
            List<FormattedCharSequence> lines = font.split(name, right - PADDING - textX);
            graphics.drawString(font, lines.get(0), textX, rowY + (ICON - font.lineHeight) / 2 + 1, row.color(), false);
            rowY += ROW_HEIGHT;
        }

        int footerY = bottom - PADDING - footer.size() * font.lineHeight;
        for (FormattedCharSequence line : footer) {
            graphics.drawString(font, line, x + PADDING, footerY, REMOVED_COLOR, false);
            footerY += font.lineHeight;
        }
    }

    // Display only; never take clicks or focus from the beacon screen.
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
