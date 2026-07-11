package akm.mrlavx.lunaMilitaryComplex.Utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class ParticleUtil {
    public static void spawnDust(Location loc, Color color, float size, int count) {
        if (loc == null || loc.getWorld() == null) return;
        Particle.DustOptions opt = new Particle.DustOptions(color, size);
        loc.getWorld().spawnParticle(Particle.DUST, loc, count, 0, 0, 0, 0, opt);
    }

    public static void drawCircle(Location center, double radius, Color color, int points) {
        if (center == null || center.getWorld() == null) return;
        World w = center.getWorld();
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location p = new Location(w, x, center.getY(), z);
            spawnDust(p, color, 1.0f, 1);
        }
    }

    public static void sphere(Location center, double radius, Color color, int density) {
        if (center == null || center.getWorld() == null) return;
        World w = center.getWorld();
        for (int i = 0; i < density; i++) {
            double theta = Math.random() * Math.PI * 2;
            double phi = Math.acos(2 * Math.random() - 1);
            double x = center.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double y = center.getY() + radius * Math.sin(phi) * Math.sin(theta);
            double z = center.getZ() + radius * Math.cos(phi);
            spawnDust(new Location(w, x, y, z), color, 1.0f, 1);
        }
    }
}
