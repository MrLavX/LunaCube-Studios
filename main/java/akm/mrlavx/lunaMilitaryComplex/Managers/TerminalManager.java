package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.Zone;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TerminalManager {
    private final LunaMilitaryComplex plugin;
    private boolean terminalOpen = false;

    public TerminalManager(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    public void openTerminal(Player player) {
        if (!terminalOpen) {
            player.sendMessage(HexUtil.color("&cТерминал закрыт."));
            return;
        }
        player.sendMessage(HexUtil.color("&a&lТерминал открыт!"));
        player.sendMessage(HexUtil.color("&7Используйте: &f/lmc strike <x> <y> <z> <world>"));
        // TODO: Open GUI when implemented fully
    }

    public void setTerminalOpen(boolean open) { this.terminalOpen = open; }
    public boolean isTerminalOpen() { return terminalOpen; }

    public void placeTerminalBlock(Zone zone) {
        if (zone == null) return;
        org.bukkit.World w = org.bukkit.Bukkit.getWorld(zone.getWorldName());
        if (w == null) return;
        int x = (zone.getMinX() + zone.getMaxX()) / 2;
        int z = (zone.getMinZ() + zone.getMaxZ()) / 2;
        int y = w.getHighestBlockYAt(x, z);
        Location loc = new Location(w, x, y, z);
        zone.setTerminalBlock(loc);
        plugin.getZoneManager().saveZones();
    }

    public boolean isTerminalBlock(Location loc) {
        if (loc == null) return false;
        for (Zone z : plugin.getZoneManager().getAllZones()) {
            Location tb = z.getTerminalBlock();
            if (tb != null && tb.getBlockX() == loc.getBlockX() && tb.getBlockY() == loc.getBlockY() && tb.getBlockZ() == loc.getBlockZ())
                return true;
        }
        return false;
    }
}
