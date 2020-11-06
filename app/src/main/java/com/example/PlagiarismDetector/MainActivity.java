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

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static androidx.documentfile.provider.DocumentFile.fromSingleUri;

public class MainActivity extends AppCompatActivity {
    Button btnAttach;
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
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/*");
//        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
                startActivityForResult(intent, PICK_PDF_FILE);
                setResult(Activity.RESULT_OK, intent);
            }
        });
    }


//    private void openFile (){
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("application/msword");
////        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
//        startActivityForResult(intent, PICK_PDF_FILE);
//        setResult(Activity.RESULT_OK, intent);
//        Uri data = intent.getData();
//        System.out.println("see here");
//        System.out.println(data);
//        onActivityResult(data);
//
//    }

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
                //                AccessFile(uri, this.context);
                //                DocumentFile documentFile = fromSingleUri(context, uri);
                String uriString = uri.toString();
                File documentFile = new File(uriString);
                String path = documentFile.getAbsolutePath();
                String displayName = null;
                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            AccessFile(displayName, documentFile);
                        }
                    } finally {
                        cursor.close();
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = documentFile.getName();
                    AccessFile(displayName, documentFile);
                }
            }
        }

    }


    public void AccessFile(String filename, File document) {
        Log.d("MainActivity", "ReachedAccessFile");
//        DocumentFile documentFile = fromSingleUri(context, uri);
//        documentFile.getName();
        try {
            Log.d("MainActivity", "ReachedAcessFileTRY");
            //change uri to file name and get it from document file
            InputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename);
            XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(fis));
            XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
            System.out.println("maybe parsed?");
            System.out.println(extractor.getText());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }







//        public void TextReader() {
//            try {
//                InputStream fis = new FileInputStream("test.docx");
//                XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(fis));
//                XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
//                System.out.println(extractor.getText());
//            } catch(Exception ex) {
//                ex.printStackTrace();
//            }
//        }



        /*    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICK_PDF_FILE
                && resultCode == Activity.RESULT_OK) {
            Log.d("MainActivity", "ReachedOnActivityResult");
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.d("MainActivity", "ReachedOnActivityResultIF");
                // Perform operations on the document using its URI.
            }
        }
    }*/















 /*   public void attachButton_OnClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void getDocument()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/msword,application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
//        startActivityForResult(intent, REQUEST_CODE_DOC);
    }


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
