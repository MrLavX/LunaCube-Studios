package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {
    private final LunaMilitaryComplex plugin;
    private final NamespacedKey itemKey;

    public ItemManager(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "lmc_item");
    }

    public ItemStack createItem(String id, String materialName, String name, List<String> lore, int modelData) {
        Material mat = Material.getMaterial(materialName);
        if (mat == null) mat = Material.PAPER;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(HexUtil.color(name));
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) coloredLore.add(HexUtil.color(line));
                meta.setLore(coloredLore);
            }
            if (modelData > 0) meta.setCustomModelData(modelData);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(itemKey, PersistentDataType.STRING, id);
            item.setItemMeta(meta);
        }
        return item;
    }

    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(itemKey, PersistentDataType.STRING);
    }

    public boolean isCustomItem(ItemStack item, String id) {
        String itemId = getItemId(item);
        return itemId != null && itemId.equals(id);
    }

    public ItemStack getMilitaryComponent() {
        return createItem("military_component",
            plugin.getConfigManager().getItems().getString("items.military-component.material", "IRON_INGOT"),
            plugin.getConfigManager().getItems().getString("items.military-component.name", "&c&lВоенный компонент"),
            plugin.getConfigManager().getItems().getStringList("items.military-component.lore"),
            plugin.getConfigManager().getItems().getInt("items.military-component.model-data", 1001));
    }

    public ItemStack getNavModule() {
        return createItem("nav_module",
            plugin.getConfigManager().getItems().getString("items.nav-module.material", "COMPASS"),
            plugin.getConfigManager().getItems().getString("items.nav-module.name", "&9&lНавигационный модуль"),
            plugin.getConfigManager().getItems().getStringList("items.nav-module.lore"),
            plugin.getConfigManager().getItems().getInt("items.nav-module.model-data", 1002));
    }

    public ItemStack getEnergyCell() {
        return createItem("energy_cell",
            plugin.getConfigManager().getItems().getString("items.energy-cell.material", "GLOWSTONE_DUST"),
            plugin.getConfigManager().getItems().getString("items.energy-cell.name", "&e&lЭнергетическая ячейка"),
            plugin.getConfigManager().getItems().getStringList("items.energy-cell.lore"),
            plugin.getConfigManager().getItems().getInt("items.energy-cell.model-data", 1003));
    }

    public ItemStack getLaunchCore() {
        return createItem("launch_core",
            plugin.getConfigManager().getItems().getString("items.launch-core.material", "NETHER_STAR"),
            plugin.getConfigManager().getItems().getString("items.launch-core.name", "&5&lЯдро запуска"),
            plugin.getConfigManager().getItems().getStringList("items.launch-core.lore"),
            plugin.getConfigManager().getItems().getInt("items.launch-core.model-data", 1004));
    }

    public ItemStack getStrikeCode() {
        return createItem("strike_code",
            plugin.getConfigManager().getItems().getString("items.strike-code.material", "PAPER"),
            plugin.getConfigManager().getItems().getString("items.strike-code.name", "&4&lКод стратегического удара"),
            plugin.getConfigManager().getItems().getStringList("items.strike-code.lore"),
            plugin.getConfigManager().getItems().getInt("items.strike-code.model-data", 1005));
    }

    public ItemStack getShieldModule() {
        return createItem("shield_module",
            plugin.getConfigManager().getItems().getString("items.shield-module.material", "DIAMOND"),
            plugin.getConfigManager().getItems().getString("items.shield-module.name", "&b&lЗащитный модуль"),
            plugin.getConfigManager().getItems().getStringList("items.shield-module.lore"),
            plugin.getConfigManager().getItems().getInt("items.shield-module.model-data", 1006));
    }

    public ItemStack getTerminalBlock() {
        return createItem("terminal_block",
            plugin.getConfigManager().getItems().getString("items.terminal-block.material", "BEACON"),
            plugin.getConfigManager().getItems().getString("items.terminal-block.name", "&d&lТерминал Военного комплекса"),
            plugin.getConfigManager().getItems().getStringList("items.terminal-block.lore"),
            plugin.getConfigManager().getItems().getInt("items.terminal-block.model-data", 1007));
    }
}
