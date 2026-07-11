package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private final LunaMilitaryComplex plugin;
    private FileConfiguration messages, items, mobs, boss, shield, strike;
    private File messagesFile, itemsFile, mobsFile, bossFile, shieldFile, strikeFile;

    public ConfigManager(LunaMilitaryComplex plugin) { this.plugin = plugin; reload(); }

    public void reload() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        itemsFile = new File(plugin.getDataFolder(), "items.yml");
        mobsFile = new File(plugin.getDataFolder(), "mobs.yml");
        bossFile = new File(plugin.getDataFolder(), "boss.yml");
        shieldFile = new File(plugin.getDataFolder(), "shield.yml");
        strikeFile = new File(plugin.getDataFolder(), "strike.yml");
        for (File f : new File[]{messagesFile, itemsFile, mobsFile, bossFile, shieldFile, strikeFile}) {
            if (!f.exists()) try { f.createNewFile(); } catch (Exception e) { e.printStackTrace(); }
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        items = YamlConfiguration.loadConfiguration(itemsFile);
        mobs = YamlConfiguration.loadConfiguration(mobsFile);
        boss = YamlConfiguration.loadConfiguration(bossFile);
        shield = YamlConfiguration.loadConfiguration(shieldFile);
        strike = YamlConfiguration.loadConfiguration(strikeFile);
    }

    public String getMessage(String path) {
        if (messages.contains(path)) {
            if (messages.isList(path)) {
                List<String> list = getMessageList(path);
                return String.join("\n", list);
            } else {
                return HexUtil.color(messages.getString(path, ""));
            }
        }
        return "";
    }

    public List<String> getMessageList(String path) {
        List<String> list = messages.getStringList(path);
        List<String> result = new ArrayList<>();
        for (String line : list) {
            result.add(HexUtil.color(line));
        }
        return result;
    }

    public List<String> getMessageList(String path, String... placeholders) {
        List<String> list = getMessageList(path);
        List<String> result = new ArrayList<>();
        for (String line : list) {
            String processed = line;
            for (int j = 0; j < placeholders.length; j += 2) {
                if (j + 1 < placeholders.length) {
                    processed = processed.replace(placeholders[j], placeholders[j + 1]);
                }
            }
            result.add(processed);
        }
        return result;
    }

    public FileConfiguration getItems() { return items; }
    public FileConfiguration getMobs() { return mobs; }
    public FileConfiguration getBoss() { return boss; }
    public FileConfiguration getShield() { return shield; }
    public FileConfiguration getStrike() { return strike; }
    public FileConfiguration getMessages() { return messages; }

    public String getDisplayType(String section) {
        return plugin.getConfig().getString(section + ".display-type", "actionbar");
    }
    public boolean isActionBar(String section) { return getDisplayType(section).equalsIgnoreCase("actionbar"); }
    public boolean isBossBar(String section) { return getDisplayType(section).equalsIgnoreCase("bossbar"); }
    public boolean isMessage(String section) { return getDisplayType(section).equalsIgnoreCase("message"); }

    public String getBossBarTitle(String section, String fallback) {
        return plugin.getConfig().getString(section + ".bossbar.title", fallback);
    }

    public BarColor getBossBarColor(String section, BarColor fallback) {
        return parseBarColor(plugin.getConfig().getString(section + ".bossbar.color"), fallback);
    }

    public BarStyle getBossBarStyle(String section, BarStyle fallback) {
        return parseBarStyle(plugin.getConfig().getString(section + ".bossbar.style"), fallback);
    }

    private BarColor parseBarColor(String raw, BarColor fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return BarColor.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private BarStyle parseBarStyle(String raw, BarStyle fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return BarStyle.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    public String getWandMaterial() { return items.getString("wand.material", "BLAZE_ROD"); }
    public String getWandName() { return HexUtil.color(items.getString("wand.name", "&d&lZone Wand")); }
    public List<String> getWandLore() { return HexUtil.color(items.getStringList("wand.lore")); }
}
