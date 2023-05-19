package hillwalk.fr.profilesrpg.manager;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.database.DatabaseManager;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ProfileManager {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private HashMap<UUID, List<Profile>> profiles;

    public ProfileManager(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.profiles = new HashMap<>();
    }

    public void loadProfile(Player player, UUID profileUUID) {
        try {
            Connection connection = databaseManager.getConnection();
            PreparedStatement statement;
            ResultSet results;

            // Load profile data
            statement = connection.prepareStatement(
                    "SELECT * FROM profiles WHERE profileUUID = ?"
            );
            statement.setString(1, profileUUID.toString());
            results = statement.executeQuery();
            if (results.next()) {
                player.setHealth(results.getDouble("health"));
                player.setFoodLevel(results.getInt("food"));
                player.setLevel(results.getInt("level"));
                player.setExp((float) results.getDouble("exp"));
                // And so on for the rest of the profile data...
            } else {
                this.plugin.getLogger().severe("Could not find profile with UUID: " + profileUUID);
            }

            // Load location data
            statement = connection.prepareStatement(
                    "SELECT * FROM locations WHERE profileUUID = ?"
            );
            statement.setString(1, profileUUID.toString());
            results = statement.executeQuery();
            if (results.next()) {
                // Load the location into the player...
            }

            // Load inventory data
            statement = connection.prepareStatement(
                    "SELECT * FROM inventories WHERE profileUUID = ? ORDER BY slot"
            );
            statement.setString(1, profileUUID.toString());
            results = statement.executeQuery();
            while (results.next()) {
                int slot = results.getInt("slot");
                String itemData = results.getString("item");

                // Convert the saved item data back into an ItemStack...
                YamlConfiguration config = new YamlConfiguration();
                config.loadFromString(itemData);
                ItemStack item = config.getItemStack("item");

                player.getInventory().setItem(slot, item);
            }

        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not load profile: " + e.getMessage());
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void createProfile(Player player) {
        try {
            Connection connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO profiles (profileUUID, playerUUID, name) VALUES (?, ?, ?)"
            );
            UUID profileUUID = UUID.randomUUID();
            statement.setString(1, profileUUID.toString());
            statement.setString(2, player.getUniqueId().toString());
            statement.setString(3, player.getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not create profile: " + e.getMessage());
        }
    }

    public void saveProfile(Player player, UUID profileUUID) {
        try {
            Connection connection = databaseManager.getConnection();
            PreparedStatement statement;

            // Save profile data
            statement = connection.prepareStatement(
                    "UPDATE profiles SET health = ?, food = ?, level = ?, exp = ? WHERE profileUUID = ?"
            );
            statement.setDouble(1, player.getHealth());
            statement.setInt(2, player.getFoodLevel());
            statement.setInt(3, player.getLevel());
            statement.setDouble(4, player.getExp());
            statement.setString(5, profileUUID.toString());
            statement.executeUpdate();


            // Save inventory data
            // First, clear the old inventory data...
            statement = connection.prepareStatement(
                    "DELETE FROM inventories WHERE profileUUID = ?"
            );
            statement.setString(1, profileUUID.toString());
            statement.executeUpdate();

            for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
                ItemStack item = player.getInventory().getItem(slot);
                if (item != null) {
                    statement = connection.prepareStatement(
                            "INSERT INTO inventories (profileUUID, slot, item) VALUES (?, ?, ?)"
                    );
                    statement.setString(1, profileUUID.toString());
                    statement.setInt(2, slot);

                    // Convert the ItemStack into a format that can be saved...
                    YamlConfiguration config = new YamlConfiguration();
                    config.set("item", item);
                    String itemData = config.saveToString();

                    statement.setString(3, itemData);
                    statement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not save profile: " + e.getMessage());
        }
    }

    public void deleteProfile(UUID profileUUID) {
        try {
            Connection connection = databaseManager.getConnection();
            PreparedStatement statement;

            // Delete profile data
            statement = connection.prepareStatement(
                    "DELETE FROM profiles WHERE profileUUID = ?"
            );
            statement.setString(1, profileUUID.toString());
            statement.executeUpdate();

            // Delete location data
            statement = connection.prepareStatement(
                    "DELETE FROM locations WHERE profileUUID = ?"
            );
            statement.setString(1, profileUUID.toString());
            statement.executeUpdate();

            // Delete inventory data
            statement = connection.prepareStatement(
                    "DELETE FROM inventories WHERE profileUUID = ?"
            );
            statement.setString(1, profileUUID.toString());
            statement.executeUpdate();

        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not delete profile: " + e.getMessage());
        }
    }


}
