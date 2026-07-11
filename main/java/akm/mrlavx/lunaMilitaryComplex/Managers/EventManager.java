package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.MilitaryEvent;
import akm.mrlavx.lunaMilitaryComplex.Models.Zone;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventManager {
    private final LunaMilitaryComplex plugin;
    private BossBar eventBar;
    private int timer = 0;
    private int wave = 0;
    private boolean running = false;
    private long nextOpenTime = 0;
    private int prepSeconds = 0;
    private MilitaryEvent currentPhase = MilitaryEvent.CLOSED;

    public EventManager(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
        nextOpenTime = plugin.getDataManager().getNextOpenTime();
        if (nextOpenTime == 0) scheduleNextOpen();
    }

    public void reload() {
        nextOpenTime = plugin.getDataManager().getNextOpenTime();
    }

    public void tick() {
        if (!running) {
            checkSchedule();
            return;
        }
        timer--;
        switch (currentPhase) {
            case PREPARING: tickPreparing(); break;
            case OPEN: tickOpen(); break;
            case ASSAULT: tickAssault(); break;
            case BOSS: tickBoss(); break;
            case CAPTURE: tickCapture(); break;
            case REWARDS: tickRewards(); break;
            case FINISHED: tickFinished(); break;
            default: break;
        }
        updateBossBar();
    }

    private void checkSchedule() {
        if (System.currentTimeMillis() >= nextOpenTime && nextOpenTime > 0) {
            startEvent();
        }
    }

    public void startEvent() {
        if (running) return;
        running = true;
        currentPhase = MilitaryEvent.PREPARING;
        prepSeconds = plugin.getConfig().getInt("event.prep-time-seconds", 1800);
        timer = prepSeconds;
        plugin.getStateManager().setState(MilitaryEvent.PREPARING);
        broadcast("event.preparing.start");
        plugin.getLogManager().log("EVENT", "Event started - preparing phase");
        createEventBar();
    }

    public void stopEvent() {
        running = false;
        currentPhase = MilitaryEvent.CLOSED;
        plugin.getStateManager().setState(MilitaryEvent.CLOSED);
        plugin.getMobManager().clearMobs();
        plugin.getBossManager().despawnBoss();
        plugin.getCaptureManager().reset();
        if (eventBar != null) { eventBar.removeAll(); eventBar = null; }
        broadcast("event.stopped");
        plugin.getLogManager().log("EVENT", "Event stopped");
        scheduleNextOpen();
    }

    private void tickPreparing() {
        int remaining = timer;
        if (remaining == 1800) broadcast("event.preparing.30min");
        else if (remaining == 600) broadcast("event.preparing.10min");
        else if (remaining == 300) broadcast("event.preparing.5min");
        else if (remaining == 60) broadcast("event.preparing.1min");
        else if (remaining == 30) broadcast("event.preparing.30sec");
        else if (remaining <= 10 && remaining > 0) {
            broadcast("event.preparing.countdown", "%seconds%", String.valueOf(remaining));
            Zone zone = getMainZone();
            if (zone != null) {
                Location center = getZoneCenter(zone);
                plugin.getEffectManager().playCountdownEffect(center, remaining);
            }
        }
        else if (remaining <= 0) {
            currentPhase = MilitaryEvent.OPEN;
            timer = plugin.getConfig().getInt("event.open-duration", 300);
            plugin.getStateManager().setState(MilitaryEvent.OPEN);
            broadcast("event.opened");
            plugin.getLogManager().log("EVENT", "Complex opened");
        }
    }

    private void tickOpen() {
        if (timer <= 0) {
            currentPhase = MilitaryEvent.ASSAULT;
            startAssault();
        }
    }

    private void tickAssault() {
        if (!plugin.getMobManager().hasAliveMobs()) {
            wave++;
            int maxWaves = plugin.getConfig().getInt("event.waves", 5);
            if (wave > maxWaves) {
                currentPhase = MilitaryEvent.BOSS;
                broadcast("event.boss.start");
                spawnBoss();
            } else {
                broadcast("event.wave.complete", "%wave%", String.valueOf(wave));
                timer = plugin.getConfig().getInt("event.wave-delay-seconds", 10);
                Bukkit.getScheduler().runTaskLater(plugin, this::spawnWave, timer * 20L);
            }
        }
    }

    private void tickBoss() {
        if (!plugin.getBossManager().isActive()) {
            currentPhase = MilitaryEvent.CAPTURE;
            startCapture();
        }
    }

    private void tickCapture() {}

    private void tickRewards() {
        if (timer <= 0) {
            currentPhase = MilitaryEvent.FINISHED;
            timer = 60;
        }
    }

    private void tickFinished() {
        if (timer <= 0) {
            stopEvent();
        }
    }

    public void startCapture() {
        currentPhase = MilitaryEvent.CAPTURE;
        plugin.getStateManager().setState(MilitaryEvent.CAPTURE);
        Location center = getZoneCenter(getMainZone());
        if (center != null) {
            plugin.getCaptureManager().startCapture(center);
        }
        broadcast("event.capture.start");
        plugin.getLogManager().log("EVENT", "Capture phase started");
    }

    public void onCaptureComplete(Player winner) {
        currentPhase = MilitaryEvent.REWARDS;
        timer = plugin.getConfig().getInt("event.rewards-duration", 300);
        plugin.getStateManager().setState(MilitaryEvent.REWARDS);
        broadcast("event.capture.complete", "%player%", winner.getName());
        plugin.getLogManager().log("CAPTURE", "Captured by " + winner.getName());
        giveRewards(winner);
    }

    private void giveRewards(Player winner) {
        winner.getInventory().addItem(plugin.getItemManager().getMilitaryComponent());
        winner.getInventory().addItem(plugin.getItemManager().getStrikeCode());
        broadcast("event.rewards.given", "%player%", winner.getName());
    }

    private void spawnWave() {
        Zone zone = getMainZone();
        if (zone == null) return;
        List<String> mobTypes = new ArrayList<>();
        List<String> configMobs = plugin.getConfigManager().getMobs().getStringList("wave-" + wave + ".mobs");
        if (configMobs.isEmpty()) {
            mobTypes = new ArrayList<>(Arrays.asList("soldier", "soldier", "heavy"));
        } else {
            mobTypes = new ArrayList<>(configMobs);
        }
        Location center = getZoneCenter(zone);
        for (String type : mobTypes) {
            Location loc = center.clone().add((Math.random()-0.5)*20, 0, (Math.random()-0.5)*20);
            loc.setY(center.getWorld().getHighestBlockYAt(loc));
            plugin.getMobManager().spawnMob(type, loc);
        }
        plugin.getEffectManager().spawnWaveEffect(center);
        broadcast("event.wave.spawn", "%wave%", String.valueOf(wave));
    }

    private void spawnBoss() {
        Zone zone = getMainZone();
        if (zone != null) plugin.getBossManager().spawnBoss(getZoneCenter(zone));
    }

    private void scheduleNextOpen() {
        int interval = plugin.getConfig().getInt("schedule.interval-minutes", 120);
        nextOpenTime = System.currentTimeMillis() + (interval * 60L * 1000L);
        plugin.getDataManager().setNextOpenTime(nextOpenTime);
        plugin.getLogManager().log("SCHEDULE", "Next open at " + new java.util.Date(nextOpenTime));
    }

    private void createEventBar() {
        if (!plugin.getConfigManager().isBossBar("display.event")) {
            if (eventBar != null) { eventBar.removeAll(); eventBar = null; }
            return;
        }
        if (eventBar != null) eventBar.removeAll();
        eventBar = Bukkit.createBossBar(
            HexUtil.color(plugin.getConfigManager().getBossBarTitle("display.event", "&d&lВоенный комплекс")),
            plugin.getConfigManager().getBossBarColor("display.event", BarColor.PURPLE),
            plugin.getConfigManager().getBossBarStyle("display.event", BarStyle.SOLID)
        );
        for (Player p : Bukkit.getOnlinePlayers()) eventBar.addPlayer(p);
    }

    private void updateBossBar() {
        if (eventBar == null || !running) return;
        String title = plugin.getConfigManager().getBossBarTitle("display.event", "&d&lВоенный комплекс")
            .replace("%phase%", getPhaseName())
            .replace("%timer%", String.valueOf(timer));
        eventBar.setTitle(HexUtil.color(title));
        double max = getPhaseMaxTime();
        eventBar.setProgress(max > 0 ? Math.max(0, Math.min(1, (double)timer / max)) : 1);
    }

    private String getPhaseName() {
        return switch(currentPhase) {
            case PREPARING -> "Подготовка";
            case OPEN -> "Открыт";
            case ASSAULT -> "Штурм";
            case BOSS -> "Босс";
            case CAPTURE -> "Захват";
            case REWARDS -> "Награды";
            case FINISHED -> "Завершение";
            default -> "Закрыт";
        };
    }

    private void startAssault() {
        currentPhase = MilitaryEvent.ASSAULT;
        plugin.getStateManager().setState(MilitaryEvent.ASSAULT);
        wave = 0;
        timer = 0;
        spawnWave();
    }

    private int getPhaseMaxTime() {
        return switch(currentPhase) {
            case PREPARING -> plugin.getConfig().getInt("event.prep-time-seconds", 1800);
            case OPEN -> plugin.getConfig().getInt("event.open-duration", 300);
            case REWARDS -> plugin.getConfig().getInt("event.rewards-duration", 300);
            case FINISHED -> 60;
            default -> 300;
        };
    }

    private void broadcast(String path, String... placeholders) {
        for (String msg : plugin.getConfigManager().getMessageList(path, placeholders)) {
            Bukkit.broadcastMessage(msg);
        }
    }

    private Zone getMainZone() {
        String mainZone = plugin.getConfig().getString("main-zone", "");
        if (mainZone.isEmpty()) {
            for (Zone z : plugin.getZoneManager().getAllZones()) return z;
            return null;
        }
        return plugin.getZoneManager().getZone(mainZone);
    }

    private Location getZoneCenter(Zone zone) {
        if (zone == null) return null;
        org.bukkit.World w = Bukkit.getWorld(zone.getWorldName());
        double x = (zone.getMinX() + zone.getMaxX()) / 2.0;
        double z = (zone.getMinZ() + zone.getMaxZ()) / 2.0;
        double y = w.getHighestBlockYAt((int)x, (int)z);
        return new Location(w, x, y, z);
    }

    public boolean isRunning() { return running; }
    public MilitaryEvent getCurrentPhase() { return currentPhase; }
    public int getTimer() { return timer; }
    public int getWave() { return wave; }
    public long getNextOpenTime() { return nextOpenTime; }
}
