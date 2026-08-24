package dev.retag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Handles /retag and the NametagEdit-compatible /nte alias.
 * NametagEdit syntax kept working: prefix/suffix/clear/reload/groups.
 */
public final class ReTagCommand implements TabExecutor {

    private final ReTagPlugin plugin;

    public ReTagCommand(ReTagPlugin plugin) {
        this.plugin = plugin;
    }

    private void msg(CommandSender to, String text) {
        to.sendMessage(Component.text("[ReTag] ", NamedTextColor.AQUA)
                .append(Component.text(text, NamedTextColor.WHITE)));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            msg(sender, "v" + plugin.getDescription().getVersion()
                    + " — /" + label + " <prefix|suffix|clear|group|import|reload>");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                plugin.reloadAll();
                msg(sender, "Configuration and tags reloaded.");
            }
            case "import" -> {
                String summary = NametagEditImporter.importInto(
                        plugin.storage(), plugin.getDataFolder().getParentFile(), plugin.getLogger());
                if (summary == null) {
                    msg(sender, "No plugins/NametagEdit folder found.");
                } else {
                    plugin.tags().applyAll();
                    msg(sender, summary + ".");
                }
            }
            case "prefix", "suffix" -> {
                if (args.length < 2) {
                    msg(sender, "Usage: /" + label + " " + sub + " <player> [value…] (empty clears)");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    msg(sender, "Player " + args[1] + " is not online.");
                    return true;
                }
                String value = args.length > 2
                        ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
                PlayerData data = plugin.storage().playerData(target.getUniqueId());
                if (sub.equals("prefix")) data.prefix = value; else data.suffix = value;
                plugin.storage().save();
                plugin.tags().apply(target);
                msg(sender, sub + " of " + target.getName()
                        + (value.isEmpty() ? " cleared." : " set to \"" + value + "\"."));
            }
            case "clear" -> {
                if (args.length < 2) {
                    msg(sender, "Usage: /" + label + " clear <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    msg(sender, "Player " + args[1] + " is not online.");
                    return true;
                }
                plugin.storage().players.remove(target.getUniqueId());
                plugin.storage().save();
                plugin.tags().apply(target);
                msg(sender, "Tags of " + target.getName() + " cleared.");
            }
            case "group", "groups" -> handleGroup(sender, label, args);
            default -> msg(sender, "Unknown subcommand. /" + label
                    + " <prefix|suffix|clear|group|import|reload>");
        }
        return true;
    }

    private void handleGroup(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || args[1].equalsIgnoreCase("list")) {
            if (plugin.storage().groups.isEmpty()) {
                msg(sender, "No groups defined. /" + label + " group add <name>");
                return;
            }
            String list = plugin.storage().groups.values().stream()
                    .map(g -> g.name + "(w" + g.weight + ")")
                    .collect(Collectors.joining(", "));
            msg(sender, "Groups: " + list);
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        if (args.length < 3) {
            msg(sender, "Usage: /" + label + " group <add|remove|prefix|suffix|weight> <name> [value…]");
            return;
        }
        String name = args[2];
        String key = name.toLowerCase(Locale.ROOT);
        switch (action) {
            case "add" -> {
                if (plugin.storage().groups.containsKey(key)) {
                    msg(sender, "Group " + name + " already exists.");
                    return;
                }
                plugin.storage().groups.put(key, new GroupData(name, "", "", 0, ""));
                plugin.storage().save();
                msg(sender, "Group " + name + " created. Permission: retag.group." + key);
            }
            case "remove" -> {
                if (plugin.storage().groups.remove(key) == null) {
                    msg(sender, "Group " + name + " does not exist.");
                    return;
                }
                plugin.storage().save();
                plugin.tags().applyAll();
                msg(sender, "Group " + name + " removed.");
            }
            case "prefix", "suffix", "weight" -> {
                GroupData group = plugin.storage().groups.get(key);
                if (group == null) {
                    msg(sender, "Group " + name + " does not exist.");
                    return;
                }
                String value = args.length > 3
                        ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : "";
                switch (action) {
                    case "prefix" -> group.prefix = value;
                    case "suffix" -> group.suffix = value;
                    case "weight" -> {
                        try {
                            group.weight = Integer.parseInt(value.trim());
                        } catch (NumberFormatException e) {
                            msg(sender, "Weight must be a number.");
                            return;
                        }
                    }
                }
                plugin.storage().save();
                plugin.tags().applyAll();
                msg(sender, "Group " + name + " " + action + " updated.");
            }
            default -> msg(sender, "Usage: /" + label
                    + " group <list|add|remove|prefix|suffix|weight> …");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return filter(List.of("prefix", "suffix", "clear", "group", "import", "reload"), args[0]);
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            if (sub.equals("group") || sub.equals("groups")) {
                return filter(List.of("list", "add", "remove", "prefix", "suffix", "weight"), args[1]);
            }
            if (List.of("prefix", "suffix", "clear").contains(sub)) {
                return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
            }
        }
        if (args.length == 3 && (sub.equals("group") || sub.equals("groups"))
                && !args[1].equalsIgnoreCase("add") && !args[1].equalsIgnoreCase("list")) {
            return filter(new ArrayList<>(plugin.storage().groups.keySet()), args[2]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String start) {
        String s = start.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(s)).toList();
    }
}
