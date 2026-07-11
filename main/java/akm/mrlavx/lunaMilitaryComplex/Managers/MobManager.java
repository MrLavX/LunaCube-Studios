package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

public class MobManager {
    private final LunaMilitaryComplex plugin;
    private final Map<String, ConfigurationSection> mobConfigs = new HashMap<>();
    private final Map<Integer, Mob> activeMobs = new HashMap<>();
    private int mobId = 0;

    public MobManager(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        mobConfigs.clear();
        ConfigurationSection section = plugin.getConfigManager().getMobs().getConfigurationSection("mobs");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                mobConfigs.put(key, section.getConfigurationSection(key));
            }
        }
    }

    public Mob spawnMob(String type, Location location) {
        ConfigurationSection config = mobConfigs.get(type.toLowerCase());
        if (config == null) return null;
        String entityType = config.getString("entity", "ZOMBIE");
        EntityType et;
        try {
            et = EntityType.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            et = EntityType.ZOMBIE;
        }
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, et);
        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;
            double health = config.getDouble("health", 20);
            double damage = config.getDouble("damage", 5);
            double armor = config.getDouble("armor", 0);
            String name = config.getString("name", type);
            mob.setCustomName(HexUtil.color(name));
            mob.setCustomNameVisible(true);

            AttributeInstance maxHealth = mob.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(health);
                mob.setHealth(health);
            }
            AttributeInstance attackDamage = mob.getAttribute(Attribute.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.setBaseValue(damage);
            }
            AttributeInstance armorAttr = mob.getAttribute(Attribute.ARMOR);
            if (armorAttr != null) {
                armorAttr.setBaseValue(armor);
            }

            // Equipment
            ConfigurationSection equip = config.getConfigurationSection("equipment");
            if (equip != null) {
                EntityEquipment equipment = mob.getEquipment();
                if (equipment != null) {
                    String helmet = equip.getString("helmet");
                    if (helmet != null) {
                        Material mat = Material.getMaterial(helmet.toUpperCase());
                        if (mat != null) equipment.setHelmet(new ItemStack(mat));
                    }
                    String chestplate = equip.getString("chestplate");
                    if (chestplate != null) {
                        Material mat = Material.getMaterial(chestplate.toUpperCase());
                        if (mat != null) equipment.setChestplate(new ItemStack(mat));
                    }
                    String weapon = equip.getString("weapon");
                    if (weapon != null) {
                        Material mat = Material.getMaterial(weapon.toUpperCase());
                        if (mat != null) equipment.setItemInMainHand(new ItemStack(mat));
                    }
                }
            }

            mob.setMetadata("lmc_mob", new FixedMetadataValue(plugin, type));
            mob.setMetadata("lmc_mob_id", new FixedMetadataValue(plugin, mobId));
            activeMobs.put(mobId, mob);
            mobId++;
            return mob;
        }
        return null;
    }

    public void clearMobs() {
        for (Mob mob : activeMobs.values()) {
            if (mob != null && !mob.isDead()) mob.remove();
        }
        activeMobs.clear();
    }

    public boolean hasAliveMobs() {
        for (Mob mob : activeMobs.values()) if (mob != null && !mob.isDead()) return true;
        return false;
    }

    public int getAliveCount() {
        int count = 0;
        for (Mob mob : activeMobs.values()) if (mob != null && !mob.isDead()) count++;
        return count;
    }

    public Map<Integer, Mob> getActiveMobs() { return activeMobs; }
}
