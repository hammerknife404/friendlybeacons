package com.friendlybeacons.client;

import com.friendlybeacons.BeaconSettings;
import com.friendlybeacons.BeaconTargetsHolder;
import com.friendlybeacons.FriendlyBeacons;
import com.friendlybeacons.mixin.client.BeaconScreenAccessor;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.BeaconMenu;
import org.jetbrains.annotations.Nullable;

/**
 * Tier 5 row in the Secondary Power column of the beacon screen: a 5-layer pyramid icon,
 * a "II" button (both powers at level II) and a third-power button that steps to the next
 * effect when clicked while selected. Greyed out until the 5th layer exists. Like vanilla
 * powers, the pick only applies when Done is pressed with a payment item.
 */
public class Tier5Row extends AbstractWidget {
    private static final ResourceLocation PYRAMID = ResourceLocation.fromNamespaceAndPath(FriendlyBeacons.MOD_ID, "container/beacon/tier5_pyramid");
    private static final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("container/beacon/button");
    private static final ResourceLocation BUTTON_DISABLED = ResourceLocation.withDefaultNamespace("container/beacon/button_disabled");
    private static final ResourceLocation BUTTON_SELECTED = ResourceLocation.withDefaultNamespace("container/beacon/button_selected");
    private static final ResourceLocation BUTTON_HIGHLIGHTED = ResourceLocation.withDefaultNamespace("container/beacon/button_highlighted");

    // Relative to the beacon screen: lines up with the third primary row, centered in the secondary column.
    public static final int SCREEN_X = 131;
    public static final int SCREEN_Y = 72;
    private static final int ICON_WIDTH = 23;
    private static final int BUTTON_SIZE = 22;
    private static final int II_X = ICON_WIDTH + 6;
    private static final int THIRD_X = II_X + BUTTON_SIZE + 2;
    private static final int TOOLTIP_WIDTH = 170;

    private static WeakReference<Tier5Row> current = new WeakReference<>(null);

    private final BeaconScreen screen;
    private final BeaconMenu menu;
    private final BeaconTargetsHolder settings;
    /** Bonus picked on this screen, or null to show the beacon's saved bonus. */
    @Nullable
    private Integer pending;
    private int thirdEffect;
    private boolean thirdPicked;

    public Tier5Row(BeaconScreen screen) {
        super(screen.getGuiLeft() + SCREEN_X, screen.getGuiTop() + SCREEN_Y, THIRD_X + BUTTON_SIZE, BUTTON_SIZE,
                Component.translatable("friendlybeacons.bonus.title"));
        this.screen = screen;
        this.menu = screen.getMenu();
        this.settings = (BeaconTargetsHolder) menu;
        current = new WeakReference<>(this);
    }

    /** Called when the vanilla Done button is pressed. */
    public static void onConfirm() {
        Tier5Row row = current.get();
        Minecraft minecraft = Minecraft.getInstance();
        if (row == null || minecraft.screen != row.screen || minecraft.gameMode == null || !row.unlocked()) return;
        minecraft.gameMode.handleInventoryButtonClick(row.menu.containerId, BeaconSettings.BUTTON_SET_BONUS + row.selectedBonus());
    }

    private boolean unlocked() {
        return menu.getLevels() >= 5;
    }

    @Nullable
    private Holder<MobEffect> primary() {
        return ((BeaconScreenAccessor) screen).friendlybeacons$getPrimary();
    }

    @Nullable
    private Holder<MobEffect> secondary() {
        return ((BeaconScreenAccessor) screen).friendlybeacons$getSecondary();
    }

    private int bonus() {
        return pending != null ? pending : settings.friendlybeacons$getBonus();
    }

    private boolean thirdSelected() {
        return bonus() != BeaconSettings.BONUS_BOTH_II;
    }

    /** Effect shown on the third-power button: the saved one, else the next effect not already covered. */
    private int thirdEffect() {
        int saved = settings.friendlybeacons$getBonus();
        if (!thirdPicked && saved != BeaconSettings.BONUS_BOTH_II) thirdEffect = saved;
        if (thirdEffect < 1 || BeaconSettings.isRedundant(thirdEffect, primary(), secondary())) {
            thirdEffect = BeaconSettings.nextEffectBonus(Math.max(thirdEffect, 0), primary(), secondary());
        }
        return thirdEffect;
    }

    private int selectedBonus() {
        return thirdSelected() ? thirdEffect() : BeaconSettings.BONUS_BOTH_II;
    }

