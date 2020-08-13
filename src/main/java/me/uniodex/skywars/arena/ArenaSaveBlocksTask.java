package me.uniodex.skywars.arena;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;

@SuppressWarnings("deprecation")
//TODO Recode
public class ArenaSaveBlocksTask {

    public ArenaSaveBlocksTask(final Skywars plugin, final Player p, final String arenaName, final Cuboid cuboid) {

        final ArrayList<String> chests = new ArrayList<String>();
        final Iterator<Block> iterator = cuboid.iterator();

        new BukkitRunnable() {
            int scannedBlocks = 0;
            int temp_counter = 0;
            int runs = 0;
            int scanSpeed = plugin.config.rollback_scan_speed;
            double totalBlocks = cuboid.getSize();

            public void run() {
                while (iterator.hasNext() && temp_counter < scanSpeed) {
                    Block b = iterator.next();
                    if (b.getType() != Material.AIR) {
                        if (b.getType().equals(Material.CHEST))
                            chests.add(b.getX() + ":" + b.getY() + ":" + b.getZ() + ":" + b.getTypeId() + ":" + b.getData() + ":Default:0");
                    }
                    temp_counter++;
                }
                scannedBlocks += temp_counter;
                temp_counter = 0;
                runs++;
                if (runs == plugin.config.rollback_send_status_update_every) {
                    sendUpdate(p, plugin, scannedBlocks, totalBlocks, chests.size(), scanSpeed, (int) ((scannedBlocks / totalBlocks) * 100), (totalBlocks - scannedBlocks) / (scanSpeed * 20));
                    runs = 0;
                }
                if (!iterator.hasNext()) {
                    cancel();
                    saveWorld(cuboid.worldName, plugin);
                    sendUpdate(p, plugin, (int) totalBlocks, totalBlocks, chests.size(), scanSpeed, 100, 0);
                    File locationsFile = new File(plugin.getDataFolder() + "/arenas/" + arenaName, "locations.dat");
                    FileConfiguration locationsEditor = YamlConfiguration.loadConfiguration(locationsFile);
                    locationsEditor.set("Cuboid", cuboid.toString());
                    locationsEditor.set("Chests", chests.toString());
                    try {
                        locationsEditor.save(locationsFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    boolean exists = plugin.arenaManager.getArenas().containsKey(arenaName.toLowerCase());
                    new Arena(plugin, arenaName);
                    if (!exists)
                        p.sendMessage(plugin.customization.prefix + "Arena " + ChatColor.AQUA + arenaName + ChatColor.GRAY + " has been " + ChatColor.GREEN + "successfully" + ChatColor.GRAY + " created!");
                    else p.sendMessage(plugin.customization.prefix + "Arena region has been updated!");
                }
            }
        }.runTaskTimer(plugin, 0, 1);

    }

    private void saveWorld(String worldName, Skywars plugin) {
        File serverDir = Bukkit.getWorldContainer();
        File backupWorldDir = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "arenas" + File.separator + worldName + File.separator + "world");
        File arenaDir = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "arenas" + File.separator + worldName);
        File worldDir = new File(serverDir.getAbsolutePath() + File.separator + worldName);

        if (Bukkit.getWorld(worldName) != null) {
            if (Bukkit.getWorld(worldName).getPlayers().size() > 0) {
                for (Player wp : Bukkit.getWorld(worldName).getPlayers()) {
                    wp.teleport(plugin.lobbyLocation);
                }
            }
            if (!Bukkit.getServer().unloadWorld(Bukkit.getWorld(worldName), false)) {
                Bukkit.getLogger().log(Level.SEVERE, "Dünya kaydedilirken bir sorun oluştu! Hata Kodu: A1");
            }
        }

        if (backupWorldDir.exists()) {
            try {
                FileUtils.deleteDirectory(backupWorldDir);
            } catch (IOException e) {
                e.printStackTrace();
                Bukkit.getLogger().log(Level.SEVERE, "Dünya kaydedilirken bir sorun oluştu! Hata Kodu: A2");
            }
        }

        try {
            FileUtils.copyDirectoryToDirectory(worldDir, arenaDir);  // Dünya klasörünü arena klasörüne olduğu gibi aktarıyoruz
            FileUtils.moveDirectory(new File(arenaDir.getAbsolutePath() + File.separator + worldName), backupWorldDir); // Arena klasörünün içindeki dünya klasörünün adını backupWorldDir'deki gibi yapıyoruz
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Dünya kaydedilirken bir sorun oluştu! Hata Kodu: A3");
        }

        if (Bukkit.getWorld(worldName) == null) {
            Bukkit.createWorld(new WorldCreator(worldName).generator(new ChunkGenerator() {
                public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.BiomeGrid biomeGrid) {
                    return new byte[world.getMaxHeight() / 16][];
                }
            }));
        } else {
            Bukkit.getLogger().log(Level.SEVERE, "Dünya kaydedilirken bir sorun oluştu! Hata Kodu: A4");
        }
    }

    private void sendUpdate(Player p, Skywars plugin, int scannedBlocks, double totalBlocks, int detectedChests, int scanSpeed, int scanPercentage, double timeLeft) {
        p.sendMessage(ChatColor.GRAY + "=======================");
        p.sendMessage(ChatColor.GRAY + "- Scanned blocks: " + ChatColor.AQUA + scannedBlocks);
        p.sendMessage(ChatColor.GRAY + "- Total blocks: " + ChatColor.YELLOW + (int) totalBlocks);
        p.sendMessage(ChatColor.GRAY + "- Detected chests: " + ChatColor.AQUA + detectedChests);
        p.sendMessage(ChatColor.GRAY + "- Scanning Speed: " + ChatColor.GREEN + scanSpeed + " Block/Tick");
        p.sendMessage(ChatColor.GRAY + "- Scanned Percentage: " + Utils.getPercentageString(scanPercentage));
        p.sendMessage(ChatColor.GRAY + "- Time left: " + ChatColor.LIGHT_PURPLE + timeLeft + "s");
        p.sendMessage(ChatColor.GRAY + "=======================");
    }

}
