package com.emperium.neoporiumscanner.events;

import com.emperium.neoporiumscanner.AdvancedGuiScreen;
import com.emperium.neoporiumscanner.xray.render.BlockDetector;
import com.emperium.neoporiumscanner.xray.render.RenderManager;
import net.minecraft.client.MinecraftClient;

public class ClientTickHandler {
    public static void onTick(MinecraftClient client) {
        if (client.player == null) return;

        // G key - Open GUI
        if (KeyInputHandler.configKey != null && KeyInputHandler.configKey.wasPressed()) {
            client.setScreen(new AdvancedGuiScreen());
        }

        // B key - Start scan
        if (KeyInputHandler.scanKey != null && KeyInputHandler.scanKey.wasPressed()) {
            if (client.world != null) {
                AdvancedScanner.scanArea(client.world, client.player);
                // Update renderers after scan
                BlockDetector.updateFromAdvancedScanner();
            }
        }

        // H key - Toggle ESP
        if (KeyInputHandler.espKey != null && KeyInputHandler.espKey.wasPressed()) {
            RenderManager.toggle();
            if (client.player != null) {
                String status = RenderManager.isEnabled() ? "§aENABLED" : "§cDISABLED";
                client.player.sendMessage(net.minecraft.text.Text.literal("§6[Neoporium] ESP: " + status), false);
            }
        }
    }
}