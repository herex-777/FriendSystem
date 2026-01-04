package me.herex.friendsystem.placeholders;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.manager.FriendManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class FriendPlaceholders extends PlaceholderExpansion {

    private final FriendSystem plugin;

    // Constructor that accepts the plugin instance
    public FriendPlaceholders(FriendSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "friendsystem"; // Custom placeholder prefix, e.g., %friendsystem_friends_online%
    }

    @Override
    public String getAuthor() {
        return "YourName"; // Your name or plugin name
    }

    @Override
    public String getVersion() {
        return "1.0"; // Version of the expansion
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        // Handle the %friendsystem_friends_online% placeholder
        if (placeholder.equalsIgnoreCase("friends_online")) {
            FriendManager friendManager = plugin.getFriendManager();
            int onlineFriendsCount = friendManager.getOnlineFriendCount(player.getUniqueId());
            return String.valueOf(onlineFriendsCount);
        }

        // Handle the %friendsystem_friends_total% placeholder
        if (placeholder.equalsIgnoreCase("friends_total")) {
            FriendManager friendManager = plugin.getFriendManager();
            int totalFriendsCount = friendManager.getFriendCount(player.getUniqueId());
            return String.valueOf(totalFriendsCount);
        }

        return null; // Return null if the placeholder is not recognized
    }
}
