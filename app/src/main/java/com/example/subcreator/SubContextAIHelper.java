package com.example.subcreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubContextAIHelper {

    private static final Map<String, String> CONTEXT_PATTERNS = new HashMap<>();

    static {
        // Tối ưu hóa ngữ cảnh dịch thô sang Tiếng Việt chuẩn
        CONTEXT_PATTERNS.put("Tôi là Xin chào", "Xin chào, tôi là");
        CONTEXT_PATTERNS.put("Xin chào Tôi là", "Xin chào, tôi là");
        CONTEXT_PATTERNS.put("Cái gì Tên", "Tên là gì");
        CONTEXT_PATTERNS.put("Tôi Yêu", "Tôi rất yêu");
        CONTEXT_PATTERNS.put("Không Có", "Không có");
        CONTEXT_PATTERNS.put("Ở Đi", "Đang đi đến");
        CONTEXT_PATTERNS.put("Học tập Làm việc", "Học tập và làm việc");
    }

    public static void reviewAndRefine(List<SubtitleItem> items) {
        if (items == null || items.isEmpty()) return;

        for (int i = 0; i < items.size(); i++) {
            SubtitleItem current = items.get(i);
            String prevText = (i > 0) ? items.get(i - 1).getTranslatedText() : "";

            String text = current.getTranslatedText();
            if (text == null || text.trim().isEmpty()) continue;

            // 1. Chuẩn hóa khoảng trắng
            text = text.trim().replaceAll("\\s+", " ");

            // 2. Thay thế các cụm từ theo ngữ cảnh
            for (Map.Entry<String, String> entry : CONTEXT_PATTERNS.entrySet()) {
                if (text.contains(entry.getKey())) {
                    text = text.replace(entry.getKey(), entry.getValue());
                }
            }

            // 3. Xử lý viết hoa/viết thường dựa theo câu phía trước
            if (prevText != null && !prevText.isEmpty() && (prevText.endsWith(",") || prevText.toLowerCase().contains("vì vậy") || prevText.toLowerCase().contains("tuy nhiên"))) {
                text = Character.toLowerCase(text.charAt(0)) + (text.length() > 1 ? text.substring(1) : "");
            } else {
                text = Character.toUpperCase(text.charAt(0)) + (text.length() > 1 ? text.substring(1) : "");
            }

            // 4. Nhận diện ngữ cảnh câu hỏi hoặc câu cảm thán
            if (text.contains("gì") || text.contains("sao") || text.contains("đâu") || text.contains("nào") || text.contains("mấy") || text.contains("ai")) {
                if (!text.endsWith("?") && !text.endsWith(".") && !text.endsWith("!")) {
                    text = text + "?";
                }
            } else if (!text.endsWith(".") && !text.endsWith("!") && !text.endsWith("?")) {
                text = text + ".";
            }

            current.setTranslatedText(text);
        }
    }
}
