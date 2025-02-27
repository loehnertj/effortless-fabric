package dev.huskcasaca.effortless.screen.buildmodifier;

import dev.huskcasaca.effortless.Effortless;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.buildmodifier.mirror.Mirror;
import dev.huskcasaca.effortless.building.ReachHelper;
import dev.huskcasaca.effortless.screen.widget.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MirrorSettingsPane extends ExpandableScrollEntry {

    protected static final ResourceLocation BUILDING_ICONS = new ResourceLocation(Effortless.MOD_ID, "textures/gui/building_icons.png");

    protected List<AbstractButton> mirrorButtonList = new ArrayList<>();
    protected List<IconButton> mirrorIconButtonList = new ArrayList<>();
    protected List<NumberField> mirrorNumberFieldList = new ArrayList<>();

    private NumberField textMirrorPosX, textMirrorPosY, textMirrorPosZ, textMirrorRadius;
    private Checkbox buttonMirrorEnabled, buttonMirrorX, buttonMirrorY, buttonMirrorZ;
    private IconButton buttonCurrentPosition, buttonToggleOdd, buttonDrawPlanes, buttonDrawLines;
    private boolean drawPlanes, drawLines, toggleOdd;

    public MirrorSettingsPane(ScrollPane scrollPane) {
        super(scrollPane);
    }

    @Override
    public void init(List<Renderable> renderables) {
        super.init(renderables);
        var modifierSettings = BuildModifierHelper.getModifierSettings(mc.player);
        var mirrorSettings = (modifierSettings!=null) ? modifierSettings.mirrorSettings(): new Mirror.MirrorSettings();
        int y = top - 2;
        buttonMirrorEnabled = Checkbox.builder(Component.literal(""), font)
                .pos(left-25+6, y)
                .selected(mirrorSettings.enabled())
                .onValueChange((checkbox, val) -> { setCollapsed(!val); } )
                .build();
        renderables.add(buttonMirrorEnabled);

        y = top + 20;
        textMirrorPosX = new NumberField(font, renderables, left + Dimen.BUTTON_OFFSET_X0, y, 90, 18);
        textMirrorPosX.setNumber(0);
        textMirrorPosX.setTooltip(
                Arrays.asList(Component.literal("The position of the mirror."), Component.literal("For odd numbered builds add 0.5.").withStyle(ChatFormatting.GRAY)));
        mirrorNumberFieldList.add(textMirrorPosX);

        textMirrorPosY = new NumberField(font, renderables, left + Dimen.BUTTON_OFFSET_X0, y + Dimen.BUTTON_VERTICAL_OFFSET, 90, 18);
        textMirrorPosY.setNumber(64);
        textMirrorPosY.setTooltip(Arrays.asList(Component.literal("The position of the mirror."), Component.literal("For odd numbered builds add 0.5.").withStyle(ChatFormatting.GRAY)));
        mirrorNumberFieldList.add(textMirrorPosY);

        textMirrorPosZ = new NumberField(font, renderables, left + Dimen.BUTTON_OFFSET_X0, y + Dimen.BUTTON_VERTICAL_OFFSET * 2, 90, 18);
        textMirrorPosZ.setNumber(0);
        textMirrorPosZ.setTooltip(Arrays.asList(Component.literal("The position of the mirror."), Component.literal("For odd numbered builds add 0.5.").withStyle(ChatFormatting.GRAY)));
        mirrorNumberFieldList.add(textMirrorPosZ);

        y = top + 44;
        buttonMirrorX = Checkbox.builder(Component.literal(" X"), font)
            .pos(left + Dimen.BUTTON_OFFSET_X1 , y)
            .selected(mirrorSettings.mirrorX())
            .build();
        mirrorButtonList.add(buttonMirrorX);

        buttonMirrorY = Checkbox.builder(Component.literal(" Y"), font)
                .pos(left + Dimen.BUTTON_OFFSET_X1  + 42, y)
                .selected(mirrorSettings.mirrorY())
                .build();
        mirrorButtonList.add(buttonMirrorY);

        buttonMirrorZ = Checkbox.builder(Component.literal(" Z"), font)
                .pos(left + Dimen.BUTTON_OFFSET_X1  + 42 * 2, y)
                .selected(mirrorSettings.mirrorZ())
                .build();
        mirrorButtonList.add(buttonMirrorZ);

        y = top + 47;
        textMirrorRadius = new NumberField(font, renderables, left + Dimen.BUTTON_OFFSET_X1, y, 80, 18);
        textMirrorRadius.setNumber(50);
        //TODO change to diameter (remove /2)
        textMirrorRadius.setTooltip(Arrays.asList(Component.literal("How far the mirror reaches in any direction."),
                Component.literal("Max: ").withStyle(ChatFormatting.GRAY).append(Component.literal(String.valueOf(ReachHelper.getMaxReachDistance(mc.player) / 2)).withStyle(ChatFormatting.GOLD)),
                Component.literal("Upgradeable in survival with reach upgrades.").withStyle(ChatFormatting.GRAY)));
        mirrorNumberFieldList.add(textMirrorRadius);

        y = top + 72;
        buttonCurrentPosition = new IconButton(left + Dimen.SECTION_OFFSET_X1, y, 0, 0, BUILDING_ICONS, button -> {
            var pos = new Vec3(Math.floor(mc.player.getX()) + 0.5, Math.floor(mc.player.getY()) + 0.5, Math.floor(mc.player.getZ()) + 0.5);
            textMirrorPosX.setNumber(pos.x);
            textMirrorPosY.setNumber(pos.y);
            textMirrorPosZ.setNumber(pos.z);
        });
        buttonCurrentPosition.setTooltip(Component.literal("Set mirror position to current player position"));
        mirrorIconButtonList.add(buttonCurrentPosition);

        buttonToggleOdd = new IconButton(left + Dimen.SECTION_OFFSET_X1 + 24, y, 0, 20, BUILDING_ICONS, button -> {
            toggleOdd = !toggleOdd;
            buttonToggleOdd.setUseAlternateIcon(toggleOdd);
            if (toggleOdd) {
                buttonToggleOdd.setTooltip(Arrays.asList(Component.literal("Set mirror position to corner of block"), Component.literal("for even numbered builds")));
                textMirrorPosX.setNumber(textMirrorPosX.getNumber() + 0.5);
                textMirrorPosY.setNumber(textMirrorPosY.getNumber() + 0.5);
                textMirrorPosZ.setNumber(textMirrorPosZ.getNumber() + 0.5);
            } else {
                buttonToggleOdd.setTooltip(Arrays.asList(Component.literal("Set mirror position to middle of block"), Component.literal("for odd numbered builds")));
                textMirrorPosX.setNumber(Math.floor(textMirrorPosX.getNumber()));
                textMirrorPosY.setNumber(Math.floor(textMirrorPosY.getNumber()));
                textMirrorPosZ.setNumber(Math.floor(textMirrorPosZ.getNumber()));
            }
        });
        buttonToggleOdd.setTooltip(Arrays.asList(Component.literal("Set mirror position to middle of block"), Component.literal("for odd numbered builds")));
        mirrorIconButtonList.add(buttonToggleOdd);

        buttonDrawLines = new IconButton(left + Dimen.SECTION_OFFSET_X1 + 24 * 2, y, 0, 40, BUILDING_ICONS, button -> {
            drawLines = !drawLines;
            buttonDrawLines.setUseAlternateIcon(drawLines);
            buttonDrawLines.setTooltip(Component.literal(drawLines ? "Hide lines" : "Show lines"));
        });
        buttonDrawLines.setTooltip(Component.literal("Show lines"));
        mirrorIconButtonList.add(buttonDrawLines);

        buttonDrawPlanes = new IconButton(left + Dimen.SECTION_OFFSET_X1 + 24 * 3, y, 0, 60, BUILDING_ICONS, button -> {
            drawPlanes = !drawPlanes;
            buttonDrawPlanes.setUseAlternateIcon(drawPlanes);
            buttonDrawPlanes.setTooltip(Component.literal(drawPlanes ? "Hide area" : "Show area"));
        });
        buttonDrawPlanes.setTooltip(Component.literal("Show area"));
        mirrorIconButtonList.add(buttonDrawPlanes);

        textMirrorPosX.setNumber(mirrorSettings.position().x);
        textMirrorPosY.setNumber(mirrorSettings.position().y);
        textMirrorPosZ.setNumber(mirrorSettings.position().z);
        textMirrorRadius.setNumber(mirrorSettings.radius());
        drawLines = mirrorSettings.drawLines();
        drawPlanes = mirrorSettings.drawPlanes();
        buttonDrawLines.setUseAlternateIcon(drawLines);
        buttonDrawPlanes.setUseAlternateIcon(drawPlanes);
        buttonDrawLines.setTooltip(Component.literal(drawLines ? "Hide lines" : "Show lines"));
        buttonDrawPlanes.setTooltip(Component.literal(drawPlanes ? "Hide area" : "Show area"));
        if (textMirrorPosX.getNumber() == Math.floor(textMirrorPosX.getNumber())) {
            toggleOdd = false;
            buttonToggleOdd.setTooltip(Arrays.asList(Component.literal("Set mirror position to middle of block"), Component.literal("for odd numbered builds")));
        } else {
            toggleOdd = true;
            buttonToggleOdd.setTooltip(Arrays.asList(Component.literal("Set mirror position to corner of block"), Component.literal("for even numbered builds")));
        }
        buttonToggleOdd.setUseAlternateIcon(toggleOdd);


        renderables.addAll(mirrorButtonList);
        renderables.addAll(mirrorIconButtonList);

        setCollapsed(!buttonMirrorEnabled.selected());
    }

    @Override
    public void drawEntry(GuiGraphics guiGraphics, int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                          boolean isSelected, float partialTicks) {

        int offset = 8;

        buttonMirrorEnabled.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (buttonMirrorEnabled.selected()) {
            buttonMirrorEnabled.setY(y);
            guiGraphics.drawString(font, "Mirror enabled", left + offset, y + 8, 0xFFFFFF);

            var positionOffsetX0 = left + Dimen.SECTION_OFFSET_X0;
            var positionOffsetX1 = left + Dimen.SECTION_OFFSET_X1;
            var positionOffsetY0 = y + 10 + 24;
            var positionOffsetY1 = y + 10 + 24 * 2;
            var positionOffsetY2 = y + 10 + 24 * 3;

            var textOffsetX = 40;
            var componentOffsetY = -5;

            guiGraphics.drawString(font, "Position", positionOffsetX0, positionOffsetY0, 0xFFFFFF);
            guiGraphics.drawString(font, "X", positionOffsetX0 + textOffsetX, positionOffsetY0, 0xFFFFFF);
            guiGraphics.drawString(font, "Y", positionOffsetX0 + textOffsetX, positionOffsetY1, 0xFFFFFF);
            guiGraphics.drawString(font, "Z", positionOffsetX0 + textOffsetX, positionOffsetY2, 0xFFFFFF);
            textMirrorPosX.y = positionOffsetY0 + componentOffsetY;
            textMirrorPosY.y = positionOffsetY1 + componentOffsetY;
            textMirrorPosZ.y = positionOffsetY2 + componentOffsetY;


            guiGraphics.drawString(font, "Radius", positionOffsetX1, positionOffsetY0, 0xFFFFFF);
            textMirrorRadius.y = positionOffsetY0 + componentOffsetY;


            guiGraphics.drawString(font, "Axis", positionOffsetX1, positionOffsetY1, 0xFFFFFF);
            buttonMirrorX.setY(positionOffsetY1 - 7);
            buttonMirrorY.setY(positionOffsetY1 - 7);
            buttonMirrorZ.setY(positionOffsetY1 - 7);

            buttonCurrentPosition.setY(positionOffsetY2 - 6);
            buttonToggleOdd.setY(positionOffsetY2 - 6);
            buttonDrawLines.setY(positionOffsetY2 - 6);
            buttonDrawPlanes.setY(positionOffsetY2 - 6);

            mirrorButtonList.forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTicks));
            mirrorIconButtonList.forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTicks));
            mirrorNumberFieldList.forEach(numberField -> numberField.render(guiGraphics, mouseX, mouseY, partialTicks));
        } else {
            buttonMirrorEnabled.setY(y);
            guiGraphics.drawString(font, "Mirror disabled", left + offset, y + 8, 0x999999);
        }

    }

    public void drawTooltip(GuiGraphics guiGraphics, Screen guiScreen, int mouseX, int mouseY) {
        //Draw tooltips last
        if (buttonMirrorEnabled.selected()) {
            mirrorIconButtonList.forEach(iconButton -> iconButton.drawTooltip(guiGraphics, scrollPane.parent, mouseX, mouseY));
            mirrorNumberFieldList.forEach(numberField -> numberField.drawTooltip(guiGraphics, scrollPane.parent, mouseX, mouseY));
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        for (NumberField numberField : mirrorNumberFieldList) {
            numberField.charTyped(typedChar, keyCode);
        }
        return true;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        mirrorNumberFieldList.forEach(numberField -> numberField.mouseClicked(mouseX, mouseY, mouseEvent));

        boolean insideMirrorEnabledLabel = mouseX >= left && mouseX < right && relativeY >= 4 && relativeY < 16;

        if (insideMirrorEnabledLabel) {
            buttonMirrorEnabled.playDownSound(this.mc.getSoundManager());
            buttonMirrorEnabled.onClick(mouseX, mouseY);
        }

        return true;
    }

    public Mirror.MirrorSettings getMirrorSettings() {
        boolean mirrorEnabled = buttonMirrorEnabled.selected();

        var mirrorPos = new Vec3(0, 64, 0);
        try {
            mirrorPos = new Vec3(textMirrorPosX.getNumber(), textMirrorPosY.getNumber(), textMirrorPosZ.getNumber());
        } catch (NumberFormatException | NullPointerException ex) {
            Effortless.log(mc.player, "Mirror position not a valid number.");
        }

        boolean mirrorX = buttonMirrorX.selected();
        boolean mirrorY = buttonMirrorY.selected();
        boolean mirrorZ = buttonMirrorZ.selected();

        int mirrorRadius = 50;
        try {
            mirrorRadius = (int) textMirrorRadius.getNumber();
        } catch (NumberFormatException | NullPointerException ex) {
            Effortless.log(mc.player, "Mirror radius not a valid number.");
        }

        return new Mirror.MirrorSettings(mirrorEnabled, mirrorPos, mirrorX, mirrorY, mirrorZ, mirrorRadius, drawLines, drawPlanes);
    }

    @Override
    protected String getName() {
        return "Mirror";
    }

    @Override
    protected int getExpandedHeight() {
        return 106;
    }
}
