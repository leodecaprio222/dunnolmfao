package com.emperium.neoporiumscanner.config;

import com.emperium.neoporiumscanner.gui.XRayConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        // FIXED: Pass parent screen to constructor
        return parent -> new XRayConfigScreen(parent);
    }
}