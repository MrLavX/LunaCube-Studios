package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogManager {
    private final LunaMilitaryComplex plugin;
    private File file;
    private FileConfiguration config;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int index = 0;

    public LogManager(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "logs.yml");
        if (!file.exists()) { try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); } }
        config = YamlConfiguration.loadConfiguration(file);
        index = config.getInt("index", 0);
    }

    public void log(String message) {
        String time = sdf.format(new Date());
        config.set("logs." + index + ".time", time);
        config.set("logs." + index + ".message", message);
        index++;
        config.set("index", index);
        save();
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[LOG] " + time + " | " + message);
        }
    }

    public void log(String type, String detail) {
        log("[" + type + "] " + detail);
    }

    public List<String> getLogs(int limit) {
        List<String> logs = new ArrayList<>();
        int start = Math.max(0, index - limit);
        for (int i = index - 1; i >= start; i--) {
            String time = config.getString("logs." + i + ".time", "?");
            String msg = config.getString("logs." + i + ".message", "?");
            logs.add(HexUtil.color("&7[" + time + "] &f" + msg));
        }
        return logs;
    }

    public void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
