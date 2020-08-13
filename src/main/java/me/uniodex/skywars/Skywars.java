package me.uniodex.skywars;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.authlib.GameProfile;
import me.uniodex.skywars.arena.Arena;
import me.uniodex.skywars.arena.ArenaManager;
import me.uniodex.skywars.commands.BaseCommand;
import me.uniodex.skywars.customization.Config;
import me.uniodex.skywars.customization.Customization;
import me.uniodex.skywars.enums.ArenaState;
import me.uniodex.skywars.listeners.*;
import me.uniodex.skywars.managers.*;
import me.uniodex.skywars.party.SWPartyManager;
import me.uniodex.skywars.player.BukkitPlayerManager;
import me.uniodex.skywars.player.SWOnlinePlayer;
import me.uniodex.skywars.player.SWPlayerManager;
import me.uniodex.skywars.utils.Utils;
import me.uniodex.skywars.utils.packages.menubuilder.inventory.InventoryListener;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Skywars extends JavaPlugin implements PluginMessageListener {

    public static Skywars instance;
    public static String nmsver;
    public static Field nameField;
    public Chat chat;
    public Permission permission;
    public Random r = new Random();
    public ChatColor[] colors = {ChatColor.DARK_AQUA, ChatColor.GOLD, ChatColor.GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW};
    public HashMap<String, Location[]> selectionMode = new HashMap<>();
    public HashMap<String, String> editingChests = new HashMap<String, String>();
    public int[] smartSlots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    public Location lobbyLocation;
    public BukkitTask savingTask;
    public BukkitTask scoreboardTitleAnimationTask;
    public Sound CLICK, NOTE_PLING;
    //Classes
    public Config config;
    public SQLManager sqlManager;
    public Customization customization;
    public Economy vault;
    public ConfigManager configManager;
    public InteractionListeners interactionListeners;
    public ClickableItemManager clickableItemManager;
    public BuyableItemManager buyableItemManager;
    public SWPlayerManager playerManager;
    public BukkitPlayerManager bukkitPlayerManager;
    public SWPartyManager partyManager;
    public MenuManager menuManager;
    public ArenaManager arenaManager;
    public ScoreboardManager scoreboardManager;
    public WorldManager worldManager;
    public ClickableItemListeners clickableItemListeners;
    public ChestEmptyListener chestEmptyListener;
    public CoreListeners coreListeners;
    public ProtectionListeners protectionListeners;
    public StatListeners statListeners;
    public InventoryListener inventoryListener;
    public boolean allowJoin = false;
    public boolean serverWillStop = false;

    public void onEnable() {
        preLoad();
        load();
        postLoad();
    }

    private void preLoad() {
        allowJoin = false;
        instance = this;
        /*if (Bukkit.getPluginManager().getPlugin("Spigot-Party-API-PAF") == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }*/
        if (!Bukkit.getPluginManager().getPlugin("HolographicDisplays").isEnabled()) {
            getLogger().severe("HolographicDisplays not found!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(this);
        FileConfiguration customizationFile = configManager.getCustomizationConfig();
        customization = new Customization(customizationFile);
        config = new Config(this);
        nameField = getField(GameProfile.class, "name");

        loadSounds();
        setupChat();
        setupPermissions();
        externalLoad();
    }

    private void load() {
        long before = System.currentTimeMillis();
        Bukkit.getConsoleSender().sendMessage(customization.prefix + "Attempting to connect to the database!");
        sqlManager = new SQLManager(this, getConfig().getString("MySQL.table"), getConfig().getString("MySQL.host"), getConfig().getString("MySQL.port"), getConfig().getString("MySQL.database"), getConfig().getString("MySQL.username"), getConfig().getString("MySQL.password"));
        Bukkit.getConsoleSender().sendMessage(customization.prefix + "Connection has been successfully established! it took " + (System.currentTimeMillis() - before) + "ms to complete!");

        clickableItemManager = new ClickableItemManager(this);
        buyableItemManager = new BuyableItemManager(this);
        playerManager = new SWPlayerManager(this);
        bukkitPlayerManager = new BukkitPlayerManager(this);
        menuManager = new MenuManager(this);
        arenaManager = new ArenaManager(this);
        scoreboardManager = new ScoreboardManager(this);
        partyManager = new SWPartyManager(this);
        worldManager = new WorldManager(this);
        registerEvents();

        getCommand("skywars").setExecutor(new BaseCommand(this));
        getCommand("sw").setExecutor(new BaseCommand(this));
        getCommand("stats").setExecutor(new BaseCommand(this));
        getCommand("istatistikler").setExecutor(new BaseCommand(this));
        getCommand("yenioyunagir").setExecutor(new BaseCommand(this));
        getCommand("hub").setExecutor(new BaseCommand(this));
        getCommand("lobi").setExecutor(new BaseCommand(this));
        getCommand("lobby").setExecutor(new BaseCommand(this));
        getCommand("kurallar").setExecutor(new BaseCommand(this));
        getCommand("rules").setExecutor(new BaseCommand(this));
        getCommand("vip").setExecutor(new BaseCommand(this));
        getCommand("vipbilgi").setExecutor(new BaseCommand(this));
        getCommand("help").setExecutor(new BaseCommand(this));
        getCommand("komutlar").setExecutor(new BaseCommand(this));
        getCommand("coin").setExecutor(new BaseCommand(this));
        getCommand("ucoin").setExecutor(new BaseCommand(this));
        getCommand("disguise").setExecutor(new BaseCommand(this));
        getCommand("undisguise").setExecutor(new BaseCommand(this));

        Bukkit.getMessenger().registerIncomingPluginChannel(this, "Return", this);
    }

    private void postLoad() {
        setupEconomy();
        if (!Bukkit.getMessenger().isOutgoingChannelRegistered(this, "BungeeCord"))
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        scheduledSetup();

        FileConfiguration customizationFile = configManager.getCustomizationConfig();
        for (ArenaState state : ArenaState.values()) {
            state.setStateName(ChatColor.translateAlternateColorCodes('&', customizationFile.getString("States." + state.name())));
        }

        loadLobbyWorld();
        arenaManager.loadChestItems();
        arenaManager.loadArenas();

        allowJoin = true;

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(ChatColor.RED + "=======================================");
        console.sendMessage(customization.prefix + ChatColor.GREEN + "Plugin has been enabled (v" + getDescription().getVersion() + ")");
        console.sendMessage(ChatColor.RED + "=======================================");
    }

    private void externalLoad() {
        // ActionBarAPI için NMS versiyonunu tanımla
        nmsver = Bukkit.getServer().getClass().getPackage().getName();
        nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

        // ClearLag log temizleyici.
        Bukkit.getServer().getScheduler()
                .scheduleAsyncDelayedTask(this, new Runnable() {
                    public void run() {
                        long time = new Date().getTime() - 86400000L * 60;

                        File folder = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath() + "/logs");
                        if (!folder.exists()) {
                            return;
                        }
                        File[] files = folder.listFiles();

                        for (File file : files) {
                            if ((file.isFile()) && (file.getName().endsWith(".log.gz")) && (time > Utils.parseTime(file.getName().replace(".log.gz", "")).getTime())) {
                                file.delete();
                            }
                        }
                        System.out.println("Loglar temizlendi.");
                    }
                }, 1L);

        // Reflex log temizleyici.
        Bukkit.getServer().getScheduler()
                .scheduleAsyncDelayedTask(this, new Runnable() {
                    public void run() {
                        long time = new Date().getTime() - 86400000L * 60;

                        File folder = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath() + "/plugins/Reflex/logs");
                        if (!folder.exists()) {
                            return;
                        }
                        File[] files = folder.listFiles();

                        for (File file : files) {
                            if ((file.isFile()) && (file.getName().endsWith(".log")) && (time > Utils.parseReflexTime(file.getName().replace(".log", "")).getTime())) {
                                file.delete();
                            }
                        }
                        System.out.println("Reflex logları temizlendi.");
                    }
                }, 1L);
    }

    //TODO Recode
    private void scheduledSetup() {
        Long serverWillStopIn = r.nextInt(72000) + 216000L;

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                serverWillStop = true;
                if (!arenaManager.isTherePlayingArenas()) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(Utils.getRandomServer(config.gameMode));
                        p.sendPluginMessage(Skywars.instance, "BungeeCord", out.toByteArray());
                    }
                    Bukkit.shutdown();
                }
            }
        }, serverWillStopIn);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                if (serverWillStop && !arenaManager.isTherePlayingArenas()) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(Utils.getRandomServer(config.gameMode));
                        p.sendPluginMessage(Skywars.instance, "BungeeCord", out.toByteArray());
                    }
                    Bukkit.shutdown();
                }
            }
        }, 100L, 5 * 1200);

        //TODO Save player data to database every x minutes without losing or duplicating data
        if (savingTask != null) savingTask.cancel();
        if (getConfig().getBoolean("Saving-Task.Enabled")) {
            savingTask = new BukkitRunnable() {
                public void run() {
                    new BukkitRunnable() {
                        public void run() {
                            for (Iterator<SWOnlinePlayer> iterator = playerManager.getOnlinePlayers().values().iterator(); iterator.hasNext(); ) {
                                SWOnlinePlayer swPlayer = iterator.next();
                                swPlayer.saveData(false, false);
                            }
                            cancel();
                            Bukkit.getConsoleSender().sendMessage(customization.prefix + "Players stats have been saved!");
                        }
                    }.runTaskTimerAsynchronously(instance, 0, 1);
                }
            }.runTaskTimer(this, getConfig().getInt("Saving-Task.Save-Every-Minutes") * 1200, getConfig().getInt("Saving-Task.Save-Every-Minutes") * 1200);
        } else savingTask = null;
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(clickableItemListeners = new ClickableItemListeners(this), this);
        Bukkit.getPluginManager().registerEvents(coreListeners = new CoreListeners(this), this);
        Bukkit.getPluginManager().registerEvents(protectionListeners = new ProtectionListeners(this), this);
        Bukkit.getPluginManager().registerEvents(statListeners = new StatListeners(this), this);
        Bukkit.getPluginManager().registerEvents(inventoryListener = new InventoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(interactionListeners = new InteractionListeners(this), this);
        Bukkit.getPluginManager().registerEvents(chestEmptyListener = new ChestEmptyListener(this), this);

    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
        return (chat != null);
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private void setupEconomy() {
        vault = null;
        if (getConfig().getBoolean("use-Vault") && Bukkit.getPluginManager().getPlugin("Vault") != null && Bukkit.getServicesManager().getRegistration(Economy.class) != null) {
            vault = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
            Bukkit.getConsoleSender().sendMessage(customization.prefix + "The plugin will be using vault economy instead of coins..");
        }
    }

    private void loadLobbyWorld() {
        if (getConfig().contains("Lobby")) {
            String worldName = getConfig().getString("Lobby").split(",")[0];

            if (Bukkit.getWorld(worldName) == null) {
                Bukkit.getConsoleSender().sendMessage("The lobby world seems to be unloaded! attempting to import it");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sw worldmanager import " + worldName);
            }

            lobbyLocation = Utils.getLocationFromString(getConfig().getString("Lobby"));
        }
    }

    private void loadSounds() {
        try {
            CLICK = Sound.valueOf("CLICK");
            NOTE_PLING = Sound.valueOf("NOTE_PLING");
        } catch (IllegalArgumentException e) {
            CLICK = Sound.valueOf("UI_BUTTON_CLICK");
            NOTE_PLING = Sound.valueOf("BLOCK_NOTE_PLING");
        }
    }

    public void onDisable() {
        for (SWOnlinePlayer p : playerManager.getOnlinePlayers().values()) {
            p.saveData(true, true);
            if (p.getPlayer() != null) {
                p.getPlayer().kickPlayer("Sunucu yeniden başlatılıyor!");
            }
        }
        chestEmptyListener.deleteAllHolograms();
        CoreListeners.onDisable();
        sqlManager.onDisable();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "Return", this);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getConsoleSender().sendMessage(customization.prefix + "Plugin has been disabled!");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player p, byte[] message) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            String sub = in.readUTF();
            if (sub.equals("command")) {
                String cmd = in.readUTF();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateScoreboardTitleTask() {
        // Scoreboard title animation task
        if (config.scoreboardTitleAnimationEnabled && scoreboardTitleAnimationTask == null) {
            if (playerManager.getOnlinePlayers().isEmpty()) {
                scoreboardTitleAnimationTask.cancel();
                scoreboardTitleAnimationTask = null;
                return;
            }

            scoreboardTitleAnimationTask = new BukkitRunnable() {
                int index = 0;

                public void run() {
                    String title = config.scoreboardTitleAnimationFrames.get(index);
                    if (++index >= config.scoreboardTitleAnimationFrames.size()) index = 0;
                    for (Arena arena : arenaManager.getArenas().values()) {
                        arena.setScoreboardName(title);
                    }
                }
            }.runTaskTimer(this, config.scoreboardTitleAnimationInterval, config.scoreboardTitleAnimationInterval);
        }
    }

    private Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
}
