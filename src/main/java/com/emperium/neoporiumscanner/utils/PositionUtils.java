package com.emperium.neoporiumscanner.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class PositionUtils {
    public static ChunkPos blockPosToChunkPos(BlockPos pos) {
        return new ChunkPos(pos);
    }

    public static BlockPos chunkPosToBlockPos(ChunkPos chunkPos, int x, int y, int z) {
        return new BlockPos(
                chunkPos.getStartX() + x,
                y,
                chunkPos.getStartZ() + z
        );
    }

    public static double distanceSquared(BlockPos pos1, BlockPos pos2) {
        double dx = pos1.getX() - pos2.getX();
        double dy = pos1.getY() - pos2.getY();
        double dz = pos1.getZ() - pos2.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}