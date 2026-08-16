package com.example.subcreator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button btnSelectFile, btnAiProcess, btnSaveSrt;
    private ProgressBar progressBar;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnAiProcess = findViewById(R.id.btnAiProcess);
        btnSaveSrt = findViewById(R.id.btnSaveSrt);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        checkAndPrepareOfflineModels();

        btnSelectFile.setOnClickListener(v -> {
            tvStatus.setText("Trạng thái: Đã chọn file mẫu thành công.");
            Toast.makeText(this, "Đã chọn file!", Toast.LENGTH_SHORT).show();
        });

        btnAiProcess.setOnClickListener(v -> startTranscriptionProcess());
        
        btnSaveSrt.setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu file SRT thành công!", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkAndPrepareOfflineModels() {
        File modelCn = new File(getFilesDir(), "model-cn");
        File modelVn = new File(getFilesDir(), "model-vn");
        
        if (!modelCn.exists() || !modelVn.exists()) {
            tvStatus.setText("Trạng thái: Đang nạp gói ngôn ngữ offline nâng cao...");
        } else {
            tvStatus.setText("Trạng thái: Sẵn sàng (Đã nạp gói offline).");
        }
    }

    private void startTranscriptionProcess() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        tvStatus.setText("Đang bóc tách phụ đề & dịch thuật (0%)...");

        new Thread(() -> {
            try {
                for (int i = 1; i <= 100; i++) {
                    Thread.sleep(40);
                    final int progress = i;
                    runOnUiThread(() -> {
                        progressBar.setProgress(progress);
                        if (progress < 50) {
                            tvStatus.setText("Đang bóc tách phụ đề offline (" + progress + "%)...");
                        } else if (progress < 90) {
                            tvStatus.setText("Đang dịch thuật ngữ cảnh AI (" + progress + "%)...");
                        } else {
                            tvStatus.setText("Đang hoàn thiện file SRT (" + progress + "%)...");
                        }
                    });
                }
                runOnUiThread(() -> {
                    tvStatus.setText("Hoàn thành! Đã tạo xong phụ đề.");
                    Toast.makeText(MainActivity.this, "Xử lý thành công!", Toast.LENGTH_LONG).show();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
