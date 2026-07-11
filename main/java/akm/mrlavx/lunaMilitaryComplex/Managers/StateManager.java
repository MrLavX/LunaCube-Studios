package akm.mrlavx.lunaMilitaryComplex.Managers;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.MilitaryEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class StateManager {
    private final LunaMilitaryComplex plugin;
    private MilitaryEvent currentState = MilitaryEvent.CLOSED;
    private File file;
    private FileConfiguration config;

    public StateManager(LunaMilitaryComplex plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "state.yml");
        if (!file.exists()) { 
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); } 
        }
        config = YamlConfiguration.loadConfiguration(file);
        String state = config.getString("state", "CLOSED");
        try { currentState = MilitaryEvent.valueOf(state); } catch (Exception e) { currentState = MilitaryEvent.CLOSED; }
    }

    public void save() {
        config.set("state", currentState.name());
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public MilitaryEvent getState() { return currentState; }
    public void setState(MilitaryEvent state) { this.currentState = state; save(); }
    public boolean isClosed() { return currentState == MilitaryEvent.CLOSED; }
    public boolean isPreparing() { return currentState == MilitaryEvent.PREPARING; }
    public boolean isOpen() { return currentState == MilitaryEvent.OPEN; }
    public boolean isAssault() { return currentState == MilitaryEvent.ASSAULT; }
    public boolean isBoss() { return currentState == MilitaryEvent.BOSS; }
    public boolean isCapture() { return currentState == MilitaryEvent.CAPTURE; }
    public boolean isRewards() { return currentState == MilitaryEvent.REWARDS; }
    public boolean isFinished() { return currentState == MilitaryEvent.FINISHED; }
}
