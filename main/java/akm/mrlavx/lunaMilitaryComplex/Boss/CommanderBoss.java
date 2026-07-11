package akm.mrlavx.lunaMilitaryComplex.Boss;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import org.bukkit.Location;
import org.bukkit.entity.Mob;

public class CommanderBoss {
    private final LunaMilitaryComplex plugin;
    private Mob entity;

    public CommanderBoss(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
    }

    public void spawn(Location location) {
        // Boss spawn logic handled in BossManager
    }

    public void onStageChange(int stage) {
        // Stage-specific abilities
    }

    public void onDeath() {
        // Death rewards and effects
    }
}
