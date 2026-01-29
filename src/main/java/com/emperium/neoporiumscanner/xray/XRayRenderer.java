package com.emperium.neoporiumscanner.xray;

import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.core.ScanProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import java.util.*;

public class XRayRenderer {
    private static final Map<String, Set<BlockPos>> blocksToRender = new HashMap<>();
    private static boolean enabled = false;
    private static String renderMode = "wireframe";
    private static boolean throughWalls = true;
    private static boolean fadeWithDistance = true;
    private static boolean showDistance = false;
    private static boolean useLOD = true;
    private static float alpha = 0.7f;
    private static float lineWidth = 2.0f;
    private static int maxDistance = 64;
    private static int lodDistance = 32;

    private static final Map<String, BasicColor> blockColors = new HashMap<>();

    public static void init() {
        // Initialize with default colors
        blockColors.put("minecraft:diamond_ore", new BasicColor(0, 200, 255, 200));
        blockColors.put("minecraft:deepslate_diamond_ore", new BasicColor(0, 150, 255, 200));
        blockColors.put("minecraft:iron_ore", new BasicColor(200, 200, 200, 200));
        blockColors.put("minecraft:gold_ore", new BasicColor(255, 215, 0, 200));
        blockColors.put("minecraft:copper_ore", new BasicColor(184, 115, 51, 200));
        blockColors.put("minecraft:coal_ore", new BasicColor(50, 50, 50, 200));
        blockColors.put("minecraft:emerald_ore", new BasicColor(0, 200, 0, 200));
        blockColors.put("minecraft:redstone_ore", new BasicColor(255, 0, 0, 200));
        blockColors.put("minecraft:lapis_ore", new BasicColor(0, 100, 200, 200));
        blockColors.put("minecraft:ancient_debris", new BasicColor(100, 50, 50, 200));
        blockColors.put("minecraft:chest", new BasicColor(200, 150, 0, 200));
        blockColors.put("minecraft:ender_chest", new BasicColor(100, 0, 150, 200));
        blockColors.put("minecraft:spawner", new BasicColor(150, 0, 150, 200));

        System.out.println("[Neoporium] XRayRenderer initialized");
    }

