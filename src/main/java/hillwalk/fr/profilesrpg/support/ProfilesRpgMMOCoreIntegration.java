package hillwalk.fr.profilesrpg.support;

import hillwalk.fr.profilesrpg.Profile;
import hillwalk.fr.profilesrpg.ProfilesRpg;

import java.util.UUID;

public class ProfilesRpgMMOCoreIntegration {

    private ProfilesRpg plugin;

    public ProfilesRpgMMOCoreIntegration(ProfilesRpg plugin) {

        this.plugin = plugin;

    }

    public void saveMmocoreProfile(UUID playerUUID, UUID profileUUID) {
        Profile profile = plugin.getProfileManager().getProfile(playerUUID, profileUUID);
        if (profile == null) {
            this.plugin.getLogger().severe("Could not find profile with UUID: " + profileUUID);
            return;
        }

        // Sauvegarder les informations MMOCORE ici...
        // Par exemple, si vous avez un object MMOCOREProfile associé à votre object Profile :
        // MMOCOREProfile mmocoreProfile = profile.getMmocoreProfile();
        // Vous devrez sauvegarder cet object dans votre base de données, fichier ou autre.
    }

    public void loadMmocoreProfile(UUID playerUUID, UUID profileUUID) {
        Profile profile = plugin.getProfileManager().getProfile(playerUUID, profileUUID);
        if (profile == null) {
            this.plugin.getLogger().severe("Could not find profile with UUID: " + profileUUID);
            return;
        }

        // Charger les informations MMOCORE ici...
        // Par exemple, si vous avez un object MMOCOREProfile associé à votre object Profile :
        // MMOCOREProfile mmocoreProfile = chargerMmocoreProfileDepuisBaseDeDonnees(profileUUID);
        // profile.setMmocoreProfile(mmocoreProfile);
    }

}
