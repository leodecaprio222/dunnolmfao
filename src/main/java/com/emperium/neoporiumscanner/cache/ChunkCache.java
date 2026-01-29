package com.emperium.neoporiumscanner.cache;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.Map;

public class ChunkCache {
    private static ChunkCache instance;
    private final Map<ChunkPos, WorldChunk> chunkCache;
    private final Map<ChunkPos, Long> lastAccessTime;

    private static final long CACHE_DURATION = 60000; // 1 minute

    private ChunkCache() {
        this.chunkCache = new HashMap<>();
        this.lastAccessTime = new HashMap<>();
    }

    public static ChunkCache getInstance() {
        if (instance == null) {
            instance = new ChunkCache();
        }
        return instance;
    }

    public void cacheChunk(WorldChunk chunk) {
        if (chunk == null) return;

        ChunkPos pos = chunk.getPos();
        chunkCache.put(pos, chunk);
        lastAccessTime.put(pos, System.currentTimeMillis());

        // Clean old entries
        cleanOldEntries();
    }

    public WorldChunk getChunk(ChunkPos pos) {
        WorldChunk chunk = chunkCache.get(pos);
        if (chunk != null) {
            lastAccessTime.put(pos, System.currentTimeMillis());
        }
        return chunk;
    }

    public void removeChunk(ChunkPos pos) {
        chunkCache.remove(pos);
        lastAccessTime.remove(pos);
    }

    private void cleanOldEntries() {
        long currentTime = System.currentTimeMillis();
        lastAccessTime.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > CACHE_DURATION) {
                chunkCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public void clear() {
        chunkCache.clear();
        lastAccessTime.clear();
    }
}