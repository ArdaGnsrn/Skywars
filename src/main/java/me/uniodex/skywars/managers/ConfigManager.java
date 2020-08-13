package me.uniodex.skywars.managers;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.logging.Level;

public class ConfigManager {

    private Skywars plugin;
    private HashMap<String, FileConfiguration> configurations = new HashMap<String, FileConfiguration>();
    private HashMap<String, String> messagesCache = new HashMap<>();

    public ConfigManager(Skywars plugin) {
        this.plugin = plugin;

        plugin.reloadConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();

        registerConfig("chests.yml");
        registerConfig("customization.yml");
        registerConfig("kits.yml");
        registerConfig("trails.yml");
        registerConfig("cages.yml");

        for (String fileName : configurations.keySet()) {
            reloadConfig(fileName);
            configurations.get(fileName).options().copyDefaults(true);
            saveConfig(fileName);
        }
    }

    // Get Configs
    public FileConfiguration getMainConfig() {
        return plugin.getConfig();
    }

    public FileConfiguration getChestsConfig() {
        return configurations.get("chests.yml");
    }

    public FileConfiguration getCustomizationConfig() {
        return configurations.get("customization.yml");
    }

    public FileConfiguration getKitsConfig() {
        return configurations.get("kits.yml");
    }

    public FileConfiguration getTrailsConfig() {
        return configurations.get("trails.yml");
    }

    public FileConfiguration getCagesConfig() {
        return configurations.get("cages.yml");
    }

    public FileConfiguration getArenaConfig(String arenaName) {
        try {
            File settingsFile = new File(plugin.getDataFolder() + "/arenas/" + arenaName, "settings.yml");
            FileConfiguration settingsEditor = YamlConfiguration.loadConfiguration(settingsFile);

            settingsEditor.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("arena-settings.yml"))));
            settingsEditor.options().copyDefaults(true);
            settingsEditor.save(settingsFile);
            return settingsEditor;
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Belirtilen arena bulunamadı. Arena adı: " + arenaName);
            e.printStackTrace();
            return null;
        }
    }

    public FileConfiguration getArenaLocations(String arenaName) {
        File locationsFile = new File(plugin.getDataFolder() + "/arenas/" + arenaName, "locations.dat");
        FileConfiguration locationsEditor = YamlConfiguration.loadConfiguration(locationsFile);
        return locationsEditor;
    }

    // Save Configs
    public void saveMainConfig() {
        plugin.saveConfig();
    }

    public void saveChestsConfig() {
        saveConfig("chests.yml");
    }

    public void saveCustomizationConfig() {
        saveConfig("customization.yml");
    }

    public void saveKitsConfig() {
        saveConfig("kits.yml");
    }

    public void saveTrailsConfig() {
        saveConfig("trails.yml");
    }

    public void saveCagesConfig() {
        saveConfig("cages.yml");
    }


    private void registerConfig(String name) {
        configurations.put(name, YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), name)));
    }

    private void reloadConfig(String fileName) {
        InputStream inputStream = plugin.getResource(fileName);
        if (inputStream != null) {
            InputStreamReader reader = new InputStreamReader(inputStream);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
            configurations.get(fileName).setDefaults(defConfig);
            try {
                reader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveConfig(String fileName) {
        try {
            configurations.get(fileName).save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException ex) {
            Bukkit.getConsoleSender().sendMessage(plugin.customization.prefix + "Couldn't save " + fileName + "!");
        }
    }

    public void deleteFile(File path) {
        if (path.exists()) {
            for (File f : path.listFiles()) {
                if (f.isDirectory()) deleteFile(f);
                else f.delete();
            }
        }
        path.delete();
    }

    public void copyFile(File source, File target) {
        try {
            if (source.isDirectory()) {
                if (!target.exists()) target.mkdirs();
                String[] files = source.list();
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(target, file);
                    copyFile(srcFile, destFile);
                }

            } else {
                FileInputStream inputStream = new FileInputStream(source);
                FileOutputStream outputStream = new FileOutputStream(target);
                FileChannel inChannel = inputStream.getChannel();
                FileChannel outChannel = outputStream.getChannel();
                try {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inChannel != null) inChannel.close();
                    if (outChannel != null) outChannel.close();
                    inputStream.close();
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(plugin.customization.prefix + "Failed to copy files!");
            e.printStackTrace();
        }
    }

    public long getSize(File file) {
        long length = 0;
        if (file.isDirectory()) {
            for (String f : file.list()) length += getSize(new File(file, f));
        } else length = file.length();
        return length;
    }

    public String getMessage(String path) {
        if (messagesCache.containsKey(path)) {
            return messagesCache.get(path);
        }
        String result = Utils.c(getCustomizationConfig().getString("Messages." + path));
        messagesCache.put(path, result);
        return result;
    }
}
