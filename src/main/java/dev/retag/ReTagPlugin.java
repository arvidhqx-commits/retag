package dev.retag;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ReTagPlugin extends JavaPlugin implements Listener {

    private Storage storage;
    private TagService tags;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create data folder.");
        }
        storage = new Storage(getDataFolder(), getLogger());
        storage.load();
        tags = new TagService(storage);

        Objects.requireNonNull(getCommand("retag")).setExecutor(new ReTagCommand(this));
        Objects.requireNonNull(getCommand("nte")).setExecutor(new ReTagCommand(this));
        getServer().getPluginManager().registerEvents(this, this);

        // One-time convenience: offer import if NametagEdit data exists and we have none.
        if (storage.groups.isEmpty() && storage.players.isEmpty()) {
            String summary = NametagEditImporter.importInto(
                    storage, getDataFolder().getParentFile(), getLogger());
            if (summary != null) getLogger().info("Auto-import: " + summary + ".");
        }

        tags.applyAll();
        getLogger().info("ReTag " + getDescription().getVersion() + " enabled — "
                + storage.groups.size() + " groups, "
                + storage.players.size() + " player overrides.");
    }

    @Override
    public void onDisable() {
        if (tags != null) tags.clearTeams();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        // Delay one tick so permission plugins finish setting up the player.
        getServer().getScheduler().runTask(this, () -> {
            if (event.getPlayer().isOnline()) tags.apply(event.getPlayer());
        });
    }

    public void reloadAll() {
        tags.clearTeams();
        storage.load();
        tags.applyAll();
    }

    public Storage storage() {
        return storage;
    }

    public TagService tags() {
        return tags;
    }
}
