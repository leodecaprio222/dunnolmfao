package com.emperium.neoporiumscanner.xray.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import com.emperium.neoporiumscanner.config.ConfigManager;
import com.emperium.neoporiumscanner.xray.BlockPosWithColor;
import java.util.ArrayList;
import java.util.List;

public class ESPRenderer {
    private List<BlockPosWithColor> blocks = new ArrayList<>();

    public void updateBlocks(List<BlockPosWithColor> newBlocks) {
        this.blocks = new ArrayList<>(newBlocks);
    }

    public void render(MatrixStack matrices) {
        if (blocks.isEmpty() || !ConfigManager.isESPEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        String mode = ConfigManager.getESPMode();

        // Setup rendering
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(ConfigManager.getESPThickness());

        if (mode.equals("BOX") || mode.equals("BOTH")) {
            renderBoxes(matrices, cameraPos);
        }

        if (mode.equals("WIREFRAME") || mode.equals("BOTH")) {
            renderWireframes(matrices, cameraPos);
        }

        // Reset render state
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void renderBoxes(MatrixStack matrices, Vec3d cameraPos) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (BlockPosWithColor block : blocks) {
            BlockPos pos = block.getPos();
            if (shouldSkipBlock(pos, cameraPos)) continue;

            Box box = new Box(pos).expand(0.002); // Slight expansion for visibility
            float[] color = block.getColor().getFloats();
            float alpha = calculateAlpha(pos, cameraPos);

            renderBoxEdges(buffer, box, color[0], color[1], color[2], alpha);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void renderWireframes(MatrixStack matrices, Vec3d cameraPos) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (BlockPosWithColor block : blocks) {
            BlockPos pos = block.getPos();
            if (shouldSkipBlock(pos, cameraPos)) continue;

            Box box = new Box(pos);
            float[] color = block.getColor().getFloats();
            float alpha = calculateAlpha(pos, cameraPos);

            renderWireframe(buffer, box, color[0], color[1], color[2], alpha * 0.8f);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void renderBoxEdges(BufferBuilder buffer, Box box, float r, float g, float b, float a) {
        // Bottom edges
        vertex(buffer, box.minX, box.minY, box.minZ, r, g, b, a);
        vertex(buffer, box.maxX, box.minY, box.minZ, r, g, b, a);

        vertex(buffer, box.maxX, box.minY, box.minZ, r, g, b, a);
        vertex(buffer, box.maxX, box.minY, box.maxZ, r, g, b, a);

        vertex(buffer, box.maxX, box.minY, box.maxZ, r, g, b, a);
        vertex(buffer, box.minX, box.minY, box.maxZ, r, g, b, a);

        vertex(buffer, box.minX, box.minY, box.maxZ, r, g, b, a);
        vertex(buffer, box.minX, box.minY, box.minZ, r, g, b, a);

        // Top edges
        vertex(buffer, box.minX, box.maxY, box.minZ, r, g, b, a);
        vertex(buffer, box.maxX, box.maxY, box.minZ, r, g, b, a);

        vertex(buffer, box.maxX, box.maxY, box.minZ, r, g, b, a);
        vertex(buffer, box.maxX, box.maxY, box.maxZ, r, g, b, a);

        vertex(buffer, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        vertex(buffer, box.minX, box.maxY, box.maxZ, r, g, b, a);

        vertex(buffer, box.minX, box.maxY, box.maxZ, r, g, b, a);
        vertex(buffer, box.minX, box.maxY, box.minZ, r, g, b, a);

        // Vertical edges
        vertex(buffer, box.minX, box.minY, box.minZ, r, g, b, a);
        vertex(buffer, box.minX, box.maxY, box.minZ, r, g, b, a);

        vertex(buffer, box.maxX, box.minY, box.minZ, r, g, b, a);
        vertex(buffer, box.maxX, box.maxY, box.minZ, r, g, b, a);

        vertex(buffer, box.maxX, box.minY, box.maxZ, r, g, b, a);
        vertex(buffer, box.maxX, box.maxY, box.maxZ, r, g, b, a);

        vertex(buffer, box.minX, box.minY, box.maxZ, r, g, b, a);
        vertex(buffer, box.minX, box.maxY, box.maxZ, r, g, b, a);
    }

    private void renderWireframe(BufferBuilder buffer, Box box, float r, float g, float b, float a) {
        // Center cross for wireframe mode
        double centerX = (box.minX + box.maxX) / 2;
        double centerY = (box.minY + box.maxY) / 2;
        double centerZ = (box.minZ + box.maxZ) / 2;

        // X-axis line
        vertex(buffer, box.minX, centerY, centerZ, r, g, b, a);
        vertex(buffer, box.maxX, centerY, centerZ, r, g, b, a);

        // Y-axis line
        vertex(buffer, centerX, box.minY, centerZ, r, g, b, a);
        vertex(buffer, centerX, box.maxY, centerZ, r, g, b, a);

        // Z-axis line
        vertex(buffer, centerX, centerY, box.minZ, r, g, b, a);
        vertex(buffer, centerX, centerY, box.maxZ, r, g, b, a);

        // Corner to corner lines
        vertex(buffer, box.minX, box.minY, box.minZ, r, g, b, a * 0.5f);
        vertex(buffer, box.maxX, box.maxY, box.maxZ, r, g, b, a * 0.5f);

        vertex(buffer, box.maxX, box.minY, box.minZ, r, g, b, a * 0.5f);
        vertex(buffer, box.minX, box.maxY, box.maxZ, r, g, b, a * 0.5f);

        vertex(buffer, box.minX, box.minY, box.maxZ, r, g, b, a * 0.5f);
        vertex(buffer, box.maxX, box.maxY, box.minZ, r, g, b, a * 0.5f);

        vertex(buffer, box.maxX, box.minY, box.maxZ, r, g, b, a * 0.5f);
        vertex(buffer, box.minX, box.maxY, box.minZ, r, g, b, a * 0.5f);
    }

    private void vertex(BufferBuilder buffer, double x, double y, double z, float r, float g, float b, float a) {
        buffer.vertex(x, y, z).color(r, g, b, a);
    }

    private boolean shouldSkipBlock(BlockPos pos, Vec3d cameraPos) {
        double distance = Math.sqrt(pos.getSquaredDistance(cameraPos.x, cameraPos.y, cameraPos.z));
        return distance > ConfigManager.getESPDistance();
    }

    private float calculateAlpha(BlockPos pos, Vec3d cameraPos) {
        if (!ConfigManager.isESPFadeEnabled()) return 1.0f;
        double distance = Math.sqrt(pos.getSquaredDistance(cameraPos.x, cameraPos.y, cameraPos.z));
        double maxDist = ConfigManager.getESPDistance();
        return (float) Math.max(0.2, 1.0 - (distance / (maxDist * 1.2)));
    }

    public void clear() {
        blocks.clear();
    }
}