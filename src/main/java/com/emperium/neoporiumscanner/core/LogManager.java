package com.emperium.neoporiumscanner.core;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogManager {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final File LOG_DIR = new File("logs/neoporium-scanner");
    private static File currentLogFile;
    private static PrintWriter writer;

    static {
        LOG_DIR.mkdirs();
    }

    public static void startNewLog() {
        closeLog();

        String timestamp = DATE_FORMAT.format(new Date());
        currentLogFile = new File(LOG_DIR, "scan_" + timestamp + ".log");

        try {
            writer = new PrintWriter(new FileWriter(currentLogFile, true));
            writer.println("=== Neoporium Scanner Log ===");
            writer.println("Started: " + new Date());
            writer.println("=============================");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logBlockFound(BlockPos pos, BlockState state) {
        if (writer != null) {
            String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
            writer.printf("[%s] Found %s at %s\n",
                    new Date(),
                    blockId,
                    formatPosition(pos)
            );
            writer.flush();
        }
    }

    private static String formatPosition(BlockPos pos) {
        return String.format("X: %d, Y: %d, Z: %d", pos.getX(), pos.getY(), pos.getZ());
    }

    public static void logMessage(String message) {
        if (writer != null) {
            writer.printf("[%s] %s\n", new Date(), message);
            writer.flush();
        }
    }

    public static void closeLog() {
        if (writer != null) {
            writer.println("=============================");
            writer.println("Ended: " + new Date());
            writer.println("=============================");
            writer.close();
            writer = null;
        }
    }

    public static File getCurrentLogFile() {
        return currentLogFile;
    }
}