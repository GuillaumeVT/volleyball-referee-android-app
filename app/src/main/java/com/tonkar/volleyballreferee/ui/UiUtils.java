package com.tonkar.volleyballreferee.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;

import java.io.ByteArrayOutputStream;

public class UiUtils {

    public static int getTextColor(Context context, int backgroundColor) {
        int textColor;

        double a = 1 - ( 0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255;

        if (a < 0.5) {
            textColor = ContextCompat.getColor(context, R.color.colorPrimaryTextDefaultMaterialLight);
        } else {
            textColor = ContextCompat.getColor(context, android.R.color.white);
        }

        return textColor;
    }

    public static void shareScreen(Context context, Window window, String summary) {
        Log.i("VBR-Share", "Share screen");
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            View rootView = window.getDecorView().findViewById(android.R.id.content);
            View screenView = rootView.getRootView();
            screenView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
            screenView.setDrawingCacheEnabled(false);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, summary, null);
            Uri bitmapUri = Uri.parse(path);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");

            intent.putExtra(android.content.Intent.EXTRA_TEXT, summary);
            intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            try {
                context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.share)));
            } catch (ActivityNotFoundException e) {
                Log.e("VBR-Share", "Exception while sharing", e);
                final Toast toast = Toast.makeText(context, context.getResources().getString(R.string.share_exception), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } else {
            Log.w("VBR-Share", "No permission to share");
        }
    }
}
