package akm.mrlavx.lunaMilitaryComplex.Listeners;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class InventoryListener implements Listener {
    private final LunaMilitaryComplex plugin;
    public InventoryListener(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (title.equals(plugin.getConfigManager().getMenuTitle("terminal_menu", "&d&lТерминал")) ||
            title.equals(plugin.getConfigManager().getMenuTitle("shield-of-defense", "&b&lЗащитный модуль")) ||
            title.equals(plugin.getConfigManager().getMenuTitle("admin_menu", "&c&lАдмин панель")) ||
            title.equals(plugin.getConfigManager().getMenuTitle("log_menu", "&9&lЖурнал событий")) ||
            title.equals(plugin.getConfigManager().getMenuTitle("confirm_menu", "&c&lПодтверждение"))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (title.equals(plugin.getConfigManager().getMenuTitle("terminal_menu", "&d&lТерминал")) ||
            title.equals(plugin.getConfigManager().getMenuTitle("shield-of-defense", "&b&lЗащитный модуль")) ||
            title.equals(plugin.getConfigManager().getMenuTitle("admin_menu", "&c&lАдмин панель")) ||
            title.equals(plugin.getConfigManager().getMenuTitle("log_menu", "&9&lЖурнал событий")) ||
            title.equals(plugin.getConfigManager().getMenuTitle("confirm_menu", "&c&lПодтверждение"))) {
            e.setCancelled(true);
        }
    }
}
