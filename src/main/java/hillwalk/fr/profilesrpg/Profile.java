package hillwalk.fr.profilesrpg;

import org.bukkit.Location;

import java.util.UUID;

public class Profile {
    private final UUID playerId;
    private String name;
    private Location spawnLocation;


    public Profile(UUID playerId, String name, Location spawnLocation ) {
        this.playerId = playerId;
        this.name = name;
        this.spawnLocation = spawnLocation;
    }


}
