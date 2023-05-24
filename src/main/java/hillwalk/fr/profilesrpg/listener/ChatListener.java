package hillwalk.fr.profilesrpg.listener;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.ProfilesRpg;
import hillwalk.fr.profilesrpg.manager.ProfileManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListener implements Listener {
    private final ProfilesRpg plugin;
    private final ProfileManager profileManager;

    public ChatListener(ProfilesRpg plugin, ProfileManager profileManager) {
        this.plugin = plugin;
        this.profileManager = profileManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (event.getPlayer().equals(player)) {
            event.setCancelled(true); // Annule l'événement de chat pour empêcher l'affichage du message
            String profileName = event.getMessage(); // Obtient le nom du profil saisi par le joueur

            //Vérifiez si le nom du profile est déjà prit.
            if(plugin.getProfileManager().isProfileNameTaken(event.getMessage())){
                String invalidNickname = ChatColor.translateAlternateColorCodes('&', plugin.getMessages().get().getString("messages.already_name"));
                player.sendMessage(invalidNickname);
                return;
            }

            // Vérifiez si le nom du profil est valide (par exemple, ne contient pas de caractères spéciaux non autorisés)
            if (!isValidProfileName(profileName)) {
                String invalidProfileNameMsg = ChatColor.translateAlternateColorCodes('&', plugin.getMessages().get().getString("messages.invalid_profile_name"));
                player.sendMessage(invalidProfileNameMsg);
                return;
            }

            // Récupérer le profil existant du joueur (s'il en a déjà un)
            ProfileManager profileManager = plugin.getProfileManager();
            Profile profile = profileManager.getProfile(player.getUniqueId());

            if (profile == null) {
                // Le joueur n'a pas de profil existant, créer un nouveau profil
                Location spawnLocation = player.getLocation(); // Utiliser la position actuelle du joueur comme spawnLocation pour le nouveau profil
                profile = new Profile(profile.getProfileId(), player.getUniqueId(), profileName, spawnLocation); // Créer un nouvel objet Profile
                profileManager.createProfile(player, profile);
                String profileCreatedMsg = ChatColor.translateAlternateColorCodes('&', plugin.getMessages().get().getString("messages.profile_created"));
                profileCreatedMsg = profileCreatedMsg.replace("%profile_name%", profileName);
                player.sendMessage(profileCreatedMsg);
            }

            // Désenregistre l'écouteur d'événements de chat
            HandlerList.unregisterAll(this);
        }
    }



    private boolean isValidProfileName(String profileName) {
        // vérification : le nom du profil ne doit pas être vide et ne doit pas contenir de caractères spéciaux
        return !profileName.isEmpty() && profileName.matches(plugin.getConfig().getString("characters_authorized"));
    }

}
