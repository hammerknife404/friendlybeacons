package com.friendlybeacons.client;

import com.friendlybeacons.BeaconTargets;
import com.friendlybeacons.BeaconTargetsHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.BeaconMenu;

/** Side panel next to the beacon screen with one checkbox per {@link BeaconTargets} category. */
public class BeaconTargetsPanel extends AbstractWidget {
    private static final ResourceLocation CHECKBOX = ResourceLocation.withDefaultNamespace("widget/checkbox");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED = ResourceLocation.withDefaultNamespace("widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("widget/checkbox_selected_highlighted");

    private static final int PADDING = 6;
    private static final int TITLE_HEIGHT = 13;
    private static final int BOX = 17;
    private static final int ROW_HEIGHT = 19;
    private static final int TEXT_COLOR = 0x404040;

    public static final int WIDTH = 84;
    public static final int HEIGHT = PADDING + TITLE_HEIGHT + BeaconTargets.values().length * ROW_HEIGHT - 2 + PADDING;

    private final BeaconMenu menu;
    private final BeaconTargetsHolder targets;

    public BeaconTargetsPanel(int x, int y, BeaconMenu menu) {
        super(x, y, WIDTH, HEIGHT, Component.translatable("friendlybeacons.targets.title"));
        this.menu = menu;
        this.targets = (BeaconTargetsHolder) menu;
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

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int x = getX(), y = getY(), right = x + width, bottom = y + height;

        // Vanilla container-style bevelled panel.
        graphics.fill(x, y, right, bottom, 0xFF000000);
        graphics.fill(x + 1, y + 1, right - 1, bottom - 1, 0xFFFFFFFF);
        graphics.fill(x + 2, y + 2, right - 1, bottom - 1, 0xFF555555);
        graphics.fill(x + 2, y + 2, right - 2, bottom - 2, 0xFFC6C6C6);

        graphics.drawString(font, getMessage(), x + PADDING, y + PADDING, TEXT_COLOR, false);

        int mask = targets.friendlybeacons$getTargets();
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

        if (hoveredRow >= 0 && Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.setTooltipForNextRenderPass(font.split(values[hoveredRow].description(), 170));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || !isValidClickButton(button)) return false;
        int row = rowAt(mouseX, mouseY);
        if (row < 0) return false;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode == null) return false;
        playDownSound(minecraft.getSoundManager());
        targets.friendlybeacons$setTargets(targets.friendlybeacons$getTargets() ^ BeaconTargets.values()[row].bit());
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, row);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
