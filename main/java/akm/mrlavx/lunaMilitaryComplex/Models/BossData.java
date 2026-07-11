package akm.mrlavx.lunaMilitaryComplex.Models;

import org.bukkit.configuration.ConfigurationSection;

public class BossData {
    public String name, entityType;
    public double health, armor, damage;
    public int stages;
    public String spawnEffect, deathEffect;
    public String bossbarTitle, bossbarColor, bossbarStyle;

    public BossData(ConfigurationSection s) {
        name = s.getString("name", "Командир комплекса");
        entityType = s.getString("entity", "ZOMBIE");
        health = s.getDouble("health", 500);
        armor = s.getDouble("armor", 20);
        damage = s.getDouble("damage", 15);
        stages = s.getInt("stages", 3);
        spawnEffect = s.getString("spawn-effect", "EXPLOSION");
        deathEffect = s.getString("death-effect", "LIGHTNING");
        bossbarTitle = s.getString("bossbar.title", "&4&l%name% &7| &c%health%/%max-health% ❤");
        bossbarColor = s.getString("bossbar.color", "RED");
        bossbarStyle = s.getString("bossbar.style", "SOLID");
    }
}
