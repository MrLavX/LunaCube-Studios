package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.Selection;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {
    private final LunaMilitaryComplex plugin;
    private final Map<UUID, Selection> selections = new HashMap<>();

    public SelectionManager(LunaMilitaryComplex plugin) { this.plugin = plugin; }
    public void reload() { selections.clear(); }
    public Selection getSelection(Player p) { return selections.computeIfAbsent(p.getUniqueId(), k -> new Selection(p)); }
    public void removeSelection(Player p) { selections.remove(p.getUniqueId()); }

    public void setPos1(Player p, Location loc) {
        Selection s = getSelection(p); s.setPos1(loc);
        sendMsg(p, s, loc, 1);
    }
    public void setPos2(Player p, Location loc) {
        Selection s = getSelection(p); s.setPos2(loc);
        sendMsg(p, s, loc, 2);
    }

    private void sendMsg(Player p, Selection s, Location loc, int point) {
        String path = point == 1 ? "zone.selection.pos1-set" : "zone.selection.pos2-set";
        if (s.hasBothPositions()) {
            for (String msg : plugin.getConfigManager().getMessageList(path,
                "%x%", String.valueOf(loc.getBlockX()), "%y%", String.valueOf(loc.getBlockY()), "%z%", String.valueOf(loc.getBlockZ()),
                "%sizex%", String.valueOf(s.getSizeX()), "%sizey%", String.valueOf(s.getSizeY()), "%sizez%", String.valueOf(s.getSizeZ()),
                "%volume%", String.valueOf(s.getVolume()))) {
                p.sendMessage(msg);
            }
        } else {
            for (String msg : plugin.getConfigManager().getMessageList(path,
                "%x%", String.valueOf(loc.getBlockX()), "%y%", String.valueOf(loc.getBlockY()), "%z%", String.valueOf(loc.getBlockZ()))) {
                p.sendMessage(msg);
            }
        }
    }

    public void toggleEffects(Player p) {
        Selection s = getSelection(p); s.setEffectsEnabled(!s.isEffectsEnabled());
        String status = s.isEffectsEnabled() ? "&aвключены" : "&cвыключены";
        for (String msg : plugin.getConfigManager().getMessageList("zone.selection.effects-toggled", "%status%", status)) {
            p.sendMessage(msg);
        }
    }

    public void showSelectionEffects() {
        for (Selection s : selections.values()) {
            if (!s.isEffectsEnabled() || !s.hasBothPositions()) continue;
            Player p = s.getPlayer(); if (p == null || !p.isOnline()) continue;
            Color color = Color.fromRGB(255, 0, 255);
            Particle.DustOptions opt = new Particle.DustOptions(color, 1.0f);
            double minX = s.getMinX(), minY = s.getMinY(), minZ = s.getMinZ();
            double maxX = s.getMaxX() + 1, maxY = s.getMaxY() + 1, maxZ = s.getMaxZ() + 1;
            for (double x = minX; x <= maxX; x += 0.5) {
                for (double y = minY; y <= maxY; y += 0.5) {
                    for (double z = minZ; z <= maxZ; z += 0.5) {
                        boolean edge = (near(x,minX)||near(x,maxX))&&(near(y,minY)||near(y,maxY))
                            || (near(x,minX)||near(x,maxX))&&(near(z,minZ)||near(z,maxZ))
                            || (near(y,minY)||near(y,maxY))&&(near(z,minZ)||near(z,maxZ));
                        if (edge) {
                            Location loc = new Location(s.getWorld(), x, y, z);
                            if (loc.distance(p.getLocation()) < 50) {
                                p.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, opt);
                            }
                        }
                    }
                }
            }
        }
    }
    private boolean near(double a, double b) { return Math.abs(a-b) < 0.3; }
    public Map<UUID, Selection> getSelections() { return selections; }
}
