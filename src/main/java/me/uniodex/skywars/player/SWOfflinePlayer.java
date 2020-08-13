package me.uniodex.skywars.player;

import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.spigot.api.party.PartyManager;
import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;
import me.uniodex.skywars.Skywars;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class SWOfflinePlayer {

    // NOT: Burada herhangi bir veri saklama çünkü burası tek kullanımlık bir yer.

    private Skywars plugin;
    private OfflinePlayer player;
    private String playerName;

    @SuppressWarnings("deprecation")
    public SWOfflinePlayer(Skywars plugin, String playerName) {
        this.player = Bukkit.getOfflinePlayer(playerName);
        this.playerName = playerName;
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    public OfflinePlayer getPlayer() {
        if (player != null) {
            return player;
        } else {
            return Bukkit.getOfflinePlayer(playerName);
        }
    }

    public String getName() {
        return playerName;
    }

    public SWOnlinePlayer getSWOnlinePlayer() {
        if (getPlayer().isOnline()) {
            return plugin.playerManager.getSWOnlinePlayer(playerName);
        } else {
            return null;
        }
    }

    public int getCoinsPerKill() {
        if (plugin.permission.playerHas("world", this.getPlayer(), "rank.vip")) {
            return plugin.config.coinsPerKill * 2;
        } else {
            return plugin.config.coinsPerKill;
        }
    }

    public PlayerParty getParty() {
        PAFPlayer pafPlayer = PAFPlayerManager.getInstance().getPlayer(this.getName());
        if (pafPlayer == null) {
            return null;
        }
        PlayerParty party = null;
        if (!(PartyManager.getInstance().getParty(pafPlayer) == null)) {
             party = PartyManager.getInstance().getParty(pafPlayer);
        }
        return party;
    }
}
