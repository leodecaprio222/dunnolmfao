package com.emperium.neoporiumscanner.core;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScanTask implements Runnable {
    private static final AtomicBoolean isScanning = new AtomicBoolean(false);
    private final ChunkPos centerChunk;
    private final int radius;
    private final int minY;
    private final int maxY;
    private final Set<String> targetBlocks;
    private final World world;

    public ScanTask(World world, ChunkPos centerChunk, int radius, int minY, int maxY, Set<String> targetBlocks) {
        this.world = world;
        this.centerChunk = centerChunk;
        this.radius = radius;
        this.minY = minY;
        this.maxY = maxY;
        this.targetBlocks = new HashSet<>(targetBlocks);
    }

    @Override
    public void run() {
        if (isScanning.get()) {
            System.out.println("[Neoporium] Scan already in progress, skipping...");
            return;
        }

        isScanning.set(true);
        try {
            if (world == null) {
                return;
            }

            Map<String, List<BlockPos>> foundBlocks = new HashMap<>();

            System.out.println("[Neoporium] Starting scan task: radius=" + radius + ", Y=" + minY + "-" + maxY);

            int totalChunks = (radius * 2 + 1) * (radius * 2 + 1);
            int scannedChunks = 0;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    ChunkPos chunkPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);

                    if (world.getChunk(chunkPos.x, chunkPos.z) != null) {
                        scanChunk(world, chunkPos, foundBlocks);
                        scannedChunks++;
                    }

                    // Small sleep to prevent lag
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            // Update the scanner with found blocks
            AdvancedScanner.getInstance().updateFoundBlocks(foundBlocks);

            System.out.println("[Neoporium] Scan complete: scanned " + scannedChunks + "/" + totalChunks + " chunks");

        } catch (Exception e) {
            System.err.println("[Neoporium] Error during scan: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isScanning.set(false);
        }
    }

    private void scanChunk(World world, ChunkPos chunkPos, Map<String, List<BlockPos>> foundBlocks) {
        int chunkStartX = chunkPos.getStartX();
        int chunkStartZ = chunkPos.getStartZ();

        for (int x = chunkStartX; x < chunkStartX + 16; x++) {
            for (int z = chunkStartZ; z < chunkStartZ + 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    String blockId = world.getBlockState(pos).getBlock().getRegistryEntry().registryKey().getValue().toString();

                    if (targetBlocks.contains(blockId)) {
                        if (!world.getBlockState(pos).isAir()) {
                            foundBlocks.computeIfAbsent(blockId, k -> new ArrayList<>()).add(pos);
                        }
                    }
                }
            }
        }
    }

    public static boolean isCurrentlyScanning() {
        return isScanning.get();
    }

    public static void cancelCurrentScan() {
        System.out.println("[Neoporium] Scan cancellation requested");
    }
}