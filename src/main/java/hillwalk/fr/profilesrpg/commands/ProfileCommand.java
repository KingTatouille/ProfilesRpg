package hillwalk.fr.profilesrpg.commands;

import hillwalk.fr.profilesrpg.ProfilesRpg;
import hillwalk.fr.profilesrpg.manager.ProfileManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfileCommand implements CommandExecutor {

    private final ProfilesRpg plugin;

    public ProfileCommand(ProfilesRpg plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getMessages().get().getString("messages.not-a-player"));
            return true;
        }


        Player player = (Player) sender;
        if (args.length == 0) {
            return false; // No arguments provided, display help message
        }

        switch (args[0].toLowerCase()) {
            case "setlobby":
                handleSetLobbyCommand(player);
                break;
            case "setprofilespawn":
                handleSetProfileSpawnCommand(player);
                break;
            case "reload":
                handleReloadConfigs(player);
                break;
            default:
                player.sendMessage(plugin.getPrefix() + plugin.getMessages().get().getString("messages.unknown-command"));
                break;
        }

        return true;
    }

    private void handleSetLobbyCommand(Player player) {
        if (!player.hasPermission("profiles.setlobby")) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessages().get().getString("messages.no-permission"));
            return;
        }

        // Set the lobby location to the player's current location
        plugin.getConfig().set("lobby.location.world", player.getLocation().getWorld().getName());
        plugin.getConfig().set("lobby.location.x", player.getLocation().getX());
        plugin.getConfig().set("lobby.location.y", player.getLocation().getY());
        plugin.getConfig().set("lobby.location.z", player.getLocation().getZ());
        plugin.saveConfig();

        player.sendMessage(plugin.getPrefix() + plugin.getMessages().get().getString("messages.lobby-set"));
    }

    private void handleSetProfileSpawnCommand(Player player) {
        if (!player.hasPermission("profiles.setprofilespawn")) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessages().get().getString("messages.no-permission"));
            return;
        }

        // Set the profile spawn location to the player's current location
        plugin.getConfig().set("profile.spawn.world", player.getLocation().getWorld().getName());
        plugin.getConfig().set("profile.spawn.x", player.getLocation().getX());
        plugin.getConfig().set("profile.spawn.y", player.getLocation().getY());
        plugin.getConfig().set("profile.spawn.z", player.getLocation().getZ());
        plugin.saveConfig();

        player.sendMessage(plugin.getPrefix() + plugin.getMessages().get().getString("messages.profile-spawn-set"));
    }

    private void handleReloadConfigs(Player player) {
        if (!player.hasPermission("profiles.reload")) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessages().get().getString("messages.no-permission"));
            return;
        }

        plugin.reloadConfig();
        plugin.getMessages().reload();
        player.sendMessage(plugin.getPrefix() + "Plugin configuration reloaded successfully.");

        }

}