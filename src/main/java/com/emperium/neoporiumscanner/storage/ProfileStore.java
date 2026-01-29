package com.emperium.neoporiumscanner.storage;

import com.emperium.neoporiumscanner.core.ScanProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;

public class ProfileStore extends Store<Map<String, ScanProfile>> {
    private static ProfileStore instance;
    private final Map<String, ScanProfile> profiles;

    private ProfileStore() {
        super("profiles");
        this.profiles = this.read();
    }

    public static ProfileStore getInstance() {
        if (instance == null) {
            instance = new ProfileStore();
        }
        return instance;
    }

    @Override
    public Map<String, ScanProfile> providedDefault() {
        Map<String, ScanProfile> defaultProfiles = new HashMap<>();

        ScanProfile defaultProfile = new ScanProfile("default");
        defaultProfile.targetBlocks.add("minecraft:diamond_ore");
        defaultProfile.targetBlocks.add("minecraft:deepslate_diamond_ore");
        defaultProfiles.put("default", defaultProfile);

        return defaultProfiles;
    }

    @Override
    public Map<String, ScanProfile> get() {
        return new HashMap<>(this.profiles);
    }

    @Override
    Type getType() {
        return new TypeToken<Map<String, ScanProfile>>() {}.getType();
    }

    @Override
    public Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public ScanProfile getProfile(String name) {
        return this.profiles.get(name);
    }

    public void saveProfile(ScanProfile profile) {
        this.profiles.put(profile.name, profile);
        this.write(this.profiles);
    }

    public void deleteProfile(String name) {
        this.profiles.remove(name);
        this.write(this.profiles);
    }

    public List<String> getProfileNames() {
        return new ArrayList<>(this.profiles.keySet());
    }

    public boolean profileExists(String name) {
        return this.profiles.containsKey(name);
    }

    public void importProfile(File file) {
        try {
            Gson gson = new Gson();
            ScanProfile profile = gson.fromJson(new java.io.FileReader(file), ScanProfile.class);
            if (profile != null && profile.name != null) {
                this.saveProfile(profile);
            }
        } catch (Exception e) {
            System.err.println("[Neoporium] Error importing profile: " + e.getMessage());
        }
    }

    public void exportProfile(String profileName, File destination) {
        ScanProfile profile = this.getProfile(profileName);
        if (profile != null) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(profile);
                java.nio.file.Files.writeString(destination.toPath(), json);
            } catch (Exception e) {
                System.err.println("[Neoporium] Error exporting profile: " + e.getMessage());
            }
        }
    }
}