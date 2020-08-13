package me.uniodex.skywars.player;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.arena.Arena;
import me.uniodex.skywars.enums.ArenaState;
import me.uniodex.skywars.utils.Utils;
import me.uniodex.skywars.utils.packages.actionbarapi.ActionBarAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

public class SWOnlinePlayer {

    // NOT: Burada veri saklayabilirsin ama oyuncu çıktıktan sonra buradaki tüm veriler sıfırlanacaktır.

    private Skywars plugin;
    private Player player;
    private String playerName;

    private Arena arena;
    private int lastKillsAmount = 0;
    private HashMap<String, Long> cooldowns = new HashMap<String, Long>();
    private String lastAttacker = "";
    private long lastAttackedTime = 0;
    private long lastPlayTime;
    private BukkitTask actionBarTask;
    private boolean isSaving = false;
    private boolean isLoading = false;

    private int coins;

    // Stats
    private int wins, playedGames, kills, deaths, projectilesHit, projectilesLaunched, blocksPlaced, blocksBroken, itemsEnchanted, itemsCrafted, fishesCaught, playTime;

    // Items
    private ArrayList<String> kits = new ArrayList<String>();
    private ArrayList<String> trails = new ArrayList<String>();
    private ArrayList<String> cages = new ArrayList<String>();

    // Selected Items
    private String selectedKit = "Default", selectedCage = "Default", selectedTrail = "Default";

    public SWOnlinePlayer(Skywars plugin, Player player) {
        if (player != null) {
            this.player = player;
            this.playerName = player.getName();
            this.plugin = plugin;
            isLoading = true;
            loadData();
        } else {
            throw new NullPointerException("Belirtilen oyuncu bulunamadı.");
        }
    }

