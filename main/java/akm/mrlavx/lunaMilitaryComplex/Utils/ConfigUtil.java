package akm.mrlavx.lunaMilitaryComplex.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

public class ConfigUtil {
    public static FileConfiguration load(JavaPlugin p, String n) {
        File f = new File(p.getDataFolder(), n);
        if (!f.exists()) {
            try {
                p.saveResource(n, false);
            } catch (Exception e) {
                try { f.createNewFile(); } catch (IOException io) { io.printStackTrace(); }
            }
        }
        return YamlConfiguration.loadConfiguration(f);
    }
    public static void save(FileConfiguration c, File f) { 
        try { c.save(f); } catch (IOException e) { e.printStackTrace(); } 
    }
}
