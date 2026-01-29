package com.emperium.neoporiumscanner.events;

import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.core.ScanController;
import com.emperium.neoporiumscanner.gui.XRayConfigScreen;
import net.minecraft.client.MinecraftClient;

public class ClientTickHandler {
    public static void onTick(MinecraftClient client) {
        if (client.player == null) return;

        // Handle key presses
        if (KeyInputHandler.configKey.wasPressed()) {
            // FIXED: Pass current screen as parent
            client.setScreen(new XRayConfigScreen(client.currentScreen));
        }

        if (KeyInputHandler.toggleKey.wasPressed()) {
            StateSettings.toggleXray();

            if (StateSettings.isXrayEnabled()) {
                ScanController.getInstance().startScan();
            } else {
                ScanController.getInstance().stopScan();
            }
        }

        // Update scanning if active
        if (StateSettings.isScanningActive() && StateSettings.isXrayEnabled()) {
            ScanController.getInstance().startScan();
        }
    }
}