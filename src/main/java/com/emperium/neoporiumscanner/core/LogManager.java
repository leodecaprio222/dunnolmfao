package com.emperium.neoporiumscanner.core;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogManager {
    private static LogManager instance;
    private File currentLogFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    private final SimpleDateFormat entryFormat = new SimpleDateFormat("HH:mm:ss");
    private int logCounter = 0;

    private LogManager() {
        createLogsDirectory();
    }

    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    private void createLogsDirectory() {
        File logsDir = new File("neoporium_logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
    }

    public void startNewLog(String profileName) {
        String timestamp = dateFormat.format(new Date());
        String fileName = String.format("scan_%s_%s_%03d.txt",
                profileName, timestamp, ++logCounter);
        currentLogFile = new File("neoporium_logs", fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(currentLogFile, true))) {
            writer.println("=========================================");
            writer.println("Neoporium Scanner Log");
            writer.println("Profile: " + profileName);
            writer.println("Started: " + new Date());
            writer.println("Format: X Y Z BlockType");
            writer.println("=========================================");
            writer.println();
        } catch (IOException e) {
            System.err.println("[Neoporium] Error creating log file: " + e.getMessage());
        }
    }

    public void logBlockFound(BlockPos pos, String blockType, World world) {
        if (currentLogFile == null) {
            startNewLog("default");
        }

        // Check if block is actually there (not air) - IMPORTANT!
        if (world.getBlockState(pos).isAir()) {
            return; // Don't log air blocks
        }

        // Format block type with proper capitalization
        String formattedBlockType = formatBlockType(blockType);

        // Format: X Y Z BlockType (exactly as you requested)
        String logEntry = String.format("%d %d %d %s",
                pos.getX(), pos.getY(), pos.getZ(), formattedBlockType);

        try (PrintWriter writer = new PrintWriter(new FileWriter(currentLogFile, true))) {
            writer.println(logEntry);
            writer.flush();
        } catch (IOException e) {
            System.err.println("[Neoporium] Error writing to log: " + e.getMessage());
        }

        // Console output for debugging
        System.out.println("[Neoporium] LOGGED: " + logEntry);
    }

    private String formatBlockType(String blockId) {
        // Convert "minecraft:diamond_ore" to "DiamondOre"
        String name = blockId.replace("minecraft:", "");
        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                // First letter uppercase, rest lowercase
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }

    public void logScanSummary(int blocksFound, int chunksScanned, String profileName) {
        if (currentLogFile == null) return;

        try (PrintWriter writer = new PrintWriter(new FileWriter(currentLogFile, true))) {
            writer.println();
            writer.println("=========================================");
            writer.println("Scan Summary");
            writer.println("Profile: " + profileName);
            writer.println("Time: " + new Date());
            writer.println("Blocks Found: " + blocksFound);
            writer.println("Chunks Scanned: " + chunksScanned);
            writer.println("=========================================");
            writer.flush();
        } catch (IOException e) {
            System.err.println("[Neoporium] Error writing scan summary: " + e.getMessage());
        }
    }

    public List<String> getRecentLogs(int count) {
        List<String> logs = new ArrayList<>();
        File logsDir = new File("neoporium_logs");

        if (logsDir.exists()) {
            File[] files = logsDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                // Sort by modification time (newest first)
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                for (int i = 0; i < Math.min(count, files.length); i++) {
                    logs.add(files[i].getName());
                }
            }
        }

        return logs;
    }

    public List<String> readLogFile(String fileName) {
        List<String> entries = new ArrayList<>();
        File logFile = new File("neoporium_logs", fileName);

        if (logFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    entries.add(line);
                }
            } catch (IOException e) {
                entries.add("Error reading log file: " + e.getMessage());
            }
        } else {
            entries.add("Log file not found: " + fileName);
        }

        return entries;
    }

    public List<String> searchInLogs(String searchTerm) {
        List<String> results = new ArrayList<>();
        File logsDir = new File("neoporium_logs");

        if (logsDir.exists()) {
            File[] files = logsDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        int lineNumber = 0;
                        while ((line = reader.readLine()) != null) {
                            lineNumber++;
                            if (line.toLowerCase().contains(searchTerm.toLowerCase())) {
                                results.add(file.getName() + ":" + lineNumber + " - " + line);
                            }
                        }
                    } catch (IOException e) {
                        results.add("Error reading " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        }

        return results;
    }

    public boolean exportLog(String fileName, String exportName) {
        try {
            File source = new File("neoporium_logs", fileName);
            File dest = new File("neoporium_logs", exportName);

            if (source.exists()) {
                Files.copy(source.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (Exception e) {
            System.err.println("[Neoporium] Error exporting log: " + e.getMessage());
        }
        return false;
    }

    public int deleteOldLogs(int keepLastDays) {
        int deleted = 0;
        File logsDir = new File("neoporium_logs");

        if (logsDir.exists()) {
            File[] files = logsDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                long cutoff = System.currentTimeMillis() - (keepLastDays * 24L * 60 * 60 * 1000);

                for (File file : files) {
                    if (file.lastModified() < cutoff) {
                        if (file.delete()) {
                            deleted++;
                        }
                    }
                }
            }
        }

        return deleted;
    }

    public String getCurrentLogFileName() {
        return currentLogFile != null ? currentLogFile.getName() : "No active log";
    }

    public int getCurrentLogSize() {
        return currentLogFile != null ? (int)currentLogFile.length() : 0;
    }
}