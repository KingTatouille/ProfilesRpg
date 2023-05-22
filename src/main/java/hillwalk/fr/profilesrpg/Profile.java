package hillwalk.fr.profilesrpg;

import net.luckperms.api.model.group.Group;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Profile {
    private final UUID profileId;
    private final UUID playerId;

    private String group;
    private String name;
    private Location spawnLocation;
    private double health;
    private int foodLevel;
    private int level;
    private float exp;
    private GameMode gameMode;
    private long lastUsed;

    public Profile(UUID playerId, String name, Location spawnLocation) {
        this.profileId = UUID.randomUUID();
        this.playerId = playerId;
        this.name = name;
        this.spawnLocation = spawnLocation;
        this.lastUsed = System.currentTimeMillis();
    }

    public void updateFromPlayer(Player player) {
        this.spawnLocation = player.getLocation();
        this.health = player.getHealth();
        this.foodLevel = player.getFoodLevel();
        this.level = player.getLevel();
        this.exp = player.getExp();
        this.gameMode = player.getGameMode();
        this.lastUsed = System.currentTimeMillis();
    }

    public void applyToPlayer(Player player) {
        player.teleport(this.spawnLocation);
        player.setHealth(this.health);
        player.setFoodLevel(this.foodLevel);
        player.setLevel(this.level);
        player.setExp(this.exp);
        player.setGameMode(this.gameMode);
    }

    public UUID getProfileId() {
        return this.profileId;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup(){
        return this.group;
    }
    public void setGroup(String group){
        this.group = group;
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public double getHealth() {
        return this.health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getExp() {
        return this.exp;
    }

    public void setExp(float exp) {
        this.exp = exp;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public long getLastUsed() {
        return this.lastUsed;
    }
}
