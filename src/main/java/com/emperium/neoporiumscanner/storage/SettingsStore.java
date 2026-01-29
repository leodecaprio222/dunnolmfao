package com.emperium.neoporiumscanner.storage;

import com.emperium.neoporiumscanner.config.StateSettings;
import com.google.gson.*;
import java.io.*;

public class SettingsStore implements Store {
    private static SettingsStore instance;
    private final File configFile;

    private SettingsStore() {
        this.configFile = new File("config/neoporium-scanner/settings.json");
    }

    public static SettingsStore getInstance() {
        if (instance == null) {
            instance = new SettingsStore();
        }
        return instance;
    }

    @Override
    public void save() {
        try {
            configFile.getParentFile().mkdirs();

            JsonObject json = new JsonObject();
            json.addProperty("scanRadius", StateSettings.getScanRadius());
            json.addProperty("maxBlocksPerTick", StateSettings.getMaxBlocksPerTick());
            json.addProperty("xrayEnabled", StateSettings.isXrayEnabled());

            try (FileWriter writer = new FileWriter(configFile)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        if (!configFile.exists()) {
            save();
            return;
        }

        try {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);

                if (json.has("scanRadius")) {
                    StateSettings.setScanRadius(json.get("scanRadius").getAsInt());
                }
                if (json.has("maxBlocksPerTick")) {
                    StateSettings.setMaxBlocksPerTick(json.get("maxBlocksPerTick").getAsInt());
                }
                if (json.has("xrayEnabled")) {
                    StateSettings.setXrayEnabled(json.get("xrayEnabled").getAsBoolean());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}