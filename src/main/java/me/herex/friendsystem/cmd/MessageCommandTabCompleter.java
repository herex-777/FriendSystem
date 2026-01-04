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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class MessageCommandTabCompleter
implements TabCompleter {
    private final FriendSystem plugin;

    public MessageCommandTabCompleter(FriendSystem plugin) {
        this.plugin = plugin;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player)sender;
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>();
            UUID uuid = player.getUniqueId();
            for (UUID friendUUID : this.plugin.getFriendManager().getFriends(uuid)) {
                Player friend = Bukkit.getPlayer((UUID)friendUUID);
                if (friend == null || !friend.getName().toLowerCase().startsWith(args[0].toLowerCase())) continue;
                completions.add(friend.getName());
            }
            return completions;
        }
        return Collections.emptyList();
    }
}

