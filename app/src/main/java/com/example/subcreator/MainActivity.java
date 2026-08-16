package com.example.subcreator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int PICK_FILE_REQUEST = 1;
    private List<SubtitleItem> subtitleList = new ArrayList<>();
    private LinearLayout subContainer;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(32, 32, 32, 32);

        // Header Title
        TextView titleText = new TextView(this);
        titleText.setText("SubCreator - Trình Tạo & Chỉnh Sửa Phụ Đề");
        titleText.setTextSize(22);
        titleText.setPadding(0, 0, 0, 24);
        rootLayout.addView(titleText);

        // Control Buttons
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button btnSelectFile = new Button(this);
        btnSelectFile.setText("Chọn Video/Audio");
        btnSelectFile.setOnClickListener(v -> openFilePicker());

        Button btnExportSrt = new Button(this);
        btnExportSrt.setText("Xuất File .SRT");
        btnExportSrt.setOnClickListener(v -> exportSrtFile());

        btnLayout.addView(btnSelectFile);
        btnLayout.addView(btnExportSrt);
        rootLayout.addView(btnLayout);

        // Status Text
        statusText = new TextView(this);
        statusText.setText("Trạng thái: Sẵn sàng chọn file...");
        statusText.setPadding(0, 16, 0, 16);
        rootLayout.addView(statusText);

        // Subtitle Editor Container (Scrollable)
        ScrollView scrollView = new ScrollView(this);
        subContainer = new LinearLayout(this);
        subContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(subContainer);

        rootLayout.addView(scrollView);
        setContentView(rootLayout);

        // Mock dữ liệu mẫu để thử nghiệm giao diện Sub Editor song ngữ
        loadMockSubtitles();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            statusText.setText("Đã chọn file: " + fileUri.getPath() + "\nĐang trích xuất âm thanh & tạo phụ đề...");
            // TODO: Tích hợp FFmpeg trích xuất audio & Sherpa-onnx / Whisper nhận diện giọng nói
        }
    }

    private void loadMockSubtitles() {
        subtitleList.clear();
        subtitleList.add(new SubtitleItem(1, "00:00:01,000", "00:00:03,500", "你好，欢迎使用 SubCreator！", "Xin chào, chào mừng bạn sử dụng SubCreator!"));
        subtitleList.add(new SubtitleItem(2, "00:00:04,000", "00:00:07,200", "这是一个离线字幕识别与AI翻译工具。", "Đây là công cụ nhận diện phụ đề offline và dịch thuật AI."));
        renderSubEditor();
    }

    private void renderSubEditor() {
        subContainer.removeAllViews();
        for (SubtitleItem item : subtitleList) {
            LinearLayout itemBox = new LinearLayout(this);
            itemBox.setOrientation(LinearLayout.VERTICAL);
            itemBox.setPadding(16, 16, 16, 16);

            TextView timeView = new TextView(this);
            timeView.setText("[" + item.getStartTime() + " -> " + item.getEndTime() + "]");
            timeView.setTextSize(12);

            TextView origView = new TextView(this);
            origView.setText("Gốc (Trung): " + item.getOriginalText());
            origView.setTextSize(14);

            TextView transView = new TextView(this);
            transView.setText("Dịch (Việt): " + item.getTranslatedText());
            transView.setTextSize(15);

            itemBox.addView(timeView);
            itemBox.addView(origView);
            itemBox.addView(transView);

            subContainer.addView(itemBox);
        }
    }

    private void exportSrtFile() {
        if (subtitleList.isEmpty()) {
            Toast.makeText(this, "Không có phụ đề để xuất!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder srtContent = new StringBuilder();
        for (SubtitleItem item : subtitleList) {
            srtContent.append(item.toSrtFormat());
        }

        try {
            File path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(path, "subcreator_output.srt");
            FileOutputStream stream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(srtContent.toString());
            writer.close();
            stream.close();

            statusText.setText("Xuất file thành công: " + file.getAbsolutePath());
            Toast.makeText(this, "Đã xuất file .srt thành công!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
