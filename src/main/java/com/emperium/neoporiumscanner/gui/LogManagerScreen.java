package com.emperium.neoporiumscanner.gui;

import com.emperium.neoporiumscanner.core.LogManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LogManagerScreen extends Screen {
    private final List<File> logFiles = new ArrayList<>();
    private File selectedFile = null;
    private TextFieldWidget searchField;
    private final List<String> logEntries = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ENTRIES_PER_PAGE = 15;
    private String currentSearch = "";

    public LogManagerScreen() {
        super(Text.literal("Log Manager"));
    }

    @Override
    protected void init() {
        super.init();

        loadLogFiles();

        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 25;

        // Title
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6§lNeoporium Log Manager"),
                button -> {}
        ).dimensions(centerX - 200, 10, 400, 20).build()).active = false;

        // File list
        createFileList(centerX, startY, spacing);

        // Search and actions
        createSearchAndActions(centerX, startY, spacing);

        // Log entries display area
        createLogDisplayArea(centerX, startY, spacing);
    }

    private void loadLogFiles() {
        logFiles.clear();
        File logsDir = new File("neoporium_logs");
        if (logsDir.exists() && logsDir.isDirectory()) {
            File[] files = logsDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                Collections.addAll(logFiles, files);
            }
        }
    }

    private void createFileList(int centerX, int startY, int spacing) {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6Log Files"),
                button -> {}
        ).dimensions(centerX - 200, startY, 195, 20).build()).active = false;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6Log Entries"),
                button -> {}
        ).dimensions(centerX + 5, startY, 195, 20).build()).active = false;

        // File list buttons
        int fileY = startY + spacing;
        int maxFiles = Math.min(8, logFiles.size());

        for (int i = 0; i < maxFiles; i++) {
            File file = logFiles.get(i);
            String displayName = file.getName();
            if (displayName.length() > 20) {
                displayName = displayName.substring(0, 17) + "...";
            }

            boolean isSelected = file.equals(selectedFile);
            String buttonText = (isSelected ? "§a► " : "§7") + displayName;

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(buttonText),
                    button -> selectFile(file)
            ).dimensions(centerX - 200, fileY, 195, 20).build());

            fileY += spacing / 2;
        }

        // Show more files button if there are more
        if (logFiles.size() > 8) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§7... " + (logFiles.size() - 8) + " more files"),
                    button -> {}
            ).dimensions(centerX - 200, fileY, 195, 20).build()).active = false;
        }
    }

    private void createSearchAndActions(int centerX, int startY, int spacing) {
        // Search field
        searchField = new TextFieldWidget(
                this.textRenderer, centerX + 5, startY + spacing,
                195, 20, Text.literal("Search")
        );
        searchField.setPlaceholder(Text.literal("Search in logs..."));
        searchField.setChangedListener(text -> {
            currentSearch = text;
            if (selectedFile != null) {
                filterLogEntries(text);
            }
        });
        this.addDrawableChild(searchField);

        // Action buttons
        int actionY = startY + spacing * 2;

        // Open logs folder
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§6Open Logs Folder"),
                button -> openLogsFolder()
        ).dimensions(centerX + 5, actionY, 195, 20).build());

        // Refresh
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aRefresh"),
                button -> refreshLogs()
        ).dimensions(centerX + 5, actionY + spacing, 195, 20).build());

        // Export selected
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§eExport Selected"),
                button -> exportSelectedLog()
        ).dimensions(centerX + 5, actionY + spacing * 2, 195, 20).build());

        // Clear old logs
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§cClear Old Logs"),
                button -> clearOldLogs()
        ).dimensions(centerX + 5, actionY + spacing * 3, 195, 20).build());

        // Search in all logs
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§bSearch All Logs"),
                button -> searchAllLogs()
        ).dimensions(centerX + 5, actionY + spacing * 4, 195, 20).build());

        // Back button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§7Back to Scanner"),
                button -> goBack()
        ).dimensions(centerX - 200, this.height - 40, 400, 20).build());
    }

    private void createLogDisplayArea(int centerX, int startY, int spacing) {
        // Log entries will be rendered in the render() method
        // Navigation buttons for log entries
        if (!logEntries.isEmpty()) {
            int navY = startY + spacing * 9;

            // Previous page button
            if (scrollOffset > 0) {
                this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("§7▲ Previous"),
                        button -> {
                            scrollOffset = Math.max(0, scrollOffset - ENTRIES_PER_PAGE);
                        }
                ).dimensions(centerX + 5, navY, 95, 20).build());
            }

            // Next page button
            if (scrollOffset < logEntries.size() - ENTRIES_PER_PAGE) {
                this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("§7▼ Next"),
                        button -> {
                            scrollOffset = Math.min(logEntries.size() - ENTRIES_PER_PAGE, scrollOffset + ENTRIES_PER_PAGE);
                        }
                ).dimensions(centerX + 105, navY, 95, 20).build());
            }
        }
    }

    private void selectFile(File file) {
        selectedFile = file;
        loadLogEntries(file);
        this.clearAndInit();
    }

    private void loadLogEntries(File file) {
        logEntries.clear();
        scrollOffset = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logEntries.add(line);
            }
        } catch (IOException e) {
            logEntries.add("§cError loading log file: " + e.getMessage());
        }

        // Apply current search if any
        if (!currentSearch.isEmpty()) {
            filterLogEntries(currentSearch);
        }
    }

    private void filterLogEntries(String searchText) {
        if (selectedFile == null) return;

        if (searchText.isEmpty()) {
            loadLogEntries(selectedFile);
        } else {
            List<String> filtered = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains(searchText.toLowerCase())) {
                        filtered.add(line);
                    }
                }
            } catch (IOException e) {
                filtered.add("§cError filtering logs: " + e.getMessage());
            }
            logEntries.clear();
            logEntries.addAll(filtered);
            scrollOffset = 0;
        }
    }

    private void openLogsFolder() {
        try {
            File logsDir = new File("neoporium_logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            // Try to open folder (platform dependent)
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("explorer \"" + logsDir.getAbsolutePath() + "\"");
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open \"" + logsDir.getAbsolutePath() + "\"");
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec("xdg-open \"" + logsDir.getAbsolutePath() + "\"");
            }

            sendMessage("§6Opening logs folder...");

        } catch (Exception e) {
            sendMessage("§cError opening logs folder: " + e.getMessage());
            sendMessage("§7Folder location: neoporium_logs/");
        }
    }

    private void refreshLogs() {
        loadLogFiles();
        if (selectedFile != null && selectedFile.exists()) {
            loadLogEntries(selectedFile);
        }
        this.clearAndInit();
        sendMessage("§aLogs refreshed");
    }

    private void exportSelectedLog() {
        if (selectedFile == null) {
            sendMessage("§cPlease select a log file first");
            return;
        }

        try {
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String exportName = "export_" + timestamp + "_" + selectedFile.getName();
            Path exportPath = Paths.get("neoporium_logs", exportName);
            Files.copy(selectedFile.toPath(), exportPath, StandardCopyOption.REPLACE_EXISTING);
            sendMessage("§aExported as: " + exportName);
        } catch (IOException e) {
            sendMessage("§cExport failed: " + e.getMessage());
        }
    }

    private void clearOldLogs() {
        try {
            File logsDir = new File("neoporium_logs");
            if (logsDir.exists()) {
                File[] files = logsDir.listFiles();
                if (files != null) {
                    // Keep only last 20 files
                    Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                    int deleted = 0;
                    for (int i = 20; i < files.length; i++) {
                        if (files[i].delete()) {
                            deleted++;
                        }
                    }
                    refreshLogs();
                    sendMessage("§aCleared " + deleted + " old logs (kept last 20 files)");
                }
            }
        } catch (Exception e) {
            sendMessage("§cError clearing logs: " + e.getMessage());
        }
    }

    private void searchAllLogs() {
        if (currentSearch.isEmpty()) {
            sendMessage("§cPlease enter a search term first");
            return;
        }

        logEntries.clear();
        logEntries.add("§6Searching for: §e" + currentSearch);
        logEntries.add("§7Searching all log files...");

        File logsDir = new File("neoporium_logs");
        if (logsDir.exists()) {
            File[] files = logsDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                int totalMatches = 0;
                for (File file : files) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        int lineNum = 0;
                        while ((line = reader.readLine()) != null) {
                            lineNum++;
                            if (line.toLowerCase().contains(currentSearch.toLowerCase())) {
                                logEntries.add("§7[" + file.getName() + ":" + lineNum + "] §f" + line);
                                totalMatches++;
                            }
                        }
                    } catch (IOException e) {
                        logEntries.add("§cError reading " + file.getName() + ": " + e.getMessage());
                    }
                }
                logEntries.add(0, "§aFound " + totalMatches + " matches in all logs");
            }
        }

        scrollOffset = 0;
        this.clearAndInit();
    }

    private void goBack() {
        if (this.client != null) {
            this.client.setScreen(new AdvancedGuiScreen());
        }
    }

    private void sendMessage(String message) {
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("[Neoporium] " + message), false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--;
        } else if (verticalAmount < 0 && scrollOffset < Math.max(0, logEntries.size() - ENTRIES_PER_PAGE)) {
            scrollOffset++;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, "§6§lNeoporium Log Manager",
                this.width / 2, 15, 0xFFFFFF);

        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 25;

        // File info
        if (selectedFile != null) {
            context.drawTextWithShadow(this.textRenderer,
                    "§eSelected: §f" + selectedFile.getName(),
                    centerX - 200, startY + spacing * 6, 0xFFFFFF);
            context.drawTextWithShadow(this.textRenderer,
                    "§eSize: §f" + (selectedFile.length() / 1024) + " KB §7| §eEntries: §f" + logEntries.size(),
                    centerX - 200, startY + spacing * 6 + 12, 0xFFFFFF);
        }

        // Log entries display
        int logX = centerX + 5;
        int logY = startY + spacing * 3;

        context.drawTextWithShadow(this.textRenderer, "§6Log Entries:",
                logX, logY - 15, 0xFFFF55);

        int visibleEntries = Math.min(ENTRIES_PER_PAGE, logEntries.size() - scrollOffset);
        for (int i = 0; i < visibleEntries; i++) {
            int entryIndex = i + scrollOffset;
            if (entryIndex < logEntries.size()) {
                String entry = logEntries.get(entryIndex);

                // Format for display
                String displayEntry = entry;
                if (entry.matches("-?\\d+\\s+-?\\d+\\s+-?\\d+\\s+.*")) {
                    // It's a coordinate entry - format nicely
                    String[] parts = entry.split("\\s+", 4);
                    if (parts.length == 4) {
                        displayEntry = String.format("§7[%s, %s, %s] §f%s",
                                parts[0], parts[1], parts[2], parts[3]);
                    }
                }

                // Highlight search terms
                if (!currentSearch.isEmpty() && entry.toLowerCase().contains(currentSearch.toLowerCase())) {
                    displayEntry = displayEntry.replace(currentSearch, "§e" + currentSearch + "§f");
                }

                context.drawTextWithShadow(this.textRenderer, displayEntry,
                        logX, logY + (i * 10), 0xFFFFFF);
            }
        }

        // Scroll indicator
        if (logEntries.size() > ENTRIES_PER_PAGE) {
            int currentPage = (scrollOffset / ENTRIES_PER_PAGE) + 1;
            int totalPages = (logEntries.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE;
            String scrollInfo = String.format("§7Page %d/%d (↑↓ scroll, mouse wheel)",
                    currentPage, totalPages);
            context.drawTextWithShadow(this.textRenderer, scrollInfo,
                    centerX + 150, this.height - 60, 0x777777);
        }

        // File count
        context.drawTextWithShadow(this.textRenderer,
                "§7Total log files: §f" + logFiles.size(),
                centerX - 200, this.height - 60, 0xAAAAAA);

        // Search info
        if (!currentSearch.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer,
                    "§7Search: §f" + currentSearch,
                    centerX - 200, this.height - 45, 0xAAAAAA);
        }

        // Instructions
        context.drawTextWithShadow(this.textRenderer,
                "§7Select a log file to view entries. Use export to save copies.",
                centerX - 200, this.height - 30, 0x777777);

        super.render(context, mouseX, mouseY, delta);
    }
}