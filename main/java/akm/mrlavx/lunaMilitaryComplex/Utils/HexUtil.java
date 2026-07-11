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

    public static String replacePlaceholders(String text, String... placeholders) {
        if (text == null || text.isEmpty() || placeholders == null) {
            return color(text);
        }
        String result = text;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            result = result.replace(placeholders[i], placeholders[i + 1]);
        }
        return color(result);
    }

    public static List<String> replacePlaceholders(List<String> lines, String... placeholders) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            result.add(replacePlaceholders(line, placeholders));
        }
        return result;
    }

    public static String formatDuration(long seconds) {
        if (seconds < 0) seconds = 0;
        if (seconds < 60) {
            return seconds + " " + plural(seconds, "секунда", "секунды", "секунд");
        }
        if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " " + plural(minutes, "минута", "минуты", "минут");
        }
        if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " " + plural(hours, "час", "часа", "часов");
        }
        long days = seconds / 86400;
        return days + " " + plural(days, "день", "дня", "дней");
    }

    private static String plural(long value, String one, String few, String many) {
        long mod100 = value % 100;
        long mod10 = value % 10;
        if (mod100 >= 11 && mod100 <= 14) return many;
        if (mod10 == 1) return one;
        if (mod10 >= 2 && mod10 <= 4) return few;
        return many;
    }
}
