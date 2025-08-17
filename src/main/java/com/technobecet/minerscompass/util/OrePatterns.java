package com.technobecet.minerscompass.util;

import net.minecraft.util.Formatting;
import java.util.*;
import java.util.Arrays;

public class OrePatterns {
    
    public static class OreDefinition {
        public final String displayName;
        public final Formatting color;
        public final List<String> blockIds;
        
        public OreDefinition(String displayName, Formatting color, String... blockIds) {
            this.displayName = displayName;
            this.color = color;
            this.blockIds = Arrays.asList(blockIds);
        }
    }
    
    // Comprehensive whitelist of known ore patterns
    private static final Map<String, OreDefinition> KNOWN_ORES = new LinkedHashMap<>();
    
    static {
        // === VANILLA ORES ===
        addOre("coal", "Coal Ore", Formatting.DARK_GRAY,
            "minecraft:coal_ore", "minecraft:deepslate_coal_ore");
            
        addOre("iron", "Iron Ore", Formatting.WHITE,
            "minecraft:iron_ore", "minecraft:deepslate_iron_ore");
            
        addOre("gold", "Gold Ore", Formatting.YELLOW,
            "minecraft:gold_ore", "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore");
            
        addOre("diamond", "Diamond Ore", Formatting.AQUA,
            "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore");
            
        addOre("emerald", "Emerald Ore", Formatting.GREEN,
            "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
            "blue_skies:everbright_emerald_ore", "blue_skies:everdawn_emerald_ore");
            
        addOre("copper", "Copper Ore", Formatting.GOLD,
            "minecraft:copper_ore", "minecraft:deepslate_copper_ore", "immersiveengineering:ore_copper");
            
        addOre("lapis", "Lapis Ore", Formatting.BLUE,
            "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore");
            
        addOre("redstone", "Redstone Ore", Formatting.RED,
            "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore");
            
        addOre("quartz", "Quartz Ore", Formatting.WHITE,
            "minecraft:nether_quartz_ore");
            
        addOre("netherite", "Ancient Debris", Formatting.DARK_PURPLE,
            "minecraft:ancient_debris");
        
        // === CREATE MOD ===
        addOre("zinc", "Zinc Ore", Formatting.GRAY,
            "create:zinc_ore", "create:deepslate_zinc_ore");
        
        // === MYTHIC METALS ===
        addOre("adamantite", "Adamantite Ore", Formatting.DARK_PURPLE,
            "mythicmetals:adamantite_ore", "mythicmetals:deepslate_adamantite_ore");
            
        addOre("aquarium", "Aquarium Ore", Formatting.AQUA,
            "mythicmetals:aquarium_ore");
            
        addOre("banglum", "Banglum Ore", Formatting.YELLOW,
            "mythicmetals:banglum_ore", "mythicmetals:nether_banglum_ore");
            
        addOre("carmot", "Carmot Ore", Formatting.RED,
            "mythicmetals:carmot_ore", "mythicmetals:deepslate_carmot_ore");
            
        addOre("kyber", "Kyber Ore", Formatting.GREEN,
            "mythicmetals:kyber_ore", "mythicmetals:calcite_kyber_ore");
            
        addOre("manganese", "Manganese Ore", Formatting.DARK_GRAY,
            "mythicmetals:manganese_ore");
            
        addOre("morkite", "Morkite Ore", Formatting.GREEN,
            "mythicmetals:morkite_ore", "mythicmetals:deepslate_morkite_ore");
            
        addOre("midas_gold", "Midas Gold Ore", Formatting.GOLD,
            "mythicmetals:midas_gold_ore");
            
        addOre("mythril", "Mythril Ore", Formatting.LIGHT_PURPLE,
            "mythicmetals:mythril_ore", "mythicmetals:deepslate_mythril_ore");
            
        addOre("orichalcum", "Orichalcum Ore", Formatting.GREEN,
            "mythicmetals:orichalcum_ore", "mythicmetals:tuff_orichalcum_ore", 
            "mythicmetals:smooth_basalt_orichalcum_ore", "mythicmetals:deepslate_orichalcum_ore");
            
        addOre("osmium", "Osmium Ore", Formatting.BLUE,
            "mythicmetals:osmium_ore");
            
        addOre("palladium", "Palladium Ore", Formatting.YELLOW,
            "mythicmetals:palladium_ore");
            
        addOre("platinum", "Platinum Ore", Formatting.WHITE,
            "mythicmetals:platinum_ore");
            
        addOre("prometheum", "Prometheum Ore", Formatting.DARK_GREEN,
            "mythicmetals:prometheum_ore", "mythicmetals:deepslate_prometheum_ore");
            
        addOre("runite", "Runite Ore", Formatting.DARK_BLUE,
            "mythicmetals:runite_ore", "mythicmetals:deepslate_runite_ore");
            
        addOre("silver", "Silver Ore", Formatting.WHITE,
            "mythicmetals:silver_ore", "clutter:silver_ore", "clutter:deepslate_silver_ore",
            "werewolves:silver_ore", "werewolves:deepslate_silver_ore",
            "immersiveengineering:ore_silver", "immersiveengineering:deepslate_ore_silver");
            
        addOre("starrite", "Starrite Ore", Formatting.YELLOW,
            "mythicmetals:starrite_ore", "mythicmetals:calcite_starrite_ore", 
            "mythicmetals:end_stone_starrite_ore");
            
        addOre("stormyx", "Stormyx Ore", Formatting.DARK_PURPLE,
            "mythicmetals:stormyx_ore", "mythicmetals:blackstone_stormyx_ore");
            
        addOre("tin", "Tin Ore", Formatting.WHITE,
            "mythicmetals:tin_ore");
            
        addOre("unobtainium", "Unobtainium Ore", Formatting.DARK_PURPLE,
            "mythicmetals:unobtainium_ore", "mythicmetals:deepslate_unobtainium_ore");
        
        // === SOULSWEAPONS ===
        addOre("moonstone", "Moonstone Ore", Formatting.LIGHT_PURPLE,
            "soulsweapons:moonstone_ore", "soulsweapons:moonstone_ore_deepslate");
            
        addOre("blue_skies_moonstone", "Blue Skies Moonstone", Formatting.AQUA,
            "blue_skies:everbright_moonstone_ore", "blue_skies:everdawn_moonstone_ore",
            "blue_skies:moonstone_ore_glow");
            
        addOre("verglas", "Verglas Ore", Formatting.AQUA,
            "soulsweapons:verglas_ore", "soulsweapons:verglas_ore_deepslate");
        
        // === BLUE SKIES ===
        addOre("aquite", "Aquite Ore", Formatting.AQUA,
            "blue_skies:everbright_aquite_ore", "blue_skies:everdawn_aquite_ore");
            
        addOre("charoite", "Charoite Ore", Formatting.DARK_PURPLE,
            "blue_skies:everbright_charoite_ore", "blue_skies:everdawn_charoite_ore");
            
        addOre("diopside", "Diopside Ore", Formatting.GREEN,
            "blue_skies:everbright_diopside_ore", "blue_skies:everdawn_diopside_ore");
            
        addOre("pyrope", "Pyrope Ore", Formatting.RED,
            "blue_skies:everbright_pyrope_ore", "blue_skies:everdawn_pyrope_ore");
            
        addOre("falsite", "Falsite Ore", Formatting.YELLOW,
            "blue_skies:falsite_ore");
            
        addOre("horizonite", "Horizonite Ore", Formatting.DARK_BLUE,
            "blue_skies:horizonite_ore");
            
        addOre("ventium", "Ventium Ore", Formatting.GREEN,
            "blue_skies:ventium_ore");
        
        // === SPECIFIC MOD ORES ===
        addOre("sulfur", "Sulfur Ore", Formatting.YELLOW,
            "clutter:blackstone_sulphur_ore", "clutter:basalt_sulphur_ore",
            "butcher:sulfurore", "butcher:deepslatesulfurore");
            
        addOre("salt", "Salt Ore", Formatting.WHITE,
            "expandeddelight:salt_ore", "expandeddelight:deepslate_salt_ore");
            
        addOre("onyx", "Onyx Ore", Formatting.DARK_GRAY,
            "clutter:onyx_ore");
            
        addOre("gleaming", "Gleaming Ore", Formatting.YELLOW,
            "things:gleaming_ore", "things:deepslate_gleaming_ore");
        
        // === AMETHYST IMBUEMENT ===
        addOre("tigers_eye", "Tiger's Eye Ore", Formatting.GOLD,
            "amethyst_imbuement:tigers_eye_blackstone_ore", "amethyst_imbuement:tigers_eye_basalt_ore");
            
        addOre("crystalline", "Crystalline Core", Formatting.LIGHT_PURPLE,
            "amethyst_imbuement:crystalline_core");
        
        // === BETTER END ===
        addOre("ender", "Ender Ore", Formatting.DARK_GREEN,
            "betterend:ender_ore");
            
        addOre("amber", "Amber Ore", Formatting.GOLD,
            "betterend:amber_ore");
            
        addOre("thallasium", "Thallasium Ore", Formatting.AQUA,
            "betterend:thallasium_ore");
        
        // === IMMERSIVE ENGINEERING ===
        addOre("aluminum", "Aluminum Ore", Formatting.LIGHT_PURPLE,
            "immersiveengineering:ore_aluminum", "immersiveengineering:deepslate_ore_aluminum");
            
        addOre("lead", "Lead Ore", Formatting.DARK_GRAY,
            "immersiveengineering:ore_lead", "immersiveengineering:deepslate_ore_lead");
            
        addOre("nickel", "Nickel Ore", Formatting.YELLOW,
            "immersiveengineering:ore_nickel", "immersiveengineering:deepslate_ore_nickel");
            
        addOre("uranium", "Uranium Ore", Formatting.GREEN,
            "immersiveengineering:ore_uranium", "immersiveengineering:deepslate_ore_uranium");
        
        // === OTHER MODS ===
        addOre("seabrass", "Seabrass Ore", Formatting.GREEN,
            "abyssal_decor:seabrass_ore");
            
        addOre("spinel", "Spinel Ore", Formatting.RED,
            "boh:spinel_ore_ore");
            
        addOre("etyr", "Etyr Ore", Formatting.DARK_PURPLE,
            "eldritch_end:etyr_ore");
            
        addOre("fossil", "Fossil Ore", Formatting.GRAY,
            "netherexp:fossil_ore");
            
        addOre("fossil_fuel", "Fossil Fuel Ore", Formatting.DARK_GRAY,
            "netherexp:fossil_fuel_ore");
            
        addOre("grimstone", "Grimstone Ore", Formatting.DARK_GRAY,
            "deep_dark_regrowth:grimstone_iron_ore", "deep_dark_regrowth:grimstone_gold_ore",
            "deep_dark_regrowth:grimstone_diamond_ore", "deep_dark_regrowth:grimstone_emerald_ore",
            "deep_dark_regrowth:grimstone_redstone_ore", "deep_dark_regrowth:grimstone_lapislazuli_ore");
    }
    
