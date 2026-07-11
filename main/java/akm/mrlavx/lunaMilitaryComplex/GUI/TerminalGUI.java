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

public class TerminalGUI implements Listener {
    private final LunaMilitaryComplex plugin;
    public TerminalGUI(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, HexUtil.color("&d&lТерминал Военного комплекса"));
        inv.setItem(20, createItem(Material.COMPASS, "&9&lВвод координат", "&7Нажмите для ввода цели"));
        inv.setItem(22, createItem(Material.TNT, "&4&lЗапуск удара", "&7Требуется код удара"));
        inv.setItem(24, createItem(Material.BOOK, "&e&lЖурнал ударов", "&7История запусков"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().contains("Терминал")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        String name = e.getCurrentItem().getItemMeta().getDisplayName();
        if (name.contains("Ввод координат")) {
            p.sendMessage("Введите координаты: /lmc strike <x> <y> <z>");
            p.closeInventory();
        } else if (name.contains("Запуск удара")) {
            p.sendMessage("Используйте: /lmc strike <x> <y> <z>");
            p.closeInventory();
        } else if (name.contains("Журнал")) {
            plugin.getLogGUI().open(p);
        }
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
