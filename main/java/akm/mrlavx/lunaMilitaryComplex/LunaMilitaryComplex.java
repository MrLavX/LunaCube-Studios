package akm.mrlavx.lunaMilitaryComplex;

import akm.mrlavx.lunaMilitaryComplex.Commands.MainCommand;
import akm.mrlavx.lunaMilitaryComplex.GUI.*;
import akm.mrlavx.lunaMilitaryComplex.Listeners.*;
import akm.mrlavx.lunaMilitaryComplex.Managers.*;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class LunaMilitaryComplex extends JavaPlugin {
    private static LunaMilitaryComplex instance;
    private ConfigManager configManager;
    private ZoneManager zoneManager;
    private SelectionManager selectionManager;
    private EventManager eventManager;
    private ItemManager itemManager;
    private MobManager mobManager;
    private BossManager bossManager;
    private CaptureManager captureManager;
    private TerminalManager terminalManager;
    private StrikeManager strikeManager;
    private ShieldModuleManager shieldModuleManager;
    private HologramManager hologramManager;
    private DataManager dataManager;
    private LogManager logManager;
    private StateManager stateManager;
    private EffectManager effectManager;
    private LogGUI logGUI;
    private AdminGUI adminGUI;
    private TerminalGUI terminalGUI;
    private ShieldGUI shieldGUI;
    private ConfirmGUI confirmGUI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfigs();
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        logManager = new LogManager(this);
        stateManager = new StateManager(this);
        zoneManager = new ZoneManager(this);
        selectionManager = new SelectionManager(this);
        itemManager = new ItemManager(this);
        mobManager = new MobManager(this);
        bossManager = new BossManager(this);
        captureManager = new CaptureManager(this);
        terminalManager = new TerminalManager(this);
        strikeManager = new StrikeManager(this);
        shieldModuleManager = new ShieldModuleManager(this);
        hologramManager = new HologramManager(this);
        effectManager = new EffectManager(this);
        eventManager = new EventManager(this);

        registerCommands();
        registerListeners();
        registerGUI();
        startTasks();

        getLogger().info(HexUtil.color("&d================================="));
        getLogger().info(HexUtil.color("&f  LunaMilitaryComplex &a" + getDescription().getVersion()));
        getLogger().info(HexUtil.color("&f  Author: &9MrLavX"));
        getLogger().info(HexUtil.color("&f  Minecraft: &d1.21.11"));
        getLogger().info(HexUtil.color("&f  Java: &a21"));
        getLogger().info(HexUtil.color("&d================================="));
        logManager.log("Plugin enabled");
    }

    @Override
    public void onDisable() {
        if (zoneManager != null) zoneManager.saveZones();
        if (dataManager != null) dataManager.save();
        if (shieldModuleManager != null) shieldModuleManager.save();
        if (hologramManager != null) hologramManager.removeAll();
        if (eventManager != null) eventManager.stopEvent();
        logManager.log("Plugin disabled");
        getLogger().info("LunaMilitaryComplex disabled.");
    }

    private void saveDefaultConfigs() {
        saveDefaultConfig();
        for (String f : new String[]{"messages.yml","items.yml","zones.yml","mobs.yml","data.yml","logs.yml"}) {
            saveResourceSafe(f);
        }
        for (String m : new String[]{"Menu/confirm_menu.yml","Menu/terminal_menu.yml","Menu/shield-of-defense.yml"}) {
            saveResourceSafe(m);
        }
    }

    private void saveResourceSafe(String path) {
        File file = new File(getDataFolder(), path);
        if (!file.exists()) saveResource(path, false);
    }

    private void registerCommands() {
        MainCommand cmd = new MainCommand(this);
        getCommand("lunamilitarycomplex").setExecutor(cmd);
        getCommand("lunamilitarycomplex").setTabCompleter(cmd);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new WandListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
    }

    private void registerGUI() {
        terminalGUI = new TerminalGUI(this);
        shieldGUI = new ShieldGUI(this);
        adminGUI = new AdminGUI(this);
        logGUI = new LogGUI(this);
        confirmGUI = new ConfirmGUI(this);
        Bukkit.getPluginManager().registerEvents(terminalGUI, this);
        Bukkit.getPluginManager().registerEvents(shieldGUI, this);
        Bukkit.getPluginManager().registerEvents(adminGUI, this);
        Bukkit.getPluginManager().registerEvents(logGUI, this);
        Bukkit.getPluginManager().registerEvents(confirmGUI, this);
    }

    private void startTasks() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            selectionManager.showSelectionEffects();
            zoneManager.checkPlayersInZones();
            eventManager.tick();
            captureManager.tick();
            shieldModuleManager.tick();
            effectManager.tick();
        }, 0L, 5L);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            dataManager.tick();
        }, 0L, 20L);
    }

    public void reloadPlugin() {
        reloadConfig();
        configManager.reload();
        zoneManager.reload();
        selectionManager.reload();
        eventManager.reload();
        mobManager.reload();
        bossManager.reload();
        strikeManager.reload();
        shieldModuleManager.reload();
        dataManager.reload();
        logManager.log("Plugin reloaded");
    }

    public static LunaMilitaryComplex getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public ZoneManager getZoneManager() { return zoneManager; }
    public SelectionManager getSelectionManager() { return selectionManager; }
    public EventManager getEventManager() { return eventManager; }
    public ItemManager getItemManager() { return itemManager; }
    public MobManager getMobManager() { return mobManager; }
    public BossManager getBossManager() { return bossManager; }
    public CaptureManager getCaptureManager() { return captureManager; }
    public TerminalManager getTerminalManager() { return terminalManager; }
    public StrikeManager getStrikeManager() { return strikeManager; }
    public ShieldModuleManager getShieldModuleManager() { return shieldModuleManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public DataManager getDataManager() { return dataManager; }
    public LogManager getLogManager() { return logManager; }
    public StateManager getStateManager() { return stateManager; }
    public EffectManager getEffectManager() { return effectManager; }
    public LogGUI getLogGUI() { return logGUI; }
}
