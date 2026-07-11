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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmGUI implements Listener {
    private final LunaMilitaryComplex plugin;
    private final Map<UUID, Runnable> confirmations = new HashMap<>();
    public ConfirmGUI(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    public void open(Player player, String action, Runnable onConfirm) {
        Inventory inv = Bukkit.createInventory(null, 27, HexUtil.color("&c&lПодтверждение: " + action));
        inv.setItem(12, createItem(Material.LIME_WOOL, "&a&lПодтвердить", "&7Нажмите для подтверждения"));
        inv.setItem(14, createItem(Material.RED_WOOL, "&c&lОтмена", "&7Нажмите для отмены"));
        confirmations.put(player.getUniqueId(), onConfirm);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().contains("Подтверждение")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;
        String name = e.getCurrentItem().getItemMeta().getDisplayName();
        Runnable action = confirmations.remove(player.getUniqueId());
        if (name.contains("Подтвердить")) {
            player.closeInventory();
            if (action != null) action.run();
        } else if (name.contains("Отмена")) {
            player.closeInventory();
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
