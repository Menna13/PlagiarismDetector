package com.example.PlagiarismDetector;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;

public class OpenPdf extends AppCompatActivity {
    PDFView pdfView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_pdf);

        pdfView = (PDFView) findViewById(R.id.pdfviewtest);
        pdfView.fromAsset("");
    }
}