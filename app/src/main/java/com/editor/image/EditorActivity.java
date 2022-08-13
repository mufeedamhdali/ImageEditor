package com.editor.image;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class EditorActivity extends AppCompatActivity {

    String imagePth;
    ImageView editImage;
    Button btnFlipHori;
    Button btnFlipVert;
    Button btnSave;
    Button btnCrop;
    Button btnDetails;
    Uri uri;
    int unit;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editImage = findViewById(R.id.editImage);
        btnFlipHori = findViewById(R.id.btnFlipH);
        btnFlipVert = findViewById(R.id.btnFlipV);
        btnSave = findViewById(R.id.btnSave);
        btnCrop = findViewById(R.id.btnCrop);
        btnDetails = findViewById(R.id.btnDetails);

        Intent intent = getIntent();
        if ((intent.getStringExtra("filePath")) != null) {
            imagePth = intent.getStringExtra("filePath");
            uri = Uri.parse(imagePth);
            Log.d("filePathReceived", imagePth);
        }

        editImage.setImageURI(uri);

        btnCrop.setOnClickListener(v -> {
            bitmap = ((BitmapDrawable) editImage.getDrawable()).getBitmap();
            int imageHeight = bitmap.getHeight();
            int imageWidth = bitmap.getWidth();
            unit = Math.min(imageHeight, imageWidth);
            showPopupMenu(v);
        });


        btnFlipHori.setOnClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) editImage.getDrawable()).getBitmap(); // get bitmap associated with your imageview
            editImage.setImageBitmap(flipHorizontal(bitmap));
        });

        btnFlipVert.setOnClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) editImage.getDrawable()).getBitmap(); // get bitmap associated with your imageview
            editImage.setImageBitmap(flipVertical(bitmap));
        });

        btnDetails.setOnClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) editImage.getDrawable()).getBitmap(); // get bitmap associated with your imageview

            AlertDialog.Builder imageDetails = new AlertDialog.Builder(EditorActivity.this);
            imageDetails.setTitle("Image Details");
            imageDetails.setMessage(
                    "Height: " + bitmap.getHeight() + "\n"
                            + "Width: " + bitmap.getWidth() + "\n"
                            + "Size: " + bytesToSize(bitmap.getByteCount()) + "\n"
                            + "Location: " + uri.getPath()
            );
            imageDetails.setCancelable(true);

            imageDetails.setPositiveButton(
                    "Ok",
                    (dialog, id) -> dialog.cancel());

            AlertDialog details = imageDetails.create();
            details.show();
        });

        btnSave.setOnClickListener(v -> {
            long tsLong = System.currentTimeMillis() / 1000;
            String fileName = Long.toString(tsLong);
            Bitmap bitmap = ((BitmapDrawable) editImage.getDrawable()).getBitmap();
            saveImage(bitmap, fileName, "jpeg");
        });

    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(EditorActivity.this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.crop_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.one_one) {
                RectF rectf = new RectF(0, 0, unit, unit);
                Bitmap resBitMap = cropImage(bitmap, rectf);
                editImage.setImageBitmap(resBitMap);
            } else if (item.getItemId() == R.id.three_four) {
                unit = unit / 3;
                RectF rectf = new RectF(0, 0, unit * 3, unit * 4);
                Bitmap resBitMap = cropImage(bitmap, rectf);
                editImage.setImageBitmap(resBitMap);
            } else if (item.getItemId() == R.id.nine_sixteen) {
                unit = unit / 9;
                RectF rectf = new RectF(0, 0, unit * 9, unit * 16);
                Bitmap resBitMap = cropImage(bitmap, rectf);
                editImage.setImageBitmap(resBitMap);
            }
            return true;
        });
        popup.show();
    }

    private static Bitmap cropImage(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        canvas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        canvas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }

    public static Bitmap flipHorizontal(Bitmap src) {
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static Bitmap flipVertical(Bitmap src) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public void saveImage(Bitmap bitmap, String name, String extension) {
        name = name + "." + extension;
        FileOutputStream fileOutputStream;
        try {

            File storageDir = new File(Environment.getExternalStorageDirectory().toString(), "Download/ImageEditor");
            storageDir.mkdirs();

            File outFile = new File(storageDir, name);
            fileOutputStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            Toast.makeText(EditorActivity.this, "File saved to " + outFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String bytesToSize(long bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;
        long terabyte = gigabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return (bytes / gigabyte) + " GB";

        } else if (bytes >= terabyte) {
            return (bytes / terabyte) + " TB";

        } else {
            return bytes + " Bytes";
        }
    }
}