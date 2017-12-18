package com.tonkar.volleyballreferee.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.io.ByteArrayOutputStream;

public class UiUtils {

    public static void styleBaseTeamButton(Context context, BaseTeamService teamService, TeamType teamType, Button button) {
        UiUtils.colorTeamButton(context, teamService.getTeamColor(teamType), button);
        button.setPaintFlags(button.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
    }

    public static void styleIndoorTeamButton(Context context, IndoorTeamService indoorTeamService, TeamType teamType, int number, Button button) {
        button.setPaintFlags(button.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));

        if (indoorTeamService.isLibero(teamType, number)) {
            colorTeamButton(context, indoorTeamService.getLiberoColor(teamType), button);
        } else {
            colorTeamButton(context, indoorTeamService.getTeamColor(teamType), button);
            if (indoorTeamService.isCaptain(teamType, number) || indoorTeamService.isActingCaptain(teamType, number)) {
                button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        }
    }

    public static void styleBaseIndoorTeamButton(Context context, BaseIndoorTeamService indoorTeamService, TeamType teamType, int number, Button button) {
        button.setPaintFlags(button.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));

        if (indoorTeamService.isLibero(teamType, number)) {
            colorTeamButton(context, indoorTeamService.getLiberoColor(teamType), button);
        } else {
            colorTeamButton(context, indoorTeamService.getTeamColor(teamType), button);
            if (indoorTeamService.isCaptain(teamType, number)) {
                button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        }
    }

    public static void styleBaseIndoorTeamText(Context context, BaseIndoorTeamService indoorTeamService, TeamType teamType, int number, TextView text) {
        text.setPaintFlags(text.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));

        if (indoorTeamService.isLibero(teamType, number)) {
            colorTeamText(context, indoorTeamService.getLiberoColor(teamType), text);
        } else {
            colorTeamText(context, indoorTeamService.getTeamColor(teamType), text);
            if (indoorTeamService.isCaptain(teamType, number)) {
                text.setPaintFlags(text.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        }
    }

    public static void colorTeamButton(Context context, int color, Button button) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setBackgroundTintList(button, ColorStateList.valueOf(color));
        } else {
            button.getBackground().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
        }
        button.setTextColor(getTextColor(context, color));
    }

    public static void colorTeamText(Context context, int color, TextView text) {
        text.setBackgroundColor(color);
        text.setTextColor(UiUtils.getTextColor(context, color));
    }

    public static int getTextColor(Context context, int backgroundColor) {
        int textColor;

        double a = 1 - ( 0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255;

        if (a < 0.5) {
            textColor = ContextCompat.getColor(context, R.color.colorTextTeamLightBackground);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextTeamDarkBackground);
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

            String title = summary.replaceAll("-|\t|\n", "_");
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, title, null);
            if (path == null) {
                Toast.makeText(context, context.getResources().getString(R.string.share_exception), Toast.LENGTH_LONG).show();
            } else {
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
                    Toast.makeText(context, context.getResources().getString(R.string.share_exception), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Log.w("VBR-Share", "No permission to share");
        }
    }

    public static void setAlertDialogMessageSize(AlertDialog alertDialog, Resources resources) {
        TextView textView = alertDialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.default_text_size));
        }
    }

    public static void navigateToHome(Activity activity) {
        navigateToHome(activity, true);
    }

    public static void navigateToHome(Activity activity, boolean showResumeGameDialog) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("show_resume_game", showResumeGameDialog);
        activity.startActivity(intent);
    }

}
