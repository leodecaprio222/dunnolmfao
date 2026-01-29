package com.emperium.neoporiumscanner.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import com.emperium.neoporiumscanner.config.ConfigManager;
import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.core.ScanController;
import com.emperium.neoporiumscanner.xray.render.RenderManager;
import java.util.function.Consumer;

public class AdvancedGuiScreen extends Screen {
    private final Screen parent;

    // XRay widgets
    private ButtonWidget xrayToggleButton;
    private SliderWidget xrayOpacitySlider;
    private ButtonWidget xraySeeThroughButton;
    private SliderWidget xrayDistanceSlider;

    // ESP widgets
    private ButtonWidget espToggleButton;
    private ButtonWidget espModeButton;
    private SliderWidget espThicknessSlider;
    private ButtonWidget espFadeToggleButton;
    private SliderWidget espDistanceSlider;

    // Scan widgets
    private ButtonWidget scanToggleButton;
    private SliderWidget scanRangeSlider;
    private ButtonWidget blockSelectionButton;
    private ButtonWidget xrayConfigButton;
    private ButtonWidget logManagerButton;
    private ButtonWidget quickAccessButton;

    public AdvancedGuiScreen(Screen parent) {
        super(Text.literal("Neoporium Scanner"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int startY = 40;
        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 25;
        int sectionSpacing = 10;

        int currentY = startY;

        // === SCAN CONTROLS ===
        addDrawableChild(new TextWidget(
                centerX - 75, currentY - 15, 150, 10,
                Text.literal("Scan Controls").formatted(Formatting.GOLD, Formatting.BOLD),
                textRenderer
        ));

        scanToggleButton = ButtonWidget.builder(
                        Text.literal(ScanController.isScanning() ? "Stop Scan" : "Start Scan")
                                .formatted(ScanController.isScanning() ? Formatting.RED : Formatting.GREEN),
                        button -> {
                            if (ScanController.isScanning()) {
                                ScanController.stopScan();
                                button.setMessage(Text.literal("Start Scan").formatted(Formatting.GREEN));
                            } else {
                                ScanController.startScan();
                                button.setMessage(Text.literal("Stop Scan").formatted(Formatting.RED));
                            }
                        })
                .dimensions(centerX - buttonWidth - 5, currentY, buttonWidth, buttonHeight)
                .build();

        scanRangeSlider = new SliderWidget(
                centerX + 5, currentY, buttonWidth, buttonHeight,
                Text.literal("Range: " + ConfigManager.getScanRange() + " blocks"),
                ConfigManager.getScanRange() / 512.0f
        ) {
            @Override
            protected void updateMessage() {
                int value = Math.round(this.value * 512);
                this.setMessage(Text.literal("Range: " + value + " blocks"));
            }

            @Override
            protected void applyValue() {
                int value = Math.round(this.value * 512);
                ConfigManager.setScanRange(Math.max(1, value));
            }
        };

        currentY += spacing + sectionSpacing;

        // === XRAY CONTROLS ===
        addDrawableChild(new TextWidget(
                centerX - 75, currentY - 15, 150, 10,
                Text.literal("XRay Settings").formatted(Formatting.GOLD, Formatting.BOLD),
                textRenderer
        ));

        xrayToggleButton = ButtonWidget.builder(
                        Text.literal("XRay: " + (ConfigManager.isXRayEnabled() ? "ON" : "OFF"))
                                .formatted(ConfigManager.isXRayEnabled() ? Formatting.GREEN : Formatting.RED),
                        button -> {
                            boolean newState = !ConfigManager.isXRayEnabled();
                            ConfigManager.setXRayEnabled(newState);
                            button.setMessage(Text.literal("XRay: " + (newState ? "ON" : "OFF"))
                                    .formatted(newState ? Formatting.GREEN : Formatting.RED));
                        })
                .dimensions(centerX - buttonWidth - 5, currentY, buttonWidth, buttonHeight)
                .build();

        xrayOpacitySlider = new SliderWidget(
                centerX + 5, currentY, buttonWidth, buttonHeight,
                Text.literal("Opacity: " + (int)(ConfigManager.getXRayOpacity() * 100) + "%"),
                ConfigManager.getXRayOpacity()
        ) {
            @Override
            protected void updateMessage() {
                int percent = Math.round(this.value * 100);
                this.setMessage(Text.literal("Opacity: " + percent + "%"));
            }

            @Override
            protected void applyValue() {
                ConfigManager.setXRayOpacity(this.value);
            }
        };

        currentY += spacing;

        xraySeeThroughButton = ButtonWidget.builder(
                        Text.literal("See-Through: " + (ConfigManager.isXRaySeeThrough() ? "ON" : "OFF"))
                                .formatted(ConfigManager.isXRaySeeThrough() ? Formatting.GREEN : Formatting.RED),
                        button -> {
                            boolean newState = !ConfigManager.isXRaySeeThrough();
                            ConfigManager.setXRaySeeThrough(newState);
                            button.setMessage(Text.literal("See-Through: " + (newState ? "ON" : "OFF"))
                                    .formatted(newState ? Formatting.GREEN : Formatting.RED));
                        })
                .dimensions(centerX - buttonWidth - 5, currentY, buttonWidth, buttonHeight)
                .build();

        xrayDistanceSlider = new SliderWidget(
                centerX + 5, currentY, buttonWidth, buttonHeight,
                Text.literal("Distance: " + ConfigManager.getXRayDistance() + " blocks"),
                ConfigManager.getXRayDistance() / 512.0f
        ) {
            @Override
            protected void updateMessage() {
                int value = Math.round(this.value * 512);
                this.setMessage(Text.literal("Distance: " + value + " blocks"));
            }

            @Override
            protected void applyValue() {
                int value = Math.round(this.value * 512);
                ConfigManager.setXRayDistance(Math.max(1, value));
            }
        };

        currentY += spacing + sectionSpacing;

        // === ESP CONTROLS ===
        addDrawableChild(new TextWidget(
                centerX - 75, currentY - 15, 150, 10,
                Text.literal("ESP Settings").formatted(Formatting.GOLD, Formatting.BOLD),
                textRenderer
        ));

        espToggleButton = ButtonWidget.builder(
                        Text.literal("ESP: " + (ConfigManager.isESPEnabled() ? "ON" : "OFF"))
                                .formatted(ConfigManager.isESPEnabled() ? Formatting.GREEN : Formatting.RED),
                        button -> {
                            boolean newState = !ConfigManager.isESPEnabled();
                            ConfigManager.setESPEnabled(newState);
                            button.setMessage(Text.literal("ESP: " + (newState ? "ON" : "OFF"))
                                    .formatted(newState ? Formatting.GREEN : Formatting.RED));
                        })
                .dimensions(centerX - buttonWidth - 5, currentY, buttonWidth, buttonHeight)
                .build();

        espModeButton = ButtonWidget.builder(
                        Text.literal("Mode: " + ConfigManager.getESPMode()),
                        button -> {
                            String current = ConfigManager.getESPMode();
                            String next = switch (current) {
                                case "BOX" -> "WIREFRAME";
                                case "WIREFRAME" -> "BOTH";
                                case "BOTH" -> "BOX";
                                default -> "BOX";
                            };
                            ConfigManager.setESPMode(next);
                            button.setMessage(Text.literal("Mode: " + next));
                        })
                .dimensions(centerX + 5, currentY, buttonWidth, buttonHeight)
                .build();

        currentY += spacing;

        espThicknessSlider = new SliderWidget(
                centerX - buttonWidth - 5, currentY, buttonWidth, buttonHeight,
                Text.literal("Thickness: " + String.format("%.1f", ConfigManager.getESPThickness())),
                (ConfigManager.getESPThickness() - 0.1f) / 9.9f
        ) {
            @Override
            protected void updateMessage() {
                float value = 0.1f + (this.value * 9.9f);
                this.setMessage(Text.literal("Thickness: " + String.format("%.1f", value)));
            }

            @Override
            protected void applyValue() {
                float value = 0.1f + (this.value * 9.9f);
                ConfigManager.setESPThickness(value);
            }
        };

        espFadeToggleButton = ButtonWidget.builder(
                        Text.literal("Fade: " + (ConfigManager.isESPFadeEnabled() ? "ON" : "OFF"))
                                .formatted(ConfigManager.isESPFadeEnabled() ? Formatting.GREEN : Formatting.RED),
                        button -> {
                            boolean newState = !ConfigManager.isESPFadeEnabled();
                            ConfigManager.setESPFadeEnabled(newState);
                            button.setMessage(Text.literal("Fade: " + (newState ? "ON" : "OFF"))
                                    .formatted(newState ? Formatting.GREEN : Formatting.RED));
                        })
                .dimensions(centerX + 5, currentY, buttonWidth, buttonHeight)
                .build();

        currentY += spacing;

        espDistanceSlider = new SliderWidget(
                centerX - buttonWidth - 5, currentY, buttonWidth, buttonHeight,
                Text.literal("Distance: " + ConfigManager.getESPDistance() + " blocks"),
                ConfigManager.getESPDistance() / 512.0f
        ) {
            @Override
            protected void updateMessage() {
                int value = Math.round(this.value * 512);
                this.setMessage(Text.literal("Distance: " + value + " blocks"));
            }

            @Override
            protected void applyValue() {
                int value = Math.round(this.value * 512);
                ConfigManager.setESPDistance(Math.max(1, value));
            }
        };

        currentY += spacing + sectionSpacing;

        // === UTILITY BUTTONS ===
        addDrawableChild(new TextWidget(
                centerX - 75, currentY - 15, 150, 10,
                Text.literal("Utilities").formatted(Formatting.GOLD, Formatting.BOLD),
                textRenderer
        ));

        blockSelectionButton = ButtonWidget.builder(
                        Text.literal("Block Selection"),
                        button -> client.setScreen(new BlockSelectionScreen(this)))
                .dimensions(centerX - buttonWidth - 5, currentY, buttonWidth, buttonHeight)
                .build();

        xrayConfigButton = ButtonWidget.builder(
                        Text.literal("XRay Config"),
                        button -> client.setScreen(new XRayConfigScreen(this)))
                .dimensions(centerX + 5, currentY, buttonWidth, buttonHeight)
                .build();

        currentY += spacing;

        logManagerButton = ButtonWidget.builder(
                        Text.literal("Log Manager"),
                        button -> client.setScreen(new LogManagerScreen(this)))
                .dimensions(centerX - buttonWidth - 5, currentY, buttonWidth, buttonHeight)
                .build();

        quickAccessButton = ButtonWidget.builder(
                        Text.literal("Quick Access"),
                        button -> client.setScreen(new QuickAccessScreen(this)))
                .dimensions(centerX + 5, currentY, buttonWidth, buttonHeight)
                .build();

        currentY += spacing + 10;

        // === BACK BUTTON ===
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("Back"),
                        button -> client.setScreen(parent))
                .dimensions(centerX - 50, height - 30, 100, buttonHeight)
                .build());

        // Add all widgets
        addDrawableChild(scanToggleButton);
        addDrawableChild(scanRangeSlider);
        addDrawableChild(xrayToggleButton);
        addDrawableChild(xrayOpacitySlider);
        addDrawableChild(xraySeeThroughButton);
        addDrawableChild(xrayDistanceSlider);
        addDrawableChild(espToggleButton);
        addDrawableChild(espModeButton);
        addDrawableChild(espThicknessSlider);
        addDrawableChild(espFadeToggleButton);
        addDrawableChild(espDistanceSlider);
        addDrawableChild(blockSelectionButton);
        addDrawableChild(xrayConfigButton);
        addDrawableChild(logManagerButton);
        addDrawableChild(quickAccessButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        // Title
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Neoporium Scanner").formatted(Formatting.GOLD, Formatting.BOLD),
                width / 2, 10, 0xFFFFFF);

        // Status line
        String status = String.format("%s | XRay: %s | ESP: %s",
                ScanController.isScanning() ? "Scanning" : "Idle",
                ConfigManager.isXRayEnabled() ? "ON" : "OFF",
                ConfigManager.isESPEnabled() ? "ON" : "OFF");

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(status).formatted(Formatting.YELLOW),
                width / 2, 25, 0xFFFFFF);

        // Key hint
        context.drawTextWithShadow(textRenderer,
                Text.literal("Press H to toggle GUI").formatted(Formatting.GRAY),
                5, height - 10, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        ConfigManager.save();
        super.close();
    }

    // Helper class for text widgets
    private static class TextWidget extends net.minecraft.client.gui.widget.ClickableWidget {
        private final Text text;
        private final net.minecraft.client.font.TextRenderer textRenderer;

        public TextWidget(int x, int y, int width, int height, Text text,
                          net.minecraft.client.font.TextRenderer textRenderer) {
            super(x, y, width, height, text);
            this.text = text;
            this.textRenderer = textRenderer;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.drawText(textRenderer, text, getX(), getY(), 0xFFFFFF, true);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            // No action
        }
    }
}