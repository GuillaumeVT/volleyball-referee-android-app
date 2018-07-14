package com.tonkar.volleyballreferee.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.PdfGameWriter;
import com.tonkar.volleyballreferee.business.data.WebUtils;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.user.UserActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Locale;
import java.util.Random;

public class UiUtils {

    private static final Random RANDOM = new Random();

    public static int getRandomShirtColor(Context context) {
        String[] colorsStr = context.getResources().getStringArray(R.array.shirt_colors);
        String randomColorStr = colorsStr[RANDOM.nextInt(colorsStr.length - 1)];
        return Color.parseColor(randomColorStr);
    }

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

    public static void styleTeamButton(Context context, BaseTeamService teamService, TeamType teamType, int number, Button button) {
        button.setPaintFlags(button.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));

        if (teamService.isLibero(teamType, number)) {
            colorTeamButton(context, teamService.getLiberoColor(teamType), button);
        } else {
            colorTeamButton(context, teamService.getTeamColor(teamType), button);
            if (teamService.isCaptain(teamType, number)) {
                button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        }
    }

    public static void styleTeamText(Context context, BaseTeamService teamService, TeamType teamType, int number, TextView text) {
        text.setPaintFlags(text.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));

        if (teamService.isLibero(teamType, number)) {
            colorTeamText(context, teamService.getLiberoColor(teamType), text);
        } else {
            colorTeamText(context, teamService.getTeamColor(teamType), text);
            if (teamService.isCaptain(teamType, number)) {
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

    public static void shareGame(Context context, Window window, GameService gameService) {
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
                if (PrefUtils.isPrefDataSyncEnabled(context)) {
                    summary = summary + "\n" + String.format(Locale.getDefault(), WebUtils.VIEW_URL, gameService.getGameDate());
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

                String summary = recordedGameService.getGameSummary();
                if (PrefUtils.isPrefDataSyncEnabled(context)) {
                    summary = summary + "\n" + String.format(Locale.getDefault(), WebUtils.VIEW_URL, recordedGameService.getGameDate());
                }

                intent.putExtra(Intent.EXTRA_TEXT, summary);
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

    public static void navigateToUser(Activity activity) {
        Intent intent = new Intent(activity, UserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

    public static String getPositionTitle(Context context, PositionType positionType) {
        String title = "";

        switch (positionType) {
            case POSITION_1:
                title = context.getResources().getString(R.string.position_1_title);
                break;
            case POSITION_2:
                title = context.getResources().getString(R.string.position_2_title);
                break;
            case POSITION_3:
                title = context.getResources().getString(R.string.position_3_title);
                break;
            case POSITION_4:
                title = context.getResources().getString(R.string.position_4_title);
                break;
            case POSITION_5:
                title = context.getResources().getString(R.string.position_5_title);
                break;
            case POSITION_6:
                title = context.getResources().getString(R.string.position_6_title);
                break;
        }

        return title;
    }

    public static void showNotification(Context context, String message) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean interactiveNotification = sharedPreferences.getBoolean("pref_interactive_notification", false);

        if (interactiveNotification) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
            builder.setMessage(message);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });
            AlertDialog alertDialog = builder.show();
            centerAlertDialogMessage(alertDialog);
            setAlertDialogMessageSize(alertDialog, context.getResources());
        } else {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    public static void setAlertDialogMessageSize(AlertDialog alertDialog, Resources resources) {
        TextView textView = alertDialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.default_text_size));
        }
    }

    private static void centerAlertDialogMessage(AlertDialog alertDialog) {
        TextView textView = alertDialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setGravity(Gravity.CENTER);
        }
    }
}
