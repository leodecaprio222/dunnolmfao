package com.emperium.neoporiumscanner.storage;

import com.emperium.neoporiumscanner.cache.BlockSearchCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.emperium.neoporiumscanner.core.ScanProfile;
import java.lang.reflect.Type;
import java.util.*;

public class BlockStore extends Store<List<ScanProfile>> {
    private static BlockStore instance;
    private final BlockSearchCache cache = new BlockSearchCache();
    private final List<ScanProfile> profiles;

    private BlockStore() {
        super("blocks");
        this.profiles = this.read();
        this.updateCache();
    }

    public static BlockStore getInstance() {
        if (instance == null) {
            instance = new BlockStore();
        }
        return instance;
    }

    public void updateCache() {
        this.cache.updateFromProfiles(this.profiles);
    }

    public BlockSearchCache getCache() {
        return this.cache;
    }

    @Override
    public List<ScanProfile> get() {
        return new ArrayList<>(this.profiles);
    }

    public ScanProfile getProfile(String name) {
        for (ScanProfile profile : this.profiles) {
            if (profile.name.equals(name)) {
                return profile;
            }
        }
        return null;
    }

    public void addProfile(ScanProfile profile) {
        this.profiles.removeIf(p -> p.name.equals(profile.name));
        this.profiles.add(profile);
        this.write(this.profiles);
        this.updateCache();
    }

    public void removeProfile(String name) {
        this.profiles.removeIf(profile -> profile.name.equals(name));
        this.write(this.profiles);
        this.updateCache();
    }

    public boolean hasProfile(String name) {
        return this.profiles.stream().anyMatch(profile -> profile.name.equals(name));
    }

    @Override
    public Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public List<ScanProfile> providedDefault() {
        List<ScanProfile> defaultProfiles = new ArrayList<>();

        // Default profile
        ScanProfile defaultProfile = new ScanProfile("default");
        defaultProfile.targetBlocks.add("minecraft:diamond_ore");
        defaultProfile.targetBlocks.add("minecraft:deepslate_diamond_ore");
        defaultProfile.targetBlocks.add("minecraft:iron_ore");
        defaultProfiles.add(defaultProfile);

        // Ores profile
        ScanProfile oresProfile = new ScanProfile("ores");
        oresProfile.targetBlocks.addAll(Arrays.asList(
                "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
                "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
                "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
                "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
                "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore",
                "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore",
                "minecraft:copper_ore", "minecraft:deepslate_copper_ore",
                "minecraft:coal_ore", "minecraft:deepslate_coal_ore"
        ));
        defaultProfiles.add(oresProfile);

        // Chests profile
        ScanProfile chestsProfile = new ScanProfile("chests");
        chestsProfile.targetBlocks.addAll(Arrays.asList(
                "minecraft:chest", "minecraft:trapped_chest",
                "minecraft:ender_chest", "minecraft:barrel"
        ));
        defaultProfiles.add(chestsProfile);

        return defaultProfiles;
    }

    @Override
    Type getType() {
        return new TypeToken<List<ScanProfile>>() {}.getType();
    }
}