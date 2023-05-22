
package hillwalk.fr.profilesrpg.gui;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.ProfilesRpg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

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

        ConfigurationSection itemsSection = config.getConfigurationSection("gui.items");
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);

            Material itemType = Material.matchMaterial(itemSection.getString("type"));
            if (itemType == null) {
                plugin.getLogger().warning("Invalid material specified in configuration for item " + key);
                continue;
            }

            ItemStack item = new ItemStack(itemType);
            ItemMeta meta = item.getItemMeta();

            String itemName = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name"));
            meta.setDisplayName(itemName);

            List<String> lore = new ArrayList<>();
            itemSection.getStringList("lore").forEach(line -> lore.add(ChatColor.translateAlternateColorCodes('&', line)));
            meta.setLore(lore);

            int customModelData = itemSection.getInt("customModelData", 0);
            if (customModelData != 0) {
                meta.setCustomModelData(customModelData);
            }

            item.setItemMeta(meta);

            if (itemSection.contains("permission") && !player.hasPermission(itemSection.getString("permission"))) {
                continue;
            }

            List<Integer> positions = itemSection.getIntegerList("position");
            for (int position : positions) {
                inventory.setItem(position, item);
            }
        }

        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        ItemStack profileSlot = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta profileSlotMeta = profileSlot.getItemMeta();

        String profileName = ChatColor.translateAlternateColorCodes('&', profile.getName());
        profileSlotMeta.setDisplayName(profileName);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RED + "Shift + Right-Click to delete this profile");

        profileSlotMeta.setLore(lore);

        profileSlot.setItemMeta(profileSlotMeta);

        inventory.setItem(0, profileSlot);

        player.openInventory(inventory);
    }

}
