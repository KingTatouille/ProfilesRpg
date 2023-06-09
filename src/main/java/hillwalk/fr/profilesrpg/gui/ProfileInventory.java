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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProfileInventory {

    private ProfilesRpg plugin;
    private Player player;
    private Inventory inventory;

    public ProfileInventory(ProfilesRpg plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        createInventory();
    }

    private void createInventory() {
        FileConfiguration config = plugin.getProfileSelection().get();
        String inventoryTitle = ChatColor.translateAlternateColorCodes('&', config.getString("gui.title"));
        int inventorySize = config.getInt("gui.size");
        inventory = Bukkit.createInventory(player, inventorySize, inventoryTitle);

        ConfigurationSection itemsSection = config.getConfigurationSection("gui.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    String type = itemSection.getString("type");
                    List<Integer> positions = itemSection.getIntegerList("position");
                    String permission = itemSection.getString("permission");
                    String name = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name"));
                    List<String> lore = itemSection.getStringList("lore").stream()
                            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                            .collect(Collectors.toList());
                    int customModelData = itemSection.getInt("customModelData", 0);

                    String function = itemSection.getString("function");
                    ItemStack item = createItem(type, name, lore, customModelData, function);

                    if (permission != null) {
                        if (player.hasPermission(permission) && key.equalsIgnoreCase("vip")) {
                            for (int position : positions) {
                                inventory.setItem(position, item.clone());
                            }
                        } else if (!player.hasPermission(permission) && key.equalsIgnoreCase("no_permissions")) {
                            for (int position : positions) {
                                inventory.setItem(position, item.clone());
                            }
                        }
                    } else {
                        List<Profile> profiles = null;
                        if (key.equalsIgnoreCase("profile_slot")) {
                            profiles = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId());
                        }

                        if (key.equalsIgnoreCase("profile_slot") && profiles != null) {
                            for (int i = 0; i < Math.min(profiles.size(), positions.size()); i++) {
                                Profile profile = profiles.get(i);
                                UUID profileUUID = profile.getProfileId();
                                String replacedName = profile.getName();
                                name = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name").replace("%profile_name%", profile.getName()));
                                item = createItem(type, name, lore, customModelData, function, profileUUID);
                                inventory.setItem(positions.get(i), item.clone());
                            }
                        } else {
                            for (int position : positions) {
                                item = createItem(type, name, lore, customModelData, function);
                                inventory.setItem(position, item.clone());
                            }
                        }
                    }
                }
            }
        }
    }

    private ItemStack createItem(String type, String name, List<String> lore, int customModelData, String function, UUID profileUUID) {
        Material material = Material.matchMaterial(type);
        if (material == null) {
            material = Material.BARRIER;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        // Set the function in the item meta
        meta.getPersistentDataContainer().set(plugin.getActionKey(), PersistentDataType.STRING, function);

        // Set the profile UUID in the item meta
        meta.getPersistentDataContainer().set(plugin.getProfileKey(), PersistentDataType.STRING, profileUUID.toString());

        meta.setLore(lore);

        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(String type, String name, List<String> lore, int customModelData, String function) {
        Material material = Material.matchMaterial(type);
        if (material == null) {
            material = Material.BARRIER;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        // Set the function in the item meta
        meta.getPersistentDataContainer().set(plugin.getActionKey(), PersistentDataType.STRING, function);

        meta.setLore(lore);

        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);
        return item;
    }

    public void openInventory() {
        player.openInventory(inventory);
    }
}
