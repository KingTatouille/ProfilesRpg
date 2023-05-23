package hillwalk.fr.profilesrpg.listener;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.ProfilesRpg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.ChatColor;

import java.util.UUID;

public class InventoryClick implements Listener {

    private enum ItemFunction {
        NO_PERMISSIONS,
        PROFILE,
        CREATE_PROFILE,
        DECORATION,
        DISCONNECT
    }

    private ProfilesRpg plugin;

    public InventoryClick(ProfilesRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();

        if (inventory == null || !inventory.equals(player.getOpenInventory().getTopInventory())) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        event.setCancelled(true);

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) {
            plugin.getLogger().info("ItemMeta is null. Ignoring event.");
            return;
        }

        String displayName = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
        ConfigurationSection itemsSection = plugin.getProfileSelection().get().getConfigurationSection("gui.items");

        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                if (itemsSection.getString(key + ".name").toLowerCase().equals(displayName)) {
                    // Perform action based on function
                    ItemFunction function = ItemFunction.valueOf(itemsSection.getString(key + ".function").toUpperCase());

                    switch (function) {
                        case NO_PERMISSIONS:
                            player.sendMessage(ChatColor.RED + "You don't have VIP permission to access these profile slots.");
                            break;
                        case PROFILE:
                            if (!meta.getPersistentDataContainer().has(plugin.getProfileKey(), PersistentDataType.STRING)) {
                                plugin.getLogger().info("ItemMeta does not contain a profile UUID. Ignoring event.");
                                return;
                            }
                            String profileUUIDString = meta.getPersistentDataContainer().get(plugin.getProfileKey(), PersistentDataType.STRING);
                            UUID profileUUID = UUID.fromString(profileUUIDString);
                            Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId(), profileUUID);
                            if (profile == null) {
                                plugin.getLogger().info("No profile found for UUID: " + profileUUIDString);
                                player.sendMessage("Profile not found.");
                                return;
                            }
                            plugin.getLogger().info("Detected profile: " + profile.getName());
                            profile.applyToPlayer(player);
                            plugin.getLogger().info("Loading profile: " + profile.getName());
                            player.sendMessage("Profile " + profile.getName() + " loaded.");
                            break;
                        case CREATE_PROFILE:
                            player.closeInventory();
                            ChatListener chatListener = new ChatListener(plugin, plugin.getProfileManager());
                            Bukkit.getPluginManager().registerEvents(chatListener, plugin);
                            plugin.getLogger().info("Creating new profile.");
                            break;
                        case DECORATION:
                            // No specific logic for decoration
                            break;
                        case DISCONNECT:
                            player.kickPlayer("You have been disconnected.");
                            plugin.getLogger().info("Player disconnected.");
                            break;
                        default:
                            plugin.getLogger().info("Unknown function detected: " + function);
                            break;
                    }
                    return;
                }
            }
        }
    }
}
