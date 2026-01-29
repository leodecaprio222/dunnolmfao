package com.emperium.neoporiumscanner.events;

import com.emperium.neoporiumscanner.config.AdvancedConfig;
import com.emperium.neoporiumscanner.core.AdvancedScanner;
import com.emperium.neoporiumscanner.core.ScanController;
import com.emperium.neoporiumscanner.core.ScanProfile;
import com.emperium.neoporiumscanner.gui.QuickAccessScreen;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;

public class ClientTickHandler {
    private static long lastAutoScanTime = 0;
    private static int tickCounter = 0;
    private static ChunkPos lastPlayerChunk = null;

    public static void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        PlayerEntity player = client.player;
        tickCounter++;

        // Handle auto-scan every second
        if (tickCounter % 20 == 0) {
            handleAutoScan(client);
            tickCounter = 0;
        }

        // Handle quick access overlay key (F6)
        if (client.options.sneakKey.isPressed() && client.options.jumpKey.wasPressed()) {
            client.setScreen(new QuickAccessScreen());
        }

        // Update XRay if needed
        if (XRayRenderer.isEnabled()) {
            XRayRenderer.updateForCurrentChunk(client.world, player.getBlockPos());
        }

        // Check if player moved to new chunk for auto-scan
        ChunkPos currentChunk = player.getChunkPos();
        if (lastPlayerChunk == null || !lastPlayerChunk.equals(currentChunk)) {
            lastPlayerChunk = currentChunk;
            // Trigger auto-scan if enabled and conditions met
            if (AdvancedConfig.get().autoScan) {
                AdvancedScanner.scanCurrentChunk(client.world, player);
            }
        }
    }

    private static void handleAutoScan(MinecraftClient client) {
        AdvancedConfig config = AdvancedConfig.get();
        if (!config.autoScan) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAutoScanTime >= config.autoScanInterval * 1000L) {
            if (client.player != null && client.world != null) {
                AdvancedScanner.scanCurrentChunk(client.world, client.player);
                lastAutoScanTime = currentTime;
            }
        }
    }

    public static void resetAutoScanTimer() {
        lastAutoScanTime = System.currentTimeMillis();
    }
}