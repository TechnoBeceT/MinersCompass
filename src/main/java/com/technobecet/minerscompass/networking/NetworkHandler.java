package com.technobecet.minerscompass.networking;

import com.technobecet.minerscompass.MinersCompassMod;
import com.technobecet.minerscompass.item.ModItems;
import com.technobecet.minerscompass.util.DynamicOreType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkHandler {
    
    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(OreSelectionSyncPacket.TYPE, (packet, player, responseSender) -> {
            // Handle the packet on the server thread
            player.server.execute(() -> {
                MinersCompassMod.LOGGER.info("Received ore selection sync from player: {}", player.getName().getString());
                MinersCompassMod.LOGGER.info("Selected ore types: {}", packet.selectedOreTypeIds().size());
                
                // Find the compass in player's inventory
                ItemStack compassStack = null;
                if (player.getMainHandStack().getItem() == ModItems.ORE_COMPASS) {
                    compassStack = player.getMainHandStack();
                } else if (player.getOffHandStack().getItem() == ModItems.ORE_COMPASS) {
                    compassStack = player.getOffHandStack();
                } else {
                    // Search in inventory
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        ItemStack stack = player.getInventory().getStack(i);
                        if (stack.getItem() == ModItems.ORE_COMPASS) {
                            compassStack = stack;
                            break;
                        }
                    }
                }
                
                if (compassStack == null) {
                    MinersCompassMod.LOGGER.warn("Could not find compass in player inventory");
                    return;
                }
                
                // Update the compass NBT on server side
                NbtCompound nbt = compassStack.getOrCreateNbt();
                
                // Clear existing ore type keys
                List<String> keysToRemove = new ArrayList<>();
                for (String key : nbt.getKeys()) {
                    if (key.startsWith("SelectedOreTypes")) {
                        keysToRemove.add(key);
                    }
                }
                keysToRemove.forEach(nbt::remove);
                
                // Save new selection
                int index = 0;
                for (String oreTypeId : packet.selectedOreTypeIds()) {
                    nbt.putString("SelectedOreTypes" + index, oreTypeId);
                    index++;
                }
                
                MinersCompassMod.LOGGER.info("Updated compass NBT with {} ore types", packet.selectedOreTypeIds().size());
            });
        });
    }
}