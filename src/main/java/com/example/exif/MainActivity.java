package com.example.exif;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    GridView gvImgs;
    Button btLoad;
    Button btClear;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;
    ArrayList<Uri> imgs;
    ImageAdapter adapter;
    ExifUtils exif = new ExifUtils();
    AlertDialog.Builder builder;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        gvImgs = findViewById(R.id.gvImgs);
        btLoad = findViewById(R.id.btLoad);
        btClear = findViewById(R.id.btClear);

        exif.mContext = this;
        imgs = new ArrayList();
        adapter = new ImageAdapter(this);
        builder = new AlertDialog.Builder(this);

        btLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImages();
            }
        });
        btClear.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                clearFiles();
            }
        });

        verifyStoragePermissions(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){

            if(data.getClipData() != null) { // MULTIPLE IMAGES
                int count = data.getClipData().getItemCount();
                for(int i = 0; i < count; i++)
                    imgs.add(data.getClipData().getItemAt(i).getUri());

            }else if(data.getData() != null) { // ONE IMAGE
                String imagePath = data.getData().getPath();
                imgs.add(data.getData());
            }
            adapter.images = imgs;
            gvImgs.setAdapter(adapter);

        }
    }
    private void loadImages() {
       Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
       gallery.setType("image/*");
       gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
       startActivityForResult(gallery, PICK_IMAGE);
    }


    private void clearFiles() {

        if(imgs.size()>0){

            StringBuilder cleaned = new StringBuilder();
            StringBuilder issues = new StringBuilder();
            builder = new AlertDialog.Builder(this);

            cleaned.append("Cleaned: \n--------------------\n");
            issues.append("\n\nWith issues: \n--------------------\n");

            for(int i = 0; i<imgs.size(); i++){

                String path = exif.getPathFromURI(imgs.get(i));

                String filename = new File(path).getName();

                String ext = String.valueOf(exif.getExtension(filename));
                Path source= Paths.get(path);

                String dest = path.replace(ext, "_cleaned"+ext);
                dest = dest.replace("/Camera/", "/Camera/Cleaned/");
                Path destination=Paths.get(dest);

                try {
                    Files.copy(source, destination);
                } catch (IOException e) {
                    Toast.makeText(this, "Operation failed - create Cleaned folder in Camera directory", Toast.LENGTH_LONG).show();
                    issues.append(source.toString() + " not copied " + e.toString() + " \n\n");
                    continue;
                }

                try{
                    exif.clearFile(dest);
                    cleaned.append("\n "+ dest + " \n");
                } catch (IOException e) {
                    issues.append("\n\n "+ dest + " - NOT cleaned " + e.toString() + "\n\n");
                }

            }

            builder.setMessage(cleaned.toString() + issues.toString())
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setTitle("ExIF Cleaning summary");
            alert.show();

        }else{
            Toast.makeText(this, "Image list is empty", Toast.LENGTH_LONG).show();
        }
    }

}