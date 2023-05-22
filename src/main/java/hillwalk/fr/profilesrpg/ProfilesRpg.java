package hillwalk.fr.profilesrpg;

import hillwalk.fr.profilesrpg.commands.ProfileCommand;
import hillwalk.fr.profilesrpg.database.CustomConfig;
import hillwalk.fr.profilesrpg.database.DatabaseManager;
import hillwalk.fr.profilesrpg.listener.ChatListener;
import hillwalk.fr.profilesrpg.listener.InventoryClick;
import hillwalk.fr.profilesrpg.listener.PlayerJoinListener;
import hillwalk.fr.profilesrpg.listener.PlayerQuitListener;
import hillwalk.fr.profilesrpg.manager.PlayerLobbyStatusManager;
import hillwalk.fr.profilesrpg.manager.ProfileManager;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class ProfilesRpg extends JavaPlugin {
    private DatabaseManager databaseManager;
    private ProfileManager profileManager;
    private CustomConfig profileSelection;
    private LuckPerms luckPermsApi;
    private CustomConfig messages;
    public static String prefix;
    private PlayerLobbyStatusManager playerLobbyStatusManager;

    @Override
    public void onEnable() {
        playerLobbyStatusManager = new PlayerLobbyStatusManager();

        profileSelection = new CustomConfig(this, "gui/profile_selection.yml");
        profileSelection.setup();

        messages = new CustomConfig(this, "messages/messages.yml");
        messages.setup();

        this.databaseManager = new DatabaseManager(this);
        this.profileManager = new ProfileManager(this, this.databaseManager);

        this.luckPermsApi = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();

        if (getServer().getPluginManager().getPlugin("MMOCore") == null) {
            getLogger().info("MMOCore not detected.");
        }

        // Sauvegarde de la config par defaut
        saveDefaultConfig();

        // Register commands
        getLogger().info("Commands charged!");
        getCommand("profile").setExecutor(new ProfileCommand(this));

        // Register events
        getLogger().info("Events charged!");
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClick(this), this);

        prefix = ChatColor.translateAlternateColorCodes('&',  messages.get().getString("messages.prefix"));
    }


    @Override
    public void onDisable() {
        this.databaseManager.disconnect();
    }

    public CustomConfig getProfileSelection() {
        return profileSelection;
    }
    public CustomConfig getMessages() {
        return messages;
    }

    public PlayerLobbyStatusManager getPlayerLobbyStatusManager() {
        return playerLobbyStatusManager;
    }

    public LuckPerms getLuckPermsApi() {
        if (this.luckPermsApi == null) {
            if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
                RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
                if (provider != null) {
                    this.luckPermsApi = provider.getProvider();
                }
            }
        }
        return this.luckPermsApi;
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public ProfileManager getProfileManager() {
        return this.profileManager;
    }

    public boolean getLobbySpawn(){

        String lobbyWorld = getConfig().getString("lobby.location.world");
        double lobbyX = getConfig().getDouble("lobby.location.x");
        double lobbyY = getConfig().getDouble("lobby.location.y");
        double lobbyZ = getConfig().getDouble("lobby.location.z");

        if (lobbyWorld.isEmpty() || lobbyX == 0 || lobbyY == 0 || lobbyZ == 0) {
            getLogger().warning("Lobby location has not been set in the config yet.");
            return true;
        }

        return false;
    }

    public boolean getProfileSpawn(){

        String profileWorld = getConfig().getString("profile.spawn.world");
        double profileX = getConfig().getDouble("profile.spawn.x");
        double profileY = getConfig().getDouble("profile.spawn.y");
        double profileZ = getConfig().getDouble("profile.spawn.z");

        if (profileWorld.isEmpty() || profileX == 0 || profileY == 0 || profileZ == 0) {
            getLogger().warning("Profile spawn location has not been set in the config yet.");
            return true;
        }

        return false;
    }


}

