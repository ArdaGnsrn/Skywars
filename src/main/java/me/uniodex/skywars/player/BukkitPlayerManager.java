package me.uniodex.skywars.player;

import lombok.SneakyThrows;
import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.enums.DefaultFontInfo;
import me.uniodex.skywars.objects.Kit;
import me.uniodex.skywars.utils.Nickname;
import me.uniodex.skywars.utils.SkinChanger;
import me.uniodex.skywars.utils.UUIDFetcher;
import me.uniodex.skywars.utils.Utils;
import me.uniodex.skywars.utils.packages.RandomNameGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skinsrestorer.bukkit.SkinsRestorer;
import skinsrestorer.bukkit.SkinsRestorerBukkitAPI;
import skinsrestorer.bungee.SkinsRestorerBungeeAPI;
import skinsrestorer.shared.exception.SkinRequestException;
import skinsrestorer.shared.utils.SkinsRestorerAPI;

import java.util.HashMap;

public class BukkitPlayerManager {

    private final static int CENTER_PX = 154;
    public HashMap<String, String> disguisedPlayers = new HashMap<String, String>();
    private Skywars plugin;

    public BukkitPlayerManager(Skywars plugin) {
        this.plugin = plugin;
    }

    public void preparePlayer(Player p, String forWhat) {
        if (forWhat.equalsIgnoreCase("join")) {
            for (Player arenaPlayer : plugin.playerManager.getSWOnlinePlayer(p.getName()).getArena().getPlayers()) {
                arenaPlayer.showPlayer(p);
                p.showPlayer(arenaPlayer);
            }

            p.setMaxHealth(20);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setFireTicks(0);
            p.spigot().setCollidesWithEntities(true);
            p.setLevel(0);
            p.setExp(0);
            p.setGameMode(GameMode.SURVIVAL);
            p.setAllowFlight(false);
            p.setFlying(false);
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            if (p.getVehicle() != null) {
                p.getVehicle().eject();
            }

            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    p.getInventory().setItem(0, plugin.clickableItemManager.getSelectKitItem());
                    p.getInventory().setItem(1, plugin.clickableItemManager.getSelectCageItem());
                    p.getInventory().setItem(2, plugin.clickableItemManager.getVoteItem());
                    p.getInventory().setItem(8, plugin.clickableItemManager.getQuitItem());
                }
            }, 1L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    p.updateInventory();
                }
            }, 2L);
        }

        if (forWhat.equalsIgnoreCase("start")) {
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            p.setMaxHealth(20);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setFireTicks(0);
            p.spigot().setCollidesWithEntities(true);
            p.setLevel(0);
            p.setExp(0);
            p.setGameMode(GameMode.SURVIVAL);
            p.setAllowFlight(false);
            p.setFlying(false);
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            if (p.getVehicle() != null) {
                p.getVehicle().eject();
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    p.updateInventory();
                }
            }, 1L);

            for (Player arenaPlayer : plugin.playerManager.getSWOnlinePlayer(p.getName()).getArena().getPlayers()) {
                arenaPlayer.showPlayer(p);
                p.showPlayer(arenaPlayer);
            }
        }

        if (forWhat.equalsIgnoreCase("spectating")) {
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 3, true));
            p.setMaxHealth(20);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setFireTicks(0);
            p.setLevel(0);
            p.setExp(0);
            p.setAllowFlight(true);
            p.setFlying(true);
            p.spigot().setCollidesWithEntities(false);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.hidePlayer(p);
            }
            p.getInventory().setArmorContents(null);
            p.getInventory().clear();

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    p.getInventory().setItem(0, plugin.clickableItemManager.getTeleporterItem());
                    p.getInventory().setItem(1, plugin.clickableItemManager.getStatsItem(p.getName()));
                    p.getInventory().setItem(7, plugin.clickableItemManager.getNewGameItem());
                    p.getInventory().setItem(8, plugin.clickableItemManager.getQuitItem());
                }
            }, 1L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    p.updateInventory();
                }
            }, 2L);
        }

        if (forWhat.equalsIgnoreCase("quit")) {
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            p.setMaxHealth(20);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setFireTicks(0);
            p.spigot().setCollidesWithEntities(true);
            p.setLevel(0);
            p.setExp(0);
            p.setGameMode(GameMode.SURVIVAL);
            p.setAllowFlight(false);
            p.setFlying(false);
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            p.updateInventory();

            if (p != null) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    p.hidePlayer(onlinePlayer);
                    onlinePlayer.hidePlayer(p);
                }
            }
        }

        if (forWhat.equalsIgnoreCase("joinserver")) {
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    p.updateInventory();
                }
            }, 1L);
            p.setMaxHealth(20);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setFireTicks(0);
            p.setLevel(0);
            p.setExp(0);
            p.setAllowFlight(false);
            p.setFlying(false);
            p.spigot().setCollidesWithEntities(false);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.hidePlayer(p);
                p.hidePlayer(onlinePlayer);
            }
        }
    }

    public void giveKit(Player p, Kit kit) {
        for (ItemStack item : kit.items) {
            if (Utils.isHelmet(item) && p.getInventory().getHelmet() == null) {
                p.getInventory().setHelmet(item);
            } else if (Utils.isChestplate(item) && p.getInventory().getChestplate() == null) {
                p.getInventory().setChestplate(item);
            } else if (Utils.isLeggings(item) && p.getInventory().getLeggings() == null) {
                p.getInventory().setLeggings(item);
            } else if (Utils.isBoots(item) && p.getInventory().getBoots() == null) {
                p.getInventory().setBoots(item);
            } else {
                p.getInventory().addItem(item);
            }
        }
        p.updateInventory();
    }

    public String getDeathMessage(String p, String killer) {
        String deathMessage = "";
        if (killer != null) {
            if (killer.equals(p)) {
                deathMessage = plugin.customization.player_suicide.replace("%player%", disguisedPlayers.containsKey(p) ? disguisedPlayers.get(p) : p);
            } else {
                //TODO Recode just next line
                deathMessage = plugin.customization.killMessages.get(plugin.r.nextInt(plugin.customization.killMessages.size())).replace("%player%", disguisedPlayers.containsKey(p) ? disguisedPlayers.get(p) : p).replace("%killer%", disguisedPlayers.containsKey(killer) ? disguisedPlayers.get(killer) : killer);
            }
        } else {
            Player player = Bukkit.getPlayer(p);
            if (player.getLastDamageCause() != null && plugin.customization.deathMessages.containsKey(player.getLastDamageCause().getCause().name())) {
                deathMessage = plugin.customization.deathMessages.get(player.getLastDamageCause().getCause().name()).replace("%player%", disguisedPlayers.containsKey(p) ? disguisedPlayers.get(p) : p);
            } else {
                deathMessage = plugin.customization.deathMessages.get("UNKNOWN").replace("%player%", disguisedPlayers.containsKey(p) ? disguisedPlayers.get(p) : p);
            }
        }
        return deathMessage;
    }

    public void sendCenteredMessage(Player player, String message) {
        if (message == null || message.equals("")) player.sendMessage("");
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            } else if (previousCode == true) {
                previousCode = false;
                if (c == 'l' || c == 'L') {
                    isBold = true;
                    continue;
                } else isBold = false;
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(sb.toString() + message);
    }

    public void disguisePlayer(Player player, String displayName) {
        if (displayName == null) {
            displayName = RandomNameGenerator.generateName();
            plugin.sqlManager.updateSQL("INSERT INTO `" + plugin.sqlManager.database + "`.`usw_disguise` (`player`, `displayName`) VALUES ('" + player.getName() + "', '" + displayName + "');");
        }
        disguisedPlayers.put(player.getName(), displayName);
        player.setDisplayName(ChatColor.DARK_AQUA + displayName);
        String finalDisplayName = displayName;
        SkinChanger.nick(player, new Nickname(UUIDFetcher.getUUID(displayName), ChatColor.GRAY, displayName));
    }

    public void undisguisePlayer(Player p, boolean delete) {
        if (p == null || !disguisedPlayers.containsKey(p.getName())) {
            return;
        }
        if (delete) {
            plugin.sqlManager.updateSQL("DELETE FROM `" + plugin.sqlManager.database + "`.`usw_disguise` WHERE `player` = '" + p.getName() + "';");
        }
        disguisedPlayers.remove(p.getName());
        if (SkinChanger.isNicked(p)) { SkinChanger.unNick(p); }
        p.setDisplayName(p.getName());
        p.setPlayerListName(p.getName());

        //plugin.sqlManager.addPlayerToResetList(p.getName());
    }
}
