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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmGUI implements Listener {
    private final LunaMilitaryComplex plugin;
    private final Map<UUID, Runnable> confirmations = new HashMap<>();
    public ConfirmGUI(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    public void open(Player player, String action, Runnable onConfirm) {
        FileConfiguration menu = plugin.getConfigManager().getMenu("confirm_menu");
        Inventory inv = Bukkit.createInventory(null, menu.getInt("size", 27), HexUtil.color(menu.getString("title", "&c&lПодтверждение")));
        inv.setItem(menu.getInt("items.confirm.slot", 12), createItem(
            resolveMaterial(menu.getString("items.confirm.material", "LIME_WOOL")),
            menu.getString("items.confirm.name", "&a&lПодтвердить").replace("%action%", action),
            menu.getStringList("items.confirm.lore").stream().map(line -> line.replace("%action%", action)).toArray(String[]::new)
        ));
        inv.setItem(menu.getInt("items.cancel.slot", 14), createItem(
            resolveMaterial(menu.getString("items.cancel.material", "RED_WOOL")),
            menu.getString("items.cancel.name", "&c&lОтмена").replace("%action%", action),
            menu.getStringList("items.cancel.lore").stream().map(line -> line.replace("%action%", action)).toArray(String[]::new)
        ));
        confirmations.put(player.getUniqueId(), onConfirm);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        FileConfiguration menu = plugin.getConfigManager().getMenu("confirm_menu");
        if (!HexUtil.color(menu.getString("title", "&c&lПодтверждение")).equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        Runnable action = confirmations.remove(player.getUniqueId());
        if (e.getSlot() == menu.getInt("items.confirm.slot", 12)) {
            player.closeInventory();
            if (action != null) action.run();
        } else if (e.getSlot() == menu.getInt("items.cancel.slot", 14)) {
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

    private Material resolveMaterial(String name) {
        Material mat = Material.matchMaterial(name);
        return mat != null ? mat : Material.STONE;
    }
}
