package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Utils.ParticleUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class EffectManager {
    private final LunaMilitaryComplex plugin;

    public EffectManager(LunaMilitaryComplex plugin) { this.plugin = plugin; }
    public void tick() {}

    public void playBossStageEffect(Location loc, int stage) {
        Color color = stage == 2 ? Color.ORANGE : Color.RED;
        ParticleUtil.sphere(loc, 3, color, 50);
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
    }

    public void playBossDeathEffect(Location loc) {
        World w = loc.getWorld();
        w.strikeLightningEffect(loc);
        ParticleUtil.sphere(loc, 5, Color.PURPLE, 100);
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
    }

    public void playSirenEffect(Location loc) {
        World w = loc.getWorld();
        w.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 2.0f, 0.5f);
        ParticleUtil.spawnDust(loc.clone().add(0, 5, 0), Color.RED, 2.0f, 5);
    }

    public void playExplosionEffect(Location loc) {
        World w = loc.getWorld();
        w.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0, 0, 0, 0);
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
    }

    public void playCaptureEffect(Location loc) {
        ParticleUtil.drawCircle(loc, 5, Color.GREEN, 30);
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }

    public void playCountdownEffect(Location loc, int seconds) {
        if (seconds <= 10) {
            loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f + (10 - seconds) * 0.1f);
        }
    }

    public void spawnWaveEffect(Location loc) {
        ParticleUtil.sphere(loc, 2, Color.YELLOW, 30);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
    }

    public void playStrikeWarning(Location loc) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }
        ParticleUtil.sphere(loc, 10, Color.RED, 50);
    }

    public void playStrikeExplosion(Location loc) {
        World w = loc.getWorld();
        w.createExplosion(loc, 4.0f, true, true);
        ParticleUtil.sphere(loc, 8, Color.ORANGE, 100);
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.3f);
    }
}
