package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.StrikeData;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
        maxStrikes = plugin.getConfig().getInt("strike.settings.max-strikes", 3);
        cooldownMinutes = plugin.getConfig().getInt("strike.settings.cooldown-minutes", 60);
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
        if (plugin.getConfig().getBoolean("strike.settings.blocked-by-shield", true) && plugin.getShieldModuleManager().tryBlockStrike(target)) {
            player.sendMessage(HexUtil.color("&cТочка защищена защитным модулем."));
            return;
        }
        UUID uuid = player.getUniqueId();
        StrikeData data = new StrikeData(uuid, target);
        activeStrikes.put(uuid, data);
        plugin.getDataManager().setStrikesLaunched(plugin.getDataManager().getStrikesLaunched() + 1);
        cooldowns.put(uuid, System.currentTimeMillis() + (cooldownMinutes * 60L * 1000L));
        plugin.getLogManager().log("STRIKE", player.getName() + " launched strike at " + target);
        startStrikeSequence(player, target);
    }

    public void requestStrike(Player player, Location target) {
        if (!canLaunch(player)) return;
        String targetName = target.getBlockX() + ", " + target.getBlockY() + ", " + target.getBlockZ() + " / " + plugin.getConfigManager().getDisplayWorld(target.getWorld().getName());
        plugin.getConfirmGUI().open(player, targetName, () -> {
            if (!consumeStrikeCode(player)) {
                for (String line : plugin.getConfigManager().getMessageList("strike.no-code")) {
                    player.sendMessage(line);
                }
                return;
            }
            launchStrike(player, target);
        });
    }

    private boolean consumeStrikeCode(Player player) {
        org.bukkit.inventory.ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            org.bukkit.inventory.ItemStack item = contents[i];
            if (plugin.getItemManager().isCustomItem(item, "strike_code")) {
                if (item.getAmount() <= 1) {
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
                return true;
            }
        }
        return false;
    }

    private void startStrikeSequence(Player player, Location target) {
        int warningTime = plugin.getConfig().getInt("strike.settings.warning-seconds", 10);
        broadcast("strike.warning", "%target%", target.getBlockX() + ", " + target.getBlockY() + ", " + target.getBlockZ());
        String displayType = plugin.getConfigManager().getDisplayType("display.strike");
        if (displayType.equalsIgnoreCase("title") || displayType.equalsIgnoreCase("subtitle")) {
            String title = plugin.getConfig().getString("display.strike.title.text", "&4&lСТРАТЕГИЧЕСКИЙ УДАР");
            String subtitle = plugin.getConfig().getString("display.strike.title.subtitle", "&7Цель: &f%target%");
            int fadeIn = plugin.getConfig().getInt("display.strike.title.fade-in", 5);
            int stay = plugin.getConfig().getInt("display.strike.title.stay", 40);
            int fadeOut = plugin.getConfig().getInt("display.strike.title.fade-out", 10);
            player.sendTitle(
                HexUtil.color(title.replace("%target%", target.getBlockX() + ", " + target.getBlockY() + ", " + target.getBlockZ())),
                HexUtil.color(subtitle.replace("%target%", target.getBlockX() + ", " + target.getBlockY() + ", " + target.getBlockZ())),
                fadeIn, stay, fadeOut
            );
        }
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
                broadcast("strike.countdown", "%time%", akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil.formatDuration(countdown));
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void executeStrike(Location target) {
        int waves = plugin.getConfig().getInt("strike.settings.waves", 3);
        int explosionsPerWave = plugin.getConfig().getInt("strike.settings.explosions-per-wave", 5);
        double power = plugin.getConfig().getDouble("strike.settings.explosion-power", 4.0);
        boolean damagePlayers = plugin.getConfig().getBoolean("strike.settings.damage-players", true);
        boolean damageBlocks = plugin.getConfig().getBoolean("strike.settings.damage-blocks", true);
        for (int wave = 1; wave <= waves; wave++) {
            final int w = wave;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int i = 0; i < explosionsPerWave; i++) {
                    Location loc = target.clone().add((Math.random()-0.5)*20, 0, (Math.random()-0.5)*20);
                    loc.setY(target.getWorld().getHighestBlockYAt(loc));
                    if (!plugin.getShieldModuleManager().tryBlockStrike(loc)) {
                        loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), (float) power, damageBlocks, damagePlayers);
                        plugin.getEffectManager().playStrikeExplosion(loc);
                    }
                }
                broadcast("strike.wave", "%wave%", String.valueOf(w));
            }, wave * 40L);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> broadcast("strike.complete"), waves * 40L + 1L);
    }

    private void broadcast(String path, String... placeholders) {
        for (String msg : plugin.getConfigManager().getMessageList(path, placeholders)) {
            Bukkit.broadcastMessage(msg);
        }
    }

    public Map<UUID, Long> getCooldowns() { return cooldowns; }
    public Map<UUID, StrikeData> getActiveStrikes() { return activeStrikes; }
}
