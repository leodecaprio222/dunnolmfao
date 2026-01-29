package com.emperium.neoporiumscanner.events;

import com.emperium.neoporiumscanner.xray.RenderOutlines;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class WorldRenderHandler implements WorldRenderEvents.AfterEntities {

    @Override
    public void afterEntities(WorldRenderContext context) {
        // Render XRay outlines
        if (XRayRenderer.isEnabled()) {
            XRayRenderer.render(
                    context.matrixStack(),
                    context.consumers(),
                    context.world(),
                    context.camera()
            );
        }

        // Render additional outlines
        RenderOutlines.render(
                context.matrixStack(),
                context.camera(),
                context.tickDelta()
        );
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(new WorldRenderHandler());
    }
}