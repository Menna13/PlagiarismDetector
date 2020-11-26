package com.example.PlagiarismDetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Executor;


//https://developer.android.com/guide/topics/search/search-dialog#SearchableActivity
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Integer responseCode = null;
    String responseMessage = "";
    //declaring layout components
    Button btnAttach;
    Button btnTokenize;
    TextView tvText;
    ProgressBar mProgressBar;

    public static final String API_KEY = "AIzaSyAPRcHFLnLYMSiwfmpSaFtZSbDIFplEts8";
    public static final String ENGINE_ID = "aa62266ab967eb02f";

    //declaring response codes
    public static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int PICK_FILE = 42;
    private static final int PARSE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //giving access permission for the app to the device internal and external storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }
        getSupportActionBar().setTitle("Plagiarism Detector");

        btnAttach = findViewById(R.id.btnUpload);
        btnTokenize = findViewById(R.id.btnTokenize);

        //disable the text manipulation button until file selection
        btnTokenize.setEnabled(false);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //method for accessing the selected text file
                searchForFile();
                btnTokenize.setText("Tokenize File");
            }
        });
    }

    //checking for access based on user selection to the pop-up window appearing when app starts
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //initialize file selection in the device directories
    private void searchForFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, PICK_FILE);
    }


    //onActivityResult handles all requests to new activities that require sending back data
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                //case for text file selection
                case PICK_FILE:
                    if (resultData != null) {
                        //retrieving file location data
                        Uri uri = resultData.getData();
                        String uriString = uri.toString();
                        if (uriString.startsWith("content://")) {
                            try (Cursor cursor = getContentResolver().
                                    query(uri, null, null, null, null)) {
                                if (cursor != null && cursor.moveToFirst()) {
                                    String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                    //display chosen file name in a toast
                                    Toast.makeText(this, displayName, Toast.LENGTH_SHORT).show();
                                    //send file path to this method to access the file
                                    readTextFromUri(uri);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
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
                //case for parsing the keywords
                case PARSE:
                    String keywords = resultData.getExtras().getString("keyword");
                    tvText.setText(keywords);
                    btnTokenize.setText("Search for occurrences");
                    btnTokenize.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String [] keywordsArray = new String[1];
                            keywordsArray[0]= keywords;
                            search(keywordsArray);
                        }
                    });

            }

        }
    }
    /*
    readTextFromUri take a file Uri path and opens it to read text
    it displays the extracted text to a TextView and enables button that allows user to tokenize document
    */
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
        //display extracted text in TextView
        tvText.setText(stringBuilder.toString());
        btnTokenize.setEnabled(true); //enable button to tokenize
        btnTokenize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call Split method to tokenize file if button is pressed
                Split(stringBuilder.toString());
            }
        });
    }

    /*
    Split method takes a string which is the extracted text and tokenize it
    it also updates tokenize button to be parsing button
    */
    private void Split(String text) {
        String[] tokens = text.split("\\.\\s|\\n");
        btnTokenize.setText("Parse Text");
        btnTokenize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call parse with the tokenized text
                Parse(tokens);
            }
        });
    }


    /*
    Parse method updates button to allow for key word insertion
    if the button is clicked, it starts a new activity which allows the user to optionally type keywords
    to help with search process
    */
    private void Parse(String[] tokens) {
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
        search(tokens);
    }



    private void search(String [] keywords) {
        //create search Url
        String urlString = "https://www.googleapis.com/customsearch/v1?q=" + keywords + "&key=" + API_KEY + "&cx=" + ENGINE_ID + "&alt=json";
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e(TAG, "ERROR converting String to URL " + e.toString());
        }
        Log.d(TAG, "Url = " + urlString);

        // start AsyncTask
        GoogleSearchAsyncTask searchTask = new GoogleSearchAsyncTask();
        //pass url to search Task and execute search process with the url
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






