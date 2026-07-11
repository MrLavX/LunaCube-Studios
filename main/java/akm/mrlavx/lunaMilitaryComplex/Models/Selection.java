package akm.mrlavx.lunaMilitaryComplex.Models;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Selection {
    private final Player player;
    private Location pos1, pos2;
    private boolean effectsEnabled = true;

    public Selection(Player player) { this.player = player; }
    public Player getPlayer() { return player; }
    public Location getPos1() { return pos1; }
    public void setPos1(Location pos1) { this.pos1 = pos1; }
    public Location getPos2() { return pos2; }
    public void setPos2(Location pos2) { this.pos2 = pos2; }
    public boolean hasBothPositions() { 
        return pos1 != null && pos2 != null && pos1.getWorld() != null && pos2.getWorld() != null && pos1.getWorld().equals(pos2.getWorld()); 
    }
    public boolean isEffectsEnabled() { return effectsEnabled; }
    public void setEffectsEnabled(boolean v) { this.effectsEnabled = v; }
    public int getSizeX() { return hasBothPositions() ? Math.abs(pos1.getBlockX()-pos2.getBlockX())+1 : 0; }
    public int getSizeY() { return hasBothPositions() ? Math.abs(pos1.getBlockY()-pos2.getBlockY())+1 : 0; }
    public int getSizeZ() { return hasBothPositions() ? Math.abs(pos1.getBlockZ()-pos2.getBlockZ())+1 : 0; }
    public int getVolume() { return getSizeX()*getSizeY()*getSizeZ(); }
    public World getWorld() { return pos1 != null ? pos1.getWorld() : (pos2 != null ? pos2.getWorld() : null); }
    public int getMinX() { return Math.min(pos1.getBlockX(), pos2.getBlockX()); }
    public int getMinY() { return Math.min(pos1.getBlockY(), pos2.getBlockY()); }
    public int getMinZ() { return Math.min(pos1.getBlockZ(), pos2.getBlockZ()); }
    public int getMaxX() { return Math.max(pos1.getBlockX(), pos2.getBlockX()); }
    public int getMaxY() { return Math.max(pos1.getBlockY(), pos2.getBlockY()); }
    public int getMaxZ() { return Math.max(pos1.getBlockZ(), pos2.getBlockZ()); }
}
