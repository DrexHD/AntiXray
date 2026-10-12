# Loaded palette preset regression

Run with Python 3 and a JDK (Java 25 is used in CI):

```sh
python3 tests/test_preset_palettes.py
```

The test extracts `addPresetValuesWithEntries` from the checked-out mixin and compiles it in a standalone Java harness. It does not copy the implementation under test. A palette model preserves duplicate loaded slots, as Lithium's loaded palette constructor does, and rejects resizing during the subsequent packet-palette lookup.

Ten cases cover full duplicate palettes with missing presets, duplicate palettes whose presets already exist, explicit resizing of unique palettes, and nonfull palettes. The missing-preset cases include 16 slots with 15 unique states and 64 slots with 53 unique states, matching palette shapes observed in an isolated Minecraft/Fabric reproduction. The unpatched method fails four cases; the patched method passes all ten.

To test a different revision without modifying the checkout:

```sh
git show fabric-1.4.16+26.3:common/src/main/java/me/drex/antixray/common/mixin/PalettedContainerMixin.java > /tmp/PalettedContainerMixin.java
python3 tests/test_preset_palettes.py /tmp/PalettedContainerMixin.java
```

This is an algorithm regression, not a Minecraft integration test. The model does not exercise mixin application, block-storage remapping, client decoding, or concurrency. Separate integration results for the actual Minecraft/Fabric packet path are recorded in issue #88; no world files or private logs are needed by this test.
