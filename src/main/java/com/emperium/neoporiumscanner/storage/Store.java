package com.emperium.neoporiumscanner.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.emperium.neoporiumscanner.NeoporiumScanner;
import java.io.*;
import java.lang.reflect.Type;

public abstract class Store<T> {
    // FIXED: Use system property instead of Minecraft client
    private static final String CONFIG_PATH = String.format("%s/config/%s",
            System.getProperty("user.dir"), NeoporiumScanner.MOD_ID);

    private final String name;
    private final String file;
    public boolean justCreated = false;

    public Store(String name) {
        this.name = name;
        this.file = String.format("%s/%s.json", CONFIG_PATH, this.name);
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File configDir = new File(CONFIG_PATH);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public Gson getGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    public T read() {
        Gson gson = this.getGson();
        File configFile = new File(this.file);

        try {
            if (!configFile.exists()) {
                this.justCreated = true;
                T defaultValue = this.providedDefault();
                this.write(defaultValue);
                return defaultValue;
            }

            try (FileReader reader = new FileReader(configFile)) {
                return gson.fromJson(reader, this.getType());
            } catch (JsonIOException | JsonSyntaxException e) {
                System.err.println("[Neoporium] Fatal error with json loading on " + this.name + ".json");
                e.printStackTrace();
                return this.providedDefault();
            }
        } catch (FileNotFoundException e) {
            this.justCreated = true;
            T defaultValue = this.providedDefault();
            this.write(defaultValue);
            return defaultValue;
        } catch (IOException e) {
            System.err.println("[Neoporium] Error reading config file: " + e.getMessage());
            return this.providedDefault();
        }
    }

    public void write(T data) {
        Gson gson = this.getGson();

        try {
            try (FileWriter writer = new FileWriter(this.file)) {
                gson.toJson(data, writer);
                writer.flush();
            }
        } catch (IOException | JsonIOException e) {
            System.err.println("[Neoporium] Error writing config file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public abstract T providedDefault();
    public abstract T get();
    abstract Type getType();

    public String getFilePath() {
        return this.file;
    }

    public boolean exists() {
        return new File(this.file).exists();
    }

    public void delete() {
        File configFile = new File(this.file);
        if (configFile.exists()) {
            configFile.delete();
        }
    }

    public void resetToDefault() {
        T defaultValue = this.providedDefault();
        this.write(defaultValue);
    }
}