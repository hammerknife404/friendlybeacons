package com.friendlybeacons.client;

import com.friendlybeacons.FriendlyBeaconsConfig;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Context;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Element;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Filter;
import org.jetbrains.annotations.Nullable;

/** Config screen filter: draws the effect's icon next to each toggle in the effects section. */
class EffectToggleFilter implements Filter {
    private static final int ICON = 18;
    private static final int ICON_SPACE = ICON + 4;

    @Override
    @Nullable
    public Element filterEntry(Context context, String key, Element original) {
        if (!context.keylist().equals(List.of(FriendlyBeaconsConfig.EFFECTS_SECTION)) || original.option() == null) return original;
        Holder<MobEffect> effect = FriendlyBeaconsConfig.effectForKey(key);
        if (effect == null) return original;
        // Same option instance, so undo and reset keep working.
        AbstractWidget toggle = original.option().createButton(Minecraft.getInstance().options, 0, 0, Button.DEFAULT_WIDTH - ICON_SPACE);
        return new Element(original.name(), original.tooltip(), new IconToggle(effect, toggle), original.undoable());
    }

    /** An effect icon followed by the config's own on/off button, which gets all the input. */
    private static class IconToggle extends AbstractWidget {
        private final Holder<MobEffect> effect;
        private final AbstractWidget toggle;

        IconToggle(Holder<MobEffect> effect, AbstractWidget toggle) {
            super(0, 0, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, toggle.getMessage());
            this.effect = effect;
            this.toggle = toggle;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(getX() + 1, getY() + (getHeight() - ICON) / 2, 0, ICON, ICON, Minecraft.getInstance().getMobEffectTextures().get(effect));
            toggle.setPosition(getX() + ICON_SPACE, getY());
            toggle.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return toggle.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return toggle.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            toggle.updateNarration(output);
        }
    }
}
