package me.uniodex.skywars.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.arena.Arena;
import me.uniodex.skywars.enums.ArenaMode;
import me.uniodex.skywars.enums.ArenaState;
import me.uniodex.skywars.enums.SpecialCharacter;
import me.uniodex.skywars.objects.Trail;
import me.uniodex.skywars.player.SWOfflinePlayer;
import me.uniodex.skywars.player.SWOnlinePlayer;
import me.uniodex.skywars.utils.Utils;
import me.uniodex.skywars.utils.packages.titleapi.TitleAPI;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import javax.rmi.CORBA.Util;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class CoreListeners implements Listener {

    public static ArrayList<EnchantingInventory> enchantInventories;
    private Skywars plugin;
    private ItemStack lapis;

    public CoreListeners(Skywars plugin) {
        this.plugin = plugin;
        Dye d = new Dye();
        d.setColor(DyeColor.BLUE);
        this.lapis = d.toItemStack();
        this.lapis.setAmount(64);
        enchantInventories = new ArrayList<EnchantingInventory>();
    }

    /*
     *
     * MAIN GAMEPLAY EVENTS
     *
     */

    public static void onDisable() {
        for (EnchantingInventory ei : enchantInventories) {
            ei.setItem(1, null);
        }
        enchantInventories = null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        final Player p = event.getPlayer();
        plugin.bukkitPlayerManager.preparePlayer(p, "joinserver");
        p.teleport(plugin.lobbyLocation);
        plugin.playerManager.onSWPlayerJoin(p);
        plugin.updateScoreboardTitleTask();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                if (p.getWorld().getName().equalsIgnoreCase("world")) {
                    if (p.getName().equals("UnioDex")) return;
                    p.kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "Giriş esnasında bir sorun yaşandığı için lobiye yönlendirildiniz. Lütfen tekrar giriş yapınız.");
                }
            }
        }, 25L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player p = event.getPlayer();
        plugin.bukkitPlayerManager.undisguisePlayer(p, false);
        if (plugin.playerManager.editMode.contains(p.getName())) {
            plugin.playerManager.editMode.remove(p.getName());
            return;
        }
        plugin.playerManager.leave(p.getName());

        //MOTD GÜNCELLE
        plugin.arenaManager.getAvailableArena(1);

        if (plugin.serverWillStop && !plugin.arenaManager.isTherePlayingArenas()) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(Utils.getRandomServer(plugin.config.gameMode));
                pl.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                Bukkit.shutdown();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoginAsync(final AsyncPlayerPreLoginEvent event) {
        if (plugin.serverWillStop && !plugin.arenaManager.isTherePlayingArenas()) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(Utils.getRandomServer(plugin.config.gameMode));
                pl.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                Bukkit.shutdown();
            }
        }

        if (plugin.arenaManager.getAvailableArena(1) == null) {
            if (event.getName().equals("ArdaGnsrn")) {
                return;
            }
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§c§lMaalesef müsait arena yok. Lütfen giriş yapabilmek için bir süre bekleyiniz.");
        }
    }

    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractEntityEvent event) {
        Player p = event.getPlayer();
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());

        // Right click a player to see its inventory when clicker is a spectator.
        if (event.getRightClicked().getType().equals(EntityType.PLAYER)) {
            if (swPlayer.getArena() != null && swPlayer.getArena().getSpectators().contains(swPlayer)) {
                Player rightClickedPlayer = (Player) event.getRightClicked();
                SWOnlinePlayer swRightClickedPlayer = plugin.playerManager.getSWOnlinePlayer(rightClickedPlayer.getName());
                if (plugin.config.allowSpectatorsViewInventory) {
                    if (!swRightClickedPlayer.getArena().getSpectators().contains(swRightClickedPlayer)) {
                        p.openInventory(rightClickedPlayer.getInventory());
                    }
                }
            }
            return;
        }

        // Right click a zombie or skeleton with an item to give the item to it.
        if (p.isSneaking() && swPlayer.getArena() != null && !swPlayer.getArena().isAvailable() && !swPlayer.getArena().getSpectators().contains(swPlayer)) {
            Entity entity = event.getRightClicked();
            if (entity instanceof Creature) {
                if (entity.getType().equals(EntityType.ZOMBIE) || entity.getType().equals(EntityType.SKELETON)) {
                    String entityOwner = "";
                    if (entity.getCustomName() != null) {
                        entityOwner = ChatColor.stripColor(entity.getCustomName().split("'in")[0]);
                    }

                    if (entity.hasMetadata("SW_MOB") && entityOwner.equalsIgnoreCase(p.getName())) {
                        ItemStack itemInHand = p.getItemInHand();
                        EntityEquipment equipment = ((LivingEntity) entity).getEquipment();
                        if (itemInHand != null && !itemInHand.getType().equals(Material.AIR)) {
                            event.setCancelled(true);
                            if (itemInHand.getType().name().contains("HELMET")) {
                                p.setItemInHand(equipment.getHelmet());
                                equipment.setHelmet(itemInHand);
                            } else if (itemInHand.getType().name().contains("CHESTPLATE")) {
                                p.setItemInHand(equipment.getChestplate());
                                equipment.setChestplate(itemInHand);
                            } else if (itemInHand.getType().name().contains("LEGGINGS")) {
                                p.setItemInHand(equipment.getLeggings());
                                equipment.setLeggings(itemInHand);
                            } else if (itemInHand.getType().name().contains("BOOTS")) {
                                p.setItemInHand(equipment.getBoots());
                                equipment.setBoots(itemInHand);
                            } else {
                                p.setItemInHand(equipment.getItemInHand());
                                equipment.setItemInHand(itemInHand);
                            }
                            p.updateInventory();
                        }
                    }
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());
        String message = event.getMessage().replaceAll("%", "%%");
        String name = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(p.getName()) ? plugin.bukkitPlayerManager.disguisedPlayers.get(p.getName()) : p.getName();
        String playerName = p.getDisplayName().replace(p.getName(), name);
        String prefix = plugin.chat.getPlayerPrefix(p);
        if (plugin.bukkitPlayerManager.disguisedPlayers.containsKey(p.getName())) {
            prefix = ChatColor.DARK_AQUA + "";
        }
        Arena arena = swPlayer.getArena();

        if (arena == null) {
            event.setCancelled(true);
        } else {
            if (!(arena.getArenaState() == ArenaState.ENDING) && arena.getSpectators().contains(swPlayer)) {
                if (plugin.config.spectatorsChat) {
                    event.setFormat("§7[İZLEYİCİ] §f| " + Utils.c(prefix + playerName) + " §3-> §f" + message);
                    event.getRecipients().clear();
                    for (Player spectator : Utils.getPlayers(arena.getSpectators())) {
                        event.getRecipients().add(spectator);
                    }
                }
            } else {
                if (plugin.config.inGameChat) {
                    event.setFormat(Utils.c(prefix + playerName) + " §3-> §f" + message);
                    event.getRecipients().clear();
                    for (Player arenaPlayer : arena.getPlayers()) {
                        event.getRecipients().add(arenaPlayer);
                    }
                }
            }

        }

    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player damager = null;

        // Damager blaze'mi? Blaze ise damager = blaze sahibi
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Blaze) {
            Blaze b = (Blaze) ((Projectile) event.getDamager()).getShooter();
            if (b.hasMetadata("SW_MOB")) {
                LivingEntity entity = b;
                String owner = ChatColor.stripColor(entity.getCustomName().split("'in")[0]);
                damager = Bukkit.getPlayer(owner);
                if (damager != null) {
                    if (damager.equals(event.getEntity())) {
                        return;
                    }
                }
            }
        }

        // Damager player mi?
        if (event.getDamager().getType().equals(EntityType.PLAYER)) {
            damager = (Player) event.getDamager();
            SWOnlinePlayer damagerData = plugin.playerManager.getSWOnlinePlayer(damager.getName());

            if (damagerData.getArena().getSpectators().contains(damagerData)) {
                return;
            }
        }

        // Victim playersa ya da damager mobsa
        if (event.getEntityType().equals(EntityType.PLAYER) || event.getEntity().hasMetadata("SW_MOB")) {
            if (damager == null) { //Damager hala nullsa
                // Damager oksa ve okun sahibi oyuncuysa
                if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    damager = (Player) ((Projectile) event.getDamager()).getShooter();
                    // Damager mobsa
                } else if (event.getDamager().hasMetadata("SW_MOB")) {
                    LivingEntity entity = (LivingEntity) event.getDamager();
                    String owner = ChatColor.stripColor(entity.getCustomName().split("'in")[0]);
                    damager = Bukkit.getPlayer(owner);
                }
            }

            // Victim player mı?
            if (event.getEntityType().equals(EntityType.PLAYER)) {
                Player victim = (Player) event.getEntity();
                SWOnlinePlayer victimData = plugin.playerManager.getSWOnlinePlayer(victim.getName());
                if (victimData == null) {
                    return;
                }

                Arena arena = victimData.getArena();
                if (arena == null) {
                    return;
                }

                if (arena.getSpectators().contains(victimData)) {
                    return;
                }

                if (damager == null) {
                    return;
                }

                if (arena.getPlayers().contains(damager)) {
                    if (event.getDamager() instanceof Projectile) {
                        SWOnlinePlayer damagerData = plugin.playerManager.getSWOnlinePlayer(damager.getName());
                        if (damagerData == null) {
                            return;
                        }
                        if (event.getDamager().getType().equals(EntityType.ARROW) && plugin.config.showHealthOnBowHit) {
                            final Player finalDamager = damager;
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                public void run() {
                                    if (!arena.getSpectators().contains(victimData)) {
                                        finalDamager.sendMessage(plugin.customization.messages.get("Arrow-Hit").replace("%player%", victim.getDisplayName()).replace("%health%", new BigDecimal(victim.getHealth()).setScale(1, RoundingMode.HALF_UP).toString()).replace("%heart%", SpecialCharacter.HEART.toString()));
                                    }
                                }
                            }, 1);
                        }
                    }
                    victimData.setLastAttacker(damager.getName());
                    victimData.setLastAttackedTime(System.currentTimeMillis());
                }
            }
        }

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType().equals(EntityType.PLAYER)) {
            Player player = (Player) event.getEntity();
            SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(player.getName());

            if (event.getCause().equals(DamageCause.VOID) && plugin.config.voidInstantKill) {
                if (swPlayer != null && swPlayer.getArena() != null && !swPlayer.getArena().isAvailable() && !swPlayer.getArena().getSpectators().contains(swPlayer)) {
                    event.setDamage(1000.0);
                }

            } else if (event.getCause().equals(DamageCause.FALL)) {
                if (swPlayer.hasCooldown("fall", 2, false)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile pr = event.getEntity();
        HashMap<String, Trail> trails = plugin.buyableItemManager.getTrails();
        if (pr.getShooter() instanceof Player) {
            Player shooter = (Player) pr.getShooter();
            SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(shooter.getName());
            String soloTrailName = swPlayer.getSelectedItem("trail");
            Trail soloTrail = trails.get(soloTrailName);
            String duoTrailName = swPlayer.getSelectedItem("trail");
            Trail duoTrail = trails.get(duoTrailName);

            if (soloTrailName == null || soloTrail == null || duoTrailName == null || duoTrail == null) {
                return;
            }

            if (swPlayer.getArena().getArenaMode() == ArenaMode.SOLO) {
                if ((pr.getType().equals(EntityType.ARROW) || pr.getType().equals(EntityType.SNOWBALL) || pr.getType().equals(EntityType.EGG)) && trails.containsKey(soloTrailName)) {
                    World w = shooter.getWorld();
                    if (!soloTrailName.equalsIgnoreCase("Default")) {
                        int id = soloTrail.trailType.getTypeId();
                        new BukkitRunnable() {
                            public void run() {
                                w.playEffect(pr.getLocation(), Effect.STEP_SOUND, id);
                                if (!pr.isValid() || pr.isOnGround() || pr.getLocation().getY() < 0) cancel();
                            }
                        }.runTaskTimer(plugin, 3, 3);
                    }
                }
            } else {
                if ((pr.getType().equals(EntityType.ARROW) || pr.getType().equals(EntityType.SNOWBALL) || pr.getType().equals(EntityType.EGG)) && trails.containsKey(duoTrailName)) {
                    final World w = shooter.getWorld();
                    if (!duoTrailName.equalsIgnoreCase("Default")) {
                        int id = duoTrail.trailType.getTypeId();
                        new BukkitRunnable() {
                            public void run() {
                                w.playEffect(pr.getLocation(), Effect.STEP_SOUND, id);
                                if (!pr.isValid() || pr.isOnGround() || pr.getLocation().getY() < 0) cancel();
                            }
                        }.runTaskTimer(plugin, 3, 3);
                    }
                }
            }
        }
    }



    /*
     *
     * LIITE GAMEPLAY IMPROVEMENTS AND MINOR GAMEPLAY EVENTS
     *
     */

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());
        Arena arena = swPlayer.getArena();

        if (arena != null && arena.getPlayers().contains(p)) {
            for (ItemStack item : event.getDrops()) {
                p.getWorld().dropItemNaturally(p.getLocation(), item);
            }
            event.getDrops().clear();

            Player killer = p.getKiller();
            SWOfflinePlayer killerData = null;

            if (killer != null) {
                killerData = plugin.playerManager.getSWPlayer(killer.getName());
            }

            if (killer == null && !swPlayer.getLastAttacker().isEmpty() && ((System.currentTimeMillis() - swPlayer.getLastAttackedTime()) / 1000) <= 15) {
                killer = Bukkit.getPlayer(swPlayer.getLastAttacker());
                killerData = plugin.playerManager.getSWPlayer(swPlayer.getLastAttacker());
            }

            event.setDeathMessage(null);
            arena.killPlayer(swPlayer, killerData);
            TitleAPI.sendTitle(p, 10, 40, 10, "§cÖldün!", "§cArtık izleyicisin.");
        } else {
            event.getDrops().clear();
        }
    }

    @EventHandler
    public void openInventoryEvent(InventoryOpenEvent e) {
        if (e.getInventory() instanceof EnchantingInventory) {
            e.getInventory().setItem(1, this.lapis);
            enchantInventories.add((EnchantingInventory) e.getInventory());
        }
    }

    @EventHandler
    public void closeInventoryEvent(InventoryCloseEvent e) {
        if (e.getInventory() instanceof EnchantingInventory) {
            if (enchantInventories.contains(e
                    .getInventory())) {
                e.getInventory().setItem(1, null);
                enchantInventories.remove(e
                        .getInventory());
            }
        }
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent e) {
        if (e.getClickedInventory() instanceof EnchantingInventory) {
            if (enchantInventories.contains(e
                    .getInventory())) {
                if (e.getSlot() == 1) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void enchantItemEvent(EnchantItemEvent e) {
        if (enchantInventories.contains(e
                .getInventory())) {
            e.getInventory().setItem(1, this.lapis);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        final Player player = e.getPlayer();

        if (e.getItem().getTypeId() == 373) {
            Bukkit.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                public void run() {
                    player.getInventory().remove(Material.GLASS_BOTTLE);
                }
            }, 1L);
        }
    }

    public List<String> tabCompletePlayers(CommandSender sender, String token) {
        ArrayList<String> tabComplete = new ArrayList<String>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String name;
            if (plugin.bukkitPlayerManager.disguisedPlayers.containsKey(p.getName())) {
                name = plugin.bukkitPlayerManager.disguisedPlayers.get(p.getName());
            } else {
                name = p.getName();
            }

            if (name.equalsIgnoreCase(token)) {
                tabComplete.clear();
                tabComplete.add(name);
                break;
            }
            if (name.toLowerCase().contains(token.toLowerCase())) {
                tabComplete.add(name);
            }
        }
        return tabComplete;
    }

    @EventHandler
    public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent event) {
        event.getTabCompletions().clear();
        event.getTabCompletions().addAll(tabCompletePlayers(event.getPlayer(), event.getLastToken()));
    }

    @EventHandler
    public void onPing(ServerListPingEvent e) {
        if (plugin.arenaManager.getAvailableArena(1) == null) {
            e.setMotd("SUNUCU DOLU");
        } else {
            e.setMotd("SUNUCU MÜSAİT");
        }
    }

    @EventHandler
    private void on(WeatherChangeEvent e) {
        e.setCancelled(e.toWeatherState());
    }
}
