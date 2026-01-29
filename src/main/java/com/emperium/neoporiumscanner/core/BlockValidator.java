package com.emperium.neoporiumscanner.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import java.util.HashSet;
import java.util.Set;

public class BlockValidator {
    private static final Set<String> BLACKLISTED_BLOCKS = new HashSet<>();

    static {
        // Add common non-ore blocks to blacklist
        BLACKLISTED_BLOCKS.add(Blocks.STONE.getTranslationKey());
        BLACKLISTED_BLOCKS.add(Blocks.DIRT.getTranslationKey());
        BLACKLISTED_BLOCKS.add(Blocks.GRASS_BLOCK.getTranslationKey());
        BLACKLISTED_BLOCKS.add(Blocks.SAND.getTranslationKey());
        BLACKLISTED_BLOCKS.add(Blocks.GRAVEL.getTranslationKey());
        BLACKLISTED_BLOCKS.add(Blocks.BEDROCK.getTranslationKey());
    }

    public static boolean isValidBlock(BlockState state) {
        if (state.isAir()) return false;

        Block block = state.getBlock();
        String blockId = Registries.BLOCK.getId(block).toString();
        String translationKey = block.getTranslationKey();

        // Check if it's blacklisted
        if (BLACKLISTED_BLOCKS.contains(translationKey)) {
            return false;
        }

        // Check if it's an ore (common ore patterns)
        return blockId.contains("ore") ||
                blockId.contains("diamond") ||
                blockId.contains("emerald") ||
                blockId.contains("gold") ||
                blockId.contains("iron") ||
                blockId.contains("coal") ||
                blockId.contains("copper") ||
                blockId.contains("lapis") ||
                blockId.contains("redstone") ||
                blockId.contains("ancient_debris");
    }

    public static void addToBlacklist(String blockId) {
        BLACKLISTED_BLOCKS.add(blockId);
    }

    public static void removeFromBlacklist(String blockId) {
        BLACKLISTED_BLOCKS.remove(blockId);
    }
}