    private boolean iiAvailable() {
        return unlocked() && !BeaconSettings.isRedundant(BeaconSettings.BONUS_BOTH_II, primary(), secondary());
    }

    private boolean thirdAvailable() {
        return unlocked() && !BeaconSettings.isRedundant(thirdEffect(), primary(), secondary());
    }

    private boolean isOver(int buttonX, double mouseX, double mouseY) {
        int x = getX() + buttonX;
        return mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= getY() && mouseY < getY() + BUTTON_SIZE;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        boolean unlocked = unlocked();
        if (!unlocked) graphics.setColor(0.4F, 0.4F, 0.4F, 1.0F);
        graphics.blitSprite(PYRAMID, getX(), getY(), ICON_WIDTH, BUTTON_SIZE);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        boolean overII = isHovered() && isOver(II_X, mouseX, mouseY);
        boolean overThird = isHovered() && isOver(THIRD_X, mouseX, mouseY);
        Holder<MobEffect> iiIcon = secondary() != null ? secondary() : primary();
        renderButton(graphics, II_X, iiAvailable(), !thirdSelected(), overII, iiIcon);
        graphics.drawString(font, "II", getX() + II_X + BUTTON_SIZE - 3 - font.width("II"), getY() + BUTTON_SIZE - 10, 0xFFFFFF, true);
        int third = thirdEffect();
        renderButton(graphics, THIRD_X, thirdAvailable(), thirdSelected(), overThird, BeaconSettings.bonusEffect(third));

        List<FormattedCharSequence> tooltip = null;
        if (overII) {
            tooltip = tooltip(iiTitle(), "friendlybeacons.bonus.both_ii.desc", unlocked);
        } else if (overThird) {
            Holder<MobEffect> effect = BeaconSettings.bonusEffect(third);
            MutableComponent title = effect != null ? Component.translatable(effect.value().getDescriptionId()) : Component.empty();
            tooltip = tooltip(title, thirdSelected() ? "friendlybeacons.bonus.extra.cycle" : "friendlybeacons.bonus.extra.desc", unlocked);
        }
        if (tooltip != null) screen.setTooltipForNextRenderPass(tooltip);
    }

    private MutableComponent iiTitle() {
        Holder<MobEffect> primary = primary(), secondary = secondary();
        if (primary == null) return Component.translatable("friendlybeacons.bonus.both_ii");
        MutableComponent title = Component.translatable(primary.value().getDescriptionId()).append(" II");
        if (secondary != null && !secondary.equals(primary)) {
            title.append(" + ").append(Component.translatable(secondary.value().getDescriptionId())).append(" II");
        }
        return title;
    }

    private List<FormattedCharSequence> tooltip(Component title, String descKey, boolean unlocked) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = new ArrayList<>(font.split(title, TOOLTIP_WIDTH));
        String key = unlocked ? descKey : "friendlybeacons.bonus.locked";
        lines.addAll(font.split(Component.translatable(key).withStyle(ChatFormatting.GRAY), TOOLTIP_WIDTH));
        return lines;
    }

    private void renderButton(GuiGraphics graphics, int buttonX, boolean active, boolean selected, boolean hovered,
            @Nullable Holder<MobEffect> icon) {
        int x = getX() + buttonX, y = getY();
        ResourceLocation sprite = !active ? BUTTON_DISABLED : selected ? BUTTON_SELECTED : hovered ? BUTTON_HIGHLIGHTED : BUTTON;
        graphics.blitSprite(sprite, x, y, BUTTON_SIZE, BUTTON_SIZE);
        if (icon != null) {
            graphics.blit(x + 2, y + 2, 0, 18, 18, Minecraft.getInstance().getMobEffectTextures().get(icon));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || !isValidClickButton(button) || !unlocked()) return false;
        Minecraft minecraft = Minecraft.getInstance();
        if (isOver(II_X, mouseX, mouseY) && iiAvailable()) {
            if (thirdSelected()) {
                playDownSound(minecraft.getSoundManager());
                pending = BeaconSettings.BONUS_BOTH_II;
            }
            return true;
        }
        if (isOver(THIRD_X, mouseX, mouseY) && thirdAvailable()) {
            playDownSound(minecraft.getSoundManager());
            if (thirdSelected()) {
                thirdEffect = BeaconSettings.nextEffectBonus(thirdEffect(), primary(), secondary());
            }
            thirdPicked = true;
            pending = thirdEffect();
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
