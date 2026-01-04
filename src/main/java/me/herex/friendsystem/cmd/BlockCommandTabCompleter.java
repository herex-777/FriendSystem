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
import me.herex.friendsystem.model.PlayerData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class BlockCommandTabCompleter
implements TabCompleter {
    private static final List<String> SUBCOMMANDS =
            java.util.Arrays.asList("add", "remove", "removeall", "confirmremoveall", "list", "help");


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
            return completions;
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            ArrayList<String> completions = new ArrayList<String>();
            if (sub.equals("add") || sub.equals("remove")) {
                HashSet<String> allNames = new HashSet<String>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getUniqueId().equals(player.getUniqueId())) continue;
                    allNames.add(p.getName());
                }
                FriendSystem plugin = FriendSystem.getInstance();
                if (plugin != null) {
                    for (UUID uuid : plugin.getDataManager().getAllKnownUUIDs()) {
                        PlayerData pd;
                        if (uuid.equals(player.getUniqueId()) || (pd = plugin.getDataManager().getPlayerData(uuid)) == null) continue;
                        allNames.add(pd.getPlayerName());
                    }
                }
                for (String name : allNames) {
                    if (!name.toLowerCase().startsWith(args[1].toLowerCase())) continue;
                    completions.add(name);
                }
            } else if (sub.equals("list")) {
                for (int i = 1; i <= 10; ++i) {
                    String page = String.valueOf(i);
                    if (!page.startsWith(args[1])) continue;
                    completions.add(page);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}

