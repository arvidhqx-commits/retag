package dev.retag;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Imports groups.yml and players.yml from an existing NametagEdit installation
 * (plugins/NametagEdit/). NametagEdit stores legacy '&' color codes, which
 * ReTag renders natively, so values are copied as-is.
 */
public final class NametagEditImporter {

    private NametagEditImporter() {}

    private static YamlConfiguration flatLoad(File file, Logger log) {
        YamlConfiguration yml = new YamlConfiguration();
        yml.options().pathSeparator('\u0001');
        try {
            yml.load(file);
        } catch (java.io.IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            log.warning("Import: could not read " + file.getName() + ": " + e.getMessage());
        }
        return yml;
    }

    /** @return summary line, or null if no NametagEdit data was found. */
    public static String importInto(Storage storage, File pluginsDir, Logger log) {
        File nteDir = new File(pluginsDir, "NametagEdit");
        if (!nteDir.isDirectory()) return null;

        int groups = 0;
        int players = 0;

        File groupsFile = new File(nteDir, "groups.yml");
        if (groupsFile.exists()) {
            // Same dot-in-name trap as in Storage: NametagEdit group names are
            // foreign data and may contain a dot. The separator must be set before
            // loading, because loading itself builds the tree with set().
            YamlConfiguration yml = flatLoad(groupsFile, log);
            ConfigurationSection root = yml.getConfigurationSection("Groups");
            if (root != null) {
                for (String name : root.getKeys(false)) {
                    ConfigurationSection g = root.getConfigurationSection(name);
                    if (g == null) continue;
                    GroupData data = new GroupData(
                            name,
                            g.getString("Prefix", ""),
                            g.getString("Suffix", ""),
                            g.getInt("SortPriority", 0) > 0
                                    // NTE: lower SortPriority = higher in tab. Invert to weight.
                                    ? Math.max(0, 100 - g.getInt("SortPriority", 0))
                                    : 0,
                            g.getString("Permission", ""));
                    storage.groups.put(name.toLowerCase(Locale.ROOT), data);
                    groups++;
                }
            }
        }

        File playersFile = new File(nteDir, "players.yml");
        if (playersFile.exists()) {
            YamlConfiguration yml = flatLoad(playersFile, log);
            ConfigurationSection root = yml.getConfigurationSection("Players");
            if (root != null) {
                for (String key : root.getKeys(false)) {
                    ConfigurationSection p = root.getConfigurationSection(key);
                    if (p == null) continue;
                    try {
                        UUID id = UUID.fromString(key);
                        PlayerData data = storage.playerData(id);
                        data.prefix = p.getString("Prefix", "");
                        data.suffix = p.getString("Suffix", "");
                        players++;
                    } catch (IllegalArgumentException e) {
                        log.warning("Import: skipping invalid UUID " + key);
                    }
                }
            }
        }

        storage.save();
        return groups + " groups and " + players + " player tags imported from NametagEdit";
    }
}
