package com.emperium.neoporiumscanner.storage;

import com.emperium.neoporiumscanner.core.LogManager;
import com.emperium.neoporiumscanner.xray.BasicColor;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockStore implements Store {
    private static BlockStore instance;
    private final Map<String, BlockEntry> blocks;
    private final Map<String, BasicColor> colorCache;
    private final File configFile;

    private static class BlockEntry {
        BasicColor color;
        boolean enabled;

        BlockEntry(BasicColor color, boolean enabled) {
            this.color = color;
            this.enabled = enabled;
        }
    }

    private BlockStore() {
        this.blocks = new ConcurrentHashMap<>();
        this.colorCache = new ConcurrentHashMap<>();
        this.configFile = new File("config/neoporium-scanner/blocks.json");
        loadDefaultBlocks();
    }

    public static BlockStore getInstance() {
        if (instance == null) {
            instance = new BlockStore();
        }
        return instance;
    }

    private void loadDefaultBlocks() {
        // Default ore configurations
        addBlock("minecraft:coal_ore", new BasicColor(50, 50, 50, 200), true);
        addBlock("minecraft:deepslate_coal_ore", new BasicColor(40, 40, 40, 200), true);
        addBlock("minecraft:iron_ore", new BasicColor(200, 150, 100, 200), true);
        addBlock("minecraft:deepslate_iron_ore", new BasicColor(180, 130, 90, 200), true);
        addBlock("minecraft:gold_ore", new BasicColor(255, 215, 0, 200), true);
        addBlock("minecraft:deepslate_gold_ore", new BasicColor(235, 195, 0, 200), true);
        addBlock("minecraft:diamond_ore", new BasicColor(100, 200, 255, 200), true);
        addBlock("minecraft:deepslate_diamond_ore", new BasicColor(80, 180, 235, 200), true);
        addBlock("minecraft:emerald_ore", new BasicColor(0, 200, 100, 200), true);
        addBlock("minecraft:deepslate_emerald_ore", new BasicColor(0, 180, 80, 200), true);
        addBlock("minecraft:redstone_ore", new BasicColor(255, 0, 0, 200), true);
        addBlock("minecraft:deepslate_redstone_ore", new BasicColor(235, 0, 0, 200), true);
        addBlock("minecraft:lapis_ore", new BasicColor(0, 0, 255, 200), true);
        addBlock("minecraft:deepslate_lapis_ore", new BasicColor(0, 0, 235, 200), true);
        addBlock("minecraft:copper_ore", new BasicColor(200, 100, 50, 200), true);
        addBlock("minecraft:deepslate_copper_ore", new BasicColor(180, 80, 40, 200), true);
        addBlock("minecraft:ancient_debris", new BasicColor(100, 50, 50, 200), true);
        addBlock("minecraft:nether_quartz_ore", new BasicColor(255, 255, 255, 200), true);
    }

    public void addBlock(String blockId, BasicColor color, boolean enabled) {
        blocks.put(blockId, new BlockEntry(color, enabled));
        colorCache.put(blockId, color);
    }

    public void removeBlock(String blockId) {
        blocks.remove(blockId);
        colorCache.remove(blockId);
    }

    public BasicColor getColor(String blockId) {
        BlockEntry entry = blocks.get(blockId);
        return entry != null ? entry.color : null;
    }

    public BasicColor getColorForBlock(BlockState state) {
        String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
        return colorCache.computeIfAbsent(blockId, id -> {
            BlockEntry entry = blocks.get(id);
            return entry != null ? entry.color : null;
        });
    }

    public boolean isEnabled(String blockId) {
        BlockEntry entry = blocks.get(blockId);
        return entry != null && entry.enabled;
    }

    public void setEnabled(String blockId, boolean enabled) {
        BlockEntry entry = blocks.get(blockId);
        if (entry != null) {
            entry.enabled = enabled;
        }
    }

    public boolean shouldRender(BlockState state) {
        String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
        return isEnabled(blockId);
    }

    public void processFoundBlock(BlockPos pos, BlockState state) {
        String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
        if (shouldRender(state)) {
            BasicColor color = getColor(blockId);
            if (color != null) {
                XRayRenderer.addBlock(pos, color);
                LogManager.logBlockFound(pos, state);
            }
        }
    }

    public Map<String, Map<String, Object>> getAllBlocks() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        blocks.forEach((id, entry) -> {
            Map<String, Object> data = new HashMap<>();
            data.put("color", Map.of(
                    "r", entry.color.r(),
                    "g", entry.color.g(),
                    "b", entry.color.b(),
                    "a", entry.color.a()
            ));
            data.put("enabled", entry.enabled);
            result.put(id, data);
        });
        return result;
    }

    @Override
    public void save() {
        try {
            configFile.getParentFile().mkdirs();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Map<String, Object> data = new HashMap<>();
            data.put("blocks", getAllBlocks());

            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        if (!configFile.exists()) {
            save();
            return;
        }

        try {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                if (json.has("blocks")) {
                    JsonObject blocksJson = json.getAsJsonObject("blocks");
                    blocks.clear();
                    colorCache.clear();

                    for (Map.Entry<String, JsonElement> entry : blocksJson.entrySet()) {
                        String blockId = entry.getKey();
                        JsonObject blockData = entry.getValue().getAsJsonObject();

                        JsonObject colorJson = blockData.getAsJsonObject("color");
                        BasicColor color = new BasicColor(
                                colorJson.get("r").getAsInt(),
                                colorJson.get("g").getAsInt(),
                                colorJson.get("b").getAsInt(),
                                colorJson.get("a").getAsInt()
                        );

                        boolean enabled = blockData.get("enabled").getAsBoolean();
                        addBlock(blockId, color, enabled);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}