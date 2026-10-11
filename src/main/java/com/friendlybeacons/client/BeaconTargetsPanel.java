package com.friendlybeacons.client;

import com.friendlybeacons.BeaconSettings;
import com.friendlybeacons.BeaconTargets;
import com.friendlybeacons.BeaconTargetsHolder;
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
 * Side panel next to the beacon screen: one checkbox per {@link BeaconTargets} category,
 * a radius cycle button and a preview of the beacon's powers. Kept to one panel on the left so
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
    private static final int POWERS_TOP = RADIUS_TOP + BUTTON_HEIGHT + 2 * GAP;

    private final BeaconMenu menu;
    private final BeaconTargetsHolder settings;
    private final PowersPreview powers;

    public BeaconTargetsPanel(int x, int y, BeaconScreen screen, @Nullable Tier5Row tier5) {
        super(x, y, WIDTH, POWERS_TOP + PADDING, Component.translatable("friendlybeacons.targets.title"));
        this.menu = screen.getMenu();
        this.settings = (BeaconTargetsHolder) menu;
        this.powers = new PowersPreview(screen, tier5);
    }

    private int rowY(int row) {
        return getY() + PADDING + TITLE_HEIGHT + row * ROW_HEIGHT;
    }

    private int rowAt(double mouseX, double mouseY) {
        if (mouseX < getX() + PADDING || mouseX >= getX() + width - PADDING) return -1;
        for (int row = 0; row < BeaconTargets.values().length; row++) {
            int y = rowY(row);
            if (mouseY >= y && mouseY < y + BOX) return row;
        }
        return -1;
    }

    private boolean isOverRadiusButton(double mouseX, double mouseY) {
        int y = getY() + RADIUS_TOP;
        return mouseX >= getX() + PADDING && mouseX < getX() + width - PADDING && mouseY >= y && mouseY < y + BUTTON_HEIGHT;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        height = POWERS_TOP + powers.update(mouseX, mouseY, width - 2 * PADDING) + PADDING;
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
            boolean selected = values[row].isIn(mask);
            boolean hovered = row == hoveredRow;
            ResourceLocation sprite = selected
                    ? (hovered ? CHECKBOX_SELECTED_HIGHLIGHTED : CHECKBOX_SELECTED)
                    : (hovered ? CHECKBOX_HIGHLIGHTED : CHECKBOX);
            int rowY = rowY(row);
            graphics.blitSprite(sprite, x + PADDING, rowY, BOX, BOX);
            graphics.drawString(font, values[row].label(), x + PADDING + BOX + 4, rowY + (BOX - font.lineHeight) / 2 + 1, TEXT_COLOR, false);
        }

        boolean radiusHovered = isHovered() && isOverRadiusButton(mouseX, mouseY);
        renderRadiusButton(graphics, font, radiusHovered);

        // Divider above the Powers section, in the panel's bevel colors.
        int dividerY = y + POWERS_TOP - GAP - 1;
        graphics.fill(x + PADDING, dividerY - 1, right - PADDING, dividerY, 0xFF555555);
        graphics.fill(x + PADDING, dividerY, right - PADDING, dividerY + 1, 0xFFFFFFFF);
        powers.render(graphics, x + PADDING, y + POWERS_TOP, width - 2 * PADDING);

        List<FormattedCharSequence> tooltip = null;
        if (hoveredRow >= 0) {
            tooltip = font.split(values[hoveredRow].description(), TOOLTIP_WIDTH);
        } else if (radiusHovered) {
            tooltip = font.split(Component.translatable("friendlybeacons.radius.desc"), TOOLTIP_WIDTH);
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
