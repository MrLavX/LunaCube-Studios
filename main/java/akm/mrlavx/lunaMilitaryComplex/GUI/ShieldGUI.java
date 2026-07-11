package akm.mrlavx.lunaMilitaryComplex.GUI;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.ShieldModule;
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

public class ShieldGUI implements Listener {
    private final LunaMilitaryComplex plugin;
    public ShieldGUI(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, HexUtil.color("&b&lЗащитный модуль"));
        inv.setItem(11, createItem(Material.LIME_WOOL, "&a&lСтатус", "&7Активен"));
        inv.setItem(13, createItem(Material.GLOWSTONE_DUST, "&e&lЭнергия", "&7Заряд модуля"));
        inv.setItem(15, createItem(Material.BOOK, "&9&lЖурнал", "&7История событий"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().contains("Защитный модуль")) return;
        e.setCancelled(true);
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
