package me.uniodex.skywars.player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;
import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.arena.Arena;
import me.uniodex.skywars.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import skinsrestorer.shared.exception.SkinRequestException;

import java.util.ArrayList;
import java.util.HashMap;

public class SWPlayerManager {

    public ArrayList<String> editMode = new ArrayList<String>();
    private Skywars plugin;
    private HashMap<String, SWOnlinePlayer> onlinePlayers = new HashMap<String, SWOnlinePlayer>();

    public SWPlayerManager(Skywars plugin) {
        this.plugin = plugin;
    }

    public HashMap<String, SWOnlinePlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    public SWOfflinePlayer getSWPlayer(String playerName) {
        SWOfflinePlayer player = new SWOfflinePlayer(plugin, playerName);
        return player;
    }
    public SWOnlinePlayer getSWOnlinePlayer(Player player) { return onlinePlayers.get(player.getName()); }
    public SWOnlinePlayer getSWOnlinePlayer(String playerName) { return onlinePlayers.get(playerName); }

    public void onSWPlayerJoin(Player player) {
        onlinePlayers.put(player.getName(), new SWOnlinePlayer(plugin, player));
        String disguiseName = plugin.sqlManager.getPlayerDisguise(player.getName());
        if (disguiseName != null) {
            plugin.bukkitPlayerManager.disguisePlayer(player, disguiseName);
        }

        SWOfflinePlayer offlinePlayer = plugin.playerManager.getSWPlayer(player.getName());
        /*PlayerParty party = offlinePlayer.getParty();
        // Partisi varsa ve kişi parti lideriyse
        if (party != null && party.getLeader().getName().equalsIgnoreCase(player.getName())) {
            sendPlayerToANewGame(getSWOnlinePlayer(player.getName()));
            return;
        }

        // Partisi yoksa ya da parti lideri oyunda değilse
        if (party == null || (!onlinePlayers.containsKey(party.getLeader().getName()))) {*/
            sendPlayerToANewGame(getSWOnlinePlayer(player.getName()));
            //return;
        /*}

        String playerName = player.getName();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                if (plugin.playerManager.getSWOnlinePlayer(playerName) != null && plugin.playerManager.getSWOnlinePlayer(playerName).getArena() == null) {
                    // Partisi varsa, kişi parti lideri değilse ve parti lideri de oyundaysa
                    if (party != null && (!party.getLeader().getName().equalsIgnoreCase(player.getName())) && onlinePlayers.containsKey(party.getLeader().getName())) {
                        sendPlayerToANewGame(getSWOnlinePlayer(player.getName()));
                    }
                }
            }
        }, 20L);*/
    }

    public void leave(String playerName) {
        SWOnlinePlayer swPlayer = onlinePlayers.get(playerName);

        if (swPlayer.getActionBarTask() != null) {
            swPlayer.cancelActionBarTask();
        }

        if (swPlayer.getArena() != null) {
            swPlayer.getArena().quitPlayer(playerName);
        }

        if (!swPlayer.isLoading()) {
            swPlayer.saveData(true, false);
        }

        onlinePlayers.remove(playerName);

        plugin.updateScoreboardTitleTask();

        if (plugin.serverWillStop && !plugin.arenaManager.isTherePlayingArenas()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(Utils.getRandomServer(plugin.config.gameMode));
                p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                Bukkit.shutdown();
            }
        }
    }

    public void sendPlayerToANewGame(SWOnlinePlayer p) {
        if (p.isLoading()) {

            final String playerName = p.getName();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    SWOnlinePlayer online = getSWOnlinePlayer(playerName);
                    if (online != null) {
                        sendPlayerToANewGame(online);
                    }
                }
            }, 3L);
            return;
        }
        SWOfflinePlayer offlinePlayer = p.getSWOfflinePlayer();
        /*PlayerParty party = offlinePlayer.getParty();
        ArrayList<SWOnlinePlayer> players = new ArrayList<SWOnlinePlayer>();*/
        Arena arena = null;
        /*// Parti yok mu
        if (party == null) {
            players.add(p);*/
            arena = plugin.arenaManager.getAvailableArena(1);
         /*   // Oyuncu parti lideri değil mi
        } else if (!party.getLeader().getName().equals(offlinePlayer.getName())) {
            players.add(p);
            SWOnlinePlayer partyLeader = plugin.playerManager.getSWOnlinePlayer(party.getLeader().getName());
            if (partyLeader != null && partyLeader.getArena() != null && partyLeader.getArena().isJoinable()) {
                arena = partyLeader.getArena();
            } else if (partyLeader == null) {
                for (PAFPlayer pafpartyMember : party.getAllPlayers()) {
                    SWOnlinePlayer partyMember = this.getSWOnlinePlayer(pafpartyMember.getName());
                    if (partyMember != null && partyMember.getArena() != null && partyMember.getArena().isJoinable()) {
                        arena = partyMember.getArena();
                        break;
                    }
                }
            }

            // Hala arena bulunamadyısa
            if (arena == null) {
                arena = plugin.arenaManager.getAvailableArena(1);
            }
            // Oyuncu parti lideri mi
        } else {
            for (PAFPlayer pafpartyMember : party.getAllPlayers()) {
                String partyMember = pafpartyMember.getName();
                SWOnlinePlayer onlinePartyMember = plugin.playerManager.getSWOnlinePlayer(partyMember);
                if (onlinePartyMember != null) {
                    players.add(onlinePartyMember);
                }
            }
            arena = plugin.arenaManager.getAvailableArena(players.size());
        }*/

        final Arena selectedArena = arena;

       /* if (players.size() < 1) {
            return;
        }*/

        if (plugin.serverWillStop) {
            arena = null;
        }

        /*if (selectedArena == null) {
            for (SWOnlinePlayer swPlayer : players) {
                Player bukkitPlayer = swPlayer.getPlayer();
                final String playerName = swPlayer.getName();
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(Utils.getRandomServer(plugin.config.gameMode));
                bukkitPlayer.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        Player p = Bukkit.getPlayer(playerName);
                        if (p != null) {
                            p.kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "Müsait arena olmadığı için lobiye yönlendirildiniz!");
                        }
                    }
                }, 5L);
            }
            return;
        }*/

       // if (party == null) {
            if (p.getArena() != null) {
                p.getArena().quitPlayer(p.getName());
            }

            final String playerName = p.getName();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    SWOnlinePlayer online = plugin.playerManager.getSWOnlinePlayer(playerName);
                    if (online != null) {
                        selectedArena.joinPlayer(online, false);
                    }
                }
            }, 3L);
      //  }
