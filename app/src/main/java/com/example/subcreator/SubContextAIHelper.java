package com.example.subcreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubContextAIHelper {

    private static final Map<String, String> ADVANCED_PATTERNS = new HashMap<>();

    static {
        ADVANCED_PATTERNS.put("tôi là xin chào", "Xin chào, tôi là");
        ADVANCED_PATTERNS.put("cái gì tên", "Tên là gì");
        ADVANCED_PATTERNS.put("không có không", "Không có đâu");
        ADVANCED_PATTERNS.put("ở đâu làm việc", "Làm việc ở đâu");
        ADVANCED_PATTERNS.put("đi đâu đi", "Đi đâu thế");
        ADVANCED_PATTERNS.put("bạn là ai người", "Bạn là người ở đâu");
    }

    public static void reviewAndRefine(List<SubtitleItem> items) {
        if (items == null || items.isEmpty()) return;

        for (int i = 0; i < items.size(); i++) {
            SubtitleItem current = items.get(i);
            String text = current.getTranslatedText();
            if (text == null || text.trim().isEmpty()) continue;

            text = text.trim().replaceAll("\\s+", " ");
            text = fixPossessiveStructure(text);

            String lowerText = text.toLowerCase();
            for (Map.Entry<String, String> entry : ADVANCED_PATTERNS.entrySet()) {
                if (lowerText.contains(entry.getKey())) {
                    text = text.replaceAll("(?i)" + entry.getKey(), entry.getValue());
                }
            }

            if (text.length() > 0) {
                text = Character.toUpperCase(text.charAt(0)) + (text.length() > 1 ? text.substring(1) : "");
            }

            if (text.toLowerCase().contains("gì") || text.toLowerCase().contains("ai") || 
                text.toLowerCase().contains("sao") || text.toLowerCase().contains("đâu") || 
                text.toLowerCase().contains("không") || text.toLowerCase().contains("mấy")) {
                if (!text.endsWith("?") && !text.endsWith(".") && !text.endsWith("!")) {
                    text = text + "?";
                }
            } else if (!text.endsWith(".") && !text.endsWith("!") && !text.endsWith("?")) {
                text = text + ".";
            }

            current.setTranslatedText(text);
        }
    }

    private static String fixPossessiveStructure(String input) {
        if (input.contains(" của ")) {
            String[] parts = input.split(" của ");
            if (parts.length == 2 && parts[0].length() < 10 && parts[1].length() < 10) {
                return parts[1] + " của " + parts[0];
            }
        }
        return input;
    }
}
