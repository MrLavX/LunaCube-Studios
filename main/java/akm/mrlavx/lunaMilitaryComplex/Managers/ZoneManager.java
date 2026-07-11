package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.Zone;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import akm.mrlavx.lunaMilitaryComplex.Utils.ParticleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ZoneManager {
    private final LunaMilitaryComplex plugin;
    private final Map<String, Zone> zones = new HashMap<>();
    private final Map<UUID, String> playerZones = new HashMap<>();
    private final Map<UUID, BossBar> playerBossBars = new HashMap<>();
    private final Map<String, Long> zoneEffects = new HashMap<>(); // zone name -> effect end time
    private File zonesFile;
    private FileConfiguration zonesConfig;

    public ZoneManager(LunaMilitaryComplex plugin) { this.plugin = plugin; loadZones(); }
    public void reload() { zones.clear(); playerZones.clear(); playerBossBars.values().forEach(BossBar::removeAll); playerBossBars.clear(); zoneEffects.clear(); loadZones(); }

    private void loadZones() {
        zonesFile = new File(plugin.getDataFolder(), "zones.yml");
        if (!zonesFile.exists()) { try { zonesFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); } }
        zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
        ConfigurationSection section = zonesConfig.getConfigurationSection("zones");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection zs = section.getConfigurationSection(key);
                if (zs != null) { Zone z = new Zone(zs); zones.put(z.getName().toLowerCase(), z); }
            }
        }
    }

    public void saveZones() {
        zonesConfig.set("zones", null);
        ConfigurationSection section = zonesConfig.createSection("zones");
        for (Zone z : zones.values()) {
            ConfigurationSection zs = section.createSection(z.getName().toLowerCase().replace(" ", "_"));
            z.save(zs);
        }
        try { zonesConfig.save(zonesFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void createZone(String name, Location pos1, Location pos2) {
        Zone z = new Zone(name, pos1.getWorld(), pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(), pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
        zones.put(name.toLowerCase(), z); saveZones();
    }
    public void deleteZone(String name) { zones.remove(name.toLowerCase()); saveZones(); }
    public Zone getZone(String name) { return zones.get(name.toLowerCase()); }
    public Collection<Zone> getAllZones() { return zones.values(); }
    public boolean zoneExists(String name) { return zones.containsKey(name.toLowerCase()); }
    public void toggleZone(String name) { Zone z = getZone(name); if (z != null) { z.setEnabled(!z.isEnabled()); saveZones(); } }
    public Zone getZoneAt(Location loc) { for (Zone z : zones.values()) if (z.isEnabled() && z.contains(loc)) return z; return null; }

    // Zone effects - enable particles around zone for X minutes
    public void enableZoneEffect(String zoneName, int minutes) {
        zoneEffects.put(zoneName.toLowerCase(), System.currentTimeMillis() + (minutes * 60L * 1000L));
    }

    public void disableZoneEffect(String zoneName) {
        zoneEffects.remove(zoneName.toLowerCase());
    }

    public boolean hasZoneEffect(String zoneName) {
        Long end = zoneEffects.get(zoneName.toLowerCase());
        if (end == null) return false;
        if (System.currentTimeMillis() > end) {
            zoneEffects.remove(zoneName.toLowerCase());
            return false;
        }
        return true;
    }

    public void checkPlayersInZones() {
        // Show zone effects
        for (Zone z : zones.values()) {
            if (z.isEnabled() && hasZoneEffect(z.getName())) {
                showZoneParticles(z);
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            Zone current = getZoneAt(p.getLocation());
            String curName = current != null ? current.getName() : null;
            String prevName = playerZones.get(p.getUniqueId());
            if (curName != null && !curName.equals(prevName)) {
                removeBossBar(p);
                sendEnter(p, current);
                playerZones.put(p.getUniqueId(), curName);
            } else if (curName == null && prevName != null) {
                Zone prev = getZone(prevName); if (prev != null) sendLeave(p, prev);
                removeBossBar(p); playerZones.remove(p.getUniqueId());
            }
            if (current != null) updateDisplay(p, current);
        }
    }

    private void showZoneParticles(Zone z) {
        org.bukkit.World w = Bukkit.getWorld(z.getWorldName());
        if (w == null) return;
        Color color = Color.fromRGB(0, 255, 255); // Cyan for zone highlight
        double minX = z.getMinX(), minY = z.getMinY(), minZ = z.getMinZ();
        double maxX = z.getMaxX() + 1, maxY = z.getMaxY() + 1, maxZ = z.getMaxZ() + 1;
        // Draw edges of zone
        for (double x = minX; x <= maxX; x += 0.5) {
            for (double y = minY; y <= maxY; y += 0.5) {
                for (double z1 = minZ; z1 <= maxZ; z1 += 0.5) {
                    boolean edge = (near(x,minX)||near(x,maxX))&&(near(y,minY)||near(y,maxY))
                        || (near(x,minX)||near(x,maxX))&&(near(z1,minZ)||near(z1,maxZ))
                        || (near(y,minY)||near(y,maxY))&&(near(z1,minZ)||near(z1,maxZ));
                    if (edge) {
                        Location loc = new Location(w, x, y, z1);
                        ParticleUtil.spawnDust(loc, color, 1.5f, 1);
                    }
                }
            }
        }
    }

    private boolean near(double a, double b) { return Math.abs(a-b) < 0.3; }

    private void sendEnter(Player p, Zone z) {
        String msg = z.getEnterMessage();
        if (msg == null || msg.isEmpty()) {
            List<String> messages = plugin.getConfigManager().getMessageList("zone.enter", "%zone%", z.getName());
            for (String line : messages) p.sendMessage(line);
        } else {
            p.sendMessage(HexUtil.color(msg.replace("%zone%", z.getName())));
        }
    }

    private void sendLeave(Player p, Zone z) {
        String msg = z.getLeaveMessage();
        if (msg == null || msg.isEmpty()) {
            List<String> messages = plugin.getConfigManager().getMessageList("zone.leave", "%zone%", z.getName());
            for (String line : messages) p.sendMessage(line);
        } else {
            p.sendMessage(HexUtil.color(msg.replace("%zone%", z.getName())));
        }
    }

    private void updateDisplay(Player p, Zone z) {
        String message = plugin.getConfigManager().getMessage("zone.display.in-zone").replace("%zone%", z.getName());
        message = HexUtil.color(message);

        String displayType = plugin.getConfigManager().getDisplayType("display.zone");

        if (displayType.equalsIgnoreCase("bossbar")) {
            BossBar bar = playerBossBars.get(p.getUniqueId());
            if (bar == null) {
                bar = Bukkit.createBossBar(message, getBarColor(z.getBarColor()), getBarStyle(z.getBarStyle()));
                bar.addPlayer(p); playerBossBars.put(p.getUniqueId(), bar);
            } else {
                bar.setTitle(message);
            }
        } else if (displayType.equalsIgnoreCase("actionbar")) {
            sendActionBar(p, message);
        } else {
            // message type - don't spam chat, only send once
        }
    }

    private void sendActionBar(Player p, String msg) {
        try {
            p.sendActionBar(net.kyori.adventure.text.Component.text(msg));
        } catch (Exception e1) {
            try {
                p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(msg));
            } catch (Exception e2) {
                // Fallback
            }
        }
    }

    private void removeBossBar(Player p) { BossBar bar = playerBossBars.remove(p.getUniqueId()); if (bar != null) bar.removePlayer(p); }
    private BarColor getBarColor(String c) { try { return BarColor.valueOf(c.toUpperCase()); } catch (Exception e) { return BarColor.WHITE; } }
    private BarStyle getBarStyle(String s) { try { return BarStyle.valueOf(s.toUpperCase()); } catch (Exception e) { return BarStyle.SOLID; } }
    public Map<UUID, String> getPlayerZones() { return playerZones; }
    public Map<String, Long> getZoneEffects() { return zoneEffects; }
    public void removePlayer(Player p) { removeBossBar(p); playerZones.remove(p.getUniqueId()); }
}
