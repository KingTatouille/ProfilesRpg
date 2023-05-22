package hillwalk.fr.profilesrpg.listener;

import hillwalk.fr.profilesrpg.ProfilesRpg;
import hillwalk.fr.profilesrpg.gui.ProfileInventory;
import org.bukkit.GameMode;
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
            player.sendMessage(plugin.prefix + "The lobby location has not been set. Please do so by using /setlobby.");
            player.sendMessage(plugin.prefix + "The profile spawn location has not been set. Please do so by using /setprofilespawn.");
            return;
        }

        // Set player in lobby status
        plugin.getPlayerLobbyStatusManager().setPlayerInLobby(player, true);

        // Set player to spectate mode to prevent movement and interaction
        player.setGameMode(GameMode.SPECTATOR);

        // Add blindness effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 255, false, false));

        // Open profile selection GUI
        new BukkitRunnable() {
            @Override
            public void run() {
                ProfileInventory profileInventory = new ProfileInventory(plugin);
                profileInventory.openProfileInventory(player);
            }
        }.runTaskLater(plugin, 1);
    }
}
