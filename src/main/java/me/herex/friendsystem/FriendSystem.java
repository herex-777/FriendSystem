package me.herex.friendsystem;

import me.herex.friendsystem.VersionHandler;
import me.herex.friendsystem.cmd.BlockCommand;
import me.herex.friendsystem.cmd.BlockCommandTabCompleter;
import me.herex.friendsystem.cmd.FriendCommand;
import me.herex.friendsystem.cmd.FriendSystemCommand;
import me.herex.friendsystem.cmd.FriendSystemCommandTabCompleter;
import me.herex.friendsystem.cmd.MessageCommand;
import me.herex.friendsystem.cmd.MessageCommandTabCompleter;
import me.herex.friendsystem.cmd.ReplyCommand;
import me.herex.friendsystem.cmd.ReplyCommandTabCompleter;
import me.herex.friendsystem.listener.FriendListener;
import me.herex.friendsystem.manager.BlockManager;
import me.herex.friendsystem.manager.DataManager;
import me.herex.friendsystem.manager.FriendManager;
import me.herex.friendsystem.manager.MessageManager;
import me.herex.friendsystem.manager.RequestManager;
import java.util.List;

import me.herex.friendsystem.placeholders.FriendPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class FriendSystem
        extends JavaPlugin {
    private static FriendSystem instance;
    private DataManager dataManager;
    private FriendManager friendManager;
    private RequestManager requestManager;
    private MessageManager messageManager;
    private BlockManager blockManager;
    private BlockCommand blockCommand;
    private List<String> allowedWorlds;
    private List<String> allowedServers;

    public void onEnable() {
        instance = this;

        // Initialize version handler
        VersionHandler.init();

        // Save and reload configuration files
        this.saveDefaultConfig();
        this.reloadConfig();
        this.loadGuiConfig();

        // Initialize managers
        this.initializeManagers();

        // Load all player data and requests
        this.dataManager.loadAllData();
        this.requestManager.loadAllPendingRequests();

        // Register commands and event listeners
        this.registerCommands();
        this.registerListeners();

        // Delay the registration of placeholders to ensure PlaceholderAPI is loaded
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                new FriendPlaceholders(this).register(); // Register your custom placeholders
                getLogger().info("FriendSystem placeholders registered successfully.");
            } else {
                getLogger().warning("PlaceholderAPI not found! Friend placeholders will not be available.");
            }
        }, 20L); // Delay the registration by 1 second (20 ticks)

        // Log plugin enabled success
        this.getLogger().info("FriendSystem has been enabled successfully!");
        this.getLogger().info("Server version: " + VersionHandler.getServerVersion());
    }

    public void onDisable() {
        if (this.dataManager != null) {
            this.dataManager.saveAllData();
        }
        this.getLogger().info("FriendSystem has been disabled!");
    }

    private void initializeManagers() {
        this.dataManager = new DataManager(this);
        this.friendManager = new FriendManager(this);
        this.requestManager = new RequestManager(this);
        this.messageManager = new MessageManager(this);
        this.blockManager = new BlockManager(this);
        this.blockCommand = new BlockCommand(this);
    }

    private void registerCommands() {
        FriendCommand friendCommand = new FriendCommand(this);
        this.getCommand("friend").setExecutor((CommandExecutor)friendCommand);
        this.getCommand("f").setExecutor((CommandExecutor)friendCommand);
        this.getCommand("msg").setExecutor((CommandExecutor)new MessageCommand(this));
        this.getCommand("msg").setTabCompleter((TabCompleter)new MessageCommandTabCompleter(this));
        this.getCommand("r").setExecutor((CommandExecutor)new ReplyCommand(this));
        this.getCommand("r").setTabCompleter((TabCompleter)new ReplyCommandTabCompleter());
        this.getCommand("block").setExecutor((CommandExecutor)new BlockCommand(this));
        this.getCommand("block").setTabCompleter((TabCompleter)new BlockCommandTabCompleter());
        this.getCommand("friendsystem").setExecutor((CommandExecutor)new FriendSystemCommand(this));
        this.getCommand("friendsystem").setTabCompleter((TabCompleter)new FriendSystemCommandTabCompleter());
    }

    private void registerListeners() {
        FriendListener listener = new FriendListener(this);
        this.getServer().getPluginManager().registerEvents((Listener)listener, (Plugin)this);
        listener.scheduleCleanupTasks();
    }

    public static FriendSystem getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public FriendManager getFriendManager() {
        return this.friendManager;
    }

    public RequestManager getRequestManager() {
        return this.requestManager;
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }

    public BlockManager getBlockManager() {
        return this.blockManager;
    }

    public void loadGuiConfig() {
        this.allowedWorlds = this.getConfig().getStringList("gui.lobby_worlds");
        this.allowedServers = this.getConfig().getStringList("gui.lobby_servers");
    }

    public static boolean isGuiAllowed(Player player) {
        FriendSystem plugin = FriendSystem.getInstance();
        String world = player.getWorld().getName();
        if (plugin.allowedWorlds != null && plugin.allowedWorlds.contains(world)) {
            return true;
        }
        String server = player.getServer().getServerName();
        return plugin.allowedServers != null && plugin.allowedServers.contains(server);
    }
}

