package akm.mrlavx.lunaMilitaryComplex.Models;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class Zone {
    private String name, worldName;
    private int x1,y1,z1,x2,y2,z2;
    private boolean enabled;
    private String enterMsg, leaveMsg, barColor, barStyle;
    private Location terminalBlock;

    public Zone(String name, World w, int x1,int y1,int z1,int x2,int y2,int z2) {
        this.name=name; this.worldName=w.getName();
        this.x1=x1;this.y1=y1;this.z1=z1;this.x2=x2;this.y2=y2;this.z2=z2;
        this.enabled=true; this.barColor="WHITE"; this.barStyle="SOLID";
    }
    public Zone(ConfigurationSection s) {
        name=s.getString("name","Unknown"); worldName=s.getString("world","");
        x1=s.getInt("x1");y1=s.getInt("y1");z1=s.getInt("z1");
        x2=s.getInt("x2");y2=s.getInt("y2");z2=s.getInt("z2");
        enabled=s.getBoolean("enabled",true);
        enterMsg=s.getString("enter-message",null);
        leaveMsg=s.getString("leave-message",null);
        barColor=s.getString("bar-color","WHITE");
        barStyle=s.getString("bar-style","SOLID");
        if (s.contains("terminal")) {
            org.bukkit.World w = org.bukkit.Bukkit.getWorld(worldName);
            if (w != null) {
                terminalBlock = new Location(w, s.getDouble("terminal.x"), s.getDouble("terminal.y"), s.getDouble("terminal.z"));
            }
        }
    }
    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null || !loc.getWorld().getName().equals(worldName)) return false;
        int x=loc.getBlockX(), y=loc.getBlockY(), z=loc.getBlockZ();
        return x>=Math.min(x1,x2)&&x<=Math.max(x1,x2)&&y>=Math.min(y1,y2)&&y<=Math.max(y1,y2)&&z>=Math.min(z1,z2)&&z<=Math.max(z1,z2);
    }
    public void save(ConfigurationSection s) {
        s.set("name",name); s.set("world",worldName);
        s.set("x1",x1);s.set("y1",y1);s.set("z1",z1);
        s.set("x2",x2);s.set("y2",y2);s.set("z2",z2);
        s.set("enabled",enabled);
        if (enterMsg!=null) s.set("enter-message",enterMsg);
        if (leaveMsg!=null) s.set("leave-message",leaveMsg);
        s.set("bar-color",barColor); s.set("bar-style",barStyle);
        if (terminalBlock != null) {
            s.set("terminal.x", terminalBlock.getX());
            s.set("terminal.y", terminalBlock.getY());
            s.set("terminal.z", terminalBlock.getZ());
        }
    }
    public String getName(){return name;} public void setName(String n){name=n;}
    public String getWorldName(){return worldName;}
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public String getEnterMessage(){return enterMsg;} public void setEnterMessage(String v){enterMsg=v;}
    public String getLeaveMessage(){return leaveMsg;} public void setLeaveMessage(String v){leaveMsg=v;}
    public String getBarColor(){return barColor;} public void setBarColor(String v){barColor=v;}
    public String getBarStyle(){return barStyle;} public void setBarStyle(String v){barStyle=v;}
    public int getMinX(){return Math.min(x1,x2);} public int getMinY(){return Math.min(y1,y2);} public int getMinZ(){return Math.min(z1,z2);}
    public int getMaxX(){return Math.max(x1,x2);} public int getMaxY(){return Math.max(y1,y2);} public int getMaxZ(){return Math.max(z1,z2);}
    public Location getTerminalBlock(){return terminalBlock;} public void setTerminalBlock(Location l){terminalBlock=l;}
}
