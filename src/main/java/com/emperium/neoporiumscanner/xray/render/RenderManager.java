package com.emperium.neoporiumscanner.xray.render;

import com.emperium.neoporiumscanner.xray.BlockPosWithColor;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import java.util.List;

public class RenderManager {
    private static final XRayRenderer XRAY_RENDERER = new XRayRenderer();
    private static final ESPRenderer ESP_RENDERER = new ESPRenderer();

    public static void render(MatrixStack matrices) {
        // Render XRay if enabled
        XRAY_RENDERER.render(matrices);

        // Render ESP if enabled
        ESP_RENDERER.render(matrices);
    }

    public static void updateBlocks(List<BlockPosWithColor> blocks) {
        XRAY_RENDERER.updateBlocks(blocks);
        ESP_RENDERER.updateBlocks(blocks);
    }

    public static void clear() {
        XRAY_RENDERER.clear();
        ESP_RENDERER.clear();
    }

    public static void onWorldUnload() {
        clear();
    }

    public static boolean needsUpdate() {
        return XRAY_RENDERER.needsUpdate();
    }

    public static void markUpdated() {
        XRAY_RENDERER.setUpdated();
    }
}