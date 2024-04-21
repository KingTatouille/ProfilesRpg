package hillwalk.fr.profilesrpg.listener;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.ProfilesRpg;
import hillwalk.fr.profilesrpg.manager.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class ChatListener implements Listener {
    private final ProfilesRpg plugin;
    private final ProfileManager profileManager;

    public ChatListener(ProfilesRpg plugin, ProfileManager profileManager) {
        this.plugin = plugin;
        this.profileManager = profileManager;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();

        if (event.getPlayer().equals(player)) {
            event.setCancelled(true); // Annule l'événement de chat pour empêcher l'affichage du message
            String profileName = event.getMessage(); // Obtient le nom du profil saisi par le joueur

            //Vérifiez si le nom du profile est déjà prit.
            if (plugin.getProfileManager().isProfileNameTaken(event.getMessage())) {
                String invalidNickname = plugin.getMessage("already_name");
                player.sendMessage(invalidNickname);
                return;
            }

            // Vérifiez si le nom du profil est valide (par exemple, ne contient pas de caractères spéciaux non autorisés)
            if (!isValidProfileName(profileName)) {
                String invalidProfileNameMsg = plugin.getMessage("invalid_profile_name");
                player.sendMessage(invalidProfileNameMsg);
                return;
            }


            // Le joueur n'a pas de profil existant, créer un nouveau profil
            Location spawnLocation = player.getLocation(); // Utiliser la position actuelle du joueur comme spawnLocation pour le nouveau profil
            UUID profileUUID = UUID.randomUUID();

            plugin.selectProfile(player.getUniqueId(), profileUUID); //On sauvegarde le joueur et son UUID

            Profile profile = new Profile(profileUUID, player.getUniqueId(), profileName, spawnLocation); // Créer un nouvel objet Profile

            profileManager.createProfile(player, profile);

            player.setDisplayName(profileName); //On set le pseudo du joueur.

            String profileCreatedMsg = plugin.getMessage("profile_created");

            profileCreatedMsg = profileCreatedMsg.replace("%profile_name%", profileName);

            player.sendMessage(profileCreatedMsg);

            //On téléporte le joueur au spawn des profiles défini plutôt par l'administrateur.
            try {
                player.teleport(new Location(
                        Bukkit.getWorld(plugin.getConfig().getString("profile.spawn.world")),
                        plugin.getConfig().getDouble("profile.spawn.x"),
                        plugin.getConfig().getDouble("profile.spawn.y"),
                        plugin.getConfig().getDouble("profile.spawn.z")
                ));
            } catch (Exception e) {
                // Gérer l'erreur en affichant un message de journalisation ou en prenant une autre action
                plugin.getLogger().warning("Error teleporting the player to the profile location: " + e.getMessage());
            }

            // On ajoute le joueur au lobby des profiles
            plugin.getPlayerLobbyStatusManager().setPlayerInLobby(player, false);

            // On met le joueur en survival
            player.setGameMode(GameMode.SURVIVAL);

            // On enlève l'effet de la potion.
            player.removePotionEffect(PotionEffectType.BLINDNESS);


            // Désenregistre l'écouteur d'événements de chat
            HandlerList.unregisterAll(this);
        }
    }



    private boolean isValidProfileName(String profileName) {
        // vérification : le nom du profil ne doit pas être vide et ne doit pas contenir de caractères spéciaux
        return !profileName.isEmpty() && profileName.matches(plugin.getConfig().getString("characters_authorized"));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        String inventoryName = event.getView().getTitle();
        if (inventoryName.equalsIgnoreCase(plugin.getProfileSelection().get().getString("gui.title"))) {
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(event.getInventory()));
        }
    }

}
