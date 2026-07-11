package akm.mrlavx.lunaMilitaryComplex.Models;

import org.bukkit.Location;
import java.util.UUID;

public class StrikeData {
    private UUID launcher;
    private Location target;
    private long launchTime;
    private int wave;
    private boolean active;

    public StrikeData(UUID launcher, Location target) {
        this.launcher = launcher; this.target = target;
        this.launchTime = System.currentTimeMillis(); this.wave = 0; this.active = true;
    }
    public UUID getLauncher(){return launcher;} public Location getTarget(){return target;}
    public long getLaunchTime(){return launchTime;} public int getWave(){return wave;} public void setWave(int v){wave=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
