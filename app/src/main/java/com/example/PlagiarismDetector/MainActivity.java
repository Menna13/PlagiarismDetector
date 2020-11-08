package com.example.PlagiarismDetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import java.util.Objects;

import static androidx.documentfile.provider.DocumentFile.fromSingleUri;

public class MainActivity extends AppCompatActivity {
    Button btnAttach;
    TextView tvText;
    Context context;
    private static final int PICK_PDF_FILE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Plagiarism Detector");
        btnAttach = findViewById(R.id.btnUpload);
        btnAttach.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                openFile();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/*");
                startActivityForResult(intent, PICK_PDF_FILE);
                setResult(Activity.RESULT_OK, intent);
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICK_PDF_FILE
                && resultCode == Activity.RESULT_OK) {
            Log.d("MainActivity", "ReachedOnActivityResult");
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                System.out.println("see here");
                System.out.println(uri);
                Log.d("MainActivity", "ReachedOnActivityResultIF");
                String uriString = uri.toString();
                System.out.println(uriString);
                File documentFile = new File(uriString);
                System.out.println(documentFile.canRead());
                System.out.println(documentFile.getName());
                String path = documentFile.getAbsolutePath();
                String displayName = null;
                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            System.out.println("got into content");
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            readTextFromUri(uri);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        cursor.close();
                    }
                } else if (uriString.startsWith("file://")) {
                    System.out.println("got into file");
                    displayName = documentFile.getName();

                }
            }
        }

    }


    private String readTextFromUri(Uri uri) throws IOException {
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
        tvText.setText(stringBuilder);
        return stringBuilder.toString();


    }


    public void AccessFile(String filename, File document, InputStream inputStream) {
        Log.d("MainActivity", "ReachedAccessFile");
        try {
            Log.d("MainActivity", "ReachedAcessFileTRY");
            //change uri to file name and get it from document file
//            InputStream fis = new FileInputStream(document);

            XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(inputStream));
            XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
            System.out.println("maybe parsed?");
            System.out.println(extractor.getText());
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }






/*

    @Override
    protected void onActivityResult(int req, int result, Intent data)
    {
        // TODO Auto-generated method stub
        super.onActivityResult(req, result, data);
        if (result == RESULT_OK)
        {
            Uri fileuri = data.getData();
            // docFilePath = getFileNameByUri(this, fileuri);
        }
    }

    // get file path

    private String getFileNameByUri(Context context, Uri uri)
    {
        String filepath = "";//default fileName
        //Uri filePathUri = uri;
        File file;
        if (uri.getScheme().toString().compareTo("content") == 0)
        {
            Cursor cursor = context.getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.ORIENTATION }, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();

            String mImagePath = cursor.getString(column_index);
            cursor.close();
            filepath = mImagePath;

        }
        else
        if (uri.getScheme().compareTo("file") == 0)
        {
            try
            {
                file = new File(new URI(uri.toString()));
                if (file.exists())
                    filepath = file.getAbsolutePath();

            }
            catch (URISyntaxException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            filepath = uri.getPath();
        }
        return filepath;
    }*/


}
