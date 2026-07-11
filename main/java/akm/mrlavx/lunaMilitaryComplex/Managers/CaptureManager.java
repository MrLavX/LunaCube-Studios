package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import akm.mrlavx.lunaMilitaryComplex.Utils.ParticleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class CaptureManager {
    private final LunaMilitaryComplex plugin;
    private Location captureCenter;
    private double captureRadius = 5.0;
    private int captureTime = 60;
    private int currentProgress = 0;
    private UUID capturer = null;
    private boolean active = false;
    private Map<UUID, Integer> playerProgress = new HashMap<>();
    private final Map<UUID, BossBar> captureBars = new HashMap<>();

    public CaptureManager(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    public void startCapture(Location center) {
        if (center == null || center.getWorld() == null) return;
        this.captureCenter = center;
        this.captureRadius = plugin.getConfig().getDouble("capture.radius", 5.0);
        this.captureTime = plugin.getConfig().getInt("capture.time-seconds", 60);
        this.currentProgress = 0;
        this.capturer = null;
        this.playerProgress.clear();
        clearCaptureBars();
        this.active = true;
    }

    public void reset() {
        active = false;
        captureCenter = null;
        capturer = null;
        currentProgress = 0;
        playerProgress.clear();
        clearCaptureBars();
    }

    public void tick() {
        if (!active || captureCenter == null) return;
        ParticleUtil.drawCircle(captureCenter, captureRadius, Color.GREEN, 30);
        Player inZone = null;
        int playersInZone = 0;
        Set<UUID> inZonePlayers = new HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(captureCenter.getWorld()) && p.getLocation().distance(captureCenter) <= captureRadius) {
                playersInZone++;
                inZone = p;
                inZonePlayers.add(p.getUniqueId());
            }
        }
        if (playersInZone == 1 && inZone != null) {
            if (capturer == null || capturer.equals(inZone.getUniqueId())) {
                capturer = inZone.getUniqueId();
                currentProgress++;
                playerProgress.put(inZone.getUniqueId(), currentProgress);
                sendCaptureBar(inZone, currentProgress, captureTime);
                if (currentProgress >= captureTime) {
                    active = false;
                    plugin.getEventManager().onCaptureComplete(inZone);
                }
            } else {
                sendCaptureMessage(inZone, "capture.contested");
            }
        } else if (playersInZone > 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(captureCenter.getWorld()) && p.getLocation().distance(captureCenter) <= captureRadius) {
                    sendCaptureMessage(p, "capture.multiple");
                }
            }
        } else {
            if (plugin.getConfig().getBoolean("capture.reset-on-leave", false)) {
                currentProgress = 0;
                capturer = null;
            } else if (plugin.getConfig().getBoolean("capture.pause-on-leave", true)) {
                // Pause - do nothing
            }
        }
        cleanupCaptureBars(inZonePlayers);
    }

    private void sendCaptureBar(Player p, int progress, int max) {
        String msg = plugin.getConfigManager().getMessage("capture.progress")
            .replace("%progress%", String.valueOf(progress))
            .replace("%max%", String.valueOf(max))
            .replace("%percent%", String.valueOf((progress*100)/max));
        msg = HexUtil.color(msg);

        String captureDisplay = plugin.getConfigManager().getDisplayType("display.capture");
        if (captureDisplay.equalsIgnoreCase("actionbar")) {
            try { p.sendActionBar(net.kyori.adventure.text.Component.text(msg)); }
            catch (Exception e) { p.sendMessage(msg); }
        } else if (captureDisplay.equalsIgnoreCase("bossbar")) {
            BossBar bar = captureBars.get(p.getUniqueId());
            if (bar == null) {
                bar = Bukkit.createBossBar(
                    HexUtil.color(plugin.getConfigManager().getBossBarTitle("display.capture", "&a&lЗахват территории")),
                    plugin.getConfigManager().getBossBarColor("display.capture", BarColor.GREEN),
                    plugin.getConfigManager().getBossBarStyle("display.capture", BarStyle.SOLID)
                );
                bar.addPlayer(p);
                captureBars.put(p.getUniqueId(), bar);
            }
            bar.setTitle(HexUtil.color(
                plugin.getConfigManager().getBossBarTitle("display.capture", "&a&lЗахват территории")
                    .replace("%progress%", String.valueOf(progress))
                    .replace("%max%", String.valueOf(max))
                    .replace("%percent%", String.valueOf((progress * 100) / max))
            ));
            bar.setProgress(Math.max(0, Math.min(1, (double) progress / max)));
        } else {
            p.sendMessage(msg);
        }
    }

    private void sendCaptureMessage(Player p, String path) {
        for (String msg : plugin.getConfigManager().getMessageList(path)) {
            p.sendMessage(msg);
        }
    }

    private void cleanupCaptureBars(Set<UUID> activePlayers) {
        captureBars.entrySet().removeIf(entry -> {
            if (activePlayers.contains(entry.getKey())) {
                return false;
            }
            entry.getValue().removeAll();
            return true;
        });
    }

    private void clearCaptureBars() {
        captureBars.values().forEach(BossBar::removeAll);
        captureBars.clear();
    }

    public boolean isActive() { return active; }
    public Location getCaptureCenter() { return captureCenter; }
    public int getProgress() { return currentProgress; }
    public UUID getCapturer() { return capturer; }
}
