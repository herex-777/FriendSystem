/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Player
 */
package me.herex.friendsystem.manager;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.VersionHandler;
import me.herex.friendsystem.cmd.FriendCommand;
import me.herex.friendsystem.model.PlayerData;
import me.herex.friendsystem.model.PlayerSettings;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageManager {
    private final FriendSystem plugin;
    private final Map<UUID, UUID> lastMessaged;
    private final Map<UUID, Long> lastMessageTime;

    public MessageManager(FriendSystem plugin) {
        this.plugin = plugin;
        this.lastMessaged = new HashMap<UUID, UUID>();
        this.lastMessageTime = new HashMap<UUID, Long>();
    }

    public boolean sendMessage(UUID fromUUID, UUID toUUID, String message) {
        if (this.plugin.getBlockManager().isBlocked(toUUID, fromUUID)) {
            this.sendBorderedError(VersionHandler.getOnlinePlayer(fromUUID), "&cYou cannot message this player as they have blocked you.");
            return false;
        }
        if (this.plugin.getBlockManager().isBlocked(fromUUID, toUUID)) {
            this.sendBorderedError(VersionHandler.getOnlinePlayer(fromUUID), "&cYou cannot message this player as you have blocked them.");
            return false;
        }
        PlayerSettings settings = this.plugin.getDataManager().getPlayerData(toUUID).getSettings();
        Player sender = VersionHandler.getOnlinePlayer(fromUUID);
        boolean canMessage = false;
        switch (settings.getMessagePrivacy()) {
            case NONE: {
                canMessage = true;
                break;
            }
            case LOW: {
                if ((sender == null || !sender.hasPermission("friendsystem.staff")) && !FriendSystem.isGuiAllowed(sender)) break;
                canMessage = true;
                break;
            }
            case MEDIUM: {
                if ((sender == null || !sender.hasPermission("friendsystem.staff")) && !this.plugin.getFriendManager().areFriends(fromUUID, toUUID)) break;
                canMessage = true;
                break;
            }
            case HIGH: {
                if ((sender == null || !sender.hasPermission("friendsystem.staff")) && !this.plugin.getFriendManager().isBestFriend(toUUID, fromUUID)) break;
                canMessage = true;
                break;
            }
            case MAX: {
                if (sender == null || !sender.hasPermission("friendsystem.staff")) break;
                canMessage = true;
            }
        }
        if (!canMessage) {
            this.sendBorderedError(VersionHandler.getOnlinePlayer(fromUUID), "&cYou cannot message this player");
            return false;
        }
        Player target = VersionHandler.getOnlinePlayer(toUUID);
        if (target == null || !target.isOnline()) {
            this.sendBorderedError(VersionHandler.getOnlinePlayer(fromUUID), "&cPlayer is not online.");
            return false;
        }
        PlayerData fromData = this.plugin.getDataManager().getPlayerData(fromUUID);
        PlayerData toData = this.plugin.getDataManager().getPlayerData(toUUID);
        String fromDisplay = FriendCommand.getDisplayName(fromUUID, toData, false, true);
        String toDisplay = FriendCommand.getDisplayName(toUUID, fromData, false, true);
        String toMsg = "&dTo " + toDisplay + "&7: &7" + message;
        String fromMsg = "&dFrom " + fromDisplay + "&7: &7" + message;
        target.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)fromMsg));
        Player senderPlayer = VersionHandler.getOnlinePlayer(fromUUID);
        if (senderPlayer != null) {
            senderPlayer.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)toMsg));
        }
        this.lastMessaged.put(fromUUID, toUUID);
        this.lastMessaged.put(toUUID, fromUUID);
        this.lastMessageTime.put(fromUUID, System.currentTimeMillis());
        this.lastMessageTime.put(toUUID, System.currentTimeMillis());
        return true;
    }

    public boolean sendMessage(Player from, String targetName, String message) {
        Player target = VersionHandler.getOnlinePlayer(targetName);
        if (target == null) {
            return false;
        }
        return this.sendMessage(from.getUniqueId(), target.getUniqueId(), message);
    }

    public boolean replyToLastMessage(UUID playerUUID, String message) {
        UUID lastMessagedUUID = this.lastMessaged.get(playerUUID);
        if (lastMessagedUUID == null) {
            return false;
        }
        return this.sendMessage(playerUUID, lastMessagedUUID, message);
    }

    public UUID getLastMessaged(UUID playerUUID) {
        return this.lastMessaged.get(playerUUID);
    }

    public Long getLastMessageTime(UUID playerUUID) {
        return this.lastMessageTime.get(playerUUID);
    }

    public boolean hasRecentMessage(UUID playerUUID, long timeThreshold) {
        Long lastTime = this.lastMessageTime.get(playerUUID);
        if (lastTime == null) {
            return false;
        }
        return System.currentTimeMillis() - lastTime < timeThreshold;
    }

    private String formatMessage(UUID fromUUID, String message) {
        String fromName = this.getPlayerName(fromUUID);
        return "&b" + fromName + " &7\u00bb &f" + message;
    }

    private String getPlayerName(UUID playerUUID) {
        Player player = VersionHandler.getOnlinePlayer(playerUUID);
        if (player != null) {
            return VersionHandler.getPlayerName(player);
        }
        return playerUUID.toString();
    }

    public void clearMessageHistory(UUID playerUUID) {
        this.lastMessaged.remove(playerUUID);
        this.lastMessageTime.remove(playerUUID);
    }

    public boolean canMessage(UUID fromUUID, UUID toUUID) {
        if (this.plugin.getBlockManager().isBlocked(toUUID, fromUUID) || this.plugin.getBlockManager().isBlocked(fromUUID, toUUID)) {
            return false;
        }
        PlayerSettings settings = this.plugin.getDataManager().getPlayerData(toUUID).getSettings();
        Player sender = VersionHandler.getOnlinePlayer(fromUUID);
        switch (settings.getMessagePrivacy()) {
            case NONE: {
                return true;
            }
            case LOW: 
            case MEDIUM: 
            case HIGH: {
                return sender != null && sender.hasPermission("friendsystem.staff") || this.plugin.getFriendManager().areFriends(fromUUID, toUUID);
            }
            case MAX: {
                return sender != null && sender.hasPermission("friendsystem.staff") || this.plugin.getFriendManager().isBestFriend(toUUID, fromUUID);
            }
        }
        Player target = VersionHandler.getOnlinePlayer(toUUID);
        return target != null && target.isOnline();
    }

    public Set<Player> getMessageableFriends(UUID playerUUID) {
        HashSet<Player> messageableFriends = new HashSet<Player>();
        Set<Player> onlineFriends = this.plugin.getFriendManager().getOnlineFriends(playerUUID);
        for (Player friend : onlineFriends) {
            if (!this.canMessage(playerUUID, friend.getUniqueId())) continue;
            messageableFriends.add(friend);
        }
        return messageableFriends;
    }

    public void sendSystemMessage(UUID playerUUID, String message) {
        Player player = VersionHandler.getOnlinePlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
        }
    }

    public void sendSystemMessage(Collection<UUID> playerUUIDs, String message) {
        for (UUID playerUUID : playerUUIDs) {
            this.sendSystemMessage(playerUUID, message);
        }
    }

    private String getPlayerWithPrefix(UUID uuid) {
        Player player = VersionHandler.getOnlinePlayer(uuid);
        if (player != null) {
            return player.getDisplayName();
        }
        return uuid.toString().substring(0, 8) + "...";
    }

    private void sendBorderedError(Player player, String line) {
        if (player == null) {
            return;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)line));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
    }
}

