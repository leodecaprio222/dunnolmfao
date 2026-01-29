package com.emperium.neoporiumscanner.core;

import com.emperium.neoporiumscanner.config.AdvancedConfig;
import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import java.util.*;

public class AdvancedScanner {
    private static AdvancedScanner instance;
    private final Map<String, ScanProfile> profiles = new HashMap<>();
    private ScanProfile currentProfile;
    private final Map<String, List<BlockPos>> foundBlocks = new HashMap<>();
    private ChunkPos lastScanChunk;
    private long lastScanTime = 0;
    private boolean isScanning = false;

    private AdvancedScanner() {
        loadProfiles();
        if (currentProfile == null) {
            currentProfile = new ScanProfile("default");
            profiles.put("default", currentProfile);
            saveProfile("default");
        }
    }

    public static AdvancedScanner init() {
        if (instance == null) {
            instance = new AdvancedScanner();
        }
        return instance;
    }

    public static AdvancedScanner getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    private void loadProfiles() {
        profiles.clear();

        ScanProfile defaultProfile = new ScanProfile("default");
        profiles.put("default", defaultProfile);

        ScanProfile oresProfile = new ScanProfile("ores");
        oresProfile.targetBlocks.addAll(Arrays.asList(
                "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
                "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
                "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
                "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore"
        ));
        profiles.put("ores", oresProfile);

        ScanProfile chestsProfile = new ScanProfile("chests");
        chestsProfile.targetBlocks.addAll(Arrays.asList(
                "minecraft:chest", "minecraft:trapped_chest",
                "minecraft:ender_chest", "minecraft:barrel"
        ));
        profiles.put("chests", chestsProfile);

        currentProfile = defaultProfile;
    }

    public Map<String, ScanProfile> getProfiles() {
        return new HashMap<>(profiles);
    }

    public ScanProfile getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(String profileName) {
        if (profiles.containsKey(profileName)) {
            currentProfile = profiles.get(profileName);
            StateSettings.getInstance().setCurrentProfile(profileName);
            StateSettings.getInstance().setActiveBlocks(currentProfile.targetBlocks);

            XRayRenderer.updateForProfile(currentProfile);
        }
    }

    public void addProfile(ScanProfile profile) {
        profiles.put(profile.name, profile);
    }

    public void saveProfile(String profileName) {
        if (profiles.containsKey(profileName)) {
            profiles.get(profileName).save();
        }
    }

    public void scanCurrentChunk(World world, PlayerEntity player) {
        if (isScanning) return;

        isScanning = true;
        try {
            ChunkPos chunkPos = player.getChunkPos();
            int[] yRange = currentProfile.getYRange();

            System.out.println("[Neoporium] Scanning chunk at " + chunkPos + ", Y: " + yRange[0] + " to " + yRange[1]);

            clearChunkResults(chunkPos);

            int blocksFound = scanChunk(world, chunkPos, yRange[0], yRange[1]);

            XRayRenderer.updateForCurrentChunk(world, player.getBlockPos());

            if (blocksFound > 0) {
                LogManager.getInstance().logScanSummary(blocksFound, 1, currentProfile.name);
                System.out.println("[Neoporium] Found " + blocksFound + " blocks in current chunk");
            }

        } finally {
            isScanning = false;
        }
    }

    public void scanArea(World world, PlayerEntity player) {
        if (isScanning) return;

        isScanning = true;
        try {
            ChunkPos centerChunk = player.getChunkPos();
            int[] yRange = currentProfile.getYRange();
            int radius = currentProfile.scanRadius;

            System.out.println("[Neoporium] Scanning area: radius=" + radius + " chunks, Y: " + yRange[0] + " to " + yRange[1]);

            int totalBlocks = 0;
            int chunksScanned = 0;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    ChunkPos chunkPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                    if (world.getChunk(chunkPos.x, chunkPos.z) != null) {
                        int blocksFound = scanChunk(world, chunkPos, yRange[0], yRange[1]);
                        totalBlocks += blocksFound;
                        chunksScanned++;
                    }
                }
            }

            XRayRenderer.updateForCurrentChunk(world, player.getBlockPos());

            if (totalBlocks > 0) {
                LogManager.getInstance().logScanSummary(totalBlocks, chunksScanned, currentProfile.name);
                System.out.println("[Neoporium] Found " + totalBlocks + " blocks in " + chunksScanned + " chunks");
            }

        } finally {
            isScanning = false;
        }
    }

    private int scanChunk(World world, ChunkPos chunkPos, int minY, int maxY) {
        int blocksFound = 0;
        int chunkStartX = chunkPos.getStartX();
        int chunkStartZ = chunkPos.getStartZ();

        boolean singleLayer = (minY == maxY);

        for (int x = chunkStartX; x < chunkStartX + 16; x++) {
            for (int z = chunkStartZ; z < chunkStartZ + 16; z++) {
                if (singleLayer) {
                    BlockPos pos = new BlockPos(x, minY, z);
                    checkAndLogBlock(world, pos);
                    if (foundBlocks.containsKey(getBlockTypeAt(world, pos))) {
                        blocksFound++;
                    }
                } else {
                    for (int y = minY; y <= maxY; y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        checkAndLogBlock(world, pos);
                        if (foundBlocks.containsKey(getBlockTypeAt(world, pos))) {
                            blocksFound++;
                        }
                    }
                }
            }
        }

        lastScanChunk = chunkPos;
        lastScanTime = System.currentTimeMillis();

        return blocksFound;
    }

    private void checkAndLogBlock(World world, BlockPos pos) {
        String blockType = getBlockTypeAt(world, pos);

        if (currentProfile.targetBlocks.contains(blockType)) {
            if (!world.getBlockState(pos).isAir()) {
                foundBlocks.computeIfAbsent(blockType, k -> new ArrayList<>()).add(pos);

                if (AdvancedConfig.get().saveLogs) {
                    LogManager.getInstance().logBlockFound(pos, blockType, world);
                }
            }
        }
    }

    private String getBlockTypeAt(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().getRegistryEntry().registryKey().getValue().toString();
    }

    private void clearChunkResults(ChunkPos chunkPos) {
        for (List<BlockPos> blockList : foundBlocks.values()) {
            blockList.removeIf(pos -> {
                ChunkPos blockChunk = new ChunkPos(pos);
                return blockChunk.equals(chunkPos);
            });
        }

        foundBlocks.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public Map<String, List<BlockPos>> getFoundBlocks() {
        return new HashMap<>(foundBlocks);
    }

    public void clearScanResults() {
        foundBlocks.clear();
        XRayRenderer.clearBlocks();
    }

    public void cycleNextProfile() {
        List<String> profileNames = new ArrayList<>(profiles.keySet());
        int currentIndex = profileNames.indexOf(currentProfile.name);
        int nextIndex = (currentIndex + 1) % profileNames.size();
        setCurrentProfile(profileNames.get(nextIndex));
    }

    // Updated method to remove Minecraft client dependency
    public boolean shouldAutoScan() {
        AdvancedConfig config = AdvancedConfig.get();
        if (!config.autoScan) return false;

        // We'll check this in ClientTickHandler instead
        // This method now just returns config.autoScan status
        return config.autoScan;
    }

    // New method to update found blocks from ScanTask
    public void updateFoundBlocks(Map<String, List<BlockPos>> newBlocks) {
        for (Map.Entry<String, List<BlockPos>> entry : newBlocks.entrySet()) {
            foundBlocks.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                    .addAll(entry.getValue());
        }
    }
}