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

    /** Every character that may follow '&' / section sign in a legacy code. */
    private static final String LEGACY_CODES = "0123456789abcdefklmnorxABCDEFKLMNORX";

    /** True if the text carries at least one real legacy colour/format code. */
    private static boolean hasLegacyCode(String raw) {
        for (int i = 0; i + 1 < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c == '&' || c == '\u00A7') && LEGACY_CODES.indexOf(raw.charAt(i + 1)) >= 0) {
                return true;
            }
        }
        return false;
    }

    public static Component parse(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        // Legacy codes win over the angle-bracket heuristic. NametagEdit configs
        // very often use angle brackets as decoration ("&7<&bVIP&7>"); reading those
        // as MiniMessage left the '&' codes visible as literal text.
        if (!hasLegacyCode(raw) && raw.indexOf('<') >= 0 && raw.indexOf('>') > raw.indexOf('<')) {
            try {
                return MINI.deserialize(raw);
            } catch (Exception ignored) {
                // fall through to legacy
            }
        }
        return LEGACY.deserialize(raw);
    }
}
