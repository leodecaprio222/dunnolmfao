package com.emperium.neoporiumscanner.events;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import com.emperium.neoporiumscanner.config.ConfigManager;
import com.emperium.neoporiumscanner.xray.render.RenderManager;

public class WorldRenderHandler {

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) {
                return;
            }

            // Only render if XRay or ESP is enabled
            if (ConfigManager.isXRayEnabled() || ConfigManager.isESPEnabled()) {
                MatrixStack matrices = context.matrixStack();
                matrices.push();

                // Apply camera transformation
                RenderManager.render(matrices);

                matrices.pop();
            }
        });
    }
}