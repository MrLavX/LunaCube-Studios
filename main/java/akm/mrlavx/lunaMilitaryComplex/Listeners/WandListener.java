package akm.mrlavx.lunaMilitaryComplex.Listeners;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WandListener implements Listener {
    private final LunaMilitaryComplex plugin;
    public WandListener(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        ItemStack item = e.getItem();
        if (!isWand(item)) return;
        e.setCancelled(true);
        if (!e.getPlayer().hasPermission("lunamilitarycomplex.zone")) return;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null) {
            plugin.getSelectionManager().setPos1(e.getPlayer(), e.getClickedBlock().getLocation());
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
            plugin.getSelectionManager().setPos2(e.getPlayer(), e.getClickedBlock().getLocation());
        }
    }

    private boolean isWand(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String mat = plugin.getConfigManager().getWandMaterial();
        if (!item.getType().name().equalsIgnoreCase(mat)) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        String expectedName = plugin.getConfigManager().getWandName();
        String actualName = meta.getDisplayName();
        return expectedName.equals(actualName);
    }
}
