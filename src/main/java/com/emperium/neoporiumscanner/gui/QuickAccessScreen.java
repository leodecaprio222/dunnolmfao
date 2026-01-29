package com.emperium.neoporiumscanner.gui;

import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.core.ScanController;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class QuickAccessScreen extends Screen {
    private final Screen parent;
    private ButtonWidget toggleXrayButton;
    private ButtonWidget scanButton;

    public QuickAccessScreen(Screen parent) {
        super(Text.literal("Quick Access"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int centerY = height / 2;

        // Quick toggle button - FIXED: store reference to update later
        toggleXrayButton = ButtonWidget.builder(
                Text.literal(StateSettings.isXrayEnabled() ? "Disable X-Ray" : "Enable X-Ray"),
                button -> {
                    StateSettings.toggleXray();
                    toggleXrayButton.setMessage(Text.literal(StateSettings.isXrayEnabled() ? "Disable X-Ray" : "Enable X-Ray"));
                }
        ).dimensions(centerX - 100, centerY - 60, 200, 20).build();
        addDrawableChild(toggleXrayButton);

        // Scan button - FIXED: store reference to update later
        scanButton = ButtonWidget.builder(
                Text.literal(StateSettings.isScanningActive() ? "Stop Scanning" : "Start Scanning"),
                button -> {
                    boolean wasActive = StateSettings.isScanningActive();
                    StateSettings.setScanningActive(!wasActive);
                    scanButton.setMessage(Text.literal(!wasActive ? "Stop Scanning" : "Start Scanning"));

                    if (!wasActive) {
                        ScanController.getInstance().startScan();
                    } else {
                        ScanController.getInstance().stopScan();
                    }
                }
        ).dimensions(centerX - 100, centerY - 30, 200, 20).build();
        addDrawableChild(scanButton);

        // ... rest of your button definitions remain the same ...

        // Quick radius buttons
        ButtonWidget radius4Button = ButtonWidget.builder(
                Text.literal("Radius: 4"),
                button -> {
                    StateSettings.setScanRadius(4);
                }
        ).dimensions(centerX - 100, centerY, 95, 20).build();
        addDrawableChild(radius4Button);

        ButtonWidget radius8Button = ButtonWidget.builder(
                Text.literal("Radius: 8"),
                button -> {
                    StateSettings.setScanRadius(8);
                }
        ).dimensions(centerX + 5, centerY, 95, 20).build();
        addDrawableChild(radius8Button);

        ButtonWidget radius12Button = ButtonWidget.builder(
                Text.literal("Radius: 12"),
                button -> {
                    StateSettings.setScanRadius(12);
                }
        ).dimensions(centerX - 100, centerY + 30, 95, 20).build();
        addDrawableChild(radius12Button);

        ButtonWidget radius16Button = ButtonWidget.builder(
                Text.literal("Radius: 16"),
                button -> {
                    StateSettings.setScanRadius(16);
                }
        ).dimensions(centerX + 5, centerY + 30, 95, 20).build();
        addDrawableChild(radius16Button);

        // Back button
        ButtonWidget backButton = ButtonWidget.builder(
                Text.literal("Back"),
                button -> client.setScreen(parent)
        ).dimensions(centerX - 100, centerY + 60, 200, 20).build();
        addDrawableChild(backButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);

        // Draw current status
        String status = String.format("X-Ray: %s | Scanning: %s | Radius: %d",
                StateSettings.isXrayEnabled() ? "§aON" : "§cOFF",
                StateSettings.isScanningActive() ? "§aACTIVE" : "§cINACTIVE",
                StateSettings.getScanRadius()
        );
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, 40, 0xFFFFFF);
    }
}