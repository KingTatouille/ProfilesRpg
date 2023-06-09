package hillwalk.fr.profilesrpg.gui;

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
import java.util.stream.Collectors;

public class ClassInventory {

    private ProfilesRpg plugin;
    private Player player;
    private Inventory inventory;

    public ClassInventory(ProfilesRpg plugin, Player player) {
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
                    String name = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name"));
                    List<String> lore = itemSection.getStringList("lore").stream()
                            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                            .collect(Collectors.toList());
                    int customModelData = itemSection.getInt("customModelData", 0);
                    String command = itemSection.getString("command");

                    ItemStack item = createItem(type, name, lore, customModelData, command);

                    for (int position : positions) {
                        inventory.setItem(position, item.clone());
                    }
                }
            }
        }
    }

    private ItemStack createItem(String type, String name, List<String> lore, int customModelData, String command) {
        Material material = Material.matchMaterial(type);
        if (material == null) {
            material = Material.BARRIER;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        // Set the command in the item meta
        meta.getPersistentDataContainer().set(plugin.getActionKey(), PersistentDataType.STRING, command);

        meta.setLore(lore);

        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void executeCommand(Player player, String command) {
        if (command != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
        }
    }

    public void openInventory() {
        player.openInventory(inventory);
    }
}
