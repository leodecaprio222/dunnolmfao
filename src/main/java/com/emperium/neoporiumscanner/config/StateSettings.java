package com.emperium.neoporiumscanner.config;

public class StateSettings {
    private static boolean xrayEnabled = false;
    private static boolean scanningActive = false;
    private static int scanRadius = 8;
    private static int maxBlocksPerTick = 100;

    public static boolean isXrayEnabled() {
        return xrayEnabled;
    }

    public static void setXrayEnabled(boolean enabled) {
        xrayEnabled = enabled;
    }

    public static void toggleXray() {
        xrayEnabled = !xrayEnabled;
    }

    public static boolean isScanningActive() {
        return scanningActive;
    }

    public static void setScanningActive(boolean active) {
        scanningActive = active;
    }

    public static int getScanRadius() {
        return scanRadius;
    }

    public static void setScanRadius(int radius) {
        scanRadius = Math.max(1, Math.min(radius, 16));
    }

    public static int getMaxBlocksPerTick() {
        return maxBlocksPerTick;
    }

    public static void setMaxBlocksPerTick(int max) {
        maxBlocksPerTick = Math.max(10, Math.min(max, 1000));
    }
}