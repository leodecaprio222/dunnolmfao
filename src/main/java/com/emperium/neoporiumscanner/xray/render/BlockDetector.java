package com.emperium.neoporiumscanner.xray.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import com.emperium.neoporiumscanner.config.ConfigManager;
import java.util.HashMap;
import java.util.Map;

public class BlockDetector {

    public enum BlockCategory {
        ORES("Ores", new int[]{255, 215, 0}),        // Gold
        CHESTS("Containers", new int[]{255, 165, 0}), // Orange
        SPAWNERS("Spawners", new int[]{255, 0, 0}),  // Red
        REDSTONE("Redstone", new int[]{255, 0, 0}),  // Red
        PORTAL("Portals", new int[]{128, 0, 128}),   // Purple
        OTHER("Other", new int[]{200, 200, 200})     // Light Gray
    }

    private static final Map<String, BlockCategory> BLOCK_CATEGORIES = new HashMap<>();

    static {
        // Ore blocks
        BLOCK_CATEGORIES.put("minecraft:coal_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:iron_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:gold_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:diamond_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:emerald_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:lapis_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:redstone_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:copper_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:deepslate_coal_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:deepslate_iron_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:deepslate_gold_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:deepslate_diamond_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:deepslate_emerald_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:deepslate_lapis_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:deepslate_redstone_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:deepslate_copper_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:nether_gold_ore", BlockCategory.ORES);
        BLOCK_CATEGORIES.put("minecraft:nether_quartz_ore", BlockCategory.ORES);

        // Container blocks
        BLOCK_CATEGORIES.put("minecraft:chest", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:ender_chest", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:trapped_chest", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:barrel", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:white_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:orange_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:magenta_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:light_blue_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:yellow_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:lime_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:pink_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:gray_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:light_gray_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:cyan_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:purple_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:blue_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:brown_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:green_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:red_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:black_shulker_box", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:dispenser", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:dropper", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:hopper", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:furnace", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:blast_furnace", BlockCategory.CHESTS);
        BLOCK_CATEGORIES.put("minecraft:smoker", BlockCategory.CHESTS);

        // Spawner blocks
        BLOCK_CATEGORIES.put("minecraft:spawner", BlockCategory.SPAWNERS);

        // Redstone blocks
        BLOCK_CATEGORIES.put("minecraft:redstone_block", BlockCategory.REDSTONE);
        BLOCK_CATEGORIES.put("minecraft:redstone_torch", BlockCategory.REDSTONE);
        BLOCK_CATEGORIES.put("minecraft:redstone_wire", BlockCategory.REDSTONE);
        BLOCK_CATEGORIES.put("minecraft:repeater", BlockCategory.REDSTONE);
        BLOCK_CATEGORIES.put("minecraft:comparator", BlockCategory.REDSTONE);
        BLOCK_CATEGORIES.put("minecraft:lever", BlockCategory.REDSTONE);
        BLOCK_CATEGORIES.put("minecraft:button", BlockCategory.REDSTONE);
        BLOCK_CATEGORIES.put("minecraft:pressure_plate", BlockCategory.REDSTONE);
        BLOCK_CATEGORIES.put("minecraft:daylight_detector", BlockCategory.REDSTONE);
        BLOCK_CATEGORIES.put("minecraft:observer", BlockCategory.REDSTONE);

        // Portal blocks
        BLOCK_CATEGORIES.put("minecraft:nether_portal", BlockCategory.PORTAL);
        BLOCK_CATEGORIES.put("minecraft:end_portal", BlockCategory.PORTAL);
        BLOCK_CATEGORIES.put("minecraft:end_portal_frame", BlockCategory.PORTAL);
    }

    public static BlockCategory categorize(BlockState state) {
        Block block = state.getBlock();
        Identifier id = Registries.BLOCK.getId(block);
        String blockId = id.toString();

        return BLOCK_CATEGORIES.getOrDefault(blockId, BlockCategory.OTHER);
    }

    public static int[] getColorForCategory(BlockCategory category) {
        return category.getColor();
    }

    public static int[] getColorForBlock(BlockState state) {
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        String blockId = id.toString();

        // Check for custom ESP color
        Map<String, int[]> espColors = ConfigManager.getESPColors();
        if (espColors.containsKey(blockId)) {
            return espColors.get(blockId);
        }

        // Use category color
        BlockCategory category = categorize(state);
        return getColorForCategory(category);
    }

    public static String getBlockId(BlockState state) {
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        return id.toString();
    }

    public static boolean isTrackedBlock(BlockState state) {
        String blockId = getBlockId(state);
        return ConfigManager.getTrackedBlocks().contains(blockId);
    }

    // Inner class for BlockCategory with color
    public enum BlockCategory {
        ORES("Ores", new int[]{255, 215, 0}),
        CHESTS("Containers", new int[]{255, 165, 0}),
        SPAWNERS("Spawners", new int[]{255, 0, 0}),
        REDSTONE("Redstone", new int[]{255, 0, 0}),
        PORTAL("Portals", new int[]{128, 0, 128}),
        OTHER("Other", new int[]{200, 200, 200});

        private final String displayName;
        private final int[] color;

        BlockCategory(String displayName, int[] color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int[] getColor() {
            return color;
        }
    }
}