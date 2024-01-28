package com.technobecet.minerscompass;

import com.technobecet.minerscompass.config.ModConfig;
import com.technobecet.minerscompass.item.ModItems;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinersCompassMod implements ModInitializer {
    public static final String MOD_ID = "miners-compass";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ModConfig config;

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        ModItems.registerModItems();
    }
}
