package hillwalk.fr.profilesrpg.listener;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.ProfilesRpg;
import hillwalk.fr.profilesrpg.manager.ProfileManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final ProfilesRpg plugin;

    public PlayerQuitListener(ProfilesRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ProfileManager profileManager = plugin.getProfileManager();
        Profile profile = profileManager.getProfile(event.getPlayer().getUniqueId());

        if (profile != null) {
            // Sauvegarder le profil du joueur
            profileManager.saveProfile(event.getPlayer(), profile.getProfileId());
        }
    }
}
