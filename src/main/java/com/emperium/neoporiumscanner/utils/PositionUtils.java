package com.emperium.neoporiumscanner.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import java.util.*;

public class PositionUtils {

    public static ChunkPos blockPosToChunkPos(BlockPos pos) {
        return new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static BlockPos chunkPosToBlockPos(ChunkPos chunkPos, int y) {
        return new BlockPos(chunkPos.getStartX(), y, chunkPos.getStartZ());
    }

    public static int getChunkDistance(ChunkPos pos1, ChunkPos pos2) {
        int dx = Math.abs(pos1.x - pos2.x);
        int dz = Math.abs(pos1.z - pos2.z);
        return Math.max(dx, dz);
    }

    public static double getDistance(BlockPos pos1, BlockPos pos2) {
        double dx = pos1.getX() - pos2.getX();
        double dy = pos1.getY() - pos2.getY();
        double dz = pos1.getZ() - pos2.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static double getDistance(BlockPos pos, Vec3d vec) {
        double dx = pos.getX() - vec.x;
        double dy = pos.getY() - vec.y;
        double dz = pos.getZ() - vec.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static List<ChunkPos> getChunksInRadius(ChunkPos center, int radius) {
        List<ChunkPos> chunks = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                chunks.add(new ChunkPos(center.x + dx, center.z + dz));
            }
        }

        return chunks;
    }

    public static List<BlockPos> getBlocksInRadius(BlockPos center, int radius, int minY, int maxY) {
        List<BlockPos> blocks = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    blocks.add(center.add(dx, y - center.getY(), dz));
                }
            }
        }

        return blocks;
    }

    public static BlockPos getNearestBlock(List<BlockPos> blocks, BlockPos target) {
        if (blocks.isEmpty()) {
            return null;
        }

        BlockPos nearest = blocks.get(0);
        double nearestDistance = getDistance(nearest, target);

        for (int i = 1; i < blocks.size(); i++) {
            BlockPos pos = blocks.get(i);
            double distance = getDistance(pos, target);

            if (distance < nearestDistance) {
                nearest = pos;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    public static String formatPosition(BlockPos pos) {
        return String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
    }

    public static String formatPositionShort(BlockPos pos) {
        return String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos parsePosition(String str) {
        try {
            String[] parts = str.trim().split("\\s+");
            if (parts.length >= 3) {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);
                return new BlockPos(x, y, z);
            }
        } catch (NumberFormatException e) {
            // Invalid format
        }
        return null;
    }

    public static boolean isWithinRenderDistance(BlockPos pos, Vec3d cameraPos, double maxDistance) {
        return getDistance(pos, cameraPos) <= maxDistance;
    }

    public static int getManhattanDistance(BlockPos pos1, BlockPos pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) +
                Math.abs(pos1.getY() - pos2.getY()) +
                Math.abs(pos1.getZ() - pos2.getZ());
    }
}