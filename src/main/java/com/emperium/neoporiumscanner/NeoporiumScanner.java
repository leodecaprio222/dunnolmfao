package com.emperium.neoporiumscanner;

import com.emperium.neoporiumscanner.config.AdvancedConfig;
import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.core.AdvancedScanner;
import com.emperium.neoporiumscanner.core.LogManager;
import com.emperium.neoporiumscanner.events.*;
import com.emperium.neoporiumscanner.gui.AdvancedGuiScreen;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class NeoporiumScanner implements ClientModInitializer {
    public static final String MOD_ID = "neoporiumscanner";
    public static final String MOD_NAME = "Neoporium Scanner";
    public static final String VERSION = "1.0.0";

    private static KeyBinding openGuiKey;
    private static KeyBinding quickScanKey;
    private static KeyBinding toggleXRayKey;
    private static KeyBinding cycleProfileKey;
    private static KeyBinding toggleAutoScanKey;

    @Override
    public void onInitializeClient() {
        System.out.println("[NeoporiumScanner] Initializing " + MOD_NAME + " v" + VERSION);

        // Initialize core systems
        AdvancedConfig.init();
        StateSettings.getInstance();
        AdvancedScanner.init();
        LogManager.getInstance();

        // Initialize XRay system
        XRayRenderer.init();

        // Register key bindings
        KeyInputHandler.registerKeyBindings();

        // Register event handlers
        registerEventHandlers();

        // Register key input handler
        KeyInputHandler.registerKeyInputs();

        System.out.println("[NeoporiumScanner] Initialization complete!");
    }

    private void registerEventHandlers() {
        // Client tick handler for key presses
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::onClientTick);

        // World render handler for XRay
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (XRayRenderer.isEnabled() && context.world() != null && context.camera() != null) {
                XRayRenderer.render(context.matrixStack(), context.consumers(), context.world(), context.camera());
            }
        });
    }
}