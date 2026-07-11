package akm.mrlavx.lunaMilitaryComplex.Listeners;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.Zone;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {
    private final LunaMilitaryComplex plugin;
    public BlockListener(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (plugin.getItemManager().isCustomItem(item, "shield_module")) {
            e.setCancelled(true);
            plugin.getShieldModuleManager().placeModule(e.getPlayer(), e.getBlock().getLocation());
            item.setAmount(item.getAmount() - 1);
            return;
        }
        if (plugin.getItemManager().isCustomItem(item, "terminal_block")) {
            for (Zone z : plugin.getZoneManager().getAllZones()) {
                if (z.contains(e.getBlock().getLocation())) {
                    z.setTerminalBlock(e.getBlock().getLocation());
                    plugin.getZoneManager().saveZones();
                    e.getPlayer().sendMessage(HexUtil.color("&aТерминал установлен для зоны: &f" + z.getName()));
                    return;
                }
            }
            e.getPlayer().sendMessage(HexUtil.color("&cУстановите терминал внутри зоны!"));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        for (Zone z : plugin.getZoneManager().getAllZones()) {
            Location tb = z.getTerminalBlock();
            if (tb != null && tb.getBlockX() == loc.getBlockX() && tb.getBlockY() == loc.getBlockY() && tb.getBlockZ() == loc.getBlockZ()) {
                z.setTerminalBlock(null);
                plugin.getZoneManager().saveZones();
                e.getPlayer().sendMessage(HexUtil.color("&aТерминал удалён."));
                return;
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (plugin.getTerminalManager().isTerminalBlock(e.getClickedBlock().getLocation())) {
            e.setCancelled(true);
            plugin.getTerminalManager().openTerminal(e.getPlayer());
        }
    }
}
