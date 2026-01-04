package me.herex.friendsystem.listener;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.VersionHandler;
import me.herex.friendsystem.cmd.FriendCommand;
import me.herex.friendsystem.model.FriendRequest;
import me.herex.friendsystem.model.PlayerData;
import me.herex.friendsystem.model.PlayerSettings;
import me.herex.friendsystem.util.ActionBarUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FriendListener implements Listener {

    private final FriendSystem plugin;
    private final Map<UUID, Integer> actionBarTasks = new ConcurrentHashMap<>();

    public FriendListener(FriendSystem plugin) {
        this.plugin = plugin;
    }

    /* =========================
       PLAYER JOIN / QUIT
       ========================= */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                handlePlayerJoin(player);
                startPersistentActionBar(player);
            }
        }.runTaskLater(plugin, 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        handlePlayerQuit(player);
        stopPersistentActionBar(player.getUniqueId());
    }

    /* =========================
       JOIN LOGIC
       ========================= */

    private void handlePlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();

        plugin.getRequestManager().activateRequestsFor(uuid);
        plugin.getRequestManager().clearExpiredRequests();
        plugin.getDataManager().clearExpiredRequests();

        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        data.setPlayerName(player.getName());

        // LuckPerms prefix handling
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(uuid);
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix == null) prefix = "";
                data.setLastKnownPrefix(prefix);
                data.setLastKnownColor(extractLastColorCode(prefix));
            }
        } catch (Exception ignored) {}

        plugin.getDataManager().savePlayerData(data);

        // Incoming friend requests
        Map<UUID, FriendRequest> requests =
                plugin.getRequestManager().getIncomingRequests(uuid);

        if (!requests.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&9&m-----------------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&eYou have &6" + requests.size() + " &epending friend request(s)!"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&7Use &e/friend requests &7to view them."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&9&m-----------------------------------------------"));
        }

        // Notify friends
        Set<UUID> friends = plugin.getFriendManager().getFriends(uuid);
        if (friends.isEmpty()) return;
        if (data.getSettings().isAppearOffline()) return;

        String display = FriendCommand.getRankColorName(uuid, data);
        String message = "&aFriend > " + display + " &ejoined.";

        for (UUID friendUUID : friends) {
            Player friend = VersionHandler.getOnlinePlayer(friendUUID);
            if (friend == null || !friend.isOnline()) continue;
            if (!plugin.getDataManager().getPlayerData(friendUUID)
                    .getSettings().isNotifyOnline()) continue;

            plugin.getMessageManager().sendSystemMessage(friendUUID, message);
        }

        plugin.getRequestManager().clearSenderBlocksOnLogin(uuid);
    }

    /* =========================
       QUIT LOGIC
       ========================= */

    private void handlePlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        Set<UUID> friends = plugin.getFriendManager().getFriends(uuid);

        if (!friends.isEmpty() && !data.getSettings().isAppearOffline()) {
            String display = FriendCommand.getRankColorName(uuid, data);
            String message = "&aFriend > " + display + " &eleft.";

            for (UUID friendUUID : friends) {
                Player friend = VersionHandler.getOnlinePlayer(friendUUID);
                if (friend == null || !friend.isOnline()) continue;
                if (!plugin.getDataManager().getPlayerData(friendUUID)
                        .getSettings().isNotifyOnline()) continue;

                plugin.getMessageManager().sendSystemMessage(friendUUID, message);
            }
        }

        plugin.getMessageManager().clearMessageHistory(uuid);
        data.setLastLogoutTime(System.currentTimeMillis());
        plugin.getDataManager().savePlayerData(data);
    }

    /* =========================
       ACTION BAR (FIXED)
       ========================= */

    private void startPersistentActionBar(Player player) {
        UUID uuid = player.getUniqueId();
        stopPersistentActionBar(uuid);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!player.isOnline()) return;
            if (!FriendSystem.isGuiAllowed(player)) return;

            PlayerSettings.OnlineStatus status =
                    plugin.getDataManager().getPlayerData(uuid)
                            .getSettings().getOnlineStatus();

            String message = getStatusMessage(status);
            if (message != null && !message.isEmpty()) {
                ActionBarUtil.sendActionBar(player, message);
            }
        }, 0L, 20L);

        actionBarTasks.put(uuid, taskId);
    }

    private void stopPersistentActionBar(UUID uuid) {
        Integer taskId = actionBarTasks.remove(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    /* =========================
       STATUS MESSAGES
       ========================= */

    private String getStatusMessage(PlayerSettings.OnlineStatus status) {
        switch (status) {
            case AWAY:
                return "§fYou are currently §cAWAY";
            case BUSY:
                return "§fYou are currently §cBUSY";
            case APPEAR_OFFLINE:
                return "§fYou are currently §cAPPEARING OFFLINE";
            case ONLINE:
            default:
                return null; // IMPORTANT: never return empty strings
        }
    }

    /* =========================
       UTIL
       ========================= */

    private static String extractLastColorCode(String input) {
        if (input == null) return "&f";
        String last = "";
        for (int i = 0; i < input.length() - 1; i++) {
            char c = input.charAt(i);
            if (c == '§' || c == '&') {
                last = "&" + input.charAt(i + 1);
            }
        }
        return last.isEmpty() ? "&f" : last;
    }

    public void scheduleCleanupTasks() {
    }
}
