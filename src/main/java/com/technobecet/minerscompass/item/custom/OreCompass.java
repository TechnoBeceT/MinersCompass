package com.technobecet.minerscompass.item.custom;

import com.technobecet.minerscompass.MinersCompassMod;
import com.technobecet.minerscompass.util.ModTags;
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

public class OreCompass extends Item {

    public static final String TARGET_BLOCK_LIST_KEY = "TargetBlockList";
    public static final String TRACKED_BLOCK_KEY = "TrackedBlock";
    public static final String TARGET_BLOCK_POS_KEY = "TargetBlockPos";
    public static final String TARGET_BLOCK_DIMENSION_KEY = "TargetBlockDimension";
    public static final String TARGET_BLOCK_TRACKED_KEY = "TargetBlockTracked";

    public OreCompass(Settings settings) {
        super(settings);
    }

    @Nullable
    public static GlobalPos getTrackedPos(NbtCompound nbt) {

        if (nbt == null)
            return null;

        boolean hasTrackedValue = hasNbtKey(nbt, TARGET_BLOCK_TRACKED_KEY);
        if (!hasTrackedValue) {
            return null;
        }
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

        List<Block> targetBlocks = getBlocksFromNbt(stack.getNbt());
        if (targetBlocks.isEmpty()) {
            NbtCompound nbt = stack.getOrCreateNbt();
            for (String key : nbt.getKeys()) {
                if (key.startsWith(TARGET_BLOCK_LIST_KEY) || key.equals(TARGET_BLOCK_POS_KEY) || key.equals(TARGET_BLOCK_DIMENSION_KEY) || key.equals(TARGET_BLOCK_TRACKED_KEY)) {
                    nbt.remove(key);
                }
            }
            return Optional.empty();
        }

        var trackedPos = getTrackedPos(stack.getNbt());
        if (!force && trackedPos != null) {
            BlockState trackedBlockState = world.getBlockState(trackedPos.getPos());
            if (targetBlocks.contains(trackedBlockState.getBlock())) {
                return Optional.of(trackedPos.getPos());
            } else {
                var dimKey = getTrackedDimension(stack.getNbt());
                if (dimKey.isPresent()
                        && !dimKey.get().toString().equals(entity.getWorld().getRegistryKey().toString())) {
                    return Optional.empty();
                }
            }
        }

        Optional<BlockPos> closest = findBlocksInNearbyChunks(stack, world, entity.getBlockPos());
        playSoundOnStateChange(world, entity, stack, closest);
        writeNbt(world.getRegistryKey(), closest, stack.getOrCreateNbt());

        return closest;
    }

    private static Optional<RegistryKey<World>> getTrackedDimension(NbtCompound nbt) {
        return World.CODEC.parse(NbtOps.INSTANCE, nbt.get(TARGET_BLOCK_DIMENSION_KEY)).result();
    }

    private static void playSoundOnStateChange(World world, Entity entity, ItemStack stack,
                                               Optional<BlockPos> closest) {
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
        if (nbt == null) {
            return;
        }
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

    private static Optional<BlockPos> findBlocksInNearbyChunks(ItemStack itemStack, World world, BlockPos entPos) {
        int chunkRadius = Math.max(0, MinersCompassMod.config.chunkRadius);
        Long2ObjectOpenHashMap<Set<BlockPos>> blocks = new Long2ObjectOpenHashMap<>();
        var chunkPos = world.getChunk(entPos).getPos();

        NbtCompound nbt = itemStack.getOrCreateNbt();
        List<Block> targetBlocks = getBlocksFromNbt(nbt);
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
        }

        return Optional.ofNullable(closestPos);
    }

    private static List<Block> getBlocksFromNbt(NbtCompound nbt) {
        if (nbt == null)
            return Collections.emptyList();
        List<Block> blocks = new ArrayList<>();

        for (String key : nbt.getKeys()) {
            if (key.startsWith(TARGET_BLOCK_LIST_KEY)) {
                Block block = Registries.BLOCK.get(new Identifier(nbt.getString(key)));
                blocks.add(block);
            }
        }

        return blocks;
    }

    private static boolean hasNbtKey(NbtCompound nbt, String key) {
        return nbt != null && nbt.contains(key);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            return TypedActionResult.success(user.getStackInHand(hand));
        }

        ItemStack itemStack = user.getStackInHand(hand);
        NbtCompound nbt = itemStack.getOrCreateNbt();

        if (user.isSneaking()) {
            clearNbtRecords(nbt);

            user.sendMessage(Text.translatable("item.miners-compass.ore_compass.cleared_blocks"), true);
            return TypedActionResult.success(itemStack);
        }

        if (nbt == null || isBlockListEmpty(nbt)) {
            user.sendMessage(Text.translatable("item.miners-compass.ore_compass.no_blocks"), true);
            return TypedActionResult.success(itemStack);
        }


        user.getItemCooldownManager().set(this, 100);
        Optional<BlockPos> pos = findBlocks(user.getMainHandStack(), world, user, true);
        playSound(world, user, pos.isPresent());

