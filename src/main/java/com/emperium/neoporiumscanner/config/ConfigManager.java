package com.emperium.neoporiumscanner.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static ConfigManager instance;
    private static final File CONFIG_DIR = new File("config/neoporiumscanner");
    private final Gson gson;

    private ConfigManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        // Ensure config directory exists
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void saveConfig(String fileName, Object config) {
        try {
            File configFile = new File(CONFIG_DIR, fileName);
            String json = gson.toJson(config);
            Files.writeString(configFile.toPath(), json);
        } catch (IOException e) {
            System.err.println("[Neoporium] Failed to save config " + fileName + ": " + e.getMessage());
        }
    }

    public <T> T loadConfig(String fileName, Class<T> clazz, T defaultValue) {
        try {
            File configFile = new File(CONFIG_DIR, fileName);
            if (!configFile.exists()) {
                return defaultValue;
            }

            String json = Files.readString(configFile.toPath());
            return gson.fromJson(json, clazz);
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            System.err.println("[Neoporium] Failed to load config " + fileName + ": " + e.getMessage());
            return defaultValue;
        }
    }

    public boolean configExists(String fileName) {
        return new File(CONFIG_DIR, fileName).exists();
    }

    public void deleteConfig(String fileName) {
        File configFile = new File(CONFIG_DIR, fileName);
        if (configFile.exists()) {
            configFile.delete();
        }
    }

    public String[] listConfigs() {
        if (!CONFIG_DIR.exists()) {
            return new String[0];
        }

        File[] files = CONFIG_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return new String[0];
        }

        String[] configNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            configNames[i] = files[i].getName();
        }

        return configNames;
    }

    public File getConfigDirectory() {
        return CONFIG_DIR;
    }
}