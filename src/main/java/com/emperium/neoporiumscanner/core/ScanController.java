package com.emperium.neoporiumscanner.core;

import com.emperium.neoporiumscanner.config.StateSettings;
import net.minecraft.world.Heightmap;
import com.emperium.neoporiumscanner.storage.BlockStore;
import com.emperium.neoporiumscanner.xray.BasicColor;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import java.util.*;
import java.util.concurrent.*;

public class ScanController {
    private static ScanController instance;
    private final ExecutorService executorService;
    private final Set<ChunkPos> scannedChunks;
    private final BlockStore blockStore;
    private boolean isScanning = false;
    private int scanRadius = 8;

    private ScanController() {
        this.executorService = Executors.newFixedThreadPool(2);
        this.scannedChunks = ConcurrentHashMap.newKeySet();
        this.blockStore = BlockStore.getInstance();
    }

    public static ScanController getInstance() {
        if (instance == null) {
            instance = new ScanController();
        }
        return instance;
    }

    public void startScan() {
        if (isScanning) return;

        isScanning = true;
        scannedChunks.clear();
        XRayRenderer.clearBlocks();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        BlockPos center = client.player.getBlockPos();
        ChunkPos centerChunk = new ChunkPos(center);

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int z = -scanRadius; z <= scanRadius; z++) {
                ChunkPos targetChunk = new ChunkPos(centerChunk.x + x, centerChunk.z + z);
                if (!scannedChunks.contains(targetChunk)) {
                    scannedChunks.add(targetChunk);
                    executorService.submit(() -> scanChunk(targetChunk, client.world));
                }
            }
        }
    }

    private void scanChunk(ChunkPos chunkPos, World world) {
        WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
        if (chunk == null) return;

        List<BlockPos> foundBlocks = new ArrayList<>();

        // Get world height for 1.21.4
        int maxY = world.getTopY(Heightmap.Type.WORLD_SURFACE, 0, 0);
        int minY = world.getBottomY();

        // Scan all blocks in chunk
        for (int x = 0; x < 16; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(
                            chunkPos.getStartX() + x,
                            y,
                            chunkPos.getStartZ() + z
                    );

                    BlockState state = chunk.getBlockState(pos);
                    if (blockStore.shouldRender(state)) {
                        foundBlocks.add(pos);
                    }
                }
            }
        }

        // Add to renderer on main thread
        if (!foundBlocks.isEmpty()) {
            MinecraftClient.getInstance().execute(() -> {
                for (BlockPos pos : foundBlocks) {
                    BlockState state = world.getBlockState(pos);
                    BasicColor color = blockStore.getColorForBlock(state);
                    if (color != null) {
                        XRayRenderer.addBlock(pos, color);
                    }
                }
            });
        }
    }

    public void stopScan() {
        isScanning = false;
        XRayRenderer.clearBlocks();
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void setScanRadius(int radius) {
        this.scanRadius = Math.max(1, Math.min(radius, 16));
    }
}