        return TypedActionResult.success(itemStack);
    }

    private void clearNbtRecords(NbtCompound nbt) {
        if (nbt == null) return;
        List<String> keysToRemove = new ArrayList<>();
        for (String key : nbt.getKeys()) {
            if (key.startsWith(TARGET_BLOCK_LIST_KEY) || key.equals(TARGET_BLOCK_POS_KEY) || key.equals(TARGET_BLOCK_DIMENSION_KEY) || key.equals(TARGET_BLOCK_TRACKED_KEY) || key.equals(TRACKED_BLOCK_KEY)) {
                keysToRemove.add(key);
            }
        }
        keysToRemove.forEach(nbt::remove);
    }

    private boolean isBlockListEmpty(NbtCompound nbt) {
        if (nbt == null)
            return true;
        for (String key : nbt.getKeys()) {
            if (key.startsWith(TARGET_BLOCK_LIST_KEY)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        ItemStack itemStack = context.getStack();
        BlockState clickedBlockState = context.getWorld().getBlockState(context.getBlockPos());
        Block clickedBlock = clickedBlockState.getBlock();
        String clickedBlockId = Registries.BLOCK.getId(clickedBlock).toString();
        String clickedBlockName = clickedBlock.getName().getString();

        if (player != null && player.isSneaking()) {
            if (!isValuableBlock(clickedBlockState)) {
                player.sendMessage(Text.of(Text.translatable("item.miners-compass.ore_compass.not_valuable", clickedBlockName).getString()), true);
                return ActionResult.SUCCESS;
            }
            NbtCompound nbt = itemStack.getOrCreateNbt();
            String existingKey = findKeyForBlock(nbt, clickedBlockId);
            String message;

            if (existingKey != null) {
                nbt.remove(existingKey);
                message = Text.translatable("item.miners-compass.ore_compass.removed_block", clickedBlockName).getString();
                if (isBlockListEmpty(nbt)) {
                    clearNbtRecords(nbt);
                }
            } else {
                List<Block> existingBlocks = getBlocksFromNbt(nbt);
                if (existingBlocks.size() < MinersCompassMod.config.maxBlocks) {
                    String nextBlockKey = findNextBlockKey(nbt);
                    nbt.putString(nextBlockKey, clickedBlockId);
                    message = Text.translatable("item.miners-compass.ore_compass.added_block", clickedBlockName).getString();
                } else {
                    message = Text.translatable("item.miners-compass.ore_compass.max_blocks").getString();
                }
            }

            player.sendMessage(Text.literal(message), true);
            return ActionResult.SUCCESS;
        }

        return super.useOnBlock(context);
    }

    private String findNextBlockKey(NbtCompound nbt) {
        int index = 0;
        while (hasNbtKey(nbt, TARGET_BLOCK_LIST_KEY + index)) {
            index++;
        }
        return TARGET_BLOCK_LIST_KEY + index;
    }

    private String findKeyForBlock(NbtCompound nbt, String blockId) {
        for (String key : nbt.getKeys()) {
            if (key.startsWith(TARGET_BLOCK_LIST_KEY) && nbt.getString(key).equals(blockId)) {
                return key;
            }
        }
        return null;
    }

    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        if (tooltipContext.isCreative()) {
            return;
        }
        NbtCompound nbt = itemStack.getNbt();
        if (getTrackedPos(nbt) != null) {
            String trackedBlockName = hasNbtKey(nbt, TRACKED_BLOCK_KEY)
                    ? Registries.BLOCK.get(new Identifier(nbt.getString(TRACKED_BLOCK_KEY))).getName().getString()
                    : "Unknown";
            tooltip.add(
                    Text.translatable("tooltip.miners-compass.ore_compass.hint").formatted(Formatting.GRAY));
            var dimKey = getTrackedDimension(itemStack.getNbt());
            if (dimKey.isPresent() && !dimKey.get().toString().equals(world.getRegistryKey().toString())) {
                tooltip.add(
                        Text.translatable("tooltip.miners-compass.ore_compass.wrong_dim1", trackedBlockName)
                                .formatted(Formatting.DARK_RED).formatted(Formatting.BOLD));
                tooltip.add(
                        Text.translatable("tooltip.miners-compass.ore_compass.wrong_dim2")
                                .formatted(Formatting.DARK_RED).formatted(Formatting.BOLD));
            } else {
                tooltip.add(
                        Text.translatable("tooltip.miners-compass.ore_compass.locked_on", trackedBlockName)
                                .formatted(Formatting.RED));
            }

        } else if (!isBlockListEmpty(nbt)) {
            tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.not_found")
                    .formatted(Formatting.DARK_PURPLE));
        } else {
            tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.no_ores")
                    .formatted(Formatting.DARK_PURPLE));
        }

        if (nbt != null && !isBlockListEmpty(nbt)) {
            List<Block> selectedBlocks = getBlocksFromNbt(nbt);
            if (!selectedBlocks.isEmpty()) {
                if (Screen.hasShiftDown()) {
                    tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.selected_blocks").formatted(Formatting.YELLOW));
                    for (Block block : selectedBlocks) {
                        String blockName = block.getName().getString();
                        tooltip.add(Text.literal(" - " + blockName).formatted(Formatting.WHITE));
                    }
                } else {
                    tooltip.add(Text.translatable("tooltip.miners-compass.ore_compass.tooltip"));
                }
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        findBlocks(stack, world, entity, false);
    }

    private boolean isValuableBlock(BlockState blockState) {
        return blockState.isIn(ModTags.Blocks.ORE_COMPASS_DETECTABLE_BLOCKS);
    }
}
