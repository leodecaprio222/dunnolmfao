package com.emperium.neoporiumscanner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import com.emperium.neoporiumscanner.commands.CommandRegistry;
import com.emperium.neoporiumscanner.config.ConfigManager;
import com.emperium.neoporiumscanner.events.WorldRenderHandler;
import com.emperium.neoporiumscanner.gui.AdvancedGuiScreen;

public class NeoporiumScanner implements ClientModInitializer {
    public static final String MOD_ID = "neoporium-scanner";
    public static final String MOD_NAME = "Neoporium Scanner";
    public static final String MOD_VERSION = "1.0.0";

    private static KeyBinding openGuiKeyBinding;

    @Override
    public void onInitializeClient() {
        System.out.println("[" + MOD_NAME + "] Initializing...");

        // Load configuration
        ConfigManager.load();

        // Register commands
        CommandRegistry.register();

        // Register key binding
        openGuiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neoporiumscanner.opengui",
                GLFW.GLFW_KEY_H,
                "category.neoporiumscanner.main"
        ));

        // Register render handler
        WorldRenderHandler.register();

        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKeyBinding.wasPressed()) {
                openGui();
            }
        });

        System.out.println("[" + MOD_NAME + "] Initialized successfully!");
    }

    public static void openGui() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.world != null) {
            client.setScreen(new AdvancedGuiScreen(client.currentScreen));
        }
    }

    public static KeyBinding getOpenGuiKeyBinding() {
        return openGuiKeyBinding;
    }
}