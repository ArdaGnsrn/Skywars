package me.uniodex.skywars.managers;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.arena.Arena;
import me.uniodex.skywars.utils.packages.pool.CredentialPackageFactory;
import me.uniodex.skywars.utils.packages.pool.Pool;
import me.uniodex.skywars.utils.packages.pool.PoolDriver;
import me.uniodex.skywars.utils.packages.pool.properties.PropertyFactory;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

//TODO Remove hardcoding after 4.0
@SuppressWarnings("deprecation")
public class SQLManager {

    private Skywars plugin;

    private Pool pool;
    public String database;

    public SQLManager(Skywars plugin, String table, String host, String port, String database, String username, String password) {
        pool = new Pool(CredentialPackageFactory.get(username, password), PoolDriver.MYSQL);
        pool.withMin(10).withMax(10).withMysqlUrl(host, database);
        pool.withProperty(PropertyFactory.leakDetectionThreshold(10000));
        pool.withProperty(PropertyFactory.connectionTimeout(15000));
        pool.build();

        this.plugin = plugin;
        this.database = database;

        setupTable();
        cleanOldSQLData();
    }

    public boolean updateSQL(String QUERY) {
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(QUERY);
            int count = statement.executeUpdate();
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateSQLAsync(String QUERY, Long delay) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try (Connection connection = pool.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(QUERY);
                    statement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };

        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, task, delay);
		/*try ( Connection connection = pool.getConnection() ) {
			PreparedStatement statement = connection.prepareStatement(QUERY);
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
    }

    private void cleanOldSQLData() {
        updateSQLAsync("DELETE FROM `" + database + "`.`usw_oyunKoduSezon2` WHERE `usw_oyunKoduSezon2`.`tarih` <= UNIX_TIMESTAMP() - 86400 AND `oyunculistesi` = '';", 20L);
    }

    private void setupTable() {
        if (!tableExists("usw_oyunKoduSezon2")) {
            updateSQL("CREATE TABLE IF NOT EXISTS `usw_oyunKoduSezon2`( `id` int(11) NOT NULL, `tarih` bigint(20) NOT NULL, `oyunculistesi` varchar(8000) NOT NULL, `serverid` varchar(255) NOT NULL, `tamamlandi` int(1) NOT NULL DEFAULT '0');");
            updateSQL("ALTER TABLE `usw_oyunKoduSezon2` ADD PRIMARY KEY (`id`);");
            updateSQL("ALTER TABLE `usw_oyunKoduSezon2` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;");
        }

        if (!tableExists("usw_cage")) {
            updateSQL("CREATE TABLE IF NOT EXISTS `usw_cage`( `id` int(11) NOT NULL, `player` varchar(255) NOT NULL, `cage` varchar(255) NOT NULL);");
            updateSQL("ALTER TABLE `usw_cage` ADD PRIMARY KEY (`id`);");
            updateSQL("ALTER TABLE `usw_cage` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;");
        }

        if (!tableExists("usw_coin")) {
            updateSQL("CREATE TABLE IF NOT EXISTS `usw_coin`( `id` int(11) NOT NULL, `player` varchar(255) NOT NULL, `coin` int(11) NOT NULL);");
            updateSQL("ALTER TABLE `usw_coin` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `player` (`player`);");
            updateSQL("ALTER TABLE `usw_coin` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;");
        }

        if (!tableExists("usw_kit")) {
            updateSQL("CREATE TABLE IF NOT EXISTS `usw_kit`( `id` int(11) NOT NULL, `player` varchar(255) NOT NULL, `kit` varchar(255) NOT NULL);");
            updateSQL("ALTER TABLE `usw_kit` ADD PRIMARY KEY (`id`);");
            updateSQL("ALTER TABLE `usw_kit` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;");
        }

        if (!tableExists("usw_trail")) {
            updateSQL("CREATE TABLE IF NOT EXISTS `usw_trail`( `id` int(11) NOT NULL, `player` varchar(255) NOT NULL, `trail` varchar(255) NOT NULL);");
            updateSQL("ALTER TABLE `usw_trail` ADD PRIMARY KEY (`id`);");
            updateSQL("ALTER TABLE `usw_trail` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;");
        }

        if (!tableExists("usw_selected_duo")) {
            updateSQL("CREATE TABLE IF NOT EXISTS `usw_selected_duo`( `id` int(11) NOT NULL, `player` varchar(255) NOT NULL, `kit` varchar(255) NOT NULL DEFAULT 'Default', `cage` varchar(255) NOT NULL DEFAULT 'Default', `trail` varchar(255) NOT NULL DEFAULT 'Default');");
            updateSQL("ALTER TABLE `usw_selected_duo` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `player` (`player`);");
            updateSQL("ALTER TABLE `usw_selected_duo` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;");
        }

        if (!tableExists("usw_selected_solo")) {
            updateSQL("CREATE TABLE IF NOT EXISTS `usw_selected_solo`( `id` int(11) NOT NULL, `player` varchar(255) NOT NULL, `kit` varchar(255) NOT NULL DEFAULT 'Default', `cage` varchar(255) NOT NULL DEFAULT 'Default', `trail` varchar(255) NOT NULL DEFAULT 'Default');");
            updateSQL("ALTER TABLE `usw_selected_solo` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `player` (`player`);");
            updateSQL("ALTER TABLE `usw_selected_solo` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;");
        }

        if (!tableExists("usw_stats_solo")) {
            updateSQL("CREATE TABLE IF NOT EXISTS `usw_stats_solo`( `id` int(11) NOT NULL, `player` varchar(20) NOT NULL, `wins` int(11) NOT NULL DEFAULT '0', `playedGames` int(11) NOT NULL DEFAULT '0', `kills` int(11) NOT NULL DEFAULT '0', `deaths` int(11) NOT NULL DEFAULT '0', `projectilesHit` int(11) NOT NULL DEFAULT '0', `projectilesLaunched` int(11) NOT NULL DEFAULT '0', `blocksPlaced` int(11) NOT NULL DEFAULT '0', `blocksBroken` int(11) NOT NULL DEFAULT '0', `itemsEnchanted` int(11) NOT NULL DEFAULT '0', `itemsCrafted` int(11) NOT NULL DEFAULT '0', `fishesCaught` int(11) NOT NULL DEFAULT '0', `playTime` int(11) NOT NULL DEFAULT '0' COMMENT 'in seconds');");
            updateSQL("ALTER TABLE `usw_stats_solo` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `username` (`player`);");
            updateSQL("ALTER TABLE `usw_stats_solo` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;");
        }

        if (!tableExists("usw_stats_duo")) {
            updateSQL("CREATE TABLE IF NOT EXISTS `usw_stats_duo`( `id` int(11) NOT NULL, `player` varchar(20) NOT NULL, `wins` int(11) NOT NULL DEFAULT '0', `playedGames` int(11) NOT NULL DEFAULT '0', `kills` int(11) NOT NULL DEFAULT '0', `deaths` int(11) NOT NULL DEFAULT '0', `projectilesHit` int(11) NOT NULL DEFAULT '0', `projectilesLaunched` int(11) NOT NULL DEFAULT '0', `blocksPlaced` int(11) NOT NULL DEFAULT '0', `blocksBroken` int(11) NOT NULL DEFAULT '0', `itemsEnchanted` int(11) NOT NULL DEFAULT '0', `itemsCrafted` int(11) NOT NULL DEFAULT '0', `fishesCaught` int(11) NOT NULL DEFAULT '0', `playTime` int(11) NOT NULL DEFAULT '0' COMMENT 'in seconds');");
            updateSQL("ALTER TABLE `usw_stats_duo` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `username` (`player`);");
            updateSQL("ALTER TABLE `usw_stats_duo` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;");
        }
    }

    public void onDisable() {
        pool.close();
    }

    public void updateGameData(String data, String value, Integer oyunKodu, Long delay) {
        updateSQLAsync("UPDATE `" + database + "`.`usw_oyunKoduSezon2` SET `" + data + "` = '" + value + "' WHERE `id` = '" + oyunKodu + "';", delay);
    }

    public void generateOyunKodu(Arena arena) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                final long unixTime = System.currentTimeMillis() / 1000L;

                try (Connection connection = pool.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + database + "`.`usw_oyunKoduSezon2` (`tarih`, `oyunculistesi`, `serverid`, `tamamlandi`) VALUES ('" + unixTime + "', '', '', '0');", new String[]{"id"});
                    if (statement.executeUpdate() > 0) {
                        ResultSet generatedKeys = statement.getGeneratedKeys();
                        if (null != generatedKeys && generatedKeys.next()) {
                            arena.setOyunKodu(generatedKeys.getInt(1));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };

        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, task);
    }

    private boolean playerExists(String player, String table, String playerNameTable) {
        String QUERY = "SELECT * FROM `" + database + "`.`" + table + "` WHERE `" + playerNameTable + "` = '" + player + "';";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(QUERY);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                return res.getString(playerNameTable) != null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean tableExists(String tableName) {
        try (Connection connection = pool.getConnection()) {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            return tables.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createPlayer(String player) {
        if (!playerExists(player, "usw_stats_solo", "player")) {
            plugin.sqlManager.updateSQL("INSERT INTO `" + database + "`.`usw_stats_solo` (`player`) VALUES ('" + player + "');");
        }
        if (!playerExists(player, "usw_stats_duo", "player")) {
            plugin.sqlManager.updateSQL("INSERT INTO `" + database + "`.`usw_stats_duo` (`player`) VALUES ('" + player + "');");
        }
        if (!playerExists(player, "usw_coin", "player")) {
            plugin.sqlManager.updateSQL("INSERT INTO `" + database + "`.`usw_coin` (`player`, `coin`) VALUES ('" + player + "', '0');");
        }
        if (!playerExists(player, "usw_selected_solo", "player")) {
            plugin.sqlManager.updateSQL("INSERT INTO `" + database + "`.`usw_selected_solo` (`player`) VALUES ('" + player + "');");
        }
        if (!playerExists(player, "usw_selected_duo", "player")) {
            plugin.sqlManager.updateSQL("INSERT INTO `" + database + "`.`usw_selected_duo` (`player`) VALUES ('" + player + "');");
        }
    }

    public HashMap<String, Integer> getPlayerStats(String player) {
        if (!playerExists(player, "usw_stats_" + plugin.config.gameMode, "player")) {
            createPlayer(player);
        }

        String statsQuery = "SELECT * FROM `" + database + "`.`usw_stats_" + plugin.config.gameMode + "` WHERE `player` = '" + player + "';";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(statsQuery);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                HashMap<String, Integer> stats = new HashMap<String, Integer>();
                stats.put("wins", res.getInt("wins"));
                stats.put("playedGames", res.getInt("playedGames"));
                stats.put("kills", res.getInt("kills"));
                stats.put("deaths", res.getInt("deaths"));
                stats.put("projectilesHit", res.getInt("projectilesHit"));
                stats.put("projectilesLaunched", res.getInt("projectilesLaunched"));
                stats.put("blocksPlaced", res.getInt("blocksPlaced"));
                stats.put("blocksBroken", res.getInt("blocksBroken"));
                stats.put("itemsEnchanted", res.getInt("itemsEnchanted"));
                stats.put("itemsCrafted", res.getInt("itemsCrafted"));
                stats.put("fishesCaught", res.getInt("fishesCaught"));
                stats.put("playTime", res.getInt("playTime"));
                return stats;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getPlayerStat(String player, String statName, boolean create) {
        if (!playerExists(player, "usw_stats_" + plugin.config.gameMode, "player")) {
            if (create) {
                createPlayer(player);
            } else {
                return 0;
            }
        }

        String QUERY = "SELECT * FROM `" + database + "`.`usw_stats_" + plugin.config.gameMode + "` WHERE `player` = '" + player + "';";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(QUERY);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                return res.getInt(statName);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getPlayerCoins(String player) {
        if (!playerExists(player, "usw_coin", "player")) {
            createPlayer(player);
        }

        String QUERY = "SELECT * FROM `" + database + "`.`usw_coin` WHERE `player` = '" + player + "';";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(QUERY);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                return res.getInt("coin");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public ArrayList<String> getPlayerKits(String player) {
        // Kits
        ArrayList<String> playerKits = new ArrayList<String>();
        playerKits.add("Default");
        String kitQuery = "SELECT kit FROM usw_kit WHERE `player` = '" + player + "';";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(kitQuery);
            ResultSet res = statement.executeQuery();
            while (res.next()) {
                ResultSetMetaData resmd = res.getMetaData();
                int columnCount = resmd.getColumnCount();
                if (columnCount != 0) {
                    for (int i = 1; i <= columnCount; i++) {
                        if (!res.getString(i).isEmpty()) {
                            playerKits.add(res.getString(i));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerKits;
    }

    public ArrayList<String> getPlayerCages(String player) {
        // Cages
        ArrayList<String> playerCages = new ArrayList<String>();
        playerCages.add("Default");
        String cageQuery = "SELECT cage FROM usw_cage WHERE `player` = '" + player + "';";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(cageQuery);
            ResultSet res = statement.executeQuery();
            while (res.next()) {
                ResultSetMetaData resmd = res.getMetaData();
                int columnCount = resmd.getColumnCount();
                if (columnCount != 0) {
                    for (int i = 1; i <= columnCount; i++) {
                        if (!res.getString(i).isEmpty()) {
                            playerCages.add(res.getString(i));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerCages;
    }

    public ArrayList<String> getPlayerTrails(String player) {
        // Trails
        ArrayList<String> playerTrails = new ArrayList<String>();
        playerTrails.add("Default");
        String trailQuery = "SELECT trail FROM usw_trail WHERE `player` = '" + player + "';";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(trailQuery);
            ResultSet res = statement.executeQuery();
            while (res.next()) {
                ResultSetMetaData resmd = res.getMetaData();
                int columnCount = resmd.getColumnCount();
                if (columnCount != 0) {
                    for (int i = 1; i <= columnCount; i++) {
                        if (!res.getString(i).isEmpty()) {
                            playerTrails.add(res.getString(i));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerTrails;
    }

    public HashMap<String, String> getPlayerSelectedItems(String player) {
        String query = "SELECT * FROM usw_selected_" + plugin.config.gameMode + " WHERE `player` = '" + player + "';";

        HashMap<String, String> items = new HashMap<String, String>();

        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                items.put("kit", res.getString("kit"));
                items.put("cage", res.getString("cage"));
                items.put("trail", res.getString("trail"));
            } else {
                items.put("kit", "Default");
                items.put("cage", "Default");
                items.put("trail", "Default");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            items.put("kit", "Default");
            items.put("cage", "Default");
            items.put("trail", "Default");
            Bukkit.getLogger().log(Level.SEVERE, "Bir MYSQL hatası yaşandığı için oyuncuya(" + player + ") Default eşyalar verildi.");
        }
        return items;
    }

    public void giveStats(String player, String statName, Integer amount) {
        if (playerExists(player, "usw_stats_" + plugin.config.gameMode, "player")) {
            int mevcutStat = getPlayerStat(player, statName, true);
            if (mevcutStat == -1) {
                return;
            }

            int ayarlanacakStat = mevcutStat + amount;

            updateSQLAsync("UPDATE `" + database + "`.`usw_stats_" + plugin.config.gameMode + "` SET " + statName + "='" + ayarlanacakStat + "' WHERE `player`='" + player + "';", 1L);
        } else {
            createPlayer(player);
            giveStats(player, statName, amount);
        }
    }

    public void giveCoins(String player, Integer amount) {
        if (playerExists(player, "usw_coin", "player")) {
            int mevcutCoin = getPlayerCoins(player);
            if (mevcutCoin == -1) {
                return;
            }

            int ayarlanacakCoin = mevcutCoin + amount;

            updateSQLAsync("UPDATE `" + database + "`.`usw_coin` SET coin='" + ayarlanacakCoin + "' WHERE `player`='" + player + "';", 1L);
        } else {
            createPlayer(player);
            giveCoins(player, amount);
        }
    }

    public void giveItem(String player, String itemId, String itemType) {
        //TODO
    }

    public void selectItem(String player, String itemId, String itemType) {
        if (this.playerHaveItem(player, itemId, itemType) || itemId.equalsIgnoreCase("Default")) {
            String serverType;
            if (itemType.contains("solo")) {
                serverType = "solo";
            } else {
                serverType = "duo";
            }

            String actualItemType = itemType.replace(serverType, "");
            String QUERY = "";
            if (!playerExists(player, "usw_selected_" + serverType, "player")) {
                if (actualItemType.equalsIgnoreCase("kit")) {
                    QUERY = "INSERT INTO `" + database + "`.`usw_selected_" + serverType + "` (`player`, `kit`, `cage`, `trail`) VALUES ('" + player + "', '" + itemId + "', 'Default', 'Default');";
                }
                if (actualItemType.equalsIgnoreCase("cage")) {
                    QUERY = "INSERT INTO `" + database + "`.`usw_selected_" + serverType + "` (`player`, `kit`, `cage`, `trail`) VALUES ('" + player + "', 'Default', '" + itemId + "', 'Default');";
                }
                if (actualItemType.equalsIgnoreCase("trail")) {
                    QUERY = "INSERT INTO `" + database + "`.`usw_selected_" + serverType + "` (`player`, `kit`, `cage`, `trail`) VALUES ('" + player + "', 'Default', 'Default', '" + itemId + "');";
                }
                try (Connection connection = pool.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(QUERY);
                    statement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                QUERY = "UPDATE `" + database + "`.`usw_selected_" + serverType + "` SET `" + actualItemType + "` = '" + itemId + "' WHERE `usw_selected_" + serverType + "`.`player` = '" + player + "';";
                try (Connection connection = pool.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(QUERY);
                    statement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void takeStats(String player, String statName, Integer amount) {
        if (playerExists(player, "usw_stats_" + plugin.config.gameMode, "player")) {
            int mevcutStat = getPlayerStat(player, statName, true);
            if (mevcutStat == -1) {
                return;
            }

            int ayarlanacakStat = mevcutStat - amount;

            updateSQLAsync("UPDATE `" + database + "`.`usw_stats_" + plugin.config.gameMode + "` SET " + statName + "='" + ayarlanacakStat + "' WHERE player='" + player + "';", 1L);
        } else {
            createPlayer(player);
            takeStats(player, statName, amount);
        }
    }

    public void takeCoins(String player, Integer amount) {
        if (playerExists(player, "usw_coin", "player")) {
            int mevcutCoin = getPlayerCoins(player);
            if (mevcutCoin == -1) {
                return;
            }

            int ayarlanacakCoin = mevcutCoin - amount;

            updateSQLAsync("UPDATE `" + database + "`.`usw_coin` SET coin='" + ayarlanacakCoin + "' WHERE player='" + player + "';", 1L);
        } else {
            createPlayer(player);
            takeCoins(player, amount);
        }
    }

    public void setStats(String player, String statName, Integer amount) {
        if (playerExists(player, "usw_stats_" + plugin.config.gameMode, "player")) {
            updateSQLAsync("UPDATE `" + database + "`.`usw_stats_" + plugin.config.gameMode + "` SET " + statName + "='" + amount + "' WHERE player='" + player + "';", 1L);
        } else {
            createPlayer(player);
            setStats(player, statName, amount);
        }
    }

    public void setCoins(String player, Integer amount, boolean sync) {
        if (playerExists(player, "usw_coin", "player")) {
            if (!sync) {
                updateSQLAsync("UPDATE `" + database + "`.`usw_coin` SET coin='" + amount + "' WHERE player='" + player + "';", 1L);
            } else {
                updateSQL("UPDATE `" + database + "`.`usw_coin` SET coin='" + amount + "' WHERE player='" + player + "';");
            }
        } else {
            createPlayer(player);
            setCoins(player, amount, sync);
        }
    }

    public boolean playerHaveItem(String p, String itemid, String itemType) {
        String serverType;
        if (itemType.contains("solo")) {
            serverType = "solo";
        } else {
            serverType = "duo";
        }

        String actualItemType = itemType.replace(serverType, "");

        ArrayList<String> playerKits = getPlayerKits(p);
        ArrayList<String> playerCages = getPlayerCages(p);
        ArrayList<String> playerTrails = getPlayerTrails(p);

        if (plugin.permission.playerHas("world", Bukkit.getOfflinePlayer(p), "rank.vip")) {
            playerKits.add("soloZirhci");
            playerKits.add("duoZirhci");
            playerKits.add("soloBalikci");
            playerKits.add("duoBalikci");
            playerKits.add("soloBalcik");
            playerKits.add("duoBalcik");
            playerCages.add("soloGorunmez");
            playerCages.add("duoGorunmez");
            playerTrails.add("soloElmas");
            playerTrails.add("duoElmas");
            playerTrails.add("soloTNT");
            playerTrails.add("duoTNT");
        }

        if (actualItemType.equalsIgnoreCase("kit")) {
            return playerKits.contains(serverType + itemid);
        }
        if (actualItemType.equalsIgnoreCase("cage")) {
            return playerCages.contains(serverType + itemid);
        }
        if (actualItemType.equalsIgnoreCase("trail")) {
            return playerTrails.contains(serverType + itemid);
        }
        return false;
    }

    public void savePlayerStats(boolean sync, String player, int wins, int playedGames, int kills, int deaths, int projectilesHit, int projectilesLaunched, int blocksPlaced, int blocksBroken, int itemsEnchanted, int itemsCrafted, int fishesCaught, int playTime) {
        if (!playerExists(player, "usw_stats_" + plugin.config.gameMode, "player")) {
            if (!sync) {
                plugin.sqlManager.updateSQLAsync("INSERT INTO `" + database + "`.`usw_stats_" + plugin.config.gameMode + "` (`player`, `wins`, `playedGames`, `kills`, `deaths`, `projectilesHit`, `projectilesLaunched`, `blocksPlaced`, `blocksBroken`, `itemsEnchanted`, `itemsCrafted`, `fishesCaught`, `playTime`) VALUES ('" + player + "', '" + wins + "', '" + playedGames + "', '" + kills + "', '" + deaths + "', '" + projectilesHit + "', '" + projectilesLaunched + "', '" + blocksPlaced + "', '" + blocksBroken + "', '" + itemsEnchanted + "', '" + itemsCrafted + "', '" + fishesCaught + "', '" + playTime + "');", 1L);
            } else {
                plugin.sqlManager.updateSQL("INSERT INTO `" + database + "`.`usw_stats_" + plugin.config.gameMode + "` (`player`, `wins`, `playedGames`, `kills`, `deaths`, `projectilesHit`, `projectilesLaunched`, `blocksPlaced`, `blocksBroken`, `itemsEnchanted`, `itemsCrafted`, `fishesCaught`, `playTime`) VALUES ('" + player + "', '" + wins + "', '" + playedGames + "', '" + kills + "', '" + deaths + "', '" + projectilesHit + "', '" + projectilesLaunched + "', '" + blocksPlaced + "', '" + blocksBroken + "', '" + itemsEnchanted + "', '" + itemsCrafted + "', '" + fishesCaught + "', '" + playTime + "');");
            }
        } else {
            if (!sync) {
                plugin.sqlManager.updateSQLAsync("UPDATE `" + database + "`.`usw_stats_" + plugin.config.gameMode + "` SET `wins` = '" + wins + "', `playedGames` = '" + playedGames + "', `kills` = '" + kills + "', `deaths` = '" + deaths + "', `projectilesHit` = '" + projectilesHit + "', `projectilesLaunched` = '" + projectilesLaunched + "', `blocksPlaced` = '" + blocksPlaced + "', `blocksBroken` = '" + blocksBroken + "', `itemsEnchanted` = '" + itemsEnchanted + "', `itemsCrafted` = '" + itemsCrafted + "', `fishesCaught` = '" + fishesCaught + "', `playTime` = '" + playTime + "' WHERE `usw_stats_" + plugin.config.gameMode + "`.`player` = '" + player + "';", 1L);
            } else {
                plugin.sqlManager.updateSQL("UPDATE `" + database + "`.`usw_stats_" + plugin.config.gameMode + "` SET `wins` = '" + wins + "', `playedGames` = '" + playedGames + "', `kills` = '" + kills + "', `deaths` = '" + deaths + "', `projectilesHit` = '" + projectilesHit + "', `projectilesLaunched` = '" + projectilesLaunched + "', `blocksPlaced` = '" + blocksPlaced + "', `blocksBroken` = '" + blocksBroken + "', `itemsEnchanted` = '" + itemsEnchanted + "', `itemsCrafted` = '" + itemsCrafted + "', `fishesCaught` = '" + fishesCaught + "', `playTime` = '" + playTime + "' WHERE `usw_stats_" + plugin.config.gameMode + "`.`player` = '" + player + "';");
            }
        }
    }

    public String getPlayerDisguise(String player) {
        String QUERY = "SELECT * FROM `" + database + "`.`usw_disguise` WHERE `player` = '" + player + "';";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(QUERY);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                String disguiseName = res.getString("displayName");
                return disguiseName;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isPlayerInResetSkinList(String playerName) {
        String QUERY = "SELECT * FROM `bungee`.`unioskin_resetSkins` WHERE `player` = '" + playerName + "';";
        try (Connection connection = pool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(QUERY);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                return res.getString("player") != null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addPlayerToResetList(String playerName) {
        if (!isPlayerInResetSkinList(playerName)) {
            updateSQLAsync("INSERT INTO `bungee`.`unioskin_resetSkins` (`id`, `player`) VALUES (NULL, '" + playerName + "');", 1L);
        }
    }

    //TODO
    // Create player
    // Load player stats
    // Load player coins
    // Load player items
    // Load player selected items
    // Give player stats
    // Give player coins
    // Give player items
    // Select player items
    // Take player stats
    // Take player coins
    // Set player stats
    // Set player coins
    // When player join keep stats in SWOnlinePlayer. When he quits save all the data in db. If player is offline just change data. Also save the data every 5 minutes

}
