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
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.PdfGameWriter;
import com.tonkar.volleyballreferee.business.data.WebUtils;
import com.tonkar.volleyballreferee.interfaces.team.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Locale;

public class UiUtils {

    public static void styleBaseTeamButton(Context context, BaseTeamService teamService, TeamType teamType, Button button) {
        colorTeamButton(context, teamService.getTeamColor(teamType), button);
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

    public static void styleBaseTeamText(Context context, BaseTeamService teamService, TeamType teamType, TextView text) {
        colorTeamText(context, teamService.getTeamColor(teamType), text);
        text.setPaintFlags(text.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setBackgroundTintList(text, ColorStateList.valueOf(color));
        } else {
            text.getBackground().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
        }
        text.setTextColor(getTextColor(context, color));
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

    public static void colorTeamIconButton(Context context, int color, ImageButton button) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setBackgroundTintList(button, ColorStateList.valueOf(color));
        } else {
            button.getBackground().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
        }
        button.getDrawable().setColorFilter(new PorterDuffColorFilter(getTextColor(context, color), PorterDuff.Mode.SRC_IN));
    }

    public static void shareGame(Context context, Window window, GameService gameService, boolean online) {
        Log.i("VBR-Share", "Share screen");
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            View rootView = window.getDecorView().findViewById(android.R.id.content);
            View screenView = rootView.getRootView();
            screenView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
            screenView.setDrawingCacheEnabled(false);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            File filedir = new File(Environment.getExternalStorageDirectory(),"Screenshots");
            filedir.mkdirs();

            String title = String.format(Locale.getDefault(), "%s_%s_%d", gameService.getTeamName(TeamType.HOME), gameService.getTeamName(TeamType.GUEST), System.currentTimeMillis());
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, title, null);
            if (path == null) {
                Toast.makeText(context, context.getResources().getString(R.string.share_exception), Toast.LENGTH_LONG).show();
            } else {
                Uri bitmapUri = Uri.parse(path);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");

                String summary = gameService.getGameSummary();
                if (online) {
                    String url = GameType.INDOOR.equals(gameService.getGameType()) ? WebUtils.VIEW_INDOOR_URL : WebUtils.VIEW_BEACH_URL;
                    summary = summary + "\n" + String.format(Locale.getDefault(), url, gameService.getGameDate());
                }

                intent.putExtra(Intent.EXTRA_TEXT, summary);
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

    public static void shareRecordedGame(Context context, RecordedGameService recordedGameService) {
        Log.i("VBR-Share", "Share recorded game");
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File file = PdfGameWriter.writeRecordedGame(context, recordedGameService);
            if (file == null) {
                Toast.makeText(context, context.getResources().getString(R.string.share_exception), Toast.LENGTH_LONG).show();
            } else {
                Uri uri = FileProvider.getUriForFile(context, "com.tonkar.volleyballreferee.fileprovider", file);
                context.grantUriPermission("com.tonkar.volleyballreferee.fileprovider", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, String.format(Locale.getDefault(), "%s - %s", context.getResources().getString(R.string.app_name), file.getName()));
                intent.putExtra(Intent.EXTRA_TEXT, recordedGameService.getGameSummary());
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.share)));
                } catch (ActivityNotFoundException e) {
                    Log.e("VBR-Share", "Exception while sharing recorded game", e);
                    Toast.makeText(context, context.getResources().getString(R.string.share_exception), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Log.w("VBR-Share", "No permission to share");
        }
    }

    public static void shareUrl(Context context, String url) {
        Log.i("VBR-Share", "Share url");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setType("text/plain");
        context.startActivity(Intent.createChooser(intent, context.getResources().getText(R.string.share)));
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("show_resume_game", showResumeGameDialog);
        activity.startActivity(intent);
    }

    public static void animate(Context context, final View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.press_button);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        view.startAnimation(animation);
    }

    public static void animateBounce(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.bounce_view);
        animation.setInterpolator(new BounceInterpolator());
        view.startAnimation(animation);
    }

    public static void playNotificationSound(Context context) {
        final MediaPlayer timeoutPlayer = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (timeoutPlayer != null) {
            timeoutPlayer.setLooping(false);
            timeoutPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    timeoutPlayer.release();
                }
            });
            timeoutPlayer.start();
        }
    }

}
