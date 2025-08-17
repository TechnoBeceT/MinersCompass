package com.technobecet.minerscompass.client.gui;

import com.technobecet.minerscompass.MinersCompassMod;
import com.technobecet.minerscompass.networking.OreSelectionSyncPacket;
import com.technobecet.minerscompass.util.DynamicOreType;
import com.technobecet.minerscompass.util.OreTypeManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class OreSelectionScreen extends Screen {
    private final ItemStack compassStack;
    private final Map<DynamicOreType, ButtonWidget> oreButtons = new HashMap<>();
    private Set<DynamicOreType> selectedOreTypes = new HashSet<>();
    private ButtonWidget clearAllButton;
    private ButtonWidget doneButton;

    public OreSelectionScreen(ItemStack compassStack) {
        super(Text.translatable("screen.miners-compass.ore_selection"));
        this.compassStack = compassStack;
        loadSelectedOreTypes();
    }

    private void loadSelectedOreTypes() {
        NbtCompound nbt = compassStack.getNbt();
        if (nbt == null) return;

        for (String key : nbt.getKeys()) {
            if (key.startsWith("SelectedOreTypes")) {
                String oreTypeId = nbt.getString(key);
                for (DynamicOreType type : DynamicOreType.getAllTypes()) {
                    if (type.getId().equals(oreTypeId)) {
                        selectedOreTypes.add(type);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 120;
        int buttonHeight = 20;
        int buttonsPerRow = 3;
        int spacing = 10;
        int startX = (width - (buttonsPerRow * buttonWidth + (buttonsPerRow - 1) * spacing)) / 2;
        int startY = 50;

        // Ensure ore discovery is up to date when GUI opens
        OreTypeManager.initialize();
        List<DynamicOreType> availableOreTypes = OreTypeManager.getAvailableOreTypes();
        
        for (int i = 0; i < availableOreTypes.size(); i++) {
            DynamicOreType oreType = availableOreTypes.get(i);
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;
            
            int x = startX + col * (buttonWidth + spacing);
            int y = startY + row * (buttonHeight + spacing);
            
            ButtonWidget button = ButtonWidget.builder(
                getOreTypeButtonText(oreType),
                btn -> toggleOreType(oreType)
            ).dimensions(x, y, buttonWidth, buttonHeight).build();
            
            oreButtons.put(oreType, button);
            addDrawableChild(button);
        }

        // Clear All button
        clearAllButton = ButtonWidget.builder(
            Text.translatable("screen.miners-compass.ore_selection.clear_all"),
            btn -> clearAll()
        ).dimensions(width / 2 - 155, height - 50, 100, 20).build();
        addDrawableChild(clearAllButton);

        // Done button
        doneButton = ButtonWidget.builder(
            Text.translatable("screen.miners-compass.ore_selection.done"),
            btn -> done()
        ).dimensions(width / 2 + 55, height - 50, 100, 20).build();
        addDrawableChild(doneButton);

        updateButtonStates();
    }

    private Text getOreTypeButtonText(DynamicOreType oreType) {
        int variantCount = OreTypeManager.getVariantCount(oreType);
        boolean isSelected = selectedOreTypes.contains(oreType);
        
        String buttonText = oreType.getDisplayName();
        if (variantCount > 1) {
            buttonText += " (" + variantCount + ")";
        }
        
        if (isSelected) {
            buttonText = "✓ " + buttonText;
        }
        
        return Text.literal(buttonText).formatted(isSelected ? oreType.getColor() : Formatting.GRAY);
    }

    private void toggleOreType(DynamicOreType oreType) {
        if (selectedOreTypes.contains(oreType)) {
            selectedOreTypes.remove(oreType);
        } else {
            if (selectedOreTypes.size() < MinersCompassMod.config.maxBlocks) {
                selectedOreTypes.add(oreType);
            }
        }
        updateButtonStates();
    }

    private void clearAll() {
        selectedOreTypes.clear();
        updateButtonStates();
    }

    private void done() {
        saveSelectedOreTypes();
        
        // Send packet to sync ore selection to server
        MinersCompassMod.LOGGER.info("Sending ore selection sync packet with {} ore types", selectedOreTypes.size());
        OreSelectionSyncPacket packet = OreSelectionSyncPacket.create(selectedOreTypes);
        ClientPlayNetworking.send(packet);
        
        close();
    }

    private void updateButtonStates() {
        for (Map.Entry<DynamicOreType, ButtonWidget> entry : oreButtons.entrySet()) {
            DynamicOreType oreType = entry.getKey();
            ButtonWidget button = entry.getValue();
            
            button.setMessage(getOreTypeButtonText(oreType));
            
            // Disable button if max selection reached and not currently selected
            boolean canSelect = selectedOreTypes.size() < MinersCompassMod.config.maxBlocks || selectedOreTypes.contains(oreType);
            button.active = canSelect;
        }
        
        clearAllButton.active = !selectedOreTypes.isEmpty();
    }

    private void saveSelectedOreTypes() {
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
        for (DynamicOreType oreType : selectedOreTypes) {
            nbt.putString("SelectedOreTypes" + index, oreType.getId());
            index++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        
        // Title
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
        
        // Selection info
        String selectionInfo = selectedOreTypes.size() + "/" + MinersCompassMod.config.maxBlocks + " ore types selected";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(selectionInfo), width / 2, height - 75, 0xAAAAAA);
        
        // Instructions
        String instructions = "Click ore types to toggle selection";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(instructions), width / 2, height - 65, 0x888888);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}