package akm.mrlavx.lunaMilitaryComplex.GUI;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LogGUI implements Listener {
    private final LunaMilitaryComplex plugin;
    public LogGUI(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, HexUtil.color("&9&lЖурнал событий"));
        List<String> logs = plugin.getLogManager().getLogs(45);
        for (int i = 0; i < Math.min(logs.size(), 45); i++) {
            inv.setItem(i, createItem(Material.PAPER, logs.get(i)));
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().contains("Журнал")) return;
        e.setCancelled(true);
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(HexUtil.color(name));
        item.setItemMeta(meta);
        return item;
    }
}
