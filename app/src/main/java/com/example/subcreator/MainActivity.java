package com.example.subcreator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import java.util.Locale;

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
        titleText.setTextSize(20);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setPadding(0, 0, 0, 16);
        rootLayout.addView(titleText);

        // Khung chọn ngôn ngữ
        LinearLayout langLayout = new LinearLayout(this);
        langLayout.setOrientation(LinearLayout.HORIZONTAL);
        langLayout.setPadding(0, 0, 0, 16);

        TextView langLabel = new TextView(this);
        langLabel.setText("Ngôn ngữ âm thanh: ");
        langLabel.setTextSize(15);
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
        statusText.setText("Đang khởi tạo các Mô hình AI Offline...");
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
        StorageService.unpack(this, "model-cn", "model-cn",
            modelCn -> {
                voskModelCn = modelCn;
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
            statusText.setText("Đang phân tích âm thanh & tính toán timeline...");
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

                long bytesProcessed = 0;
                long startMs = 0;
                double bytesPerMs = 32.0; // Giả định PCM 16kHz 16-bit Mono

                while ((nbytes = is.read(buffer)) >= 0) {
                    bytesProcessed += nbytes;
                    long currentMs = (long) (bytesProcessed / bytesPerMs);

                    if (recognizer.acceptWaveForm(buffer, nbytes)) {
                        String resultJson = recognizer.getResult();
                        String text = extractTextFromJson(resultJson);
                        if (!text.trim().isEmpty()) {
                            String startTimeStr = formatSrtTime(startMs);
                            String endTimeStr = formatSrtTime(currentMs);
                            subtitleList.add(new SubtitleItem(id++, startTimeStr, endTimeStr, text));
                            startMs = currentMs;
                        }
                    }
                }

                String finalJson = recognizer.getFinalResult();
                String finalText = extractTextFromJson(finalJson);
                if (!finalText.trim().isEmpty()) {
                    long currentMs = (long) (bytesProcessed / bytesPerMs);
                    subtitleList.add(new SubtitleItem(id++, formatSrtTime(startMs), formatSrtTime(currentMs), finalText));
                }

                is.close();
                recognizer.close();

                runOnUiThread(() -> {
                    statusText.setText("Hoàn tất! Bóc tách được " + subtitleList.size() + " câu phụ đề kèm Timeline.");
                    renderSubEditor();
                });

            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText("Lỗi xử lý file: " + e.getMessage()));
            }
        }).start();
    }

    private String formatSrtTime(long ms) {
        long hours = ms / (1000 * 60 * 60);
        long minutes = (ms % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (ms % (1000 * 60)) / 1000;
        long millis = ms % 1000;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d,%03d", hours, minutes, seconds, millis);
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
            LinearLayout itemCard = new LinearLayout(this);
            itemCard.setOrientation(LinearLayout.VERTICAL);
            itemCard.setPadding(24, 20, 24, 20);

            // Mốc thời gian (Timeline)
            TextView timeView = new TextView(this);
            timeView.setText("[" + item.getStartTime() + "  -->  " + item.getEndTime() + "]");
            timeView.setTextSize(13);
            timeView.setTypeface(null, Typeface.BOLD);
            timeView.setTextColor(Color.parseColor("#0066CC"));
            timeView.setPadding(0, 0, 0, 6);

            // Nội dung Phụ đề
            TextView subTextView = new TextView(this);
            subTextView.setText(item.getOriginalText());
            subTextView.setTextSize(16);
            subTextView.setTextColor(Color.BLACK);

            itemCard.addView(timeView);
            itemCard.addView(subTextView);

            subContainer.addView(itemCard);
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
