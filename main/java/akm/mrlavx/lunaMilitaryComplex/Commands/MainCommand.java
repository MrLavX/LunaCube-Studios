package akm.mrlavx.lunaMilitaryComplex.Commands;

import akm.mrlavx.lunaMilitaryComplex.LunaMilitaryComplex;
import akm.mrlavx.lunaMilitaryComplex.Models.MilitaryEvent;
import akm.mrlavx.lunaMilitaryComplex.Models.Selection;
import akm.mrlavx.lunaMilitaryComplex.Models.Zone;
import akm.mrlavx.lunaMilitaryComplex.Utils.HexUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {
    private final LunaMilitaryComplex plugin;
    public MainCommand(LunaMilitaryComplex plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { sendHelp(sender); return true; }
        switch (args[0].toLowerCase()) {
            case "zone": handleZone(sender, args); break;
            case "start": handleStart(sender); break;
            case "stop": handleStop(sender); break;
            case "stage": handleStage(sender, args); break;
            case "next": handleNext(sender); break;
            case "status": handleStatus(sender); break;
            case "give": handleGive(sender, args); break;
            case "boss": handleBoss(sender, args); break;
            case "strike": handleStrike(sender, args); break;
            case "shield": handleShield(sender, args); break;
            case "reload": handleReload(sender); break;
            case "debug": handleDebug(sender); break;
            case "help": sendHelp(sender); break;
            default: sendMessage(sender, "general.unknown-command"); break;
        }
        return true;
    }

    private void handleZone(CommandSender sender, String[] args) {
        if (!checkPerm(sender, "lunamilitarycomplex.zone")) return;
        if (!(sender instanceof Player)) { sendMessage(sender, "general.player-only"); return; }
        Player p = (Player) sender;
        if (args.length < 2) { sendMessageList(p, "zone.help"); return; }
        switch (args[1].toLowerCase()) {
            case "wand": giveWand(p); break;
            case "effect": plugin.getSelectionManager().toggleEffects(p); break;
            case "create":
                if (args.length < 3) { sendMessageList(p, "zone.create.usage"); return; }
                createZone(p, args); break;
            case "delete":
                if (args.length < 3) { sendMessageList(p, "zone.delete.usage"); return; }
                deleteZone(p, args[2]); break;
            case "toggle":
                if (args.length < 3) { sendMessageList(p, "zone.toggle.usage"); return; }
                toggleZone(p, args[2]); break;
            case "list": listZones(p); break;
            case "info":
                if (args.length < 3) { sendMessageList(p, "zone.info.usage"); return; }
                zoneInfo(p, args[2]); break;
            case "terminal":
                Zone z = plugin.getZoneManager().getZoneAt(p.getLocation());
                if (z != null) { plugin.getTerminalManager().placeTerminalBlock(z); p.sendMessage(HexUtil.color("&aТерминал размещён для зоны: &f" + z.getName())); }
                else p.sendMessage(HexUtil.color("&cВы не в зоне."));
                break;
            case "highlight":
                if (args.length < 4) { p.sendMessage(HexUtil.color("&cИспользование: /lmc zone highlight <название> <минуты>")); return; }
                highlightZone(p, args[2], args[3]);
                break;
            default: sendMessageList(p, "zone.help"); break;
        }
    }

    private void highlightZone(Player p, String zoneName, String minutesStr) {
        Zone z = plugin.getZoneManager().getZone(zoneName);
        if (z == null) { p.sendMessage(HexUtil.color("&cЗона не найдена.")); return; }
        try {
            int minutes = Integer.parseInt(minutesStr);
            plugin.getZoneManager().enableZoneEffect(zoneName, minutes);
            p.sendMessage(HexUtil.color("&aЭффекты зоны '&f" + zoneName + "&a' включены на &f" + HexUtil.formatDuration(minutes * 60L) + "&a."));
        } catch (NumberFormatException e) {
            p.sendMessage(HexUtil.color("&cУкажите число минут."));
        }
    }

    private void handleStart(CommandSender sender) {
        if (!checkPerm(sender, "lunamilitarycomplex.admin")) return;
        plugin.getEventManager().startEvent();
        sendMessageList(sender, "admin.started");
    }

    private void handleStop(CommandSender sender) {
        if (!checkPerm(sender, "lunamilitarycomplex.admin")) return;
        plugin.getEventManager().stopEvent();
        sendMessageList(sender, "admin.stopped");
    }

    private void handleStage(CommandSender sender, String[] args) {
        if (!checkPerm(sender, "lunamilitarycomplex.admin")) return;
        if (args.length < 2) {
            sender.sendMessage(HexUtil.color("&c/lmc stage <preparing|open|assault|boss|capture|rewards|finished>"));
            return;
        }
        try {
            MilitaryEvent stage = MilitaryEvent.valueOf(args[1].toUpperCase());
            plugin.getEventManager().jumpToPhase(stage);
            sendMessageList(sender, "general.stage-updated", "%stage%", stage.name());
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(HexUtil.color("&cНеизвестная фаза."));
        }
    }

    private void handleNext(CommandSender sender) {
        if (!checkPerm(sender, "lunamilitarycomplex.admin")) return;
        long next = plugin.getEventManager().getNextOpenTime();
        if (next > 0) {
            long diff = next - System.currentTimeMillis();
            sender.sendMessage(HexUtil.color("&aСледующее открытие через: &f" + HexUtil.formatDuration(diff / 1000) + "&a."));
        } else {
            sender.sendMessage(HexUtil.color("&cРасписание не установлено."));
        }
    }

    private void handleStatus(CommandSender sender) {
        MilitaryEvent state = plugin.getStateManager().getState();
        sender.sendMessage(HexUtil.color("&d&lСтатус комплекса:"));
        sender.sendMessage(HexUtil.color(" &7Состояние: &f" + state.name()));
        sender.sendMessage(HexUtil.color(" &7Фаза: &f" + plugin.getEventManager().getCurrentPhase().name()));
        sender.sendMessage(HexUtil.color(" &7Таймер: &f" + HexUtil.formatDuration(plugin.getEventManager().getTimer())));
        sender.sendMessage(HexUtil.color(" &7Волна: &f" + plugin.getEventManager().getWave()));
        sender.sendMessage(HexUtil.color(" &7Зон: &f" + plugin.getZoneManager().getAllZones().size()));
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!checkPerm(sender, "lunamilitarycomplex.admin")) return;
        if (args.length < 2) { sender.sendMessage(HexUtil.color("&c/lmc give <component|module|cell|core|strikecode|shield> [player]")); return; }
        Player target = (sender instanceof Player) ? (Player)sender : null;
        if (args.length >= 3) target = Bukkit.getPlayer(args[2]);
        if (target == null) { sender.sendMessage(HexUtil.color("&cИгрок не найден.")); return; }
        ItemStack item = switch(args[1].toLowerCase()) {
            case "component" -> plugin.getItemManager().getMilitaryComponent();
            case "module" -> plugin.getItemManager().getNavModule();
            case "cell" -> plugin.getItemManager().getEnergyCell();
            case "core" -> plugin.getItemManager().getLaunchCore();
            case "strikecode" -> plugin.getItemManager().getStrikeCode();
            case "shield" -> plugin.getItemManager().getShieldModule();
            default -> null;
        };
        if (item != null) { target.getInventory().addItem(item); sender.sendMessage(HexUtil.color("&aВыдано: &f" + args[1])); }
        else sender.sendMessage(HexUtil.color("&cНеизвестный предмет."));
    }

    private void handleBoss(CommandSender sender, String[] args) {
        if (!checkPerm(sender, "lunamilitarycomplex.admin")) return;
        if (!(sender instanceof Player)) return;
        if (args.length < 2) { sender.sendMessage(HexUtil.color("&c/lmc boss spawn")); return; }
        if (args[1].equalsIgnoreCase("spawn")) {
            plugin.getBossManager().spawnBoss(((Player)sender).getLocation());
            sender.sendMessage(HexUtil.color("&aБосс заспавнен."));
        }
    }

    private void handleStrike(CommandSender sender, String[] args) {
        if (!checkPerm(sender, "lunamilitarycomplex.strike")) return;
        if (!(sender instanceof Player)) return;
        Player p = (Player) sender;
        if (args.length < 5) { sender.sendMessage(HexUtil.color("&c/lmc strike <x> <y> <z> <world>")); return; }
        try {
            double x = Double.parseDouble(args[1]);
            double y = Double.parseDouble(args[2]);
            double z = Double.parseDouble(args[3]);
            org.bukkit.World w = Bukkit.getWorld(args[4]);
            if (w == null) { p.sendMessage(HexUtil.color("&cМир не найден.")); return; }
            Location target = new Location(w, x, y, z);
            plugin.getStrikeManager().requestStrike(p, target);
        } catch (Exception e) { sender.sendMessage(HexUtil.color("&c/lmc strike <x> <y> <z> <world>")); }
    }

    private void handleShield(CommandSender sender, String[] args) {
        if (!checkPerm(sender, "lunamilitarycomplex.admin")) return;
        if (args.length < 2) { sender.sendMessage(HexUtil.color("&c/lmc shield list | remove <id>")); return; }
        if (args[1].equalsIgnoreCase("list")) {
            plugin.getShieldModuleManager().getAllModules().forEach(m ->
                sender.sendMessage(HexUtil.color("&7" + m.getId() + " | &f" + org.bukkit.Bukkit.getOfflinePlayer(m.getOwner()).getName() + " | &e" + (int)m.getEnergyPercent() + "%")));
        } else if (args[1].equalsIgnoreCase("remove") && args.length >= 3) {
            plugin.getShieldModuleManager().removeModule(args[2]);
            sender.sendMessage(HexUtil.color("&aМодуль удалён."));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!checkPerm(sender, "lunamilitarycomplex.reload")) return;
        plugin.reloadPlugin();
        sendMessageList(sender, "general.reload-success");
    }

    private void handleDebug(CommandSender sender) {
        if (!checkPerm(sender, "lunamilitarycomplex.admin")) return;
        boolean debug = !plugin.getConfig().getBoolean("debug", false);
        plugin.getConfig().set("debug", debug);
        plugin.saveConfig();
        sender.sendMessage(HexUtil.color("&aDebug: &f" + debug));
    }

    private void giveWand(Player p) {
        Material mat = Material.getMaterial(plugin.getConfigManager().getWandMaterial());
        if (mat == null) mat = Material.BLAZE_ROD;
        ItemStack wand = new ItemStack(mat);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getWandName());
            List<String> lore = plugin.getConfigManager().getWandLore();
            if (!lore.isEmpty()) meta.setLore(lore);
            wand.setItemMeta(meta);
        }
        p.getInventory().addItem(wand);
        sendMessageList(p, "zone.wand.given");
    }

    private void createZone(Player p, String[] args) {
        Selection s = plugin.getSelectionManager().getSelection(p);
        if (!s.hasBothPositions()) { sendMessageList(p, "zone.create.no-selection"); return; }
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) { if (i > 2) sb.append(" "); sb.append(args[i]); }
        String name = sb.toString();
        if (plugin.getZoneManager().zoneExists(name)) { sendMessageList(p, "zone.create.already-exists", "%zone%", name); return; }
        plugin.getZoneManager().createZone(name, s.getPos1(), s.getPos2());
        plugin.getSelectionManager().removeSelection(p);
        Zone created = plugin.getZoneManager().getZone(name);
        if (created != null && plugin.getConfig().getBoolean("terminal.auto-place", true)) {
            plugin.getTerminalManager().placeTerminalBlock(created);
        }
        sendMessageList(p, "zone.create.success", "%zone%", name, "%sizex%", String.valueOf(s.getSizeX()), "%sizey%", String.valueOf(s.getSizeY()), "%sizez%", String.valueOf(s.getSizeZ()), "%volume%", String.valueOf(s.getVolume()));
    }

    private void deleteZone(Player p, String name) {
        if (!plugin.getZoneManager().zoneExists(name)) { sendMessageList(p, "zone.not-found", "%zone%", name); return; }
        plugin.getZoneManager().deleteZone(name);
        sendMessageList(p, "zone.delete.success", "%zone%", name);
    }

    private void toggleZone(Player p, String name) {
        if (!plugin.getZoneManager().zoneExists(name)) { sendMessageList(p, "zone.not-found", "%zone%", name); return; }
        plugin.getZoneManager().toggleZone(name);
        Zone z = plugin.getZoneManager().getZone(name);
        String status = z.isEnabled() ? "&aвключена" : "&cвыключена";
        sendMessageList(p, "zone.toggle.success", "%zone%", name, "%status%", status);
    }

    private void listZones(Player p) {
        List<Zone> zones = new ArrayList<>(plugin.getZoneManager().getAllZones());
        if (zones.isEmpty()) { sendMessageList(p, "zone.list.empty"); return; }
        sendMessageList(p, "zone.list.header", "%count%", String.valueOf(zones.size()));
        for (Zone z : zones) p.sendMessage(HexUtil.color(" &7- &f" + z.getName() + " " + (z.isEnabled() ? "&a[ВКЛ]" : "&c[ВЫКЛ]")));
    }

    private void zoneInfo(Player p, String name) {
        Zone z = plugin.getZoneManager().getZone(name);
        if (z == null) { sendMessageList(p, "zone.not-found", "%zone%", name); return; }
        sendMessageList(p, "zone.info.header", "%zone%", z.getName());
        p.sendMessage(HexUtil.color(" &7Мир: &f" + plugin.getConfigManager().getDisplayWorld(z.getWorldName()) + " &7(" + z.getWorldName() + ")"));
        p.sendMessage(HexUtil.color(" &7Координаты: &f" + z.getMinX() + ", " + z.getMinY() + ", " + z.getMinZ() + " &7-> &f" + z.getMaxX() + ", " + z.getMaxY() + ", " + z.getMaxZ()));
        p.sendMessage(HexUtil.color(" &7Статус: " + (z.isEnabled() ? "&aВключена" : "&cВыключена")));
        if (z.getTerminalBlock() != null) {
            Location tb = z.getTerminalBlock();
            p.sendMessage(HexUtil.color(" &7Терминал: &f" + tb.getBlockX() + ", " + tb.getBlockY() + ", " + tb.getBlockZ()));
        }
    }

    private boolean checkPerm(CommandSender s, String perm) {
        if (!s.hasPermission(perm)) { sendMessage(s, "general.no-permission"); return false; }
        return true;
    }

    private void sendHelp(CommandSender s) { sendMessageList(s, "general.help"); }
    private void sendMessage(CommandSender s, String path) {
        String msg = plugin.getConfigManager().getMessage(path);
        if (!msg.isEmpty()) s.sendMessage(msg);
    }
    private void sendMessageList(CommandSender s, String path, String... placeholders) {
        for (String msg : plugin.getConfigManager().getMessageList(path, placeholders)) {
            if (!msg.isEmpty()) s.sendMessage(msg);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("zone","start","stop","stage","next","status","give","boss","strike","shield","reload","debug","help"));
        } else if (args.length == 2) {
            switch(args[0].toLowerCase()) {
                case "zone": completions.addAll(Arrays.asList("wand","effect","create","delete","toggle","list","info","terminal","highlight")); break;
                case "give": completions.addAll(Arrays.asList("component","module","cell","core","strikecode","shield")); break;
                case "boss": completions.add("spawn"); break;
                case "shield": completions.addAll(Arrays.asList("list","remove")); break;
                case "stage": completions.addAll(Arrays.asList("preparing","open","assault","boss","capture","rewards","finished")); break;
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("zone")) {
            String action = args[1].toLowerCase();
            if (action.equals("delete") || action.equals("toggle") || action.equals("info") || action.equals("highlight")) {
                completions.addAll(plugin.getZoneManager().getAllZones().stream().map(Zone::getName).collect(Collectors.toList()));
            }
        }
        return completions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length-1].toLowerCase())).collect(Collectors.toList());
    }
}
