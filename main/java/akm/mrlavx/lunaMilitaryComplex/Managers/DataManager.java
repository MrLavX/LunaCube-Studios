package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataManager {
    private final LunaMilitaryComplex plugin;
    private File file;
    private FileConfiguration config;
    private boolean dirty = false;

    public DataManager(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) { try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); } }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() { load(); }
    public void save() { try { config.save(file); } catch (IOException e) { e.printStackTrace(); } }
    public void tick() { if (dirty) save(); }

    public void set(String path, Object value) { config.set(path, value); dirty = true; }
    public Object get(String path) { return config.get(path); }
    public String getString(String path, String def) { return config.getString(path, def); }
    public int getInt(String path, int def) { return config.getInt(path, def); }
    public double getDouble(String path, double def) { return config.getDouble(path, def); }
    public long getLong(String path, long def) { return config.getLong(path, def); }

    public void setPlayerStat(UUID uuid, String stat, int value) { config.set("players." + uuid + "." + stat, value); dirty = true; }
    public int getPlayerStat(UUID uuid, String stat, int def) { return config.getInt("players." + uuid + "." + stat, def); }
    public void addPlayerStat(UUID uuid, String stat, int amount) { setPlayerStat(uuid, stat, getPlayerStat(uuid, stat, 0) + amount); }

    public void setNextOpenTime(long time) { config.set("next-open", time); dirty = true; }
    public long getNextOpenTime() { return config.getLong("next-open", 0); }
    public void setStrikesLaunched(int count) { config.set("strikes-launched", count); dirty = true; }
    public int getStrikesLaunched() { return config.getInt("strikes-launched", 0); }
}
