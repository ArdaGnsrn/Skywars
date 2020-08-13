package me.uniodex.skywars.listeners;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.arena.Arena;
import me.uniodex.skywars.player.SWOnlinePlayer;
import me.uniodex.skywars.utils.Utils;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class ProtectionListeners implements Listener {

    private Skywars plugin;

    public ProtectionListeners(Skywars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        if (!plugin.allowJoin) {
            event.setKickMessage("Sunucu başlatılıyor. Lütfen bekleyiniz ve bir süre sonra tekrar giriş yapmayı deneyiniz.");
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.disallow(Result.KICK_OTHER, "Sunucu başlatılıyor. Lütfen bekleyiniz ve bir süre sonra tekrar giriş yapmayı deneyiniz.");
        }
    }


    /**
     * * * * * * * * * * * * *
     * *
     * *
     * INVENTORY LISTENERS	*
     * *
     * *
     * * * * * * * * * ** * * *
     */

   @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (plugin.playerManager.editMode.contains(event.getWhoClicked().getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(event.getWhoClicked().getName());
        if (!swPlayer.canBuild()) {
            if (event.getClickedInventory() != null) {
                if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClickDispenserFix(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.DISPENSER) {
            Player p = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            if ((item != null) && (item.getType() == Material.ITEM_FRAME)) {
                event.setCancelled(true);
                p.sendMessage("§2[§bImperialCube§2] §cBazı açıklardan dolayı fırlatıcıya eşya çerçevesi koyulması engellenmiştir!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void EnvantereTiklayinca(InventoryClickEvent e) {
        if (!e.isCancelled()) {
            HumanEntity ent = e.getWhoClicked();
            if ((ent instanceof Player)) {
                Player player = (Player) ent;
                Inventory inv = e.getInventory();

                if ((inv instanceof AnvilInventory)) {
                    InventoryView view = e.getView();
                    int rawSlot = e.getRawSlot();
                    if (rawSlot == view.convertSlot(rawSlot)) {
                        if (rawSlot == 2) {
                            ItemStack item = e.getCurrentItem();
                            if (item != null) {
                                ItemMeta meta = item.getItemMeta();
                                if ((meta != null) &&
                                        (meta.hasDisplayName())) {
                                    String displayName = meta.getDisplayName();
                                    if (displayName.contains("̇")) {
                                        e.setCancelled(true);
                                        player.sendMessage("§2[§bAntiCrash§2] §cHatalı bir isim girdiniz!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * * * * * * * * * * * * *
     * *
     * *
     * PLAYER LISTENERS	*
     * *
     * *
     * * * * * * * * * ** * * *
     */

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        final Player p = e.getPlayer();

        // Oyuncu dünya değiştirdikten sonra 1.5 saniye boyunca bir arenaya giriş yapamadıysa lobiye yönlendir.
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                if (p.getWorld().getName().equalsIgnoreCase("world")) {
                    if (!plugin.playerManager.editMode.contains(p.getName())) {
                        p.kickPlayer(ChatColor.RED + "" + ChatColor.BOLD + "Oyuna girme esnasında bir sorun oluştuğu için lobiye yönlendirildiniz.");
                    }
                }
            }
        }, 30L);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        if (plugin.playerManager.editMode.contains(p.getName())) {
            return;
        }
        if (p.hasPermission("skywars.unblockcmds")) {
            return;
        }

        if (!plugin.config.allowedCommands.contains(e.getMessage().split(" ")[0].replace("/", "").toLowerCase())) {
            e.setCancelled(true);
            p.sendMessage(plugin.customization.messages.get("Command-Block"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        Action act = e.getAction();
        if (plugin.playerManager.editMode.contains(p.getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());

        if (act == Action.LEFT_CLICK_BLOCK) {
            if (p.getTargetBlock((Set<Material>) null, 5).getType() == Material.FIRE) {
                if (!swPlayer.canBuild()) {
                    e.setCancelled(true);
                }
            }
        }

        if (act.equals(Action.RIGHT_CLICK_AIR) || act.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (!swPlayer.canBuild()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (e.isCancelled()) return;
        if (plugin.playerManager.editMode.contains(e.getPlayer().getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(e.getPlayer().getName());
        if (!swPlayer.canBuild()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        if (e.isCancelled()) return;
        if (plugin.playerManager.editMode.contains(e.getPlayer().getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(e.getPlayer().getName());
        if (!swPlayer.canBuild()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntityType().equals(EntityType.PLAYER)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) return;
        Player p = event.getPlayer();
        if (plugin.playerManager.editMode.contains(p.getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());

        if (event.getBucket().equals(Material.LAVA_BUCKET)) {
            Block block = event.getBlockClicked().getRelative(event.getBlockFace());
            if (swPlayer.getArena() != null) {
                Team team = swPlayer.getArena().getTeam(swPlayer);
                if (team != null && team.getSize() > 1) {
                    ArrayList<Location> locs = new ArrayList<Location>();
                    locs.add(block.getLocation());
                    locs.add(block.getLocation().add(1, 0, 0));
                    locs.add(block.getLocation().add(0, 0, 1));
                    locs.add(block.getLocation().add(-1, 0, 0));
                    locs.add(block.getLocation().add(0, 0, -1));

                    for (OfflinePlayer player : team.getPlayers()) {
                        if (player instanceof Player && !player.getName().equals(p.getName())) {
                            for (Location loc : locs) {
                                if (((Player) player).getLocation().getBlock().getLocation().equals(loc)) {
                                    p.sendMessage(plugin.customization.prefix + "§cTakım arkadaşınızın altına lav koyamazsınız!");
                                    event.setCancelled(true);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void OyuncuKonustugunda(AsyncPlayerChatEvent olay) {
        if ((olay.getPlayer() instanceof Player)) {
            Player oyuncu = olay.getPlayer();
            if (olay.getMessage().contains("̇")) {
                olay.setCancelled(true);
                oyuncu.sendMessage("§2[§bAntiCrash§2] §cHatalı bir mesaj yazdınız!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void OyuncuKomutKullandiginda(PlayerCommandPreprocessEvent olay) {
        if ((olay.getPlayer() instanceof Player)) {
            Player oyuncu = olay.getPlayer();
            if (olay.getMessage().contains("̇")) {
                olay.setCancelled(true);
                oyuncu.sendMessage("§2[§bAntiCrash§2] §cHatalı bir mesaj yazdınız!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void CommandEngelPattern(PlayerCommandPreprocessEvent evt) {
        if (evt.isCancelled()) return;
        Player player = evt.getPlayer();
        // Komutlara : işareti ile başlamayı engeller
        Pattern pt = Pattern.compile("^/([a-zA-Z0-9_]+):");
        Matcher m = pt.matcher(evt.getMessage());
        if (!m.find()) {
            return;
        }
        String pluginRef = m.group(1);
        if ((pluginRef.toLowerCase().contains("bukkit")) || (pluginRef.toLowerCase().contains("minecraft"))) {
            evt.setCancelled(true);
            player.sendMessage(plugin.customization.messages.get("Command-Block"));
        } else {
            for (Plugin plugins : Bukkit.getPluginManager().getPlugins()) {
                if (plugins.getName().toLowerCase().contains(pluginRef)) {
                    evt.setCancelled(true);
                    player.sendMessage(plugin.customization.messages.get("Command-Block"));
                    break;
                }
            }
        }
    }

    /**
     * * * * * * * * * * * * *
     * *
     * *
     * ENTITY LISTENERS	*
     * *
     * *
     * * * * * * * * * ** * * *
     */

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        for (LivingEntity e : event.getAffectedEntities()) {
            if (e.getType().equals(EntityType.PLAYER)) {
                Player p = (Player) e;
                SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());
                if ((swPlayer != null) && (swPlayer.getArena() != null) && (swPlayer.getArena().getSpectators().contains(swPlayer))) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.getTarget() == null) {
            return;
        }

        if (e.getEntity().hasMetadata("SW_MOB")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCombust(final EntityCombustEvent event) {
        // Ignore if this is caused by an event lower down the chain.
        if (event instanceof EntityCombustByEntityEvent || event instanceof EntityCombustByBlockEvent) {
            return;
        }

        World world = event.getEntity().getWorld();

        // Ignore world's without sunlight
        if (world.getEnvironment() != Environment.NORMAL) {
            return;
        }

        // Prevent the target from burning.
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason().equals(SpawnReason.NATURAL) && plugin.config.disableNaturalMobSpawning) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntityType().equals(EntityType.PLAYER)) {
            Player player = (Player) e.getEntity();
            if (plugin.playerManager.editMode.contains(player.getName())) {
                return;
            }
            SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(player.getName());

            if (!swPlayer.canBuild()) {
                e.setCancelled(true);
            }
        } else if (e.getEntity().hasMetadata("SW_MOB") && e.getCause().equals(DamageCause.FALL)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;

        Entity entityDamager = e.getDamager();
        Entity entityVictim = e.getEntity();
        Player damager = null;

        // Saldırgan firework ve kurban oyuncu ise bunu engelle
        if (entityDamager.getType().equals(EntityType.FIREWORK) && (entityVictim.getType().equals(EntityType.PLAYER))) {
            e.setCancelled(true);
            return;
        }

        // Saldırgan blaze ise ve sahibine vuruyorsa bunu engelle
        if (entityDamager instanceof Projectile && ((Projectile) entityDamager).getShooter() instanceof Blaze) {
            Blaze b = (Blaze) ((Projectile) e.getDamager()).getShooter();
            if (b.hasMetadata("SW_MOB")) {
                LivingEntity entity = b;
                String owner = ChatColor.stripColor(entity.getCustomName().split("'in")[0]);
                damager = Bukkit.getPlayer(owner);
                if (damager.equals(entityVictim)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // Saldırgan oyuncu ise
        if (entityDamager.getType().equals(EntityType.PLAYER)) {
            damager = (Player) e.getDamager();
            SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(damager.getName());

            if (!swPlayer.canBuild()) {
                e.setCancelled(true);
            }
        }

        // Kurban oyuncu ise
        if (entityVictim.getType().equals(EntityType.PLAYER)) {
            Player victim = (Player) e.getEntity();
            SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(victim.getName());

            if (!swPlayer.canBuild()) {
                e.setCancelled(true);
            }
        }

        // Saldırgan oyuncu ve kurban bu saldırganın mobu ise bunu engelle
        if (!entityVictim.getType().equals(EntityType.PLAYER) && entityVictim.hasMetadata("SW_MOB")) {
            if (entityDamager.getType().equals(EntityType.PLAYER)) {
                LivingEntity entity = (LivingEntity) entityVictim;
                String ownerName = ChatColor.stripColor(entity.getCustomName().split("'in")[0]);
                Player owner = Bukkit.getPlayer(ownerName);
                if (owner != null && owner.equals(entityDamager)) {
                    e.setCancelled(true);
                }
            }
        }

        // Kurban oyuncu ise ve kurbanın SW_MOB metadatası varsa
        if (entityVictim.getType().equals(EntityType.PLAYER) && entityVictim.hasMetadata("SW_MOB")) {
            if (damager == null) {
                // Saldırgan ok ise ve bu okun sahibi oyuncu ise asıl saldırganı bu okun sahibi olarak ayarla
                if (entityDamager instanceof Projectile && ((Projectile) entityDamager).getShooter() instanceof Player) {
                    damager = (Player) ((Projectile) entityDamager).getShooter();
                }
                // Saldırganın da SW_MOB metadatası varsa asıl saldırganı bu mobun sahibi yap
                else if (entityDamager.hasMetadata("SW_MOB")) {
                    LivingEntity entity = (LivingEntity) entityDamager;
                    String owner = ChatColor.stripColor(entity.getCustomName().split("'in")[0]);
                    damager = Bukkit.getPlayer(owner);
                }
            }

            if (damager != null) {
                LivingEntity entity = (LivingEntity) entityVictim;
                if (ChatColor.stripColor(entity.getCustomName().split("'in")[0]).equals(damager.getName())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * * * * * * * * * * * * *
     * *
     * *
     * OTHER LISTENERS	    *
     * *
     * *
     * * * * * * * * * ** * * *
     */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void TabelaDegistirince(SignChangeEvent e) {
        for (int i = 0; i < 4; i++) {
            if (e.getLine(i).matches("^[a-zA-Z0-9_]*$")) {
                if (e.getLine(i).length() > 16) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§2[§bAntiCrash§2] §cHatalı bir tabela oluşturdunuz!");
                }
            }
        }
    }

    /*@EventHandler(priority = EventPriority.HIGHEST)
    public void worldInit(WorldInitEvent event) {
        event.getWorld().setKeepSpawnInMemory(false);
        event.getWorld().setAutoSave(false);
    }

    @EventHandler
    public void stopSaving(WorldSaveEvent event) {
        World world = event.getWorld();
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(false);
    }

    @EventHandler
    public void stopAutoSave(WorldLoadEvent event) {
        World world = event.getWorld();
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(false);
    }*/
}
