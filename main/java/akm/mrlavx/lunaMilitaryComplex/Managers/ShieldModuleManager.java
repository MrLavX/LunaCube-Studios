package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.ShieldModule;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShieldModuleManager {
    private final LunaMilitaryComplex plugin;
    private final Map<String, ShieldModule> modules = new HashMap<>();
    private File file;
    private FileConfiguration config;
    private double energyDrainPerSecond = 1.0;
    private double rechargeRate = 2.0;

    public ShieldModuleManager(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        modules.clear();
        file = new File(plugin.getDataFolder(), "shield_modules.yml");
        if (!file.exists()) { try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); } }
        config = YamlConfiguration.loadConfiguration(file);
        energyDrainPerSecond = plugin.getConfig().getDouble("shield.settings.energy-drain", 1.0);
        rechargeRate = plugin.getConfig().getDouble("shield.settings.recharge-rate", 2.0);
        ConfigurationSection section = config.getConfigurationSection("modules");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection s = section.getConfigurationSection(key);
                if (s != null) loadModule(key, s);
            }
        }
    }

    private void loadModule(String id, ConfigurationSection s) {
        UUID owner = UUID.fromString(s.getString("owner"));
        org.bukkit.World w = org.bukkit.Bukkit.getWorld(s.getString("world", "world"));
        Location loc = new Location(w, s.getDouble("x"), s.getDouble("y"), s.getDouble("z"));
        double maxEnergy = s.getDouble("max-energy", 1000);
        int radius = s.getInt("radius", 50);
        ShieldModule module = new ShieldModule(id, owner, loc, maxEnergy, radius);
        module.setEnergy(s.getDouble("energy", maxEnergy));
        module.setActive(s.getBoolean("active", true));
        modules.put(id, module);
    }

    public void save() {
        config.set("modules", null);
        ConfigurationSection section = config.createSection("modules");
        for (ShieldModule m : modules.values()) {
            ConfigurationSection s = section.createSection(m.getId());
            s.set("owner", m.getOwner().toString());
            s.set("world", m.getLocation().getWorld().getName());
            s.set("x", m.getLocation().getX());
            s.set("y", m.getLocation().getY());
            s.set("z", m.getLocation().getZ());
            s.set("energy", m.getEnergy());
            s.set("max-energy", m.getMaxEnergy());
            s.set("radius", m.getRadius());
            s.set("active", m.isActive());
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public void placeModule(Player player, Location location) {
        String id = "shield_" + System.currentTimeMillis();
        double maxEnergy = plugin.getConfig().getDouble("shield.settings.default-max-energy", 1000);
        int radius = plugin.getConfig().getInt("shield.settings.default-radius", 50);
        ShieldModule module = new ShieldModule(id, player.getUniqueId(), location, maxEnergy, radius);
        modules.put(id, module);
        save();
        plugin.getLogManager().log("SHIELD", player.getName() + " placed module at " + location);
        plugin.getHologramManager().createShieldHologram(module);
        player.sendMessage(HexUtil.color("&aЗащитный модуль установлен!"));
    }

    public void removeModule(String id) {
        ShieldModule m = modules.remove(id);
        if (m != null) {
            plugin.getHologramManager().removeShieldHologram(id);
            save();
        }
    }

    public void tick() {
        for (ShieldModule m : modules.values()) {
            if (!m.isActive()) continue;
            if (m.getEnergy() > 0) {
                m.setEnergy(m.getEnergy() - energyDrainPerSecond / 4);
            } else {
                m.setActive(false);
            }
            plugin.getHologramManager().updateShieldHologram(m);
        }
    }

    public void rechargeModule(String id, double amount) {
        ShieldModule m = modules.get(id);
        if (m != null) {
            m.setEnergy(m.getEnergy() + amount);
            if (m.getEnergy() > 0 && !m.isActive()) m.setActive(true);
            save();
        }
    }

    public boolean isProtected(Location location) {
        for (ShieldModule m : modules.values()) {
            if (!m.isActive() || m.getEnergy() <= 0) continue;
            if (m.getLocation().getWorld().equals(location.getWorld()) &&
                m.getLocation().distance(location) <= m.getRadius()) {
                return true;
            }
        }
        return false;
    }

    public ShieldModule getModule(String id) { return modules.get(id); }
    public Collection<ShieldModule> getAllModules() { return modules.values(); }
}
