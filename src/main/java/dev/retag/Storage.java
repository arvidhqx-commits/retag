package dev.retag;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/** YAML persistence for groups and player overrides. */
public final class Storage {

    /**
     * Group names come from foreign data (NametagEdit) and may contain a dot.
     * Bukkit reads '.' as a path separator, which silently turned a group
     * "vip.plus" into a nested section and back into a group "vip". A separator
     * that cannot occur in a name switches that off.
     */
    private static final char SEP = '\u0001';

    private static YamlConfiguration flat() {
        YamlConfiguration yml = new YamlConfiguration();
        yml.options().pathSeparator(SEP);
        return yml;
    }

    /**
     * The separator must be set BEFORE loading: loadConfiguration() already builds
     * the section tree with set(), so a dotted key would have been split by then.
     */
    private static YamlConfiguration flatLoad(File file, Logger log) {
        YamlConfiguration yml = flat();
        try {
            yml.load(file);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            log.warning("Could not read " + file.getName() + ": " + e.getMessage());
        }
        return yml;
    }

    private final File groupsFile;
    private final File playersFile;
    private final Logger log;

    public final Map<String, GroupData> groups = new LinkedHashMap<>();
    public final Map<UUID, PlayerData> players = new LinkedHashMap<>();

    public Storage(File dataFolder, Logger log) {
        this.groupsFile = new File(dataFolder, "groups.yml");
        this.playersFile = new File(dataFolder, "players.yml");
        this.log = log;
    }

    public void load() {
        groups.clear();
        players.clear();

        if (groupsFile.exists()) {
            YamlConfiguration yml = flatLoad(groupsFile, log);
            ConfigurationSection root = yml.getConfigurationSection("groups");
            if (root != null) {
                for (String name : root.getKeys(false)) {
                    ConfigurationSection g = root.getConfigurationSection(name);
                    if (g == null) continue;
                    groups.put(name.toLowerCase(Locale.ROOT), new GroupData(
                            name,
                            g.getString("prefix", ""),
                            g.getString("suffix", ""),
                            g.getInt("weight", 0),
                            g.getString("permission", "")));
                }
            }
        }

        if (playersFile.exists()) {
            YamlConfiguration yml = flatLoad(playersFile, log);
            ConfigurationSection root = yml.getConfigurationSection("players");
            if (root != null) {
                for (String key : root.getKeys(false)) {
                    ConfigurationSection p = root.getConfigurationSection(key);
                    if (p == null) continue;
                    try {
                        UUID id = UUID.fromString(key);
                        PlayerData data = new PlayerData();
                        data.prefix = p.getString("prefix", "");
                        data.suffix = p.getString("suffix", "");
                        if (!data.isEmpty()) players.put(id, data);
                    } catch (IllegalArgumentException e) {
                        log.warning("Skipping invalid UUID in players.yml: " + key);
                    }
                }
            }
        }
    }

    public void save() {
        YamlConfiguration g = flat();
        for (GroupData group : groups.values()) {
            String base = "groups" + SEP + group.name + SEP;
            g.set(base + "prefix", group.prefix);
            g.set(base + "suffix", group.suffix);
            g.set(base + "weight", group.weight);
            g.set(base + "permission", group.permission);
        }
        YamlConfiguration p = flat();
        for (Map.Entry<UUID, PlayerData> e : players.entrySet()) {
            String base = "players" + SEP + e.getKey() + SEP;
            p.set(base + "prefix", e.getValue().prefix);
            p.set(base + "suffix", e.getValue().suffix);
        }
        try {
            g.save(groupsFile);
            p.save(playersFile);
        } catch (IOException e) {
            log.severe("Could not save ReTag data: " + e.getMessage());
        }
    }

    public PlayerData playerData(UUID id) {
        return players.computeIfAbsent(id, k -> new PlayerData());
    }
}
