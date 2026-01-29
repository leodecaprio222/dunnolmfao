package com.emperium.neoporiumscanner.config;

import com.emperium.neoporiumscanner.xray.BasicColor;
import java.util.*;

public class StateSettings {
    private static StateSettings instance;

    private boolean isActive = false;
    private boolean showLava = false;
    private int range = 3;
    private boolean showOverlay = true;
    private String currentProfile = "default";
    private Set<String> activeBlocks = new HashSet<>();
    private Map<String, BasicColor> blockColors = new HashMap<>();

    private StateSettings() {
        // Default colors for common blocks
        setDefaultColors();
    }

    public static StateSettings getInstance() {
        if (instance == null) {
            instance = new StateSettings();
        }
        return instance;
    }

    private void setDefaultColors() {
        // Ore colors
        blockColors.put("minecraft:diamond_ore", new BasicColor(0, 200, 255, 200));
        blockColors.put("minecraft:deepslate_diamond_ore", new BasicColor(0, 150, 255, 200));
        blockColors.put("minecraft:iron_ore", new BasicColor(200, 200, 200, 200));
        blockColors.put("minecraft:gold_ore", new BasicColor(255, 215, 0, 200));
        blockColors.put("minecraft:copper_ore", new BasicColor(184, 115, 51, 200));
        blockColors.put("minecraft:coal_ore", new BasicColor(50, 50, 50, 200));
        blockColors.put("minecraft:emerald_ore", new BasicColor(0, 200, 0, 200));
        blockColors.put("minecraft:redstone_ore", new BasicColor(255, 0, 0, 200));
        blockColors.put("minecraft:lapis_ore", new BasicColor(0, 100, 200, 200));
        blockColors.put("minecraft:ancient_debris", new BasicColor(100, 50, 50, 200));

        // Container colors
        blockColors.put("minecraft:chest", new BasicColor(200, 150, 0, 200));
        blockColors.put("minecraft:ender_chest", new BasicColor(100, 0, 150, 200));
        blockColors.put("minecraft:spawner", new BasicColor(150, 0, 150, 200));
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isShowLava() {
        return this.showLava;
    }

    public void setShowLava(boolean showLava) {
        this.showLava = showLava;
    }

    public int getRange() {
        return Math.max(0, Math.min(16, this.range));
    }

    public void setRange(int range) {
        this.range = Math.max(0, Math.min(16, range));
    }

    public boolean showOverlay() {
        return this.showOverlay;
    }

    public void setShowOverlay(boolean showOverlay) {
        this.showOverlay = showOverlay;
    }

    public String getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(String profileName) {
        this.currentProfile = profileName;
    }

    public Set<String> getActiveBlocks() {
        return new HashSet<>(activeBlocks);
    }

    public void setActiveBlocks(Set<String> blocks) {
        this.activeBlocks.clear();
        this.activeBlocks.addAll(blocks);
    }

    public BasicColor getColor(String blockId) {
        return blockColors.getOrDefault(blockId, new BasicColor(255, 0, 0, 200));
    }

    public BasicColor getColor(String blockId, BasicColor defaultColor) {
        return blockColors.getOrDefault(blockId, defaultColor);
    }

    public void setColor(String blockId, BasicColor color) {
        blockColors.put(blockId, color);
    }

    public Map<String, BasicColor> getAllColors() {
        return new HashMap<>(blockColors);
    }

    public void increaseRange() {
        if (this.range < 16) {
            this.range++;
        } else {
            this.range = 0;
        }
    }

    public void decreaseRange() {
        if (this.range > 0) {
            this.range--;
        } else {
            this.range = 16;
        }
    }

    public int getVisualRadius() {
        return Math.max(1, getRange());
    }

    public static int getMaxStepsToScan() {
        return 16;
    }
}