package com.emperium.neoporiumscanner.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

public class ScanProfile {
    public String name;
    public String yRangeMode; // "bedrock", "diamond", "full", "custom"
    public String scanMode; // "radius", "single_chunk"
    public int minY;
    public int maxY;
    public int customMinY;
    public int customMaxY;
    public int scanRadius; // 0-16 chunks
    public int targetChunks; // For area scanning
    public Set<String> targetBlocks = new HashSet<>();
    public boolean logToFile = true;
    public String logFormat = "X Y Z BlockType";
    public boolean useYRange = true;
    public boolean useChunksMode = false;

    public ScanProfile(String name) {
        this.name = name;
        this.yRangeMode = "bedrock";
        this.scanMode = "radius";
        this.minY = -64;
        this.maxY = 5;
        this.customMinY = -64;
        this.customMaxY = 5;
        this.scanRadius = 3;
        this.targetChunks = 1;
        this.useYRange = true;
        this.useChunksMode = false;

        // Default target blocks
        targetBlocks.add("minecraft:diamond_ore");
        targetBlocks.add("minecraft:deepslate_diamond_ore");
    }

    public int[] getYRange() {
        switch (yRangeMode) {
            case "bedrock":
                return new int[]{-64, 5};
            case "diamond":
                return new int[]{-59, -59};
            case "full":
                return new int[]{-64, 320};
            case "custom":
                return new int[]{customMinY, customMaxY};
            default:
                return new int[]{-64, 5};
        }
    }

    public boolean isSingleLayer() {
        return yRangeMode.equals("diamond") || (minY == maxY);
    }

    public void save() {
        try {
            File profilesDir = new File("config/neoporiumscanner/profiles");
            if (!profilesDir.exists()) {
                profilesDir.mkdirs();
            }

            File profileFile = new File(profilesDir, name + ".json");

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ProfileData data = new ProfileData(this);

            try (FileWriter writer = new FileWriter(profileFile)) {
                gson.toJson(data, writer);
            }

        } catch (IOException e) {
            System.err.println("[Neoporium] Error saving profile '" + name + "': " + e.getMessage());
        }
    }

    public static ScanProfile load(String name) {
        try {
            File profileFile = new File("config/neoporiumscanner/profiles", name + ".json");
            if (!profileFile.exists()) {
                return new ScanProfile(name);
            }

            Gson gson = new Gson();
            try (FileReader reader = new FileReader(profileFile)) {
                ProfileData data = gson.fromJson(reader, ProfileData.class);
                return data.toScanProfile();
            }

        } catch (Exception e) {
            System.err.println("[Neoporium] Error loading profile '" + name + "': " + e.getMessage());
            return new ScanProfile(name);
        }
    }

    // Helper class for JSON serialization
    private static class ProfileData {
        String name;
        String yRangeMode;
        String scanMode;
        int minY;
        int maxY;
        int customMinY;
        int customMaxY;
        int scanRadius;
        int targetChunks;
        Set<String> targetBlocks;
        boolean logToFile;
        String logFormat;
        boolean useYRange;
        boolean useChunksMode;

        ProfileData(ScanProfile profile) {
            this.name = profile.name;
            this.yRangeMode = profile.yRangeMode;
            this.scanMode = profile.scanMode;
            this.minY = profile.minY;
            this.maxY = profile.maxY;
            this.customMinY = profile.customMinY;
            this.customMaxY = profile.customMaxY;
            this.scanRadius = profile.scanRadius;
            this.targetChunks = profile.targetChunks;
            this.targetBlocks = profile.targetBlocks;
            this.logToFile = profile.logToFile;
            this.logFormat = profile.logFormat;
            this.useYRange = profile.useYRange;
            this.useChunksMode = profile.useChunksMode;
        }

        ScanProfile toScanProfile() {
            ScanProfile profile = new ScanProfile(name);
            profile.yRangeMode = yRangeMode;
            profile.scanMode = scanMode;
            profile.minY = minY;
            profile.maxY = maxY;
            profile.customMinY = customMinY;
            profile.customMaxY = customMaxY;
            profile.scanRadius = scanRadius;
            profile.targetChunks = targetChunks;
            profile.targetBlocks = targetBlocks != null ? targetBlocks : new HashSet<>();
            profile.logToFile = logToFile;
            profile.logFormat = logFormat;
            profile.useYRange = useYRange;
            profile.useChunksMode = useChunksMode;
            return profile;
        }
    }

    @Override
    public String toString() {
        return "ScanProfile{" +
                "name='" + name + '\'' +
                ", yRangeMode='" + yRangeMode + '\'' +
                ", scanMode='" + scanMode + '\'' +
                ", minY=" + minY +
                ", maxY=" + maxY +
                ", blocks=" + targetBlocks.size() +
                '}';
    }
}