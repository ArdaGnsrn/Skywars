package me.uniodex.skywars.managers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.arena.Arena;
import me.uniodex.skywars.arena.TeamData;
import me.uniodex.skywars.enums.ArenaMode;
import me.uniodex.skywars.objects.Cage;
import me.uniodex.skywars.objects.Kit;
import me.uniodex.skywars.player.SWOfflinePlayer;
import me.uniodex.skywars.player.SWOnlinePlayer;
import me.uniodex.skywars.utils.ItemStackBuilder;
import me.uniodex.skywars.utils.Utils;
import me.uniodex.skywars.utils.packages.menubuilder.inventory.InventoryMenuBuilder;
import me.uniodex.skywars.utils.packages.menubuilder.inventory.ItemListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

@SuppressWarnings("deprecation")
public class MenuManager {

    private Skywars plugin;
    private ItemListener emptyListener;
    private ItemListener cageListener;

    public MenuManager(Skywars plugin) {
        this.plugin = plugin;
        emptyListener = new ItemListener() {
            @Override
            public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                return;
            }
        };
        loadListeners();

    }
    private void loadListeners() {
    }




    public Inventory getStatsInventory(SWOfflinePlayer swPlayer) {
        InventoryMenuBuilder menu = new InventoryMenuBuilder(36, "§1İstatistikler > " + swPlayer.getName());

        int kills = 0;
        int deaths = 0;
        int playedGames = 0;
        int wins = 0;
        String playTime = "";
        int projectilesHit = 0;
        int projectilesLaunched = 0;
        int blocksPlaced = 0;
        int blocksBroken = 0;
        int itemsEnchanted = 0;
        int itemsCrafted = 0;
        int fishesCaught = 0;

        if (swPlayer.getSWOnlinePlayer() != null) {
            swPlayer.getSWOnlinePlayer().updatePlayTime();
            kills = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "kills");
            deaths = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "deaths");
            playedGames = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "playedGames");
            wins = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "wins");
            float actplayTime = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "playTime") / 3600f;
            double playTimeD = Utils.round(actplayTime);
            playTime = playTimeD + " saat";
            projectilesHit = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "projectilesHit");
            projectilesLaunched = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "projectilesLaunched");
            blocksPlaced = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "blocksPlaced");
            blocksBroken = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "blocksBroken");
            itemsEnchanted = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "itemsEnchanted");
            itemsCrafted = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "itemsCrafted");
            fishesCaught = plugin.playerManager.getStat(swPlayer.getSWOnlinePlayer(), "fishesCaught");
        } else {
            kills = plugin.playerManager.getStat(swPlayer, "kills");
            deaths = plugin.playerManager.getStat(swPlayer, "deaths");
            playedGames = plugin.playerManager.getStat(swPlayer, "playedGames");
            wins = plugin.playerManager.getStat(swPlayer, "wins");
            double playTimeD = Utils.round(plugin.playerManager.getStat(swPlayer, "playTime") / 3600);
            playTime = playTimeD + " saat";
            projectilesHit = plugin.playerManager.getStat(swPlayer, "projectilesHit");
            projectilesLaunched = plugin.playerManager.getStat(swPlayer, "projectilesLaunched");
            blocksPlaced = plugin.playerManager.getStat(swPlayer, "blocksPlaced");
            blocksBroken = plugin.playerManager.getStat(swPlayer, "blocksBroken");
            itemsEnchanted = plugin.playerManager.getStat(swPlayer, "itemsEnchanted");
            itemsCrafted = plugin.playerManager.getStat(swPlayer, "itemsCrafted");
            fishesCaught = plugin.playerManager.getStat(swPlayer, "fishesCaught");
        }
        double kdr = new BigDecimal(deaths > 1 ? Double.valueOf(kills) / deaths : kills).setScale(2, RoundingMode.HALF_UP).doubleValue();
        int accuracy = (int) (projectilesLaunched > 0 ? (projectilesHit / Double.valueOf(projectilesLaunched)) * 100 : 0);

        Utils.cageMenu(menu, false);

        ItemStackBuilder builder = new ItemStackBuilder(Material.PAPER);
        menu.withItem(builder.setName(ChatColor.GREEN + "Öldürme:").addLore(ChatColor.YELLOW + String.valueOf(kills)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "Ölme:").addLore(ChatColor.YELLOW + String.valueOf(deaths)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "KDR:").addLore("§7(Öldürme/Ölme Oranı)").addLore(ChatColor.YELLOW + String.valueOf(kdr)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "Oynanan Oyun:").addLore(ChatColor.YELLOW + String.valueOf(playedGames)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "Kazanma:").addLore(ChatColor.YELLOW + String.valueOf(wins)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "Oynama Süresi:").addLore(ChatColor.YELLOW + playTime).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "İsabetli Ok Sayısı:").addLore(ChatColor.YELLOW + String.valueOf(projectilesHit)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "Atılan Ok Sayısı:").addLore(ChatColor.YELLOW + String.valueOf(projectilesLaunched)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "İsabet Oranı:").addLore(ChatColor.YELLOW + "%" + accuracy).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "Koyulan Blok:").addLore(ChatColor.YELLOW + String.valueOf(blocksPlaced)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "Kırılan Blok:").addLore(ChatColor.YELLOW + String.valueOf(blocksBroken)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "Büyülenen Eşya:").addLore(ChatColor.YELLOW + String.valueOf(itemsEnchanted)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "Oluşturulan Eşya:").addLore(ChatColor.YELLOW + String.valueOf(itemsCrafted)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(builder.setName(ChatColor.GREEN + "Yakalanan Balık:").addLore(ChatColor.YELLOW + String.valueOf(fishesCaught)).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        return menu.build();
    }

    public Inventory getKitInventory(SWOnlinePlayer swPlayer, String gameMode) {
        InventoryMenuBuilder menu = new InventoryMenuBuilder(27, "§1Bir Kit Seçin");
        HashMap<String, Kit> kits = plugin.buyableItemManager.getKits();

        int menuSlot = 0;
        for (int i = 0; i < swPlayer.getPlayerKits().size(); i++) {
            if (gameMode.equalsIgnoreCase("solo")) {
                if (swPlayer.getPlayerKits().get(i).contains("duo")) {
                    continue;
                }
            } else {
                if (swPlayer.getPlayerKits().get(i).contains("solo")) {
                    continue;
                }
            }
            final Kit kit = kits.get(swPlayer.getPlayerKits().get(i).replace("solo", "").replace("duo", ""));
            ItemStack itm = plugin.buyableItemManager.getItem(swPlayer, kit, gameMode.equalsIgnoreCase("solo") ? "solokit" : "duokit");
            if (gameMode.equalsIgnoreCase("solo")) {
                if (kit.equals(kits.get(swPlayer.getSelectedItem("kit")))) {
                    itm = Utils.addGlow(itm);
                }
            } else {
                if (kit.equals(kits.get(swPlayer.getSelectedItem("kit")))) {
                    itm = Utils.addGlow(itm);
                }
            }

            menu.withItem(menuSlot, itm, new ItemListener() {
                @Override
                public void onInteract(final Player player, ClickType action, ItemStack item) {
                    player.closeInventory();
                    if (plugin.buyableItemManager.playerHaveItem(swPlayer, kit.itemId, (gameMode.equalsIgnoreCase("solo") ? "solokit" : "duokit")) || kit.itemId.equalsIgnoreCase("Default")) {
                        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                            public void run() {
                                plugin.buyableItemManager.selectItem(swPlayer, kit.itemId, (gameMode.equalsIgnoreCase("solo") ? "solokit" : "duokit"));
                            }
                        }, 1L);
                    } else {
                        player.sendMessage(plugin.customization.prefix + "§cBu kite sahip olmadığınız için seçemezsiniz!");
                    }
                }
            }, InventoryMenuBuilder.ALL_CLICK_TYPES);
            menuSlot++;
        }
        for (Entry<String, Kit> entry : kits.entrySet()) {
            final Kit kit = entry.getValue();

            if (kit.itemId.equalsIgnoreCase("Default")) {
                continue;
            }

            if (!plugin.buyableItemManager.playerHaveItem(swPlayer, kit.itemId, gameMode.equalsIgnoreCase("solo") ? "solokit" : "duokit")) {
                menu.withItem(menuSlot, plugin.buyableItemManager.getItem(swPlayer, kit, gameMode.equalsIgnoreCase("solo") ? "solokit" : "duokit"), new ItemListener() {
                    @Override
                    public void onInteract(final Player player, ClickType action, ItemStack item) {
                        player.closeInventory();
                        if (plugin.buyableItemManager.playerHaveItem(swPlayer, kit.itemId, (gameMode.equalsIgnoreCase("solo") ? "solokit" : "duokit")) || kit.itemId.equalsIgnoreCase("Default")) {
                            Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                                public void run() {
                                    plugin.buyableItemManager.selectItem(swPlayer, kit.itemId, (gameMode.equalsIgnoreCase("solo") ? "solokit" : "duokit"));
                                }
                            }, 1L);
                        } else {
                            player.sendMessage(plugin.customization.prefix + "§cBu kite sahip olmadığınız için seçemezsiniz!");
                        }
                    }
                }, InventoryMenuBuilder.ALL_CLICK_TYPES);
                menuSlot++;
            }
        }
        return menu.build();
    }

    public Inventory getEditor(String arenaName) {
        InventoryMenuBuilder menu = new InventoryMenuBuilder(54, "§4Düzenleniyor: §1" + arenaName);
        Arena arena = plugin.arenaManager.getArena(arenaName);
        Utils.cageMenu(menu, true);

        for (int i = 0; i < 5; i++) {
            final int slot = i;
            menu.withItem(i, plugin.clickableItemManager.getPlusItem(), new ItemListener() {
                @Override
                public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                    ItemStack item = menu.getInventory().getItem(slot + 9);
                    String option = ChatColor.stripColor(item.getItemMeta().getDisplayName()).split(": ")[0];
                    int newValue = Integer.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName()).split(": ")[1]) + (1);
                    if (newValue < 1) return;
                    ItemStack lastItem = new ItemStackBuilder(item).setName(ChatColor.YELLOW + option + ": " + ChatColor.GOLD + newValue).build();
                    menu.getInventory().setItem(slot, lastItem);
                }
            }, InventoryMenuBuilder.ALL_CLICK_TYPES);
        }

        for (int i = 18; i < 23; i++) {
            final int slot = i;
            menu.withItem(i, plugin.clickableItemManager.getMinusItem(), new ItemListener() {
                @Override
                public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                    ItemStack item = menu.getInventory().getItem(slot - 9);
                    String option = ChatColor.stripColor(item.getItemMeta().getDisplayName()).split(": ")[0];
                    int newValue = Integer.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName()).split(": ")[1]) + (-1);
                    if (newValue < 1) return;
                    ItemStack lastItem = new ItemStackBuilder(item).setName(ChatColor.YELLOW + option + ": " + ChatColor.GOLD + newValue).build();
                    menu.getInventory().setItem(slot, lastItem);
                }
            }, InventoryMenuBuilder.ALL_CLICK_TYPES);
        }

        ItemStackBuilder builder = new ItemStackBuilder(Material.PAPER);
        menu.withItem(9, builder.setName(ChatColor.YELLOW + "Team size: " + ChatColor.GOLD + arena.getPlayerSizePerTeam()).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(10, builder.setName(ChatColor.YELLOW + "Min teams: " + ChatColor.GOLD + arena.getMinTeamAmount()).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(11, builder.setName(ChatColor.YELLOW + "Max teams: " + ChatColor.GOLD + arena.getMaxTeamAmount()).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(12, builder.setName(ChatColor.YELLOW + "Lobby countdown: " + ChatColor.GOLD + arena.getLobbyCountdown()).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);
        menu.withItem(13, builder.setName(ChatColor.YELLOW + "Game length: " + ChatColor.GOLD + arena.getGameLength()).build(), emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);

        String[] cubsplit = arena.getCuboid().toString().split(", ");
        String introduction = ChatColor.AQUA + "- " + ChatColor.YELLOW;
        int size = (int) (plugin.configManager.getSize(new File(plugin.getDataFolder() + "/arenas", arena.getArenaName())) / 1000);
        ItemStack information = new ItemStackBuilder(Material.CHEST).setName(ChatColor.YELLOW + "Bilgi").
                addLore(introduction + "Dünya: " + ChatColor.GOLD + cubsplit[0], introduction + "Alt Köşe: " + ChatColor.GOLD + cubsplit[1] + ", " + cubsplit[2] + ", " + cubsplit[3],
                        introduction + "Üst Köşe: " + ChatColor.GOLD + cubsplit[4] + ", " + cubsplit[5] + ", " + cubsplit[6], introduction + "Bloklar: " + ChatColor.GOLD + arena.getCuboid().getSize(),
                        introduction + "Dosyaların Boyutu: " + ChatColor.GOLD + size + " kb",
                        introduction + "Boyut Tipi: " + (size < 501 ? ChatColor.GREEN + "Küçük" : size < 1001 ? ChatColor.YELLOW + "Orta" : ChatColor.RED + "Büyük")).build();
        menu.withItem(37, information, emptyListener, InventoryMenuBuilder.ALL_CLICK_TYPES);

        menu.withItem(40, new ItemStackBuilder(Material.INK_SACK).setName(arena.isEnabled() ? ChatColor.GREEN + "Aktif" : ChatColor.RED + "Devre Dışı").setDurability(arena.isEnabled() ? 10 : 8).build(), new ItemListener() {
            @Override
            public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                ItemStack item = new ItemStackBuilder(itemStack).setDurability(itemStack.getDurability() == 8 ? 10 : 8).setName(itemStack.getDurability() == 10 ? ChatColor.GREEN + "Aktif" : ChatColor.RED + "Devre Dışı").build();
                menu.getInventory().setItem(40, item);
            }
        }, InventoryMenuBuilder.ALL_CLICK_TYPES);

        menu.withItem(43, plugin.clickableItemManager.getSaveItem(), new ItemListener() {
            @Override
            public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                long before = System.currentTimeMillis();
                HashMap<String, Integer> values = new HashMap<String, Integer>();
                for (int i = 9; i < 14; i++) {
                    values.put(ChatColor.stripColor(menu.getInventory().getItem(i).getItemMeta().getDisplayName()).split(": ")[0].toLowerCase().replace(" ", "-"), Integer.valueOf(ChatColor.stripColor(menu.getInventory().getItem(i).getItemMeta().getDisplayName()).split(": ")[1]));
                }
                if (values.get("min-teams") < 2) {
                    player.sendMessage(plugin.customization.prefix + "Minimum takım sayısı en az 2 olmalıdır!");
                    return;
                }
                if (arena.isAvailable()) {
                    final Iterator<Team> iterator = arena.getAliveTeams().iterator();
                    while (iterator.hasNext()) {
                        arena.destroyCage(iterator.next());
                    }
                }
                boolean enabled = menu.getInventory().getItem(40).getDurability() == 10;
                arena.setEnabled(enabled);
                arena.setTeamSize(values.get("team-size"));
                arena.setMinTeams(values.get("min-teams"));
                arena.setMaxTeams(values.get("max-teams"));
                arena.setLobbyCountdown(values.get("lobby-countdown"));
                arena.setGameLength(values.get("game-length"));
                arena.updateGameMode();

                File file = new File(plugin.getDataFolder() + "/arenas/" + arena.getArenaName(), "settings.yml");
                FileConfiguration editor = YamlConfiguration.loadConfiguration(file);
                for (String key : values.keySet()) editor.set(key, values.get(key));
                editor.set("enabled", enabled);
                try {
                    editor.save(file);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                arena.stop();

                player.sendMessage(plugin.customization.prefix + "Ayarlarınız kaydedildi ve uygulandı! İşlem Süresi: " + ChatColor.LIGHT_PURPLE + (System.currentTimeMillis() - before) + "ms!");
                player.closeInventory();
            }
        }, InventoryMenuBuilder.ALL_CLICK_TYPES);
        return menu.build();
    }

    public Inventory getQuitInventory() {
        InventoryMenuBuilder menu = new InventoryMenuBuilder(9, plugin.customization.inventories.get("Quit-Inventory"));
        menu.withItem(2, plugin.clickableItemManager.getConfirmItem(), new ItemListener() {
            @Override
            public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                String playerName = player.getName();
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(plugin.config.bungee_mode_hub);
                player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        Player p = Bukkit.getPlayer(playerName);
                        if (p != null) {
                            p.kickPlayer("Lobiye yönlendirildiniz.");
                        }
                    }
                }, 5L);
            }
        }, InventoryMenuBuilder.ALL_CLICK_TYPES);

        menu.withItem(6, plugin.clickableItemManager.getCancelItem(), new ItemListener() {
            @Override
            public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                player.closeInventory();
            }
        }, InventoryMenuBuilder.ALL_CLICK_TYPES);
        return menu.build();
    }

    public Inventory getVoteMenu(Arena arena) {
        return arena.getVoteManager().getEntry("Chests");
    }

    public Inventory getSpectatorMenu(Arena arena) {
        if (arena != null) {
            int menuSize = 0;
            for (Player x : arena.getPlayers()) {
                SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(x.getName());
                if (arena.getSpectators().contains(swPlayer)) {
                    continue;
                }
                menuSize++;
            }
            menuSize = Utils.getInventorySize(menuSize);

            InventoryMenuBuilder menu = new InventoryMenuBuilder(menuSize, plugin.customization.inventories.get("Spectator-Teleporter"));
            for (Player x : arena.getPlayers()) {
                SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(x.getName());
                if (arena.getSpectators().contains(swPlayer)) { continue; }
                String name = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(x.getName()) ? plugin.bukkitPlayerManager.disguisedPlayers.get(x.getName()) : x.getName();
                menu.withItem(Utils.getSkull(name, ChatColor.AQUA + name), getSpectatorListener(x), InventoryMenuBuilder.ALL_CLICK_TYPES);
            }
            return menu.build();
        } else {
            return null;
        }
    }
    private ItemListener getSpectatorListener(Player x) {
        return new ItemListener() {
            @Override
            public void onInteract(Player player, ClickType action, ItemStack item) {
                Player target = x;
                if (target != null) {
                    player.teleport(target.getLocation().add(0, 3, 0));
                    player.closeInventory();
                }
            }
        };
    }

    public Inventory getCageInventory(SWOnlinePlayer swPlayer, String gameMode) {
        InventoryMenuBuilder menu = new InventoryMenuBuilder(36, "§1Bir Kafes Kenar Seçin");
        HashMap<String, Cage> cages = plugin.buyableItemManager.getCages();

        int menuSlot = 0;
        for (int i = 0; i < swPlayer.getPlayerCages().size(); i++) {
            if (gameMode.equalsIgnoreCase("solo")) { if (swPlayer.getPlayerCages().get(i).contains("duo")) { continue; } }
            else { if (swPlayer.getPlayerCages().get(i).contains("solo")) { continue; } }

            final Cage cage = cages.get(swPlayer.getPlayerCages().get(i).replace("solo", "").replace("duo", ""));
            ItemStack itm = plugin.buyableItemManager.getItem(swPlayer, cage, gameMode.equalsIgnoreCase("solo") ? "solocage" : "duocage");
            if (gameMode.equalsIgnoreCase("solo")) {
                if (cage.equals(cages.get(swPlayer.getSelectedItem("cage")))) { itm = Utils.addGlow(itm); } }
            else {
                if (cage.equals(cages.get(swPlayer.getSelectedItem("cage")))) { itm = Utils.addGlow(itm); } }
            menu.withItem(menuSlot, itm, getCageListener(swPlayer, cage, gameMode), InventoryMenuBuilder.ALL_CLICK_TYPES);
            menuSlot++;
        }
        for (Entry<String, Cage> entry : cages.entrySet()) {
            final Cage cage = entry.getValue();
            if (cage.itemId.equalsIgnoreCase("Default")) { continue; }
            if (!plugin.buyableItemManager.playerHaveItem(swPlayer, cage.itemId, gameMode.equalsIgnoreCase("solo") ? "solocage" : "duocage")) {
                menu.withItem(menuSlot, plugin.buyableItemManager.getItem(swPlayer, cage, gameMode.equalsIgnoreCase("solo") ? "solocage" : "duocage"), getCageListener(swPlayer, cage, gameMode), InventoryMenuBuilder.ALL_CLICK_TYPES);
                menuSlot++;
            }
        }
        return menu.build();
    }
    private ItemListener getCageListener(SWOnlinePlayer swPlayer, Cage cage, String gameMode) {
        return new ItemListener() {
            @Override
            public void onInteract(Player player, ClickType action, ItemStack item) {
                player.closeInventory();
                if (plugin.buyableItemManager.playerHaveItem(swPlayer, cage.itemId, (gameMode.equalsIgnoreCase("solo") ? "solocage" : "duocage")) || cage.itemId.equalsIgnoreCase("Default")) {
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            plugin.buyableItemManager.selectItem(swPlayer, cage.itemId, (gameMode.equalsIgnoreCase("solo") ? "solocage" : "duocage"));
                        }
                    }, 1L);
                } else {
                    player.sendMessage(plugin.customization.prefix + "§cBu kafese sahip olmadığınız için seçemezsiniz!");
                }
            }
        };
    }
}
