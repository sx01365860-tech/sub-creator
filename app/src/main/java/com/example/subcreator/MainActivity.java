package com.example.subcreator;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("SubCreator App Created Successfully!");
        textView.setTextSize(20);
        setContentView(textView);
    }
}
