package com.example.subcreator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
    private Model voskModelCn;
    private Model voskModelVn;
    private Spinner langSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(32, 32, 32, 32);

        TextView titleText = new TextView(this);
        titleText.setText("SubCreator - Trình Tạo Phụ Đề Offline");
        titleText.setTextSize(22);
        titleText.setPadding(0, 0, 0, 16);
        rootLayout.addView(titleText);

        // Khung chọn ngôn ngữ nhận diện
        LinearLayout langLayout = new LinearLayout(this);
        langLayout.setOrientation(LinearLayout.HORIZONTAL);
        langLayout.setPadding(0, 0, 0, 16);

        TextView langLabel = new TextView(this);
        langLabel.setText("Chọn ngôn ngữ âm thanh: ");
        langLabel.setTextSize(16);
        langLayout.addView(langLabel);

        langSpinner = new Spinner(this);
        String[] languages = {"Tiếng Trung (Mandarin)", "Tiếng Việt (Vietnamese)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        langSpinner.setAdapter(adapter);
        langLayout.addView(langSpinner);

        rootLayout.addView(langLayout);

        // Khung nút bấm
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
        statusText.setText("Đang khởi tạo các Mô hình AI Offline (Trung & Việt)...");
        statusText.setPadding(0, 16, 0, 16);
        rootLayout.addView(statusText);

        ScrollView scrollView = new ScrollView(this);
        subContainer = new LinearLayout(this);
        subContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(subContainer);

        rootLayout.addView(scrollView);
        setContentView(rootLayout);

        initOfflineModels();
    }

    private void initOfflineModels() {
        // Khởi tạo mô hình Tiếng Trung
        StorageService.unpack(this, "model-cn", "model-cn",
            modelCn -> {
                voskModelCn = modelCn;
                // Khởi tạo tiếp mô hình Tiếng Việt
                StorageService.unpack(this, "model-vn", "model-vn",
                    modelVn -> {
                        voskModelVn = modelVn;
                        statusText.setText("Trạng thái: Đã sẵn sàng mô hình AI Tiếng Trung & Tiếng Việt!");
                    },
                    exception -> statusText.setText("Lỗi tải Mô hình AI Tiếng Việt: " + exception.getMessage())
                );
            },
            exception -> statusText.setText("Lỗi tải Mô hình AI Tiếng Trung: " + exception.getMessage())
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
        boolean isChinese = langSpinner.getSelectedItemPosition() == 0;
        Model activeModel = isChinese ? voskModelCn : voskModelVn;

        if (activeModel == null) {
            Toast.makeText(this, "Mô hình AI cho ngôn ngữ này chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Recognizer recognizer = new Recognizer(activeModel, 16000.0f);
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
                                String langPrefix = isChinese ? "Gốc (Trung): " : "Gốc (Việt): ";
                                subtitleList.add(new SubtitleItem(id++, "00:00:00,000", "00:00:05,000", text, langPrefix + text));
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
            origView.setText("Nội dung bóc tách: " + item.getOriginalText());
            origView.setTextSize(15);

            itemBox.addView(origView);
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
