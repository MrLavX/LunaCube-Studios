package akm.mrlavx.lunaMilitaryComplex.GUI;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Bukkit;
import java.util.Arrays;
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
        Inventory inv = Bukkit.createInventory(null, 27, HexUtil.color("&c&lАдмин панель"));
        inv.setItem(10, createItem(Material.GREEN_WOOL, "&a&lСтарт", "&7Запустить комплекс"));
        inv.setItem(12, createItem(Material.RED_WOOL, "&c&lСтоп", "&7Остановить комплекс"));
        inv.setItem(14, createItem(Material.CLOCK, "&e&lСтатус", "&7Текущий статус"));
        inv.setItem(16, createItem(Material.BOOK, "&9&lЖурнал", "&7Логи событий"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().contains("Админ")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        String name = e.getCurrentItem().getItemMeta().getDisplayName();
        if (name.contains("Старт")) { plugin.getEventManager().startEvent(); p.closeInventory(); }
        else if (name.contains("Стоп")) { plugin.getEventManager().stopEvent(); p.closeInventory(); }
        else if (name.contains("Статус")) { p.performCommand("lmc status"); p.closeInventory(); }
        else if (name.contains("Журнал")) { plugin.getLogGUI().open(p); }
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
