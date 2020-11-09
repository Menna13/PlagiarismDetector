package com.example.PlagiarismDetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.barteksc.pdfviewer.PDFView;

public class keyWords extends AppCompatActivity {
    EditText etKeywords;
    Button btnSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_words);
        etKeywords = findViewById(R.id.etKeywords);
        btnSubmit = findViewById(R.id.btSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etKeywords.getText().toString();
                Intent data = new Intent();
                data.putExtra("keyword", text);
                setResult(RESULT_OK, data);
                finish();
            }
        });

    }
}