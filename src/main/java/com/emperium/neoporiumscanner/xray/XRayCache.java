package com.emperium.neoporiumscanner.xray;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class XRayCache {
    private static XRayCache instance;

    // Cache structure: ChunkPos -> Map<BlockType, Set<BlockPos>>
    private final Map<ChunkPos, Map<String, Set<BlockPos>>> chunkCache = new ConcurrentHashMap<>();
    private final Map<String, BasicColor> colorCache = new ConcurrentHashMap<>();
    private final Set<ChunkPos> dirtyChunks = ConcurrentHashMap.newKeySet();
    private long lastCleanup = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL = 30000; // 30 seconds

    private XRayCache() {
        // Initialize with default colors
        colorCache.put("minecraft:diamond_ore", new BasicColor(0, 200, 255, 200));
        colorCache.put("minecraft:deepslate_diamond_ore", new BasicColor(0, 150, 255, 200));
        colorCache.put("minecraft:iron_ore", new BasicColor(200, 200, 200, 200));
        colorCache.put("minecraft:gold_ore", new BasicColor(255, 215, 0, 200));
        colorCache.put("minecraft:copper_ore", new BasicColor(184, 115, 51, 200));
        colorCache.put("minecraft:coal_ore", new BasicColor(50, 50, 50, 200));
        colorCache.put("minecraft:emerald_ore", new BasicColor(0, 200, 0, 200));
        colorCache.put("minecraft:redstone_ore", new BasicColor(255, 0, 0, 200));
        colorCache.put("minecraft:lapis_ore", new BasicColor(0, 100, 200, 200));
        colorCache.put("minecraft:ancient_debris", new BasicColor(100, 50, 50, 200));
    }

    public static XRayCache getInstance() {
        if (instance == null) {
            instance = new XRayCache();
        }
        return instance;
    }

    public void addBlock(ChunkPos chunkPos, String blockType, BlockPos blockPos) {
        chunkCache.computeIfAbsent(chunkPos, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(blockType, k -> ConcurrentHashMap.newKeySet())
                .add(blockPos);

        dirtyChunks.add(chunkPos);
    }

    public void addBlocks(ChunkPos chunkPos, String blockType, Collection<BlockPos> blockPositions) {
        if (blockPositions.isEmpty()) {
            return;
        }

        chunkCache.computeIfAbsent(chunkPos, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(blockType, k -> ConcurrentHashMap.newKeySet())
                .addAll(blockPositions);

        dirtyChunks.add(chunkPos);
    }

    public void removeBlock(ChunkPos chunkPos, String blockType, BlockPos blockPos) {
        Map<String, Set<BlockPos>> chunkData = chunkCache.get(chunkPos);
        if (chunkData != null) {
            Set<BlockPos> blocks = chunkData.get(blockType);
            if (blocks != null) {
                blocks.remove(blockPos);
                if (blocks.isEmpty()) {
                    chunkData.remove(blockType);
                }
                if (chunkData.isEmpty()) {
                    chunkCache.remove(chunkPos);
                }
                dirtyChunks.add(chunkPos);
            }
        }
    }

    public void clearChunk(ChunkPos chunkPos) {
        chunkCache.remove(chunkPos);
        dirtyChunks.add(chunkPos);
    }

    public void clearAll() {
        chunkCache.clear();
        dirtyChunks.clear();
    }

    public Map<String, Set<BlockPos>> getBlocksForChunk(ChunkPos chunkPos) {
        Map<String, Set<BlockPos>> chunkData = chunkCache.get(chunkPos);
        if (chunkData == null) {
            return Collections.emptyMap();
        }

        // Return a copy to avoid modification issues
        Map<String, Set<BlockPos>> result = new HashMap<>();
        for (Map.Entry<String, Set<BlockPos>> entry : chunkData.entrySet()) {
            result.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        return result;
    }

    public Set<BlockPos> getBlocksOfType(String blockType) {
        Set<BlockPos> allBlocks = new HashSet<>();

        for (Map<String, Set<BlockPos>> chunkData : chunkCache.values()) {
            Set<BlockPos> blocks = chunkData.get(blockType);
            if (blocks != null) {
                allBlocks.addAll(blocks);
            }
        }

        return allBlocks;
    }

    public int getBlockCount() {
        int total = 0;
        for (Map<String, Set<BlockPos>> chunkData : chunkCache.values()) {
            for (Set<BlockPos> blocks : chunkData.values()) {
                total += blocks.size();
            }
        }
        return total;
    }

    public int getChunkCount() {
        return chunkCache.size();
    }

    public Set<ChunkPos> getCachedChunks() {
        return new HashSet<>(chunkCache.keySet());
    }

    public Set<ChunkPos> getDirtyChunks() {
        return new HashSet<>(dirtyChunks);
    }

    public void clearDirtyChunks() {
        dirtyChunks.clear();
    }

    public void markChunkDirty(ChunkPos chunkPos) {
        dirtyChunks.add(chunkPos);
    }

    public boolean isChunkDirty(ChunkPos chunkPos) {
        return dirtyChunks.contains(chunkPos);
    }

    // Color cache methods
    public BasicColor getColor(String blockType) {
        return colorCache.getOrDefault(blockType, new BasicColor(255, 0, 0, 200));
    }

    public void setColor(String blockType, BasicColor color) {
        colorCache.put(blockType, color);
    }

    public boolean hasColor(String blockType) {
        return colorCache.containsKey(blockType);
    }

    public Map<String, BasicColor> getAllColors() {
        return new HashMap<>(colorCache);
    }

    public void cleanup() {
        long currentTime = System.currentTimeMillis();

        // Only cleanup every 30 seconds
        if (currentTime - lastCleanup < CLEANUP_INTERVAL) {
            return;
        }

        lastCleanup = currentTime;

        // Remove empty chunks
        Iterator<Map.Entry<ChunkPos, Map<String, Set<BlockPos>>>> iterator =
                chunkCache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ChunkPos, Map<String, Set<BlockPos>>> entry = iterator.next();
            if (entry.getValue().isEmpty()) {
                iterator.remove();
                dirtyChunks.remove(entry.getKey());
            }
        }

        System.out.println("[Neoporium] XRayCache cleaned up. Chunks: " +
                chunkCache.size() + ", Dirty: " + dirtyChunks.size());
    }

    public void invalidateChunk(ChunkPos chunkPos) {
        clearChunk(chunkPos);
        markChunkDirty(chunkPos);
    }

    public boolean isChunkCached(ChunkPos chunkPos) {
        return chunkCache.containsKey(chunkPos);
    }

    public Map<ChunkPos, Map<String, Set<BlockPos>>> getCacheSnapshot() {
        Map<ChunkPos, Map<String, Set<BlockPos>>> snapshot = new HashMap<>();

        for (Map.Entry<ChunkPos, Map<String, Set<BlockPos>>> chunkEntry : chunkCache.entrySet()) {
            Map<String, Set<BlockPos>> chunkSnapshot = new HashMap<>();

            for (Map.Entry<String, Set<BlockPos>> blockEntry : chunkEntry.getValue().entrySet()) {
                chunkSnapshot.put(blockEntry.getKey(), new HashSet<>(blockEntry.getValue()));
            }

            snapshot.put(chunkEntry.getKey(), chunkSnapshot);
        }

        return snapshot;
    }
}