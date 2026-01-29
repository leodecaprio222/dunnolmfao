package com.emperium.neoporiumscanner.gui;

import com.emperium.neoporiumscanner.core.ScanProfile;
import com.emperium.neoporiumscanner.core.AdvancedScanner;
import com.emperium.neoporiumscanner.core.BlockValidator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import java.util.*;

public class BlockSelectionScreen extends Screen {
    private final Set<String> selectedBlocks = new HashSet<>();
    private final ScanProfile currentProfile;
    private final Set<String> originalBlocks = new HashSet<>();
    private TextFieldWidget searchField;
    private TextFieldWidget customBlockField;
    private String currentSearch = "";
    private int scrollOffset = 0;
    private static final int BLOCKS_PER_PAGE = 32;
    private static final String[][] COMMON_BLOCKS = {
            // Ores
            {"Diamond Ore", "minecraft:diamond_ore"},
            {"Deepslate Diamond", "minecraft:deepslate_diamond_ore"},
            {"Iron Ore", "minecraft:iron_ore"},
            {"Deepslate Iron", "minecraft:deepslate_iron_ore"},
            {"Gold Ore", "minecraft:gold_ore"},
            {"Deepslate Gold", "minecraft:deepslate_gold_ore"},
            {"Copper Ore", "minecraft:copper_ore"},
            {"Deepslate Copper", "minecraft:deepslate_copper_ore"},
            {"Coal Ore", "minecraft:coal_ore"},
            {"Deepslate Coal", "minecraft:deepslate_coal_ore"},
            {"Emerald Ore", "minecraft:emerald_ore"},
            {"Deepslate Emerald", "minecraft:deepslate_emerald_ore"},
            {"Redstone Ore", "minecraft:redstone_ore"},
            {"Deepslate Redstone", "minecraft:deepslate_redstone_ore"},
            {"Lapis Ore", "minecraft:lapis_ore"},
            {"Deepslate Lapis", "minecraft:deepslate_lapis_ore"},
            {"Ancient Debris", "minecraft:ancient_debris"},
            {"Nether Quartz", "minecraft:nether_quartz_ore"},
            {"Nether Gold", "minecraft:nether_gold_ore"},

            // Containers
            {"Chest", "minecraft:chest"},
            {"Trapped Chest", "minecraft:trapped_chest"},
            {"Ender Chest", "minecraft:ender_chest"},
            {"Barrel", "minecraft:barrel"},
            {"Shulker Box", "minecraft:shulker_box"},

            // Special blocks
            {"Spawner", "minecraft:spawner"},
            {"Bedrock", "minecraft:bedrock"},
            {"Obsidian", "minecraft:obsidian"},
            {"Amethyst Cluster", "minecraft:amethyst_cluster"},
            {"Budding Amethyst", "minecraft:budding_amethyst"},

            // Valuable blocks
            {"Diamond Block", "minecraft:diamond_block"},
            {"Gold Block", "minecraft:gold_block"},
            {"Iron Block", "minecraft:iron_block"},
            {"Emerald Block", "minecraft:emerald_block"},
            {"Netherite Block", "minecraft:netherite_block"},
            {"Copper Block", "minecraft:copper_block"},
            {"Lapis Block", "minecraft:lapis_block"},
            {"Redstone Block", "minecraft:redstone_block"},
            {"Coal Block", "minecraft:coal_block"},

            // Minerals
            {"Raw Iron Block", "minecraft:raw_iron_block"},
            {"Raw Gold Block", "minecraft:raw_gold_block"},
            {"Raw Copper Block", "minecraft:raw_copper_block"},

            // Other useful blocks
            {"Bookshelf", "minecraft:bookshelf"},
            {"Enchanting Table", "minecraft:enchanting_table"},
            {"Anvil", "minecraft:anvil"},
            {"Beacon", "minecraft:beacon"},
            {"Conduit", "minecraft:conduit"},
            {"Dragon Egg", "minecraft:dragon_egg"}
    };

