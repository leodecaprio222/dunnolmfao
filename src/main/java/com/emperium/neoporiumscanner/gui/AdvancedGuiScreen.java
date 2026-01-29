package com.emperium.neoporiumscanner.gui;

import com.emperium.neoporiumscanner.config.AdvancedConfig;
import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.core.AdvancedScanner;
import com.emperium.neoporiumscanner.core.ScanProfile;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import java.util.*;

public class AdvancedGuiScreen extends Screen {
    private CyclingButtonWidget<String> profileSelector;
    private CyclingButtonWidget<String> yRangeSelector;
    private CyclingButtonWidget<String> scanModeSelector;
    private ButtonWidget blockSelectionButton;
    private ButtonWidget autoToggleButton;
    private ButtonWidget xrayToggleButton;
    private ButtonWidget xraySettingsButton;
    private ButtonWidget logManagerButton;
    private final List<TextFieldWidget> textFields = new ArrayList<>();
    private String yRangeMode = "bedrock";
    private String scanMode = "radius";
    private int customMinY = -64;
    private int customMaxY = 5;
    private int scanRadius = 3;
    private int targetChunks = 1;

    public AdvancedGuiScreen() {
        super(Text.literal("Neoporium Scanner"));
    }

    @Override
    protected void init() {
        super.init();

        AdvancedScanner.getInstance();
        ScanProfile profile = AdvancedScanner.getCurrentProfile();
        if (profile != null) {
            yRangeMode = profile.yRangeMode;
            scanMode = profile.scanMode;
            customMinY = profile.customMinY;
            customMaxY = profile.customMaxY;
            scanRadius = profile.scanRadius;
            targetChunks = profile.targetChunks;
        }

        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 25;

        // Title
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6§lNeoporium Scanner §7v1.0"),
                button -> {}
        ).dimensions(centerX - 150, 10, 300, 20).build()).active = false;

        // Profile selector
        List<String> profileNames = new ArrayList<>(AdvancedScanner.getProfiles().keySet());
        String currentProfileName = profile != null ? profile.name : "default";
        this.profileSelector = CyclingButtonWidget.<String>builder(Text::of)
                .values(profileNames)
                .initially(currentProfileName)
                .build(centerX - 150, startY, 300, 20,
                        Text.literal("Scan Profile"),
                        this::handleProfileSelection);
        this.addDrawableChild(this.profileSelector);

        // Y Range Mode selector
        List<String> yRangeModes = Arrays.asList("bedrock", "diamond", "full", "custom");
        this.yRangeSelector = CyclingButtonWidget.<String>builder(this::formatYRangeMode)
                .values(yRangeModes)
                .initially(yRangeMode)
                .build(centerX - 150, startY + spacing, 145, 20,
                        Text.literal("Y Range Mode"),
                        this::handleYRangeModeChange);
        this.addDrawableChild(yRangeSelector);

        // Scan Mode selector
        List<String> scanModes = Arrays.asList("radius", "single_chunk");
        this.scanModeSelector = CyclingButtonWidget.<String>builder(this::formatScanMode)
                .values(scanModes)
                .initially(scanMode)
                .build(centerX + 5, startY + spacing, 145, 20,
                        Text.literal("Scan Mode"),
                        this::handleScanModeChange);
        this.addDrawableChild(scanModeSelector);

        // Custom Y Range fields
        createYRangeFields(centerX, startY, spacing);

        // Scan parameters
        createScanFields(centerX, startY, spacing);

        // Action buttons
        createActionButtons(centerX, startY, spacing);

        // Toggle buttons
        createToggleButtons(centerX, startY, spacing);

        updateFieldVisibility();
        updateToggleButtons();
        updateBlockSelectionButton();

        // Initialize XRay if needed
        if (!XRayRenderer.isEnabled()) {
            XRayRenderer.init();
        }
    }

    private void createYRangeFields(int centerX, int startY, int spacing) {
        // Min Y field
        TextFieldWidget minYField = new TextFieldWidget(
                this.textRenderer, centerX - 150, startY + spacing * 2,
                145, 20, Text.literal("Min Y")
        );
        minYField.setText(String.valueOf(customMinY));
        minYField.setPlaceholder(Text.literal("-64 to 320"));
        minYField.setChangedListener(text -> {
            try {
                customMinY = Integer.parseInt(text);
                customMinY = Math.max(-64, Math.min(320, customMinY));
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(minYField);
        textFields.add(minYField);

        // Max Y field
        TextFieldWidget maxYField = new TextFieldWidget(
                this.textRenderer, centerX + 5, startY + spacing * 2,
                145, 20, Text.literal("Max Y")
        );
        maxYField.setText(String.valueOf(customMaxY));
        maxYField.setPlaceholder(Text.literal("-64 to 320"));
        maxYField.setChangedListener(text -> {
            try {
                customMaxY = Integer.parseInt(text);
                customMaxY = Math.max(-64, Math.min(320, customMaxY));
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(maxYField);
        textFields.add(maxYField);
    }

    private void createScanFields(int centerX, int startY, int spacing) {
        // Scan Radius field
        TextFieldWidget radiusField = new TextFieldWidget(
                this.textRenderer, centerX - 150, startY + spacing * 3,
                145, 20, Text.literal("Scan Radius")
        );
        radiusField.setText(String.valueOf(scanRadius));
        radiusField.setPlaceholder(Text.literal("0-16"));
        radiusField.setChangedListener(text -> {
            try {
                scanRadius = Integer.parseInt(text);
                scanRadius = Math.max(0, Math.min(16, scanRadius));
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(radiusField);
        textFields.add(radiusField);

        // Target Chunks field
        TextFieldWidget chunksField = new TextFieldWidget(
                this.textRenderer, centerX + 5, startY + spacing * 3,
                145, 20, Text.literal("Target Chunks")
        );
        chunksField.setText(String.valueOf(targetChunks));
        chunksField.setPlaceholder(Text.literal("1-256"));
        chunksField.setChangedListener(text -> {
            try {
                targetChunks = Integer.parseInt(text);
                targetChunks = Math.max(1, Math.min(256, targetChunks));
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(chunksField);
        textFields.add(chunksField);
    }

    private void createActionButtons(int centerX, int startY, int spacing) {
        // Block selection button
        this.blockSelectionButton = ButtonWidget.builder(
                Text.literal("§6Select Target Blocks"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new BlockSelectionScreen(AdvancedScanner.getCurrentProfile()));
                    }
                }
        ).dimensions(centerX - 150, startY + spacing * 5, 145, 20).build();
        this.addDrawableChild(this.blockSelectionButton);

        // XRay Settings button
        this.xraySettingsButton = ButtonWidget.builder(
                Text.literal("§6XRay Settings"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new XRayConfigScreen());
                    }
                }
        ).dimensions(centerX + 5, startY + spacing * 5, 145, 20).build();
        this.addDrawableChild(this.xraySettingsButton);

        // Log Manager button
        this.logManagerButton = ButtonWidget.builder(
                Text.literal("§6Log Manager"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new LogManagerScreen());
                    }
                }
        ).dimensions(centerX - 150, startY + spacing * 6, 145, 20).build();
        this.addDrawableChild(this.logManagerButton);

        // ESP Screen button (goes to XRay block selection)
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§dESP Screen"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new XRayConfigScreen());
                    }
                }
        ).dimensions(centerX + 5, startY + spacing * 6, 145, 20).build());

        // Scan buttons
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aScan Current Chunk"),
                button -> handleScanCurrentChunk()
        ).dimensions(centerX - 150, startY + spacing * 7, 145, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aScan Area"),
                button -> handleScanArea()
        ).dimensions(centerX + 5, startY + spacing * 7, 145, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§eTest XRay Now"),
                button -> {
                    if (this.client != null && this.client.player != null) {
                        XRayRenderer.test();
                        this.client.player.sendMessage(Text.literal("§6[Neoporium] XRay test activated"), false);
                    }
                }
        ).dimensions(centerX - 150, startY + spacing * 8, 145, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§cClear Results"),
                button -> handleClearResults()
        ).dimensions(centerX + 5, startY + spacing * 8, 145, 20).build());
    }

    private void createToggleButtons(int centerX, int startY, int spacing) {
        // Auto-scan toggle
        this.autoToggleButton = ButtonWidget.builder(
                Text.literal(getAutoToggleText()),
                this::handleAutoToggle
        ).dimensions(centerX - 150, startY + spacing * 9, 145, 20).build();
        this.addDrawableChild(this.autoToggleButton);

        // XRay toggle
        this.xrayToggleButton = ButtonWidget.builder(
                Text.literal(getXRayToggleText()),
                button -> handleXRayToggle()
        ).dimensions(centerX + 5, startY + spacing * 9, 145, 20).build();
        this.addDrawableChild(this.xrayToggleButton);
    }

    private Text formatYRangeMode(String mode) {
        return switch (mode) {
            case "bedrock" -> Text.literal("§bBedrock (-64 to 5)");
            case "diamond" -> Text.literal("§aDiamond (-59)");
            case "full" -> Text.literal("§eFull World");
            case "custom" -> Text.literal("§6Custom Range");
            default -> Text.literal("§bBedrock");
        };
    }

    private Text formatScanMode(String mode) {
        return switch (mode) {
            case "radius" -> Text.literal("§9Radius Mode");
            case "single_chunk" -> Text.literal("§aCurrent Chunk");
            default -> Text.literal("§9Radius");
        };
    }

    private void handleProfileSelection(CyclingButtonWidget<String> button, String selectedProfileName) {
        AdvancedScanner.setCurrentProfile(selectedProfileName);
        ScanProfile profile = AdvancedScanner.getCurrentProfile();
        if (profile != null) {
            yRangeMode = profile.yRangeMode;
            scanMode = profile.scanMode;
            customMinY = profile.customMinY;
            customMaxY = profile.customMaxY;
            scanRadius = profile.scanRadius;
            targetChunks = profile.targetChunks;

            yRangeSelector.setValue(yRangeMode);
            scanModeSelector.setValue(scanMode);

            if (textFields.size() >= 4) {
                textFields.get(0).setText(String.valueOf(customMinY));
                textFields.get(1).setText(String.valueOf(customMaxY));
                textFields.get(2).setText(String.valueOf(scanRadius));
                textFields.get(3).setText(String.valueOf(targetChunks));
            }
        }
        updateBlockSelectionButton();
    }

    private void handleYRangeModeChange(CyclingButtonWidget<String> button, String mode) {
        yRangeMode = mode;
        updateFieldVisibility();
    }

    private void handleScanModeChange(CyclingButtonWidget<String> button, String mode) {
        scanMode = mode;
        updateFieldVisibility();
    }

    private void handleAutoToggle(ButtonWidget button) {
        AdvancedConfig config = AdvancedConfig.get();
        config.autoScan = !config.autoScan;
        AdvancedConfig.save();
        updateToggleButtons();
        if (this.client != null && this.client.player != null) {
            String status = config.autoScan ? "§aENABLED" : "§cDISABLED";
            this.client.player.sendMessage(Text.literal("§6[Neoporium] Auto-scan: " + status), false);
        }
    }

    private void handleXRayToggle() {
        XRayRenderer.toggle();
        updateToggleButtons();
        if (this.client != null && this.client.player != null) {
            String status = XRayRenderer.isEnabled() ? "§aON" : "§cOFF";
            this.client.player.sendMessage(Text.literal("§6[Neoporium] XRay: " + status), false);
        }
    }

    private void handleScanCurrentChunk() {
        saveConfig();
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("§6[Neoporium] Scanning current chunk..."), false);
            AdvancedScanner.scanCurrentChunk(this.client.world, this.client.player);
            updateStatusDisplay();
        }
    }

    private void handleScanArea() {
        saveConfig();
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("§6[Neoporium] Scanning area..."), false);
            AdvancedScanner.scanArea(this.client.world, this.client.player);
            updateStatusDisplay();
        }
    }

    private void handleClearResults() {
        AdvancedScanner.clearScanResults();
        XRayRenderer.clearBlocks();
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("§6[Neoporium] All results cleared"), false);
        }
        updateStatusDisplay();
    }

    private String getAutoToggleText() {
        AdvancedConfig config = AdvancedConfig.get();
        return "Auto-scan: " + (config.autoScan ? "§aON" : "§cOFF");
    }

    private String getXRayToggleText() {
        return "XRay: " + (XRayRenderer.isEnabled() ? "§aON" : "§cOFF");
    }

    private void updateFieldVisibility() {
        boolean showCustomY = yRangeMode.equals("custom");
        boolean showRadius = scanMode.equals("radius");

        if (textFields.size() >= 4) {
            // Min/Max Y fields
            textFields.get(0).visible = showCustomY;
            textFields.get(1).visible = showCustomY;

            // Radius field
            textFields.get(2).visible = showRadius;

            // Target chunks field
            textFields.get(3).visible = true;
        }
    }

    private void updateBlockSelectionButton() {
        ScanProfile profile = AdvancedScanner.getCurrentProfile();
        if (blockSelectionButton != null && profile != null) {
            int blockCount = profile.targetBlocks.size();
            blockSelectionButton.setMessage(Text.literal("§6Select Blocks (" + blockCount + ")"));
        }
    }

    private void updateToggleButtons() {
        if (autoToggleButton != null) {
            autoToggleButton.setMessage(Text.literal(getAutoToggleText()));
        }
        if (xrayToggleButton != null) {
            xrayToggleButton.setMessage(Text.literal(getXRayToggleText()));
        }
    }

    private void updateStatusDisplay() {
        // This will trigger a re-render
    }

    private void saveConfig() {
        try {
            ScanProfile profile = AdvancedScanner.getCurrentProfile();
            if (profile == null) {
                profile = new ScanProfile("default");
                AdvancedScanner.addProfile(profile);
            }

            profile.yRangeMode = yRangeMode;
            profile.scanMode = scanMode;

            // Parse Y range based on mode
            switch (yRangeMode) {
                case "bedrock":
                    profile.minY = -64;
                    profile.maxY = 5;
                    break;
                case "diamond":
                    profile.minY = -59;
                    profile.maxY = -59;
                    break;
                case "full":
                    profile.minY = -64;
                    profile.maxY = 320;
                    break;
                case "custom":
                    profile.customMinY = customMinY;
                    profile.customMaxY = customMaxY;
                    profile.minY = Math.max(-64, Math.min(320, customMinY));
                    profile.maxY = Math.max(-64, Math.min(320, customMaxY));
                    break;
            }

            // Parse scan parameters
            if (textFields.size() >= 4) {
                if (scanMode.equals("radius")) {
                    profile.scanRadius = scanRadius;
                }
                profile.targetChunks = targetChunks;
            }

            // Save profile
            AdvancedScanner.saveProfile(profile.name);
            AdvancedConfig.save();

            sendSaveMessage(profile);

        } catch (Exception e) {
            System.err.println("[Neoporium] Error saving config: " + e.getMessage());
            handleSaveError();
        }
    }

    private void sendSaveMessage(ScanProfile profile) {
        if (this.client != null && this.client.player != null) {
            String yText = getYRangeText(profile);
            String scanText = getScanText(profile);

            this.client.player.sendMessage(Text.literal(
                    String.format("§a[Neoporium] Config saved: §e%s§a, %s, %s",
                            profile.name, yText, scanText)
            ), false);
        }
    }

    private String getYRangeText(ScanProfile profile) {
        switch (profile.yRangeMode) {
            case "bedrock": return "Y: §bBedrock Layer (-64 to 5)";
            case "diamond": return "Y: §aDiamond Level (-59)";
            case "full": return "Y: §eFull World (-64 to 320)";
            case "custom": return String.format("Y: §6Custom (%d to %d)", profile.minY, profile.maxY);
            default: return "Y: §bBedrock Layer";
        }
    }

    private String getScanText(ScanProfile profile) {
        if (profile.scanMode.equals("single_chunk")) {
            return "Scan: §aCurrent Chunk Only";
        } else {
            if (profile.scanRadius == 0) {
                return "Scan: §9Current Chunk Only";
            } else {
                int chunks = (profile.scanRadius * 2 + 1) * (profile.scanRadius * 2 + 1);
                return String.format("Scan: §9Radius %d (%d chunks)", profile.scanRadius, chunks);
            }
        }
    }

    private void handleSaveError() {
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("§c[Neoporium] Invalid number! Using defaults."), false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, "§6§lNeoporium Scanner §7v1.0",
                this.width / 2, 15, 0xFFFFFF);

        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 25;

        // Display current scan info
        ScanProfile profile = AdvancedScanner.getCurrentProfile();
        if (profile != null) {
            int y = startY + spacing * 10;

            // Block count
            int blockCount = profile.targetBlocks.size();
            context.drawTextWithShadow(this.textRenderer,
                    "§eTarget Blocks: §f" + blockCount,
                    centerX - 150, y, 0xFFFFFF);

            // XRay status
            int xrayCount = XRayRenderer.getBlockCount();
            String xrayStatus = XRayRenderer.isEnabled() ? "§aON" : "§cOFF";
            context.drawTextWithShadow(this.textRenderer,
                    "§eXRay: " + xrayStatus + " §7| §eBlocks: §f" + xrayCount,
                    centerX - 150, y + 15, 0xFFFFFF);

            // Found blocks
            Map<String, List<BlockPos>> foundBlocks = AdvancedScanner.getFoundBlocks();
            int totalFound = foundBlocks.values().stream().mapToInt(List::size).sum();
            context.drawTextWithShadow(this.textRenderer,
                    "§eFound Blocks: §f" + totalFound,
                    centerX - 150, y + 30, 0xFFFFFF);

            // Current Y range info
            String yInfo = getCurrentYRangeInfo(profile);
            context.drawTextWithShadow(this.textRenderer,
                    "§e" + yInfo,
                    centerX - 150, y + 45, 0xFFFFFF);
        }

        // Keybinds info
        context.drawTextWithShadow(this.textRenderer,
                "§7Keybinds: §6B§7=Quick Scan, §6G§7=GUI, §6X§7=XRay Toggle, §6L§7=Logs, §6P§7=Cycle Profile",
                centerX - 150, this.height - 40, 0xAAAAAA);

        context.drawTextWithShadow(this.textRenderer,
                "§7Automatically scans and logs blocks in specified Y range",
                centerX - 150, this.height - 25, 0x777777);

        super.render(context, mouseX, mouseY, delta);
    }

    private String getCurrentYRangeInfo(ScanProfile profile) {
        switch (profile.yRangeMode) {
            case "bedrock": return "Scanning: Bedrock Layer (Y=-64 to 5)";
            case "diamond": return "Scanning: Diamond Level (Y=-59)";
            case "full": return "Scanning: Full World (Y=-64 to 320)";
            case "custom": return String.format("Scanning: Y=%d to %d", profile.minY, profile.maxY);
            default: return "Scanning: Bedrock Layer";
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateBlockSelectionButton();
        updateToggleButtons();
    }

    @Override
    public void close() {
        saveConfig();
        super.close();
    }
}