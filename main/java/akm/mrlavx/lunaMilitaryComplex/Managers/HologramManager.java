package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.ShieldModule;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class HologramManager {
    private final LunaMilitaryComplex plugin;
    private final Map<String, Object> holograms = new HashMap<>();
    private boolean decentHolograms = false;

    public HologramManager(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
        try {
            decentHolograms = plugin.getServer().getPluginManager().getPlugin("DecentHolograms") != null;
        } catch (Exception e) {
            decentHolograms = false;
        }
    }

    public void createShieldHologram(ShieldModule module) {
        if (!decentHolograms) return;
        Location loc = module.getLocation().clone().add(0, 2.5, 0);
        try {
            invokeDhApi("createHologram", new Class<?>[] {
                String.class, Location.class, String[].class
            }, "lmc_shield_" + module.getId(), loc, new String[] {
                HexUtil.color("&b&lЗащитный модуль"),
                HexUtil.color("&7Владелец: &f" + org.bukkit.Bukkit.getOfflinePlayer(module.getOwner()).getName()),
                HexUtil.color("&7Энергия: &e" + (int) module.getEnergyPercent() + "%")
            });
            holograms.put(module.getId(), "dh");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void updateShieldHologram(ShieldModule module) {
        if (!decentHolograms) return;
        try {
            removeShieldHologram(module.getId());
            createShieldHologram(module);
        } catch (Exception e) {}
    }

    public void removeShieldHologram(String id) {
        if (!decentHolograms) return;
        try {
            invokeDhApi("removeHologram", new Class<?>[] { String.class }, "lmc_shield_" + id);
            holograms.remove(id);
        } catch (Exception e) {}
    }

    public void removeAll() {
        for (String id : new HashMap<>(holograms).keySet()) {
            removeShieldHologram(id);
        }
    }

    private Object invokeDhApi(String method, Class<?>[] signature, Object... args) throws Exception {
        Class<?> dhapi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
        return dhapi.getMethod(method, signature).invoke(null, args);
    }
}
