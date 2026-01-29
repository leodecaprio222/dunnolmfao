package com.emperium.neoporiumscanner.core;

import com.emperium.neoporiumscanner.storage.BlockStore;
import net.minecraft.world.Heightmap;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import java.util.ArrayList;
import java.util.List;

public class ScanTask implements Runnable {
    private final ChunkPos chunkPos;
    private final BlockStore blockStore;
    private volatile boolean cancelled = false;

    public ScanTask(ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
        this.blockStore = BlockStore.getInstance();
    }

    @Override
    public void run() {
        if (cancelled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        World world = client.world;
        WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
        if (chunk == null) return;

        List<BlockPos> foundBlocks = new ArrayList<>();

        // Get world height for 1.21.4 Updated
        int maxY = world.getTopY(Heightmap.Type.WORLD_SURFACE, 0, 0);
        int minY = world.getBottomY();

        //Chunk Scanner
        for (int x = 0; x < 16; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = 0; z < 16; z++) {
                    if (cancelled) return;

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

        if (!cancelled && !foundBlocks.isEmpty()) {
            client.execute(() -> {
                for (BlockPos pos : foundBlocks) {
                    BlockState state = world.getBlockState(pos);
                    blockStore.processFoundBlock(pos, state);
                }
            });
        }
    }

    public void cancel() {
        cancelled = true;
    }
}