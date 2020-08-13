package me.uniodex.skywars.listeners;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.arena.Arena;
import me.uniodex.skywars.player.SWOnlinePlayer;
import me.uniodex.skywars.utils.ItemStackBuilder;
import me.uniodex.skywars.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("deprecation")
public class ClickableItemListeners implements Listener {

    private Skywars plugin;

    public ClickableItemListeners(Skywars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());
        if (plugin.selectionMode.containsKey(p.getName()) && Utils.compareItem(p.getItemInHand(), plugin.clickableItemManager.getWandItem())) {
            e.setCancelled(true);
            int id = e.getAction().equals(Action.LEFT_CLICK_BLOCK) ? 0 : e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? 1 : 2;
            if (id == 2) return;
            Location l = e.getClickedBlock().getLocation();
            plugin.selectionMode.get(p.getName())[id] = l;
            p.sendMessage(plugin.customization.prefix + "You have set the " + ChatColor.LIGHT_PURPLE + "#" + (id + 1) + ChatColor.GRAY + " corner at " + Utils.getReadableLocationString(l, false));
            return;
        }

        if (p.getItemInHand() != null && p.getItemInHand().getType().equals(plugin.clickableItemManager.getChestToolItem().getType()) && p.getItemInHand().getItemMeta().hasDisplayName() && p.getItemInHand().getItemMeta().getDisplayName().equals(plugin.clickableItemManager.getChestToolItem().getItemMeta().getDisplayName())) {
            if (p.hasPermission("skywars.chestmanager")) {
                e.setCancelled(true);
                if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    Block clicked = e.getClickedBlock();
                    if (clicked.getType().equals(Material.CHEST)) {

                        Location l = clicked.getLocation();
                        for (Arena arena : plugin.arenaManager.getArenas().values()) {
                            HashMap<Location, String> chests = arena.getChests();
                            for (Location x : chests.keySet()) {
                                if (x.equals(l)) {
                                    if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                                        String type = ChatColor.stripColor(p.getItemInHand().getItemMeta().getLore().get(p.getItemInHand().getItemMeta().getLore().size() - 2).split(": ")[1]);
                                        String spawnpoint = ChatColor.stripColor(p.getItemInHand().getItemMeta().getLore().get(p.getItemInHand().getItemMeta().getLore().size() - 1).split(": ")[1]);
                                        if (type.equalsIgnoreCase("Default") || type.equalsIgnoreCase("Mid")) {
                                            long before = System.currentTimeMillis();
                                            chests.put(x, type.toLowerCase());
                                            arena.setChests(chests);
                                            File file = new File(plugin.getDataFolder() + "/arenas/" + arena.getArenaName(), "locations.dat");
                                            FileConfiguration editor = YamlConfiguration.loadConfiguration(file);
                                            ArrayList<String> locations = new ArrayList<String>();
                                            for (Location loc : chests.keySet()) {
                                                locations.add(loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ":" + loc.getBlock().getTypeId() + ":" + loc.getBlock().getData() + ":" + chests.get(loc) + ":" + spawnpoint);
                                            }
                                            editor.set("Chests", locations.toString());
                                            try {
                                                editor.save(file);
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            }
                                            p.sendMessage(plugin.customization.prefix + "You have changed this chest type to " + ChatColor.AQUA + type + ChatColor.GRAY + "! took " + ChatColor.LIGHT_PURPLE + (System.currentTimeMillis() - before) + "ms");
                                        } else {
                                            p.sendMessage(plugin.customization.prefix + "You had an invalid " + plugin.clickableItemManager.getChestToolItem().getItemMeta().getDisplayName());
                                            p.setItemInHand(new ItemStack(Material.AIR));
                                        }
                                    } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                                        String type = chests.get(x);
                                        p.sendMessage(plugin.customization.prefix + "Arena: " + ChatColor.LIGHT_PURPLE + arena.getArenaName() + ChatColor.GRAY + " - Chest type: " + ChatColor.AQUA + type);
                                    }

                                    return;
                                }
                            }
                        }

                        p.sendMessage(plugin.customization.prefix + "Couldn't find this chest in any of the arenas!");

                    } else p.sendMessage(plugin.customization.prefix + "You can only use this tool on chests!");
                }
            } else p.setItemInHand(new ItemStack(Material.AIR));
            return;
        }

        if (plugin.playerManager.getOnlinePlayers().containsKey(p.getName())) {
            if (Utils.compareItem(p.getItemInHand(), plugin.clickableItemManager.getQuitItem())) {
                e.setCancelled(true);
                p.openInventory(plugin.menuManager.getQuitInventory());
                return;
            }

            if (p.getItemInHand() != null && p.getItemInHand().getItemMeta() != null && p.getItemInHand().getItemMeta().getDisplayName() != null) {
                if (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(plugin.clickableItemManager.getStatsItem(p.getName()).getItemMeta().getDisplayName())) {
                    if (swPlayer.getArena() != null && swPlayer.getArena().getSpectators().contains(swPlayer)) {
                        e.setCancelled(true);
                        p.openInventory(plugin.menuManager.getStatsInventory(swPlayer.getSWOfflinePlayer()));
                    }
                    return;
                }
            }

            if (Utils.compareItem(p.getItemInHand(), plugin.clickableItemManager.getNewGameItem())) {
                e.setCancelled(true);
                p.performCommand("yenioyunagir");
                return;
            }

            if (Utils.compareItem(p.getItemInHand(), plugin.clickableItemManager.getSelectKitItem())) {
                e.setCancelled(true);
                if (swPlayer.getArena() != null && swPlayer.getArena().getArenaState().isAvailable()) {
                    p.openInventory(plugin.menuManager.getKitInventory(swPlayer, swPlayer.getArena().getArenaMode().toString()));
                }
                return;
            }
            if (Utils.compareItem(p.getItemInHand(), plugin.clickableItemManager.getSelectCageItem())) {
                e.setCancelled(true);
                if (swPlayer.getArena() != null && swPlayer.getArena().getArenaState().isAvailable()) {
                    p.openInventory(plugin.menuManager.getCageInventory(swPlayer, swPlayer.getArena().getArenaMode().toString()));
                }
                return;
            }

            if (Utils.compareItem(p.getItemInHand(), plugin.clickableItemManager.getVoteItem())) {
                e.setCancelled(true);
                if (swPlayer.getArena() != null && swPlayer.getArena().getArenaState().isAvailable()) {
                    if (!p.hasPermission("skywars.vote.chests")) {
                        p.sendMessage(plugin.customization.messages.get("No-Permission"));
                    } else {
                        swPlayer.getArena().getVoteManager().openEntry(p, "Chests");
                    }
                }
                return;
            }

            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                if (Utils.compareItem(p.getItemInHand(), plugin.clickableItemManager.getTeleporterItem())) {
                    e.setCancelled(true);
                    if (swPlayer.getArena() != null && swPlayer.getArena().getSpectators().contains(swPlayer)) {
                        if (swPlayer.hasCooldown("TELEPORT_GUI", 1, true)) {
                            return;
                        }
                        swPlayer.addCooldown("TELEPORT_GUI", 1);
                        p.openInventory(plugin.menuManager.getSpectatorMenu(swPlayer.getArena()));
                    }
                    return;
                }
            }

            if (p.getItemInHand().getType().equals(Material.COMPASS)) {
                e.setCancelled(true);
                Arena arena = swPlayer.getArena();

                if (arena != null && arena.getPlayers().contains(p) && !arena.getSpectators().contains(swPlayer)) {
                    if (swPlayer.hasCooldown("COMPASS_TRACK", 5, true)) {
                        return;
                    }
                    swPlayer.addCooldown("COMPASS_TRACK", 5);
                    int distance = Integer.MAX_VALUE;
                    Player nearest = null;
                    for (SWOnlinePlayer swEnemy : swPlayer.getEnemies()) {
                        Player swEnemyPlayer = swEnemy.getPlayer();
                        if (!p.getWorld().getName().equals(swEnemyPlayer.getWorld().getName())) {
                            continue;
                        }
                        double current_distance = swEnemyPlayer.getLocation().distance(p.getLocation());
                        if (current_distance < distance) {
                            distance = (int) current_distance;
                            nearest = swEnemyPlayer;
                        }
                    }
                    if (nearest != null) {
                        String nearestName = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(nearest.getName()) ? plugin.bukkitPlayerManager.disguisedPlayers.get(nearest.getName()) : nearest.getName();
                        new ItemStackBuilder(p.getItemInHand()).setName(ChatColor.BOLD + "İzleniyor: " + ChatColor.RED + nearestName + ChatColor.WHITE + ChatColor.BOLD + " - Mesafe: " + ChatColor.RED + distance + ".0").build();
                        p.setCompassTarget(nearest.getLocation());
                        p.sendMessage(plugin.customization.messages.get("Tracking-Player").replace("%target%", nearestName));
                    } else {
                        new ItemStackBuilder(p.getItemInHand()).setName(ChatColor.RED + "Yakınında oyuncu yok!").build();
                    }
                }
                return;
            }
        }

    }
}
