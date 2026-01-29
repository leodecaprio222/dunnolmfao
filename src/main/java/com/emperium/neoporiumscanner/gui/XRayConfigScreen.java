package com.emperium.neoporiumscanner.gui;

import com.emperium.neoporiumscanner.config.ConfigManager;
import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.storage.BlockStore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class XRayConfigScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget radiusField;
    private ButtonWidget toggleButton;
    private ButtonWidget addBlockButton;
    private ButtonWidget scanButton;

    public XRayConfigScreen(Screen parent) {
        super(Text.literal("Neoporium Scanner Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int centerY = height / 2;

        // Radius input field
        radiusField = new TextFieldWidget(textRenderer, centerX - 100, centerY - 60, 200, 20, Text.literal("Scan Radius"));
        radiusField.setText(String.valueOf(StateSettings.getScanRadius()));
        addDrawableChild(radiusField);

        // Toggle X-Ray button
        toggleButton = ButtonWidget.builder(
                Text.literal(StateSettings.isXrayEnabled() ? "Disable X-Ray" : "Enable X-Ray"),
                button -> {
                    StateSettings.toggleXray();
                    toggleButton.setMessage(Text.literal(StateSettings.isXrayEnabled() ? "Disable X-Ray" : "Enable X-Ray"));
                }
        ).dimensions(centerX - 100, centerY - 30, 200, 20).build();
        addDrawableChild(toggleButton);

        // Scan button
        scanButton = ButtonWidget.builder(
                Text.literal("Start Scan"),
                button -> {
                    try {
                        int radius = Integer.parseInt(radiusField.getText());
                        StateSettings.setScanRadius(radius);
                        StateSettings.setScanningActive(!StateSettings.isScanningActive());
                        scanButton.setMessage(Text.literal(StateSettings.isScanningActive() ? "Stop Scan" : "Start Scan"));
                    } catch (NumberFormatException e) {
                        radiusField.setText("8");
                    }
                }
        ).dimensions(centerX - 100, centerY, 200, 20).build();
        addDrawableChild(scanButton);

        // Add block button
        addBlockButton = ButtonWidget.builder(
                Text.literal("Add/Remove Blocks"),
                button -> {
                    MinecraftClient.getInstance().setScreen(new BlockSelectionScreen(this));
                }
        ).dimensions(centerX - 100, centerY + 30, 200, 20).build();
        addDrawableChild(addBlockButton);

        // Advanced settings button
        ButtonWidget advancedButton = ButtonWidget.builder(
                Text.literal("Advanced Settings"),
                button -> {
                    MinecraftClient.getInstance().setScreen(new AdvancedGuiScreen(this));
                }
        ).dimensions(centerX - 100, centerY + 60, 200, 20).build();
        addDrawableChild(advancedButton);

        // Save button
        ButtonWidget saveButton = ButtonWidget.builder(
                Text.literal("Save & Close"),
                button -> {
                    try {
                        int radius = Integer.parseInt(radiusField.getText());
                        StateSettings.setScanRadius(radius);
                        ConfigManager.saveAll();
                        close();
                    } catch (NumberFormatException e) {
                        radiusField.setText("8");
                    }
                }
        ).dimensions(centerX - 100, centerY + 90, 200, 20).build();
        addDrawableChild(saveButton);

        // Back button
        ButtonWidget backButton = ButtonWidget.builder(
                Text.literal("Back"),
                button -> close()
        ).dimensions(centerX - 100, centerY + 120, 200, 20).build();
        addDrawableChild(backButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // FIXED: Updated renderBackground for 1.21.4
        renderBackground(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        // FIXED: Use DrawContext methods for 1.21.4
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);

        // Draw status
        String status = StateSettings.isXrayEnabled() ? "§aEnabled" : "§cDisabled";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Status: " + status), width / 2, 40, 0xFFFFFF);

        // Draw block count
        BlockStore store = BlockStore.getInstance();
        int enabledCount = (int) store.getAllBlocks().entrySet().stream()
                .filter(entry -> (Boolean) entry.getValue().get("enabled"))
                .count();
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Blocks: " + enabledCount + " enabled"), width / 2, 55, 0xFFFFFF);
    }
}