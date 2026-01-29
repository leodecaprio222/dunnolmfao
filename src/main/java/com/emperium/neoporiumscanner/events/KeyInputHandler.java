package com.emperium.neoporiumscanner.events;

import com.emperium.neoporiumscanner.config.AdvancedConfig;
import com.emperium.neoporiumscanner.core.AdvancedScanner;
import com.emperium.neoporiumscanner.gui.AdvancedGuiScreen;
import com.emperium.neoporiumscanner.gui.QuickAccessScreen;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    private static KeyBinding openGuiKey;
    private static KeyBinding quickScanKey;
    private static KeyBinding toggleXRayKey;
    private static KeyBinding cycleProfileKey;
    private static KeyBinding toggleAutoScanKey;
    private static KeyBinding quickAccessKey;

    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Open GUI (G key)
            if (openGuiKey.wasPressed()) {
                client.setScreen(new AdvancedGuiScreen());
            }

            // Quick scan (B key)
            if (quickScanKey.wasPressed()) {
                if (client.world != null && client.player != null) {
                    AdvancedScanner.scanCurrentChunk(client.world, client.player);
                    client.player.sendMessage(Text.literal("§6[Neoporium] Quick scan complete!"), false);
                }
            }

            // Toggle XRay (X key)
            if (toggleXRayKey.wasPressed()) {
                XRayRenderer.toggle();
                String status = XRayRenderer.isEnabled() ? "§aENABLED" : "§cDISABLED";
                client.player.sendMessage(Text.literal("§6[Neoporium] XRay: " + status), false);
            }

            // Cycle profiles (P key)
            if (cycleProfileKey.wasPressed()) {
                AdvancedScanner.cycleNextProfile();
                ScanProfile profile = AdvancedScanner.getCurrentProfile();
                if (profile != null) {
                    client.player.sendMessage(Text.literal("§6[Neoporium] Switched to profile: §e" + profile.name), false);
                }
            }

            // Toggle auto-scan (A key)
            if (toggleAutoScanKey.wasPressed()) {
                AdvancedConfig config = AdvancedConfig.get();
                config.autoScan = !config.autoScan;
                AdvancedConfig.save();
                String status = config.autoScan ? "§aENABLED" : "§cDISABLED";
                client.player.sendMessage(Text.literal("§6[Neoporium] Auto-scan: " + status), false);
            }

            // Quick access (F6 key)
            if (quickAccessKey.wasPressed()) {
                client.setScreen(new QuickAccessScreen());
            }
        });
    }

    public static void registerKeyBindings() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neoporiumscanner.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.neoporiumscanner.main"
        ));

        quickScanKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neoporiumscanner.quick_scan",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.neoporiumscanner.main"
        ));

        toggleXRayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neoporiumscanner.toggle_xray",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.neoporiumscanner.main"
        ));

        cycleProfileKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neoporiumscanner.cycle_profile",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.neoporiumscanner.main"
        ));

        toggleAutoScanKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neoporiumscanner.toggle_autoscan",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_A,
                "category.neoporiumscanner.main"
        ));

        quickAccessKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neoporiumscanner.quick_access",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                "category.neoporiumscanner.main"
        ));
    }

    public static KeyBinding getOpenGuiKey() {
        return openGuiKey;
    }

    public static KeyBinding getQuickScanKey() {
        return quickScanKey;
    }

    public static KeyBinding getToggleXRayKey() {
        return toggleXRayKey;
    }

    public static KeyBinding getCycleProfileKey() {
        return cycleProfileKey;
    }

    public static KeyBinding getToggleAutoScanKey() {
        return toggleAutoScanKey;
    }

    public static KeyBinding getQuickAccessKey() {
        return quickAccessKey;
    }
}