package hillwalk.fr.profilesrpg.manager;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.ProfilesRpg;
import hillwalk.fr.profilesrpg.database.DatabaseManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ProfileManager {
    private final ProfilesRpg plugin;
    private final DatabaseManager databaseManager;
    private HashMap<UUID, List<Profile>> profiles;

    public ProfileManager(ProfilesRpg plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.profiles = new HashMap<>();
    }


    public void loadProfile(UUID playerUUID, UUID profileUUID) {
        Player player = this.plugin.getServer().getPlayer(playerUUID);
        if (player == null) {
            this.plugin.getLogger().severe("Could not find player with UUID: " + playerUUID);
            return;
        }
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
                Profile profile = new Profile(
                        UUID.fromString(results.getString("profileUUID")),
                        UUID.fromString(results.getString("playerUUID")),
                        results.getString("name"),
                        null // Set the spawn location later
                );

                //Renommer le joueur lors du chargement du profile.
                player.setDisplayName(results.getString("name"));

                profile.setGroup(results.getString("user_group"));
                // Set other profile data
                profile.setHealth(results.getDouble("health"));
                profile.setFoodLevel(results.getInt("food"));
                profile.setLevel(results.getInt("level"));
                profile.setExp((float) results.getDouble("exp"));

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
                Profile profile = getProfile(player.getUniqueId(), profileUUID);
                if (profile != null) {
                    Location spawnLocation = new Location(
                            player.getWorld(),
                            results.getDouble("x"),
                            results.getDouble("y"),
                            results.getDouble("z"),
                            results.getFloat("yaw"),
                            results.getFloat("pitch")
                    );
                    profile.setSpawnLocation(spawnLocation);
                }
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

            // Set player in lobby status
            plugin.getPlayerLobbyStatusManager().setPlayerInLobby(player, false);

            // Set player to spectate mode to prevent movement and interaction
            player.setGameMode(GameMode.SURVIVAL);

            // Add blindness effect
            player.removePotionEffect(PotionEffectType.BLINDNESS);


        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not load profile: " + e.getMessage());
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public Profile getProfile(UUID playerUUID, UUID profileUUID) {
        List<Profile> playerProfiles = profiles.get(playerUUID);
        if (playerProfiles != null) {
            for (Profile profile : playerProfiles) {
                if (profile.getProfileId().equals(profileUUID)) {
                    return profile;
                }
            }
        }
        return null;
    }

    public List<Profile> getPlayerProfiles(UUID playerUUID) {
        List<Profile> playerProfiles = new ArrayList<>();

        try {
            Connection connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM profiles LEFT JOIN locations ON profiles.profileUUID = locations.profileUUID WHERE profiles.playerUUID = ?"
            );
            statement.setString(1, playerUUID.toString());
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                UUID profileUUID = UUID.fromString(results.getString("profileUUID"));
                String name = results.getString("name");

                double x = results.getDouble("x");
                double y = results.getDouble("y");
                double z = results.getDouble("z");
                float yaw = results.getFloat("yaw");
                float pitch = results.getFloat("pitch");

                Location spawnLocation = new Location(Bukkit.getWorld(results.getString("world")), x, y, z, yaw, pitch);

                Profile profile = new Profile(profileUUID, playerUUID, name, spawnLocation);
                playerProfiles.add(profile);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not fetch player profiles: " + e.getMessage());
        }

        return playerProfiles;
    }

    public Profile getProfile(UUID playerUUID) {
        List<Profile> playerProfiles = profiles.get(playerUUID);
        if (playerProfiles != null) {
            for (Profile profile : playerProfiles) {
                if (profile.getProfileId().equals(playerUUID)) {
                    return profile;
                }
            }
        }
        return null;
    }

    public void createProfile(Player player, Profile profile) {
        try {
            Connection connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO profiles (profileUUID, playerUUID, name, user_group) VALUES (?, ?, ?, ?)"
            );
            UUID profileUUID = profile.getProfileId();
            String userGroup = profile.getGroup();
            statement.setString(1, profileUUID.toString());
            statement.setString(2, profile.getPlayerId().toString());
            statement.setString(3, profile.getName());
            if (userGroup != null) {
                statement.setString(4, userGroup);
            } else {
                statement.setString(4, "null");
            }
            statement.executeUpdate();

            // Save the spawn location
            Location spawnLocation = profile.getSpawnLocation();
            if (spawnLocation != null) {
                statement = connection.prepareStatement(
                        "INSERT INTO locations (profileUUID, world, x, y, z, pitch, yaw) VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                statement.setString(1, profileUUID.toString());
                statement.setString(2, spawnLocation.getWorld().getName());
                statement.setDouble(3, spawnLocation.getX());
                statement.setDouble(4, spawnLocation.getY());
                statement.setDouble(5, spawnLocation.getZ());
                statement.setFloat(6, spawnLocation.getPitch());
                statement.setFloat(7, spawnLocation.getYaw());
                statement.executeUpdate();
            }

            addProfile(player.getUniqueId(), profile);
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not create profile: " + e.getMessage());
        }
    }


    public void saveProfile(Player player, UUID profileUUID) {
        try {
            Connection connection = databaseManager.getConnection();
            PreparedStatement statement;

            // Save profile data
            Profile profile = getProfile(player.getUniqueId(), profileUUID);
            if (profile != null) {
                statement = connection.prepareStatement(
                        "UPDATE profiles SET health = ?, food = ?, level = ?, exp = ?, user_group = ? WHERE profileUUID = ?"
                );
                statement.setDouble(1, player.getHealth());
                statement.setInt(2, player.getFoodLevel());
                statement.setInt(3, player.getLevel());
                statement.setDouble(4, player.getExp());

                // Check if group is null
                if (profile.getGroup() == null) {
                    statement.setString(5, "null"); // Set the group as SQL NULL
                } else {
                    statement.setString(5, profile.getGroup());
                }

                statement.setString(6, profileUUID.toString());
                statement.executeUpdate();
            }


            // Save location data
            if (profile != null) {
                Location spawnLocation = profile.getSpawnLocation();
                if (spawnLocation != null) {
                    statement = connection.prepareStatement(
                            "REPLACE INTO locations (profileUUID, world, x, y, z, pitch, yaw) VALUES (?, ?, ?, ?, ?, ?, ?)"
                    );
                    statement.setString(1, profileUUID.toString());
                    statement.setString(2, spawnLocation.getWorld().getName());
                    statement.setDouble(3, spawnLocation.getX());
                    statement.setDouble(4, spawnLocation.getY());
                    statement.setDouble(5, spawnLocation.getZ());
                    statement.setFloat(6, spawnLocation.getPitch());
                    statement.setFloat(7, spawnLocation.getYaw());
                    statement.executeUpdate();
                }
            }

            // Save inventory data
            if (profile != null) {
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
            }

            //Ajout de la location du spawn
            profile.setSpawnLocation(player.getLocation());

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

    private void addProfile(UUID playerUUID, Profile profile) {
        List<Profile> playerProfiles = profiles.getOrDefault(playerUUID, new ArrayList<>());
        playerProfiles.add(profile);
        profiles.put(playerUUID, playerProfiles);
    }

    public boolean isProfileNameTaken(String profileName) {
        try {
            Connection connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) AS count FROM profiles WHERE name = ?"
            );
            statement.setString(1, profileName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                return count > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error while checking profile name availability: " + e.getMessage());
        }
        return false;
    }

    //Section pour les groupes.

//    public void applyProfile(Player player, Profile profile) {
//        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
//            User user = plugin.getLuckPermsApi().getUserManager().getUser(player.getUniqueId());
//            if (user != null) {
//                user.data().clear(NodeType.INHERITANCE::matches);  // Remove all groups
//                user.data().add(Node.builder("group." + profile.getGroup()).build());  // Add the new group
//                plugin.getLuckPermsApi().getUserManager().saveUser(user);
//            }
//        }
//    }
//
//    public void removeProfile(Player player) {
//        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
//            User user = plugin.getLuckPermsApi().getUserManager().getUser(player.getUniqueId());
//            if (user != null) {
//                user.data().clear(NodeType.INHERITANCE::matches);  // Remove all groups
//                plugin.getLuckPermsApi().getUserManager().saveUser(user);
//            }
//        }
//    }

}
