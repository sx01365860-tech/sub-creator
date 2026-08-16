package com.example.subcreator;

import java.util.HashMap;
import java.util.Map;

public class TranslateHelper {
    private static final Map<String, String> DICT = new HashMap<>();

    static {
        DICT.put("你好", "Xin chào");
        DICT.put("谢谢", "Cảm ơn");
        DICT.put("再见", "Tạm biệt");
        DICT.put("欢迎", "Hoan nghênh");
        DICT.put("中国", "Trung Quốc");
        DICT.put("越南", "Việt Nam");
        DICT.put("我是", "Tôi là");
        DICT.put("什么", "Cái gì");
        DICT.put("名字", "Tên");
        DICT.put("朋友", "Bạn bè");
        DICT.put("学习", "Học tập");
        DICT.put("工作", "Làm việc");
        DICT.put("好", "Tốt");
        DICT.put("大", "Lớn");
        DICT.put("小", "Nhỏ");
        DICT.put("多", "Nhiều");
        DICT.put("少", "Ít");
        DICT.put("爱", "Yêu");
        DICT.put("喜欢", "Thích");
        DICT.put("不", "Không");
        DICT.put("是", "Là");
        DICT.put("有", "Có");
        DICT.put("没有", "Không có");
        DICT.put("在", "Ở");
        DICT.put("去", "Đi");
        DICT.put("来", "Đến");
        DICT.put("吃", "Ăn");
        DICT.put("喝", "Uống");
        DICT.put("看", "Xem");
        DICT.put("听", "Nghe");
        DICT.put("说", "Nói");
        DICT.put("写", "Viết");
        DICT.put("读", "Đọc");
        DICT.put("今天", "Hôm nay");
        DICT.put("明天", "Ngày mai");
        DICT.put("昨天", "Hôm qua");
    }

    public static String translateCnToVn(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String result = text;
        for (Map.Entry<String, String> entry : DICT.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue() + " ");
        }
        if (result.equals(text)) {
            return "[Dịch]: " + text;
        }
        return result.replaceAll("\\s+", " ").trim();
    }
}
