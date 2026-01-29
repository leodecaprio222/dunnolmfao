package com.emperium.neoporiumscanner.xray;

import net.minecraft.util.math.BlockPos;

public class BlockPosWithColor {
    private final BlockPos pos;
    private final BasicColor color;
    private final String blockId;
    private final long timestamp;

    public BlockPosWithColor(BlockPos pos, BasicColor color, String blockId) {
        this.pos = pos;
        this.color = color;
        this.blockId = blockId;
        this.timestamp = System.currentTimeMillis();
    }

    public BlockPos getPos() {
        return pos;
    }

    public BasicColor getColor() {
        return color;
    }

    public String getBlockId() {
        return blockId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public BlockPosWithColor withNewColor(BasicColor newColor) {
        return new BlockPosWithColor(pos, newColor, blockId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BlockPosWithColor that = (BlockPosWithColor) obj;
        return pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public String toString() {
        return "BlockPosWithColor{" +
                "pos=" + pos +
                ", color=" + color +
                ", blockId='" + blockId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}