/*
        if (party.getLeader().getName().equals(offlinePlayer.getName())) {
            for (SWOnlinePlayer partyMember : players) {
                if (partyMember == null) {
                    continue;
                }

                if (partyMember.getArena() != null) {
                    partyMember.getArena().quitPlayer(partyMember.getName());
                }

                final String playerName = partyMember.getName();
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        SWOnlinePlayer online = plugin.playerManager.getSWOnlinePlayer(playerName);
                        if (online != null) {
                            selectedArena.joinPlayer(online, false);
                        }
                    }
                }, 3L);
            }
        } else {
            if (p.getArena() != null) {
                p.getArena().quitPlayer(p.getName());
            }

            final String playerName = p.getName();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    SWOnlinePlayer online = plugin.playerManager.getSWOnlinePlayer(playerName);
                    if (online != null) {
                        selectedArena.joinPlayer(online, false);
                    }
                }
            }, 3L);
        }*/
    }

    public int getCoin(String player) {
        if (this.onlinePlayers.containsKey(player)) {
            return this.getSWOnlinePlayer(player).getCoin();
        } else {
            return plugin.sqlManager.getPlayerCoins(player);
        }
    }

    public int getCoin(SWOfflinePlayer player) {
        if (this.onlinePlayers.containsKey(player.getName())) {
            return this.getSWOnlinePlayer(player.getName()).getCoin();
        } else {
            return plugin.sqlManager.getPlayerCoins(player.getName());
        }
    }

    public int getCoin(Player player) {
        if (this.onlinePlayers.containsKey(player.getName())) {
            return this.getSWOnlinePlayer(player.getName()).getCoin();
        } else {
            return plugin.sqlManager.getPlayerCoins(player.getName());
        }
    }

    public int getCoin(SWOnlinePlayer player) {
        return player.getCoin();
    }

    public int getStat(String player, String statName) {
        if (this.onlinePlayers.containsKey(player)) {
            return getStat(onlinePlayers.get(player), statName);
        } else {
            return plugin.sqlManager.getPlayerStat(player, statName, false);
        }
    }

    public int getStat(SWOfflinePlayer player, String statName) {
        if (this.onlinePlayers.containsKey(player.getName())) {
            return getStat(onlinePlayers.get(player.getName()), statName);
        } else {
            return plugin.sqlManager.getPlayerStat(player.getName(), statName, false);
        }
    }

    public int getStat(Player player, String statName) {
        if (this.onlinePlayers.containsKey(player.getName())) {
            return getStat(onlinePlayers.get(player.getName()), statName);
        } else {
            return plugin.sqlManager.getPlayerStat(player.getName(), statName, false);
        }
    }

    public int getStat(SWOnlinePlayer player, String statName) {
        return player.getStat(statName);
    }

    public void giveStat(Player player, String statName, Integer amount) {
        if (this.onlinePlayers.containsKey(player.getName())) {
            giveStat(onlinePlayers.get(player.getName()), statName, amount);
        } else {
            plugin.sqlManager.giveStats(player.getName(), statName, amount);
        }
    }

    public void giveStat(SWOnlinePlayer player, String statName, Integer amount) {
        player.giveStat(statName, amount);
    }

    public void giveStat(String player, String statName, Integer amount) {
        if (this.onlinePlayers.containsKey(player)) {
            giveStat(onlinePlayers.get(player), statName, amount);
        } else {
            plugin.sqlManager.giveStats(player, statName, amount);
        }
    }

    public void giveStat(SWOfflinePlayer player, String statName, Integer amount) {
        if (this.onlinePlayers.containsKey(player.getName())) {
            giveStat(onlinePlayers.get(player.getName()), statName, amount);
        } else {
            plugin.sqlManager.giveStats(player.getName(), statName, amount);
        }
    }

    public void giveCoin(Player player, Integer amount) {
        if (this.onlinePlayers.containsKey(player.getName())) {
            giveCoin(onlinePlayers.get(player.getName()), amount);
        } else {
            plugin.sqlManager.giveCoins(player.getName(), amount);
        }
    }

    public void giveCoin(SWOnlinePlayer player, Integer amount) {
        player.giveCoins(amount);
    }

    public void giveCoin(String player, Integer amount) {
        if (this.onlinePlayers.containsKey(player)) {
            giveCoin(onlinePlayers.get(player), amount);
        } else {
            plugin.sqlManager.giveCoins(player, amount);
        }
    }

    public void giveCoin(SWOfflinePlayer player, Integer amount) {
        if (this.onlinePlayers.containsKey(player.getName())) {
            giveCoin(onlinePlayers.get(player.getName()), amount);
        } else {
            plugin.sqlManager.giveCoins(player.getName(), amount);
        }
    }
}
