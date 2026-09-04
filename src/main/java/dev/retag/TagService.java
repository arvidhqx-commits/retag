package dev.retag;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Comparator;
import java.util.Locale;

/**
 * Applies nametags via scoreboard teams on the main scoreboard.
 * Team names encode group weight so the client sorts the tab list:
 * lower team names sort first, so weight is inverted into the name.
 */
public final class TagService {

    private static final String TEAM_PREFIX = "rtg_";
    private static final int MAX_WEIGHT = 999;

    private final Storage storage;

    public TagService(Storage storage) {
        this.storage = storage;
    }

    private Scoreboard board() {
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    /** Highest-weight group whose permission the player holds. */
    public GroupData groupOf(Player player) {
        return storage.groups.values().stream()
                .filter(g -> player.hasPermission(g.permission))
                .max(Comparator.comparingInt(g -> g.weight))
                .orElse(null);
    }

    public void apply(Player player) {
        GroupData group = groupOf(player);
        PlayerData override = storage.players.get(player.getUniqueId());

        String prefix = override != null && !override.prefix.isEmpty()
                ? override.prefix : group != null ? group.prefix : "";
        String suffix = override != null && !override.suffix.isEmpty()
                ? override.suffix : group != null ? group.suffix : "";
        int weight = group != null ? group.weight : 0;

        if (prefix.isEmpty() && suffix.isEmpty()) {
            remove(player);
            return;
        }

        String teamName = teamName(weight, player);
        Scoreboard board = board();

        // Drop membership in any stale ReTag team first.
        Team current = board.getEntryTeam(player.getName());
        if (current != null && current.getName().startsWith(TEAM_PREFIX)
                && !current.getName().equals(teamName)) {
            current.removeEntry(player.getName());
            if (current.getEntries().isEmpty()) current.unregister();
        }

        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);
        team.prefix(pad(Tags.parse(prefix), false));
        team.suffix(pad(Tags.parse(suffix), true));
        if (!team.hasEntry(player.getName())) team.addEntry(player.getName());
    }

    /** Adds the space between tag and name, matching NametagEdit behavior. */
    private Component pad(Component c, boolean suffixSide) {
        if (c.equals(Component.empty())) return c;
        return suffixSide ? Component.text(" ").append(c) : c.append(Component.text(" "));
    }

    private String teamName(int weight, Player player) {
        return teamName(weight, player.getName(), player.getUniqueId());
    }

    /**
     * Same computation without needing a Player, so it can be asserted directly.
     * Team names are limited; 3-digit sort + 8 chars of the name keeps it unique enough,
     * plus 4 chars of UUID to avoid collisions between same-prefix names.
     */
    static String teamName(int weight, String playerName, java.util.UUID uuid) {
        int sort = MAX_WEIGHT - Math.min(Math.max(weight, 0), MAX_WEIGHT);
        String base = playerName.toLowerCase(Locale.ROOT);
        if (base.length() > 8) base = base.substring(0, 8);
        return TEAM_PREFIX + String.format("%03d", sort) + "_" + base + "_"
                + uuid.toString().substring(0, 4);
    }

    public void remove(Player player) {
        Team current = board().getEntryTeam(player.getName());
        if (current != null && current.getName().startsWith(TEAM_PREFIX)) {
            current.removeEntry(player.getName());
            if (current.getEntries().isEmpty()) current.unregister();
        }
    }

    public void applyAll() {
        for (Player p : Bukkit.getOnlinePlayers()) apply(p);
    }

    /** Removes all ReTag teams, e.g. before reload or on shutdown. */
    public void clearTeams() {
        for (Team team : board().getTeams()) {
            if (team.getName().startsWith(TEAM_PREFIX)) team.unregister();
        }
    }
}
