package com.emperium.neoporiumscanner.xray;

import com.emperium.neoporiumscanner.config.StateSettings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class XRayRenderer {
    private static final Queue<BlockPosWithColor> blocksToRender = new ConcurrentLinkedQueue<>();
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void render(MatrixStack matrices, Camera camera) {
        if (!StateSettings.isXrayEnabled() || blocksToRender.isEmpty()) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // FIXED: Minecraft 1.21.4 doesn't use setShader() anymore
        // Line rendering now uses a different approach
        RenderSystem.lineWidth(2.0f);  // Set line thickness

        Tessellator tessellator = Tessellator.getInstance();

        // FIXED: Use LINE_STRIP for better line rendering in 1.21.4
        BufferBuilder buffer = tessellator.begin(
                VertexFormat.DrawMode.DEBUG_LINE_STRIP,
                VertexFormats.LINES
        );

        Vec3d cameraPos = camera.getPos();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Group blocks by color for better rendering performance
        for (BlockPosWithColor block : blocksToRender) {
            if (block != null) {
                renderBlockOutline(buffer, matrix, block, cameraPos);
            }
        }

        // Draw all the lines
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void renderBlockOutline(BufferBuilder buffer, Matrix4f matrix, BlockPosWithColor block, Vec3d cameraPos) {
        BlockPos pos = block.pos();
        BasicColor color = block.color();

        double x = pos.getX() - cameraPos.x;
        double y = pos.getY() - cameraPos.y;
        double z = pos.getZ() - cameraPos.z;

        Box box = new Box(x, y, z, x + 1, y + 1, z + 1);

        // Convert color to float values (0-1 range)
        float r = color.r() / 255.0f;
        float g = color.g() / 255.0f;
        float b = color.b() / 255.0f;
        float a = color.a() / 255.0f;

        // Draw cube outline - optimized for DEBUG_LINE_STRIP
        // Bottom square
        drawColoredVertex(buffer, matrix, (float)box.minX, (float)box.minY, (float)box.minZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.minX, (float)box.minY, (float)box.minZ, r, g, b, a);

        // Top square
        drawColoredVertex(buffer, matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, r, g, b, a);

        // Vertical connections
        drawColoredVertex(buffer, matrix, (float)box.minX, (float)box.minY, (float)box.minZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, r, g, b, a);

        drawColoredVertex(buffer, matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ, r, g, b, a);

        drawColoredVertex(buffer, matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ, r, g, b, a);

        drawColoredVertex(buffer, matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, r, g, b, a);
        drawColoredVertex(buffer, matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ, r, g, b, a);
    }

    private static void drawColoredVertex(BufferBuilder buffer, Matrix4f matrix,
                                          float x, float y, float z,
                                          float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .normal(0, 1, 0);  // Simple normal
    }

    public static void addBlock(BlockPos pos, BasicColor color) {
        blocksToRender.add(new BlockPosWithColor(pos, color));
    }

    public static void clearBlocks() {
        blocksToRender.clear();
    }

    public static int getBlockCount() {
        return blocksToRender.size();
    }
}