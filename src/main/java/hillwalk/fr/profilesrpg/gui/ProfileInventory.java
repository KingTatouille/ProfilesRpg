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
        int inventorySize = config.getInt("size");
        String inventoryTitle = ChatColor.translateAlternateColorCodes('&', config.getString("title"));
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryTitle);

        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);

            Material itemType = Material.getMaterial(itemSection.getString("material"));
            ItemStack item = new ItemStack(itemType, 1);
            ItemMeta meta = item.getItemMeta();

            String displayName = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name"));
            meta.setDisplayName(displayName);

            List<String> lore = itemSection.getStringList("lore");
            meta.setLore(lore);

            String action = itemSection.getString("action");
            if (action != null) {
                meta.getPersistentDataContainer().set(plugin.getActionKey(), PersistentDataType.STRING, action);
            }

            item.setItemMeta(meta);
            inventory.setItem(itemSection.getInt("slot"), item);
        }

        player.openInventory(inventory);
    }
}
