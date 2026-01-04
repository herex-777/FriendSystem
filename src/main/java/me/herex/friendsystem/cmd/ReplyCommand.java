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
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyCommand
implements CommandExecutor {
    private final FriendSystem plugin;

    public ReplyCommand(FriendSystem plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (args.length < 1) {
            this.sendMessage(player, "&cUsage: /r <message>");
            return true;
        }
        String message = String.join((CharSequence)" ", args);
        if (message.trim().isEmpty()) {
            this.sendMessage(player, "&cMessage cannot be empty!");
            return true;
        }
        UUID lastMessagedUUID = this.plugin.getMessageManager().getLastMessaged(player.getUniqueId());
        if (lastMessagedUUID == null) {
            this.sendMessage(player, "&cYou have no recent messages to reply to!");
            return true;
        }
        Player target = VersionHandler.getOnlinePlayer(lastMessagedUUID);
        if (target == null) {
            this.sendMessage(player, "&cThe player you're replying to is no longer online!");
            return true;
        }
        if (!this.plugin.getFriendManager().areFriends(player.getUniqueId(), target.getUniqueId())) {
            this.sendMessage(player, "&cYou can only reply to friends!");
            return true;
        }
        if (this.plugin.getDataManager().getPlayerData(target.getUniqueId()).getSettings().isAppearOffline()) {
            this.sendMessage(player, "&c&e" + target.getName() + " &cappears to be offline!");
            return true;
        }
        if (this.plugin.getMessageManager().replyToLastMessage(player.getUniqueId(), message)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&f" + message)));
        } else {
            this.sendMessage(player, "&cFailed to send reply to &e" + target.getName() + "&c!");
        }
        return true;
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
    }
}

