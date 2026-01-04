/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.ClickEvent
 *  net.md_5.bungee.api.chat.ClickEvent$Action
 *  net.md_5.bungee.api.chat.ComponentBuilder
 *  net.md_5.bungee.api.chat.HoverEvent
 *  net.md_5.bungee.api.chat.HoverEvent$Action
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package me.herex.friendsystem.cmd;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.VersionHandler;
import me.herex.friendsystem.manager.BlockManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockCommand
implements CommandExecutor {
    private final FriendSystem plugin;
    private final BlockManager blockManager;
    private final int BLOCKS_PER_PAGE = 8;

    public BlockCommand(FriendSystem plugin) {
        this.plugin = plugin;
        this.blockManager = plugin.getBlockManager();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            this.showHelp(player);
            return true;
        }
        String sub = args[0].toLowerCase();
        if (!(sub.equals("add") || sub.equals("remove") || sub.equals("removeall") || sub.equals("confirmremoveall") || sub.equals("list") || sub.equals("help"))) {
            this.handleAdd(player, args);
            return true;
        }
        switch (sub) {
            case "add": {
                this.handleAdd(player, args);
                break;
            }
            case "remove": {
                this.handleRemove(player, args);
                break;
            }
            case "removeall": {
                this.handleRemoveAll(player);
                break;
            }
            case "confirmremoveall": {
                this.handleConfirmRemoveAll(player);
                break;
            }
            case "list": {
                this.handleList(player, args);
                break;
            }
            default: {
                this.showHelp(player);
            }
        }
        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&aBlock commands:"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/block add <player> &7- &bBlock a player."));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/block help &7- &bPrints this help message."));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/block list <page> &7- &bList blocked players."));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/block remove <player> &7- &bUnlock a player."));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/block removeall &7- &bUnlock all players."));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 2) {
            this.sendBordered(player, "&cUsage: /block add <player>");
            return;
        }
        String targetName = args[1];
        Player target = VersionHandler.getOnlinePlayer(targetName);
        UUID targetUUID = null;
        if (target != null) {
            targetUUID = target.getUniqueId();
        } else {
            try {
                targetUUID = Bukkit.getOfflinePlayer((String)targetName).getUniqueId();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (targetUUID == null) {
            this.sendBordered(player, "&cPlayer &e" + targetName + " &cnot found!");
            return;
        }
        if (player.getUniqueId().equals(targetUUID)) {
            this.sendBordered(player, "&cYou cannot block yourself!");
            return;
        }
        if (this.blockManager.blockPlayer(player.getUniqueId(), targetUUID)) {
            if (this.plugin.getFriendManager().areFriends(player.getUniqueId(), targetUUID)) {
                this.plugin.getFriendManager().removeFriend(player.getUniqueId(), targetUUID);
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&aBlocked " + targetName + ".")));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&cYou have already blocked " + targetName + ".")));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
        }
    }

    private void handleRemove(Player player, String[] args) {
        UUID targetUUID;
        if (args.length < 2) {
            this.sendBordered(player, "&cUsage: /block remove <player>");
            return;
        }
        Player target = VersionHandler.getOnlinePlayer(args[1]);
        UUID uUID = targetUUID = target != null ? target.getUniqueId() : null;
        if (targetUUID == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&cYou don't have that player blocked."));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
            return;
        }
        if (this.blockManager.unblockPlayer(player.getUniqueId(), targetUUID)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&aUnblocked " + target.getName() + ".")));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&cYou don't have that player blocked."));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
        }
    }

    private void handleRemoveAll(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&eYou're about to remove &c&lALL &eblocked players. Are you sure you want to do this?"));
        TextComponent cancel = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)"&c[CANCEL]"));
        cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/block list"));
        TextComponent clear = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)"&eor &c[CLEAR BLOCKED PLAYERS]"));
        clear.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/block confirmremoveall"));
        clear.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unblock all players").create()));
        cancel.addExtra(" ");
        cancel.addExtra((BaseComponent)clear);
        player.spigot().sendMessage((BaseComponent)cancel);
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
    }

    public void handleConfirmRemoveAll(Player player) {
        this.blockManager.unblockAll(player.getUniqueId());
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&eRemoved all blocked players."));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
    }

    private void handleList(Player player, String[] args) {
        ArrayList<UUID> blocked;
        int total;
        int maxPage;
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) {
                    page = 1;
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (page > (maxPage = Math.max(1, (int)Math.ceil((double)(total = (blocked = new ArrayList<UUID>(this.blockManager.getBlockedPlayers(player.getUniqueId()))).size()) / 8.0)))) {
            page = maxPage;
        }
        int start = (page - 1) * 8;
        int end = Math.min(start + 8, total);
        if (start < 0) {
            start = 0;
        }
        if (start >= total) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&eBlocked Players (Page " + page + " of " + maxPage + ")")));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&cYou have not blocked any players."));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
            return;
        }
        List pageBlocked = blocked.subList(start, end);
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&eBlocked Players (Page " + page + " of " + maxPage + ")")));
        if (pageBlocked.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&cYou have not blocked any players."));
        } else {
            int idx = start + 1;
            for (Object uuid : pageBlocked) {
                String name = Bukkit.getOfflinePlayer((UUID)uuid).getName();
                player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&b" + idx + ". &e" + (name != null ? name : uuid.toString().substring(0, 8) + "..."))));
                ++idx;
            }
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
    }

    private void sendBordered(Player player, String ... lines) {
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
        for (String line : lines) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)line));
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
    }
}