    public void startActionBarTask() {
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                String killAmount = arena.getKillers().containsKey(playerName) ? String.valueOf(arena.getKillers().get(playerName)) : "0";
                String text = Utils.c("&6&lLeşler: &f" + killAmount + " &0: &6&lSiz: &f" + playerName);
                ActionBarAPI.sendActionBar(player, text);
            }
        }, 20L, 20L);

   }

    public Player getPlayer() {
        if (player != null) {
            return player;
        } else {
            if (playerName != null) {
                return Bukkit.getPlayer(playerName);
            } else {
                return null;
            }
        }
    }

    public SWOfflinePlayer getSWOfflinePlayer() {
        return plugin.playerManager.getSWPlayer(playerName);
    }

    private void loadData() {
        new BukkitRunnable() {
            public void run() {
                if (playerName != null) {
                    plugin.sqlManager.createPlayer(playerName);
                }
                loadStats();
                loadCoins();
                loadItems();
                loadSelectedItems();
                isLoading = false;
            }
        }.runTaskAsynchronously(plugin);
        lastPlayTime = System.currentTimeMillis();
    }

    public void saveData(boolean destroy, boolean sync) {
        updatePlayTime();
        if (!isSaving) {
            isSaving = true;
            plugin.sqlManager.savePlayerStats(sync, playerName, wins, playedGames, kills, deaths, projectilesHit, projectilesLaunched, blocksPlaced, blocksBroken, itemsEnchanted, itemsCrafted, fishesCaught, playTime);
            plugin.sqlManager.setCoins(playerName, coins, sync);
            isSaving = false;
        }

        if (destroy) {
            destroy();
        }

    }

    public void destroy() {
        plugin = null;
        player = null;
        playerName = null;
        arena = null;
        cooldowns = null;
        lastAttacker = null;
        lastAttackedTime = 0;
        lastPlayTime = 0;
        actionBarTask = null;
        coins = 0;
        wins = 0;
        playedGames = 0;
        playTime = 0;
        kills = 0;
        deaths = 0;
        projectilesHit = 0;
        projectilesLaunched = 0;
        blocksPlaced = 0;
        blocksBroken = 0;
        itemsEnchanted = 0;
        itemsCrafted = 0;
        fishesCaught = 0;
        selectedKit = null;
        selectedCage = null;
        selectedTrail = null;
    }

    private void loadStats() {
        if (playerName == null) {
            return;
        }

        HashMap<String, Integer> stats = plugin.sqlManager.getPlayerStats(playerName);
        if (stats == null) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    getPlayer().kickPlayer("İstatistiklerinizi yüklerken bir sorun oluştu.");
                }
            });
            return;
        }
        wins = stats.get("wins");
        playedGames = stats.get("playedGames");
        kills = stats.get("kills");
        deaths = stats.get("deaths");
        projectilesHit = stats.get("projectilesHit");
        projectilesLaunched = stats.get("projectilesLaunched");
        blocksPlaced = stats.get("blocksPlaced");
        blocksBroken = stats.get("blocksBroken");
        itemsEnchanted = stats.get("itemsEnchanted");
        itemsCrafted = stats.get("itemsCrafted");
        fishesCaught = stats.get("fishesCaught");
        playTime = stats.get("playTime");
    }

    private void loadCoins() {
        if (playerName == null) {
            return;
        }
        int coins = plugin.sqlManager.getPlayerCoins(playerName);
        if (coins != -1) {
            setCoins(coins);
        }
    }

    private void loadItems() {
        if (playerName == null) {
            return;
        }
        ArrayList<String> playerKits = plugin.sqlManager.getPlayerKits(playerName);
        if (getPlayer().hasPermission("rank.vip")) {
            playerKits.add("soloZirhci");
            playerKits.add("duoZirhci");
            playerKits.add("soloBalikci");
            playerKits.add("duoBalikci");
            playerKits.add("soloBalcik");
            playerKits.add("duoBalcik");
        }
        kits.addAll(playerKits);

        ArrayList<String> playerCages = plugin.sqlManager.getPlayerCages(playerName);
        if (getPlayer().hasPermission("rank.vip")) {
            playerCages.add("soloGorunmez");
            playerCages.add("duoGorunmez");
        }
        cages.addAll(playerCages);

        ArrayList<String> playerTrails = plugin.sqlManager.getPlayerTrails(playerName);
        if (getPlayer().hasPermission("rank.vip")) {
            playerTrails.add("soloElmas");
            playerTrails.add("duoElmas");
            playerTrails.add("soloTNT");
            playerTrails.add("duoTNT");
        }
        trails.addAll(playerTrails);
    }

    private void loadSelectedItems() {
        if (playerName == null) {
            return;
        }
        HashMap<String, String> items = plugin.sqlManager.getPlayerSelectedItems(playerName);
        setSelectedItem("kit", items.get("kit"));
        setSelectedItem("cage", items.get("cage"));
        setSelectedItem("trail", items.get("trail"));
    }

    public boolean isLoading() {
        return isLoading;
    }

    public Integer getCoin() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void giveCoins(int coins) {
        this.coins += coins;
    }

    public Integer getStat(String statName) {

        if (statName.equals("wins")) {
            return wins;
        }

        if (statName.equals("playedGames")) {
            return playedGames;
        }

        if (statName.equals("kills")) {
            return kills;
        }

        if (statName.equals("deaths")) {
            return deaths;
        }

        if (statName.equals("projectilesHit")) {
            return projectilesHit;
        }

        if (statName.equals("projectilesLaunched")) {
            return projectilesLaunched;
        }

        if (statName.equals("blocksPlaced")) {
            return blocksPlaced;
        }

        if (statName.equals("blocksBroken")) {
            return blocksBroken;
        }

        if (statName.equals("itemsEnchanted")) {
            return itemsEnchanted;
        }

        if (statName.equals("itemsCrafted")) {
            return itemsCrafted;
        }

        if (statName.equals("fishesCaught")) {
            return fishesCaught;
        }

        if (statName.equals("playTime")) {
            return playTime;
        }
        return 0;
    }

    public void giveStat(String statName, Integer amount) {

        if (statName.equals("wins")) {
            wins += amount;
        }

        if (statName.equals("playedGames")) {
            playedGames += amount;
        }

        if (statName.equals("kills")) {
            kills += amount;
        }

        if (statName.equals("deaths")) {
            deaths += amount;
        }

        if (statName.equals("projectilesHit")) {
            projectilesHit += amount;
        }

        if (statName.equals("projectilesLaunched")) {
            projectilesLaunched += amount;
        }

        if (statName.equals("blocksPlaced")) {
            blocksPlaced += amount;
        }

        if (statName.equals("blocksBroken")) {
            blocksBroken += amount;
        }

        if (statName.equals("itemsEnchanted")) {
            itemsEnchanted += amount;
        }

        if (statName.equals("itemsCrafted")) {
            itemsCrafted += amount;
        }

        if (statName.equals("fishesCaught")) {
            fishesCaught += amount;
        }

        if (statName.equals("playTime")) {
            playTime += amount;
        }
    }

    public Arena getArena() {
        return arena;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public String getName() {
        return playerName;
    }

    public boolean hasCooldown(String key, int seconds, boolean sendMessage) {
        long difference = cooldowns.containsKey(key) ? cooldowns.get(key) - System.currentTimeMillis() : 0;
        if (difference > 0) {
            if (sendMessage) {
                getPlayer().sendMessage(plugin.customization.messages.get("Cooldown").replace("%seconds%", String.valueOf(new BigDecimal(Double.valueOf(difference) / 1000).setScale(1, RoundingMode.HALF_UP).doubleValue())));
            }
            return true;
        }
        return false;
    }

    public void addCooldown(String key, int seconds) {
        cooldowns.put(key, System.currentTimeMillis() + (seconds * 1000));
    }

    public void updatePlayTime() {
        playTime += (System.currentTimeMillis() - lastPlayTime) / 1000;
        lastPlayTime = System.currentTimeMillis();
    }

    public BukkitTask getActionBarTask() {
        return actionBarTask;
    }

    public void cancelActionBarTask() {
        actionBarTask.cancel();
    }

    public String getSelectedItem(String itemType) {
        if (itemType.equalsIgnoreCase("cage")) {
            return selectedCage;
        }

        if (itemType.equalsIgnoreCase("kit")) {
            return selectedKit;
        }

        if (itemType.equalsIgnoreCase("trail")) {
            return selectedTrail;
        }
        return null;
    }

    public void setSelectedItem(String itemType, String itemid) {
        if (itemType.equalsIgnoreCase("kit")) {
            selectedKit = itemid;
        }
        if (itemType.equalsIgnoreCase("cage")) {
            selectedCage = itemid;
        }
        if (itemType.equalsIgnoreCase("trail")) {
            selectedTrail = itemid;
        }
    }

    public void selectItem(String itemType, String itemId) {
        setSelectedItem(itemType, itemId);
        plugin.sqlManager.selectItem(playerName, itemId, itemType);
    }

    public long getLastAttackedTime() {
        return lastAttackedTime;
    }

    public void setLastAttackedTime(long time) {
        lastAttackedTime = time;
    }

    public String getLastAttacker() {
        return lastAttacker;
    }

    public void setLastAttacker(String playerName) {
        lastAttacker = playerName;
    }

    @SuppressWarnings("deprecation")
    public ArrayList<SWOnlinePlayer> getEnemies() {
        ArrayList<SWOnlinePlayer> enemies = new ArrayList<SWOnlinePlayer>();
        if (getArena() != null) {
            for (Player player : getArena().getPlayers()) {
                SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(player.getName());
                if (!getArena().getSpectators().contains(swPlayer)
                        && !getArena().getTeam(this).hasPlayer(player)) {
                    enemies.add(swPlayer);
                }
            }
        } else {
            return null;
        }
        return enemies;
    }

    public ArrayList<String> getPlayerKits() {
        return kits;
    }

    public ArrayList<String> getPlayerTrails() {
        return trails;
    }

    public ArrayList<String> getPlayerCages() {
        return cages;
    }

    public boolean canBuild() {
        if (this.arena == null) {
            return false;
        }

        if (!(this.arena.getArenaState().equals(ArenaState.INGAME) || this.arena.getArenaState().equals(ArenaState.HELL))) {
            return false;
        }

        return !this.arena.getSpectators().contains(this);

    }
}
