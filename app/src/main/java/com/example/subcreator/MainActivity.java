package com.example.subcreator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.StorageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int PICK_FILE_REQUEST = 1;
    private List<SubtitleItem> subtitleList = new ArrayList<>();
    private LinearLayout subContainer;
    private TextView statusText;
    private Model voskModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(32, 32, 32, 32);

        TextView titleText = new TextView(this);
        titleText.setText("SubCreator - Trình Tạo Phụ Đề Offline");
        titleText.setTextSize(22);
        titleText.setPadding(0, 0, 0, 24);
        rootLayout.addView(titleText);

        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button btnSelectFile = new Button(this);
        btnSelectFile.setText("Chọn File Âm Thanh/Video");
        btnSelectFile.setOnClickListener(v -> openFilePicker());

        Button btnExportSrt = new Button(this);
        btnExportSrt.setText("Xuất File .SRT");
        btnExportSrt.setOnClickListener(v -> exportSrtFile());

        btnLayout.addView(btnSelectFile);
        btnLayout.addView(btnExportSrt);
        rootLayout.addView(btnLayout);

        statusText = new TextView(this);
        statusText.setText("Đang tải Mô hình AI Tiếng Trung Offline...");
        statusText.setPadding(0, 16, 0, 16);
        rootLayout.addView(statusText);

        ScrollView scrollView = new ScrollView(this);
        subContainer = new LinearLayout(this);
        subContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(subContainer);

        rootLayout.addView(scrollView);
        setContentView(rootLayout);

        initOfflineModel();
    }

    private void initOfflineModel() {
        StorageService.unpack(this, "model-cn", "model-cn",
            model -> {
                voskModel = model;
                statusText.setText("Trạng thái: Mô hình AI Offline đã sẵn sàng!");
            },
            exception -> statusText.setText("Lỗi tải Mô hình AI: " + exception.getMessage())
        );
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
            statusText.setText("Đang xử lý âm thanh...");
            processAudioFile(fileUri);
        }
    }

    private void processAudioFile(Uri uri) {
        if (voskModel == null) {
            Toast.makeText(this, "Mô hình AI chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Recognizer recognizer = new Recognizer(voskModel, 16000.0f);
                byte[] buffer = new byte[4096];
                int nbytes;
                subtitleList.clear();
                int id = 1;

                while ((nbytes = is.read(buffer)) >= 0) {
                    if (recognizer.acceptWaveForm(buffer, nbytes)) {
                        String resultJson = recognizer.getResult();
                        if (resultJson.contains("\"text\" : \"")) {
                            String text = extractTextFromJson(resultJson);
                            if (!text.trim().isEmpty()) {
                                subtitleList.add(new SubtitleItem(id++, "00:00:00,000", "00:00:05,000", text, "[Dịch AI]: " + text));
                            }
                        }
                    }
                }
                is.close();
                recognizer.close();

                runOnUiThread(() -> {
                    statusText.setText("Hoàn tất! Tìm thấy " + subtitleList.size() + " câu phụ đề.");
                    renderSubEditor();
                });

            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("Lỗi xử lý file: " + e.getMessage()));
            }
        }).start();
    }

    private String extractTextFromJson(String json) {
        try {
            int start = json.indexOf("\"text\" : \"") + 10;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }

    private void renderSubEditor() {
        subContainer.removeAllViews();
        for (SubtitleItem item : subtitleList) {
            LinearLayout itemBox = new LinearLayout(this);
            itemBox.setOrientation(LinearLayout.VERTICAL);
            itemBox.setPadding(16, 16, 16, 16);

            TextView origView = new TextView(this);
            origView.setText("Gốc (Trung): " + item.getOriginalText());
            origView.setTextSize(14);

            TextView transView = new TextView(this);
            transView.setText("Dịch (Việt): " + item.getTranslatedText());
            transView.setTextSize(15);

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
