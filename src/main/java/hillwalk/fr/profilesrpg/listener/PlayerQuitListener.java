package hillwalk.fr.profilesrpg.listener;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.ProfilesRpg;
import hillwalk.fr.profilesrpg.manager.ProfileManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener implements Listener {
    private final ProfilesRpg plugin;

    public PlayerQuitListener(ProfilesRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ProfileManager profileManager = plugin.getProfileManager();
        Player player = event.getPlayer();

        UUID profileUUID = plugin.getSelectedProfile(player.getUniqueId());

        if (profileUUID != null) {
            Profile profile = profileManager.getProfile(player.getUniqueId(), profileUUID);

            if (profile != null) {
                // Sauvegarder le profil du joueur
                profileManager.saveProfile(player, profile.getProfileId());
            } else {
                // Gérez le cas où aucun profil n'a été trouvé pour le joueur
                player.sendMessage("No profile found.");
            }
        } else {
            // Gérez le cas où aucun profil n'a été sélectionné pour le joueur
            player.sendMessage("No profile selected.");
        }
    }
}
