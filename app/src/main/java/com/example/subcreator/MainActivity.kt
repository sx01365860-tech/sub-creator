package com.example.subcreator

import android.os.Bundle
import android.app.Activity
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this).apply {
            text = "SubCreator App Created Successfully!"
            textSize = 20f
        }
        setContentView(textView)
    }
}
