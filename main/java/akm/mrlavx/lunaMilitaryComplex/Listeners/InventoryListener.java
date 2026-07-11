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
        if (title.contains("Терминал") || title.contains("Защитный модуль") || title.contains("Админ") || title.contains("Журнал") || title.contains("Подтверждение")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (title.contains("Терминал") || title.contains("Защитный модуль") || title.contains("Админ") || title.contains("Журнал") || title.contains("Подтверждение")) {
            e.setCancelled(true);
        }
    }
}
