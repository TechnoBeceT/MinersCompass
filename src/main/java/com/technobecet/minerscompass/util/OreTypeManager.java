package com.technobecet.minerscompass.util;

import com.technobecet.minerscompass.MinersCompassMod;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.*;
import java.util.stream.Collectors;

public class OreTypeManager {
    
    private static Map<DynamicOreType, List<Block>> oreTypeToBlocks = null;
    private static Map<Block, DynamicOreType> blockToOreType = null;
    
    public static void initialize() {
        if (oreTypeToBlocks == null) {
            MinersCompassMod.LOGGER.info("Initializing ore type mappings...");
            CustomOreTypeConfig.registerCustomOreTypes();
            buildOreTypeMappings();
            MinersCompassMod.LOGGER.info("Ore type mapping complete! Found {} ore types", oreTypeToBlocks.size());
        }
    }
    
    public static void refresh() {
        MinersCompassMod.LOGGER.info("Refreshing ore type mappings...");
        oreTypeToBlocks = null;
        blockToOreType = null;
        DynamicOreDetection.refreshOreList();
        initialize();
        MinersCompassMod.LOGGER.info("Ore type mappings refreshed!");
    }
    
    private static void buildOreTypeMappings() {
        oreTypeToBlocks = new HashMap<>();
        blockToOreType = new HashMap<>();
        
        MinersCompassMod.LOGGER.info("Starting ore discovery process...");
        Set<Block> allOres = DynamicOreDetection.getDiscoveredOres();
        MinersCompassMod.LOGGER.info("Found {} potential ore blocks", allOres.size());
        
        // Debug: Log some example discovered blocks
        int debugCount = 0;
        for (Block block : allOres) {
            if (debugCount < 10) {
                MinersCompassMod.LOGGER.info("Example discovered ore: {}", 
                    com.technobecet.minerscompass.util.DynamicOreDetection.getBlockId(block));
                debugCount++;
            }
            
            DynamicOreType oreType = DynamicOreType.fromBlock(block);
            if (oreType != null) {
                oreTypeToBlocks.computeIfAbsent(oreType, k -> new ArrayList<>()).add(block);
                blockToOreType.put(block, oreType);
            }
        }
        
        // Debug: Log ore types found
        MinersCompassMod.LOGGER.info("Mapped {} blocks into {} ore types:", 
            blockToOreType.size(), oreTypeToBlocks.size());
        for (DynamicOreType oreType : oreTypeToBlocks.keySet()) {
            List<Block> blocks = oreTypeToBlocks.get(oreType);
            MinersCompassMod.LOGGER.info("  {}: {} variants", oreType.getDisplayName(), blocks.size());
        }
    }
    
    public static Map<DynamicOreType, List<Block>> getOreTypeToBlocks() {
        initialize();
        return new HashMap<>(oreTypeToBlocks);
    }
    
    public static List<Block> getBlocksForOreType(DynamicOreType oreType) {
        initialize();
        return oreTypeToBlocks.getOrDefault(oreType, new ArrayList<>());
    }
    
    public static Optional<DynamicOreType> getOreTypeForBlock(Block block) {
        initialize();
        return Optional.ofNullable(blockToOreType.get(block));
    }
    
    public static int getVariantCount(DynamicOreType oreType) {
        return getBlocksForOreType(oreType).size();
    }
    
    public static List<DynamicOreType> getAvailableOreTypes() {
        initialize();
        return oreTypeToBlocks.keySet().stream()
                .filter(type -> !getBlocksForOreType(type).isEmpty())
                .sorted(Comparator.comparing(DynamicOreType::getDisplayName))
                .collect(Collectors.toList());
    }
    
    public static boolean isOreTypeSelected(Block block, Set<DynamicOreType> selectedTypes) {
        Optional<DynamicOreType> oreType = getOreTypeForBlock(block);
        return oreType.isPresent() && selectedTypes.contains(oreType.get());
    }
    
    public static void sendOreTypeMessage(PlayerEntity player, DynamicOreType oreType, boolean added) {
        int variantCount = getVariantCount(oreType);
        Text message;
        
        if (added) {
            if (variantCount > 1) {
                message = Text.translatable("item.miners-compass.ore_compass.ore_type_added_variants", 
                    oreType.getDisplayName(), variantCount);
            } else {
                message = Text.translatable("item.miners-compass.ore_compass.ore_type_added", 
                    oreType.getDisplayName());
            }
        } else {
            message = Text.translatable("item.miners-compass.ore_compass.ore_type_removed", 
                oreType.getDisplayName());
        }
        
        player.sendMessage(message, true);
    }
    
    public static List<Block> getAllBlocksForOreTypes(Set<DynamicOreType> oreTypes) {
        initialize();
        return oreTypes.stream()
                .flatMap(type -> getBlocksForOreType(type).stream())
                .collect(Collectors.toList());
    }
    
    public static Set<DynamicOreType> getOreTypesFromBlocks(List<Block> blocks) {
        return blocks.stream()
                .map(OreTypeManager::getOreTypeForBlock)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }
}