    public BlockSelectionScreen(ScanProfile profile) {
        super(Text.literal("Block Selection"));
        this.currentProfile = profile;
        this.selectedBlocks.addAll(profile.targetBlocks);
        this.originalBlocks.addAll(profile.targetBlocks);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 25;

        // Title
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6§lSelect Target Blocks - Profile: " + currentProfile.name),
                button -> {}
        ).dimensions(centerX - 200, 10, 400, 20).build()).active = false;

        // Search field
        searchField = new TextFieldWidget(
                this.textRenderer, centerX - 200, startY,
                300, 20, Text.literal("Search...")
        );
        searchField.setPlaceholder(Text.literal("Type to search blocks"));
        searchField.setChangedListener(text -> {
            currentSearch = text.toLowerCase();
            scrollOffset = 0;
            this.clearAndInit();
        });
        this.addDrawableChild(searchField);

        // Clear search button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§cX"),
                button -> {
                    searchField.setText("");
                    currentSearch = "";
                    scrollOffset = 0;
                    this.clearAndInit();
                }
        ).dimensions(centerX + 110, startY, 90, 20).build());

        // Block grid
        createBlockGrid(centerX, startY, spacing);

        // Custom block input
        createCustomBlockInput(centerX, startY, spacing);

        // Action buttons
        createActionButtons(centerX, startY, spacing);

        // Navigation buttons
        createNavigationButtons(centerX, startY, spacing);
    }

    private void createBlockGrid(int centerX, int startY, int spacing) {
        int gridX = centerX - 200;
        int gridY = startY + spacing + 5;
        int buttonWidth = 95;
        int buttonHeight = 20;
        int columns = 4;

        List<String[]> filteredBlocks = getFilteredBlocks();
        int totalPages = (int) Math.ceil(filteredBlocks.size() / (double) BLOCKS_PER_PAGE);
        int startIndex = scrollOffset * BLOCKS_PER_PAGE;
        int endIndex = Math.min(startIndex + BLOCKS_PER_PAGE, filteredBlocks.size());

        for (int i = startIndex; i < endIndex; i++) {
            String displayName = filteredBlocks.get(i)[0];
            String blockId = filteredBlocks.get(i)[1];

            int relativeIndex = i - startIndex;
            int row = relativeIndex / columns;
            int col = relativeIndex % columns;
            int x = gridX + (col * 100);
            int y = gridY + (row * (spacing - 5));

            boolean isSelected = selectedBlocks.contains(blockId);
            String buttonText = (isSelected ? "§a✓ " : "§7") + displayName;

            ButtonWidget blockButton = ButtonWidget.builder(
                    Text.literal(buttonText),
                    button -> toggleBlockSelection(blockId, displayName)
            ).dimensions(x, y, buttonWidth, buttonHeight).build();

            this.addDrawableChild(blockButton);
        }
    }

    private void createCustomBlockInput(int centerX, int startY, int spacing) {
        int customY = startY + spacing * 9;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6Add Custom Block"),
                button -> {}
        ).dimensions(centerX - 200, customY, 400, 20).build()).active = false;

        customBlockField = new TextFieldWidget(
                this.textRenderer, centerX - 200, customY + spacing,
                300, 20, Text.literal("Custom Block")
        );
        customBlockField.setPlaceholder(Text.literal("minecraft:block_name or block_name"));
        this.addDrawableChild(customBlockField);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aAdd"),
                button -> addCustomBlock(customBlockField.getText())
        ).dimensions(centerX + 110, customY + spacing, 90, 20).build());
    }

    private void createActionButtons(int centerX, int startY, int spacing) {
        int actionY = startY + spacing * 11;

        // Selection info
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§eSelected: " + selectedBlocks.size() + " blocks"),
                button -> {}
        ).dimensions(centerX - 200, actionY, 400, 20).build()).active = false;

        // Clear button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§cClear All"),
                button -> clearAllSelections()
        ).dimensions(centerX - 200, actionY + spacing, 195, 20).build());

        // Save button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aSave & Close"),
                button -> saveAndClose()
        ).dimensions(centerX + 5, actionY + spacing, 195, 20).build());

        // Back button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§7Back to Scanner"),
                button -> goBack()
        ).dimensions(centerX - 200, actionY + spacing * 2, 400, 20).build());
    }

    private void createNavigationButtons(int centerX, int startY, int spacing) {
        List<String[]> filteredBlocks = getFilteredBlocks();
        int totalPages = (int) Math.ceil(filteredBlocks.size() / (double) BLOCKS_PER_PAGE);

        if (totalPages > 1) {
            int navY = startY + spacing * 8;

            // Previous page
            if (scrollOffset > 0) {
                this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("§7◀ Previous"),
                        button -> {
                            scrollOffset--;
                            this.clearAndInit();
                        }
                ).dimensions(centerX - 200, navY, 95, 20).build());
            }

            // Page indicator
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§7Page " + (scrollOffset + 1) + "/" + totalPages),
                    button -> {}
            ).dimensions(centerX - 100, navY, 200, 20).build()).active = false;

            // Next page
            if (scrollOffset < totalPages - 1) {
                this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("§7Next ▶"),
                        button -> {
                            scrollOffset++;
                            this.clearAndInit();
                        }
                ).dimensions(centerX + 105, navY, 95, 20).build());
            }
        }
    }

    private List<String[]> getFilteredBlocks() {
        List<String[]> filtered = new ArrayList<>();
        for (String[] block : COMMON_BLOCKS) {
            if (currentSearch.isEmpty() ||
                    block[0].toLowerCase().contains(currentSearch) ||
                    block[1].toLowerCase().contains(currentSearch)) {
                filtered.add(block);
            }
        }
        return filtered;
    }

    private void toggleBlockSelection(String blockId, String displayName) {
        if (selectedBlocks.contains(blockId)) {
            selectedBlocks.remove(blockId);
            sendMessage("§7Removed: " + displayName);
        } else {
            selectedBlocks.add(blockId);
            sendMessage("§aAdded: " + displayName);
        }
        this.clearAndInit();
    }

    private void addCustomBlock(String blockIdText) {
        String blockId = blockIdText.trim();
        if (blockId.isEmpty()) {
            sendMessage("§cPlease enter a block ID!");
            return;
        }

        if (!BlockValidator.isValidBlockId(blockId)) {
            sendMessage("§cInvalid block ID: " + blockId);
            return;
        }

        String normalizedId = BlockValidator.normalizeBlockId(blockId);
        if (selectedBlocks.contains(normalizedId)) {
            sendMessage("§7Block already selected: " + normalizedId);
            return;
        }

        selectedBlocks.add(normalizedId);
        customBlockField.setText("");
        sendMessage("§aAdded custom block: " + normalizedId);
        this.clearAndInit();
    }

    private void clearAllSelections() {
        selectedBlocks.clear();
        sendMessage("§6Cleared all selections");
        this.clearAndInit();
    }

    private void saveAndClose() {
        // Save to profile
        currentProfile.targetBlocks.clear();
        currentProfile.targetBlocks.addAll(selectedBlocks);

        // Save to settings
        AdvancedScanner.saveProfile(currentProfile.name);

        sendMessage("§aSaved " + selectedBlocks.size() + " blocks to profile");
        goBack();
    }

    private void goBack() {
        if (this.client != null) {
            this.client.setScreen(new AdvancedGuiScreen());
        }
    }

    private void sendMessage(String message) {
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(net.minecraft.text.Text.literal("[Neoporium] " + message), false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
                "§6§lSelect Target Blocks - Profile: " + currentProfile.name,
                this.width / 2, 15, 0xFFFFFF);

        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 25;

        // Search info
        if (!currentSearch.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer,
                    "§7Searching for: §f" + currentSearch,
                    centerX - 200, startY + 25, 0xAAAAAA);
        }

        // Block count info
        List<String[]> filteredBlocks = getFilteredBlocks();
        context.drawTextWithShadow(this.textRenderer,
                "§7Showing: §f" + Math.min(BLOCKS_PER_PAGE, filteredBlocks.size() - scrollOffset * BLOCKS_PER_PAGE) +
                        "§7 of §f" + filteredBlocks.size() + "§7 blocks",
                centerX - 200, startY + 40, 0xAAAAAA);

        // Instructions
        context.drawTextWithShadow(this.textRenderer,
                "§7Click blocks to select/deselect. Green check = selected.",
                centerX - 200, this.height - 80, 0xAAAAAA);

        context.drawTextWithShadow(this.textRenderer,
                "§7Use custom field for modded blocks (e.g., 'create:zinc_ore')",
                centerX - 200, this.height - 65, 0xAAAAAA);

        // Selected blocks preview
        renderSelectedBlocksPreview(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSelectedBlocksPreview(DrawContext context) {
        int previewY = this.height - 50;
        context.drawTextWithShadow(this.textRenderer, "§eSelected Blocks Preview:",
                this.width / 2 - 200, previewY, 0xFFFF55);

        if (selectedBlocks.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, "§7None selected",
                    this.width / 2 - 200, previewY + 12, 0x777777);
        } else {
            List<String> displayList = new ArrayList<>();
            for (String blockId : selectedBlocks) {
                String displayName = BlockValidator.getDisplayName(blockId);
                if (displayName.length() > 20) {
                    displayName = displayName.substring(0, 17) + "...";
                }
                displayList.add("§a• " + displayName);
            }

            // Show first 5 blocks
            int maxShow = 5;
            for (int i = 0; i < Math.min(maxShow, displayList.size()); i++) {
                context.drawTextWithShadow(this.textRenderer, displayList.get(i),
                        this.width / 2 - 200, previewY + 12 + (i * 10), 0x55FF55);
            }

            if (selectedBlocks.size() > maxShow) {
                context.drawTextWithShadow(this.textRenderer,
                        "§7... and " + (selectedBlocks.size() - maxShow) + " more",
                        this.width / 2 - 200, previewY + 12 + (maxShow * 10), 0x777777);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        List<String[]> filteredBlocks = getFilteredBlocks();
        int totalPages = (int) Math.ceil(filteredBlocks.size() / (double) BLOCKS_PER_PAGE);

        if (verticalAmount < 0 && scrollOffset < totalPages - 1) {
            scrollOffset++;
            this.clearAndInit();
            return true;
        } else if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--;
            this.clearAndInit();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        if (!selectedBlocks.equals(originalBlocks)) {
            saveAndClose();
        } else {
            goBack();
        }
        super.close();
    }
}