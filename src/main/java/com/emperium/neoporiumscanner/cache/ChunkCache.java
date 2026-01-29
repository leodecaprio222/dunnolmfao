package com.emperium.neoporiumscanner.cache;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import java.util.*;

public class ChunkCache {
    private static ChunkCache instance;

    private final Map<ChunkPos, ChunkData> cache = new HashMap<>();
    private final Set<ChunkPos> scannedChunks = new HashSet<>();
    private final Map<ChunkPos, Long> chunkScanTimes = new HashMap<>();
    private static final long CHUNK_CACHE_DURATION = 300000; // 5 minutes

    private static class ChunkData {
        final Map<String, Set<BlockPos>> blocksByType = new HashMap<>();
        final long timestamp;

        ChunkData() {
            this.timestamp = System.currentTimeMillis();
        }

        void addBlock(String blockType, BlockPos pos) {
            blocksByType.computeIfAbsent(blockType, k -> new HashSet<>()).add(pos);
        }

        void removeBlock(String blockType, BlockPos pos) {
            Set<BlockPos> blocks = blocksByType.get(blockType);
            if (blocks != null) {
                blocks.remove(pos);
                if (blocks.isEmpty()) {
                    blocksByType.remove(blockType);
                }
            }
        }

        Set<BlockPos> getBlocks(String blockType) {
            return blocksByType.getOrDefault(blockType, Collections.emptySet());
        }

        Map<String, Set<BlockPos>> getAllBlocks() {
            return new HashMap<>(blocksByType);
        }

        boolean isEmpty() {
            return blocksByType.isEmpty();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CHUNK_CACHE_DURATION;
        }
    }

    private ChunkCache() {}

    public static ChunkCache getInstance() {
        if (instance == null) {
            instance = new ChunkCache();
        }
        return instance;
    }

    public void cacheChunkData(ChunkPos chunkPos, Map<String, Set<BlockPos>> blocks) {
        ChunkData data = new ChunkData();
        for (Map.Entry<String, Set<BlockPos>> entry : blocks.entrySet()) {
            for (BlockPos pos : entry.getValue()) {
                data.addBlock(entry.getKey(), pos);
            }
        }
        cache.put(chunkPos, data);
        scannedChunks.add(chunkPos);
        chunkScanTimes.put(chunkPos, System.currentTimeMillis());
    }

    public Map<String, Set<BlockPos>> getChunkData(ChunkPos chunkPos) {
        ChunkData data = cache.get(chunkPos);
        if (data != null) {
            if (data.isExpired()) {
                cache.remove(chunkPos);
                scannedChunks.remove(chunkPos);
                chunkScanTimes.remove(chunkPos);
                return Collections.emptyMap();
            }
            return data.getAllBlocks();
        }
        return Collections.emptyMap();
    }

    public Set<BlockPos> getBlocksOfType(ChunkPos chunkPos, String blockType) {
        ChunkData data = cache.get(chunkPos);
        if (data != null && !data.isExpired()) {
            return data.getBlocks(blockType);
        }
        return Collections.emptySet();
    }

    public void addBlock(ChunkPos chunkPos, String blockType, BlockPos blockPos) {
        ChunkData data = cache.computeIfAbsent(chunkPos, k -> new ChunkData());
        data.addBlock(blockType, blockPos);
        scannedChunks.add(chunkPos);
        chunkScanTimes.put(chunkPos, System.currentTimeMillis());
    }

    public void removeBlock(ChunkPos chunkPos, String blockType, BlockPos blockPos) {
        ChunkData data = cache.get(chunkPos);
        if (data != null) {
            data.removeBlock(blockType, blockPos);
            if (data.isEmpty()) {
                cache.remove(chunkPos);
                scannedChunks.remove(chunkPos);
                chunkScanTimes.remove(chunkPos);
            }
        }
    }

    public void clearChunk(ChunkPos chunkPos) {
        cache.remove(chunkPos);
        scannedChunks.remove(chunkPos);
        chunkScanTimes.remove(chunkPos);
    }

    public void clearAll() {
        cache.clear();
        scannedChunks.clear();
        chunkScanTimes.clear();
    }

    public boolean isChunkScanned(ChunkPos chunkPos) {
        return scannedChunks.contains(chunkPos);
    }

    public boolean isChunkDataValid(ChunkPos chunkPos) {
        ChunkData data = cache.get(chunkPos);
        return data != null && !data.isExpired();
    }

    public long getChunkScanTime(ChunkPos chunkPos) {
        return chunkScanTimes.getOrDefault(chunkPos, 0L);
    }

    public Set<ChunkPos> getScannedChunks() {
        return new HashSet<>(scannedChunks);
    }

    public Set<ChunkPos> getCachedChunks() {
        return new HashSet<>(cache.keySet());
    }

    public int getTotalBlocks() {
        int total = 0;
        for (ChunkData data : cache.values()) {
            for (Set<BlockPos> blocks : data.blocksByType.values()) {
                total += blocks.size();
            }
        }
        return total;
    }

    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<ChunkPos, ChunkData>> iterator = cache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ChunkPos, ChunkData> entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                scannedChunks.remove(entry.getKey());
                chunkScanTimes.remove(entry.getKey());
            }
        }
    }

    public void validateBlocks(World world) {
        // Remove blocks that no longer exist
        for (Map.Entry<ChunkPos, ChunkData> entry : cache.entrySet()) {
            ChunkData data = entry.getValue();
            Iterator<Map.Entry<String, Set<BlockPos>>> blockIterator = data.blocksByType.entrySet().iterator();

            while (blockIterator.hasNext()) {
                Map.Entry<String, Set<BlockPos>> blockEntry = blockIterator.next();
                String blockType = blockEntry.getKey();
                Iterator<BlockPos> posIterator = blockEntry.getValue().iterator();

                while (posIterator.hasNext()) {
                    BlockPos pos = posIterator.next();
                    // Check if block is still there and is the same type
                    if (world.getBlockState(pos).isAir()) {
                        posIterator.remove();
                    }
                }

                if (blockEntry.getValue().isEmpty()) {
                    blockIterator.remove();
                }
            }

            if (data.isEmpty()) {
                cache.remove(entry.getKey());
                scannedChunks.remove(entry.getKey());
                chunkScanTimes.remove(entry.getKey());
            }
        }
    }
}