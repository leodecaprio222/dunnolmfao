package com.emperium.neoporiumscanner.gui;

import com.emperium.neoporiumscanner.core.LogManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class LogManagerScreen extends Screen {
    private final Screen parent;
    private String logContent = "";

    public LogManagerScreen(Screen parent) {
        super(Text.literal("Log Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        loadLogContent();

        // Clear log button
        ButtonWidget clearButton = ButtonWidget.builder(
                Text.literal("Clear Current Log"),
                button -> {
                    LogManager.closeLog();
                    LogManager.startNewLog();
                    loadLogContent();
                }
        ).dimensions(width / 2 - 150, height - 30, 140, 20).build();
        addDrawableChild(clearButton);

        // Open log folder button
        ButtonWidget folderButton = ButtonWidget.builder(
                Text.literal("Open Log Folder"),
                button -> {
                    try {
                        File logDir = new File("logs/neoporium-scanner");
                        java.awt.Desktop.getDesktop().open(logDir);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        ).dimensions(width / 2, height - 30, 140, 20).build();
        addDrawableChild(folderButton);

        // Back button
        ButtonWidget backButton = ButtonWidget.builder(
                Text.literal("Back"),
                button -> client.setScreen(parent)
        ).dimensions(width / 2 + 160, height - 30, 140, 20).build();
        addDrawableChild(backButton);
    }

    private void loadLogContent() {
        try {
            File logFile = LogManager.getCurrentLogFile();
            if (logFile != null && logFile.exists()) {
                List<String> lines = Files.readAllLines(logFile.toPath());
                logContent = String.join("\n", lines);
                if (logContent.length() > 10000) {
                    logContent = logContent.substring(logContent.length() - 10000);
                }
            } else {
                logContent = "No log file found.";
            }
        } catch (Exception e) {
            logContent = "Error loading log: " + e.getMessage();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // FIXED: Updated renderBackground for 1.21.4
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);

        // Draw log content
        int y = 30;
        String[] lines = logContent.split("\n");
        for (String line : lines) {
            if (y < height - 50) {
                context.drawText(textRenderer, Text.literal(line), 10, y, 0xFFFFFF, false);
                y += 10;
            }
        }

        // Draw scroll hint if needed
        if (lines.length * 10 > height - 80) {
            context.drawText(textRenderer, Text.literal("Scroll with mouse wheel"), width - 150, height - 15, 0xAAAAAA, false);
        }
    }
}