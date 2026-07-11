package akm.mrlavx.lunaMilitaryComplex.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public final class HexUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private HexUtil() {}

    public static String color(String msg) {
        if (msg == null || msg.isBlank()) return "";
        Matcher m = HEX_PATTERN.matcher(msg);
        StringBuffer b = new StringBuffer();
        while (m.find()) {
            String hex = m.group(1), repl;
            try { repl = ChatColor.of("#" + hex).toString(); } catch (IllegalArgumentException e) { repl = "&#" + hex; }
            m.appendReplacement(b, Matcher.quoteReplacement(repl));
        }
        m.appendTail(b);
        return ChatColor.translateAlternateColorCodes('&', b.toString());
    }

    public static List<String> color(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            result.add(color(line));
        }
        return result;
    }
}
