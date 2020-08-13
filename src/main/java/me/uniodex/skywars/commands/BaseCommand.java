package me.uniodex.skywars.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.arena.Arena;
import me.uniodex.skywars.arena.ArenaSaveBlocksTask;
import me.uniodex.skywars.arena.Cuboid;
import me.uniodex.skywars.enums.ArenaState;
import me.uniodex.skywars.enums.SpecialCharacter;
import me.uniodex.skywars.managers.arena.ArenaStartManager;
import me.uniodex.skywars.player.SWOnlinePlayer;
import me.uniodex.skywars.utils.ItemStackBuilder;
import me.uniodex.skywars.utils.Utils;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

// TODO Seperate every command to it's own class after 4.0
// TODO Get rid of unnecessary commands and features
public class BaseCommand implements CommandExecutor {

    private Skywars plugin;

    public BaseCommand(Skywars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player p = null;
        SWOnlinePlayer swPlayer = null;
        if (sender instanceof Player) {
            p = (Player) sender;
            swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());
        }

        if (commandLabel.equalsIgnoreCase("skywars") || commandLabel.equalsIgnoreCase("sw")) {
            //Command sound
            if (p != null) {
                p.playSound(p.getLocation(), plugin.CLICK, 1, 1);
            }

            if (args.length == 0) {
                ChatColor c = plugin.colors[plugin.r.nextInt(plugin.colors.length)];
                sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "-------------" + ChatColor.YELLOW + " Skywars " + ChatColor.GRAY + "[" + plugin.getDescription().getVersion() + "] " + c + "" + ChatColor.STRIKETHROUGH + "-------------");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/Skywars | sw " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Shows a list of commands");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Leave " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Removes you from the game!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Autojoin " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Puts you in the best available arena!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw List " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Shows a list of arenas and other information!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw " + (sender.hasPermission("skywars.admin") ? ChatColor.GREEN : ChatColor.RED) + "Admin " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Shows a list of admin commands");
                sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "--------------------------------------");
                return true;
            }

            String command = args[0].toLowerCase();

            if (command.equals("admin")) {
                if (!checkSender(sender, false, "skywars.admin")) return true;
                ChatColor c = plugin.colors[plugin.r.nextInt(plugin.colors.length)];
                sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "------------" + ChatColor.YELLOW + " Skywars " + ChatColor.RED + "Admin " + c + "" + ChatColor.STRIKETHROUGH + "------------");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Setlobby " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Sets the lobby location!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Wand " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Gives you a selection wand!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Create " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Creates a new arena!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Delete " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Deletes an existing arena!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Addspawn " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Adds a spawnpoint to an arena!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Removespawn " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Removes a spawnpoint from an arena!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Start/Stop " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Forces an arena to start/stop");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Setspectators " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Sets the location that spectators teleport to");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Coins " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Modifies a player coins!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Setmodifier " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Changes a player modifier!");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Reset " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Resets a player stats");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Edit " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Allows arena modifications");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Editmode " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Allows the user to modify surroundings in bungeemode");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Reload " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Reloads the plugin");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw Updateregion " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Updates an arena region");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw ChestManager " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Allows to modify chest types");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw WorldManager " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Shows a list of World management commands");
                sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "-------------------------------------");
                return true;
            }

            if (command.equals("leave")) {
                if (!checkSender(sender, true, "")) return true;
                p.openInventory(plugin.menuManager.getQuitInventory());
                return true;
            }

            if (command.equals("autojoin")) {
                if (!checkSender(sender, true, "")) return true;
                p.performCommand("yenioyunagir");
                return true;
            }

            if (command.equals("join")) {
                if (args.length >= 2) {
                    if (!checkSender(sender, true, "")) return true;
                    if (plugin.arenaManager.getArena(args[1]) != null) {
                        plugin.arenaManager.getArena(args[1]).joinPlayer(plugin.playerManager.getSWOnlinePlayer(sender.getName()), false);
                    }
                }
                return true;
            }

            if (command.equals("setlobby")) {
                if (!checkSender(sender, true, "skywars.setlobby")) return true;
                plugin.lobbyLocation = p.getLocation();
                plugin.getConfig().set("Lobby", Utils.getStringFromLocation(plugin.lobbyLocation, true));
                plugin.saveConfig();
                p.sendMessage(plugin.customization.prefix + "Lobby has been " + ChatColor.GREEN + "successfully" + ChatColor.GRAY + " set at " + Utils.getReadableLocationString(plugin.lobbyLocation, true));
                return true;
            }

            if (command.equals("markasstop")) {
                if (!checkSender(sender, false, "skywars.stopserver")) return true;
                if (plugin.serverWillStop == true) {
                    plugin.serverWillStop = false;
                    sender.sendMessage(plugin.customization.prefix + "Sunucunun kapatılacak işareti kaldırıldı.");
                } else {
                    plugin.serverWillStop = true;
                    sender.sendMessage(plugin.customization.prefix + "Sunucu kapatılacak olarak işaretlendi.");
                }
                return true;
            }

            if (command.equals("showteam")) {
                if (!checkSender(sender, true, "skywars.showteam")) return true;
                p.sendMessage(swPlayer.getArena().getTeam(swPlayer).toString());
                return true;
            }

            if (command.equals("disableall")) {
                if (!checkSender(sender, false, "skywars.disableall")) return true;
                for (Arena arena : plugin.arenaManager.getArenas().values()) {
                    arena.setEnabled(false);
                }
                sender.sendMessage("Tüm arenalar devre dışı bırakıldı.");
                return true;
            }

            if (command.equals("enableall")) {
                if (!checkSender(sender, false, "skywars.enableall")) return true;
                for (Arena arena : plugin.arenaManager.getArenas().values()) {
                    arena.setEnabled(true);
                }
                sender.sendMessage("Tüm arenalar etkinleştirildi.");
                return true;
            }

            if (command.equals("wand")) {
                if (!checkSender(sender, true, "skywars.wand")) return true;
                if (!plugin.selectionMode.containsKey(p.getName())) {
                    p.getInventory().addItem(plugin.clickableItemManager.getWandItem());
                    plugin.selectionMode.put(p.getName(), new Location[2]);
                    p.sendMessage(plugin.customization.prefix + "You have entered the selection mode!");
                } else {
                    p.getInventory().removeItem(plugin.clickableItemManager.getWandItem());
                    plugin.selectionMode.remove(p.getName());
                    p.sendMessage(plugin.customization.prefix + "You have left the selection mode!");
                }
                return true;
            }

            if (command.equals("create")) {
                if (!checkSender(sender, true, "skywars.create")) return true;
                if (args.length < 5 || !Utils.checkNumbers(args[2], args[3], args[4]) || Integer.valueOf(args[2]) < 1 || Integer.valueOf(args[3]) < 2 || Integer.valueOf(args[4]) < Integer.valueOf(args[3])) {
                    sendCommandUsage(sender, "Create", "<Name> <teamSize> <minTeams> <maxTeams>", "Team size is the amount of players in each team, if you put 1 for example, the arena will be in Solo mode",
                            "Min teams is the minimum amount of teams for the arena to start", "Max teams is the maximum amount of teams the arena can handle");
                    return true;
                }
                String arenaName = args[1];
                if (plugin.arenaManager.getArenas().containsKey(arenaName)) {
                    p.sendMessage(plugin.customization.prefix + "There is already an arena with that name!");
                    return true;
                }
                if (!plugin.selectionMode.containsKey(p.getName()) || plugin.selectionMode.get(p.getName())[0] == null || plugin.selectionMode.get(p.getName())[1] == null) {
                    p.sendMessage(plugin.customization.prefix + "You haven't selected the 2 corners yet!");
                    return true;
                }
                Cuboid cuboid = new Cuboid(plugin.selectionMode.get(p.getName())[0], plugin.selectionMode.get(p.getName())[1]);
                if (cuboid.getSize() > plugin.config.maxArenaSize) {
                    p.sendMessage(plugin.customization.prefix + "Your current selection exceeds the max arena size set in the config! " + ChatColor.LIGHT_PURPLE + "(" + cuboid.getSize() + "/" + plugin.config.maxArenaSize + ")!");
                    return true;
                }
                p.sendMessage(plugin.customization.prefix + "Creating the arena " + ChatColor.AQUA + arenaName + ChatColor.GRAY + "!");
                File settingsFile = new File(plugin.getDataFolder() + "/arenas/" + arenaName, "settings.yml");
                FileConfiguration editor = YamlConfiguration.loadConfiguration(settingsFile);

                editor.set("team-size", Integer.valueOf(args[2]));
                editor.set("min-teams", Integer.valueOf(args[3]));
                editor.set("max-teams", Integer.valueOf(args[4]));

                try {
                    editor.save(settingsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new ArenaSaveBlocksTask(plugin, p, arenaName, cuboid);
                return true;
            }

            if (command.equals("delete")) {
                if (!checkSender(sender, false, "skywars.delete")) return true;
                if (args.length == 1) {
                    sendCommandUsage(sender, "Delete", "<Name>");
                    return true;
                }
                String arenaName = args[1].toLowerCase();
                if (!plugin.arenaManager.getArenas().containsKey(arenaName)) {
                    sender.sendMessage(plugin.customization.prefix + "Couldn't find an arena with that name!");
                    return true;
                }
                Arena arena = plugin.arenaManager.getArenas().get(arenaName);
                arena.stop();
                File arenaFile = new File(plugin.getDataFolder() + "/arenas/", arena.getArenaName());
                plugin.configManager.deleteFile(arenaFile);
                plugin.arenaManager.removeArena(arenaName);
                sender.sendMessage(plugin.customization.prefix + "Arena has been deleted!");
                return true;
            }

            if (command.equals("list")) {
                ChatColor c = plugin.colors[plugin.r.nextInt(plugin.colors.length)];
                sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "----------------" + ChatColor.YELLOW + " List " + c + "" + ChatColor.STRIKETHROUGH + "----------------");
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "Players: " + ChatColor.GREEN + plugin.playerManager.getOnlinePlayers().size());
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "Bukkit: " + ChatColor.GREEN + Bukkit.getBukkitVersion());
                sender.sendMessage(c + " - " + ChatColor.YELLOW + "Loaded arenas: " + ChatColor.GREEN + plugin.arenaManager.getArenas().size());
                for (Arena arena : plugin.arenaManager.getArenas().values()) {
                    sender.sendMessage(c + " - " + ChatColor.YELLOW + arena.getArenaName() + c + " | State: " + arena.getArenaState().getStateName() + c + " | Players: " + ChatColor.LIGHT_PURPLE + (arena.getPlayers().size() - arena.getSpectators().size()) + c + " | Spawnpoints: " + ChatColor.AQUA + arena.getCurrentTeamAmount() + c + " | Chests: " + ChatColor.GREEN + arena.getChests().size() + c + " | Team size: " + ChatColor.GOLD + arena.getPlayerSizePerTeam() + c + " | Enabled: " + (arena.isEnabled() ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
                }
                sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "-------------------------------------");
                return true;
            }

            if (command.equals("addspawn")) {
                if (!checkSender(sender, true, "skywars.addspawn")) return true;
                if (args.length == 1) {
                    sendCommandUsage(sender, "Addspawn", "<Name>");
                    return true;
                }
                String arenaName = args[1].toLowerCase();
                if (!plugin.arenaManager.getArenas().containsKey(arenaName)) {
                    p.sendMessage(plugin.customization.prefix + "Couldn't find an arena with that name!");
                    return true;
                }
                Arena arena = plugin.arenaManager.getArenas().get(arenaName);
                File locationsFile = new File(plugin.getDataFolder() + "/arenas/" + arena.getArenaName(), "locations.dat");
                FileConfiguration locationsEditor = YamlConfiguration.loadConfiguration(locationsFile);
                int spawnId = arena.getCurrentTeamAmount() + 1;
                String location = Utils.getStringFromLocation(p.getLocation(), true);
                locationsEditor.set("Spawnpoints." + spawnId, location);
                try {
                    locationsEditor.save(locationsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                arena.registerTeam(String.valueOf(spawnId), Utils.getLocationFromString(location));
                p.sendMessage(plugin.customization.prefix + "You have added spawnpoint " + ChatColor.LIGHT_PURPLE + "#" + spawnId + ChatColor.GRAY + " to the arena " + ChatColor.AQUA + arena.getArenaName() + ChatColor.GRAY + " at " + Utils.getReadableLocationString(p.getLocation(), true));
                return true;
            }

            if (command.equals("removespawn")) {
                if (!checkSender(sender, false, "skywars.removespawn")) return true;
                if (args.length == 1) {
                    sendCommandUsage(sender, "Removespawn", "<Name>");
                    return true;
                }
                String arenaName = args[1].toLowerCase();
                if (!plugin.arenaManager.getArenas().containsKey(arenaName)) {
                    sender.sendMessage(plugin.customization.prefix + "Couldn't find an arena with that name!");
                    return true;
                }
                Arena arena = plugin.arenaManager.getArenas().get(arenaName);
                if (arena.getTeams().isEmpty()) {
                    sender.sendMessage(plugin.customization.prefix + "That arena doesn't have any spawnpoints!");
                    return true;
                }
                File locationsFile = new File(plugin.getDataFolder() + "/arenas/" + arena.getArenaName(), "locations.dat");
                FileConfiguration locationsEditor = YamlConfiguration.loadConfiguration(locationsFile);
                int spawnId = arena.getTeams().size();
                locationsEditor.set("Spawnpoints." + spawnId, null);
                try {
                    locationsEditor.save(locationsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (final Team team : arena.getTeams().keySet()) {
                    if (team.getName().equals(String.valueOf(spawnId))) {
                        arena.removeTeam(team);
                        break;
                    }
                }
                sender.sendMessage(plugin.customization.prefix + "You have removed the last spawnpoint!");
                return true;
            }

            if (command.equals("setspectators")) {
                if (!this.checkSender(sender, true, "skywars.setspectators")) {
                    return true;
                }
                if (args.length < 2) {
                    this.sendCommandUsage(sender, "Setspectators", "<Arena>");
                    return true;
                }
                final String lowerCase5 = args[1].toLowerCase();
                if (!plugin.arenaManager.getArenas().containsKey(lowerCase5)) {
                    sender.sendMessage(plugin.customization.prefix + "Couldn't find an arena with that name!");
                    return true;
                }
                final Player player8 = (Player) sender;
                final Arena arena5 = plugin.arenaManager.getArenas().get(lowerCase5);
                final File file4 = new File(plugin.getDataFolder() + "/arenas/" + arena5.getArenaName(), "locations.dat");
                final YamlConfiguration loadConfiguration4 = YamlConfiguration.loadConfiguration(file4);
                loadConfiguration4.set("Spectators-Spawnpoint", Utils.getStringFromLocation(player8.getLocation(), true));
                try {
                    loadConfiguration4.save(file4);
                } catch (IOException ex4) {
                    ex4.printStackTrace();
                }
                arena5.setSpectatorsLocation(player8.getLocation().getBlock().getLocation().add(0.5, 1.0, 0.5));
                player8.sendMessage(plugin.customization.prefix + "You have set the spectators spawnpoint for the arena " + ChatColor.LIGHT_PURPLE + arena5.getArenaName() + ChatColor.GRAY + "!");
                return true;
            }

            if (command.equals("start")) {
                if (!checkSender(sender, false, "skywars.start")) return true;
                String arenaName;
                if (args.length == 1) {
                    if (swPlayer.getArena() == null) {
                        sendCommandUsage(sender, "Start", "<Name>");
                        return true;
                    } else arenaName = swPlayer.getArena().getArenaName();
                } else arenaName = args[1];
                if (!plugin.arenaManager.getArenas().containsKey(arenaName)) {
                    sender.sendMessage(plugin.customization.prefix + "Couldn't find an arena with that name!");
                    return true;
                }
                Arena arena = plugin.arenaManager.getArenas().get(arenaName);
                if (!arena.getArenaState().isAvailable()) {
                    sender.sendMessage(plugin.customization.prefix + "You may not start the arena in its current state!");
                    return true;
                }
                if (arena.getAliveTeams().size() < 2) {
                    sender.sendMessage(plugin.customization.prefix + "There must be at least 2 teams for the arena to start!");
                    return true;
                }
                new ArenaStartManager(arena, plugin);
                //arena.start();
                sender.sendMessage(plugin.customization.prefix + "You have forced the arena " + ChatColor.YELLOW + arena.getArenaName() + ChatColor.GRAY + " to start!");
                return true;
            }
            if (command.equals("ucur")) {
                p.setVelocity(p.getLocation().add(0, 5, 0).getDirection().multiply(5));
                p.getWorld().createExplosion(p.getLocation(), 0);
            }
            if (command.equalsIgnoreCase("iteminfo")) {
                ItemStack item = p.getItemInHand();
                if (item == null) { return true;}
                p.sendMessage(item.getType().toString());
            }
            if (command.equals("stop")) {
                if (!checkSender(sender, false, "skywars.stop")) return true;
                String arenaName;
                if (args.length == 1) {
                    if (!(sender instanceof Player && swPlayer.getArena() != null)) {
                        sendCommandUsage(sender, "Start", "<Name>");
                        return true;
                    } else arenaName = swPlayer.getArena().getArenaName();
                } else arenaName = args[1];
                if (!plugin.arenaManager.getArenas().containsKey(arenaName)) {
                    sender.sendMessage(plugin.customization.prefix + "Couldn't find an arena with that name!");
                    return true;
                }
                Arena arena = plugin.arenaManager.getArenas().get(arenaName);
                if (!arena.getArenaState().equals(ArenaState.INGAME) && !arena.getArenaState().equals(ArenaState.ENDING)) {
                    sender.sendMessage(plugin.customization.prefix + "You may not stop the arena in its current state!");
                    return true;
                }
                arena.stop();
                sender.sendMessage(plugin.customization.prefix + "You have forced the arena " + ChatColor.YELLOW + arena.getArenaName() + ChatColor.GRAY + " to stop!");
                return true;
            }

            if (command.equals("edit")) {
                if (!checkSender(sender, true, "skywars.edit")) return true;
                if (args.length == 1) {
                    sendCommandUsage(sender, "Edit", "<Name>");
                    return true;
                }
                String arenaName = args[1];
                if (!plugin.arenaManager.getArenas().containsKey(arenaName)) {
                    p.sendMessage(plugin.customization.prefix + "Couldn't find an arena with that name!");
                    return true;
                }
                p.openInventory(plugin.menuManager.getEditor(arenaName));
                return true;
            }

            if (command.equals("editmode")) {
                if (!checkSender(sender, true, "skywars.editmode")) return true;
                if (plugin.playerManager.getOnlinePlayers().containsKey(p.getName())) {
                    p.sendMessage(plugin.customization.prefix + "You have " + ChatColor.GREEN + "enabled" + ChatColor.GRAY + " the editing mode!");
                    String hubName = plugin.config.bungee_mode_hub;
                    plugin.config.bungee_mode_hub = String.valueOf(plugin.r.nextInt());
                    plugin.playerManager.leave(p.getName());
                    plugin.config.bungee_mode_hub = hubName;
                    plugin.playerManager.editMode.add(p.getName());
                } else {
                    p.sendMessage(plugin.customization.prefix + "You have " + ChatColor.RED + "disabled" + ChatColor.GRAY + " the editing mode!");
                    p.kickPlayer("Lobiye yönlendirildiniz.");
                    plugin.playerManager.editMode.remove(p.getName());
                }
                return true;
            }

            if (command.equals("updateregion")) {
                if (!checkSender(sender, true, "skywars.updateregion")) return true;
                if (args.length == 1) {
                    sendCommandUsage(sender, "Updateregion", "<Arena>");
                    return true;
                }

                String arenaName = args[1];
                if (!plugin.arenaManager.getArenas().containsKey(arenaName)) {
                    p.sendMessage(plugin.customization.prefix + "Couldn't find an arena with that name!");
                    return true;
                }
                if (!plugin.selectionMode.containsKey(p.getName()) || plugin.selectionMode.get(p.getName())[0] == null || plugin.selectionMode.get(p.getName())[1] == null) {
                    p.sendMessage(plugin.customization.prefix + "You haven't selected the 2 corners yet!");
                    return true;
                }
                Cuboid cuboid = new Cuboid(plugin.selectionMode.get(p.getName())[0], plugin.selectionMode.get(p.getName())[1]);
                if (cuboid.getSize() > plugin.config.maxArenaSize) {
                    p.sendMessage(plugin.customization.prefix + "Your current selection exceeds the max arena size set in the config! " + ChatColor.LIGHT_PURPLE + "(" + cuboid.getSize() + "/" + plugin.config.maxArenaSize + ")!");
                    return true;
                }
                p.sendMessage(plugin.customization.prefix + "Updating the arena region...");
                Arena arena = plugin.arenaManager.getArenas().get(arenaName);
                arena.setEnabled(false);
                arena.stop();
                new ArenaSaveBlocksTask(plugin, p, arena.getArenaName(), cuboid);
                return true;
            }

            if (command.equals("chestmanager") || command.equals("cm")) {
                if (!checkSender(sender, true, "skywars.chestmanager")) return true;
                if (args.length == 1) {
                    ChatColor c = plugin.colors[plugin.r.nextInt(plugin.colors.length)];
                    p.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "------------" + ChatColor.YELLOW + " Skywars " + ChatColor.RED + "ChestManager " + c + "" + ChatColor.STRIKETHROUGH + "------------");
                    p.sendMessage(c + " - " + ChatColor.YELLOW + "/sw ChestManager Tool <Type> <Spawnpoint>" + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Gives you a wand to change a chest type");
                    p.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------");
                    return true;
                }
                String subCommand = args[1].toLowerCase();
                final Player player17 = (Player) sender;

                if (subCommand.equals("tool")) {
                    if (args.length == 2) {
                        sendCommandUsage(sender, "ChestManager tool", "<Type> <Spawnpoint>");
                        return true;
                    }
                    final String lowerCase14 = args[2].toLowerCase();
                    final String lowerCase15 = args[3].toLowerCase();
                    if (!(lowerCase14.equalsIgnoreCase("Default") || lowerCase14.equalsIgnoreCase("Mid"))) {
                        player17.sendMessage(plugin.customization.prefix + "Couldn't find a chest type with that name!");
                        return true;
                    }
                    final ItemStack build = new ItemStackBuilder(plugin.clickableItemManager.getChestToolItem().clone()).addLore(" ", ChatColor.YELLOW + "Type: " + ChatColor.GOLD + lowerCase14).addLore(ChatColor.YELLOW + "Spawnpoint: " + ChatColor.GOLD + lowerCase15).build();
                    player17.getInventory().addItem(build);
                    player17.sendMessage(plugin.customization.prefix + "You have received the " + build.getItemMeta().getDisplayName() + ChatColor.GRAY + "!");
                    return true;
                }
                player17.sendMessage(plugin.customization.prefix + "Unknown command! use /sw ChestManager for a list of commands");
                return true;
            }

            if (command.equals("worldmanager") || command.equals("wm")) {
                if (!checkSender(sender, false, "skywars.worldmanager")) return true;
                if (args.length == 1) {
                    ChatColor c = plugin.colors[plugin.r.nextInt(plugin.colors.length)];
                    sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "------------" + ChatColor.YELLOW + " Skywars " + ChatColor.RED + "WorldManager " + c + "" + ChatColor.STRIKETHROUGH + "------------");
                    sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw WorldManager Create <World> " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Creates a new world");
                    sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw WorldManager Delete <World> " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Deletes an existing world");
                    sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw WorldManager Import <World> " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Imports a new world!");
                    sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw WorldManager Backup <World> " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Creates a backup of a world");
                    sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw WorldManager Restore <World> " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Restores a world from the backup file");
                    sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw WorldManager Tp <World> " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Teleports you to a world");
                    sender.sendMessage(c + " - " + ChatColor.YELLOW + "/ew WorldManager Setspawn <World> " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Sets the spawn of a world");
                    sender.sendMessage(c + " - " + ChatColor.YELLOW + "/ew WorldManager Backupall " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Creates a backup of all loaded worlds");
                    sender.sendMessage(c + " - " + ChatColor.YELLOW + "/sw WorldManager List " + c + SpecialCharacter.ARROW + ChatColor.GREEN + " Lists loaded worlds!");
                    sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------");
                    return true;
                }
                String subCommand = args[1].toLowerCase();

                if (subCommand.equals("create")) {
                    List<String> types = Arrays.asList("normal", "nether", "the_end", "empty");
                    if (args.length < 4 || !types.contains(args[3].toLowerCase())) {
                        sendCommandUsage(sender, "WorldManager Create", "<Name> <Type>", "Type can be normal, nether, the_end, empty");
                        return true;
                    }
                    String worldName = args[2];
                    String worldType = args[3].toLowerCase();
                    if (Bukkit.getWorld(worldName) != null) {
                        sender.sendMessage(plugin.customization.prefix + "There is a world with that name!");
                        return true;
                    }
                    for (String x : Bukkit.getWorldContainer().list()) {
                        if (x.equalsIgnoreCase(worldName)) {
                            sender.sendMessage(plugin.customization.prefix + "Seems like there is an unloaded world with that name! try using /sw WorldManager Import <Name>");
                            return true;
                        }
                    }
                    sender.sendMessage(plugin.customization.prefix + "Creating a new world!");
                    long before = System.currentTimeMillis();
                    if (worldType.equals("empty")) {
                        Bukkit.createWorld(new WorldCreator(worldName).generator(new ChunkGenerator() {
                            public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.BiomeGrid biomeGrid) {
                                return new byte[world.getMaxHeight() / 16][];
                            }
                        }));
                    } else
                        Bukkit.createWorld(new WorldCreator(worldName).environment(Environment.valueOf(worldType.toUpperCase())));
                    sender.sendMessage(plugin.customization.prefix + "World has been created! took " + ChatColor.LIGHT_PURPLE + (System.currentTimeMillis() - before) + "ms" + ChatColor.GRAY + " to complete the process! use " + ChatColor.GREEN + "/sw WorldManager Tp " + worldName + ChatColor.GRAY + " if you would like to be teleported to that world!");
                    return true;
                }

                if (subCommand.equals("delete")) {
                    if (args.length == 2) {
                        sendCommandUsage(sender, "WorldManager Delete", "<Name>");
                        return true;
                    }
                    String worldName = args[2].toLowerCase();
                    World w = Bukkit.getWorld(worldName);
                    File worldFile = null;
                    if (w != null) {
                        if (!w.getPlayers().isEmpty()) {
                            sender.sendMessage(plugin.customization.prefix + "The world contains players. so the world can't be deleted!");
                            for (Player x : w.getPlayers())
                                sender.sendMessage(plugin.customization.prefix + "- " + x.getName());
                            return true;
                        }
                        worldFile = w.getWorldFolder();
                        Bukkit.unloadWorld(w, false);
                    } else {
                        sender.sendMessage(plugin.customization.prefix + "The world you looking for seems to be unloaded, looking up folders...");
                        for (File f : Bukkit.getWorldContainer().listFiles()) {
                            if (f.getName().equalsIgnoreCase(args[2])) {
                                worldFile = f;
                                break;
                            }
                        }
                    }

                    if (worldFile != null) {
                        long before = System.currentTimeMillis();
                        plugin.configManager.deleteFile(worldFile);
                        sender.sendMessage(plugin.customization.prefix + "World has been deleted! took " + ChatColor.LIGHT_PURPLE + (System.currentTimeMillis() - before) + "ms!");
                    } else {
                        sender.sendMessage(plugin.customization.prefix + "Couldn't find a world with that name!");
                    }
                    return true;
                }

                if (subCommand.equals("import")) {
                    if (args.length == 2) {
                        sendCommandUsage(sender, "WorldManager Import", "<Name>");
                        return true;
                    }
                    String worldName = args[2];
                    if (Bukkit.getWorld(worldName) != null) {
                        sender.sendMessage(plugin.customization.prefix + "There is a loaded world with that name!");
                        return true;
                    }
                    for (String fileName : Bukkit.getWorldContainer().list()) {
                        if (worldName.equalsIgnoreCase(fileName)) {
                            sender.sendMessage(plugin.customization.prefix + "Importing the world...");
                            long before = System.currentTimeMillis();
                            if (plugin.config.emptyChunkGenerator) {
                                Bukkit.createWorld(new WorldCreator(fileName).generator(new ChunkGenerator() {
                                    public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.BiomeGrid biomeGrid) {
                                        return new byte[world.getMaxHeight() / 16][];
                                    }
                                }));
                            } else Bukkit.createWorld(new WorldCreator(fileName));
                            sender.sendMessage(plugin.customization.prefix + "World has been imported! check your logs! took " + ChatColor.LIGHT_PURPLE + (System.currentTimeMillis() - before) + "ms!");
                            return true;
                        }
                    }
                    sender.sendMessage(plugin.customization.prefix + "Couldn't find any world with that name!");
                    return true;
                }

                if (subCommand.equals("backup") || subCommand.equals("restore")) {
                    if (args.length == 2) {
                        sendCommandUsage(sender, "WorldManager " + subCommand.substring(0, 1).toUpperCase() + subCommand.substring(1), "<Name>");
                        return true;
                    }
                    final World w = Bukkit.getWorld(args[2]);
                    if (w == null) {
                        sender.sendMessage(plugin.customization.prefix + "Couldn't find a loaded world with that name!");
                        return true;
                    }

                    if (subCommand.equals("backup")) {
                        new BukkitRunnable() {
                            public void run() {
                                sender.sendMessage(plugin.customization.prefix + "Creating a backup of the world " + ChatColor.GREEN + w.getName() + ChatColor.GRAY + "!");
                                long before = System.currentTimeMillis();
                                w.save();
                                plugin.configManager.copyFile(w.getWorldFolder(), new File(plugin.getDataFolder() + "/backup", w.getName()));
                                sender.sendMessage(plugin.customization.prefix + "Backup has been " + ChatColor.GREEN + "successfully" + ChatColor.GRAY + " created! took " + ChatColor.LIGHT_PURPLE + (System.currentTimeMillis() - before) + "ms!");
                            }
                        }.runTaskAsynchronously(plugin);
                        return true;
                    }

                    if (subCommand.equals("restore")) {
                        if (!w.getPlayers().isEmpty()) {
                            sender.sendMessage(plugin.customization.prefix + "The world contains players. so the world can't be restored!");
                            for (Player x : w.getPlayers())
                                sender.sendMessage(plugin.customization.prefix + "- " + x.getName());
                            return true;
                        }
                        final File backup = new File(plugin.getDataFolder() + "/backup", w.getName());
                        if (!backup.exists()) {
                            sender.sendMessage(plugin.customization.prefix + "Couldn't find a backup for that world!");
                            return true;
                        }
                        new BukkitRunnable() {
                            public void run() {
                                sender.sendMessage(plugin.customization.prefix + "Restoring the world " + ChatColor.LIGHT_PURPLE + w.getName() + ChatColor.GRAY + "!");
                                final long before = System.currentTimeMillis();
                                File worldFile = w.getWorldFolder();
                                final Environment environment = w.getEnvironment();
                                Bukkit.unloadWorld(w, false);
                                plugin.configManager.deleteFile(worldFile);
                                plugin.configManager.copyFile(backup, worldFile);
                                new BukkitRunnable() {
                                    public void run() {
                                        Bukkit.createWorld(new WorldCreator(backup.getName()).environment(environment));
                                        sender.sendMessage(plugin.customization.prefix + "World " + ChatColor.AQUA + backup.getName() + ChatColor.GRAY + " has been " + ChatColor.GREEN + "successfully" + ChatColor.GRAY + " restored! took " + ChatColor.LIGHT_PURPLE + (System.currentTimeMillis() - before) + "ms!");
                                        sender.sendMessage(plugin.customization.prefix + "If the world didn't restore correctly, try using the restore command again!");
                                    }
                                }.runTask(plugin);
                            }
                        }.runTaskAsynchronously(plugin);
                        return true;
                    }

                    return true;
                }

                if (subCommand.equals("backupall")) {
                    if (!checkSender(sender, false, "skywars.worldmanager")) return true;
                    final List<World> worlds = Bukkit.getWorlds();
                    sender.sendMessage(plugin.customization.prefix + ChatColor.GREEN + "Creating a backup of all loaded worlds " + ChatColor.GOLD + "(" + worlds.size() + ")" + ChatColor.GREEN + "!");
                    final long before = System.currentTimeMillis();
                    new BukkitRunnable() {
                        int i = 0;
                        boolean busy = false;

                        public void run() {
                            if (i >= worlds.size()) {
                                sender.sendMessage(plugin.customization.prefix + ChatColor.GREEN + "A backup for " + ChatColor.GOLD + "(" + i + ")" + ChatColor.GREEN + " worlds has been created! " + ChatColor.LIGHT_PURPLE + "Took: " + (System.currentTimeMillis() - before) + "ms");
                                cancel();
                                return;
                            }

                            if (!busy) {
                                World world = worlds.get(i);
                                busy = true;
                                sender.sendMessage(plugin.customization.prefix + "Creating a backup for the world " + ChatColor.AQUA + world.getName());
                                world.save();
                                plugin.configManager.copyFile(world.getWorldFolder(), new File(plugin.getDataFolder() + "/backup", world.getName()));
                                busy = false;
                                i++;
                            }

                        }
                    }.runTaskTimerAsynchronously(plugin, 0, 20);
                    return true;
                }

                if (subCommand.equals("tp")) {
                    if (!checkSender(sender, true, "skywars.worldmanager")) return true;
                    if (args.length == 2) {
                        sendCommandUsage(sender, "WorldManager Tp", "<Name>");
                        return true;
                    }

                    String worldName = args[2];
                    if (Bukkit.getWorld(worldName) == null) {
                        p.sendMessage(plugin.customization.prefix + "Couldn't find a world with that name!");
                        return true;
                    }
                    p.teleport(Bukkit.getWorld(worldName).getSpawnLocation());
                    p.sendMessage(plugin.customization.prefix + "You have been teleported to " + ChatColor.GREEN + worldName + ChatColor.GRAY + "!");
                    return true;
                }

                if (subCommand.equals("setspawn")) {
                    if (!checkSender(sender, true, "skywars.worldmanager")) return true;
                    if (args.length == 2) {
                        sendCommandUsage(sender, "WorldManager Setspawn", "<Name>");
                        return true;
                    }

                    String worldName = args[2];
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        p.sendMessage(plugin.customization.prefix + "Couldn't find a world with that name!");
                        return true;
                    }
                    if (!world.getName().equals(p.getWorld().getName())) {
                        p.sendMessage(plugin.customization.prefix + "You are not in that world to set its spawn!");
                        return true;
                    }
                    world.setSpawnLocation(p.getLocation().getBlockX(), p.getLocation().getBlockY() + 1, p.getLocation().getBlockZ());
                    p.sendMessage(plugin.customization.prefix + "You have set the world spawn location!");
                    return true;
                }

                if (subCommand.equals("list")) {
                    ChatColor c = plugin.colors[plugin.r.nextInt(plugin.colors.length)];
                    sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "------------" + ChatColor.YELLOW + " Skywars " + ChatColor.RED + "WorldManager " + c + "" + ChatColor.STRIKETHROUGH + "------------");
                    sender.sendMessage(plugin.customization.prefix + "Loaded worlds: " + ChatColor.LIGHT_PURPLE + Bukkit.getWorlds().size());
                    for (World w : Bukkit.getWorlds()) {
                        sender.sendMessage(ChatColor.AQUA + "- " + ChatColor.LIGHT_PURPLE + w.getName() + ChatColor.DARK_AQUA + " -> " + ChatColor.RED + "Environment: " + ChatColor.AQUA + w.getEnvironment().name() +
                                ", " + ChatColor.YELLOW + "Difficulty: " + ChatColor.AQUA + w.getDifficulty().name() + ", " + ChatColor.GREEN + "PVP: " + ChatColor.AQUA + w.getPVP() + ", " + ChatColor.DARK_AQUA + "Players: " + ChatColor.AQUA + w.getPlayers().size());
                    }
                    sender.sendMessage(c + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------");
                    return true;
                }

                sender.sendMessage(plugin.customization.prefix + "Unknown command! use /sw WorldManager for a list of commands");
                return true;
            }

            //Unknown command
            sender.sendMessage(plugin.customization.messages.get("Unknown-Command"));

        } else if (commandLabel.equalsIgnoreCase("disguise")) {
            if (sender instanceof Player) {
                if (plugin.bukkitPlayerManager.disguisedPlayers.containsKey(sender.getName())) {
                    sender.sendMessage(plugin.customization.prefix + "§cZaten dönüşüm aktif! Dönüşümü devre dışı bırakmak için '/undisguise' komutunu kullanın.");
                } else {
                    plugin.bukkitPlayerManager.disguisePlayer(((Player) sender).getPlayer(), null);
                    sender.sendMessage(plugin.customization.prefix + "§aDönüşüm aktif edildi! Yeni kullanıcı adı: " + plugin.bukkitPlayerManager.disguisedPlayers.get(sender.getName()));
                }
            }
        } else if (commandLabel.equalsIgnoreCase("undisguise")) {
            if (sender instanceof Player) {
                if (plugin.bukkitPlayerManager.disguisedPlayers.containsKey(sender.getName())) {
                    plugin.bukkitPlayerManager.undisguisePlayer(((Player) sender).getPlayer(), true);
                    sender.sendMessage(plugin.customization.prefix + "§aDönüşüm başarıyla devre dışı bırakıldı!");
                } else {
                    sender.sendMessage(plugin.customization.prefix + "§cAktif bir dönüşümünüz yok! Dönüşmek için '/disguise' komutunu kullanın!");
                }
            }
        } else if (commandLabel.equalsIgnoreCase("stats") || commandLabel.equalsIgnoreCase("istatistikler")) {
            if ((sender instanceof Player) && (args.length == 0)) {
                p.openInventory(plugin.menuManager.getStatsInventory(swPlayer.getSWOfflinePlayer()));
            } else if ((args.length == 1)) {
                p.openInventory(plugin.menuManager.getStatsInventory(plugin.playerManager.getSWPlayer(args[0])));
            } else {
                sender.sendMessage(plugin.customization.prefix + "§aKullanım: §b/istatistikler [isim]");
            }
        } else if (commandLabel.equalsIgnoreCase("yenioyunagir")) {
            if ((sender instanceof Player) && (args.length == 0)) {
                plugin.playerManager.sendPlayerToANewGame(swPlayer);
            }
        } else if (commandLabel.equalsIgnoreCase("hub") || commandLabel.equalsIgnoreCase("lobi") || commandLabel.equalsIgnoreCase("lobby")) {
            if ((sender instanceof Player) && (args.length == 0)) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF("Lobi1");
                p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                return true;
            }
        } else if (commandLabel.equalsIgnoreCase("kurallar") || commandLabel.equalsIgnoreCase("rules")) {
            p.sendMessage("§c----Kurallar-----\n" +
                    "§21- §aHile kullanmak ve oyun açıklarından yararlanmak yasaktır.\n" +
                    "§22- §aChatte spam yapmak ve küfür etmek yasaktır.\n" +
                    "§23- §aOtomatik vurucu (Makro) kullanmak yasaktır.\n" +
                    "§24- §aDiğer oyuncuların huzurunu bozmak yasaktır.\n" +
                    "§25- §aSolo odalarda takım olmak yasaktır.\n" +
                    "§26- §auniocraft.com/kurallar adresindeki diğer kurallara da göz atınız.\n" +
                    "§cKurallara uymayanlar cezalandırılacaktır.");
            return true;
        } else if (commandLabel.equalsIgnoreCase("vip") || commandLabel.equalsIgnoreCase("vipbilgi")) {
            p.sendMessage("§c----VIP Özellikleri-----\n" +
                    "§21- §aMarketteki VIP özel eşyaları kullanabilirsin!\n" +
                    "§22- §aİki kat daha fazla uCoin kazanırsınız!\n" +
                    "§23- §aVerdiğiniz sandık oyları 2 kişilik sayılır!\n" +
                    "§24- §aDolu arenalara girebilirsin! (YAKINDA)\n" +
                    "§25- §aAdının başında VIP yazar ve diğerlerinden farklı olursun.\n" +
                    "§26- §aLobide de VIP olursun.\n" +
                    "§27- §aLobide yaratıklara dönüşebilirsin, evcil hayvan sahibi olabilirsin.\n" +
                    "§28- §aLobide uçabilirsin.\n" +
                    "§29- §aLobide şapkalar ve özel efektler kullanabilirsin.");
            return true;
        } else if (commandLabel.equalsIgnoreCase("help") || commandLabel.equalsIgnoreCase("komutlar")) {
            p.sendMessage("§c----Komutlar-----\n" +
                    "§2/hub: §aLobiye geri dönersin.\n" +
                    "§2/istatistikler: §aİstatistiklerini görürsün.\n" +
                    "§2/ucoin: §auCoin miktarını görürsün.\n" +
                    "§2/vipbilgi: §aVIP bilgilerini görürsün.\n" +
                    "§2/kurallar: §aKuralları görürsün.\n" +
                    "§2/komutlar: §aKullanabileceğin komutların listesini görürsün.");
            return true;
        } else if (commandLabel.equalsIgnoreCase("coin") || commandLabel.equalsIgnoreCase("ucoin")) {
            if ((sender instanceof Player) && (args.length == 0)) {
                sender.sendMessage(plugin.customization.prefix + "§auCoin Miktarınız: §b" + NumberFormat.getNumberInstance(Locale.GERMANY).format(plugin.playerManager.getCoin(sender.getName())));
            } else if ((args.length == 1)) {
                if (args[0].matches("^[a-zA-Z0-9_]*$")) {
                    sender.sendMessage(plugin.customization.prefix + "§a" + args[0] + " isimli oyucunun uCoin Miktarı: §b" + NumberFormat.getNumberInstance(Locale.GERMANY).format(plugin.playerManager.getCoin(args[0])));
                } else {
                    sender.sendMessage(plugin.customization.prefix + "§cLütfen geçerli bir oyuncu ismi giriniz!");
                }
            } else {
                sender.sendMessage(plugin.customization.prefix + "§aKullanım: §b/ucoin [isim]");
            }
        }
        return false;
    }

    public void sendCommandUsage(CommandSender receiver, String command, String arguments, String... additional) {
        receiver.sendMessage(plugin.customization.prefix + "Kullanım: /sw " + ChatColor.GREEN + command + ChatColor.GRAY + " " + arguments);
        for (String add : additional)
            receiver.sendMessage(plugin.customization.prefix + ChatColor.AQUA + "- " + ChatColor.GRAY + add);
    }

    public boolean checkSender(CommandSender sender, boolean mustBePlayer, String permission) {
        if (mustBePlayer && !(sender instanceof Player)) {
            sender.sendMessage(plugin.customization.prefix + "Bu komutu kullanabilmek için oyuncu olmalısınız.");
            return false;
        }
        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            sender.sendMessage(plugin.customization.messages.get("No-Permission"));
            return false;
        }
        return true;
    }

}
