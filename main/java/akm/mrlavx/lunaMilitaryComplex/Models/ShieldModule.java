package akm.mrlavx.lunaMilitaryComplex.Models;

import org.bukkit.Location;
import java.util.UUID;

public class ShieldModule {
    private UUID owner;
    private Location location;
    private double energy;
    private double maxEnergy;
    private int radius;
    private boolean active;
    private String id;

    public ShieldModule(String id, UUID owner, Location location, double maxEnergy, int radius) {
        this.id = id; this.owner = owner; this.location = location;
        this.maxEnergy = maxEnergy; this.energy = maxEnergy; this.radius = radius; this.active = true;
    }
    public UUID getOwner(){return owner;} public void setOwner(UUID v){owner=v;}
    public Location getLocation(){return location;} public void setLocation(Location v){location=v;}
    public double getEnergy(){return energy;} public void setEnergy(double v){energy=Math.max(0, Math.min(v,maxEnergy));}
    public double getMaxEnergy(){return maxEnergy;} public void setMaxEnergy(double v){maxEnergy=v;}
    public int getRadius(){return radius;} public void setRadius(int v){radius=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public String getId(){return id;}
    public double getEnergyPercent(){return maxEnergy <= 0 ? 0 : (energy/maxEnergy)*100;}
}
