package com.example.PlagiarismDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.SearchManager;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;

import static androidx.documentfile.provider.DocumentFile.fromSingleUri;



//https://developer.android.com/guide/topics/search/search-dialog#SearchableActivity
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Integer responseCode = null;
    String responseMessage = "";
    Button btnAttach;
    Button btnTokenize;
    TextView tvText;
    Context context;
    ScrollView scrollView;
    ProgressBar mProgressBar;
    public static final String API_KEY = "AIzaSyAPRcHFLnLYMSiwfmpSaFtZSbDIFplEts8";
    public static final String ENGINE_ID = "aa62266ab967eb02f";
    public static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int PICK_FILE = 42;
    private static final int PARSE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }
        getSupportActionBar().setTitle("Plagiarism Detector");
        btnAttach = findViewById(R.id.btnUpload);
        btnTokenize = findViewById(R.id.btnTokenize);
        btnTokenize.setEnabled(false);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchForFile();
                btnTokenize.setText("Tokenize File");
            }
        });
    }

    private void searchForFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, PICK_FILE);
//        setResult(Activity.RESULT_OK, intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permisson not granted!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_FILE:
                    Uri uri = null;
                    if (resultData != null) {
                        uri = resultData.getData();
                        String uriString = uri.toString();
                        String displayName = null;
                        if (uriString.startsWith("content://")) {
                            Cursor cursor = null;
                            try {
                                cursor = getContentResolver().query(uri, null, null, null, null);
                                if (cursor != null && cursor.moveToFirst()) {
                                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                    Toast.makeText(this, displayName, Toast.LENGTH_SHORT).show();
                                    readTextFromUri(uri);

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                cursor.close();
                            }
                        } else if (uriString.startsWith("file://")) {
                            try {
                                readTextFromUri(uri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case PARSE:
                    String keywords = resultData.getExtras().getString("keyword");
                    tvText.setText(keywords);
                    btnTokenize.setText("Search for occurrences");
                    btnTokenize.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            search(keywords);
                        }
                    });

            }

        }
    }


    private void readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
        }
        tvText = findViewById(R.id.tvExtracted);
        tvText.setText(stringBuilder.toString());
        btnTokenize.setEnabled(true); //enable button to tokenize
        btnTokenize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Tokenize(stringBuilder.toString());
                Split(stringBuilder.toString());
            }
        });
    }

    private void Split(String text) {
        String[] tokens = text.split("\\.\\s|\\n");
        tvText.setText(tokens.toString());
        for (String s : tokens) {
            System.out.println(s);
        }
        btnTokenize.setText("Parse Text");
        btnTokenize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Parse(tokens);
            }
        });
    }

    private void Parse(String[] tokens) {
//        if (! Python.isStarted()) {
//            Python.start(new AndroidPlatform(context));
//            Python py = Python.getInstance();
//            PyObject gensim = py.getModule("gensim");
//            PyObject lds = gensim.callAttr("LdaModle", mm, tokens, 100, 1, 10000, 1);
//            PyObject lda  = py.getModule("load_from_all");
        btnTokenize.setText("Insert Key Words");
        btnTokenize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "got into parse");
                Intent i = new Intent(MainActivity.this, keyWords.class);
                startActivityForResult(i, PARSE);
                Log.d("MainActivity", "got into end of parse");
            }
        });
    }


    private void Tokenize(String text) {
        ArrayList<String> tokens = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(text);
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        for (String s : tokens) {
            System.out.println(s);
        }
    }


    private void search(String keywords) {
        String searchStringNoSpaces = keywords.replace(",", "+");
        String urlString = "https://www.googleapis.com/customsearch/v1?q=" + searchStringNoSpaces + "&key=" + API_KEY + "&cx=" + ENGINE_ID + "&alt=json";
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e(TAG, "ERROR converting String to URL " + e.toString());
        }
        Log.d(TAG, "Url = " + urlString);

        // start AsyncTask
        GoogleSearchAsyncTask searchTask = new GoogleSearchAsyncTask();
        searchTask.execute(url);
    }


    private class GoogleSearchAsyncTask extends Executor<URL,Integer,String> {

        @Override
        public void execute(Runnable command) {
            Log.d(TAG, "AsyncTask - onPreExecute");
            // show mProgressBar
            mProgressBar.setVisibility(View.VISIBLE);
        }

            URL url = urls[0];

            // Http connection
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                Log.e(TAG, "Http connection ERROR " + e.toString());
            }


            try {
                responseCode = conn.getResponseCode();
                responseMessage = conn.getResponseMessage();
            } catch (IOException e) {
                Log.e(TAG, "Http getting response code ERROR " + e.toString());

            }


                if (responseCode != null && responseCode == 200) {

                    // response OK

                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = rd.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    rd.close();

                    conn.disconnect();

                    result = sb.toString();



            }
        }

    }


}




