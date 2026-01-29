package com.emperium.neoporiumscanner.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class AdvancedConfig {
    private static AdvancedConfig instance;
    private static final File CONFIG_FILE = new File("config/neoporiumscanner/config.json");

    // Configuration options
    public boolean autoScan = false;
    public int autoScanInterval = 60; // seconds
    public boolean showHudOverlay = true;
    public boolean saveLogs = true;
    public String logDirectory = "neoporium_logs";
    public boolean enableSounds = true;
    public boolean highlightBlocks = true;
    public int maxRenderDistance = 64;
    public String defaultProfile = "default";
    public boolean chunkBasedScanning = true;
    public boolean verifyBlocksExist = true;
    public boolean logOnlyValidBlocks = true;

    private AdvancedConfig() {}

    public static AdvancedConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void init() {
        get();
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try {
                String content = new String(Files.readAllBytes(CONFIG_FILE.toPath()));
                Gson gson = new Gson();
                instance = gson.fromJson(content, AdvancedConfig.class);
            } catch (IOException e) {
                System.err.println("[Neoporium] Error loading config: " + e.getMessage());
                instance = new AdvancedConfig();
                save();
            }
        } else {
            instance = new AdvancedConfig();
            save();
        }
    }

    public static void save() {
        try {
            Path configDir = CONFIG_FILE.getParentFile().toPath();
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(instance);
            Files.writeString(CONFIG_FILE.toPath(), json);
        } catch (IOException e) {
            System.err.println("[Neoporium] Error saving config: " + e.getMessage());
        }
    }

    public void resetToDefaults() {
        autoScan = false;
        autoScanInterval = 60;
        showHudOverlay = true;
        saveLogs = true;
        logDirectory = "neoporium_logs";
        enableSounds = true;
        highlightBlocks = true;
        maxRenderDistance = 64;
        defaultProfile = "default";
        chunkBasedScanning = true;
        verifyBlocksExist = true;
        logOnlyValidBlocks = true;
    }
}