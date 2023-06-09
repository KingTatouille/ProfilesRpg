package hillwalk.fr.profilesrpg.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        // Create the directory where your database file will be stored if it does not exist
        File dataFolder = new File(plugin.getDataFolder(), "playerdata.db");

        // Connect to the SQLite database
        try {
            if (!dataFolder.exists()) {
                dataFolder.createNewFile();
            }

            if (this.connection != null && !this.connection.isClosed()) {
                return;
            }

            String url = "jdbc:sqlite:" + dataFolder;
            this.connection = DriverManager.getConnection(url);

            plugin.getLogger().info("Connected to the SQLite database.");

            // Now that we're connected, set up the tables
            setupTables();

        } catch (SQLException | IOException e) {
            plugin.getLogger().severe("Could not connect to the SQLite database: " + e.getMessage());
        }
    }


    public void setupTables() {
        try {
            PreparedStatement statement;

            if(plugin.getServer().getPluginManager().getPlugin("MMOCore") == null){

                statement = this.connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS players (" +
                                "profileUUID TEXT PRIMARY KEY," +
                                "playerUUID TEXT," +
                                "name TEXT," +
                                "health REAL," +
                                "maxHealth REAL," +
                                "food INT," +
                                "level INT," +
                                "experience REAL," +
                                "class TEXT," +
                                "mana REAL," +
                                "stellium REAL," +
                                "stamina REAL," +
                                "lastLogin BIGINT," +
                                "classPoints INT," +
                                "skillPoints INT," +
                                "skillReallocationPoints INT," +
                                "attributePoints INT," +
                                "attributeReallocPoints INT," +
                                "skillTreePoints TEXT," +
                                "skillTreeReallocationPoints INT," +
                                "waypoints TEXT," +
                                "friends TEXT," +
                                "timesClaimed TEXT," +
                                "boundSkills TEXT," +
                                "attributes TEXT," +
                                "professions TEXT," +
                                "classInfo TEXT," +
                                "quest TEXT" +
                                ")"
                );
                statement.executeUpdate();


            }

            // Creating table for player profiles
            statement = this.connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS profiles (" +
                            "profileUUID TEXT PRIMARY KEY," +
                            "playerUUID TEXT," +
                            "name TEXT," +
                            "health REAL," +
                            "maxHealth REAL," +
                            "food INT," +
                            "level INT," +
                            "exp REAL," +
                            "saturation REAL," +
                            "remainingAir INT," +
                            "maximumAir INT," +
                            "fireTicks INT," +
                            "gameMode TEXT," +
                            "user_group TEXT," +
                            "allowFlight BOOLEAN," +
                            "fallDistance REAL" +
                            ")"
            );
            statement.executeUpdate();

            // Creating table for player locations
            statement = this.connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS locations (" +
                            "profileUUID TEXT," +
                            "world TEXT," +
                            "x REAL," +
                            "y REAL," +
                            "z REAL," +
                            "pitch REAL," +
                            "yaw REAL" +
                            ")"
            );
            statement.executeUpdate();

            // Creating table for player inventories
            // Note: Saving and loading complete inventories like this might not be the best approach.
            // You may want to come up with a more sophisticated solution.
            statement = this.connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS inventories (" +
                            "profileUUID TEXT," +
                            "slot INT," +
                            "item TEXT" +
                            ")"
            );
            statement.executeUpdate();

        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not create tables: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
                plugin.getLogger().info("Disconnected from the SQLite database.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not close the SQLite database connection: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

}
