package com.emperium.neoporiumscanner.gui;

import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.core.LogManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.File;

public class AdvancedGuiScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget maxBlocksField;

    public AdvancedGuiScreen(Screen parent) {
        super(Text.literal("Advanced Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int centerY = height / 2;

        // Max blocks per tick field
        maxBlocksField = new TextFieldWidget(textRenderer, centerX - 100, centerY - 60, 200, 20,
                Text.literal("Max Blocks Per Tick"));
        maxBlocksField.setText(String.valueOf(StateSettings.getMaxBlocksPerTick()));
        addDrawableChild(maxBlocksField);

        // Log manager button
        ButtonWidget logButton = ButtonWidget.builder(
                Text.literal("Open Log Manager"),
                button -> client.setScreen(new LogManagerScreen(this))
        ).dimensions(centerX - 100, centerY - 30, 200, 20).build();
        addDrawableChild(logButton);

        // Quick access button
        ButtonWidget quickAccessButton = ButtonWidget.builder(
                Text.literal("Quick Access"),
                button -> client.setScreen(new QuickAccessScreen(this))
        ).dimensions(centerX - 100, centerY, 200, 20).build();
        addDrawableChild(quickAccessButton);

        // Clear cache button
        ButtonWidget clearCacheButton = ButtonWidget.builder(
                Text.literal("Clear Cache"),
                button -> {
                    // Clear all caches
                    StateSettings.setScanningActive(false);
                    LogManager.closeLog();
                    LogManager.startNewLog();
                }
        ).dimensions(centerX - 100, centerY + 30, 200, 20).build();
        addDrawableChild(clearCacheButton);

        // Export config button
        ButtonWidget exportButton = ButtonWidget.builder(
                Text.literal("Export Configuration"),
                button -> exportConfig()
        ).dimensions(centerX - 100, centerY + 60, 200, 20).build();
        addDrawableChild(exportButton);

        // Save button
        ButtonWidget saveButton = ButtonWidget.builder(
                Text.literal("Save"),
                button -> {
                    try {
                        int maxBlocks = Integer.parseInt(maxBlocksField.getText());
                        StateSettings.setMaxBlocksPerTick(maxBlocks);
                    } catch (NumberFormatException e) {
                        maxBlocksField.setText("100");
                    }
                }
        ).dimensions(centerX - 100, centerY + 90, 95, 20).build();
        addDrawableChild(saveButton);

        // Back button
        ButtonWidget backButton = ButtonWidget.builder(
                Text.literal("Back"),
                button -> client.setScreen(parent)
        ).dimensions(centerX + 5, centerY + 90, 95, 20).build();
        addDrawableChild(backButton);
    }

    private void exportConfig() {
        try {
            File exportDir = new File("config/neoporium-scanner/export");
            exportDir.mkdirs();

            // Export would copy all config files to export directory
            // Implementation depends on your specific needs

            // Show success message
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // FIXED: Updated renderBackground for 1.21.4
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
    }
}