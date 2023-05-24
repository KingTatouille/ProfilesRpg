package hillwalk.fr.profilesrpg.gui;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.ProfilesRpg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfileInventory {
    private ProfilesRpg plugin;

    public ProfileInventory(ProfilesRpg plugin) {
        this.plugin = plugin;
    }

    public void openProfileInventory(Player player) {
        FileConfiguration config = plugin.getProfileSelection().get();
        int inventorySize = config.getInt("gui.size");
        String inventoryTitle = ChatColor.translateAlternateColorCodes('&', config.getString("gui.title"));
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryTitle);

        // Load player profiles
        UUID playerUUID = player.getUniqueId();
        List<Profile> playerProfiles = plugin.getProfileManager().getPlayerProfiles(playerUUID);
        if (playerProfiles != null) {
            int profileSlotIndex = 0;
            for (Profile profile : playerProfiles) {
                if (profileSlotIndex >= 10) {
                    // Reached the maximum number of profile slots
                    break;
                }

                ItemStack item = createProfileItem(profile);
                if (item != null) {
                    // Add the profile slot item to the inventory at the specified position
                    inventory.setItem(profileSlotIndex, item);
                    profileSlotIndex++;
                    plugin.getLogger().info("Added profile item to inventory for player: " + player.getName() + ", Profile: " + profile.getName());
                } else {
                    plugin.getLogger().warning("Failed to create profile item for player: " + player.getName() + ", Profile: " + profile.getName());
                }
            }
        } else {
            plugin.getLogger().warning("No profiles found for player with UUID: " + playerUUID);
        }

        // Load the rest of the items from config
        ConfigurationSection itemsSection = config.getConfigurationSection("gui.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                if (key.equals("profile_slot")) {
                    // Skip processing the profile slot item
                    continue;
                }

                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    ItemStack item = createItemFromConfig(itemSection);
                    if (item != null) {
                        List<Integer> positions = itemSection.getIntegerList("position");
                        for (Integer position : positions) {
                            inventory.setItem(position, item);
                        }
                    } else {
                        plugin.getLogger().warning("Failed to create item from config for key: " + key);
                    }
                } else {
                    plugin.getLogger().warning("Invalid item section for key: " + key);
                }
            }
        } else {
            plugin.getLogger().warning("No items found in config section: gui.items");
        }

        player.openInventory(inventory);
    }

    private ItemStack createProfileItem(Profile profile) {
        FileConfiguration config = plugin.getProfileSelection().get();
        ConfigurationSection profileSlotSection = config.getConfigurationSection("gui.items.profile_slot");
        if (profileSlotSection != null) {
            ItemStack item = createItemFromConfig(profileSlotSection);
            if (item != null) {
                ItemMeta meta = item.getItemMeta();

                String displayName = ChatColor.translateAlternateColorCodes('&', profile.getName());
                meta.setDisplayName(displayName);

                List<String> lore = new ArrayList<>();
                lore.add("Level: " + profile.getLevel());

                meta.setLore(lore);
                item.setItemMeta(meta);

                return item;
            } else {
                plugin.getLogger().warning("Failed to create profile item for Profile: " + profile.getName());
            }
        } else {
            plugin.getLogger().warning("Invalid profile slot section in config");
        }
        return null;
    }

    private ItemStack createItemFromConfig(ConfigurationSection itemSection) {
        Material itemType = Material.getMaterial(itemSection.getString("type"));
        if (itemType != null) {
            ItemStack item = new ItemStack(itemType, 1);
            ItemMeta meta = item.getItemMeta();

            String displayName = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name"));
            meta.setDisplayName(displayName);

            List<String> lore = itemSection.getStringList("lore");
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);

            String action = itemSection.getString("function");
            if (action != null) {
                meta.getPersistentDataContainer().set(plugin.getActionKey(), PersistentDataType.STRING, action);
            }

            item.setItemMeta(meta);
            return item;
        } else {
            plugin.getLogger().warning("Invalid material type specified in config");
        }
        return null;
    }
}
