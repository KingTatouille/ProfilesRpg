package hillwalk.fr.profilesrpg.database;

import hillwalk.fr.profilesrpg.ProfilesRpg;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomConfig {

    private File file;
    private FileConfiguration customFile;
    private String fileName;
    private ProfilesRpg plugin;

    public CustomConfig(ProfilesRpg plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }

        customFile = YamlConfiguration.loadConfiguration(file);
    }


    public FileConfiguration get() {
        customFile = YamlConfiguration.loadConfiguration(file);
        return customFile;
    }

    public void save() {
        try {
            customFile.save(file);
        } catch (IOException e) {
            // handle error here
            e.printStackTrace();
        }
    }

    public void reload() {
        customFile = YamlConfiguration.loadConfiguration(file);
    }
}
