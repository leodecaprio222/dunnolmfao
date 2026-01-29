package com.emperium.neoporiumscanner.events;

import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class WorldRenderHandler {
    public static void onRender(WorldRenderContext context) {
        XRayRenderer.render(context.matrixStack(), context.camera());
    }

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(WorldRenderHandler::onRender);
    }
}