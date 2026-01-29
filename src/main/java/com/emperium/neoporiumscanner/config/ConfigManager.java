package com.emperium.neoporiumscanner.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("neoporium-scanner.json");

    // XRay settings
    private static boolean xrayEnabled = false;
    private static float xrayOpacity = 0.5f;
    private static boolean xraySeeThrough = true;
    private static int xrayDistance = 64;

    // ESP settings
    private static boolean espEnabled = false;
    private static String espMode = "BOX"; // BOX, WIREFRAME, BOTH
    private static float espThickness = 2.0f;
    private static boolean espFadeEnabled = true;
    private static int espDistance = 128;
    private static Map<String, int[]> espColors = new HashMap<>();

    // General settings
    private static int scanRange = 96;
    private static Set<String> trackedBlocks = new HashSet<>();

    static {
        // Default ESP colors
        espColors.put("minecraft:diamond_ore", new int[]{85, 255, 255}); // Cyan
        espColors.put("minecraft:emerald_ore", new int[]{0, 255, 0}); // Green
        espColors.put("minecraft:gold_ore", new int[]{255, 255, 0}); // Yellow
        espColors.put("minecraft:iron_ore", new int[]{255, 165, 0}); // Orange
        espColors.put("minecraft:coal_ore", new int[]{64, 64, 64}); // Dark Gray
        espColors.put("minecraft:chest", new int[]{255, 215, 0}); // Gold
        espColors.put("minecraft:ender_chest", new int[]{128, 0, 128}); // Purple
        espColors.put("minecraft:spawner", new int[]{255, 0, 0}); // Red

        // Default tracked blocks
        trackedBlocks.addAll(espColors.keySet());
    }

    public static void load() {
        if (!CONFIG_PATH.toFile().exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
            Map<String, Object> config = GSON.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());

            if (config != null) {
                xrayEnabled = getBoolean(config, "xrayEnabled", xrayEnabled);
                xrayOpacity = getFloat(config, "xrayOpacity", xrayOpacity);
                xraySeeThrough = getBoolean(config, "xraySeeThrough", xraySeeThrough);
                xrayDistance = getInt(config, "xrayDistance", xrayDistance);

                espEnabled = getBoolean(config, "espEnabled", espEnabled);
                espMode = getString(config, "espMode", espMode);
                espThickness = getFloat(config, "espThickness", espThickness);
                espFadeEnabled = getBoolean(config, "espFadeEnabled", espFadeEnabled);
                espDistance = getInt(config, "espDistance", espDistance);

                if (config.containsKey("espColors")) {
                    try {
                        espColors = GSON.fromJson(GSON.toJson(config.get("espColors")),
                                new TypeToken<Map<String, int[]>>(){}.getType());
                    } catch (Exception e) {
                        System.err.println("Failed to load ESP colors: " + e.getMessage());
                    }
                }

                scanRange = getInt(config, "scanRange", scanRange);

                if (config.containsKey("trackedBlocks")) {
                    try {
                        trackedBlocks = GSON.fromJson(GSON.toJson(config.get("trackedBlocks")),
                                new TypeToken<Set<String>>(){}.getType());
                    } catch (Exception e) {
                        System.err.println("Failed to load tracked blocks: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        Map<String, Object> config = new HashMap<>();

        // XRay settings
        config.put("xrayEnabled", xrayEnabled);
        config.put("xrayOpacity", xrayOpacity);
        config.put("xraySeeThrough", xraySeeThrough);
        config.put("xrayDistance", xrayDistance);

        // ESP settings
        config.put("espEnabled", espEnabled);
        config.put("espMode", espMode);
        config.put("espThickness", espThickness);
        config.put("espFadeEnabled", espFadeEnabled);
        config.put("espDistance", espDistance);
        config.put("espColors", espColors);

        // General settings
        config.put("scanRange", scanRange);
        config.put("trackedBlocks", trackedBlocks);

        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    // Helper methods
    private static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    private static int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private static float getFloat(Map<String, Object> map, String key, float defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return defaultValue;
    }

    private static String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value instanceof String ? (String) value : defaultValue;
    }

    // XRay Getters and Setters
    public static boolean isXRayEnabled() { return xrayEnabled; }
    public static void setXRayEnabled(boolean enabled) { xrayEnabled = enabled; save(); }

    public static float getXRayOpacity() { return xrayOpacity; }
    public static void setXRayOpacity(float opacity) { xrayOpacity = Math.max(0, Math.min(1, opacity)); save(); }

    public static boolean isXRaySeeThrough() { return xraySeeThrough; }
    public static void setXRaySeeThrough(boolean seeThrough) { xraySeeThrough = seeThrough; save(); }

    public static int getXRayDistance() { return xrayDistance; }
    public static void setXRayDistance(int distance) { xrayDistance = Math.max(1, Math.min(512, distance)); save(); }

    // ESP Getters and Setters
    public static boolean isESPEnabled() { return espEnabled; }
    public static void setESPEnabled(boolean enabled) { espEnabled = enabled; save(); }

    public static String getESPMode() { return espMode; }
    public static void setESPMode(String mode) {
        if (mode.equals("BOX") || mode.equals("WIREFRAME") || mode.equals("BOTH")) {
            espMode = mode;
            save();
        }
    }

    public static float getESPThickness() { return espThickness; }
    public static void setESPThickness(float thickness) { espThickness = Math.max(0.1f, Math.min(10f, thickness)); save(); }

    public static boolean isESPFadeEnabled() { return espFadeEnabled; }
    public static void setESPFadeEnabled(boolean enabled) { espFadeEnabled = enabled; save(); }

    public static int getESPDistance() { return espDistance; }
    public static void setESPDistance(int distance) { espDistance = Math.max(1, Math.min(512, distance)); save(); }

    public static Map<String, int[]> getESPColors() { return new HashMap<>(espColors); }
    public static void setESPColor(String blockId, int r, int g, int b) {
        espColors.put(blockId, new int[]{r, g, b});
        save();
    }
    public static void removeESPColor(String blockId) {
        espColors.remove(blockId);
        save();
    }

    // General Getters and Setters
    public static int getScanRange() { return scanRange; }
    public static void setScanRange(int range) { scanRange = Math.max(1, Math.min(512, range)); save(); }

    public static Set<String> getTrackedBlocks() { return new HashSet<>(trackedBlocks); }
    public static void addTrackedBlock(String blockId) { trackedBlocks.add(blockId); save(); }
    public static void removeTrackedBlock(String blockId) { trackedBlocks.remove(blockId); save(); }
    public static void clearTrackedBlocks() { trackedBlocks.clear(); save(); }
}