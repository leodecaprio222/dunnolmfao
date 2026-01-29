package com.emperium.neoporiumscanner.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.emperium.neoporiumscanner.config.ConfigManager;
import java.util.HashSet;
import java.util.Set;

public class BlockSelectionScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget blockIdField;
    private ButtonWidget addButton;
    private ButtonWidget removeButton;
    private ButtonWidget clearButton;
    private Set<String> displayedBlocks = new HashSet<>();

    public BlockSelectionScreen(Screen parent) {
        super(Text.literal("Block Selection"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int startY = 40;
        int fieldWidth = 200;
        int buttonWidth = 100;
        int buttonHeight = 20;
        int spacing = 25;

        // Block ID input field
        blockIdField = new TextFieldWidget(textRenderer,
                centerX - fieldWidth/2, startY, fieldWidth, buttonHeight,
                Text.literal("Block ID (e.g., minecraft:diamond_ore)"));
        blockIdField.setMaxLength(256);
        blockIdField.setPlaceholder(Text.literal("Enter block ID...").formatted(Formatting.GRAY));
        addDrawableChild(blockIdField);

        startY += spacing;

        // Add button
        addButton = ButtonWidget.builder(
                        Text.literal("Add Block"),
                        button -> {
                            String blockId = blockIdField.getText().trim();
                            if (!blockId.isEmpty()) {
                                ConfigManager.addTrackedBlock(blockId);
                                blockIdField.setText("");
                                refreshDisplayedBlocks();
                            }
                        })
                .dimensions(centerX - buttonWidth - 5, startY, buttonWidth, buttonHeight)
                .build();

        // Remove button
        removeButton = ButtonWidget.builder(
                        Text.literal("Remove Block"),
                        button -> {
                            String blockId = blockIdField.getText().trim();
                            if (!blockId.isEmpty()) {
                                ConfigManager.removeTrackedBlock(blockId);
                                blockIdField.setText("");
                                refreshDisplayedBlocks();
                            }
                        })
                .dimensions(centerX + 5, startY, buttonWidth, buttonHeight)
                .build();

        startY += spacing;

        // Clear button
        clearButton = ButtonWidget.builder(
                        Text.literal("Clear All"),
                        button -> {
                            ConfigManager.clearTrackedBlocks();
                            refreshDisplayedBlocks();
                        })
                .dimensions(centerX - buttonWidth/2, startY, buttonWidth, buttonHeight)
                .build();

        startY += spacing + 10;

        // Back button
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("Back"),
                        button -> client.setScreen(parent))
                .dimensions(centerX - 50, height - 30, 100, buttonHeight)
                .build());

        addDrawableChild(addButton);
        addDrawableChild(removeButton);
        addDrawableChild(clearButton);

        refreshDisplayedBlocks();
    }

    private void refreshDisplayedBlocks() {
        displayedBlocks = ConfigManager.getTrackedBlocks();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        // Title
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Block Selection").formatted(Formatting.GOLD, Formatting.BOLD),
                width / 2, 10, 0xFFFFFF);

        // Instructions
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Enter Minecraft block IDs (e.g., minecraft:diamond_ore)"),
                width / 2, 25, 0xFFFFFF);

        // Tracked blocks count
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Tracked Blocks: " + displayedBlocks.size()).formatted(Formatting.YELLOW),
                width / 2, 70, 0xFFFFFF);

        // Display tracked blocks
        int startY = 90;
        int maxWidth = width - 40;
        int lineHeight = 12;

        for (String blockId : displayedBlocks) {
            if (startY > height - 50) break;

            String displayText = blockId.length() > 50 ? blockId.substring(0, 47) + "..." : blockId;
            context.drawTextWithShadow(textRenderer,
                    Text.literal("• " + displayText).formatted(Formatting.WHITE),
                    20, startY, 0xFFFFFF);
            startY += lineHeight;
        }

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
}