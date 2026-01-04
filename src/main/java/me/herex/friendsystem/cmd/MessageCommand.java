/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package me.herex.friendsystem.cmd;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.VersionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageCommand
implements CommandExecutor {
    private final FriendSystem plugin;

    public MessageCommand(FriendSystem plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (args.length < 2) {
            this.sendMessage(player, "&cUsage: /msg <player> <message>");
            return true;
        }
        String targetName = args[0];
        String message = String.join((CharSequence)" ", args).substring(targetName.length() + 1);
        if (message.trim().isEmpty()) {
            this.sendMessage(player, "&cMessage cannot be empty!");
            return true;
        }
        Player target = VersionHandler.getOnlinePlayer(targetName);
        if (target == null) {
            this.sendMessage(player, "&cPlayer &e" + targetName + " &cis not online!");
            return true;
        }
        if (!this.plugin.getFriendManager().areFriends(player.getUniqueId(), target.getUniqueId())) {
            this.sendMessage(player, "&cYou can only message friends! Use &e/friend add " + targetName + " &cto add them as a friend.");
            return true;
        }
        if (this.plugin.getDataManager().getPlayerData(target.getUniqueId()).getSettings().isAppearOffline()) {
            this.sendMessage(player, "&c&e" + targetName + " &cappears to be offline!");
            return true;
        }
        if (!this.plugin.getMessageManager().sendMessage(player, targetName, message)) {
            this.sendMessage(player, "&cFailed to send message to &e" + targetName + "&c!");
        }
        return true;
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
    }
}

