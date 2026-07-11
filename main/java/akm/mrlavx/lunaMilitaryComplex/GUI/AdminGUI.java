package akm.mrlavx.lunaMilitaryComplex.GUI;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminGUI implements Listener {
    private final LunaMilitaryComplex plugin;
    public AdminGUI(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    public void open(Player player) {
        FileConfiguration menu = plugin.getConfigManager().getMenu("admin_menu");
        Inventory inv = Bukkit.createInventory(null, menu.getInt("size", 27), HexUtil.color(menu.getString("title", "&c&lАдмин панель")));
        setItem(inv, menu, "start");
        setItem(inv, menu, "stop");
        setItem(inv, menu, "status");
        setItem(inv, menu, "logs");
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        FileConfiguration menu = plugin.getConfigManager().getMenu("admin_menu");
        if (!HexUtil.color(menu.getString("title", "&c&lАдмин панель")).equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if (e.getSlot() == menu.getInt("items.start.slot", 10)) { plugin.getEventManager().startEvent(); p.closeInventory(); }
        else if (e.getSlot() == menu.getInt("items.stop.slot", 12)) { plugin.getEventManager().stopEvent(); p.closeInventory(); }
        else if (e.getSlot() == menu.getInt("items.status.slot", 14)) { p.performCommand("lmc status"); p.closeInventory(); }
        else if (e.getSlot() == menu.getInt("items.logs.slot", 16)) { plugin.getLogGUI().open(p); }
    }

    private void setItem(Inventory inv, FileConfiguration menu, String key) {
        String base = "items." + key + ".";
        Material material = Material.matchMaterial(menu.getString(base + "material", "STONE"));
        if (material == null) material = Material.STONE;
        inv.setItem(menu.getInt(base + "slot", 0), createItem(material,
            menu.getString(base + "name", "&f" + key),
            menu.getStringList(base + "lore").toArray(new String[0])));
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(HexUtil.color(name));
        if (lore.length > 0) {
            java.util.List<String> coloredLore = new java.util.ArrayList<>();
            for (String line : lore) coloredLore.add(HexUtil.color(line));
            meta.setLore(coloredLore);
        }
        item.setItemMeta(meta);
        return item;
    }
}
