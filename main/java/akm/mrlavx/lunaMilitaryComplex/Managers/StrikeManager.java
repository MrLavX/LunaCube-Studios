package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.StrikeData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StrikeManager {
    private final LunaMilitaryComplex plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, StrikeData> activeStrikes = new HashMap<>();
    private int maxStrikes = 3;
    private int cooldownMinutes = 60;

    public StrikeManager(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        maxStrikes = plugin.getConfigManager().getStrike().getInt("settings.max-strikes", 3);
        cooldownMinutes = plugin.getConfigManager().getStrike().getInt("settings.cooldown-minutes", 60);
    }

    public boolean canLaunch(Player player) {
        UUID uuid = player.getUniqueId();
        if (!player.hasPermission("lunamilitarycomplex.strike")) return false;
        if (plugin.getDataManager().getStrikesLaunched() >= maxStrikes) {
            player.sendMessage("Достигнут лимит ударов."); return false;
        }
        Long cd = cooldowns.get(uuid);
        if (cd != null && System.currentTimeMillis() < cd) {
            long remaining = (cd - System.currentTimeMillis()) / 1000;
            player.sendMessage("Кулдаун: " + remaining + " сек."); return false;
        }
        return true;
    }

    public void launchStrike(Player player, Location target) {
        if (!canLaunch(player)) return;
        UUID uuid = player.getUniqueId();
        StrikeData data = new StrikeData(uuid, target);
        activeStrikes.put(uuid, data);
        plugin.getDataManager().setStrikesLaunched(plugin.getDataManager().getStrikesLaunched() + 1);
        cooldowns.put(uuid, System.currentTimeMillis() + (cooldownMinutes * 60L * 1000L));
        plugin.getLogManager().log("STRIKE", player.getName() + " launched strike at " + target);
        startStrikeSequence(player, target);
    }

    private void startStrikeSequence(Player player, Location target) {
        int warningTime = plugin.getConfigManager().getStrike().getInt("settings.warning-seconds", 10);
        broadcast("strike.warning", "%target%", target.getBlockX() + ", " + target.getBlockY() + ", " + target.getBlockZ());
        plugin.getEffectManager().playStrikeWarning(target);
        new BukkitRunnable() {
            int countdown = warningTime;
            @Override
            public void run() {
                if (countdown <= 0) {
                    executeStrike(target);
                    cancel();
                    return;
                }
                broadcast("strike.countdown", "%seconds%", String.valueOf(countdown));
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void executeStrike(Location target) {
        for (int wave = 1; wave <= 3; wave++) {
            final int w = wave;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int i = 0; i < 5; i++) {
                    Location loc = target.clone().add((Math.random()-0.5)*20, 0, (Math.random()-0.5)*20);
                    loc.setY(target.getWorld().getHighestBlockYAt(loc));
                    plugin.getEffectManager().playStrikeExplosion(loc);
                }
                broadcast("strike.wave", "%wave%", String.valueOf(w));
            }, wave * 40L);
        }
        broadcast("strike.complete");
    }

    private void broadcast(String path, String... placeholders) {
        for (String msg : plugin.getConfigManager().getMessageList(path, placeholders)) {
            Bukkit.broadcastMessage(msg);
        }
    }

    public Map<UUID, Long> getCooldowns() { return cooldowns; }
    public Map<UUID, StrikeData> getActiveStrikes() { return activeStrikes; }
}
