# Anti Xray

Anti Xray is a lightweight fabric mod that allows server owners to combat xrayers.

## Config

```toml
enabled = false

[overworld]
enabled = true
engineMode = 2
maxBlockHeight = 256
updateRadius = 2
lavaObscures = false
hiddenBlocks = ["copper_ore", "deepslate_copper_ore", "gold_ore", "deepslate_gold_ore", "iron_ore", "deepslate_iron_ore", "coal_ore", "deepslate_coal_ore", "lapis_ore", "deepslate_lapis_ore", "mossy_cobblestone", "obsidian", "chest", "diamond_ore", "deepslate_diamond_ore", "redstone_ore", "deepslate_redstone_ore", "clay", "emerald_ore", "deepslate_emerald_ore", "ender_chest"]
replacementBlocks = ["stone", "deepslate"]

[the_nether]
enabled = true
engineMode = 1
maxBlockHeight = 128
updateRadius = 2
lavaObscures = false
hiddenBlocks = ["ancient_debris", "nether_quartz_ore", "nether_gold_ore"]
replacementBlocks = ["netherrack"]
```

### Config option overview

`enabled` if set to true anti xray will be active in the specified world

`engineMode` can either be 1 or 2, see [Engine Modes](#Engine Modes)

`maxBlockHeight` controls the max height at which blocks should get obfuscated

`updateRadius` controls how many blocks away from shown blocks obfuscation should start (if your players see fake ores
it is recommended to increase this value)

`lavaObscures` if set to true blocks next to lava will get obscured

`hiddenBlocks` is a list of block ids that will get hidden

`replacementBlocks` is a list of blocks that will get used for block obfuscation

### Engine Modes

| Info | Image |
:-------------------------:|:-------------------------:
**Anti xray disabled:** This is just for reference |  ![](https://i.imgur.com/ypGa36J.png)
**EngineMode 1:** This mode will replace all fully obscured (no air around) blocks from `hiddenBlocks` with blocks from `replacementBlocks` |  ![](https://i.imgur.com/Lnnbrr0.png)
**EngineMode 2 (recommended):** This mode will replace all blocks from `hiddenBlocks` and `replacementBlocks` with random blocks from `hiddenBlocks` |  ![](https://i.imgur.com/Sx49SFD.png)
**Legit player view:** Legit players wont notice any changes when this mod is installed (unless they have high ping or modify a lot of blocks at once, eg: explosions) |  ![](https://i.imgur.com/HRC0heX.png)

## Other
Players with `antixray.bypass` permission will be excluded 

## About

This mod is a port of
Papers [Async Anti Xray Patch](https://github.com/PaperMC/Paper/blob/master/patches/server/0367-Anti-Xray.patch) from
1.17
and [it's 1.14 patch](https://github.com/PaperMC/Paper/blob/ver/1.14/Spigot-Server-Patches/0397-Anti-Xray.patch#L1379)
for networking code, to fabric
