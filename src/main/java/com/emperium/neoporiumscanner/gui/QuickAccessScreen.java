package com.emperium.neoporiumscanner.gui;

import com.emperium.neoporiumscanner.core.AdvancedScanner;
import com.emperium.neoporiumscanner.core.ScanProfile;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import java.util.Map;

public class QuickAccessScreen extends Screen {
    private ButtonWidget scanToggleButton;
    private ButtonWidget xrayToggleButton;
    private ButtonWidget profileCycleButton;
    private ButtonWidget quickScanButton;
    private int updateCounter = 0;

    public QuickAccessScreen() {
        super(Text.literal("Quick Access"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 50;
        int spacing = 30;

        // Title
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6§lNeoporium Quick Access"),
                button -> {}
        ).dimensions(centerX - 100, startY - 30, 200, 20).build()).active = false;

        // Quick scan button
        quickScanButton = ButtonWidget.builder(
                Text.literal("§a⚡ Quick Scan"),
                button -> performQuickScan()
        ).dimensions(centerX - 100, startY, 200, 20).build();
        this.addDrawableChild(quickScanButton);

        // Scan toggle button
        scanToggleButton = ButtonWidget.builder(
                Text.literal(getScanToggleText()),
                button -> toggleAutoScan()
        ).dimensions(centerX - 100, startY + spacing, 200, 20).build();
        this.addDrawableChild(scanToggleButton);

        // XRay toggle button
        xrayToggleButton = ButtonWidget.builder(
                Text.literal(getXRayToggleText()),
                button -> toggleXRay()
        ).dimensions(centerX - 100, startY + spacing * 2, 200, 20).build();
        this.addDrawableChild(xrayToggleButton);

        // Profile cycle button
        profileCycleButton = ButtonWidget.builder(
                Text.literal(getProfileText()),
                button -> cycleProfile()
        ).dimensions(centerX - 100, startY + spacing * 3, 200, 20).build();
        this.addDrawableChild(profileCycleButton);

        // Open full GUI button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6Open Full GUI"),
                button -> openFullGUI()
        ).dimensions(centerX - 100, startY + spacing * 4, 200, 20).build());

        // Close button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§7Close"),
                button -> this.close()
        ).dimensions(centerX - 100, startY + spacing * 5, 200, 20).build());
    }

    private String getScanToggleText() {
        com.emperium.neoporiumscanner.config.AdvancedConfig config =
                com.emperium.neoporiumscanner.config.AdvancedConfig.get();
        return "Auto-scan: " + (config.autoScan ? "§aON" : "§cOFF");
    }

    private String getXRayToggleText() {
        return "XRay: " + (XRayRenderer.isEnabled() ? "§aON" : "§cOFF");
    }

    private String getProfileText() {
        ScanProfile profile = AdvancedScanner.getCurrentProfile();
        if (profile != null) {
            return "Profile: §e" + profile.name;
        }
        return "Profile: §7Unknown";
    }

    private void performQuickScan() {
        if (this.client != null && this.client.player != null && this.client.world != null) {
            this.client.player.sendMessage(Text.literal("§6[Neoporium] Quick scanning..."), false);
            AdvancedScanner.scanCurrentChunk(this.client.world, this.client.player);
            updateButtons();
        }
    }

    private void toggleAutoScan() {
        com.emperium.neoporiumscanner.config.AdvancedConfig config =
                com.emperium.neoporiumscanner.config.AdvancedConfig.get();
        config.autoScan = !config.autoScan;
        com.emperium.neoporiumscanner.config.AdvancedConfig.save();

        if (this.client != null && this.client.player != null) {
            String status = config.autoScan ? "§aENABLED" : "§cDISABLED";
            this.client.player.sendMessage(Text.literal("§6[Neoporium] Auto-scan: " + status), false);
        }

        updateButtons();
    }

    private void toggleXRay() {
        XRayRenderer.toggle();

        if (this.client != null && this.client.player != null) {
            String status = XRayRenderer.isEnabled() ? "§aENABLED" : "§cDISABLED";
            this.client.player.sendMessage(Text.literal("§6[Neoporium] XRay: " + status), false);
        }

        updateButtons();
    }

    private void cycleProfile() {
        AdvancedScanner.cycleNextProfile();

        if (this.client != null && this.client.player != null) {
            ScanProfile profile = AdvancedScanner.getCurrentProfile();
            this.client.player.sendMessage(Text.literal("§6[Neoporium] Switched to profile: §e" + profile.name), false);
        }

        updateButtons();
    }

    private void openFullGUI() {
        if (this.client != null) {
            this.client.setScreen(new AdvancedGuiScreen());
        }
    }

    private void updateButtons() {
        scanToggleButton.setMessage(Text.literal(getScanToggleText()));
        xrayToggleButton.setMessage(Text.literal(getXRayToggleText()));
        profileCycleButton.setMessage(Text.literal(getProfileText()));
    }

    @Override
    public void tick() {
        super.tick();
        updateCounter++;

        // Update buttons every 20 ticks (1 second)
        if (updateCounter % 20 == 0) {
            updateButtons();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Semi-transparent background
        context.fill(0, 0, this.width, this.height, 0x80000000);

        int centerX = this.width / 2;
        int startY = this.height / 2 - 50;

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, "§6§lNeoporium Quick Access",
                centerX, startY - 30, 0xFFFFFF);

        // Stats overlay
        renderStatsOverlay(context);

        // Instructions
        context.drawTextWithShadow(this.textRenderer,
                "§7Press ESC or click outside to close",
                centerX - 100, this.height - 30, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderStatsOverlay(DrawContext context) {
        int statsX = 10;
        int statsY = 10;

        // Current profile info
        ScanProfile profile = AdvancedScanner.getCurrentProfile();
        if (profile != null) {
            context.drawTextWithShadow(this.textRenderer,
                    "§eProfile: §f" + profile.name,
                    statsX, statsY, 0xFFFFFF);

            context.drawTextWithShadow(this.textRenderer,
                    "§eBlocks: §f" + profile.targetBlocks.size(),
                    statsX, statsY + 12, 0xFFFFFF);

            // Y range info
            String yRange = getYRangeText(profile);
            context.drawTextWithShadow(this.textRenderer,
                    "§eY Range: §f" + yRange,
                    statsX, statsY + 24, 0xFFFFFF);
        }

        // XRay status
        context.drawTextWithShadow(this.textRenderer,
                "§eXRay: " + (XRayRenderer.isEnabled() ? "§aON" : "§cOFF"),
                statsX, statsY + 36, 0xFFFFFF);

        // Found blocks count
        Map<String, java.util.List<net.minecraft.util.math.BlockPos>> foundBlocks = AdvancedScanner.getFoundBlocks();
        int totalFound = foundBlocks.values().stream().mapToInt(java.util.List::size).sum();
        context.drawTextWithShadow(this.textRenderer,
                "§eFound: §f" + totalFound + " blocks",
                statsX, statsY + 48, 0xFFFFFF);
    }

    private String getYRangeText(ScanProfile profile) {
        switch (profile.yRangeMode) {
            case "bedrock": return "-64 to 5";
            case "diamond": return "-59";
            case "full": return "-64 to 320";
            case "custom": return profile.minY + " to " + profile.maxY;
            default: return "-64 to 5";
        }
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game when this screen is open
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Close when clicking outside the main panel
        int centerX = this.width / 2;
        int panelLeft = centerX - 110;
        int panelRight = centerX + 110;
        int panelTop = this.height / 2 - 60;
        int panelBottom = this.height / 2 + 110;

        if (mouseX < panelLeft || mouseX > panelRight || mouseY < panelTop || mouseY > panelBottom) {
            this.close();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Close with Escape key
        if (keyCode == 256) { // Escape key
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}