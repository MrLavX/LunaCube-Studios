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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class EventManager {
    private final LunaMilitaryComplex plugin;
    private BossBar eventBar;
    private int timer = 0;
    private int wave = 0;
    private boolean running = false;
    private boolean awaitingWave = false;
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
        if (nextOpenTime == 0 && plugin.getConfig().getBoolean("schedule.enabled", true)) {
            scheduleNextOpen();
        }
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
        if (!plugin.getConfig().getBoolean("schedule.enabled", true)) {
            return;
        }
        if (System.currentTimeMillis() >= nextOpenTime && nextOpenTime > 0) {
            startEvent();
        }
    }

    public void startEvent() {
        if (running) return;
        running = true;
        prepSeconds = plugin.getConfig().getInt("event.prep-time-seconds", 1800);
        timer = prepSeconds;
        plugin.getTerminalManager().setTerminalOpen(false);
        setPhase(MilitaryEvent.PREPARING);
        broadcast("event.preparing.start");
        plugin.getLogManager().log("EVENT", "Event started - preparing phase");
        createEventBar();
        announcePhase("event.phase.preparing", HexUtil.formatDuration(timer));
    }

    public void stopEvent() {
        running = false;
        setPhase(MilitaryEvent.CLOSED);
        plugin.getMobManager().clearMobs();
        plugin.getBossManager().despawnBoss();
        plugin.getCaptureManager().reset();
        plugin.getHologramManager().removeAll();
        awaitingWave = false;
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
            broadcast("event.preparing.countdown", "%time%", HexUtil.formatDuration(remaining));
            Zone zone = getMainZone();
            if (zone != null) {
                Location center = getZoneCenter(zone);
                if (center != null) {
                    plugin.getEffectManager().playCountdownEffect(center, remaining);
                }
            }
        }
        else if (remaining <= 0) {
            setPhase(MilitaryEvent.OPEN);
            timer = plugin.getConfig().getInt("event.open-duration", 300);
            plugin.getTerminalManager().setTerminalOpen(true);
            broadcast("event.opened");
            plugin.getLogManager().log("EVENT", "Complex opened");
            announcePhase("event.phase.open", HexUtil.formatDuration(timer));
        }
    }

    private void tickOpen() {
        if (timer <= 0) {
            currentPhase = MilitaryEvent.ASSAULT;
            startAssault();
        }
    }

    private void tickAssault() {
        if (awaitingWave) return;
        if (!plugin.getMobManager().hasAliveMobs()) {
            wave++;
            int maxWaves = plugin.getConfig().getInt("event.waves", 5);
            if (wave > maxWaves) {
                setPhase(MilitaryEvent.BOSS);
                broadcast("event.boss.start");
                spawnBoss();
                announcePhase("event.phase.boss", "");
            } else {
                broadcast("event.wave.complete", "%wave%", String.valueOf(wave));
                timer = plugin.getConfig().getInt("event.wave-delay-seconds", 10);
                awaitingWave = true;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    awaitingWave = false;
                    spawnWave();
                }, timer * 20L);
            }
        }
    }

    private void tickBoss() {
        if (!plugin.getBossManager().isActive()) {
            setPhase(MilitaryEvent.CAPTURE);
            startCapture();
        }
    }

    private void tickCapture() {}

    private void tickRewards() {
        if (timer <= 0) {
            setPhase(MilitaryEvent.FINISHED);
            timer = 60;
            announcePhase("event.phase.rewards", HexUtil.formatDuration(timer));
        }
    }

    private void tickFinished() {
        if (timer <= 0) {
            stopEvent();
        }
    }

    public void startCapture() {
        setPhase(MilitaryEvent.CAPTURE);
        Location center = getZoneCenter(getMainZone());
        if (center != null) {
            plugin.getCaptureManager().startCapture(center);
        }
        broadcast("event.capture.start");
        plugin.getLogManager().log("EVENT", "Capture phase started");
        announcePhase("event.phase.capture", "");
    }

    public void onCaptureComplete(Player winner) {
        setPhase(MilitaryEvent.REWARDS);
        timer = plugin.getConfig().getInt("event.rewards-duration", 300);
        broadcast("event.capture.complete", "%player%", winner.getName());
        plugin.getLogManager().log("CAPTURE", "Captured by " + winner.getName());
        giveRewards(winner);
        announcePhase("event.phase.rewards", HexUtil.formatDuration(timer));
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
        if (center == null) return;
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
        if (!plugin.getConfig().getBoolean("schedule.enabled", true)) {
            nextOpenTime = 0;
            plugin.getDataManager().setNextOpenTime(0);
            return;
        }
        List<String> times = plugin.getConfig().getStringList("schedule.times");
        String timezone = plugin.getConfig().getString("schedule.timezone", "Europe/Moscow");
        nextOpenTime = computeNextOpenTime(times, timezone);
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
            .replace("%time%", HexUtil.formatDuration(timer));
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

    public boolean canEnterMainZone() {
        return currentPhase == MilitaryEvent.OPEN || currentPhase == MilitaryEvent.ASSAULT
            || currentPhase == MilitaryEvent.BOSS || currentPhase == MilitaryEvent.CAPTURE
            || currentPhase == MilitaryEvent.REWARDS;
    }

    public void setPhase(MilitaryEvent phase) {
        currentPhase = phase;
        plugin.getStateManager().setState(phase);
        plugin.getTerminalManager().setTerminalOpen(phase == MilitaryEvent.OPEN || phase == MilitaryEvent.ASSAULT || phase == MilitaryEvent.BOSS || phase == MilitaryEvent.CAPTURE || phase == MilitaryEvent.REWARDS);
    }

    public void jumpToPhase(MilitaryEvent phase) {
        if (phase == MilitaryEvent.CLOSED) {
            stopEvent();
            return;
        }
        running = true;
        setPhase(phase);
        switch (phase) {
            case PREPARING -> {
                timer = plugin.getConfig().getInt("event.prep-time-seconds", 1800);
                createEventBar();
                broadcast("event.preparing.start");
                announcePhase("event.phase.preparing", HexUtil.formatDuration(timer));
            }
            case OPEN -> {
                timer = plugin.getConfig().getInt("event.open-duration", 300);
                createEventBar();
                broadcast("event.opened");
                announcePhase("event.phase.open", HexUtil.formatDuration(timer));
            }
            case ASSAULT -> {
                broadcast("event.phase.assault");
                startAssault();
            }
            case BOSS -> {
                broadcast("event.boss.start");
                spawnBoss();
                announcePhase("event.phase.boss", "");
            }
            case CAPTURE -> {
                startCapture();
            }
            case REWARDS -> {
                timer = plugin.getConfig().getInt("event.rewards-duration", 300);
                broadcast("event.phase.rewards", "%time%", HexUtil.formatDuration(timer));
                announcePhase("event.phase.rewards", HexUtil.formatDuration(timer));
            }
            case FINISHED -> timer = 60;
            case CLOSED -> stopEvent();
        }
    }

    private void announcePhase(String path, String time) {
        if (!plugin.getConfig().getBoolean("event.title.enabled", true)) return;
        String title = plugin.getConfig().getString("display.event.title.text", "&d&lВоенный комплекс");
        String subtitle = plugin.getConfig().getString("display.event.title.subtitle", "&7%phase% &f%time%");
        String phase = getPhaseName();
        int fadeIn = plugin.getConfig().getInt("event.title.fade-in", 10);
        int stay = plugin.getConfig().getInt("event.title.stay", 60);
        int fadeOut = plugin.getConfig().getInt("event.title.fade-out", 10);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(
                HexUtil.color(title.replace("%phase%", phase).replace("%time%", time)),
                HexUtil.color(subtitle.replace("%phase%", phase).replace("%time%", time)),
                fadeIn, stay, fadeOut
            );
        }
    }

    private long computeNextOpenTime(List<String> times, String timezoneId) {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timezoneId);
        } catch (Exception ignored) {
            zoneId = ZoneId.systemDefault();
        }
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime candidate = now.plusMinutes(Math.max(1, plugin.getConfig().getInt("schedule.interval-minutes", 120)));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        for (String raw : times) {
            try {
                LocalTime localTime = LocalTime.parse(raw, formatter);
                ZonedDateTime next = now.withHour(localTime.getHour()).withMinute(localTime.getMinute()).withSecond(0).withNano(0);
                if (next.isBefore(now)) {
                    next = next.plusDays(1);
                }
                if (candidate == null || next.isBefore(candidate)) {
                    candidate = next;
                }
            } catch (Exception ignored) {}
        }
        return candidate.toInstant().toEpochMilli();
    }

    private void startAssault() {
        setPhase(MilitaryEvent.ASSAULT);
        wave = 0;
        timer = 0;
        awaitingWave = false;
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
        if (w == null) return null;
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
    public void addViewer(Player player) { if (eventBar != null && player != null) eventBar.addPlayer(player); }
}
