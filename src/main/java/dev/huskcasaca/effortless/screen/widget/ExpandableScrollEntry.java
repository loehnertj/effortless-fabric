package dev.huskcasaca.effortless.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ExpandableScrollEntry implements ScrollPane.IScrollEntry {

    public ScrollPane scrollPane;
    protected Font font;
    protected Minecraft mc;

    protected boolean isCollapsed = true;
    protected int left, right, top, bottom;

    public ExpandableScrollEntry(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
        this.font = scrollPane.font;
        this.mc = Minecraft.getInstance();
    }

    @Override
    public void init(List<Renderable> renderables) {
        left = Math.round(scrollPane.getWidth() / 2f - 140);
        right = Math.round(scrollPane.getWidth() / 2f + 140);
        top = Math.round(scrollPane.getHeight() / 2f - 100);
        bottom = Math.round(scrollPane.getHeight() / 2f + 100);
    }

    @Override
    public void updateScreen() {
    }

    @Override
    public void drawTooltip(GuiGraphics guiGraphics, Screen guiScreen, int mouseX, int mouseY) {
    }

    @Override
    public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

    }

    @Override
    public boolean charTyped(char eventChar, int eventKey) {
        return false;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        return false;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

    }

    @Override
    public void onGuiClosed() {
    }

    @Override
    public int getHeight() {
        return isCollapsed ? getCollapsedHeight() : getExpandedHeight();
    }

    public void setCollapsed(boolean collapsed) {
        this.isCollapsed = collapsed;
    }

    protected String getName() {
        return "Collapsible scroll entry";
    }

    protected int getCollapsedHeight() {
        return 34;
    }

    protected int getExpandedHeight() {
        return 100;
    }
}
