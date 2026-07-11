package akm.mrlavx.lunaMilitaryComplex.Listeners;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final LunaMilitaryComplex plugin;
    public PlayerListener(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getSelectionManager().removeSelection(e.getPlayer());
        plugin.getZoneManager().removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getEventManager().addViewer(e.getPlayer());
    }
}
