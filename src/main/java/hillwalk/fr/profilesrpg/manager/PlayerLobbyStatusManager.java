package hillwalk.fr.profilesrpg.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PlayerLobbyStatusManager {
    private final Set<Player> lobbyPlayers = new HashSet<>();

    public boolean isPlayerInLobby(Player player) {
        return lobbyPlayers.contains(player);
    }

    public void setPlayerInLobby(Player player, boolean inLobby) {
        if (inLobby) {
            lobbyPlayers.add(player);
        } else {
            lobbyPlayers.remove(player);
        }
    }
}