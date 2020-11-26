package com.example.PlagiarismDetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class keyWords extends AppCompatActivity {
    EditText etKeywords;
    Button btnSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_words);
        //declaring layout components
        etKeywords = findViewById(R.id.etKeywords);
        btnSubmit = findViewById(R.id.btSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              retrieve user input keywords from the text book
                String text = etKeywords.getText().toString();
                Intent data = new Intent();
                //pass the keywords text as data to the Main activity
                data.putExtra("keyword", text);
                setResult(RESULT_OK, data);
                finish();
            }
        });

    }
}