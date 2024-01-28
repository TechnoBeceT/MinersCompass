# Elevate Your Mining Experience!

The Miner's Compass is an innovative tool designed for the savvy Minecraft miner. With its unique functionality, this
compass is not just a direction finder but a game-changer in your mining adventures.

This mod is perfect for miners who want to streamline their resource gathering, offering a smart, customizable, and
efficient way to locate essential ores. Enhance your mining efficiency and never miss valuable ore deposits again with
the Miner's Compass!

## Key Features

### Ore Detection Customization

Customize your search with ease by shift-right-clicking any ore block. The compass can store up to 3 ore types, a limit
adjustable via the **_maxBlocks_** variable in the configuration file.

### Dynamic Targeting System

As you explore the Minecraft world, the compass dynamically targets nearby ores from your selected list, scanning an
impressive 5x5 chunk radius. This radius is adjustable through the **_chunkRadius_** variable.

### Exclusive Ore Compatibility

Designed specifically for ore detection, the compass initially works with a range of ores. This includes gold, coal,
copper, diamond, iron, lapis, redstone, emerald ores, and nether quartz ore.

### Extendable Ore List

Enhance the compass's capabilities by creating a datapack with *
*_data/miners-compass/tags/blocks/ore_compass_detectable_blocks.json_** file, allowing the addition of more ores for
detection.

### Smart Memory Management

To add an ore to your search list, simply shift-right-click the ore block for removing do same process again. For a
fresh start, a shift-right-click while looking at the air clears your entire search list.

### Crafting Recipe

![Crafting Recipe](https://cdn.modrinth.com/data/tPs9k0db/images/911f5ac3881396fe92675c434f9a6ad87eaaa68e.png)

<details>
<summary>Example ore_compass_detectable_blocks.json</summary>

```
{
  "replace": false,
  "values": [
    "#minecraft:gold_ores",
    "#minecraft:coal_ores",
    "#minecraft:copper_ores",
    "#minecraft:diamond_ores",
    "#minecraft:iron_ores",
    "#minecraft:lapis_ores",
    "#minecraft:redstone_ores",
    "#minecraft:emerald_ores",
    "minecraft:nether_quartz_ore"
  ]
}
```

</details>

<details>
<summary>Example config</summary>

```
{
    //The entity's chunk, 1 = 3x3 chunks around the entity, 2 = 5x5 etc...
    "chunkRadius": 2,
    //Max holdable ore block amount
    "maxBlocks": 3
}
```

</details>

# Q&A

### Forge/Backport?

No, I mainly play Fabric.

### Curse Forge?

Nope, Modrinth is the reason i did start publishing my mods.
