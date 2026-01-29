package com.emperium.neoporiumscanner.events;

import net.minecraft.client.option.KeyBinding;

public class KeyInputHandler {
    public static KeyBinding configKey;
    public static KeyBinding toggleKey;

    public static void initialize(KeyBinding configKeyBinding, KeyBinding toggleKeyBinding) {
        configKey = configKeyBinding;
        toggleKey = toggleKeyBinding;
    }
}