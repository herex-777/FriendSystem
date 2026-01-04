/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.luckperms.api.LuckPerms
 *  net.luckperms.api.LuckPermsProvider
 *  net.luckperms.api.model.user.User
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
import me.herex.friendsystem.gui.FriendSettingsGUI;
import me.herex.friendsystem.manager.RequestManager;
import me.herex.friendsystem.model.FriendRequest;
import me.herex.friendsystem.model.PlayerData;
import me.herex.friendsystem.model.PlayerSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
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

public class FriendCommand
implements CommandExecutor {
    private final FriendSystem plugin;

    public FriendCommand(FriendSystem plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (label.equalsIgnoreCase("fl")) {
            this.handleList(player, new String[0]);
            return true;
        }
        if (args.length == 0) {
            this.showHelp(player);
            return true;
        }
        String subCommand = args[0].toLowerCase();
        if ((label.equalsIgnoreCase("f") || label.equalsIgnoreCase("friend")) && args.length == 1 && !this.isKnownSubCommand(subCommand)) {
            this.handleAdd(player, args);
            return true;
        }
        switch (subCommand) {
            case "add": {
                if (args.length < 2) {
                    this.sendBorderedMessage(player, "&cUsage: /friend add <player>");
                    break;
                }
                this.handleAdd(player, Arrays.copyOfRange(args, 1, args.length));
                break;
            }
            case "remove": {
                this.handleRemove(player, args);
                break;
            }
            case "list": {
                this.handleList(player, args);
                break;
            }
            case "accept": {
                this.handleAccept(player, args);
                break;
            }
            case "deny": {
                this.handleDeny(player, args);
                break;
            }
            case "settings": {
                new FriendSettingsGUI(this.plugin, player).open();
                break;
            }
            case "requests": {
                this.handleRequests(player, args);
                break;
            }
            case "notifications": {
                this.handleNotifications(player, args);
                break;
            }
            case "nickname": {
                this.handleNickname(player, args);
                break;
            }
            case "best": {
                this.handleBest(player, args);
                break;
            }
            case "removeall": {
                this.handleRemoveAll(player, args);
                break;
            }
            case "help": {
                this.showHelp(player);
                break;
            }
            default: {
                this.handleAdd(player, args);
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> completions;
        block11: {
            block12: {
                String sub;
                block14: {
                    PlayerData playerData;
                    block13: {
                        Player player;
                        block10: {
                            completions = new ArrayList<String>();
                            if (!(sender instanceof Player)) {
                                return completions;
                            }
                            player = (Player)sender;
                            playerData = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
                            if (args.length != 1) break block10;
                            List<String> subs = Arrays.asList("add", "remove", "accept", "deny", "list", "nickname", "best", "block", "unblock", "notifications", "help");
                            for (String sub2 : subs) {
                                if (!sub2.startsWith(args[0].toLowerCase())) continue;
                                completions.add(sub2);
                            }
                            break block11;
                        }
                        if (args.length != 2) break block12;
                        sub = args[0].toLowerCase();
                        if (!sub.equals("add") && !sub.equals("remove") && !sub.equals("nickname") && !sub.equals("best")) break block13;
                        HashSet<String> allNames = new HashSet<String>();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getUniqueId().equals(player.getUniqueId())) continue;
                            allNames.add(p.getName());
                        }
                        for (UUID uuid : this.plugin.getDataManager().getAllKnownUUIDs()) {
                            PlayerData pd;
                            if (uuid.equals(player.getUniqueId()) || (pd = this.plugin.getDataManager().getPlayerData(uuid)) == null) continue;
                            allNames.add(pd.getPlayerName());
                        }
                        for (String name : allNames) {
                            if (!name.toLowerCase().startsWith(args[1].toLowerCase())) continue;
                            completions.add(name);
                        }
                        break block11;
                    }
                    if (!sub.equals("accept") && !sub.equals("deny")) break block14;
                    for (UUID req : playerData.getIncomingRequests().keySet()) {
                        String name;
                        PlayerData pd = this.plugin.getDataManager().getPlayerData(req);
                        if (pd == null || !(name = pd.getPlayerName()).toLowerCase().startsWith(args[1].toLowerCase())) continue;
                        completions.add(name);
                    }
                    break block11;
                }
                if (!sub.equals("list")) break block11;
                if ("best".startsWith(args[1].toLowerCase())) {
                    completions.add("best");
                }
                for (int i = 1; i <= 10; ++i) {
                    String page = String.valueOf(i);
                    if (!page.startsWith(args[1])) continue;
                    completions.add(page);
                }
                break block11;
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("list")) {
                for (int i = 1; i <= 10; ++i) {
                    String page = String.valueOf(i);
                    if (!page.startsWith(args[2])) continue;
                    completions.add(page);
                }
            }
        }
        return completions;
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 1) {
            this.sendBorderedMessage(player, "&cUsage: /friend add <player>");
            return;
        }
        String targetName = args[0];
        UUID targetUUID = this.findPlayerUUID(targetName);
        if (targetUUID == null) {
            this.sendBorderedMessage(player, "&cNo player found with name " + targetName + "&c!");
            return;
        }
        RequestManager.RequestResult result = this.plugin.getRequestManager().sendRequest(player.getUniqueId(), targetUUID);
        switch (result) {
            case SUCCESS: {
                PlayerData targetData = this.plugin.getDataManager().getPlayerData(targetUUID);
                String display = FriendCommand.getDisplayName(targetUUID, targetData, false, true);
                player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
                player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&eYou sent a friend request to " + display + "&e! They have 5 minutes to accept it!")));
                player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
                Player targetPlayer = VersionHandler.getOnlinePlayer(targetUUID);
                if (targetPlayer == null) break;
                this.sendClickableRequestMessage(targetPlayer, player.getUniqueId());
                break;
            }
            case ALREADY_FRIENDS: {
                this.sendBorderedMessage(player, "&cYou are already friends with this person!");
                break;
            }
            case REQUEST_ALREADY_SENT: {
                this.sendBorderedMessage(player, "&cYou have already sent a friend request to this player.");
                break;
            }
            case SELF_REQUEST: {
                this.sendBorderedMessage(player, "&cYou cannot add yourself as a friend!");
                break;
            }
            case PRIVACY_LEVEL_DENIED: {
                this.sendBorderedMessage(player, "&cThis person has friend requests disabled!");
                break;
            }
            case TARGET_BLOCKED_YOU: {
                this.sendBorderedMessage(player, "&cYou cannot send a friend request to a player who has blocked you.");
                break;
            }
            case YOU_BLOCKED_TARGET: {
                this.sendBorderedMessage(player, "&cYou cannot send a friend request to a player you have blocked. Use /unblock <player> first.");
                break;
            }
            case MAX_REQUESTS_SENT: {
                this.sendBorderedMessage(player, "&cYou have sent too many friend requests. Please wait until some are accepted or expire.");
                break;
            }
            default: {
                this.sendBorderedMessage(player, "&cFailed to send friend request to " + targetName + "&c! (Unknown reason)");
            }
        }
    }

    private UUID findPlayerUUID(String playerName) {
        Player onlinePlayer = VersionHandler.getOnlinePlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }
        for (Map.Entry<UUID, PlayerData> entry : this.plugin.getDataManager().getAllPlayerData().entrySet()) {
            PlayerData playerData = entry.getValue();
            if (!playerData.getPlayerName().equalsIgnoreCase(playerName)) continue;
            return entry.getKey();
        }
        return null;
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            this.sendBorderedMessage(player, "&cUsage: /friend remove <player>");
            return;
        }
        String targetName = args[1];
        UUID targetUUID = this.findPlayerUUID(targetName);
        if (targetUUID == null) {
            this.sendBorderedMessage(player, "&cNo player found with name " + targetName + "&c!");
            return;
        }
        if (targetUUID.equals(player.getUniqueId())) {
            this.sendBorderedMessage(player, "&cYou cannot remove yourself!");
            return;
        }
        PlayerData playerData = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
        PlayerData targetData = this.plugin.getDataManager().getPlayerData(targetUUID);
        String senderDisplay = FriendCommand.getDisplayName(player.getUniqueId(), playerData, false, true);
        String targetDisplay = FriendCommand.getDisplayName(targetUUID, targetData, false, true);
        if (this.plugin.getFriendManager().removeFriend(player.getUniqueId(), targetUUID)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&eYou removed " + targetDisplay + " &efrom your friends list!")));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
            Player targetPlayer = VersionHandler.getOnlinePlayer(targetUUID);
            if (targetPlayer != null) {
                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&c" + senderDisplay + " removed you from their friends list!")));
                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
            }
        } else {
            this.sendBorderedMessage(player, targetDisplay + " &cisn't on your friends list!");
        }
    }

    private void handleList(Player player, String[] args) {
        int friendsPerPage = 8;
        int page = 1;
        boolean bestOnly = false;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("best")) {
                bestOnly = true;
                if (args.length >= 3) {
                    try {
                        page = Integer.parseInt(args[2]);
                    }
                    catch (NumberFormatException numberFormatException) {}
                }
            } else {
                try {
                    page = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
        }
        if (page < 1) {
            page = 1;
        }
        PlayerData playerData = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
        ArrayList<UUID> allFriends = bestOnly ? new ArrayList<UUID>(playerData.getBestFriends()) : new ArrayList<UUID>(this.plugin.getFriendManager().getFriends(player.getUniqueId()));
        int totalFriends = allFriends.size();
        int maxPage = Math.max(1, (int)Math.ceil((double)totalFriends / (double)friendsPerPage));
        if (page > maxPage) {
            page = maxPage;
        }
        int start = (page - 1) * friendsPerPage;
        int end = Math.min(start + friendsPerPage, totalFriends);
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
        if (maxPage > 1) {
            TextComponent header = new TextComponent("");
            if (page > 1) {
                TextComponent left = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e<< "));
                left.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend list " + (page - 1)));
                left.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Previous page").create()));
                header.addExtra((BaseComponent)left);
            }
            header.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)("&6Friends (Page " + page + " of " + maxPage + ") "))));
            if (page < maxPage) {
                TextComponent right = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e&l>>"));
                right.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend list " + (page + 1)));
                right.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Next page").create()));
                header.addExtra((BaseComponent)right);
            }
            player.spigot().sendMessage((BaseComponent)header);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&6Friends (Page " + page + " of " + maxPage + ")")));
        }
        if (start >= totalFriends) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&cNo friends to display."));
        } else {
            for (int i = start; i < end; ++i) {
                String statusMsg;
                UUID friendUUID = (UUID)allFriends.get(i);
                PlayerData friendData = this.plugin.getDataManager().getPlayerData(friendUUID);
                String colorPart = friendData.getLastKnownColor();
                if (VersionHandler.getOnlinePlayer(friendUUID) != null) {
                    colorPart = FriendCommand.getRankColor(friendUUID, friendData);
                }
                boolean isBest = playerData.isBestFriend(friendUUID);
                String nickname = playerData.getNickname(friendUUID);
                String namePart = nickname != null && !nickname.isEmpty() ? nickname + "*" : friendData.getPlayerName();
                TextComponent nameComponent = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)(colorPart + namePart)));
                nameComponent.setBold(Boolean.valueOf(isBest));
                Player friend = VersionHandler.getOnlinePlayer(friendUUID);
                PlayerSettings.OnlineStatus status = friendData.getSettings().getOnlineStatus();
                if (friend == null || status == PlayerSettings.OnlineStatus.APPEAR_OFFLINE) {
                    statusMsg = " &cis currently offline";
                } else if (status == PlayerSettings.OnlineStatus.AWAY) {
                    statusMsg = " &eis currently Away";
                } else if (status == PlayerSettings.OnlineStatus.BUSY) {
                    statusMsg = " &eis currently Busy";
                } else {
                    String world = VersionHandler.getPlayerWorldName(friend);
                    statusMsg = " &eis in " + world;
                }
                TextComponent statusComponent = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)statusMsg));
                statusComponent.setBold(Boolean.valueOf(false));
                TextComponent lineComponent = new TextComponent("");
                lineComponent.addExtra((BaseComponent)nameComponent);
                lineComponent.addExtra((BaseComponent)statusComponent);
                long friendshipTimestamp = this.plugin.getFriendManager().getFriendshipTimestamp(player.getUniqueId(), friendUUID);
                String friendsForText = friendshipTimestamp > 0L ? "&7Friends for " + this.formatDuration(System.currentTimeMillis() - friendshipTimestamp) : "";
                long lastSeenTimestamp = friendData.getLastLogoutTime();
                String lastSeenText = lastSeenTimestamp > 0L ? "&7Last seen " + this.formatDuration(System.currentTimeMillis() - lastSeenTimestamp) + " ago" : "";
                String hover = "";
                hover = friend == null || status == PlayerSettings.OnlineStatus.APPEAR_OFFLINE ? (!lastSeenText.isEmpty() && !friendsForText.isEmpty() ? lastSeenText + "\n" + friendsForText : (lastSeenText.isEmpty() ? friendsForText : lastSeenText)) : friendsForText;
                if (!hover.isEmpty()) {
                    lineComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes((char)'&', (String)hover)).create()));
                }
                player.spigot().sendMessage((BaseComponent)lineComponent);
            }
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            this.sendBorderedMessage(player, "&cUsage: /friend accept <player>");
            return;
        }
        String targetName = args[1];
        UUID targetUUID = this.findPlayerUUID(targetName);
        if (targetUUID == null) {
            this.sendBorderedMessage(player, "&cNo player found with name " + targetName + "&c!");
            return;
        }
        if (!this.plugin.getRequestManager().hasActiveRequest(targetUUID, player.getUniqueId())) {
            this.sendBorderedMessage(player, "&cThat person hasn't invited you to be friends! Try &e/friend " + targetName);
            return;
        }
        if (this.plugin.getRequestManager().acceptRequest(targetUUID, player.getUniqueId())) {
            String display = FriendCommand.getDisplayName(targetUUID, this.plugin.getDataManager().getPlayerData(player.getUniqueId()), false, true);
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&aYou are now friends with " + display)));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
            Player targetPlayer = VersionHandler.getOnlinePlayer(targetUUID);
            if (targetPlayer != null) {
                String display2 = FriendCommand.getDisplayName(player.getUniqueId(), this.plugin.getDataManager().getPlayerData(targetUUID), false, true);
                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&aYou are now friends with " + display2)));
                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
            }
        } else {
            this.sendBorderedMessage(player, "&cNo pending friend request from " + targetName + "&c!");
        }
    }

    private void handleDeny(Player player, String[] args) {
        if (args.length < 2) {
            this.sendBorderedMessage(player, "&cUsage: /friend deny <player>");
            return;
        }
        String targetName = args[1];
        UUID targetUUID = this.findPlayerUUID(targetName);
        if (targetUUID == null) {
            this.sendBorderedMessage(player, "&cNo player found with name " + targetName + "&c!");
            return;
        }
        if (!this.plugin.getRequestManager().hasActiveRequest(targetUUID, player.getUniqueId())) {
            this.sendBorderedMessage(player, "&cThat person hasn't invited you to be friends! Try &e/friend " + targetName);
            return;
        }
        if (this.plugin.getRequestManager().denyRequest(targetUUID, player.getUniqueId())) {
            this.sendBorderedMessage(player, "&cFriend request from " + targetName + " &cdenied!");
            Player targetPlayer = VersionHandler.getOnlinePlayer(targetUUID);
            if (targetPlayer != null) {
                this.sendBorderedMessage(targetPlayer, "&c&e" + player.getName() + " &cdenied your friend request!");
            }
        } else {
            this.sendBorderedMessage(player, "&cNo pending friend request from " + targetName + "&c!");
        }
    }

    private void handleRequests(Player player, String[] args) {
        Map<UUID, FriendRequest> incomingRequests = this.plugin.getRequestManager().getIncomingRequests(player.getUniqueId());
        if (incomingRequests.isEmpty()) {
            this.sendBorderedMessage(player, "&eNo pending friend requests.");
            return;
        }
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
        int requestsPerPage = 8;
        int totalRequests = incomingRequests.size();
        int maxPage = Math.max(1, (int)Math.ceil((double)totalRequests / (double)requestsPerPage));
        if (page > maxPage) {
            page = maxPage;
        }
        int start = (page - 1) * requestsPerPage;
        int end = Math.min(start + requestsPerPage, totalRequests);
        ArrayList<Map.Entry<UUID, FriendRequest>> requestList = new ArrayList<Map.Entry<UUID, FriendRequest>>(incomingRequests.entrySet());
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&6Friend Requests (Page " + page + " of " + maxPage + ")")));
        PlayerData playerData = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
        for (int i = start; i < end; ++i) {
            Map.Entry entry = (Map.Entry)requestList.get(i);
            UUID fromUUID = (UUID)entry.getKey();
            String display = FriendCommand.getDisplayName(fromUUID, playerData, false, true);
            String realName = this.plugin.getDataManager().getPlayerData(fromUUID).getPlayerName();
            TextComponent line = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)("&b" + (i - start + 1) + ". " + display + " &7- ")));
            TextComponent accept = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)"&a&l[ACCEPT]"));
            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + realName));
            accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to accept").create()));
            TextComponent deny = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)" &7- &c&l[DENY]"));
            deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + realName));
            deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to deny").create()));
            TextComponent ignore = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)" &7- &8&l[BLOCK]"));
            ignore.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/block add " + realName));
            ignore.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to block").create()));
            line.addExtra((BaseComponent)accept);
            line.addExtra((BaseComponent)deny);
            line.addExtra((BaseComponent)ignore);
            player.spigot().sendMessage((BaseComponent)line);
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m-----------------------------------------------------"));
    }

    private void handleNotifications(Player player, String[] args) {
        PlayerData data = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
        PlayerSettings settings = data.getSettings();
        int notifLevel = settings.getNotificationLevel();
        if (notifLevel == 2) {
            settings.setNotificationLevel(0);
            this.sendBorderedMessage(player, "&eFriend notifications set to: &aALL");
        } else {
            settings.setNotificationLevel(2);
            this.sendBorderedMessage(player, "&eFriend notifications set to: &cOFF");
        }
        data.setSettings(settings);
        this.plugin.getDataManager().savePlayerData(data);
    }

    private void handleNickname(Player player, String[] args) {
        if (args.length < 2) {
            this.sendBorderedMessage(player, "&cUsage: /friend nickname <player> <nickname>");
            return;
        }
        String targetName = args[1];
        UUID targetUUID = this.findPlayerUUID(targetName);
        if (targetUUID == null) {
            this.sendBorderedMessage(player, "&cNo player found with name " + targetName + "!");
            return;
        }
        if (targetUUID.equals(player.getUniqueId())) {
            this.sendBorderedMessage(player, "&cYou cannot nickname yourself!");
            return;
        }
        PlayerData playerData = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (!this.plugin.getFriendManager().areFriends(player.getUniqueId(), targetUUID)) {
            PlayerData targetData = this.plugin.getDataManager().getPlayerData(targetUUID);
            String targetDisplay = FriendCommand.getDisplayName(targetUUID, targetData, false, true);
            this.sendBorderedMessage(player, targetDisplay + " &cisn't on your friends list!");
            return;
        }
        PlayerData targetData = this.plugin.getDataManager().getPlayerData(targetUUID);
        if (args.length == 2) {
            playerData.removeNickname(targetUUID);
            String targetDisplay = FriendCommand.getDisplayName(targetUUID, targetData, false, true);
            this.sendBorderedMessage(player, targetDisplay + " &ano longer has a nickname!");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; ++i) {
                sb.append(args[i]).append(" ");
            }
            String nickname = sb.toString().trim();
            playerData.setNickname(targetUUID, nickname);
            String targetDisplay = FriendCommand.getDisplayName(targetUUID, targetData, true, true);
            this.sendBorderedMessage(player, "&aSuccessfully set nickname for " + targetDisplay + "&a!");
        }
        this.plugin.getDataManager().savePlayerData(playerData);
    }

    private void handleBest(Player player, String[] args) {
        if (args.length < 2) {
            this.sendBorderedMessage(player, "&cUsage: /friend best <player>");
            return;
        }
        String targetName = args[1];
        UUID targetUUID = this.findPlayerUUID(targetName);
        if (targetUUID == null) {
            this.sendBorderedMessage(player, "&cNo player found with name " + targetName + "!");
            return;
        }
        if (targetUUID.equals(player.getUniqueId())) {
            this.sendBorderedMessage(player, "&cYou cannot best friend yourself!");
            return;
        }
        PlayerData playerData = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (!this.plugin.getFriendManager().areFriends(player.getUniqueId(), targetUUID)) {
            PlayerData targetData = this.plugin.getDataManager().getPlayerData(targetUUID);
            String targetDisplay = FriendCommand.getDisplayName(targetUUID, targetData, false, true);
            this.sendBorderedMessage(player, targetDisplay + " &cisn't on your friends list!");
            return;
        }
        PlayerData targetData = this.plugin.getDataManager().getPlayerData(targetUUID);
        playerData.toggleBestFriend(targetUUID);
        this.plugin.getDataManager().savePlayerData(playerData);
        boolean isBest = playerData.isBestFriend(targetUUID);
        String prefix = targetData.getLastKnownPrefix() != null ? targetData.getLastKnownPrefix() : "";
        String color = targetData.getLastKnownColor() != null ? targetData.getLastKnownColor() : "&f";
        String name = targetData.getPlayerName();
        String rest = isBest ? " &ais now a best friend!" : " &eis no longer a best friend!";
        String msg = ChatColor.translateAlternateColorCodes((char)'&', (String)(prefix + color + name + rest));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
        player.sendMessage(msg);
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
    }

    private void handleRemoveAll(Player player, String[] args) {
        Set<UUID> friends = this.plugin.getFriendManager().getFriends(player.getUniqueId());
        if (friends.isEmpty()) {
            this.sendBorderedMessage(player, "&cYou don't have any friends to remove!");
            return;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&cAre you sure you want to remove all your friends?"));
            TextComponent line = new TextComponent("");
            TextComponent yes = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)"&a&l[YES]"));
            yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend removeall confirm"));
            yes.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to confirm").create()));
            TextComponent no = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)" &c&l[NO]"));
            no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend list"));
            no.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Cancel").create()));
            line.addExtra((BaseComponent)yes);
            line.addExtra((BaseComponent)no);
            player.spigot().sendMessage((BaseComponent)line);
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
            return;
        }
        PlayerData playerData = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
        String senderDisplay = FriendCommand.getDisplayName(player.getUniqueId(), playerData, false, true);
        Set<UUID> bestFriends = playerData.getBestFriends();
        int removedCount = 0;
        for (UUID friendUUID : new HashSet<UUID>(friends)) {
            if (bestFriends.contains(friendUUID)) continue;
            this.plugin.getFriendManager().removeFriend(player.getUniqueId(), friendUUID);
            ++removedCount;
        }
        if (removedCount > 0) {
            this.sendBorderedMessage(player, "&cRemoved " + removedCount + " friend(s). Best friends were not removed.");
        } else {
            this.sendBorderedMessage(player, "&cYou have no friends to remove (best friends are not affected).");
        }
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&aFriend Commands:"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend accept <player> &7- &bAccept a friend request"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend add <player> &7- &bAdd a player as a friend"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend best <player> &7- &bToggle a player as best friend"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend deny <player> &7- &bDecline a friend request"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend help &7- &bPrints all available friend commands."));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend list <page/best> &7- &bList your friends"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend nickname <player> <nickname> &7- &bSet a nickname of a friend"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend notifications &7- &bToggle friend join/leave notifications"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend remove <player> &7- &bRemove a player from your friends"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend removeall &7- &bRemove all your friends (excluding best friends)"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend requests <page> &7- &bView friend requests"));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&e/friend settings &7- &bOpen the friend settings menu."));
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
    }

    private void sendBorderedMessage(Player player, String ... lines) {
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
        for (String line : lines) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)line));
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
    }

    private String getPlayerStatus(Player player) {
        String world = VersionHandler.getPlayerWorldName(player);
        int ping = VersionHandler.getPlayerPing(player);
        if (ping >= 0) {
            return world + " (" + ping + "ms)";
        }
        return world;
    }

    private void sendClickableRequestMessage(Player target, UUID fromUUID) {
        PlayerData fromData = this.plugin.getDataManager().getPlayerData(fromUUID);
        String display = FriendCommand.getDisplayName(fromUUID, fromData, false, true);
        String realName = fromData.getPlayerName();
        target.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
        target.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&eFriend request from " + display)));
        TextComponent accept = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)"&a&l[ACCEPT]"));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + realName));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to accept").create()));
        TextComponent deny = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)" &7- &c&l[DENY]"));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + realName));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to deny").create()));
        TextComponent block = new TextComponent(ChatColor.translateAlternateColorCodes((char)'&', (String)" &7- &8&l[BLOCK]"));
        block.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/block add " + realName));
        block.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Block and remove friend request").create()));
        TextComponent line = new TextComponent("");
        line.addExtra((BaseComponent)accept);
        line.addExtra((BaseComponent)deny);
        line.addExtra((BaseComponent)block);
        target.spigot().sendMessage((BaseComponent)line);
        target.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9&m-----------------------------------------------------"));
    }

    public static String getDisplayName(UUID uuid, PlayerData data, boolean useNickname, boolean useRankColor) {
        Player player = Bukkit.getPlayer((UUID)uuid);
        String name = null;
        String prefix = "";
        String color = "&f";
        if (player != null) {
            try {
                LuckPerms luckPerms = LuckPermsProvider.get();
                User user = luckPerms.getUserManager().getUser(uuid);
                if (user != null) {
                    prefix = user.getCachedData().getMetaData().getPrefix();
                    if (prefix == null) {
                        prefix = "";
                    }
                    color = FriendCommand.extractLastColorCode(prefix);
                }
                name = player.getName();
            }
            catch (Exception e) {
                name = player.getDisplayName();
            }
        }
        if (name == null || name.isEmpty()) {
            name = data.getPlayerName();
            prefix = data.getLastKnownPrefix();
            color = data.getLastKnownColor();
        }
        String display = (prefix != null ? prefix : "") + (useRankColor ? color : "") + name;
        if (useNickname) {
            String nickname = data.getNickname(uuid);
            boolean isBest = data.isBestFriend(uuid);
            if (nickname != null && !nickname.isEmpty()) {
                display = nickname + "*";
            }
            if (isBest) {
                display = "&l" + display;
            }
        }
        return display;
    }

    private static String extractLastColorCode(String input) {
        String last = "";
        for (int i = 0; i < input.length() - 1; ++i) {
            if (input.charAt(i) != '\u00a7' && input.charAt(i) != '&') continue;
            last = "&" + input.charAt(i + 1);
        }
        return last.isEmpty() ? "&f" : last;
    }

    public static String getRankColor(UUID uuid) {
        Player player = Bukkit.getPlayer((UUID)uuid);
        if (player != null) {
            try {
                String color;
                LuckPerms luckPerms = LuckPermsProvider.get();
                User user = luckPerms.getUserManager().getUser(uuid);
                if (user != null && (color = user.getCachedData().getMetaData().getMetaValue("color")) != null) {
                    return color + player.getName();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return player.getDisplayName();
        }
        return uuid.toString().substring(0, 8) + "...";
    }

    public static String getRankColorName(UUID uuid, PlayerData data) {
        Player player = Bukkit.getPlayer((UUID)uuid);
        String name = null;
        String color = "&7";
        if (player != null) {
            try {
                String prefix;
                LuckPerms luckPerms = LuckPermsProvider.get();
                User user = luckPerms.getUserManager().getUser(uuid);
                if (user != null && (prefix = user.getCachedData().getMetaData().getPrefix()) != null) {
                    color = FriendCommand.extractLastColorCode(prefix);
                }
                name = player.getName();
            }
            catch (Exception e) {
                name = player.getDisplayName();
            }
        }
        if (name == null || name.isEmpty()) {
            name = data.getPlayerName();
            color = data.getLastKnownColor();
        }
        return color + name;
    }

    private boolean isKnownSubCommand(String subCommand) {
        return subCommand.equals("add") || subCommand.equals("remove") || subCommand.equals("list") || subCommand.equals("accept") || subCommand.equals("deny") || subCommand.equals("settings") || subCommand.equals("requests") || subCommand.equals("notifications") || subCommand.equals("nickname") || subCommand.equals("best") || subCommand.equals("removeall") || subCommand.equals("help");
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long months = days / 30L;
        if (months > 0L) {
            return months + " month" + (months > 1L ? "s" : "");
        }
        if (days > 0L) {
            return days + " day" + (days > 1L ? "s" : "") + " and " + hours % 24L + " hour" + (hours % 24L != 1L ? "s" : "");
        }
        if (hours > 0L) {
            return hours + " hour" + (hours > 1L ? "s" : "") + " and " + minutes % 60L + " minute" + (minutes % 60L != 1L ? "s" : "");
        }
        if (minutes > 0L) {
            return minutes + " minute" + (minutes > 1L ? "s" : "");
        }
        return seconds + " second" + (seconds != 1L ? "s" : "");
    }

    private static String getRankColor(UUID uuid, PlayerData data) {
        Player player = Bukkit.getPlayer((UUID)uuid);
        String color = data.getLastKnownColor();
        if (player != null) {
            try {
                String prefix;
                LuckPerms luckPerms = LuckPermsProvider.get();
                User user = luckPerms.getUserManager().getUser(uuid);
                if (user != null && (prefix = user.getCachedData().getMetaData().getPrefix()) != null) {
                    color = FriendCommand.extractLastColorCode(prefix);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return color;
    }
}

