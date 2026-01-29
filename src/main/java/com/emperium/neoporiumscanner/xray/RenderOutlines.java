package com.emperium.neoporiumscanner.xray;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.util.*;

public class RenderOutlines {
    private static final List<BlockOutline> outlinesToRender = new ArrayList<>();
    private static boolean needsRefresh = false;

    public static class BlockOutline {
        public final BlockPos position;
        public final BasicColor color;
        public final long creationTime;
        public float alphaMultiplier = 1.0f;

        public BlockOutline(BlockPos position, BasicColor color) {
            this.position = position;
            this.color = color;
            this.creationTime = System.currentTimeMillis();
        }
    }

    public static void addOutline(BlockPos pos, BasicColor color) {
        outlinesToRender.add(new BlockOutline(pos, color));
        needsRefresh = true;
    }

    public static void addOutlines(List<BlockPos> positions, BasicColor color) {
        for (BlockPos pos : positions) {
            outlinesToRender.add(new BlockOutline(pos, color));
        }
        needsRefresh = true;
    }

    public static void clearOutlines() {
        outlinesToRender.clear();
        needsRefresh = true;
    }

    public static void removeOutline(BlockPos pos) {
        outlinesToRender.removeIf(outline -> outline.position.equals(pos));
        needsRefresh = true;
    }

    public static void updateOutlineColor(BlockPos pos, BasicColor newColor) {
        for (BlockOutline outline : outlinesToRender) {
            if (outline.position.equals(pos)) {
                // Can't modify final field, so we'd need to remove and re-add
                removeOutline(pos);
                addOutline(pos, newColor);
                break;
            }
        }
    }

    public static void render(MatrixStack matrices, Camera camera, float tickDelta) {
        if (outlinesToRender.isEmpty()) {
            return;
        }

        Vec3d cameraPos = camera.getPos();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Setup rendering
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        // Set line width
        RenderSystem.lineWidth(2.0f);

        long currentTime = System.currentTimeMillis();
        Iterator<BlockOutline> iterator = outlinesToRender.iterator();

        while (iterator.hasNext()) {
            BlockOutline outline = iterator.next();

            // Check if outline is too old (5 minute lifetime)
            if (currentTime - outline.creationTime > 300000) {
                iterator.remove();
                continue;
            }

            // Calculate distance from camera
            double distance = Math.sqrt(outline.position.getSquaredDistance(cameraPos.x, cameraPos.y, cameraPos.z));

            // Skip if too far
            if (distance > 64) {
                continue;
            }

            // Calculate alpha based on distance and time
            float alpha = outline.color.getAlphaFloat() * outline.alphaMultiplier;
            if (distance > 32) {
                alpha *= (float)(1.0 - (distance - 32) / 32.0);
            }

            // Pulse effect for new outlines
            long age = currentTime - outline.creationTime;
            if (age < 1000) {
                float pulse = (float)Math.sin(age / 100.0) * 0.3f + 0.7f;
                alpha *= pulse;
            }

            renderBlockOutline(buffer, matrix, outline.position, outline.color, alpha);
        }

        tessellator.draw();
        matrices.pop();

        // Restore rendering state
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        needsRefresh = false;
    }

    private static void renderBlockOutline(BufferBuilder buffer, Matrix4f matrix, BlockPos pos,
                                           BasicColor color, float alpha) {
        float x1 = pos.getX();
        float y1 = pos.getY();
        float z1 = pos.getZ();
        float x2 = x1 + 1.0f;
        float y2 = y1 + 1.0f;
        float z2 = z1 + 1.0f;

        float r = color.getRedFloat();
        float g = color.getGreenFloat();
        float b = color.getBlueFloat();

        // Bottom face
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, alpha).next();

        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, alpha).next();

        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, alpha).next();

        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha).next();

        // Top face
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, alpha).next();

        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha).next();

        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, alpha).next();

        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, alpha).next();

        // Vertical edges
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, alpha).next();

        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, alpha).next();

        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, alpha).next();

        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, alpha).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, alpha).next();
    }

    public static void setNeedsRefresh(boolean refresh) {
        needsRefresh = refresh;
    }

    public static boolean needsRefresh() {
        return needsRefresh;
    }

    public static int getOutlineCount() {
        return outlinesToRender.size();
    }

    public static void cleanupOldOutlines() {
        long currentTime = System.currentTimeMillis();
        outlinesToRender.removeIf(outline -> currentTime - outline.creationTime > 300000);
    }

    public static void fadeOutOutlines() {
        for (BlockOutline outline : outlinesToRender) {
            outline.alphaMultiplier *= 0.9f;
            if (outline.alphaMultiplier < 0.1f) {
                outline.alphaMultiplier = 0.1f;
            }
        }
    }

    public static void fadeInOutlines() {
        for (BlockOutline outline : outlinesToRender) {
            outline.alphaMultiplier = Math.min(1.0f, outline.alphaMultiplier * 1.1f);
        }
    }
}