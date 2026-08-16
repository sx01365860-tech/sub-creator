package com.example.subcreator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.StorageService;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int PICK_FILE_REQUEST = 1;
    private static final int CREATE_FILE_REQUEST = 2;

    private List<SubtitleItem> subtitleList = new ArrayList<>();
    private LinearLayout subContainer;
    private TextView statusText;
    private Model voskModelCn;
    private Model voskModelVn;
    private Spinner langSpinner;
    private Spinner exportModeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(32, 32, 32, 32);

        TextView titleText = new TextView(this);
        titleText.setText("SubCreator - Trình Tạo Phụ Đề & AI Ngữ Cảnh");
        titleText.setTextSize(20);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setPadding(0, 0, 0, 16);
        rootLayout.addView(titleText);

        // Khung chọn ngôn ngữ âm thanh
        LinearLayout langLayout = new LinearLayout(this);
        langLayout.setOrientation(LinearLayout.HORIZONTAL);
        langLayout.setPadding(0, 0, 0, 12);

        TextView langLabel = new TextView(this);
        langLabel.setText("Ngôn ngữ đầu vào: ");
        langLabel.setTextSize(15);
        langLayout.addView(langLabel);

        langSpinner = new Spinner(this);
        String[] languages = {"Tiếng Trung (Mandarin)", "Tiếng Việt (Vietnamese)"};
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        langSpinner.setAdapter(langAdapter);
        langLayout.addView(langSpinner);
        rootLayout.addView(langLayout);

        // Khung chọn chế độ xuất file SRT
        LinearLayout exportLayout = new LinearLayout(this);
        exportLayout.setOrientation(LinearLayout.HORIZONTAL);
        exportLayout.setPadding(0, 0, 0, 16);

        TextView exportLabel = new TextView(this);
        exportLabel.setText("Định dạng xuất SRT: ");
        exportLabel.setTextSize(15);
        exportLayout.addView(exportLabel);

        exportModeSpinner = new Spinner(this);
        String[] exportModes = {"Song ngữ (Trung - Việt)", "Chỉ Tiếng Việt", "Chỉ Tiếng Gốc"};
        ArrayAdapter<String> exportAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, exportModes);
        exportModeSpinner.setAdapter(exportAdapter);
        exportLayout.addView(exportModeSpinner);
        rootLayout.addView(exportLayout);

        // Khung các nút thao tác chính
        LinearLayout btnRow1 = new LinearLayout(this);
        btnRow1.setOrientation(LinearLayout.HORIZONTAL);

        Button btnSelectFile = new Button(this);
        btnSelectFile.setText("1. Chọn File");
        btnSelectFile.setOnClickListener(v -> openFilePicker());

        Button btnAiContext = new Button(this);
        btnAiContext.setText("2. AI Rà Soát Ngữ Cảnh");
        btnAiContext.setOnClickListener(v -> applyAiContextReview());

        btnRow1.addView(btnSelectFile);
        btnRow1.addView(btnAiContext);
        rootLayout.addView(btnRow1);

        Button btnExportSrt = new Button(this);
        btnExportSrt.setText("3. Lưu / Chọn Thư Mục SRT");
        btnExportSrt.setOnClickListener(v -> openSaveFilePicker());
        rootLayout.addView(btnExportSrt);

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
                        statusText.setText("Trạng thái: Đã sẵn sàng Mô hình AI & Rà soát Ngữ cảnh!");
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

    private void applyAiContextReview() {
        if (subtitleList.isEmpty()) {
            Toast.makeText(this, "Chưa có phụ đề để rà soát ngữ cảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        SubContextAIHelper.reviewAndRefine(subtitleList);
        renderSubEditor();
        statusText.setText("Đã AI rà soát & tối ưu ngữ cảnh cho " + subtitleList.size() + " câu phụ đề!");
        Toast.makeText(this, "Tối ưu ngữ cảnh phụ đề thành công!", Toast.LENGTH_SHORT).show();
    }

    private void openSaveFilePicker() {
        if (subtitleList.isEmpty()) {
            Toast.makeText(this, "Chưa có phụ đề để lưu!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "subcreator_output.srt");
        startActivityForResult(intent, CREATE_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_FILE_REQUEST) {
                Uri fileUri = data.getData();
                statusText.setText("Đang bóc tách phụ đề & dịch thuật...");
                processAudioFile(fileUri);
            } else if (requestCode == CREATE_FILE_REQUEST) {
                Uri saveUri = data.getData();
                writeSrtToUri(saveUri);
            }
        }
    }

    private void processAudioFile(Uri uri) {
        boolean isChinese = langSpinner.getSelectedItemPosition() == 0;
        Model activeModel = isChinese ? voskModelCn : voskModelVn;

        if (activeModel == null) {
            Toast.makeText(this, "Mô hình AI chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
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
                double bytesPerMs = 32.0;

                while ((nbytes = is.read(buffer)) >= 0) {
                    bytesProcessed += nbytes;
                    long currentMs = (long) (bytesProcessed / bytesPerMs);

                    if (recognizer.acceptWaveForm(buffer, nbytes)) {
                        String resultJson = recognizer.getResult();
                        String text = extractTextFromJson(resultJson);
                        if (!text.trim().isEmpty()) {
                            String translated = isChinese ? TranslateHelper.translateCnToVn(text) : text;
                            subtitleList.add(new SubtitleItem(id++, formatSrtTime(startMs), formatSrtTime(currentMs), text, translated));
                            startMs = currentMs;
                        }
                    }
                }

                String finalJson = recognizer.getFinalResult();
                String finalText = extractTextFromJson(finalJson);
                if (!finalText.trim().isEmpty()) {
                    long currentMs = (long) (bytesProcessed / bytesPerMs);
                    String translated = isChinese ? TranslateHelper.translateCnToVn(finalText) : finalText;
                    subtitleList.add(new SubtitleItem(id++, formatSrtTime(startMs), formatSrtTime(currentMs), finalText, translated));
                }

                is.close();
                recognizer.close();

                // Tự động rà soát ngữ cảnh ban đầu
                SubContextAIHelper.reviewAndRefine(subtitleList);

                runOnUiThread(() -> {
                    statusText.setText("Hoàn tất! Bóc tách & rà soát ngữ cảnh " + subtitleList.size() + " câu phụ đề.");
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

            // Timeline
            TextView timeView = new TextView(this);
            timeView.setText("[" + item.getStartTime() + "  -->  " + item.getEndTime() + "]");
            timeView.setTextSize(13);
            timeView.setTypeface(null, Typeface.BOLD);
            timeView.setTextColor(Color.parseColor("#0066CC"));
            timeView.setPadding(0, 0, 0, 6);

            // Câu gốc
            TextView origView = new TextView(this);
            origView.setText("Gốc: " + item.getOriginalText());
            origView.setTextSize(14);

            // Câu dịch Tiếng Việt (có thể chỉnh sửa thủ công)
            EditText transEdit = new EditText(this);
            transEdit.setText(item.getTranslatedText());
            transEdit.setTextSize(15);
            transEdit.setTextColor(Color.parseColor("#008800"));
            transEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.setTranslatedText(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            itemCard.addView(timeView);
            itemCard.addView(origView);
            itemCard.addView(transEdit);

            subContainer.addView(itemCard);
        }
    }

    private void writeSrtToUri(Uri uri) {
        try {
            int mode = exportModeSpinner.getSelectedItemPosition();
            StringBuilder srtContent = new StringBuilder();
            for (SubtitleItem item : subtitleList) {
                srtContent.append(item.toSrtFormat(mode));
            }

            OutputStream os = getContentResolver().openOutputStream(uri);
            OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
            writer.write(srtContent.toString());
            writer.close();
            if (os != null) os.close();

            statusText.setText("Đã lưu file SRT thành công!");
            Toast.makeText(this, "Đã lưu file SRT thành công!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi lưu file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
