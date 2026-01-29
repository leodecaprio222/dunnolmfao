package com.emperium.neoporiumscanner.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.*;

public class BlockUtils {

    public static String getBlockId(BlockState state) {
        Block block = state.getBlock();
        return block.getRegistryEntry().registryKey().getValue().toString();
    }

    public static String getBlockId(Block block) {
        return block.getRegistryEntry().registryKey().getValue().toString();
    }

    public static boolean isOreBlock(String blockId) {
        return blockId.contains("_ore") ||
                blockId.contains("ancient_debris") ||
                blockId.contains("nether_quartz") ||
                blockId.contains("nether_gold");
    }

    public static boolean isContainerBlock(String blockId) {
        return blockId.contains("chest") ||
                blockId.contains("barrel") ||
                blockId.contains("shulker") ||
                blockId.contains("hopper") ||
                blockId.contains("dispenser") ||
                blockId.contains("dropper");
    }

    public static boolean isValuableBlock(String blockId) {
        return blockId.contains("diamond") ||
                blockId.contains("emerald") ||
                blockId.contains("netherite") ||
                blockId.contains("gold_block") ||
                blockId.contains("iron_block") ||
                blockId.contains("lapis_block") ||
                blockId.contains("redstone_block") ||
                blockId.contains("coal_block") ||
                blockId.contains("copper_block");
    }

    public static String getBlockDisplayName(String blockId) {
        String name = blockId.replace("minecraft:", "");
        String[] parts = name.split("_");
        StringBuilder displayName = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                displayName.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return displayName.toString().trim();
    }

    public static String getBlockCategory(String blockId) {
        if (isOreBlock(blockId)) {
            return "ore";
        } else if (isContainerBlock(blockId)) {
            return "container";
        } else if (isValuableBlock(blockId)) {
            return "valuable";
        } else if (blockId.contains("spawner")) {
            return "spawner";
        } else if (blockId.contains("bedrock")) {
            return "bedrock";
        } else {
            return "other";
        }
    }

    public static boolean isBlockInRange(BlockPos pos, BlockPos center, int radius) {
        int dx = Math.abs(pos.getX() - center.getX());
        int dz = Math.abs(pos.getZ() - center.getZ());
        return dx <= radius && dz <= radius;
    }

    public static boolean isBlockInChunk(BlockPos pos, int chunkX, int chunkZ) {
        int blockChunkX = pos.getX() >> 4;
        int blockChunkZ = pos.getZ() >> 4;
        return blockChunkX == chunkX && blockChunkZ == chunkZ;
    }

    public static List<BlockPos> findBlocksInChunk(World world, int chunkX, int chunkZ,
                                                   int minY, int maxY, Set<String> targetBlocks) {
        List<BlockPos> foundBlocks = new ArrayList<>();
        int chunkStartX = chunkX << 4;
        int chunkStartZ = chunkZ << 4;

        for (int x = chunkStartX; x < chunkStartX + 16; x++) {
            for (int z = chunkStartZ; z < chunkStartZ + 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    String blockId = getBlockId(state);

                    if (targetBlocks.contains(blockId) && !state.isAir()) {
                        foundBlocks.add(pos);
                    }
                }
            }
        }

        return foundBlocks;
    }

    public static Map<String, List<BlockPos>> groupBlocksByType(List<BlockPos> blocks, World world) {
        Map<String, List<BlockPos>> grouped = new HashMap<>();

        for (BlockPos pos : blocks) {
            BlockState state = world.getBlockState(pos);
            String blockId = getBlockId(state);

            grouped.computeIfAbsent(blockId, k -> new ArrayList<>()).add(pos);
        }

        return grouped;
    }

    public static int countBlocksOfType(World world, BlockPos center, int radius, String blockId) {
        int count = 0;
        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = world.getBottomY(); y <= world.getTopY(); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    if (getBlockId(state).equals(blockId) && !state.isAir()) {
                        count++;
                    }
                }
            }
        }

        return count;
    }
}