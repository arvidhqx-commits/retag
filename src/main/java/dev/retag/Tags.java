package dev.retag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/** Text parsing: accepts legacy '&' codes and MiniMessage, returns Components. */
public final class Tags {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private Tags() {}

    public static Component parse(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        // Heuristic: MiniMessage if it contains a tag, otherwise legacy codes.
        if (raw.indexOf('<') >= 0 && raw.indexOf('>') > raw.indexOf('<')) {
            try {
                return MINI.deserialize(raw);
            } catch (Exception ignored) {
                // fall through to legacy
            }
        }
        return LEGACY.deserialize(raw);
    }
}
