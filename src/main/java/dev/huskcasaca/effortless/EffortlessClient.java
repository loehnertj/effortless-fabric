package dev.huskcasaca.effortless;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.huskcasaca.effortless.building.BuildAction;
import dev.huskcasaca.effortless.building.BuildActionHandler;
import dev.huskcasaca.effortless.building.BuildHandler;
import dev.huskcasaca.effortless.buildmode.BuildModeHelper;
import dev.huskcasaca.effortless.buildmodifier.BuildModifierHelper;
import dev.huskcasaca.effortless.control.Keys;
import dev.huskcasaca.effortless.event.ClientReloadShadersEvent;
import dev.huskcasaca.effortless.event.ClientScreenEvent;
import dev.huskcasaca.effortless.event.ClientScreenInputEvent;
import dev.huskcasaca.effortless.building.ReachHelper;
import dev.huskcasaca.effortless.network.Packets;
import dev.huskcasaca.effortless.network.protocol.player.*;
import dev.huskcasaca.effortless.render.BlockPreviewRenderer;
import dev.huskcasaca.effortless.render.BuildRenderType;
import dev.huskcasaca.effortless.render.ModifierRenderer;
import dev.huskcasaca.effortless.render.StructureHudRenderer;
import dev.huskcasaca.effortless.screen.buildmode.RadialMenuScreen;
import dev.huskcasaca.effortless.screen.buildmodifier.ModifierSettingsScreen;
import dev.huskcasaca.effortless.screen.config.EffortlessConfigScreen;
import dev.huskcasaca.effortless.utils.InventoryHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class EffortlessClient implements ClientModInitializer {

    public static KeyMapping[] keyBindings;
    public static HitResult previousLookAt;
    public static HitResult currentLookAt;
    private static int ticksInGame = 0;

    public static void onStartClientTick(Minecraft client) {
        //Update previousLookAt
        HitResult objectMouseOver = Minecraft.getInstance().hitResult;
        //Checking for null is necessary! Even in vanilla when looking down ladders it is occasionally null (instead of Type MISS)
        if (objectMouseOver == null) return;

        if (currentLookAt == null) {
            currentLookAt = objectMouseOver;
            previousLookAt = objectMouseOver;
            return;
        }

        if (objectMouseOver.getType() == HitResult.Type.BLOCK) {
            if (currentLookAt.getType() != HitResult.Type.BLOCK) {
                currentLookAt = objectMouseOver;
                previousLookAt = objectMouseOver;
            } else {
                if (((BlockHitResult) currentLookAt).getBlockPos() != ((BlockHitResult) objectMouseOver).getBlockPos()) {
                    previousLookAt = currentLookAt;
                    currentLookAt = objectMouseOver;
                }
            }
        }

    }

    public static void onEndClientTick(Minecraft client) {
        Screen gui = Minecraft.getInstance().screen;
        if (gui == null || !gui.isPauseScreen()) {
            ticksInGame++;
        }
    }


    //    @SubscribeEvent(receiveCanceled = true)
    public static void onKeyPress(int key, int scanCode, int action, int modifiers) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
//
//        //Remember to send packet to server if necessary
//        //Show Modifier Settings GUI
        if (Keys.MODIFIER_MENU.getKeyMapping().consumeClick()) {
            openModifierSettings();
        }
//
//        //QuickReplace toggle
        if (Keys.TOGGLE_REPLACE.getKeyMapping().consumeClick()) {
            BuildActionHandler.performAction(player, BuildAction.REPLACE);
            Packets.sendToServer(new ServerboundPlayerBuildActionPacket(BuildAction.REPLACE));
        }
        if (Keys.CYCLE_MIRROR.getKeyMapping().consumeClick()) {
            BuildActionHandler.performAction(player, BuildAction.CYCLE_MIRROR);
            Packets.sendToServer(new ServerboundPlayerBuildActionPacket(BuildAction.CYCLE_MIRROR));
        }

        //Radial menu
        if (Keys.SHOW_RADIAL_MENU.isDown()) {
            if (!RadialMenuScreen.getInstance().isVisible()) {
                Minecraft.getInstance().setScreen(RadialMenuScreen.getInstance());
            }
//            if (ReachHelper.getMaxReachDistance(player) > 0) {
//            } else {
//                Effortless.log(player, "Build modes are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
//            }
        }
//
//        //Undo (Ctrl+Z)
//        if (keyBindings[3].consumeClick()) {
//            BuildAction undoAction = BuildAction.UNDO;
//            BuildActionHandler.performAction(player, undoAction);
//            Packets.sendToServer(new ModeActionMessage(undoAction));
//        }
//
//        //Redo (Ctrl+Y)
//        if (keyBindings[4].consumeClick()) {
//            BuildAction redoAction = BuildAction.REDO;
//            BuildActionHandler.performAction(player, redoAction);
//            Packets.sendToServer(new ModeActionMessage(redoAction));
//        }
//
//        //Change placement mode
//        if (keyBindings[5].consumeClick()) {
//            //Toggle between first two actions of the first option of the current build mode
//            BuildMode currentBuildMode = ModeSettingsManager.getModeSettings(player).buildMode();
//            if (currentBuildMode.options.length > 0) {
//                BuildOption option = currentBuildMode.options[0];
//                if (option.actions.length >= 2) {
//                    if (BuildActionHandler.getOptionSetting(option) == option.actions[0]) {
//                        BuildActionHandler.performAction(player, option.actions[1]);
//                        Packets.sendToServer(new ModeActionMessage(option.actions[1]));
//                    } else {
//                        BuildActionHandler.performAction(player, option.actions[0]);
//                        Packets.sendToServer(new ModeActionMessage(option.actions[0]));
//                    }
//                }
//            }
//        }

    }

    public static void openModifierSettings() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) return;

        //Disabled if max reach is 0, might be set in the config that way.
        if (ReachHelper.getMaxReachDistance(player) == 0) {
            Effortless.log(player, "Build modifiers are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
        } else {

            mc.setScreen(null);
            mc.setScreen(new ModifierSettingsScreen());
        }
    }

    public static void openSettings() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(EffortlessConfigScreen.createConfigScreen(mc.screen));

    }

    public static void onScreenEvent(Screen screen) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            var modeSettings = BuildModeHelper.getModeSettings(player);
            BuildModeHelper.setModeSettings(player, modeSettings);
            Packets.sendToServer(new ServerboundPlayerSetBuildModePacket(modeSettings));
        }
    }

    protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
        float f = player.getXRot();
        float g = player.getYRot();
        var vec3 = player.getEyePosition();
        float h = Mth.cos(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float i = Mth.sin(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float j = -Mth.cos(-f * ((float) Math.PI / 180));
        float k = Mth.sin(-f * ((float) Math.PI / 180));
        float l = i * j;
        float m = k;
        float n = h * j;
        double d = 5.0;
        var vec32 = vec3.add((double) l * 5.0, (double) m * 5.0, (double) n * 5.0);
        return level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluid, player));
    }

    public static HitResult getLookingAt(Player player) {
        var level = player.level();

        //base distance off of player ability (config)
        float raytraceRange = ReachHelper.getPlacementReach(player) * 4;

        var look = player.getLookAngle();
        var start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        var end = new Vec3(player.getX() + look.x * raytraceRange, player.getY() + player.getEyeHeight() + look.y * raytraceRange, player.getZ() + look.z * raytraceRange);
        var holdingBucket = InventoryHelper.holdingBucket(player, true);
//        return player.rayTrace(raytraceRange, 1f, RayTraceFluidMode.NEVER);
        //TODO 1.14 check if correct
        return level.clip(
            new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                holdingBucket ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE,
                player
            )
        );
    }

    public static void registerShaders(ResourceProvider resourceProvider, ClientReloadShadersEvent.ShaderRegister.ShadersSink sink) throws IOException {
        sink.registerShader(
                // TODO: 10/9/22 use custom namespace
                new ShaderInstance(resourceProvider, "dissolve", DefaultVertexFormat.BLOCK),
                (shaderInstance) -> BuildRenderType.setDissolveShaderInstance(shaderInstance)
        );
    }

    public static int getTicksInGame() {
        return ticksInGame;
    }

    @Override
    public void onInitializeClient() {
        // register key bindings
        Keys.register();

        // Register network packet handlers
        ClientPlayNetworking.registerGlobalReceiver(
            ClientboundPlayerBuildModePacket.TYPE, EffortlessClient::handlePacket
        );
        ClientPlayNetworking.registerGlobalReceiver(
            ClientboundPlayerBuildModifierPacket.TYPE, EffortlessClient::handlePacket
        );
        ClientPlayNetworking.registerGlobalReceiver(
                ClientboundPlayerReachPacket.TYPE, EffortlessClient::handlePacket
        );

        ClientScreenEvent.SCREEN_OPENING_EVENT.register(EffortlessClient::onScreenEvent);

        ClientScreenInputEvent.KEY_PRESS_EVENT.register(EffortlessClient::onKeyPress);

        ClientTickEvents.START_CLIENT_TICK.register(EffortlessClient::onStartClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(EffortlessClient::onEndClientTick);

        ClientReloadShadersEvent.REGISTER_SHADER.register(EffortlessClient::registerShaders);

        WorldRenderEvents.AFTER_ENTITIES.register((context) -> renderBlockPreview(context.matrixStack(), context.camera()));
        WorldRenderEvents.LAST.register((context) -> renderModifierSettings(context.matrixStack(), context.camera()));
        HudRenderCallback.EVENT.register((context, tickDelta) -> renderStructuresHud(context));

    }

    public static void renderBlockPreview(PoseStack poseStack, Camera camera) {
        var bufferBuilder = Tesselator.getInstance().getBuilder();
        var bufferSource = MultiBufferSource.immediate(bufferBuilder);
        var player = Minecraft.getInstance().player;

        BlockPreviewRenderer.getInstance().render(player, poseStack, bufferSource, camera);
    }

    public static void renderModifierSettings(PoseStack poseStack, Camera camera) {
        var bufferBuilder = Tesselator.getInstance().getBuilder();
        var bufferSource = MultiBufferSource.immediate(bufferBuilder);
        var player = Minecraft.getInstance().player;

        ModifierRenderer.getInstance().render(player, poseStack, bufferSource, camera);
    }
    public static void renderStructuresHud(GuiGraphics guiGraphics) {
        var player = Minecraft.getInstance().player;
        StructureHudRenderer.render(player, guiGraphics);
    }
    public static void handlePacket(
            ClientboundPlayerBuildModePacket packet, LocalPlayer player, PacketSender sender
    ) {
        BuildModeHelper.setModeSettings(player, BuildModeHelper.sanitize(packet.modeSettings(), player));
    }

    public static void handlePacket(
            ClientboundPlayerBuildModifierPacket packet, LocalPlayer player, PacketSender sender
    ) {
        BuildModifierHelper.setModifierSettings(player, BuildModifierHelper.sanitize(packet.modifierSettings(), player));
    }

    public static void handlePacket(
            ClientboundPlayerReachPacket packet, LocalPlayer player, PacketSender sender
    ) {

        ReachHelper.setReachSettings(player, ReachHelper.sanitize(packet.reachSettings(), player));
        BuildHandler.initialize(player);
    }
}
