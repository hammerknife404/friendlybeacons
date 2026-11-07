package com.friendlybeacons.client;

import com.friendlybeacons.BeaconSettings;
import com.friendlybeacons.BeaconTargets;
import com.friendlybeacons.BeaconTargetsHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.BeaconMenu;
import org.jetbrains.annotations.Nullable;

/**
 * Side panel next to the beacon screen: one checkbox per {@link BeaconTargets} category, with
 * a second column of tier 5 Glowing checkboxes on the right, a radius cycle button and a preview
 * of the beacon's powers. Kept to one panel on the left so
 * item list overlays such as JEI's on the right don't cover it.
 */
public class BeaconTargetsPanel extends AbstractWidget {
    private static final ResourceLocation CHECKBOX = ResourceLocation.withDefaultNamespace("widget/checkbox");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED = ResourceLocation.withDefaultNamespace("widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation BUTTON_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("widget/button_highlighted");
    private static final ResourceLocation BUTTON_DISABLED = ResourceLocation.withDefaultNamespace("widget/button_disabled");

    private static final int PADDING = 6;
    private static final int TITLE_HEIGHT = 13;
    private static final int BOX = 17;
    private static final int ROW_HEIGHT = 19;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 4;
    private static final int TEXT_COLOR = 0x404040;
    private static final int TOOLTIP_WIDTH = 170;

    /** Fits "Regeneration II" next to its icon. */
    public static final int WIDTH = 112;
    private static final int ROWS_BOTTOM = PADDING + TITLE_HEIGHT + BeaconTargets.values().length * ROW_HEIGHT - 2;
    private static final int RADIUS_TOP = ROWS_BOTTOM + GAP;
    /** Size of the Glowing icon above the Glowing column. */
    private static final int GLOW_ICON = 12;

    private final BeaconMenu menu;
    private final BeaconTargetsHolder settings;
    private final PowersPreview powers;
    /** The tier 5 Glowing column is only shown when tier 5 is enabled. */
    private final boolean showGlow;
    private final int powersTop;

    public BeaconTargetsPanel(int x, int y, BeaconScreen screen, @Nullable Tier5Row tier5) {
        super(x, y, WIDTH, 0, Component.translatable("friendlybeacons.targets.title"));
        this.menu = screen.getMenu();
        this.settings = (BeaconTargetsHolder) menu;
        this.powers = new PowersPreview(screen, tier5);
        this.showGlow = tier5 != null;
        this.powersTop = RADIUS_TOP + BUTTON_HEIGHT + 2 * GAP;
        this.height = powersTop + PADDING;
    }

    private int rowY(int row) {
        return getY() + PADDING + TITLE_HEIGHT + row * ROW_HEIGHT;
    }

    private int glowX() {
        return getX() + width - PADDING - BOX;
    }

    private int rowAtY(double mouseY) {
        for (int row = 0; row < BeaconTargets.values().length; row++) {
            int y = rowY(row);
            if (mouseY >= y && mouseY < y + BOX) return row;
        }
        return -1;
    }

    /** Affects row under the mouse: its checkbox and label, left of the Glowing column. */
    private int rowAt(double mouseX, double mouseY) {
        int right = showGlow ? glowX() - GAP : getX() + width - PADDING;
        if (mouseX < getX() + PADDING || mouseX >= right) return -1;
        return rowAtY(mouseY);
    }

    /** Row of the Glowing checkbox under the mouse. */
    private int glowRowAt(double mouseX, double mouseY) {
        if (!showGlow || mouseX < glowX() || mouseX >= glowX() + BOX) return -1;
        return rowAtY(mouseY);
    }

    private boolean isOverGlowIcon(double mouseX, double mouseY) {
        int x = glowX() + (BOX - GLOW_ICON) / 2, y = getY() + PADDING - 2;
        return showGlow && mouseX >= x && mouseX < x + GLOW_ICON && mouseY >= y && mouseY < y + GLOW_ICON;
    }

    private boolean isOverRadiusButton(double mouseX, double mouseY) {
        int y = getY() + RADIUS_TOP;
        return mouseX >= getX() + PADDING && mouseX < getX() + width - PADDING && mouseY >= y && mouseY < y + BUTTON_HEIGHT;
    }

    private boolean glowDisabled() {
        return BeaconSettings.isDisabled(BeaconSettings.GLOW_EFFECT, settings.friendlybeacons$getDisabledEffects());
    }

    private boolean glowAvailable() {
        return menu.getLevels() >= 5 && !glowDisabled();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        height = powersTop + powers.update(mouseX, mouseY, width - 2 * PADDING) + PADDING;
        int x = getX(), y = getY(), right = x + width, bottom = y + height;

        // Vanilla container-style bevelled panel.
        graphics.fill(x, y, right, bottom, 0xFF000000);
        graphics.fill(x + 1, y + 1, right - 1, bottom - 1, 0xFFFFFFFF);
        graphics.fill(x + 2, y + 2, right - 1, bottom - 1, 0xFF555555);
        graphics.fill(x + 2, y + 2, right - 2, bottom - 2, 0xFFC6C6C6);

        graphics.drawString(font, getMessage(), x + PADDING, y + PADDING, TEXT_COLOR, false);

        int mask = settings.friendlybeacons$getTargets();
        int hoveredRow = isHovered() ? rowAt(mouseX, mouseY) : -1;
        BeaconTargets[] values = BeaconTargets.values();
        for (int row = 0; row < values.length; row++) {
            int rowY = rowY(row);
            renderCheckbox(graphics, x + PADDING, rowY, values[row].isIn(mask), row == hoveredRow, true);
            graphics.drawString(font, values[row].label(), x + PADDING + BOX + 4, rowY + (BOX - font.lineHeight) / 2 + 1, TEXT_COLOR, false);
        }

        int hoveredGlowRow = isHovered() ? glowRowAt(mouseX, mouseY) : -1;
        boolean glowIconHovered = isHovered() && isOverGlowIcon(mouseX, mouseY);
        if (showGlow) renderGlowColumn(graphics, hoveredGlowRow);

        boolean radiusHovered = isHovered() && isOverRadiusButton(mouseX, mouseY);
        renderRadiusButton(graphics, font, radiusHovered);

        // Divider above the Powers section, in the panel's bevel colors.
        int dividerY = y + powersTop - GAP - 1;
        graphics.fill(x + PADDING, dividerY - 1, right - PADDING, dividerY, 0xFF555555);
        graphics.fill(x + PADDING, dividerY, right - PADDING, dividerY + 1, 0xFFFFFFFF);
        powers.render(graphics, x + PADDING, y + powersTop, width - 2 * PADDING);

        List<FormattedCharSequence> tooltip = null;
        if (hoveredRow >= 0) {
            tooltip = font.split(values[hoveredRow].description(), TOOLTIP_WIDTH);
        } else if (radiusHovered) {
            tooltip = font.split(Component.translatable("friendlybeacons.radius.desc"), TOOLTIP_WIDTH);
        } else if (hoveredGlowRow >= 0 || glowIconHovered) {
            Component heading = hoveredGlowRow >= 0
                    ? Component.translatable("friendlybeacons.glow.target", values[hoveredGlowRow].label())
                    : Component.translatable("friendlybeacons.glow");
            String key = glowDisabled() ? "friendlybeacons.glow.disabled"
                    : menu.getLevels() < 5 ? "friendlybeacons.glow.locked"
                    : "friendlybeacons.glow.desc";
            tooltip = new ArrayList<>(font.split(heading, TOOLTIP_WIDTH));
            tooltip.addAll(font.split(Component.translatable(key), TOOLTIP_WIDTH));
        }
        if (tooltip != null && Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.setTooltipForNextRenderPass(tooltip);
        }
    }

    private void renderRadiusButton(GuiGraphics graphics, Font font, boolean hovered) {
        int radius = settings.friendlybeacons$getRadius();
        boolean enabled = radius > 0;
        Component label = enabled
                ? Component.translatable("friendlybeacons.radius", radius)
                : Component.translatable("friendlybeacons.radius.none");
        int bx = getX() + PADDING, by = getY() + RADIUS_TOP, bw = width - 2 * PADDING;
        ResourceLocation sprite = !enabled ? BUTTON_DISABLED : hovered ? BUTTON_HIGHLIGHTED : BUTTON;
        graphics.blitSprite(sprite, bx, by, bw, BUTTON_HEIGHT);
        graphics.drawCenteredString(font, label, bx + bw / 2, by + (BUTTON_HEIGHT - 8) / 2, enabled ? 0xFFFFFF : 0xA0A0A0);
    }

    private static void renderCheckbox(GuiGraphics graphics, int x, int y, boolean selected, boolean hovered, boolean available) {
        boolean highlighted = hovered && available;
        ResourceLocation sprite = selected
                ? (highlighted ? CHECKBOX_SELECTED_HIGHLIGHTED : CHECKBOX_SELECTED)
                : (highlighted ? CHECKBOX_HIGHLIGHTED : CHECKBOX);
        if (!available) graphics.setColor(0.5F, 0.5F, 0.5F, 1.0F);
        graphics.blitSprite(sprite, x, y, BOX, BOX);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Tier 5 Glowing checkboxes, one per Affects row, under a Glowing icon. Independent of the
     * Affects checkboxes. Dimmed until tier 5 or while Glowing is disabled server-wide.
     */
    private void renderGlowColumn(GuiGraphics graphics, int hoveredRow) {
        boolean available = glowAvailable();
        int x = glowX();
        if (!available) graphics.setColor(0.5F, 0.5F, 0.5F, 1.0F);
        graphics.blit(x + (BOX - GLOW_ICON) / 2, getY() + PADDING - 2, 0, GLOW_ICON, GLOW_ICON,
                Minecraft.getInstance().getMobEffectTextures().get(BeaconSettings.GLOW_EFFECT));
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int mask = settings.friendlybeacons$getGlowTargets();
        BeaconTargets[] values = BeaconTargets.values();
        for (int row = 0; row < values.length; row++) {
            renderCheckbox(graphics, x, rowY(row), values[row].isIn(mask), row == hoveredRow, available);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || !isValidClickButton(button)) return false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode == null) return false;

        int row = rowAt(mouseX, mouseY);
        if (row >= 0) {
            playDownSound(minecraft.getSoundManager());
            settings.friendlybeacons$setTargets(settings.friendlybeacons$getTargets() ^ BeaconTargets.values()[row].bit());
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, row);
            return true;
        }

        int glowRow = glowRowAt(mouseX, mouseY);
        if (glowRow >= 0) {
            if (!glowAvailable()) return false;
            playDownSound(minecraft.getSoundManager());
            settings.friendlybeacons$setGlowTargets(settings.friendlybeacons$getGlowTargets() ^ BeaconTargets.values()[glowRow].bit());
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, BeaconSettings.BUTTON_GLOW_TARGETS + glowRow);
            return true;
        }

        if (!isOverRadiusButton(mouseX, mouseY) || settings.friendlybeacons$getRadius() <= 0) return false;
        playDownSound(minecraft.getSoundManager());
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, BeaconSettings.BUTTON_RADIUS);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
