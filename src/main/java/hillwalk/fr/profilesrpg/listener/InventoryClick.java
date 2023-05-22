package hillwalk.fr.profilesrpg.listener;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.ProfilesRpg;
import hillwalk.fr.profilesrpg.manager.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class InventoryClick implements Listener {

    private ProfilesRpg plugin;
    public InventoryClick(ProfilesRpg plugin){
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

        int slot = event.getSlot();

        FileConfiguration config = plugin.getProfileSelection().get();
        ConfigurationSection itemsSection = config.getConfigurationSection("gui.items");

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null) {
                continue;
            }

            List<Integer> positions = itemSection.getIntegerList("position");
            if (positions.contains(slot)) {
                handleItemClick(player, itemSection, player.getDisplayName());
                break;
            }
        }
    }

    private void handleItemClick(Player player, ConfigurationSection itemSection, String name) {
        String action = itemSection.getString("action");
        if (action == null) {
            return;
        }

        ProfileManager profileManager = plugin.getProfileManager(); // Declare the variable outside the switch
        UUID playerId = player.getUniqueId(); // Declare the playerId outside the switch

        switch (action.toLowerCase()) {
            case "create_profile":
                // Ferme l'inventaire
                player.closeInventory();

                // Enregistre l'écouteur de chat
                ChatListener chatListener = new ChatListener(plugin, profileManager);
                Bukkit.getPluginManager().registerEvents(chatListener, plugin);
                break;

            case "load_profile":
                Profile loadProfile = profileManager.getProfile(playerId);

                if (loadProfile != null) {
                    loadProfile.applyToPlayer(player);
                    String loadedMessage = String.format(plugin.getMessages().get().getString("profile_loaded"), name);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', loadedMessage));
                } else {
                    String notFoundMessage = String.format(plugin.getMessages().get().getString("profile_not_found"), name);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', notFoundMessage));
                }
                break;

            case "delete_profile":
                profileManager.deleteProfile(playerId);
                break;

            case "disconnect":
                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().get().getString("deconnexion")));
                break;

            default:
                break;
        }
    }



}
