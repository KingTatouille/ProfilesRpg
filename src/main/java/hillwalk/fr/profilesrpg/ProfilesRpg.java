package hillwalk.fr.profilesrpg;

import hillwalk.fr.profilesrpg.database.CustomConfig;
import hillwalk.fr.profilesrpg.database.DatabaseManager;
import hillwalk.fr.profilesrpg.manager.ProfileManager;
import org.bukkit.plugin.java.JavaPlugin;


public class ProfilesRpg extends JavaPlugin {
    private DatabaseManager databaseManager;
    private ProfileManager profileManager;
    private CustomConfig profileSelection;

    @Override
    public void onEnable() {

        profileSelection = new CustomConfig(this, "gui/profileSelection.yml");
        profileSelection.setup();

        this.databaseManager = new DatabaseManager(this);
        this.profileManager = new ProfileManager(this, this.databaseManager);
        // Register commands, events etc.
    }

    @Override
    public void onDisable() {
        this.databaseManager.disconnect();
    }

    public CustomConfig getProfileSelection() {
        return profileSelection;
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public ProfileManager getProfileManager() {
        return this.profileManager;
    }
}

