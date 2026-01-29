package com.emperium.neoporiumscanner.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockUtils {
    public static String getBlockId(BlockState state) {
        return Registries.BLOCK.getId(state.getBlock()).toString();
    }

    public static String getBlockId(Block block) {
        return Registries.BLOCK.getId(block).toString();
    }

    public static Block getBlockFromId(String blockId) {
        // FIXED: Use Identifier.ofVanilla() for 1.21.4
        return Registries.BLOCK.get(Identifier.ofVanilla(blockId));
    }

    public static boolean isOre(BlockState state) {
        String blockId = getBlockId(state);
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
}