package com.emperium.neoporiumscanner.config;

import com.emperium.neoporiumscanner.storage.BlockStore;
import com.emperium.neoporiumscanner.storage.SettingsStore;

public class ConfigManager {
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;

        BlockStore.getInstance().load();
        SettingsStore.getInstance().load();

        initialized = true;
    }

    public static void saveAll() {
        BlockStore.getInstance().save();
        SettingsStore.getInstance().save();
    }
}