/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 */
package me.herex.friendsystem.cmd;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.model.FriendRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class FriendCommandTabCompleter
implements TabCompleter {
    private final FriendSystem plugin;
    private static final List<String> SUBCOMMANDS = Arrays.asList("add", "remove", "list", "accept", "deny", "settings", "requests", "notifications", "nickname", "best", "removeall", "help");

    public FriendCommandTabCompleter(FriendSystem plugin) {
        this.plugin = plugin;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player)sender;
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>();
            for (String sub : SUBCOMMANDS) {
                if (!sub.startsWith(args[0].toLowerCase())) continue;
                completions.add(sub);
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().toLowerCase().startsWith(args[0].toLowerCase())) continue;
                completions.add(p.getName());
            }
            return completions;
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            ArrayList<String> completions = new ArrayList<String>();
            if (sub.equals("add") || sub.equals("remove") || sub.equals("nickname") || sub.equals("best")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getName().toLowerCase().startsWith(args[1].toLowerCase())) continue;
                    completions.add(p.getName());
                }
            } else if (sub.equals("accept") || sub.equals("deny")) {
                Map<UUID, FriendRequest> incoming = this.plugin.getRequestManager().getIncomingRequests(player.getUniqueId());
                for (Map.Entry<UUID, FriendRequest> entry : incoming.entrySet()) {
                    String name = this.plugin.getDataManager().getPlayerData(entry.getKey()).getPlayerName();
                    if (name == null || !name.toLowerCase().startsWith(args[1].toLowerCase())) continue;
                    completions.add(name);
                }
            } else if (sub.equals("list")) {
                if ("best".startsWith(args[1].toLowerCase())) {
                    completions.add("best");
                }
                for (int i = 1; i <= 5; ++i) {
                    if (!("" + i).startsWith(args[1])) continue;
                    completions.add("" + i);
                }
            }
            return completions;
        }
        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            ArrayList<String> completions = new ArrayList<String>();
            if (sub.equals("nickname")) {
                return Collections.emptyList();
            }
            if (sub.equals("list") && args[1].equalsIgnoreCase("best")) {
                for (int i = 1; i <= 5; ++i) {
                    if (!("" + i).startsWith(args[2])) continue;
                    completions.add("" + i);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}

