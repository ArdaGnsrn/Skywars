package me.uniodex.skywars.managers;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.objects.Cage;
import me.uniodex.skywars.objects.Item;
import me.uniodex.skywars.objects.Kit;
import me.uniodex.skywars.objects.Trail;
import me.uniodex.skywars.player.SWOnlinePlayer;
import me.uniodex.skywars.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuyableItemManager {

    private Skywars plugin;

    private HashMap<String, Kit> kits;
    private HashMap<String, Cage> cages;
    private HashMap<String, Trail> trails;

    public BuyableItemManager(Skywars plugin) {
        this.plugin = plugin;
        loadKits();
        loadCages();
        loadTrails();
    }

    private void loadKits() {
        this.kits = new HashMap<String, Kit>();

        FileConfiguration kitsFile = plugin.configManager.getKitsConfig();
        for (String kitId : kitsFile.getConfigurationSection("Kits").getKeys(false)) {
            String path = "Kits." + kitId + ".";

            String kitName = kitsFile.getString(path + "name");
            String kitDesc = kitsFile.getString(path + "description");
            String preKitIcon = kitsFile.getString(path + "icon");
            String rarity = kitsFile.getString(path + "rarity");
            int cost = kitsFile.getInt(path + "cost");
            String permission = kitsFile.getString(path + "permission");
            ArrayList<ItemStack> kitItems = new ArrayList<ItemStack>();
            for (String item : kitsFile.getStringList(path + "items")) {
                ItemStack kitItem = Utils.getItemStack(item, true, true);
                kitItems.add(kitItem);
            }

            kitName = ChatColor.translateAlternateColorCodes('&', kitName);
            ItemStack kitIcon = Utils.getItemStack(preKitIcon, true, true);

            this.kits.put(kitId, new Kit(kitId, kitName, kitIcon, kitDesc, rarity, cost, permission, kitItems));
        }
    }

    private void loadCages() {
        this.cages = new HashMap<String, Cage>();
        FileConfiguration cagesFile = plugin.configManager.getCagesConfig();
        for (String cageId : cagesFile.getConfigurationSection("Cages").getKeys(false)) {
            String path = "Cages." + cageId + ".";

            String cageName = cagesFile.getString(path + "name");
            String cageDesc = cagesFile.getString(path + "description");
            String preCageIcon = cagesFile.getString(path + "icon");
            String rarity = cagesFile.getString(path + "rarity");
            int cost = cagesFile.getInt(path + "cost");
            String permission = cagesFile.getString(path + "permission");

            /*
             * 0 = ceiling
             * 1 = ceilingBorder
             * 2 = higherMiddle
             * 3 = higherMiddleBorder
             * 4 = middle
             * 5 = middleBorder
             * 6 = lowerMiddle
             * 7 = lowerMiddleBorder
             * 8 = floorBorder
             * 9 = floor
             */
            String[] cagePartsString = new String[]{"ceiling", "ceilingBorder", "higherMiddle", "higherMiddleBorder", "middle", "middleBorder", "lowerMiddle", "lowerMiddleBorder", "floorBorder", "floor"};
            ItemStack[] cageParts = new ItemStack[10];
            for (int i = 0; i < cageParts.length; i++) {
                cageParts[i] = Utils.getItemStack(cagesFile.getString(path + cagePartsString[i]), false, false);
            }

            cageName = ChatColor.translateAlternateColorCodes('&', cageName);
            ItemStack cageIcon = Utils.getItemStack(preCageIcon, true, true);

            this.cages.put(cageId, new Cage(cageId, cageName, cageIcon, cageDesc, rarity, cost, permission, cageParts));
        }
    }

    private void loadTrails() {
        this.trails = new HashMap<String, Trail>();
        FileConfiguration trailsFile = plugin.configManager.getTrailsConfig();
        for (String trailId : trailsFile.getConfigurationSection("Trails").getKeys(false)) {
            String path = "Trails." + trailId + ".";

            String trailName = trailsFile.getString(path + "name");
            String trailDesc = trailsFile.getString(path + "description");
            String preTrailIcon = trailsFile.getString(path + "icon");
            String rarity = trailsFile.getString(path + "rarity");
            int cost = trailsFile.getInt(path + "cost");
            String permission = trailsFile.getString(path + "permission");

            ItemStack trailItem = Utils.getItemStack(trailsFile.getString(path + "icon"), false, false);

            trailName = ChatColor.translateAlternateColorCodes('&', trailName);
            ItemStack trailIcon = Utils.getItemStack(preTrailIcon, true, true);

            this.trails.put(trailId, new Trail(trailId, trailName, trailIcon, trailDesc, rarity, cost, permission, trailItem));
        }
    }

    public boolean playerHaveItem(SWOnlinePlayer p, String itemid, String itemType) {
        String serverType;
        if (itemType.contains("solo")) {
            serverType = "solo";
        } else {
            serverType = "duo";
        }

        String actualItemType = itemType.replace(serverType, "");

        if (actualItemType.equalsIgnoreCase("kit")) {
            return p.getPlayerKits().contains(serverType + itemid);
        }
        if (actualItemType.equalsIgnoreCase("cage")) {
            return p.getPlayerCages().contains(serverType + itemid);
        }
        if (actualItemType.equalsIgnoreCase("trail")) {
            return p.getPlayerTrails().contains(serverType + itemid);
        }
        return false;
    }

    public ItemStack getItem(SWOnlinePlayer p, Item item, String itemType) {
        boolean greyedOut = false, buyable = false, chooseable = false, youDontHavethat = false, vipOzel = false;
        if (this.playerHaveItem(p, item.itemId, itemType) || item.itemId.equalsIgnoreCase("Default")) {
            chooseable = true;
        } else {
            greyedOut = true;
            youDontHavethat = true;
            //buyable = true;
            if (!item.permission.equalsIgnoreCase("NONE")) {
                vipOzel = true;
                youDontHavethat = false;
                //buyable = false;
            }
        }

        List<String> splittedDesc = new ArrayList<String>();
        String itemInfoInstance = item.itemInfo;
        String[] itemInfoSplitted = itemInfoInstance.split("\n");
        for (String itemInfo : itemInfoSplitted) {
            if (itemInfo.length() > 35) {
                String[] splitted = Utils.splitString(itemInfo, 4).split("\n");
                for (String itemInfomsg : splitted) {
                    splittedDesc.add(itemInfomsg);
                }
            } else {
                splittedDesc.add(itemInfo);
            }
        }

        ItemStack itemIcon = item.itemIcon;
        if (greyedOut) {
            itemIcon = new ItemStack(Material.STAINED_GLASS_PANE);
            itemIcon.setDurability((short) 14);
        }

        List<String> lore = new ArrayList<String>();

        for (String lorePart : splittedDesc) {
            if (lorePart.length() > 0) {
                lore.add("§7" + ChatColor.translateAlternateColorCodes('&', lorePart));
            }
        }

        lore.add(" ");
        String rarityInstance = item.rarity;
        if (rarityInstance.equalsIgnoreCase("yaygin")) {
            rarityInstance = "§aYaygın";
        } else if (rarityInstance.equalsIgnoreCase("nadir")) {
            rarityInstance = "§5Nadir";
        } else if (rarityInstance.equalsIgnoreCase("efsanevi")) {
            rarityInstance = "§cEfsanevi";
        } else if (rarityInstance.equalsIgnoreCase("destansi")) {
            rarityInstance = "§dDestansı";
        }
        //int costInstance = item.cost;
        lore.add("§7Nadirlik: " + rarityInstance);
        //lore.add("§7Fiyat: §2"+ NumberFormat.getNumberInstance(Locale.GERMAN).format(costInstance) + " uCoin");
        lore.add(" ");
        if (buyable) {
            lore.add("§a§lSatın almak için tıkla!");
        }
        if (chooseable) {
            if (itemType.contains("cage")) {
                lore.add("§a§lBu kafesi seçmek için tıkla!");
            } else if (itemType.contains("kit")) {
                lore.add("§a§lBu kiti seçmek için tıkla!");
            } else if (itemType.contains("trail")) {
                lore.add("§a§lBu iz efektini seçmek için tıkla!");
            }
        }

        if (vipOzel) {
            lore.add("§c§lBu eşya VIP oyunculara özeldir!");
        }

        if (youDontHavethat) {
            lore.add("§c§lBu kite sahip değilsiniz!");
        }

        ItemMeta meta = itemIcon.getItemMeta();
        meta.setDisplayName("§6§l" + item.itemName);
        meta.setLore(lore);
        itemIcon.setItemMeta(meta);
        return itemIcon;
    }

    public void selectItem(SWOnlinePlayer p, String itemid, String itemType) {
        String serverType;
        if (itemType.contains("solo")) {
            serverType = "solo";
        } else {
            serverType = "duo";
        }

        String actualItemType = itemType.replace(serverType, "");

        p.setSelectedItem(actualItemType, itemid);
        plugin.sqlManager.selectItem(p.getName(), itemid, itemType);
        p.getPlayer().sendMessage("§2[§bImperialCube§2] §aEşya başarıyla seçildi.");
        if (actualItemType.equalsIgnoreCase("cage")) {
            p.getPlayer().sendMessage("§2[§bImperialCube§2] §aSeçtiğiniz kafes bir sonraki oyununuzda aktif olacak!");
        }
    }

    /* PUBLIC GET METHODS */

    public HashMap<String, Kit> getKits() {
        return kits;
    }

    public HashMap<String, Cage> getCages() {
        return cages;
    }

    public HashMap<String, Trail> getTrails() {
        return trails;
    }
}
