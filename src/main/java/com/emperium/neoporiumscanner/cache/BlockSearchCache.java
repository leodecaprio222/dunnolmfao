package com.emperium.neoporiumscanner.cache;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockSearchCache {
    private static BlockSearchCache instance;
    private final Map<ChunkPos, Set<BlockPos>> cache;
    private final Set<ChunkPos> scannedChunks;

    private BlockSearchCache() {
        this.cache = new ConcurrentHashMap<>();
        this.scannedChunks = ConcurrentHashMap.newKeySet();
    }

    public static BlockSearchCache getInstance() {
        if (instance == null) {
            instance = new BlockSearchCache();
        }
        return instance;
    }

    public void addBlocks(ChunkPos chunkPos, Collection<BlockPos> blocks) {
        cache.computeIfAbsent(chunkPos, k -> ConcurrentHashMap.newKeySet()).addAll(blocks);
        scannedChunks.add(chunkPos);
    }

    public Set<BlockPos> getBlocks(ChunkPos chunkPos) {
        return cache.getOrDefault(chunkPos, Collections.emptySet());
    }

    public boolean isChunkScanned(ChunkPos chunkPos) {
        return scannedChunks.contains(chunkPos);
    }

    public void markChunkScanned(ChunkPos chunkPos) {
        scannedChunks.add(chunkPos);
    }

    public void clear() {
        cache.clear();
        scannedChunks.clear();
    }

    public void removeChunk(ChunkPos chunkPos) {
        cache.remove(chunkPos);
        scannedChunks.remove(chunkPos);
    }

    public int getCachedBlockCount() {
        return cache.values().stream().mapToInt(Set::size).sum();
    }
}