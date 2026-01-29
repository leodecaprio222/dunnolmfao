package com.emperium.neoporiumscanner.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;

public class CommandRegistry {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AdvancedCommands.registerCommands(dispatcher, registryAccess, environment);
        });
    }
}