package com.emperium.neoporiumscanner.gui;

import com.emperium.neoporiumscanner.config.StateSettings;
import com.emperium.neoporiumscanner.xray.XRayRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import java.util.*;

public class XRayConfigScreen extends Screen {
    private final Map<String, com.emperium.neoporiumscanner.xray.BasicColor> tempColors = new HashMap<>();
    private String selectedBlock = "minecraft:diamond_ore";
    private TextFieldWidget redField;
    private TextFieldWidget greenField;
    private TextFieldWidget blueField;
    private TextFieldWidget alphaColorField;
    private CyclingButtonWidget<String> renderModeSelector;
    private CheckboxWidget throughWallsCheckbox;
    private CheckboxWidget fadeDistanceCheckbox;
    private CheckboxWidget showDistanceCheckbox;
    private CheckboxWidget useLODCheckbox;
    private TextFieldWidget alphaField;
    private TextFieldWidget lineWidthField;
    private TextFieldWidget maxDistanceField;
    private TextFieldWidget lodDistanceField;

    public XRayConfigScreen() {
        super(Text.literal("XRay Configuration"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 25;

        // Title
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6XRay Configuration"),
                button -> {}
        ).dimensions(centerX - 150, 10, 300, 20).build()).active = false;

        // Render mode selector
        List<String> renderModes = Arrays.asList("wireframe", "solid", "transparent", "glowing");
        renderModeSelector = CyclingButtonWidget.<String>builder(mode ->
                        Text.of(mode.substring(0, 1).toUpperCase() + mode.substring(1)))
                .values(renderModes)
                .initially(XRayRenderer.getRenderMode())
                .build(centerX - 150, startY, 300, 20,
                        Text.literal("Render Mode"),
                        (button, mode) -> XRayRenderer.setRenderMode(mode));
        this.addDrawableChild(renderModeSelector);

        // Checkboxes
        throughWallsCheckbox = CheckboxWidget.builder(
                        Text.literal("Render Through Walls"),
                        this.textRenderer
                )
                .pos(centerX - 150, startY + spacing)
                .checked(XRayRenderer.isThroughWalls())
                .callback((checkbox, checked) -> XRayRenderer.setThroughWalls(checked))
                .build();
        this.addDrawableChild(throughWallsCheckbox);

        fadeDistanceCheckbox = CheckboxWidget.builder(
                        Text.literal("Fade With Distance"),
                        this.textRenderer
                )
                .pos(centerX + 50, startY + spacing)
                .checked(XRayRenderer.isFadeWithDistance())
                .callback((checkbox, checked) -> XRayRenderer.setFadeWithDistance(checked))
                .build();
        this.addDrawableChild(fadeDistanceCheckbox);

        showDistanceCheckbox = CheckboxWidget.builder(
                        Text.literal("Show Distance"),
                        this.textRenderer
                )
                .pos(centerX - 150, startY + spacing * 2)
                .checked(XRayRenderer.isShowDistance())
                .callback((checkbox, checked) -> XRayRenderer.setShowDistance(checked))
                .build();
        this.addDrawableChild(showDistanceCheckbox);

        useLODCheckbox = CheckboxWidget.builder(
                        Text.literal("Use Level of Detail"),
                        this.textRenderer
                )
                .pos(centerX + 50, startY + spacing * 2)
                .checked(XRayRenderer.isUseLOD())
                .callback((checkbox, checked) -> XRayRenderer.setUseLOD(checked))
                .build();
        this.addDrawableChild(useLODCheckbox);

        // Alpha field
        alphaField = new TextFieldWidget(
                this.textRenderer, centerX - 150, startY + spacing * 3,
                145, 20, Text.literal("Alpha")
        );
        alphaField.setPlaceholder(Text.literal("0.0-1.0"));
        alphaField.setText(String.valueOf(XRayRenderer.getAlpha()));
        alphaField.setChangedListener(text -> {
            try {
                float alphaValue = Float.parseFloat(text);
                XRayRenderer.setAlpha(Math.max(0.1f, Math.min(1.0f, alphaValue)));
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(alphaField);

        // Line width field
        lineWidthField = new TextFieldWidget(
                this.textRenderer, centerX + 5, startY + spacing * 3,
                145, 20, Text.literal("Line Width")
        );
        lineWidthField.setPlaceholder(Text.literal("0.5-5.0"));
        lineWidthField.setText(String.valueOf(XRayRenderer.getLineWidth()));
        lineWidthField.setChangedListener(text -> {
            try {
                float width = Float.parseFloat(text);
                XRayRenderer.setLineWidth(Math.max(0.5f, Math.min(5.0f, width)));
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(lineWidthField);

        // Max distance field
        maxDistanceField = new TextFieldWidget(
                this.textRenderer, centerX - 150, startY + spacing * 4,
                145, 20, Text.literal("Max Distance")
        );
        maxDistanceField.setPlaceholder(Text.literal("16-256"));
        maxDistanceField.setText(String.valueOf(XRayRenderer.getMaxDistance()));
        maxDistanceField.setChangedListener(text -> {
            try {
                int distance = Integer.parseInt(text);
                XRayRenderer.setMaxDistance(Math.max(16, Math.min(256, distance)));
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(maxDistanceField);

        // LOD distance field
        lodDistanceField = new TextFieldWidget(
                this.textRenderer, centerX + 5, startY + spacing * 4,
                145, 20, Text.literal("LOD Distance")
        );
        lodDistanceField.setPlaceholder(Text.literal("16-128"));
        lodDistanceField.setText(String.valueOf(XRayRenderer.getLODDistance()));
        lodDistanceField.setChangedListener(text -> {
            try {
                int distance = Integer.parseInt(text);
                XRayRenderer.setLODDistance(Math.max(16, Math.min(128, distance)));
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(lodDistanceField);

        // Block color settings title
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6Block Color Settings"),
                button -> {}
        ).dimensions(centerX - 150, startY + spacing * 6, 300, 20).build()).active = false;

        // Get common blocks
        List<String> blockTypes = getCommonBlockTypes();
        Collections.sort(blockTypes);

        if (!blockTypes.contains(selectedBlock)) {
            selectedBlock = !blockTypes.isEmpty() ? blockTypes.getFirst() : "minecraft:diamond_ore";
        }

        // Block selector
        CyclingButtonWidget<String> blockSelector = CyclingButtonWidget.<String>builder(this::formatBlockNameForDisplay)
                .values(blockTypes)
                .initially(selectedBlock)
                .build(centerX - 150, startY + spacing * 7, 300, 20,
                        Text.literal("Select Block"),
                        (button, blockName) -> {
                            selectedBlock = blockName;
                            updateColorFields();
                        });
        this.addDrawableChild(blockSelector);

        // Color input fields
        createColorInputFields(centerX, startY, spacing);

        // Quick color buttons
        createQuickColorButtons(centerX, startY, spacing);

        // Custom block input
        createCustomBlockInput(centerX, startY, spacing);

        // Action buttons
        createActionButtons(centerX, startY, spacing);

        // Load current colors
        loadCurrentColors();
        updateColorFields();
    }

    private void createColorInputFields(int centerX, int startY, int spacing) {
        this.redField = new TextFieldWidget(
                this.textRenderer, centerX - 150, startY + spacing * 8,
                70, 20, Text.literal("Red")
        );
        this.redField.setPlaceholder(Text.literal("0-255"));
        this.redField.setMaxLength(3);
        this.addDrawableChild(this.redField);

        this.greenField = new TextFieldWidget(
                this.textRenderer, centerX - 75, startY + spacing * 8,
                70, 20, Text.literal("Green")
        );
        this.greenField.setPlaceholder(Text.literal("0-255"));
        this.greenField.setMaxLength(3);
        this.addDrawableChild(this.greenField);

        this.blueField = new TextFieldWidget(
                this.textRenderer, centerX, startY + spacing * 8,
                70, 20, Text.literal("Blue")
        );
        this.blueField.setPlaceholder(Text.literal("0-255"));
        this.blueField.setMaxLength(3);
        this.addDrawableChild(this.blueField);

        this.alphaColorField = new TextFieldWidget(
                this.textRenderer, centerX + 75, startY + spacing * 8,
                70, 20, Text.literal("Alpha")
        );
        this.alphaColorField.setPlaceholder(Text.literal("0-255"));
        this.alphaColorField.setMaxLength(3);
        this.addDrawableChild(this.alphaColorField);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aApply Color"),
                button -> applyColor()
        ).dimensions(centerX - 150, startY + spacing * 9, 300, 20).build());
    }

    private void createQuickColorButtons(int centerX, int startY, int spacing) {
        // First row
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§cRed"),
                button -> setQuickColor(255, 0, 0, 200)
        ).dimensions(centerX - 150, startY + spacing * 10, 70, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aGreen"),
                button -> setQuickColor(0, 255, 0, 200)
        ).dimensions(centerX - 75, startY + spacing * 10, 70, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§9Blue"),
                button -> setQuickColor(0, 0, 255, 200)
        ).dimensions(centerX, startY + spacing * 10, 70, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§eGold"),
                button -> setQuickColor(255, 215, 0, 200)
        ).dimensions(centerX + 75, startY + spacing * 10, 70, 20).build());

        // Second row
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§bDiamond"),
                button -> setQuickColor(0, 200, 255, 200)
        ).dimensions(centerX - 150, startY + spacing * 11, 145, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§5Emerald"),
                button -> setQuickColor(0, 200, 0, 200)
        ).dimensions(centerX + 5, startY + spacing * 11, 145, 20).build());

        // Third row
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§4Redstone"),
                button -> setQuickColor(255, 0, 0, 200)
        ).dimensions(centerX - 150, startY + spacing * 12, 145, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§1Lapis"),
                button -> setQuickColor(0, 100, 200, 200)
        ).dimensions(centerX + 5, startY + spacing * 12, 145, 20).build());

        // Fourth row
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§7Iron"),
                button -> setQuickColor(200, 200, 200, 200)
        ).dimensions(centerX - 150, startY + spacing * 13, 145, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§8Coal"),
                button -> setQuickColor(50, 50, 50, 200)
        ).dimensions(centerX + 5, startY + spacing * 13, 145, 20).build());
    }

