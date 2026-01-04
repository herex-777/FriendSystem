/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package me.herex.friendsystem.cmd;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.model.PlayerData;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FriendSystemCommand
implements CommandExecutor {
    private final FriendSystem plugin;

    public FriendSystemCommand(FriendSystem plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subCommand;
        if (args.length == 0) {
            this.sendHelp(sender);
            return true;
        }
        switch (subCommand = args[0].toLowerCase()) {
            case "reload": {
                this.handleReload(sender);
                break;
            }
            case "cleardata": {
                this.handleClearData(sender, args);
                break;
            }
            default: {
                this.sendHelp(sender);
            }
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("friendsystem.admin.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        sender.sendMessage(ChatColor.YELLOW + "Saving current data...");
        this.plugin.getDataManager().saveAllData();
        sender.sendMessage(ChatColor.YELLOW + "Reloading configuration...");
        this.plugin.reloadConfig();
        sender.sendMessage(ChatColor.YELLOW + "Reloading player data...");
        this.plugin.getDataManager().loadAllData();
        sender.sendMessage(ChatColor.YELLOW + "Reloading friend requests...");
        this.plugin.getRequestManager().loadAllPendingRequests();
        sender.sendMessage(ChatColor.GREEN + "FriendSystem has been reloaded successfully.");
    }

    private void handleClearData(CommandSender sender, String[] args) {
        if (!sender.hasPermission("friendsystem.admin.cleardata")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /friendsystem cleardata <player|all> [confirm]");
            return;
        }
        String target = args[1].toLowerCase();
        if (target.equals("all")) {
            if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                sender.sendMessage(ChatColor.YELLOW + "This is a destructive operation that will wipe all player data.");
                sender.sendMessage(ChatColor.YELLOW + "To confirm, please use: /friendsystem cleardata all confirm");
                return;
            }
            this.plugin.getDataManager().clearAllPlayerData();
            this.plugin.getDataManager().saveAllData();
            sender.sendMessage(ChatColor.GREEN + "All FriendSystem player data has been cleared.");
        } else {
            UUID targetUUID = this.findPlayerUUID(target);
            if (targetUUID == null) {
                sender.sendMessage(ChatColor.RED + "Could not find player data for '" + args[1] + "'.");
                return;
            }
            this.plugin.getDataManager().removePlayerData(targetUUID);
            this.plugin.getDataManager().saveAllData();
            sender.sendMessage(ChatColor.GREEN + "Data for '" + args[1] + "' has been cleared.");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
        sender.sendMessage(ChatColor.GOLD + "FriendSystem Admin Commands:");
        sender.sendMessage(ChatColor.YELLOW + "/friendsystem reload" + ChatColor.GRAY + " - Reloads the plugin config and data.");
        sender.sendMessage(ChatColor.YELLOW + "/friendsystem cleardata <player>" + ChatColor.GRAY + " - Clears a specific player's data.");
        sender.sendMessage(ChatColor.YELLOW + "/friendsystem cleardata all confirm" + ChatColor.GRAY + " - Clears all player data.");
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
    }

    private UUID findPlayerUUID(String playerName) {
        Player onlinePlayer = Bukkit.getPlayerExact((String)playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }
        for (Map.Entry<UUID, PlayerData> entry : this.plugin.getDataManager().getAllPlayerData().entrySet()) {
            if (!entry.getValue().getPlayerName().equalsIgnoreCase(playerName)) continue;
            return entry.getKey();
        }
        return Bukkit.getOfflinePlayer((String)playerName).getUniqueId();
    }
}

