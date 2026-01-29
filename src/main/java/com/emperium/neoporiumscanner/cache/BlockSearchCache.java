package com.emperium.neoporiumscanner.cache;

import com.emperium.neoporiumscanner.core.ScanProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.*;

public class BlockSearchCache {
    private final Map<String, BlockSearchEntry> cache = new HashMap<>();
    private final Set<String> enabledBlocks = new HashSet<>();
    private boolean showLava = false;
    private boolean needsRefresh = true;

    public static class BlockSearchEntry {
        private final String blockId;
        private final BlockState defaultState;
        private final com.emperium.neoporiumscanner.xray.BasicColor color;
        private final boolean isDefault;

        public BlockSearchEntry(String blockId, BlockState defaultState,
                                com.emperium.neoporiumscanner.xray.BasicColor color, boolean isDefault) {
            this.blockId = blockId;
            this.defaultState = defaultState;
            this.color = color;
            this.isDefault = isDefault;
        }

        public String getBlockId() { return blockId; }
        public BlockState getDefaultState() { return defaultState; }
        public com.emperium.neoporiumscanner.xray.BasicColor getColor() { return color; }
        public boolean isDefault() { return isDefault; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockSearchEntry that = (BlockSearchEntry) o;
            return blockId.equals(that.blockId);
        }

        @Override
        public int hashCode() {
            return blockId.hashCode();
        }
    }

    public void updateFromProfiles(List<ScanProfile> profiles) {
        cache.clear();
        enabledBlocks.clear();

        for (ScanProfile profile : profiles) {
            for (String blockId : profile.targetBlocks) {
                if (!enabledBlocks.contains(blockId)) {
                    addBlock(blockId);
                    enabledBlocks.add(blockId);
                }
            }
        }

        needsRefresh = true;
    }

    public void addBlock(String blockId) {
        try {
            Identifier id = Identifier.tryParse(blockId);
            if (id != null) {
                Optional<Block> block = Registries.BLOCK.getOrEmpty(id);
                if (block.isPresent()) {
                    BlockState defaultState = block.get().getDefaultState();
                    com.emperium.neoporiumscanner.xray.BasicColor color =
                            com.emperium.neoporiumscanner.core.BlockValidator.getRecommendedColor(blockId);

                    BlockSearchEntry entry = new BlockSearchEntry(blockId, defaultState, color, true);
                    cache.put(blockId, entry);
                    needsRefresh = true;
                }
            }
        } catch (Exception e) {
            System.err.println("[Neoporium] Error adding block to cache: " + blockId + " - " + e.getMessage());
        }
    }

    public void removeBlock(String blockId) {
        cache.remove(blockId);
        enabledBlocks.remove(blockId);
        needsRefresh = true;
    }

    public boolean containsBlock(String blockId) {
        return cache.containsKey(blockId);
    }

    public boolean containsBlockState(BlockState state) {
        for (BlockSearchEntry entry : cache.values()) {
            if (entry.isDefault() && entry.getDefaultState().equals(state)) {
                return true;
            }
        }
        return false;
    }

    public com.emperium.neoporiumscanner.xray.BasicColor getColorForBlock(String blockId) {
        BlockSearchEntry entry = cache.get(blockId);
        return entry != null ? entry.getColor() : new com.emperium.neoporiumscanner.xray.BasicColor(255, 0, 0, 200);
    }

    public com.emperium.neoporiumscanner.xray.BasicColor getColorForBlockState(BlockState state) {
        for (BlockSearchEntry entry : cache.values()) {
            if (entry.isDefault() && entry.getDefaultState().equals(state)) {
                return entry.getColor();
            }
        }
        return new com.emperium.neoporiumscanner.xray.BasicColor(255, 0, 0, 200);
    }

    public void setColorForBlock(String blockId, com.emperium.neoporiumscanner.xray.BasicColor color) {
        BlockSearchEntry entry = cache.get(blockId);
        if (entry != null) {
            cache.put(blockId, new BlockSearchEntry(blockId, entry.getDefaultState(), color, entry.isDefault()));
            needsRefresh = true;
        }
    }

    public Set<BlockSearchEntry> getEntries() {
        return new HashSet<>(cache.values());
    }

    public Set<String> getEnabledBlocks() {
        return new HashSet<>(enabledBlocks);
    }

    public int size() {
        return cache.size();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public void clear() {
        cache.clear();
        enabledBlocks.clear();
        needsRefresh = true;
    }

    public void setShowLava(boolean showLava) {
        this.showLava = showLava;
        needsRefresh = true;
    }

    public boolean isShowLava() {
        return showLava;
    }

    public boolean needsRefresh() {
        return needsRefresh;
    }

    public void markRefreshed() {
        needsRefresh = false;
    }

    public List<String> getBlockIds() {
        return new ArrayList<>(cache.keySet());
    }

    public Map<String, com.emperium.neoporiumscanner.xray.BasicColor> getColorMap() {
        Map<String, com.emperium.neoporiumscanner.xray.BasicColor> colorMap = new HashMap<>();
        for (Map.Entry<String, BlockSearchEntry> entry : cache.entrySet()) {
            colorMap.put(entry.getKey(), entry.getValue().getColor());
        }
        return colorMap;
    }
}