package com.emperium.neoporiumscanner.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import java.util.*;

public class BlockValidator {

    /**
     * Validates if a block ID string is valid
     */
    public static boolean isValidBlockId(String blockId) {
        if (blockId == null || blockId.isEmpty()) {
            return false;
        }

        try {
            // Check if it has namespace:path format
            if (!blockId.contains(":")) {
                blockId = "minecraft:" + blockId;
            }

            Identifier id = Identifier.tryParse(blockId);
            if (id == null) {
                return false;
            }

            // Check if block exists in registry
            return Registries.BLOCK.getOrEmpty(id).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Normalizes block ID (ensures it has namespace)
     */
    public static String normalizeBlockId(String blockId) {
        if (blockId == null || blockId.isEmpty()) {
            return blockId;
        }

        if (!blockId.contains(":")) {
            return "minecraft:" + blockId;
        }

        return blockId.toLowerCase();
    }

    /**
     * Gets display name for a block ID
     */
    public static String getDisplayName(String blockId) {
        if (!isValidBlockId(blockId)) {
            return "Unknown Block";
        }

        String normalized = normalizeBlockId(blockId);
        Identifier id = Identifier.tryParse(normalized);

        if (id != null) {
            Optional<Block> block = Registries.BLOCK.getOrEmpty(id);
            if (block.isPresent()) {
                // Get translation key and try to make it readable
                String key = block.get().getTranslationKey();
                if (key.contains(".")) {
                    String[] parts = key.split("\\.");
                    if (parts.length > 0) {
                        String lastPart = parts[parts.length - 1];
                        return formatDisplayName(lastPart);
                    }
                }
            }
        }

        // Fallback: format the block ID
        return formatDisplayName(blockId.replace("minecraft:", "").replace(":", "_"));
    }

    /**
     * Formats a string for display (e.g., "diamond_ore" -> "Diamond Ore")
     */
    public static String formatDisplayName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Checks if a block at a position is still valid (not air, not replaced)
     */
    public static boolean isBlockStillValid(World world, BlockPos pos, String expectedBlockId) {
        if (world == null || pos == null || expectedBlockId == null) {
            return false;
        }

        BlockState currentState = world.getBlockState(pos);

        // Check if block is air (was removed)
        if (currentState.isAir()) {
            return false;
        }

        // Check if it's still the expected block
        String currentBlockId = currentState.getBlock().getRegistryEntry().registryKey().getValue().toString();
        return currentBlockId.equals(expectedBlockId);
    }

    /**
     * Gets all valid block IDs from a list, filtering out invalid ones
     */
    public static Set<String> filterValidBlockIds(Collection<String> blockIds) {
        Set<String> validBlocks = new HashSet<>();

        for (String blockId : blockIds) {
            if (isValidBlockId(blockId)) {
                validBlocks.add(normalizeBlockId(blockId));
            } else {
                System.err.println("[Neoporium] Invalid block ID ignored: " + blockId);
            }
        }

        return validBlocks;
    }

    /**
     * Gets block category (ore, container, etc.)
     */
    public static String getBlockCategory(String blockId) {
        if (blockId == null) {
            return "unknown";
        }

        String normalized = normalizeBlockId(blockId);

        // Check for ores
        if (normalized.contains("_ore") || normalized.contains("ancient_debris")) {
            return "ore";
        }

        // Check for containers
        if (normalized.contains("chest") || normalized.contains("barrel") || normalized.contains("shulker")) {
            return "container";
        }

        // Check for spawners
        if (normalized.contains("spawner")) {
            return "spawner";
        }

        // Check for valuable blocks
        if (normalized.contains("_block") &&
                (normalized.contains("diamond") || normalized.contains("gold") ||
                        normalized.contains("iron") || normalized.contains("emerald") ||
                        normalized.contains("netherite") || normalized.contains("lapis") ||
                        normalized.contains("redstone") || normalized.contains("coal"))) {
            return "valuable";
        }

        return "other";
    }

    /**
     * Gets recommended color for a block based on its type
     */
    public static com.emperium.neoporiumscanner.xray.BasicColor getRecommendedColor(String blockId) {
        String category = getBlockCategory(blockId);

        return switch (category) {
            case "ore" -> {
                if (blockId.contains("diamond")) yield new com.emperium.neoporiumscanner.xray.BasicColor(0, 200, 255, 200);
                if (blockId.contains("gold")) yield new com.emperium.neoporiumscanner.xray.BasicColor(255, 215, 0, 200);
                if (blockId.contains("iron")) yield new com.emperium.neoporiumscanner.xray.BasicColor(200, 200, 200, 200);
                if (blockId.contains("emerald")) yield new com.emperium.neoporiumscanner.xray.BasicColor(0, 200, 0, 200);
                if (blockId.contains("redstone")) yield new com.emperium.neoporiumscanner.xray.BasicColor(255, 0, 0, 200);
                if (blockId.contains("lapis")) yield new com.emperium.neoporiumscanner.xray.BasicColor(0, 100, 200, 200);
                if (blockId.contains("coal")) yield new com.emperium.neoporiumscanner.xray.BasicColor(50, 50, 50, 200);
                if (blockId.contains("copper")) yield new com.emperium.neoporiumscanner.xray.BasicColor(184, 115, 51, 200);
                if (blockId.contains("ancient_debris")) yield new com.emperium.neoporiumscanner.xray.BasicColor(100, 50, 50, 200);
                yield new com.emperium.neoporiumscanner.xray.BasicColor(255, 165, 0, 200); // Orange for other ores
            }
            case "container" -> new com.emperium.neoporiumscanner.xray.BasicColor(200, 150, 0, 200);
            case "spawner" -> new com.emperium.neoporiumscanner.xray.BasicColor(150, 0, 150, 200);
            case "valuable" -> new com.emperium.neoporiumscanner.xray.BasicColor(255, 255, 100, 200);
            default -> new com.emperium.neoporiumscanner.xray.BasicColor(200, 200, 200, 200);
        };
    }
}