package com.emperium.neoporiumscanner.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import com.emperium.neoporiumscanner.xray.BlockPosWithColor;
import com.emperium.neoporiumscanner.xray.BasicColor;
import com.emperium.neoporiumscanner.xray.render.BlockDetector;
import com.emperium.neoporiumscanner.xray.render.RenderManager;
import com.emperium.neoporiumscanner.config.ConfigManager;
import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.cache.ChunkCache;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScanController {
    private static ScanController instance;
    private final ExecutorService executorService;
    private Future<?> currentScanTask;
    private boolean isScanning = false;
    private final List<BlockPosWithColor> scannedBlocks = new ArrayList<>();
    private final ChunkCache chunkCache = new ChunkCache();

    private ScanController() {
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "Neoporium-Scanner-ScanThread");
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
    }

    public static ScanController getInstance() {
        if (instance == null) {
            instance = new ScanController();
        }
        return instance;
    }

    public synchronized void startScan() {
        if (isScanning) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        isScanning = true;
        StateSettings.setScanning(true);

        // Clear previous results
        scannedBlocks.clear();
        RenderManager.clear();

        // Start new scan task
        currentScanTask = executorService.submit(() -> {
            try {
                World world = client.world;
                BlockPos playerPos = client.player.getBlockPos();
                int range = ConfigManager.getScanRange();

                // Calculate scan area
                int minX = playerPos.getX() - range;
                int maxX = playerPos.getX() + range;
                int minY = Math.max(world.getBottomY(), playerPos.getY() - range);
                int maxY = Math.min(world.getTopY(), playerPos.getY() + range);
                int minZ = playerPos.getZ() - range;
                int maxZ = playerPos.getZ() + range;

                List<BlockPosWithColor> newBlocks = new ArrayList<>();

                // Scan in chunks for better performance
                int chunkMinX = minX >> 4;
                int chunkMaxX = maxX >> 4;
                int chunkMinZ = minZ >> 4;
                int chunkMaxZ = maxZ >> 4;

                for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                    for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                        if (!isScanning) {
                            break;
                        }

                        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

                        // Check if chunk is already cached
                        if (chunkCache.isChunkCached(chunkPos)) {
                            List<BlockPosWithColor> cachedBlocks = chunkCache.getBlocksInChunk(chunkPos);
                            newBlocks.addAll(cachedBlocks);
                            continue;
                        }

                        // Scan this chunk
                        List<BlockPosWithColor> chunkBlocks = scanChunk(world, chunkPos, minY, maxY,
                                Math.max(minX, chunkX << 4),
                                Math.min(maxX, (chunkX << 4) + 15),
                                Math.max(minZ, chunkZ << 4),
                                Math.min(maxZ, (chunkZ << 4) + 15));

                        // Cache the chunk
                        chunkCache.cacheChunk(chunkPos, chunkBlocks);
                        newBlocks.addAll(chunkBlocks);

                        // Update renderer periodically
                        if (newBlocks.size() >= 1000) {
                            updateRenderer(newBlocks);
                            newBlocks.clear();
                        }
                    }
                }

                // Update with remaining blocks
                if (!newBlocks.isEmpty()) {
                    updateRenderer(newBlocks);
                }

                // Add all blocks to scanned list
                scannedBlocks.addAll(newBlocks);

            } catch (Exception e) {
                System.err.println("Scan error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                synchronized (ScanController.this) {
                    isScanning = false;
                    StateSettings.setScanning(false);
                    currentScanTask = null;
                }
            }
        });
    }

    private List<BlockPosWithColor> scanChunk(World world, ChunkPos chunkPos, int minY, int maxY,
                                              int minX, int maxX, int minZ, int maxZ) {
        List<BlockPosWithColor> chunkBlocks = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!isScanning) {
                        return chunkBlocks;
                    }

                    BlockPos pos = new BlockPos(x, y, z);

                    // Check if block should be scanned
                    if (BlockValidator.shouldScanBlock(world, pos)) {
                        int[] colorArray = BlockDetector.getColorForBlock(world.getBlockState(pos));
                        BasicColor color = new BasicColor(colorArray[0], colorArray[1], colorArray[2]);
                        String blockId = BlockDetector.getBlockId(world.getBlockState(pos));

                        chunkBlocks.add(new BlockPosWithColor(pos, color, blockId));
                    }
                }
            }
        }

        return chunkBlocks;
    }

    private void updateRenderer(List<BlockPosWithColor> blocks) {
        MinecraftClient.getInstance().execute(() -> {
            RenderManager.updateBlocks(blocks);
        });
    }

    public synchronized void stopScan() {
        if (!isScanning) {
            return;
        }

        isScanning = false;
        StateSettings.setScanning(false);

        if (currentScanTask != null && !currentScanTask.isDone()) {
            currentScanTask.cancel(true);
        }
    }

    public synchronized boolean isScanning() {
        return isScanning;
    }

    public List<BlockPosWithColor> getScannedBlocks() {
        return new ArrayList<>(scannedBlocks);
    }

    public void clearCache() {
        chunkCache.clear();
        scannedBlocks.clear();
        RenderManager.clear();
    }

    public void onWorldUnload() {
        stopScan();
        clearCache();
        RenderManager.onWorldUnload();
    }

    // Static convenience methods
    public static void startScan() {
        getInstance().startScan();
    }

    public static void stopScan() {
        getInstance().stopScan();
    }

    public static boolean isScanning() {
        return getInstance().isScanning();
    }
}