package dev.retag;

/** Per-player overrides. Empty string means "not set" (group value applies). */
public final class PlayerData {
    public String prefix = "";
    public String suffix = "";

    public boolean isEmpty() {
        return prefix.isEmpty() && suffix.isEmpty();
    }
}
