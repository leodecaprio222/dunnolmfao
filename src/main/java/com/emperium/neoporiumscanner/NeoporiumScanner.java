package com.emperium.neoporiumscanner;

import com.emperium.neoporiumscanner.config.ConfigManager;
import com.emperium.neoporiumscanner.events.ClientTickHandler;
import com.emperium.neoporiumscanner.events.KeyInputHandler;
import com.emperium.neoporiumscanner.gui.XRayConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeoporiumScanner implements ClientModInitializer {
    public static final String MOD_ID = "neoporium-scanner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding configKey;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Neoporium Scanner");

        // Initialize configuration
        ConfigManager.initialize();

        // Register keybindings
        registerKeybindings();

        // Register event handlers
        registerEvents();

        LOGGER.info("Neoporium Scanner initialized successfully");
    }

    private void registerKeybindings() {
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neoporiumscanner.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.neoporiumscanner.main"
        ));

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neoporiumscanner.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.neoporiumscanner.main"
        ));

        KeyInputHandler.initialize(configKey, toggleKey);
    }

    private void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::onTick);
    }

    public static KeyBinding getConfigKey() {
        return configKey;
    }

    public static KeyBinding getToggleKey() {
        return toggleKey;
    }
}