    private static void addOre(String id, String displayName, Formatting color, String... patterns) {
        KNOWN_ORES.put(id, new OreDefinition(displayName, color, patterns));
    }
    
    public static Optional<String> identifyOre(String blockId) {
        String normalizedId = blockId.toLowerCase();
        
        // First try exact matching
        for (Map.Entry<String, OreDefinition> entry : KNOWN_ORES.entrySet()) {
            String oreId = entry.getKey();
            OreDefinition definition = entry.getValue();
            
            for (String knownBlockId : definition.blockIds) {
                if (normalizedId.equals(knownBlockId.toLowerCase())) {
                    return Optional.of(oreId);
                }
            }
        }
        
        // Fallback: try pattern matching for missed ores
        if (normalizedId.contains("_ore") || normalizedId.contains("ore_") || normalizedId.endsWith("ore")) {
            // Extract potential ore name for unknown ores
            String path = normalizedId.substring(normalizedId.indexOf(':') + 1);
            
            // Common patterns
            if (path.matches(".*zinc.*ore.*")) return Optional.of("zinc");
            if (path.matches(".*sulfur.*ore.*") || path.matches(".*sulphur.*ore.*")) return Optional.of("sulfur");
            if (path.matches(".*moonstone.*ore.*")) {
                // Distinguish between SoulsWeapons and Blue Skies moonstone
                if (normalizedId.startsWith("blue_skies:")) {
                    return Optional.of("blue_skies_moonstone");
                } else {
                    return Optional.of("moonstone");
                }
            }
            if (path.matches(".*silver.*ore.*")) return Optional.of("silver");
            if (path.matches(".*salt.*ore.*")) return Optional.of("salt");
            if (path.matches(".*gleaming.*ore.*")) return Optional.of("gleaming");
            if (path.matches(".*onyx.*ore.*")) return Optional.of("onyx");
            if (path.matches(".*aluminum.*ore.*") || path.matches(".*ore.*aluminum.*")) return Optional.of("aluminum");
            if (path.matches(".*lead.*ore.*") || path.matches(".*ore.*lead.*")) return Optional.of("lead");
            if (path.matches(".*nickel.*ore.*") || path.matches(".*ore.*nickel.*")) return Optional.of("nickel");
            if (path.matches(".*uranium.*ore.*") || path.matches(".*ore.*uranium.*")) return Optional.of("uranium");
        }
        
        return Optional.empty();
    }
    
    public static Optional<OreDefinition> getOreDefinition(String oreId) {
        return Optional.ofNullable(KNOWN_ORES.get(oreId));
    }
    
    public static Set<String> getAllKnownOreIds() {
        return KNOWN_ORES.keySet();
    }
    
    public static Map<String, OreDefinition> getAllDefinitions() {
        return Collections.unmodifiableMap(KNOWN_ORES);
    }
}