    public static void updateForCurrentChunk(World world, BlockPos playerPos) {
        if (!enabled) return;

        // Clear previous blocks
        blocksToRender.clear();

        // Get current chunk coordinates
        int chunkX = playerPos.getX() >> 4;
        int chunkZ = playerPos.getZ() >> 4;

        // This would normally come from AdvancedScanner
        // For now, we'll use a test pattern
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            createTestPattern(playerPos, chunkX, chunkZ);
        }
    }

    private static void createTestPattern(BlockPos playerPos, int chunkX, int chunkZ) {
        // Create a test pattern of blocks around the player
        Set<BlockPos> diamondBlocks = new HashSet<>();
        Set<BlockPos> ironBlocks = new HashSet<>();

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos testPos = playerPos.add(dx * 3, dy * 3, dz * 3);

                    // Only add if in current chunk
                    int blockChunkX = testPos.getX() >> 4;
                    int blockChunkZ = testPos.getZ() >> 4;

                    if (blockChunkX == chunkX && blockChunkZ == chunkZ) {
                        if ((dx + dz) % 2 == 0) {
                            diamondBlocks.add(testPos);
                        } else {
                            ironBlocks.add(testPos);
                        }
                    }
                }
            }
        }

        if (!diamondBlocks.isEmpty()) {
            blocksToRender.put("minecraft:diamond_ore", diamondBlocks);
        }
        if (!ironBlocks.isEmpty()) {
            blocksToRender.put("minecraft:iron_ore", ironBlocks);
        }
    }

    public static void updateForProfile(ScanProfile profile) {
        // Update renderer with profile-specific settings
        if (profile != null) {
            for (String blockId : profile.targetBlocks) {
                if (!blockColors.containsKey(blockId)) {
                    // Assign a random color for new blocks
                    blockColors.put(blockId, generateRandomColor());
                }
            }
        }
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, net.minecraft.client.render.Camera camera) {
        if (!enabled || blocksToRender.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Vec3d cameraPos = camera.getPos();

        // Setup rendering
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        matrices.push();

        // Translate to camera-relative coordinates
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Render each block type
        for (Map.Entry<String, Set<BlockPos>> entry : blocksToRender.entrySet()) {
            String blockType = entry.getKey();
            BasicColor color = getColorForBlock(blockType);

            for (BlockPos pos : entry.getValue()) {
                // Check distance
                double distance = Math.sqrt(pos.getSquaredDistance(cameraPos.x, cameraPos.y, cameraPos.z));
                if (distance > maxDistance) continue;

                // Calculate alpha based on distance if fading is enabled
                float renderAlpha = alpha;
                if (fadeWithDistance) {
                    float fadeStart = maxDistance * 0.7f;
                    if (distance > fadeStart) {
                        renderAlpha = alpha * (1.0f - (float)((distance - fadeStart) / (maxDistance - fadeStart)));
                    }
                }

                // Apply LOD if enabled
                boolean useDetailed = true;
                if (useLOD && distance > lodDistance) {
                    useDetailed = false;
                }

                // Render the block outline
                renderBlockOutline(matrices, buffer, pos, color, renderAlpha, useDetailed);
            }
        }

        matrices.pop();

        // Restore rendering state
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        // Draw the buffers
        tessellator.draw();
    }

    private static void renderBlockOutline(MatrixStack matrices, BufferBuilder buffer, BlockPos pos,
                                           BasicColor color, float alpha, boolean detailed) {
        float x1 = pos.getX();
        float y1 = pos.getY();
        float z1 = pos.getZ();
        float x2 = x1 + 1.0f;
        float y2 = y1 + 1.0f;
        float z2 = z1 + 1.0f;

        float r = color.r() / 255.0f;
        float g = color.g() / 255.0f;
        float b = color.b() / 255.0f;
        float a = alpha * (color.a() / 255.0f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        // Set line width
        RenderSystem.lineWidth(lineWidth);

        // Bottom face
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).next();

        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).next();

        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).next();

        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).next();

        // Top face
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();

        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();

        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();

        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();

        // Vertical edges
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();

        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();

        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();

        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();
    }

    public static void updateBlocks(Map<String, Set<BlockPos>> blocks) {
        blocksToRender.clear();
        blocksToRender.putAll(blocks);
    }

    public static void clearBlocks() {
        blocksToRender.clear();
    }

    public static void toggle() {
        enabled = !enabled;
        System.out.println("[Neoporium] XRay " + (enabled ? "enabled" : "disabled"));
    }

    public static void test() {
        // Create a test pattern around the player
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            BlockPos playerPos = client.player.getBlockPos();
            updateForCurrentChunk(client.world, playerPos);

            if (client.player != null) {
                client.player.sendMessage(net.minecraft.text.Text.literal(
                        "§6[Neoporium] XRay test pattern created around you"), false);
            }
        }
    }

    // Getters and setters
    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }

    public static String getRenderMode() { return renderMode; }
    public static void setRenderMode(String mode) {
        if (Arrays.asList("wireframe", "solid", "transparent", "glowing").contains(mode)) {
            renderMode = mode;
        }
    }

    public static boolean isThroughWalls() { return throughWalls; }
    public static void setThroughWalls(boolean value) { throughWalls = value; }

    public static boolean isFadeWithDistance() { return fadeWithDistance; }
    public static void setFadeWithDistance(boolean value) { fadeWithDistance = value; }

    public static boolean isShowDistance() { return showDistance; }
    public static void setShowDistance(boolean value) { showDistance = value; }

    public static boolean isUseLOD() { return useLOD; }
    public static void setUseLOD(boolean value) { useLOD = value; }

    public static float getAlpha() { return alpha; }
    public static void setAlpha(float value) { alpha = Math.max(0.1f, Math.min(1.0f, value)); }

    public static float getLineWidth() { return lineWidth; }
    public static void setLineWidth(float value) { lineWidth = Math.max(0.5f, Math.min(5.0f, value)); }

    public static int getMaxDistance() { return maxDistance; }
    public static void setMaxDistance(int value) { maxDistance = Math.max(16, Math.min(256, value)); }

    public static int getLODDistance() { return lodDistance; }
    public static void setLODDistance(int value) { lodDistance = Math.max(16, Math.min(128, value)); }

    public static int getBlockCount() {
        return blocksToRender.values().stream().mapToInt(Set::size).sum();
    }

    public static BasicColor getColorForBlock(String blockId) {
        return blockColors.getOrDefault(blockId, new BasicColor(255, 0, 0, 200));
    }

    public static void setColorForBlock(String blockId, BasicColor color) {
        blockColors.put(blockId, color);
    }

    private static BasicColor generateRandomColor() {
        Random rand = new Random();
        return new BasicColor(
                rand.nextInt(256),
                rand.nextInt(256),
                rand.nextInt(256),
                200
        );
    }

    public static void saveSettings() {
        // Save XRay settings to config
        try {
            java.io.File configDir = new java.io.File("config/neoporiumscanner");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            java.io.File xrayConfig = new java.io.File(configDir, "xray.json");
            try (java.io.FileWriter writer = new java.io.FileWriter(xrayConfig)) {
                com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
                XRaySettings settings = new XRaySettings();
                gson.toJson(settings, writer);
            }
        } catch (Exception e) {
            System.err.println("[Neoporium] Error saving XRay settings: " + e.getMessage());
        }
    }

    private static class XRaySettings {
        boolean enabled = XRayRenderer.enabled;
        String renderMode = XRayRenderer.renderMode;
        boolean throughWalls = XRayRenderer.throughWalls;
        boolean fadeWithDistance = XRayRenderer.fadeWithDistance;
        boolean showDistance = XRayRenderer.showDistance;
        boolean useLOD = XRayRenderer.useLOD;
        float alpha = XRayRenderer.alpha;
        float lineWidth = XRayRenderer.lineWidth;
        int maxDistance = XRayRenderer.maxDistance;
        int lodDistance = XRayRenderer.lodDistance;
        Map<String, BasicColor> blockColors = XRayRenderer.blockColors;
    }
}