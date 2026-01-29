package com.emperium.neoporiumscanner.core;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ScanController {
    private static ScanController instance;
    public static Set<BlockPos> renderQueue = Collections.synchronizedSet(new HashSet<>());
    private static ChunkPos playerLastChunk;
    private static long lastScanTime = 0;
    private static final long SCAN_COOLDOWN = 1000;

    private ScanController() {}

    public static ScanController getInstance() {
        if (instance == null) {
            instance = new ScanController();
        }
        return instance;
    }

    // Removed playerLocationChanged() method as it needs Minecraft client

    public static synchronized void runTask(World world, ChunkPos playerChunk, boolean forceRerun) {
        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime < SCAN_COOLDOWN && !forceRerun) {
            return;
        }

        // Simple check: if player moved to new chunk
        if (playerLastChunk != null && playerLastChunk.equals(playerChunk) && !forceRerun) {
            return;
        }

        // Update the players last chunk
        playerLastChunk = playerChunk;

        // Run scan in background
        new Thread(() -> {
            // This would need a player reference, so we'll handle this differently
            // Scan will be triggered from ClientTickHandler instead
            lastScanTime = System.currentTimeMillis();
        }).start();
    }

    public static void blockBroken(World world, BlockPos blockPos) {
        if (renderQueue.contains(blockPos)) {
            // This will be handled by the main scanner
        }
    }

    public static void blockPlaced(BlockPos blockPos) {
        // This will be handled by the main scanner
    }

    // New methods for chunk events
    public void onChunkLoaded(int chunkX, int chunkZ) {
        System.out.println("[Neoporium] Chunk loaded: " + chunkX + ", " + chunkZ);
    }

    public void onChunkUnloaded(int chunkX, int chunkZ) {
        System.out.println("[Neoporium] Chunk unloaded: " + chunkX + ", " + chunkZ);
    }

    public void onBlockUpdated(BlockPos pos) {
        System.out.println("[Neoporium] Block updated at: " + pos);
    }

    public void onClientTick() {
        // Client tick logic moved to ClientTickHandler
    }

    public static void clearQueue() {
        renderQueue.clear();
        playerLastChunk = null;
    }
}