package com.emperium.neoporiumscanner.gui;

import com.emperium.neoporiumscanner.storage.BlockStore;
import com.emperium.neoporiumscanner.xray.BasicColor;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public class BlockSelectionScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget searchField;
    private List<BlockEntry> blockEntries;
    private List<BlockEntry> filteredEntries;
    private BlockListWidget blockListWidget;

    public BlockSelectionScreen(Screen parent) {
        super(Text.literal("Block Selection"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Search field
        searchField = new TextFieldWidget(textRenderer, 10, 10, width - 20, 20, Text.literal("Search blocks..."));
        searchField.setChangedListener(this::updateFilter);
        addDrawableChild(searchField);

        // Load all blocks
        loadBlocks();

        // Block list
        blockListWidget = new BlockListWidget(client, width, height, 40, height - 40, 20);
        blockListWidget.setEntries(filteredEntries);
        addSelectableChild(blockListWidget);

        // Back button
        ButtonWidget backButton = ButtonWidget.builder(
                Text.literal("Back"),
                button -> client.setScreen(parent)
        ).dimensions(width / 2 - 100, height - 30, 200, 20).build();
        addDrawableChild(backButton);
    }

    private void loadBlocks() {
        blockEntries = new ArrayList<>();
        BlockStore store = BlockStore.getInstance();

        // Get all registered blocks
        for (Block block : Registries.BLOCK) {
            Identifier id = Registries.BLOCK.getId(block);
            String blockId = id.toString();

            boolean enabled = store.isEnabled(blockId);
            BasicColor color = store.getColor(blockId);

            if (color == null) {
                // Assign default color for new blocks
                int hash = blockId.hashCode();
                color = new BasicColor(
                        (hash & 0xFF0000) >> 16,
                        (hash & 0x00FF00) >> 8,
                        hash & 0x0000FF,
                        200
                );
            }

            blockEntries.add(new BlockEntry(blockId, color, enabled, block.getName()));
        }

        // FIXED: Updated Comparator for 1.21.4
        blockEntries.sort(Comparator.comparing(entry -> entry.name().getString()));
        filteredEntries = new ArrayList<>(blockEntries);
    }

    private void updateFilter(String search) {
        if (search.isEmpty()) {
            filteredEntries = new ArrayList<>(blockEntries);
        } else {
            String lowerSearch = search.toLowerCase();
            filteredEntries = blockEntries.stream()
                    .filter(entry -> entry.name().getString().toLowerCase().contains(lowerSearch) ||
                            entry.blockId().toLowerCase().contains(lowerSearch))
                    .collect(Collectors.toList());
        }

        if (blockListWidget != null) {
            blockListWidget.setEntries(filteredEntries);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // FIXED: Updated renderBackground for 1.21.4
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Draw title
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xFFFFFF);

        // Draw stats
        String stats = String.format("Showing %d/%d blocks", filteredEntries.size(), blockEntries.size());
        context.drawTextWithShadow(textRenderer, Text.literal(stats), 10, 35, 0xAAAAAA);
    }

    private record BlockEntry(String blockId, BasicColor color, boolean enabled, Text name) {
    }

    private class BlockListWidget extends net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget<BlockEntryWidget> {
        public BlockListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
            super(client, width, height, top, bottom, itemHeight);
        }

        public void setEntries(List<BlockEntry> entries) {
            clearEntries();
            for (BlockEntry entry : entries) {
                addEntry(new BlockEntryWidget(entry));
            }
        }
    }

    private class BlockEntryWidget extends net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget.Entry<BlockEntryWidget> {
        private final BlockEntry entry;

        public BlockEntryWidget(BlockEntry entry) {
            this.entry = entry;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            // Draw background
            if (hovered) {
                context.fill(x, y, x + entryWidth, y + entryHeight, 0x33FFFFFF);
            }

            // Draw color preview
            BasicColor color = entry.color();
            context.fill(x + 2, y + 2, x + 12, y + entryHeight - 2, color.getRGB());

            // Draw block name
            String displayName = entry.name().getString();
            if (displayName.length() > 30) {
                displayName = displayName.substring(0, 27) + "...";
            }

            context.drawText(textRenderer, Text.literal(displayName), x + 15, y + 6, 0xFFFFFF, false);

            // Draw block ID
            context.drawText(textRenderer, Text.literal(entry.blockId()), x + 15, y + 16, 0xAAAAAA, false);

            // Draw enabled status
            String status = entry.enabled() ? "§a✓ Enabled" : "§c✗ Disabled";
            context.drawText(textRenderer, Text.literal(status), x + entryWidth - 80, y + 6, 0xFFFFFF, false);

            // Draw toggle button area
            if (hovered && mouseX >= x + entryWidth - 60 && mouseX < x + entryWidth - 10 &&
                    mouseY >= y + 4 && mouseY < y + entryHeight - 4) {
                context.fill(x + entryWidth - 60, y + 4, x + entryWidth - 10, y + entryHeight - 4, 0x44FFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                BlockStore store = BlockStore.getInstance();
                store.setEnabled(entry.blockId(), !entry.enabled());
                return true;
            }
            return false;
        }

        // FIXED: Added required getNarration method for 1.21.4
        @Override
        public Text getNarration() {
            return Text.literal("Block entry: " + entry.name().getString());
        }
    }
}