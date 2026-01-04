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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class FriendSystemCommandTabCompleter
implements TabCompleter {
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            if ("cleardata".startsWith(args[0].toLowerCase())) {
                completions.add("cleardata");
            }
            return completions;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("cleardata")) {
            ArrayList<String> completions = new ArrayList<String>();
            completions.add("all");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().toLowerCase().startsWith(args[1].toLowerCase())) continue;
                completions.add(p.getName());
            }
            return completions;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("cleardata") && args[1].equalsIgnoreCase("all") && "confirm".startsWith(args[2].toLowerCase())) {
            return Collections.singletonList("confirm");
        }
        return Collections.emptyList();
    }
}

