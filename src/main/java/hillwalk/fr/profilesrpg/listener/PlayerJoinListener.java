package hillwalk.fr.profilesrpg.listener;

import hillwalk.fr.profilesrpg.ProfilesRpg;
import hillwalk.fr.profilesrpg.gui.ProfileInventory;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {
    private final ProfilesRpg plugin;

    public PlayerJoinListener(ProfilesRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();


        if (player.isOp() && plugin.getLobbySpawn() && plugin.getProfileSpawn()) {
            player.sendMessage(plugin.getPrefix() + "The lobby location has not been set. Please do so by using /setlobby.");
            player.sendMessage(plugin.getPrefix() + "The profile spawn location has not been set. Please do so by using /setprofilespawn.");
            return;
        }

        player.getInventory().clear();

        // Set player in lobby status
        plugin.getPlayerLobbyStatusManager().setPlayerInLobby(player, true);

        // Set player to spectate mode to prevent movement and interaction
        player.setGameMode(GameMode.SPECTATOR);

        player.setHealth(20);
        player.setFoodLevel(20);
        player.setLevel(1);

        // Add blindness effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 255, false, false));

        //Télportation au lobby.
        player.teleport(new Location(Bukkit.getWorld(plugin.getConfig().getString("lobby.location.world")),
                plugin.getConfig().getDouble("lobby.location.x"),
                plugin.getConfig().getDouble("lobby.location.y"),
                plugin.getConfig().getDouble("lobby.location.z")));

        // Open profile selection GUI
        new BukkitRunnable() {
            @Override
            public void run() {
                ProfileInventory profileInventory = new ProfileInventory(plugin, player);
                profileInventory.openInventory();
            }
        }.runTaskLater(plugin, 1);
    }
}
