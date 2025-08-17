package com.technobecet.minerscompass.item.custom;

import com.technobecet.minerscompass.MinersCompassMod;
import com.technobecet.minerscompass.util.DynamicOreDetection;
import com.technobecet.minerscompass.util.DynamicOreType;
import com.technobecet.minerscompass.util.OreTypeManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class OreCompass extends Item {

    public static final String SELECTED_ORE_TYPES_KEY = "SelectedOreTypes";
    public static final String TRACKED_BLOCK_KEY = "TrackedBlock";
    public static final String TRACKED_ORE_TYPE_KEY = "TrackedOreType";
    public static final String TARGET_BLOCK_POS_KEY = "TargetBlockPos";
    public static final String TARGET_BLOCK_DIMENSION_KEY = "TargetBlockDimension";
    public static final String TARGET_BLOCK_TRACKED_KEY = "TargetBlockTracked";

    public OreCompass(Settings settings) {
        super(settings);
    }

    @Nullable
    public static GlobalPos getTrackedPos(NbtCompound nbt) {
        if (nbt == null) return null;

        boolean hasTrackedValue = hasNbtKey(nbt, TARGET_BLOCK_TRACKED_KEY);
        if (!hasTrackedValue) return null;
        
        boolean tracked = nbt.getBoolean(TARGET_BLOCK_TRACKED_KEY);

        Optional<RegistryKey<World>> worldKey;
        boolean hasPosKey = hasNbtKey(nbt, TARGET_BLOCK_POS_KEY);
        boolean hasDimKey = hasNbtKey(nbt, TARGET_BLOCK_DIMENSION_KEY);

        if (hasPosKey && hasDimKey && tracked && (worldKey = getTrackedDimension(nbt)).isPresent()) {
            BlockPos blockPos = NbtHelper.toBlockPos(nbt.getCompound(TARGET_BLOCK_POS_KEY));
            return GlobalPos.create(worldKey.get(), blockPos);
        }
        return null;
    }

    private static Optional<BlockPos> findBlocks(ItemStack stack, World world, Entity entity, boolean force) {
        if (world.isClient) {
            return Optional.empty();
        }

        MinersCompassMod.LOGGER.info("Finding blocks for player: {} in world: {}", entity.getName().getString(), world.getRegistryKey().getValue());
        
        Set<DynamicOreType> selectedOreTypes = getSelectedOreTypesFromNbt(stack.getNbt());
        MinersCompassMod.LOGGER.info("Selected ore types: {}", selectedOreTypes.size());
        
        if (selectedOreTypes.isEmpty()) {
            MinersCompassMod.LOGGER.info("No ore types selected - clearing NBT and returning empty");
            clearNbtRecords(stack.getOrCreateNbt());
            return Optional.empty();
        }

        List<Block> targetBlocks = OreTypeManager.getAllBlocksForOreTypes(selectedOreTypes);
        MinersCompassMod.LOGGER.info("Target blocks found: {}", targetBlocks.size());
        
        if (targetBlocks.isEmpty()) {
            MinersCompassMod.LOGGER.info("No target blocks found - clearing NBT and returning empty");
            clearNbtRecords(stack.getOrCreateNbt());
            return Optional.empty();
        }

        var trackedPos = getTrackedPos(stack.getNbt());
        if (!force && trackedPos != null) {
            BlockState trackedBlockState = world.getBlockState(trackedPos.getPos());
            if (targetBlocks.contains(trackedBlockState.getBlock())) {
                return Optional.of(trackedPos.getPos());
            } else {
                var dimKey = getTrackedDimension(stack.getNbt());
                if (dimKey.isPresent() && !dimKey.get().toString().equals(entity.getWorld().getRegistryKey().toString())) {
                    return Optional.empty();
                }
            }
        }

        Optional<BlockPos> closest = findBlocksInNearbyChunks(stack, world, entity.getBlockPos(), targetBlocks);
        MinersCompassMod.LOGGER.info("Closest ore block found: {}", closest.isPresent() ? closest.get() : "none");
        
        playSoundOnStateChange(world, entity, stack, closest);
        writeNbt(world.getRegistryKey(), closest, stack.getOrCreateNbt());

        return closest;
    }

    private static Optional<RegistryKey<World>> getTrackedDimension(NbtCompound nbt) {
        return World.CODEC.parse(NbtOps.INSTANCE, nbt.get(TARGET_BLOCK_DIMENSION_KEY)).result();
    }

    private static void playSoundOnStateChange(World world, Entity entity, ItemStack stack, Optional<BlockPos> closest) {
        if (closest.isPresent()) {
            playSound(world, entity, true);
            return;
        }
        var trackedPos = getTrackedPos(stack.getNbt());
        if (!closest.isPresent() && trackedPos != null) {
            playSound(world, entity, false);
        }
    }

    private static void writeNbt(RegistryKey<World> worldKey, Optional<BlockPos> closest, NbtCompound nbt) {
        if (nbt == null) return;
        
        if (closest.isPresent()) {
            BlockPos pos = closest.get();
            nbt.put(TARGET_BLOCK_POS_KEY, NbtHelper.fromBlockPos(pos));
            World.CODEC.encodeStart(NbtOps.INSTANCE, worldKey)
                    .resultOrPartial(MinersCompassMod.LOGGER::error)
                    .ifPresent(nbtElement -> nbt.put(TARGET_BLOCK_DIMENSION_KEY, nbtElement));
            nbt.putBoolean(TARGET_BLOCK_TRACKED_KEY, true);
        } else {
            nbt.put(TARGET_BLOCK_POS_KEY, NbtHelper.fromBlockPos(BlockPos.ORIGIN));
            nbt.putBoolean(TARGET_BLOCK_TRACKED_KEY, false);
            nbt.remove(TARGET_BLOCK_DIMENSION_KEY);
        }
    }

    private static void playSound(World world, Entity entity, boolean success) {
        world.playSound(null, entity.getBlockPos(),
                success ? SoundEvents.ITEM_LODESTONE_COMPASS_LOCK : SoundEvents.BLOCK_FIRE_EXTINGUISH,
                SoundCategory.PLAYERS, 1f, 1f);
    }

    private static Optional<BlockPos> findBlocksInNearbyChunks(ItemStack itemStack, World world, BlockPos entPos, List<Block> targetBlocks) {
        int chunkRadius = Math.max(0, MinersCompassMod.config.chunkRadius);
        Long2ObjectOpenHashMap<Set<BlockPos>> blocks = new Long2ObjectOpenHashMap<>();
        var chunkPos = world.getChunk(entPos).getPos();

        BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
        int startY = world.getBottomY();
        int endY = world.getTopY();

        for (int x = chunkPos.x - chunkRadius; x <= chunkPos.x + chunkRadius; x++) {
            for (int z = chunkPos.z - chunkRadius; z <= chunkPos.z + chunkRadius; z++) {
                Chunk chunk = world.getChunk(x, z);
                for (int i = 0; i < 16; ++i) {
                    for (int j = startY; j < endY; ++j) {
                        for (int k = 0; k < 16; ++k) {
                            mutableBlockPos.set(x * 16 + i, j, z * 16 + k);
                            BlockState blockState = world.getBlockState(mutableBlockPos);
                            if (targetBlocks.contains(blockState.getBlock())) {
                                blocks.computeIfAbsent(chunk.getPos().toLong(), key -> new HashSet<>())
                                        .add(mutableBlockPos.toImmutable());
                            }
                        }
                    }
                }
            }
        }
        return getClosestBlockPos(itemStack, blocks, entPos, world);
    }

    private static Optional<BlockPos> getClosestBlockPos(ItemStack itemStack, Long2ObjectOpenHashMap<Set<BlockPos>> blockMap, BlockPos entPos, World world) {
        double closestDistanceSq = Double.MAX_VALUE;
        BlockPos closestPos = null;
        Block closestBlock = null;

        for (Set<BlockPos> positions : blockMap.values()) {
            for (BlockPos blockPos : positions) {
                double dx = blockPos.getX() - entPos.getX();
                double dy = blockPos.getY() - entPos.getY();
                double dz = blockPos.getZ() - entPos.getZ();
                double distanceSq = dx * dx + dy * dy + dz * dz;

                if (distanceSq < closestDistanceSq) {
                    closestPos = blockPos;
                    closestDistanceSq = distanceSq;
                    closestBlock = world.getBlockState(closestPos).getBlock();
                }
            }
        }

        if (closestPos != null && closestBlock != null) {
            NbtCompound nbt = itemStack.getOrCreateNbt();
            nbt.putString(TRACKED_BLOCK_KEY, Registries.BLOCK.getId(closestBlock).toString());
            
            Optional<DynamicOreType> oreType = OreTypeManager.getOreTypeForBlock(closestBlock);
            oreType.ifPresent(type -> nbt.putString(TRACKED_ORE_TYPE_KEY, type.getId()));
        }

        return Optional.ofNullable(closestPos);
    }

    private static Set<DynamicOreType> getSelectedOreTypesFromNbt(NbtCompound nbt) {
        if (nbt == null) return Collections.emptySet();
        
        Set<DynamicOreType> oreTypes = new HashSet<>();
        for (String key : nbt.getKeys()) {
            if (key.startsWith(SELECTED_ORE_TYPES_KEY)) {
                String oreTypeId = nbt.getString(key);
                // Find existing ore type by ID
                for (DynamicOreType type : DynamicOreType.getAllTypes()) {
                    if (type.getId().equals(oreTypeId)) {
                        oreTypes.add(type);
                        break;
                    }
                }
            }
        }
        return oreTypes;
    }

    private static void saveSelectedOreTypesToNbt(NbtCompound nbt, Set<DynamicOreType> oreTypes) {
        if (nbt == null) return;
        
        clearOreTypeKeys(nbt);
        
        int index = 0;
        for (DynamicOreType oreType : oreTypes) {
            nbt.putString(SELECTED_ORE_TYPES_KEY + index, oreType.getId());
            index++;
        }
    }

    private static void clearOreTypeKeys(NbtCompound nbt) {
        List<String> keysToRemove = new ArrayList<>();
        for (String key : nbt.getKeys()) {
            if (key.startsWith(SELECTED_ORE_TYPES_KEY)) {
                keysToRemove.add(key);
            }
        }
        keysToRemove.forEach(nbt::remove);
    }

    private static boolean hasNbtKey(NbtCompound nbt, String key) {
        return nbt != null && nbt.contains(key);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        MinersCompassMod.LOGGER.info("Ore compass right-clicked by {} in world: {} (isClient: {})", 
            user.getName().getString(), world.getRegistryKey().getValue(), world.isClient);
            
        ItemStack itemStack = user.getStackInHand(hand);
        NbtCompound nbt = itemStack.getOrCreateNbt();

        if (user.isSneaking()) {
            if (world.isClient) {
                return TypedActionResult.success(itemStack);
            }
            clearNbtRecords(nbt);
            user.sendMessage(Text.translatable("item.miners-compass.ore_compass.cleared_all"), true);
            return TypedActionResult.success(itemStack);
        }

        if (world.isClient) {
            // Open GUI on client side
            openOreSelectionGui(itemStack);
            return TypedActionResult.success(itemStack);
        }

        Set<DynamicOreType> selectedOreTypes = getSelectedOreTypesFromNbt(nbt);
        if (selectedOreTypes.isEmpty()) {
            user.sendMessage(Text.translatable("item.miners-compass.ore_compass.no_ore_types"), true);
            return TypedActionResult.success(itemStack);
        }

        user.getItemCooldownManager().set(this, 100);
        Optional<BlockPos> pos = findBlocks(user.getMainHandStack(), world, user, true);
        playSound(world, user, pos.isPresent());

        return TypedActionResult.success(itemStack);
    }

    @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
    private void openOreSelectionGui(ItemStack itemStack) {
        net.minecraft.client.MinecraftClient.getInstance().setScreen(
            new com.technobecet.minerscompass.client.gui.OreSelectionScreen(itemStack)
        );
    }

    private static void clearNbtRecords(NbtCompound nbt) {
        if (nbt == null) return;
        List<String> keysToRemove = new ArrayList<>();
        for (String key : nbt.getKeys()) {
            if (key.startsWith(SELECTED_ORE_TYPES_KEY) || key.equals(TARGET_BLOCK_POS_KEY) || 
                key.equals(TARGET_BLOCK_DIMENSION_KEY) || key.equals(TARGET_BLOCK_TRACKED_KEY) || 
                key.equals(TRACKED_BLOCK_KEY) || key.equals(TRACKED_ORE_TYPE_KEY)) {
                keysToRemove.add(key);
            }
        }
        keysToRemove.forEach(nbt::remove);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        ItemStack itemStack = context.getStack();
        BlockState clickedBlockState = context.getWorld().getBlockState(context.getBlockPos());
        Block clickedBlock = clickedBlockState.getBlock();

        if (player != null && player.isSneaking()) {
            if (!DynamicOreDetection.isValidOre(clickedBlock)) {
                String blockName = clickedBlock.getName().getString();
                player.sendMessage(Text.translatable("item.miners-compass.ore_compass.not_ore", blockName), true);
                return ActionResult.SUCCESS;
            }

            Optional<DynamicOreType> oreTypeOpt = OreTypeManager.getOreTypeForBlock(clickedBlock);
            if (oreTypeOpt.isEmpty()) {
                player.sendMessage(Text.translatable("item.miners-compass.ore_compass.unknown_ore"), true);
                return ActionResult.SUCCESS;
            }

            DynamicOreType oreType = oreTypeOpt.get();
            NbtCompound nbt = itemStack.getOrCreateNbt();
            Set<DynamicOreType> selectedOreTypes = getSelectedOreTypesFromNbt(nbt);

            if (selectedOreTypes.contains(oreType)) {
                selectedOreTypes.remove(oreType);
                OreTypeManager.sendOreTypeMessage(player, oreType, false);
                if (selectedOreTypes.isEmpty()) {
                    clearNbtRecords(nbt);
                }
            } else {
                if (selectedOreTypes.size() < MinersCompassMod.config.maxBlocks) {
                    selectedOreTypes.add(oreType);
                    OreTypeManager.sendOreTypeMessage(player, oreType, true);
                } else {
                    player.sendMessage(Text.translatable("item.miners-compass.ore_compass.max_ore_types"), true);
                    return ActionResult.SUCCESS;
                }
            }

            saveSelectedOreTypesToNbt(nbt, selectedOreTypes);
            return ActionResult.SUCCESS;
        }

        return super.useOnBlock(context);
    }

    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        if (tooltipContext.isCreative()) return;
        
        NbtCompound nbt = itemStack.getNbt();
        Set<DynamicOreType> selectedOreTypes = getSelectedOreTypesFromNbt(nbt);

        if (getTrackedPos(nbt) != null) {
            String trackedOreTypeName = "Unknown";
            if (hasNbtKey(nbt, TRACKED_ORE_TYPE_KEY)) {
                String oreTypeId = nbt.getString(TRACKED_ORE_TYPE_KEY);
                for (DynamicOreType type : DynamicOreType.getAllTypes()) {
                    if (type.getId().equals(oreTypeId)) {
                        trackedOreTypeName = type.getDisplayName();
                        break;
                    }
                }
            }

            tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.hint").formatted(Formatting.GRAY));
            
            var dimKey = getTrackedDimension(nbt);
            if (dimKey.isPresent() && !dimKey.get().toString().equals(world.getRegistryKey().toString())) {
                tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.wrong_dim1", trackedOreTypeName)
                        .formatted(Formatting.DARK_RED).formatted(Formatting.BOLD));
                tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.wrong_dim2")
                        .formatted(Formatting.DARK_RED).formatted(Formatting.BOLD));
            } else {
                tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.locked_on", trackedOreTypeName)
                        .formatted(Formatting.RED));
            }
        } else if (!selectedOreTypes.isEmpty()) {
            tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.not_found")
                    .formatted(Formatting.DARK_PURPLE));
        } else {
            tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.no_ore_types")
                    .formatted(Formatting.DARK_PURPLE));
        }

        if (!selectedOreTypes.isEmpty()) {
            if (Screen.hasShiftDown()) {
                tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.selected_ore_types")
                        .formatted(Formatting.YELLOW));
                for (DynamicOreType oreType : selectedOreTypes) {
                    int variantCount = OreTypeManager.getVariantCount(oreType);
                    String displayText = variantCount > 1 
                        ? oreType.getDisplayName() + " (" + variantCount + " variants)"
                        : oreType.getDisplayName();
                    tooltip.add(Text.literal(" - " + displayText).formatted(oreType.getColor()));
                }
            } else {
                tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.tooltip"));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        findBlocks(stack, world, entity, false);
    }
}