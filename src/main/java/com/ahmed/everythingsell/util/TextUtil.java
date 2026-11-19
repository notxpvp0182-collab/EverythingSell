package com.ahmed.everythingsell.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&?#([0-9A-F]{6})");
    private static final char[] SMALL_CAPS_MAP = {
            'ᴀ', 'ʙ', 'ᴄ', 'ᴅ', 'ᴇ', 'ғ', 'ɢ', 'ʜ', 'ɪ', 'ᴊ', 'ᴋ', 'ʟ', 'ᴍ',
            'ɴ', 'ᴏ', 'ᴘ', 'ǫ', 'ʀ', 's', 'ᴛ', 'ᴜ', 'ᴠ', 'ᴡ', 'x', 'ʏ', 'ᴢ'
    };

    private TextUtil() {
    }

    public static String colorize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder buffer = new StringBuilder(input.length() + 32);

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (int i = 0; i < hex.length(); i++) {
                replacement.append('§').append(hex.charAt(i));
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static List<String> colorizeList(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(colorize(line));
        }
        return result;
    }

    public static String toSmallCaps(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(input.length());
        boolean inPlaceholder = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '{') {
                inPlaceholder = true;
                result.append(c);
                continue;
            }
            if (c == '}') {
                inPlaceholder = false;
                result.append(c);
                continue;
            }
            if (!inPlaceholder && (c == '&' || c == '§') && i + 1 < input.length()) {
                result.append(c).append(input.charAt(++i));
                continue;
            }
            if (!inPlaceholder && c >= 'a' && c <= 'z') {
                result.append(SMALL_CAPS_MAP[c - 'a']);
            } else if (!inPlaceholder && c >= 'A' && c <= 'Z') {
                result.append(SMALL_CAPS_MAP[c - 'A']);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
