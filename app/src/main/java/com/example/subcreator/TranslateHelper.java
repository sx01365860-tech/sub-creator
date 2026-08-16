package com.example.subcreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslateHelper {
    private static final Map<String, String> DICT = new HashMap<>();
    private static final List<String> SORTED_KEYS = new ArrayList<>();

    static {
        // 1. Chào hỏi & Giao tiếp
        DICT.put("你好", "xin chào");
        DICT.put("您好", "chào ông/bà");
        DICT.put("谢谢", "cảm ơn");
        DICT.put("不客气", "không có gì");
        DICT.put("对不起", "xin lỗi");
        DICT.put("没关系", "không sao đâu");
        DICT.put("再见", "tạm biệt");
        DICT.put("早上好", "chào buổi sáng");
        DICT.put("晚安", "chúc ngủ ngon");
        DICT.put("欢迎", "hoan nghênh");

        // 2. Đại từ nhân xưng & Xưng hô
        DICT.put("我", "tôi");
        DICT.put("你", "bạn");
        DICT.put("他", "anh ấy");
        DICT.put("她", "cô ấy");
        DICT.put("它", "nó");
        DICT.put("我们", "chúng tôi");
        DICT.put("你们", "các bạn");
        DICT.put("他们", "họ");
        DICT.put("大家", "mọi người");
        DICT.put("先生", "ông");
        DICT.put("女士", "bà");
        DICT.put("老师", "thầy/cô giáo");
        DICT.put("学生", "học sinh");
        DICT.put("朋友", "bạn bè");

        // 3. Phó từ, Thời gian & Ngữ pháp
        DICT.put("今天", "hôm nay");
        DICT.put("明天", "ngày mai");
        DICT.put("昨天", "hôm qua");
        DICT.put("现在", "bây giờ");
        DICT.put("刚才", "vừa mới");
        DICT.put("已经", "đã");
        DICT.put("正在", "đang");
        DICT.put("准备", "chuẩn bị");
        DICT.put("打算", "dự định");
        DICT.put("总是", "luôn luôn");
        DICT.put("常常", "thường xuyên");
        DICT.put("一起", "cùng nhau");
        DICT.put("非常", "rất");
        DICT.put("特别", "đặc biệt");
        DICT.put("太", "quá");
        DICT.put("最", "nhất");
        DICT.put("不", "không");
        DICT.put("没", "chưa/không");
        DICT.put("没有", "không có");
        DICT.put("也", "cũng");
        DICT.put("都", "đều");
        DICT.put("就", "thì/chính");
        DICT.put("还", "vẫn/còn");

        // 4. Động từ thông dụng
        DICT.put("是", "là");
        DICT.put("有", "có");
        DICT.put("去", "đi");
        DICT.put("来", "đến");
        DICT.put("吃", "ăn");
        DICT.put("喝", "uống");
        DICT.put("看", "xem/nhìn");
        DICT.put("听", "nghe");
        DICT.put("说", "nói");
        DICT.put("读", "đọc");
        DICT.put("写", "viết");
        DICT.put("想", "muốn/nghĩ");
        DICT.put("要", "cần/muốn");
        DICT.put("喜欢", "thích");
        DICT.put("爱", "yêu");
        DICT.put("知道", "biết");
        DICT.put("认识", "quen biết");
        DICT.put("明白", "hiểu");
        DICT.put("懂", "hiểu");
        DICT.put("工作", "làm việc");
        DICT.put("学习", "học tập");
        DICT.put("休息", "nghỉ ngơi");

        // 5. Danh từ & Địa điểm
        DICT.put("中国", "Trung Quốc");
        DICT.put("越南", "Việt Nam");
        DICT.put("北京", "Bắc Kinh");
        DICT.put("上海", "Thượng Hải");
        DICT.put("家", "nhà");
        DICT.put("学校", "trường học");
        DICT.put("公司", "công ty");
        DICT.put("医院", "bệnh viện");
        DICT.put("手机", "điện thoại");
        DICT.put("电脑", "máy tính");
        DICT.put("钱", "tiền");
        DICT.put("时间", "thời gian");
        DICT.put("名字", "tên");
        DICT.put("什么", "cái gì");
        DICT.put("谁", "ai");
        DICT.put("哪儿", "đâu/ở đâu");
        DICT.put("为什么", "tại sao");
        DICT.put("怎么", "làm sao");

        // 6. Trợ từ & Cấu trúc ngữ pháp
        DICT.put("的", "của");
        DICT.put("了", "rồi");
        DICT.put("过", "qua/rồi");
        DICT.put("着", "đang");
        DICT.put("吗", "không");
        DICT.put("呢", "nhỉ/sao");
        DICT.put("吧", "đi/nhé");

        SORTED_KEYS.addAll(DICT.keySet());
        // Khớp từ ghép dài trước từ đơn sau để đảm bảo độ chính xác
        Collections.sort(SORTED_KEYS, (a, b) -> Integer.compare(b.length(), a.length()));
    }

    public static String translateCnToVn(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String result = text;

        for (String key : SORTED_KEYS) {
            if (result.contains(key)) {
                result = result.replace(key, " " + DICT.get(key) + " ");
            }
        }

        if (result.equals(text)) {
            return "[Dịch]: " + text;
        }

        return result.replaceAll("\\s+", " ").trim();
    }
}
