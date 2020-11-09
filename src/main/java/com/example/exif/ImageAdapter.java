package com.example.exif;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by HeadFullOfCiphers on 12-09-2020.
 *
 * Based on:
     * a
 * by tutlane on 24-08-2017.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    AlertDialog.Builder builder;
    ExifUtils exif = new ExifUtils();
    public ArrayList<Uri> images = new ArrayList();

    public ImageAdapter(Context c) {
        mContext = c;
    }
    public int getCount() {
        return images.size();
    }
    public Object getItem(int position) {
        return null;
    }
    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        exif.mContext = mContext;
        builder = new AlertDialog.Builder(mContext);
        ImageView imageView = new ImageView(mContext);

        imageView.setLayoutParams(new GridView.LayoutParams(450, 400));
        imageView.setPadding(10,0,0,0);

        try {
            // here we are trying to introduce some optimisation
            // by reducing required memory and lower loading the time
            Bitmap img = exif.getThumbnail(images.get(position));
            imageView.setImageBitmap(img);
        } catch (IOException e) {
            // if there is an exception we can do it less efficient way
            imageView.setImageURI(images.get(position));
        }

        imageView.setClickable(true);
        imageView.bringToFront();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               builder.setMessage(exif.getExifFromUri(images.get(position)))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                String filename = new File(exif.getPathFromURI(images.get(position))).getName();
                alert.setTitle("ExIF " +filename);
                alert.show();
            }
        });
        return imageView;
    }


}