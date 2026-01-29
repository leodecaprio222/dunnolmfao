package com.emperium.neoporiumscanner.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.emperium.neoporiumscanner.NeoporiumScanner;
import com.emperium.neoporiumscanner.config.ConfigManager;
import com.emperium.neoporiumscanner.xray.render.RenderManager;
import com.emperium.neoporiumscanner.core.ScanController;
import java.util.Map;

public class AdvancedCommands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                        CommandRegistryAccess registryAccess,
                                        CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal("neoscanner")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.literal("gui")
                        .executes(context -> openGui(context)))
                .then(CommandManager.literal("xray")
                        .then(CommandManager.literal("on")
                                .executes(context -> setXRay(context, true)))
                        .then(CommandManager.literal("off")
                                .executes(context -> setXRay(context, false)))
                        .then(CommandManager.literal("opacity")
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 100))
                                        .executes(context -> setXRayOpacity(context))))
                        .then(CommandManager.literal("range")
                                .then(CommandManager.argument("distance", IntegerArgumentType.integer(1, 512))
                                        .executes(context -> setXRayRange(context))))
                        .executes(context -> showXRayStatus(context)))
                .then(CommandManager.literal("esp")
                        .then(CommandManager.literal("on")
                                .executes(context -> setESP(context, true)))
                        .then(CommandManager.literal("off")
                                .executes(context -> setESP(context, false)))
                        .then(CommandManager.literal("mode")
                                .then(CommandManager.argument("mode", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("BOX");
                                            builder.suggest("WIREFRAME");
                                            builder.suggest("BOTH");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> setESPMode(context))))
                        .then(CommandManager.literal("thickness")
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(1, 10))
                                        .executes(context -> setESPThickness(context))))
                        .then(CommandManager.literal("range")
                                .then(CommandManager.argument("distance", IntegerArgumentType.integer(1, 512))
                                        .executes(context -> setESPRange(context))))
                        .then(CommandManager.literal("fade")
                                .then(CommandManager.literal("on")
                                        .executes(context -> setESPFade(context, true)))
                                .then(CommandManager.literal("off")
                                        .executes(context -> setESPFade(context, false))))
                        .then(CommandManager.literal("colors")
                                .executes(context -> listESPColors(context)))
                        .executes(context -> showESPStatus(context)))
                .then(CommandManager.literal("scan")
                        .then(CommandManager.literal("start")
                                .executes(context -> startScan(context)))
                        .then(CommandManager.literal("stop")
                                .executes(context -> stopScan(context)))
                        .then(CommandManager.literal("range")
                                .then(CommandManager.argument("distance", IntegerArgumentType.integer(1, 512))
                                        .executes(context -> setScanRange(context))))
                        .executes(context -> toggleScan(context)))
                .then(CommandManager.literal("reload")
                        .executes(context -> reloadConfig(context)))
                .then(CommandManager.literal("help")
                        .executes(context -> showHelp(context)))
                .executes(context -> showStatus(context))
        );
    }

    // Command implementations
    private static int openGui(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(
                Text.literal("Opening Neoporium Scanner GUI...").formatted(Formatting.GREEN)
        );
        NeoporiumScanner.openGui();
        return 1;
    }

    private static int setXRay(CommandContext<ServerCommandSource> context, boolean enabled) {
        ConfigManager.setXRayEnabled(enabled);
        context.getSource().sendMessage(
                Text.literal("XRay " + (enabled ? "enabled" : "disabled")).formatted(
                        enabled ? Formatting.GREEN : Formatting.RED)
        );
        return 1;
    }

    private static int setXRayOpacity(CommandContext<ServerCommandSource> context) {
        int opacity = IntegerArgumentType.getInteger(context, "value");
        ConfigManager.setXRayOpacity(opacity / 100.0f);
        context.getSource().sendMessage(
                Text.literal("XRay opacity set to " + opacity + "%").formatted(Formatting.GREEN)
        );
        return 1;
    }

    private static int setXRayRange(CommandContext<ServerCommandSource> context) {
        int distance = IntegerArgumentType.getInteger(context, "distance");
        ConfigManager.setXRayDistance(distance);
        context.getSource().sendMessage(
                Text.literal("XRay range set to " + distance + " blocks").formatted(Formatting.GREEN)
        );
        return 1;
    }

    private static int setESP(CommandContext<ServerCommandSource> context, boolean enabled) {
        ConfigManager.setESPEnabled(enabled);
        context.getSource().sendMessage(
                Text.literal("ESP " + (enabled ? "enabled" : "disabled")).formatted(
                        enabled ? Formatting.GREEN : Formatting.RED)
        );
        return 1;
    }

    private static int setESPMode(CommandContext<ServerCommandSource> context) {
        String mode = StringArgumentType.getString(context, "mode").toUpperCase();
        if (mode.equals("BOX") || mode.equals("WIREFRAME") || mode.equals("BOTH")) {
            ConfigManager.setESPMode(mode);
            context.getSource().sendMessage(
                    Text.literal("ESP mode set to " + mode).formatted(Formatting.GREEN)
            );
        } else {
            context.getSource().sendMessage(
                    Text.literal("Invalid ESP mode. Use BOX, WIREFRAME, or BOTH").formatted(Formatting.RED)
            );
        }
        return 1;
    }

    private static int setESPThickness(CommandContext<ServerCommandSource> context) {
        int thickness = IntegerArgumentType.getInteger(context, "value");
        ConfigManager.setESPThickness(thickness);
        context.getSource().sendMessage(
                Text.literal("ESP thickness set to " + thickness).formatted(Formatting.GREEN)
        );
        return 1;
    }

    private static int setESPRange(CommandContext<ServerCommandSource> context) {
        int distance = IntegerArgumentType.getInteger(context, "distance");
        ConfigManager.setESPDistance(distance);
        context.getSource().sendMessage(
                Text.literal("ESP range set to " + distance + " blocks").formatted(Formatting.GREEN)
        );
        return 1;
    }

    private static int setESPFade(CommandContext<ServerCommandSource> context, boolean enabled) {
        ConfigManager.setESPFadeEnabled(enabled);
        context.getSource().sendMessage(
                Text.literal("ESP distance fade " + (enabled ? "enabled" : "disabled")).formatted(
                        enabled ? Formatting.GREEN : Formatting.RED)
        );
        return 1;
    }

    private static int setScanRange(CommandContext<ServerCommandSource> context) {
        int distance = IntegerArgumentType.getInteger(context, "distance");
        ConfigManager.setScanRange(distance);
        context.getSource().sendMessage(
                Text.literal("Scan range set to " + distance + " blocks").formatted(Formatting.GREEN)
        );
        return 1;
    }

    private static int startScan(CommandContext<ServerCommandSource> context) {
        ScanController.startScan();
        context.getSource().sendMessage(
                Text.literal("Scan started").formatted(Formatting.GREEN)
        );
        return 1;
    }

    private static int stopScan(CommandContext<ServerCommandSource> context) {
        ScanController.stopScan();
        context.getSource().sendMessage(
                Text.literal("Scan stopped").formatted(Formatting.RED)
        );
        return 1;
    }

    private static int toggleScan(CommandContext<ServerCommandSource> context) {
        if (ScanController.isScanning()) {
            return stopScan(context);
        } else {
            return startScan(context);
        }
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ConfigManager.load();
        context.getSource().sendMessage(
                Text.literal("Configuration reloaded").formatted(Formatting.GREEN)
        );
        return 1;
    }

    private static int listESPColors(CommandContext<ServerCommandSource> context) {
        Map<String, int[]> colors = ConfigManager.getESPColors();
        context.getSource().sendMessage(
                Text.literal("=== ESP Colors ===").formatted(Formatting.GOLD)
        );

        if (colors.isEmpty()) {
            context.getSource().sendMessage(
                    Text.literal("No ESP colors configured").formatted(Formatting.GRAY)
            );
        } else {
            for (Map.Entry<String, int[]> entry : colors.entrySet()) {
                int[] rgb = entry.getValue();
                Text colorText = Text.literal("■ ").formatted(Formatting.byColorIndex(rgb[0] * 36 + rgb[1] * 6 + rgb[2]))
                        .append(Text.literal(" " + entry.getKey() + ": RGB(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")"));
                context.getSource().sendMessage(colorText);
            }
        }
        return 1;
    }

    private static int showXRayStatus(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(
                Text.literal("=== XRay Status ===").formatted(Formatting.GOLD)
        );
        context.getSource().sendMessage(
                Text.literal("Enabled: ").formatted(Formatting.YELLOW)
                        .append(Text.literal(ConfigManager.isXRayEnabled() ? "YES" : "NO")
                                .formatted(ConfigManager.isXRayEnabled() ? Formatting.GREEN : Formatting.RED))
        );
        context.getSource().sendMessage(
                Text.literal("Opacity: " + (int)(ConfigManager.getXRayOpacity() * 100) + "%").formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("Range: " + ConfigManager.getXRayDistance() + " blocks").formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("See-Through: ").formatted(Formatting.YELLOW)
                        .append(Text.literal(ConfigManager.isXRaySeeThrough() ? "YES" : "NO")
                                .formatted(ConfigManager.isXRaySeeThrough() ? Formatting.GREEN : Formatting.RED))
        );
        return 1;
    }

    private static int showESPStatus(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(
                Text.literal("=== ESP Status ===").formatted(Formatting.GOLD)
        );
        context.getSource().sendMessage(
                Text.literal("Enabled: ").formatted(Formatting.YELLOW)
                        .append(Text.literal(ConfigManager.isESPEnabled() ? "YES" : "NO")
                                .formatted(ConfigManager.isESPEnabled() ? Formatting.GREEN : Formatting.RED))
        );
        context.getSource().sendMessage(
                Text.literal("Mode: " + ConfigManager.getESPMode()).formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("Thickness: " + ConfigManager.getESPThickness()).formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("Range: " + ConfigManager.getESPDistance() + " blocks").formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("Fade: ").formatted(Formatting.YELLOW)
                        .append(Text.literal(ConfigManager.isESPFadeEnabled() ? "YES" : "NO")
                                .formatted(ConfigManager.isESPFadeEnabled() ? Formatting.GREEN : Formatting.RED))
        );
        return 1;
    }

    private static int showStatus(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(
                Text.literal("=== Neoporium Scanner ===").formatted(Formatting.GOLD)
        );

        // Scanning status
        context.getSource().sendMessage(
                Text.literal("Scanning: ").formatted(Formatting.YELLOW)
                        .append(Text.literal(ScanController.isScanning() ? "ACTIVE" : "INACTIVE")
                                .formatted(ScanController.isScanning() ? Formatting.GREEN : Formatting.RED))
        );

        // XRay status
        context.getSource().sendMessage(
                Text.literal("XRay: ").formatted(Formatting.YELLOW)
                        .append(Text.literal(ConfigManager.isXRayEnabled() ? "ON" : "OFF")
                                .formatted(ConfigManager.isXRayEnabled() ? Formatting.GREEN : Formatting.RED))
        );

        // ESP status
        context.getSource().sendMessage(
                Text.literal("ESP: ").formatted(Formatting.YELLOW)
                        .append(Text.literal(ConfigManager.isESPEnabled() ? "ON" : "OFF")
                                .formatted(ConfigManager.isESPEnabled() ? Formatting.GREEN : Formatting.RED))
                        .append(Text.literal(" (" + ConfigManager.getESPMode() + ")").formatted(Formatting.GRAY))
        );

        // Range info
        context.getSource().sendMessage(
                Text.literal("Scan Range: " + ConfigManager.getScanRange() + " blocks").formatted(Formatting.YELLOW)
        );

        // Help hint
        context.getSource().sendMessage(
                Text.literal("Use /neoscanner help for commands").formatted(Formatting.GRAY)
        );

        return 1;
    }

    private static int showHelp(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(
                Text.literal("=== Neoporium Scanner Commands ===").formatted(Formatting.GOLD)
        );
        context.getSource().sendMessage(
                Text.literal("/neoscanner gui - Open GUI").formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("/neoscanner xray <on|off|opacity|range> - XRay controls").formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("/neoscanner esp <on|off|mode|thickness|range|fade|colors> - ESP controls").formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("/neoscanner scan <start|stop|range> - Scanning controls").formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("/neoscanner reload - Reload configuration").formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("/neoscanner help - Show this help").formatted(Formatting.YELLOW)
        );
        context.getSource().sendMessage(
                Text.literal("/neoscanner - Show status").formatted(Formatting.YELLOW)
        );
        return 1;
    }
}