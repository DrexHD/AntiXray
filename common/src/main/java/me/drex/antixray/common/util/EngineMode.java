package me.drex.antixray.common.util;

public enum EngineMode {
    HIDE(1, "hide ores"),
    OBFUSCATE(2, "obfuscate"),
    OBFUSCATE_LAYER(3, "obfuscate_layer");

    private final int id;

    EngineMode(int id, String description) {
        this.id = id;
    }

    public static EngineMode getById(int id) {
        for (EngineMode engineMode : values()) {
            if (engineMode.id == id) {
                return engineMode;
            }
        }

        return null;
    }
}
