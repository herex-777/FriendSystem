package me.herex.friendsystem.gui;

import me.herex.friendsystem.FriendSystem;
import me.herex.friendsystem.model.PlayerSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendSettingsGUI implements Listener {

    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "Social Settings";
    private static final int GUI_SIZE = 54;

    private final FriendSystem plugin;
    private final Player player;
    private final UUID uuid;
    private Inventory inventory;

    public FriendSettingsGUI(FriendSystem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.uuid = player.getUniqueId();
    }

    /* =========================
       OPEN GUI
       ========================= */

    public void open() {
        inventory = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        updateItems();
        player.openInventory(inventory);
    }

    private void updateItems() {
        inventory.clear();
        addOnlineStatus();
        addRequestPrivacy();
        addMessagePrivacy();
        addFriendNotifications();
    }

    /* =========================
       ONLINE STATUS
       ========================= */

    private void addOnlineStatus() {
        PlayerSettings settings = plugin.getDataManager().getPlayerData(uuid).getSettings();
        PlayerSettings.OnlineStatus current = settings.getOnlineStatus();

        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Change your online status.");
        lore.add("");
        lore.add(ChatColor.GRAY + "Options:");

        for (PlayerSettings.OnlineStatus status : PlayerSettings.OnlineStatus.values()) {
            String color = ChatColor.GREEN.toString();
            if (status == PlayerSettings.OnlineStatus.AWAY) color = ChatColor.YELLOW.toString();
            if (status == PlayerSettings.OnlineStatus.BUSY) color = ChatColor.DARK_PURPLE.toString();
            if (status == PlayerSettings.OnlineStatus.APPEAR_OFFLINE) color = ChatColor.GRAY.toString();

            String prefix = status == current ? color + "» " : "  ";
            lore.add(prefix + color + toTitle(status.name()));
        }

        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to change!");

        inventory.setItem(4, createItem(Material.ENDER_PEARL, ChatColor.GREEN + "Online Status", lore));
        inventory.setItem(13, createDye(getStatusDye(current)));
    }

    /* =========================
       REQUEST PRIVACY
       ========================= */

    private void addRequestPrivacy() {
        PlayerSettings settings = plugin.getDataManager().getPlayerData(uuid).getSettings();
        PlayerSettings.RequestPrivacy current = settings.getRequestPrivacy();

        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Who can send you requests?");
        lore.add("");
        lore.add(ChatColor.GRAY + "Options:");
        lore.add((current == PlayerSettings.RequestPrivacy.MAX ? ChatColor.RED + "» " : "  ") + ChatColor.RED + "Max (Staff)");
        lore.add((current == PlayerSettings.RequestPrivacy.HIGH ? ChatColor.GOLD + "» " : "  ") + ChatColor.GOLD + "High (Staff, Lobby)");
        lore.add((current == PlayerSettings.RequestPrivacy.NONE ? ChatColor.WHITE + "» " : "  ") + ChatColor.WHITE + "None (Anyone)");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to change!");

        inventory.setItem(30, createItem(Material.JUKEBOX, ChatColor.GREEN + "Friend Request Privacy", lore));
        inventory.setItem(39, createDye(getRequestPrivacyDye(current)));
    }

    /* =========================
       MESSAGE PRIVACY
       ========================= */

    private void addMessagePrivacy() {
        PlayerSettings settings = plugin.getDataManager().getPlayerData(uuid).getSettings();
        PlayerSettings.MessagePrivacy current = settings.getMessagePrivacy();

        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Who can private message you?");
        lore.add("");
        lore.add(ChatColor.GRAY + "Options:");

        for (PlayerSettings.MessagePrivacy p : PlayerSettings.MessagePrivacy.values()) {
            String prefix = p == current ? ChatColor.GREEN + "» " : "  ";
            lore.add(prefix + ChatColor.WHITE + toTitle(p.name()));
        }

        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to change!");

        inventory.setItem(31, createItem(Material.SIGN, ChatColor.GREEN + "Private Messages", lore));
        inventory.setItem(40, createDye(getMessagePrivacyDye(current)));
    }

    /* =========================
       FRIEND NOTIFICATIONS
       ========================= */

    private void addFriendNotifications() {
        PlayerSettings settings = plugin.getDataManager().getPlayerData(uuid).getSettings();
        int level = settings.getNotificationLevel();

        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Friend join/leave messages.");
        lore.add("");
        lore.add((level == 0 ? ChatColor.GOLD + "» " : "  ") + "All");
        lore.add((level == 1 ? ChatColor.GREEN + "» " : "  ") + "Best");
        lore.add((level == 2 ? ChatColor.WHITE + "» " : "  ") + "None");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to change!");

        inventory.setItem(32, createItem(Material.BOOK, ChatColor.GREEN + "Friend Notifications", lore));
        inventory.setItem(41, createDye(level == 0 ? 11 : level == 1 ? 10 : 15));
    }

    /* =========================
       CLICK HANDLING (FIXED)
       ========================= */

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory() == null) return;
        if (!e.getInventory().equals(inventory)) return;

        e.setCancelled(true);

        int slot = e.getRawSlot();
        PlayerSettings settings = plugin.getDataManager().getPlayerData(uuid).getSettings();

        if (slot == 4 || slot == 13) {
            settings.setOnlineStatus(getNextOnlineStatus(settings.getOnlineStatus()));
            feedback("Online status updated");
        } else if (slot == 30 || slot == 39) {
            settings.setRequestPrivacy(getNextRequestPrivacy(settings.getRequestPrivacy()));
            feedback("Request privacy updated");
        } else if (slot == 31 || slot == 40) {
            settings.setMessagePrivacy(getNextMessagePrivacy(settings.getMessagePrivacy()));
            feedback("Message privacy updated");
        } else if (slot == 32 || slot == 41) {
            settings.setNotificationLevel((settings.getNotificationLevel() + 1) % 3);
            feedback("Friend notifications updated");
        }

        plugin.getDataManager().savePlayerData(
                plugin.getDataManager().getPlayerData(uuid)
        );

        updateItems();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }

    /* =========================
       UTIL
       ========================= */

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDye(int data) {
        return new ItemStack(Material.INK_SACK, 1, (short) data);
    }

    private int getStatusDye(PlayerSettings.OnlineStatus s) {
        if (s == PlayerSettings.OnlineStatus.AWAY) return 11;
        if (s == PlayerSettings.OnlineStatus.BUSY) return 5;
        if (s == PlayerSettings.OnlineStatus.APPEAR_OFFLINE) return 8;
        return 10;
    }

    private int getRequestPrivacyDye(PlayerSettings.RequestPrivacy p) {
        if (p == PlayerSettings.RequestPrivacy.MAX) return 1;
        if (p == PlayerSettings.RequestPrivacy.HIGH) return 14;
        return 15;
    }

    private int getMessagePrivacyDye(PlayerSettings.MessagePrivacy p) {
        if (p == PlayerSettings.MessagePrivacy.MAX) return 1;
        if (p == PlayerSettings.MessagePrivacy.HIGH) return 14;
        if (p == PlayerSettings.MessagePrivacy.MEDIUM) return 11;
        if (p == PlayerSettings.MessagePrivacy.LOW) return 10;
        return 15;
    }

    private PlayerSettings.OnlineStatus getNextOnlineStatus(PlayerSettings.OnlineStatus c) {
        PlayerSettings.OnlineStatus[] v = PlayerSettings.OnlineStatus.values();
        return v[(c.ordinal() + 1) % v.length];
    }

    private PlayerSettings.RequestPrivacy getNextRequestPrivacy(PlayerSettings.RequestPrivacy c) {
        PlayerSettings.RequestPrivacy[] v = PlayerSettings.RequestPrivacy.values();
        return v[(c.ordinal() + 1) % v.length];
    }

    private PlayerSettings.MessagePrivacy getNextMessagePrivacy(PlayerSettings.MessagePrivacy c) {
        PlayerSettings.MessagePrivacy[] v = PlayerSettings.MessagePrivacy.values();
        return v[(c.ordinal() + 1) % v.length];
    }

    private void feedback(String msg) {
        player.playSound(player.getLocation(), "CLICK", 1f, 1f);
        player.sendMessage(ChatColor.GREEN + "✔ " + msg);
    }

    private String toTitle(String s) {
        return s.substring(0, 1).toUpperCase()
                + s.substring(1).toLowerCase().replace("_", " ");
    }
}
