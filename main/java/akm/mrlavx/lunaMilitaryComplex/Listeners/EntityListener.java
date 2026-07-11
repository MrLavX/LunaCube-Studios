package akm.mrlavx.lunaMilitaryComplex.Listeners;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityListener implements Listener {
    private final LunaMilitaryComplex plugin;
    public EntityListener(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Mob) {
            Mob mob = (Mob) e.getEntity();
            if (mob.hasMetadata("lmc_boss")) {
                plugin.getLogManager().log("BOSS", "Boss died");
            } else if (mob.hasMetadata("lmc_mob")) {
                String mobType = "unknown";
                try {
                    mobType = mob.getMetadata("lmc_mob").get(0).asString();
                } catch (Exception ex) {}
                plugin.getLogManager().log("MOB", "Mob killed: " + mobType);
            }
        }
    }
}