    private void createCustomBlockInput(int centerX, int startY, int spacing) {
        TextFieldWidget blockNameField = new TextFieldWidget(
                this.textRenderer, centerX - 150, startY + spacing * 14,
                200, 20, Text.literal("Custom Block")
        );
        blockNameField.setPlaceholder(Text.literal("minecraft:block_name"));
        this.addDrawableChild(blockNameField);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6Add Custom"),
                button -> addCustomBlock(blockNameField.getText())
        ).dimensions(centerX + 55, startY + spacing * 14, 95, 20).build());
    }

    private void createActionButtons(int centerX, int startY, int spacing) {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aSave & Close"),
                button -> saveAndClose()
        ).dimensions(centerX - 150, startY + spacing * 16, 145, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§7Back to Main"),
                button -> goBack()
        ).dimensions(centerX + 5, startY + spacing * 16, 145, 20).build());
    }

    private List<String> getCommonBlockTypes() {
        return Arrays.asList(
                "minecraft:diamond_ore",
                "minecraft:deepslate_diamond_ore",
                "minecraft:gold_ore",
                "minecraft:deepslate_gold_ore",
                "minecraft:iron_ore",
                "minecraft:deepslate_iron_ore",
                "minecraft:coal_ore",
                "minecraft:deepslate_coal_ore",
                "minecraft:emerald_ore",
                "minecraft:deepslate_emerald_ore",
                "minecraft:redstone_ore",
                "minecraft:deepslate_redstone_ore",
                "minecraft:lapis_ore",
                "minecraft:deepslate_lapis_ore",
                "minecraft:copper_ore",
                "minecraft:deepslate_copper_ore",
                "minecraft:ancient_debris",
                "minecraft:chest",
                "minecraft:trapped_chest",
                "minecraft:ender_chest",
                "minecraft:spawner",
                "minecraft:bedrock",
                "minecraft:obsidian"
        );
    }

    private Text formatBlockNameForDisplay(String blockId) {
        if (blockId == null || blockId.isEmpty()) {
            return Text.of("Unknown");
        }

        String displayName = blockId.replace("minecraft:", "");
        if (displayName.contains("_")) {
            String[] parts = displayName.split("_");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    result.append(parts[i].substring(0, 1).toUpperCase())
                            .append(parts[i].substring(1).toLowerCase());
                    if (i < parts.length - 1) {
                        result.append(" ");
                    }
                }
            }
            return Text.of(result.toString());
        }
        return Text.of(displayName.substring(0, 1).toUpperCase() + displayName.substring(1).toLowerCase());
    }

    private void loadCurrentColors() {
        tempColors.clear();
        StateSettings settings = StateSettings.getInstance();

        for (String blockId : getCommonBlockTypes()) {
            com.emperium.neoporiumscanner.xray.BasicColor savedColor = settings.getColor(blockId,
                    com.emperium.neoporiumscanner.core.BlockValidator.getRecommendedColor(blockId));
            tempColors.put(blockId, savedColor);
        }
    }

    private void updateColorFields() {
        com.emperium.neoporiumscanner.xray.BasicColor color = tempColors.getOrDefault(selectedBlock,
                new com.emperium.neoporiumscanner.xray.BasicColor(255, 0, 0, 200));
        if (redField != null) redField.setText(String.valueOf(color.r()));
        if (greenField != null) greenField.setText(String.valueOf(color.g()));
        if (blueField != null) blueField.setText(String.valueOf(color.b()));
        if (alphaColorField != null) alphaColorField.setText(String.valueOf(color.a()));
    }

    private void applyColor() {
        try {
            int r = Integer.parseInt(redField.getText());
            int g = Integer.parseInt(greenField.getText());
            int b = Integer.parseInt(blueField.getText());
            int a = Integer.parseInt(alphaColorField.getText());

            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));
            a = Math.max(0, Math.min(255, a));

            com.emperium.neoporiumscanner.xray.BasicColor newColor = new com.emperium.neoporiumscanner.xray.BasicColor(r, g, b, a);
            tempColors.put(selectedBlock, newColor);

            // Save to settings
            StateSettings settings = StateSettings.getInstance();
            settings.setColor(selectedBlock, newColor);

            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(
                        Text.literal(String.format("§a[Neoporium] Color for §e%s§a set to §cR:%d §aG:%d §9B:%d §7A:%d",
                                formatBlockNameForDisplay(selectedBlock).getString(), r, g, b, a)),
                        false
                );
            }

        } catch (NumberFormatException e) {
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.literal("§c[Neoporium] Invalid RGBA values! Use 0-255."), false);
            }
        }
    }

    private void setQuickColor(int r, int g, int b, int a) {
        redField.setText(String.valueOf(r));
        greenField.setText(String.valueOf(g));
        blueField.setText(String.valueOf(b));
        alphaColorField.setText(String.valueOf(a));
        applyColor();
    }

    private void addCustomBlock(String customName) {
        String blockId = customName.trim();

        if (blockId.isEmpty()) {
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.literal("§c[Neoporium] Please enter a block ID!"), false);
            }
            return;
        }

        if (!blockId.contains(":")) {
            blockId = "minecraft:" + blockId;
        }

        if (tempColors.containsKey(blockId)) {
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.literal("§7[Neoporium] Block already exists: " + blockId), false);
            }
            selectedBlock = blockId;
            updateColorFields();
            return;
        }

        tempColors.put(blockId, new com.emperium.neoporiumscanner.xray.BasicColor(255, 0, 0, 200));

        // Save to settings
        StateSettings settings = StateSettings.getInstance();
        settings.setColor(blockId, new com.emperium.neoporiumscanner.xray.BasicColor(255, 0, 0, 200));

        selectedBlock = blockId;
        updateColorFields();

        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(
                    Text.literal("§a[Neoporium] Added custom block: §e" + blockId),
                    false
            );
        }

        this.clearAndInit();
    }

    private void saveAndClose() {
        try {
            // Save all settings
            XRayRenderer.saveSettings();

            this.close();

            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.literal("§a[Neoporium] XRay settings saved!"), false);
                this.client.player.sendMessage(Text.literal("§7Mode: " + XRayRenderer.getRenderMode()), false);
            }

        } catch (NumberFormatException e) {
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.literal("§c[Neoporium] Invalid number format in settings!"), false);
            }
        } catch (Exception e) {
            System.err.println("[Neoporium] Error saving XRay config: " + e.getMessage());

            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.literal("§c[Neoporium] Error saving config: " + e.getMessage()), false);
            }
        }
    }

    private void goBack() {
        if (this.client != null) {
            this.client.setScreen(new AdvancedGuiScreen());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, "§6XRay Configuration",
                this.width / 2, 15, 0xFFFFFF);

        int startY = 40;
        int spacing = 25;

        // Section titles
        context.drawTextWithShadow(this.textRenderer, "Render Settings",
                this.width / 2 - 150, startY - 5, 0xAAAAAA);

        context.drawTextWithShadow(this.textRenderer, "Alpha (0.1-1.0)",
                this.width / 2 - 150, startY + spacing * 3 - 10, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Line Width (0.5-5.0)",
                this.width / 2 + 5, startY + spacing * 3 - 10, 0xAAAAAA);

        context.drawTextWithShadow(this.textRenderer, "Max Distance (16-256)",
                this.width / 2 - 150, startY + spacing * 4 - 10, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "LOD Distance (16-128)",
                this.width / 2 + 5, startY + spacing * 4 - 10, 0xAAAAAA);

        context.drawTextWithShadow(this.textRenderer, "Block Color Settings",
                this.width / 2 - 150, startY + spacing * 6 - 5, 0xAAAAAA);

        // Color field labels
        context.drawTextWithShadow(this.textRenderer, "Red (0-255)",
                this.width / 2 - 150, startY + spacing * 8 - 10, 0xFF5555);
        context.drawTextWithShadow(this.textRenderer, "Green (0-255)",
                this.width / 2 - 75, startY + spacing * 8 - 10, 0x55FF55);
        context.drawTextWithShadow(this.textRenderer, "Blue (0-255)",
                this.width / 2, startY + spacing * 8 - 10, 0x5555FF);
        context.drawTextWithShadow(this.textRenderer, "Alpha (0-255)",
                this.width / 2 + 75, startY + spacing * 8 - 10, 0xFFFFFF);

        // Color preview
        com.emperium.neoporiumscanner.xray.BasicColor color = tempColors.getOrDefault(selectedBlock,
                new com.emperium.neoporiumscanner.xray.BasicColor(255, 0, 0, 200));
        int previewX = this.width / 2 + 180;
        int previewY = startY + spacing * 8;

        int rgbColor = (color.r() << 16) | (color.g() << 8) | color.b();
        context.fill(previewX, previewY, previewX + 30, previewY + 20, 0xFF000000 | rgbColor);
        context.drawBorder(previewX, previewY, 30, 20, 0xFFFFFFFF);

        // Selected block info
        String displayName = formatBlockNameForDisplay(selectedBlock).getString();
        context.drawTextWithShadow(this.textRenderer,
                "§eSelected: §f" + displayName,
                this.width / 2 - 150, startY + spacing * 15 + 5, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
                String.format("§cR:%d §aG:%d §9B:%d §7A:%d", color.r(), color.g(), color.b(), color.a()),
                this.width / 2 - 150, startY + spacing * 15 + 20, 0xFFFFFF);

        // Current render mode info
        String renderMode = XRayRenderer.getRenderMode();
        String renderInfo = getRenderModeDescription(renderMode);

        context.drawTextWithShadow(this.textRenderer,
                "§7Current: " + renderInfo,
                this.width / 2 - 150, startY + spacing * 15 + 35, 0xAAAAAA);

        context.drawTextWithShadow(this.textRenderer,
                "§7Press X in-game to toggle XRay",
                this.width / 2 - 150, startY + spacing * 15 + 50, 0xAAAAAA);

        context.drawTextWithShadow(this.textRenderer,
                "§7Tip: Use full block IDs (minecraft:diamond_ore)",
                this.width / 2 - 150, this.height - 30, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    private String getRenderModeDescription(String renderMode) {
        return switch (renderMode) {
            case "wireframe" -> "§9Wireframe boxes";
            case "solid" -> "§aSolid boxes";
            case "transparent" -> "§bTransparent boxes";
            case "glowing" -> "§dGlowing boxes";
            default -> "§9Wireframe boxes";
        };
    }

    @Override
    public void close() {
        super.close();
    }
}