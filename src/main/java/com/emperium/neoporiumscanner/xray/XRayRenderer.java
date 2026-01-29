package com.emperium.neoporiumscanner.xray;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import com.emperium.neoporiumscanner.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;

public class XRayRenderer {
    private List<BlockPosWithColor> blocks = new ArrayList<>();
    private boolean needsUpdate = false;

    public void updateBlocks(List<BlockPosWithColor> newBlocks) {
        this.blocks = new ArrayList<>(newBlocks);
        this.needsUpdate = true;
    }

    public void render(MatrixStack matrices) {
        if (blocks.isEmpty() || !ConfigManager.isXRayEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        // Setup rendering
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, ConfigManager.getXRayOpacity());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (BlockPosWithColor block : blocks) {
            BlockPos pos = block.getPos();
            if (shouldSkipBlock(pos, cameraPos)) continue;

            Box box = new Box(pos);
            float[] color = block.getColor().getFloats();
            float alpha = calculateAlpha(pos, cameraPos);

            renderBlockFaces(buffer, box, color[0], color[1], color[2], alpha);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Reset render state
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderBlockFaces(BufferBuilder buffer, Box box, float r, float g, float b, float a) {
        // Bottom face
        addVertex(buffer, box.minX, box.minY, box.minZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.minY, box.minZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.minY, box.maxZ, r, g, b, a);
        addVertex(buffer, box.minX, box.minY, box.maxZ, r, g, b, a);

        // Top face
        addVertex(buffer, box.minX, box.maxY, box.minZ, r, g, b, a);
        addVertex(buffer, box.minX, box.maxY, box.maxZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.maxY, box.minZ, r, g, b, a);

        // North face
        addVertex(buffer, box.minX, box.minY, box.minZ, r, g, b, a);
        addVertex(buffer, box.minX, box.maxY, box.minZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.maxY, box.minZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.minY, box.minZ, r, g, b, a);

        // South face
        addVertex(buffer, box.minX, box.minY, box.maxZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.minY, box.maxZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        addVertex(buffer, box.minX, box.maxY, box.maxZ, r, g, b, a);

        // West face
        addVertex(buffer, box.minX, box.minY, box.minZ, r, g, b, a);
        addVertex(buffer, box.minX, box.minY, box.maxZ, r, g, b, a);
        addVertex(buffer, box.minX, box.maxY, box.maxZ, r, g, b, a);
        addVertex(buffer, box.minX, box.maxY, box.minZ, r, g, b, a);

        // East face
        addVertex(buffer, box.maxX, box.minY, box.minZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.maxY, box.minZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        addVertex(buffer, box.maxX, box.minY, box.maxZ, r, g, b, a);
    }

    private void addVertex(BufferBuilder buffer, double x, double y, double z, float r, float g, float b, float a) {
        buffer.vertex(x, y, z).color(r, g, b, a);
    }

    private boolean shouldSkipBlock(BlockPos pos, Vec3d cameraPos) {
        double distance = Math.sqrt(pos.getSquaredDistance(cameraPos.x, cameraPos.y, cameraPos.z));
        return distance > ConfigManager.getXRayDistance();
    }

    private float calculateAlpha(BlockPos pos, Vec3d cameraPos) {
        if (!ConfigManager.isXRaySeeThrough()) return 1.0f;
        double distance = Math.sqrt(pos.getSquaredDistance(cameraPos.x, cameraPos.y, cameraPos.z));
        double maxDist = ConfigManager.getXRayDistance();
        return (float) Math.max(0.3, 1.0 - (distance / (maxDist * 1.5)));
    }

    public void clear() {
        blocks.clear();
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }

    public void setUpdated() {
        needsUpdate = false;
    }
}