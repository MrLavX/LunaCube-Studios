package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.BossData;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class BossManager {
    private final LunaMilitaryComplex plugin;
    private BossData bossData;
    private Mob boss;
    private BossBar healthBar;
    private int currentStage = 1;
    private boolean active = false;

    public BossManager(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        bossData = new BossData(plugin.getConfig().getConfigurationSection("boss"));
    }

    public void spawnBoss(Location location) {
        if (active) return;
        if (location == null || location.getWorld() == null) return;
        EntityType et;
        try {
            et = EntityType.valueOf(bossData.entityType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            et = EntityType.ZOMBIE;
        }
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, et);
        if (entity instanceof Mob) {
            boss = (Mob) entity;
            boss.setCustomName(HexUtil.color(bossData.name));
            boss.setCustomNameVisible(true);

            AttributeInstance maxHealth = boss.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(bossData.health);
                boss.setHealth(bossData.health);
            }
            AttributeInstance attackDamage = boss.getAttribute(Attribute.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.setBaseValue(bossData.damage);
            }
            AttributeInstance armor = boss.getAttribute(Attribute.ARMOR);
            if (armor != null) {
                armor.setBaseValue(bossData.armor);
            }

            boss.setMetadata("lmc_boss", new FixedMetadataValue(plugin, true));
            active = true;
            currentStage = 1;
            healthBar = Bukkit.createBossBar(formatBossBarTitle(boss.getHealth(), bossData.health), parseColor(), parseStyle());
            for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) healthBar.addPlayer(p);
            startBossTick();
            plugin.getLogManager().log("BOSS", "Boss spawned at " + location);
        }
    }

    private void startBossTick() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || boss == null || boss.isDead()) {
                    if (healthBar != null) { healthBar.removeAll(); healthBar = null; }
                    active = false;
                    if (boss != null && boss.isDead()) onBossDeath();
                    cancel();
                    return;
                }
                double health = boss.getHealth();
                AttributeInstance maxHealthAttr = boss.getAttribute(Attribute.MAX_HEALTH);
                double max = maxHealthAttr != null ? maxHealthAttr.getValue() : health;
                healthBar.setProgress(Math.max(0, Math.min(1, health / max)));
                healthBar.setTitle(formatBossBarTitle(health, max));
                checkStageTransition(health, max);
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void checkStageTransition(double health, double max) {
        double percent = health / max;
        int newStage = 1;
        if (percent <= 0.33) newStage = 3;
        else if (percent <= 0.66) newStage = 2;
        if (newStage != currentStage) {
            currentStage = newStage;
            plugin.getLogManager().log("BOSS", "Stage changed to " + currentStage);
            plugin.getEffectManager().playBossStageEffect(boss.getLocation(), currentStage);
        }
    }

    private void onBossDeath() {
        Location deathLocation = boss != null ? boss.getLocation() : null;
        plugin.getLogManager().log("BOSS", "Boss killed");
        if (deathLocation != null) {
            plugin.getEffectManager().playBossDeathEffect(deathLocation);
        }
        plugin.getStateManager().setState(akm.mrlavx.lunaMilitaryComplex.Models.MilitaryEvent.CAPTURE);
        plugin.getEventManager().startCapture();
        if (healthBar != null) { healthBar.removeAll(); healthBar = null; }
        boss = null;
        active = false;
    }

    public void despawnBoss() {
        if (boss != null && !boss.isDead()) boss.remove();
        if (healthBar != null) { healthBar.removeAll(); healthBar = null; }
        active = false;
        boss = null;
    }

    public boolean isActive() { return active; }
    public Mob getBoss() { return boss; }
    public int getCurrentStage() { return currentStage; }

    private String formatBossBarTitle(double health, double maxHealth) {
        String title = bossData.bossbarTitle
            .replace("%name%", bossData.name)
            .replace("%health%", String.valueOf((int) health))
            .replace("%max-health%", String.valueOf((int) maxHealth));
        return HexUtil.color(title);
    }

    private BarColor parseColor() {
        try {
            return BarColor.valueOf(bossData.bossbarColor.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarColor.RED;
        }
    }

    private BarStyle parseStyle() {
        try {
            return BarStyle.valueOf(bossData.bossbarStyle.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarStyle.SOLID;
        }
    }
}
