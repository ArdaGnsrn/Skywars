package me.uniodex.skywars.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.enums.ArenaMode;
import me.uniodex.skywars.enums.NextEvent;
import me.uniodex.skywars.player.SWOnlinePlayer;
import me.uniodex.skywars.utils.packages.menubuilder.inventory.InventoryMenuBuilder;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {
    //TODO Organize

    public static boolean isHelmet(ItemStack item) {
        return item.getType().equals(Material.CHAINMAIL_HELMET) || item.getType().equals(Material.SKULL_ITEM) || item.getType().equals(Material.DIAMOND_HELMET) || item.getType().equals(Material.GOLD_HELMET) || item.getType().equals(Material.IRON_HELMET) || item.getType().equals(Material.LEATHER_HELMET);
    }

    public static boolean isChestplate(ItemStack item) {
        return item.getType().equals(Material.CHAINMAIL_CHESTPLATE) || item.getType().equals(Material.DIAMOND_CHESTPLATE) || item.getType().equals(Material.GOLD_CHESTPLATE) || item.getType().equals(Material.IRON_CHESTPLATE) || item.getType().equals(Material.LEATHER_CHESTPLATE);
    }

    public static boolean isLeggings(ItemStack item) {
        return item.getType().equals(Material.CHAINMAIL_LEGGINGS) || item.getType().equals(Material.DIAMOND_LEGGINGS) || item.getType().equals(Material.GOLD_LEGGINGS) || item.getType().equals(Material.IRON_LEGGINGS) || item.getType().equals(Material.LEATHER_LEGGINGS);
    }

    public static boolean isBoots(ItemStack item) {
        return item.getType().equals(Material.CHAINMAIL_BOOTS) || item.getType().equals(Material.DIAMOND_BOOTS) || item.getType().equals(Material.GOLD_BOOTS) || item.getType().equals(Material.IRON_BOOTS) || item.getType().equals(Material.LEATHER_BOOTS);
    }

    public static boolean isSword(ItemStack item) {
        return item.getType().equals(Material.DIAMOND_SWORD) || item.getType().equals(Material.GOLD_SWORD) || item.getType().equals(Material.IRON_SWORD) || item.getType().equals(Material.STONE_SWORD) || item.getType().equals(Material.WOOD_SWORD);
    }

    public static boolean isBlock(ItemStack item) {
        return item.getType().isBlock();
    }

    public static ItemStack getRandomItemStack(ArrayList<ItemStack> list, ArrayList<ItemStack> exclude) {
        Random rand = new Random();
        int random = rand.nextInt(list.size());
        ItemStack randomItem = list.get(random);

        while (exclude.contains(randomItem)) {
            random = rand.nextInt(list.size());
            randomItem = list.get(random);
        }

        return randomItem;
    }

    public static int getHighest(Collection<Integer> list, int limit) {
        int highest = 0;
        for (int i : list) {
            if (i > highest && i < limit) {
                highest = i;
            }
        }
        return highest;
    }

    public static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public static String getItemName(ItemStack item) {
        return item.getType().toString();
    }

    public static ItemStack getItemStack(String item, boolean amount, boolean extra) {
        String[] split = item.split(" : ");
        if (split[0].equalsIgnoreCase("ozelIksirAtestenKorunma")) {
            ItemStack pot = new ItemStack(Material.POTION);
            ItemStackBuilder builder = new ItemStackBuilder(addPotionEffect(pot, PotionEffectType.FIRE_RESISTANCE, 5, 1, false));
            builder.setName("§5Ateşten Korunma İksiri");
            if (amount) builder.setAmount(Integer.valueOf(split[1]));
            return builder.build();
        }
        if (split[0].equalsIgnoreCase("ozelIksirRegen2")) {
            ItemStack pot = new ItemStack(Material.POTION);
            ItemStackBuilder builder = new ItemStackBuilder(addPotionEffect(pot, PotionEffectType.REGENERATION, 16, 2, true));
            builder.setName("§aPatlayıcı Yenilenme İksiri");
            if (amount) builder.setAmount(Integer.valueOf(split[1]));
            return builder.build();
        }
        if (split[0].equalsIgnoreCase("ozelIksirZehir")) {
            ItemStack pot = new ItemStack(Material.POTION);
            ItemStackBuilder builder = new ItemStackBuilder(addPotionEffect(pot, PotionEffectType.POISON, 12, 1, true));
            builder.setName("§cPatlayıcı Zehir İksiri");
            if (amount) builder.setAmount(Integer.valueOf(split[1]));
            return builder.build();
        }
        if (split[0].equalsIgnoreCase("ozelIksirCeviklik")) {
            ItemStack pot = new ItemStack(Material.POTION);
            ItemStackBuilder builder = new ItemStackBuilder(addPotionEffect(pot, PotionEffectType.SPEED, 67, 2, true));
            builder.setName("§bPatlayıcı Çeviklik İksiri");
            if (amount) builder.setAmount(Integer.valueOf(split[1]));
            return builder.build();
        }
        if (split[0].equalsIgnoreCase("korlukIksiri")) {
            ItemStack pot = new ItemStack(Material.POTION);
            ItemStackBuilder builder = new ItemStackBuilder(addPotionEffect(pot, PotionEffectType.BLINDNESS, 8, 1, true));
            builder.setName("§0Yarasa Adamın İksiri");
            if (amount) builder.setAmount(Integer.valueOf(split[1]));
            return builder.build();
        }
        if (split[0].equalsIgnoreCase("gucIksiri")) {
            ItemStack pot = new ItemStack(Material.POTION);
            ItemStackBuilder builder = new ItemStackBuilder(addPotionEffect(pot, PotionEffectType.INCREASE_DAMAGE, 5, 1, false));
            builder.setName("§6Güç İksiri");
            if (amount) builder.setAmount(Integer.valueOf(split[1]));
            return builder.build();
        }
        if (split[0].equalsIgnoreCase("kurbagaIksiri")) {
            ItemStack pot = new ItemStack(Material.POTION);
            ItemStack builder1 = new ItemStackBuilder(addPotionEffect(pot, PotionEffectType.SPEED, 20, 2, true)).build();
            ItemStackBuilder builder = new ItemStackBuilder(addPotionEffect(builder1, PotionEffectType.JUMP, 20, 2, true));
            builder.setName("§2Kurbağanın İksiri");
            if (amount) builder.setAmount(Integer.valueOf(split[1]));
            return builder.build();
        }
        if (split[0].equalsIgnoreCase("hiz2Iksiri")) {
            ItemStack pot = new ItemStack(Material.POTION);
            ItemStackBuilder builder = new ItemStackBuilder(addPotionEffect(pot, PotionEffectType.SPEED, 15, 2, true));
            builder.setName("§bAtılabilir Hız İksiri");
            if (amount) builder.setAmount(Integer.valueOf(split[1]));
            return builder.build();
        }
        if (split[0].equalsIgnoreCase("rastgeleMuzikDiski")) {
            ItemStack[] disk = new ItemStack[]{new ItemStack(Material.RECORD_3), new ItemStack(Material.RECORD_4), new ItemStack(Material.RECORD_5), new ItemStack(Material.RECORD_6), new ItemStack(Material.RECORD_7), new ItemStack(Material.RECORD_8), new ItemStack(Material.RECORD_9), new ItemStack(Material.RECORD_10), new ItemStack(Material.RECORD_11), new ItemStack(Material.RECORD_12)};
            Random generator = new Random();
            int randomIndex = generator.nextInt(disk.length);
            ItemStackBuilder builder = new ItemStackBuilder(disk[randomIndex]);
            if (amount) builder.setAmount(Integer.valueOf(split[1]));
            return builder.build();
        }
        if (split[0].equalsIgnoreCase("kurbagaKafasi")) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            PropertyMap propertyMap = profile.getProperties();
            if (propertyMap == null) {
                throw new IllegalStateException("Profile doesn't contain a property map");
            }
            String encodedData = Base64.encodeBytes(String.format("{textures:{SKIN:{url:\"%s\"}}}", "http://textures.minecraft.net/texture/d2c3b98ada19957f8d83a7d42faf81a290fae7d08dbf6c1f8992a1ada44b31").getBytes());
            propertyMap.put("textures", new Property("textures", encodedData));
            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            ItemMeta headMeta = head.getItemMeta();
            Class<?> headMetaClass = headMeta.getClass();
            Reflections.getField(headMetaClass, "profile", GameProfile.class).set(headMeta, profile);
            head.setItemMeta(headMeta);

            if (amount) head.setAmount(Integer.valueOf(split[1]));
            return head;
        }

        ItemStackBuilder builder = new ItemStackBuilder(split[0].contains(":") ? Material.getMaterial(split[0].split(":")[0].toUpperCase()) : Material.getMaterial(split[0].toUpperCase()));
        Material itemType = split[0].contains(":") ? Material.getMaterial(split[0].split(":")[0].toUpperCase()) : Material.getMaterial(split[0].toUpperCase());
        if (amount) builder.setAmount(Integer.valueOf(split[1]));
        if (split[0].contains(":")) builder.setDurability(Integer.valueOf(split[0].split(":")[1]));
        if (extra) {
            for (int i = amount ? 2 : 1; i < split.length; i++) {
                String type = split[i].split(":")[0].toLowerCase();
                if (type.equals("name"))
                    builder.setName(ChatColor.translateAlternateColorCodes('&', split[i].split(":")[1]));
                else if (type.equals("lore"))
                    builder.addLore(ChatColor.translateAlternateColorCodes('&', split[i].split(":")[1]));
                else if (type.equals("enchant")) {
                    if (itemType.equals(Material.ENCHANTED_BOOK)) {
                        int enchantAmount = (split[i].split(":").length) / 2;
                        int d = 0;
                        while (d < enchantAmount) {
                            builder.addBookEnchantment(Enchantment.getByName(split[i].split(":")[d + 1].toUpperCase()), Integer.valueOf(split[i].split(":")[d + 2]));
                            d += 2;
                        }
                    } else {
                        int enchantAmount = (split[i].split(":").length) / 2;
                        int d = 0;
                        while (d < enchantAmount) {
                            builder.addEnchantment(Enchantment.getByName(split[i].split(":")[d + 1].toUpperCase()), Integer.valueOf(split[i].split(":")[d + 2]));
                            d += 2;
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    public static String getItemStackString(ItemStack item) {
        String itemString = item.getType().name() + (item.getDurability() != 0 ? ":" + item.getDurability() : "") + " : " + item.getAmount();
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) itemString += " : name:" + meta.getDisplayName();
        if (meta.hasLore()) for (String lore : item.getItemMeta().getLore()) itemString += " : lore:" + lore;
        if (meta.hasEnchants()) for (Enchantment ench : meta.getEnchants().keySet())
            itemString += " : enchant:" + ench.getName() + ":" + meta.getEnchants().get(ench);
        return itemString;
    }


    /**
     * Insert line-breaks into the text so that each line has maximum number of words.
     *
     * @param text         the text to insert line-breaks into
     * @param wordsPerLine maximum number of words per line
     * @return a new text with linebreaks
     */
    public static String splitString(String text, int wordsPerLine) {
        String WHITESPACE = " ";
        String LINEBREAK = System.getProperty("line.separator");
        final StringBuilder newText = new StringBuilder();

        final StringTokenizer wordTokenizer = new StringTokenizer(text);
        long wordCount = 1;
        while (wordTokenizer.hasMoreTokens()) {
            newText.append(wordTokenizer.nextToken());
            if (wordTokenizer.hasMoreTokens()) {
                if (wordCount++ % wordsPerLine == 0) {
                    newText.append(LINEBREAK);
                } else {
                    newText.append(WHITESPACE);
                }
            }
        }
        return newText.toString();
    }

    /**
     * Returns a potion with added effects
     *
     * @param is        The input ItemStack
     * @param effect    The potion's effect
     * @param duration  The potion's effect duration in seconds
     * @param amplifier The potion's effect level
     * @return The potion with an added effect
     */
    public static ItemStack addPotionEffect(ItemStack potion, PotionEffectType effect, int duration, int amplifier, boolean isSplashable) {
        PotionMeta pm = (PotionMeta) potion.getItemMeta();
        pm.addCustomEffect(new PotionEffect(effect, duration * 20, amplifier - 1), true);
        potion.setItemMeta(pm);
        Potion po = new Potion((byte) 16427);
        po.setSplash(isSplashable);
        po.apply(potion);
        return potion;
    }

    public static ItemStack addGlow(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = null;
        if (!nmsStack.hasTag()) {
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        }
        if (tag == null) tag = nmsStack.getTag();
        NBTTagList ench = new NBTTagList();
        tag.set("ench", ench);
        nmsStack.setTag(tag);
        return CraftItemStack.asCraftMirror(nmsStack);
    }

    public static boolean checkNumbers(String... x) {
        try {
            for (String o : x) Integer.parseInt(o);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static Date parseTime(String time) {
        try {
            String[] frag = time.split("-");
            if (frag.length < 2) {
                return new Date();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(frag[0] + "-" + frag[1] + "-" + frag[2]);
        } catch (Exception e) {
        }
        return new Date();
    }

    public static Date parseReflexTime(String time) {
        try {
            String[] frag = time.split("-");
            if (frag.length < 2) {
                return new Date();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            return dateFormat.parse(frag[0] + "-" + frag[1] + "-" + frag[2]);
        } catch (Exception e) {
        }
        return new Date();
    }

    public static List<Player> getPlayers(Collection<SWOnlinePlayer> list) {
        List<Player> players = new ArrayList<Player>();
        for (SWOnlinePlayer player : list) if (player.getPlayer() != null) players.add(player.getPlayer());
        return players;
    }

    public static String getStringFromLocation(Location l, boolean center) {
        return l.getWorld().getName() + ", " + (l.getBlockX() + (center ? 0.5 : 0)) + ", " + (l.getBlockY() + (center ? 1 : 0)) + ", " + (l.getBlockZ() + (center ? 0.5 : 0)) + ", " + l.getYaw() + ", " + l.getPitch();
    }

    public static Location getLocationFromString(String l) {
        String[] split = l.split(", ");
        World w = Bukkit.getWorld(split[0]);
        double x = Double.valueOf(split[1]), y = Double.valueOf(split[2]), z = Double.valueOf(split[3]);
        float yaw = Float.valueOf(split[4]), pitch = Float.valueOf(split[5]);
        return new Location(w, x, y, z, yaw, pitch);
    }

    public static String getReadableLocationString(Location l, boolean center) {
        return "" + ChatColor.GREEN + (l.getBlockX() + (center ? 0.5 : 0)) + ChatColor.GRAY + ", " + ChatColor.GREEN + (l.getBlockY() + (center ? 1 : 0)) + ChatColor.GRAY + ", " + ChatColor.GREEN + (l.getBlockZ() + (center ? 0.5 : 0));
    }

    // TODO Change caging system
    public static void cageMenu(InventoryMenuBuilder menu, boolean full) {
        if (full) {
            for (int i = 0; i < menu.getInventory().getSize(); i++)
                menu.withItem(i, Skywars.instance.clickableItemManager.getEmptyItem());
            return;
        }
        for (int i = 0; i < 9; i++) menu.withItem(i, Skywars.instance.clickableItemManager.getEmptyItem());
        for (int i = menu.getInventory().getSize() - 9; i < menu.getInventory().getSize(); i++)
            menu.withItem(i, Skywars.instance.clickableItemManager.getEmptyItem());
        int rows = (menu.getInventory().getSize() / 9) - 2;
        if (rows < 1) return;
        for (int i = 9; i < ((9 * rows) + 1); i += 9)
            menu.withItem(i, Skywars.instance.clickableItemManager.getEmptyItem());
        for (int i = 17; i < (9 * (rows + 1)); i += 9)
            menu.withItem(i, Skywars.instance.clickableItemManager.getEmptyItem());
    }

    public static void cageInventory(Inventory inv, boolean full) {
        if (full) {
            for (int i = 0; i < inv.getSize(); i++)
                inv.setItem(i, Skywars.instance.clickableItemManager.getEmptyItem());
            return;
        }
        for (int i = 0; i < 9; i++) inv.setItem(i, Skywars.instance.clickableItemManager.getEmptyItem());
        for (int i = inv.getSize() - 9; i < inv.getSize(); i++)
            inv.setItem(i, Skywars.instance.clickableItemManager.getEmptyItem());
        int rows = (inv.getSize() / 9) - 2;
        if (rows < 1) return;
        for (int i = 9; i < ((9 * rows) + 1); i += 9)
            inv.setItem(i, Skywars.instance.clickableItemManager.getEmptyItem());
        for (int i = 17; i < (9 * (rows + 1)); i += 9)
            inv.setItem(i, Skywars.instance.clickableItemManager.getEmptyItem());
    }

    public static int getInventorySize(int amount) {
        return amount < 10 ? 9 : amount < 19 ? 18 : amount < 28 ? 27 : amount < 37 ? 36 : amount < 46 ? 45 : 54;
    }

    public static boolean compareItem(ItemStack item1, ItemStack item2) {
        return item1 != null && item2 != null && item1.getType().equals(item2.getType()) && item1.getItemMeta().equals(item2.getItemMeta());
    }

    public static ItemStack getSkull(String name, String displayName) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (Skywars.instance.config.loadSkinsOnSkulls) meta.setOwner(name);
        meta.setDisplayName(displayName);
        skull.setItemMeta(meta);
        return skull;
    }

    public static String getPercentageString(int percentage) {
        return "" + (percentage < 40 ? ChatColor.RED : percentage < 80 ? ChatColor.YELLOW : ChatColor.GREEN) + percentage + "%";
    }

    protected static String randomize(HashMap<String, Integer> list) {
        int total = 0;
        for (int chance : list.values()) total += chance;
        int num = Skywars.instance.r.nextInt(total);
        int counter = 0;
        for (String key : list.keySet()) {
            counter += list.get(key);
            if (num >= total - counter) return key;
        }
        return null;
    }

    public static String getRandomServer(String gameMode) {
        ArrayList<String> servers = new ArrayList<String>();
        if (gameMode.equalsIgnoreCase("solo")) {
            servers.add("SoloSkywars01");
            servers.add("SoloSkywars02");
            //servers.add("SoloSkywars03");
            //servers.add("SoloSkywars04");
        } else {
            servers.add("DuoSkywars01");
            //servers.add("DuoSkywars02");
        }
        String currentServer = Bukkit.getWorldContainer().getAbsolutePath().split("/")[4];
        String selectedServer = "";
        for (String server : servers) {
            if (!server.equalsIgnoreCase(currentServer)) {
                selectedServer = server;
                return selectedServer;
            }
        }
        return selectedServer;
    }


    public static void setKnockback(Entity center, double radius) {
        setKnockback(center.getLocation(), radius);
    }
    public static void setKnockback(Location target, double radius) {
        int maxHeight = 15;
        Collection<Entity> nearbyEntities = target.getWorld().getNearbyEntities(target, radius, radius, radius);
        if ((nearbyEntities == null) || nearbyEntities.isEmpty()) { return; }
        List<LivingEntity> validEntities = new ArrayList<>();
        for (Entity entity : nearbyEntities) {
            if (entity.isValid() && (entity instanceof LivingEntity)) {
                validEntities.add((LivingEntity) entity);
            }
        }
        for (LivingEntity entity : validEntities) {
            if ((entity instanceof Player)) {
                Player player = (Player) entity;
                if (player.isFlying()||player.isConversing()) {
                    continue;
                }
            }
            double distance = (maxHeight - entity.getLocation().distance(target));
            double TWO_PI = 1.76 * Math.PI;
            Vector variantVel = entity.getLocation().getDirection().multiply(-1);
            variantVel = variantVel.setY(distance / TWO_PI);
            entity.setVelocity(variantVel);
        }
        return;
    }
    public static String getNextEventFormatted(NextEvent event) {
        if (event == NextEvent.REFILL) {
            return "Yenilenme";
        } else if (event == NextEvent.HELL) {
            return c("&cCehennem");
        }
        return "";
    }
    public static String c(String text) {
        if (text == null) { return ""; }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    public static ArenaMode getArenaMode(int teamSize) {
        if (teamSize == 1) {
            return ArenaMode.SOLO;
        } else {
            return ArenaMode.DUO;
        }
    }
}
