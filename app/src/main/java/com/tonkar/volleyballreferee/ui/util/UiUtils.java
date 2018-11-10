package com.tonkar.volleyballreferee.ui.util;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
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

import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.ScoreSheetWriter;
import com.tonkar.volleyballreferee.business.web.WebUtils;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.TimeBasedGameService;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.MainActivity;
import com.tonkar.volleyballreferee.ui.user.UserSignInActivity;

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

    public static void styleBaseTeamButton(Context context, BaseTeamService teamService, TeamType teamType, MaterialButton button) {
        colorTeamButton(context, teamService.getTeamColor(teamType), button);
        button.setPaintFlags(button.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
    }

    public static void styleIndoorTeamButton(Context context, IndoorTeamService indoorTeamService, TeamType teamType, int number, MaterialButton button) {
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

    public static void styleTeamButton(Context context, BaseTeamService teamService, TeamType teamType, int number, MaterialButton button) {
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

    public static void colorTeamButton(Context context, int color, MaterialButton button) {
        int textColor = getTextColor(context, color);
        button.setTextColor(textColor);
        button.setBackgroundTintList(ColorStateList.valueOf(color));
        if (button.getIcon() != null) {
            button.setIconTint(ColorStateList.valueOf(textColor));
        }
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

    public static void colorTeamIconButton(Context context, int color, FloatingActionButton button) {
        button.setBackgroundTintList(ColorStateList.valueOf(color));
        if (button.getDrawable() != null) {
            button.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(getTextColor(context, color), PorterDuff.Mode.SRC_IN));
        }
    }

    public static void colorIconButtonInWhite(FloatingActionButton button) {
        int color = Color.parseColor("#ffffff");
        button.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    public static void fixFabCompatPadding(FloatingActionButton button) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            button.setCompatElevation(0);
        }
    }

    public static void shareGame(Context context, Window window, GameService gameService) {
        Log.i(Tags.UTILS_UI, "Share screen");
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
                UiUtils.makeText(context, context.getResources().getString(R.string.share_exception), Toast.LENGTH_LONG).show();
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
                    Log.e(Tags.UTILS_UI, "Exception while sharing", e);
                    UiUtils.makeText(context, context.getResources().getString(R.string.share_exception), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Log.w(Tags.UTILS_UI, "No permission to share");
        }
    }

    public static void shareRecordedGame(Context context, RecordedGameService recordedGameService) {
        Log.i(Tags.UTILS_UI, "Share recorded game");
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File file = ScoreSheetWriter.writeRecordedGame(context, recordedGameService);
            if (file == null) {
                UiUtils.makeText(context, context.getResources().getString(R.string.share_exception), Toast.LENGTH_LONG).show();
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
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.share)));
                } catch (ActivityNotFoundException e) {
                    Log.e(Tags.UTILS_UI, "Exception while sharing recorded game", e);
                    UiUtils.makeText(context, context.getResources().getString(R.string.share_exception), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Log.w(Tags.UTILS_UI, "No permission to share");
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

    public static void navigateToHomeWithDialog(Activity activity, GameService gameService) {
        Log.i(Tags.GAME_UI, "Navigate to home");
        if (gameService.isMatchCompleted()) {
            UiUtils.navigateToHome(activity, false);
        } else if (GameType.TIME.equals(gameService.getGameType())) {
            TimeBasedGameService timeBasedGameService = (TimeBasedGameService) gameService;
            if (timeBasedGameService.getRemainingTime() > 0L) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dialog);
                builder.setTitle(activity.getString(R.string.navigate_home)).setMessage(activity.getString(R.string.navigate_home_question));
                builder.setPositiveButton(android.R.string.yes, (dialog, which) -> UiUtils.navigateToHome(activity, false));
                builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

                AlertDialog alertDialog = builder.show();
                UiUtils.setAlertDialogMessageSize(alertDialog, activity.getResources());
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dialog);
                builder.setTitle(R.string.stop_match_description).setMessage(activity.getString(R.string.confirm_stop_match_question));
                builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Log.i(Tags.GAME_UI, "User accepts to stop");
                    timeBasedGameService.stop();
                    UiUtils.navigateToHome(activity, false);
                });
                builder.setNegativeButton(android.R.string.no, (dialog, which) -> Log.i(Tags.GAME_UI, "User refuses to stop"));
                AlertDialog alertDialog = builder.show();
                UiUtils.setAlertDialogMessageSize(alertDialog, activity.getResources());
            }
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dialog);
            builder.setTitle(activity.getString(R.string.navigate_home)).setMessage(activity.getString(R.string.navigate_home_question));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> UiUtils.navigateToHome(activity, false));
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, activity.getResources());
        }
    }

    public static void navigateToUserSignIn(Activity activity) {
        Intent intent = new Intent(activity, UserSignInActivity.class);
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
            timeoutPlayer.setOnCompletionListener(mediaPlayer -> timeoutPlayer.release());
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
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {});
            AlertDialog alertDialog = builder.show();
            centerAlertDialogMessage(alertDialog);
            setAlertDialogMessageSize(alertDialog, context.getResources());
        } else {
            makeText(context, message, Toast.LENGTH_LONG).show();
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

    public static Toast makeText(Context context, CharSequence text, int duration) {
        View layout = View.inflate(context, R.layout.custom_toast, null);

        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(text);

        int backgroundColor = ContextCompat.getColor(context, R.color.colorPrimaryLight);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setBackgroundTintList(textView, ColorStateList.valueOf(backgroundColor));
        } else {
            textView.getBackground().setColorFilter(new PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.SRC));
        }
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

        Toast toast = new Toast(context.getApplicationContext());
        toast.setDuration(duration);
        toast.setView(layout);

        return toast;
    }

    public static String formatNumberFromLocale(int number) {
        return String.format(Locale.getDefault(), "%d", number);
    }

    public static String formatScoreFromLocale(int leftScore, int rightScore, boolean withSpace) {
        String format = withSpace ? "%d\t-\t%d" : "%d-%d";
        return String.format(Locale.getDefault(), format, leftScore, rightScore);
    }

    public static void setSanctionImage(ImageView imageView, SanctionType sanctionType) {
        switch (sanctionType) {
            case YELLOW:
                imageView.setImageResource(R.drawable.yellow_card);
                break;
            case RED:
                imageView.setImageResource(R.drawable.red_card);
                break;
            case RED_EXPULSION:
                imageView.setImageResource(R.drawable.expulsion_card);
                break;
            case RED_DISQUALIFICATION:
                imageView.setImageResource(R.drawable.disqualification_card);
                break;
            case DELAY_WARNING:
                imageView.setImageResource(R.drawable.delay_warning);
                break;
            case DELAY_PENALTY:
                imageView.setImageResource(R.drawable.delay_penalty);
                break;
        }
    }
}
