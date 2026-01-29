package com.emperium.neoporiumscanner.config;

public class StateSettings {
    private static boolean scanning = false;
    private static boolean paused = false;
    private static boolean guiVisible = false;

    public static boolean isScanning() {
        return scanning;
    }

    public static void setScanning(boolean scanning) {
        StateSettings.scanning = scanning;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static void setPaused(boolean paused) {
        StateSettings.paused = paused;
    }

    public static boolean isGuiVisible() {
        return guiVisible;
    }

    public static void setGuiVisible(boolean guiVisible) {
        StateSettings.guiVisible = guiVisible;
    }
}