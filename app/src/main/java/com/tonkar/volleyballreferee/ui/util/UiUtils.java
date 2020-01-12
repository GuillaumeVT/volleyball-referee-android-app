package com.tonkar.volleyballreferee.ui.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AnimRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.BuildConfig;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.game.ITimeBasedGame;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.stored.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.engine.team.IClassicTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.MainActivity;
import com.tonkar.volleyballreferee.ui.stored.game.StoredGamesListActivity;
import com.tonkar.volleyballreferee.ui.user.UserActivity;

import java.util.Locale;
import java.util.Random;

public class UiUtils {

    private static final Random RANDOM = new Random();

    public static int getRandomShirtColor(Context context) {
        String[] colorsStr = context.getResources().getStringArray(R.array.shirt_colors);
        String randomColorStr = colorsStr[RANDOM.nextInt(colorsStr.length - 1)];
        return Color.parseColor(randomColorStr);
    }

    public static void styleBaseTeamButton(Context context, IBaseTeam teamService, TeamType teamType, MaterialButton button) {
        colorTeamButton(context, teamService.getTeamColor(teamType), button);
        button.setPaintFlags(button.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
    }

    public static void styleIndoorTeamButton(Context context, IClassicTeam indoorTeam, TeamType teamType, int number, MaterialButton button) {
        button.setPaintFlags(button.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));

        if (indoorTeam.isLibero(teamType, number)) {
            colorTeamButton(context, indoorTeam.getLiberoColor(teamType), button);
        } else {
            colorTeamButton(context, indoorTeam.getTeamColor(teamType), button);
            if (indoorTeam.isCaptain(teamType, number) || indoorTeam.isActingCaptain(teamType, number)) {
                button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        }
    }

    public static void styleTeamButton(Context context, IBaseTeam teamService, TeamType teamType, int number, MaterialButton button) {
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

    public static void styleTeamText(Context context, IBaseTeam teamService, TeamType teamType, int number, TextView text) {
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
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            button.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        }
        if (button.getIcon() != null) {
            button.setIconTint(ColorStateList.valueOf(textColor));
        }
    }

    public static void colorTeamText(Context context, int color, TextView text) {
        ViewCompat.setBackgroundTintList(text, ColorStateList.valueOf(color));
        text.setTextColor(getTextColor(context, color));
    }

    public static int getTextColor(Context context, int backgroundColor) {
        int textColor;

        double a = 1 - ( 0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255;

        if (a < 0.5) {
            textColor = ContextCompat.getColor(context, R.color.colorOnLightSurface);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorOnDarkSurface);
        }

        return textColor;
    }

    public static void colorTeamIconButton(Context context, @ColorInt int color, FloatingActionButton button) {
        button.setBackgroundTintList(ColorStateList.valueOf(color));
        if (button.getDrawable() != null) {
            button.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(getTextColor(context, color), PorterDuff.Mode.SRC_IN));
        }
    }

    public static void colorTeamIconButton(Context context, @ColorInt int color, @DrawableRes int drawable, FloatingActionButton button) {
        button.setImageResource(drawable);
        colorTeamIconButton(context, color, button);
    }

    public static void colorPlusIconButton(Context context, FloatingActionButton button) {
        button.setImageResource(R.drawable.ic_plus);
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorFab)));
        button.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorOnFab), PorterDuff.Mode.SRC_IN));
    }

    public static void colorCloseIconButton(Context context, FloatingActionButton button) {
        button.setImageResource(R.drawable.ic_close);
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorFab)));
        button.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorOnFab), PorterDuff.Mode.SRC_IN));
    }

    public static void colorIconButtonInWhite(FloatingActionButton button) {
        int color = Color.parseColor("#ffffff");
        button.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    public static void colorChipIcon(Context context, @ColorRes int color, @DrawableRes int drawable, Chip chip) {
        chip.setChipIconResource(drawable);
        chip.setChipBackgroundColorResource(color);
        chip.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorOnLightSurface), PorterDuff.Mode.SRC_IN));
    }

    public static void fixFabCompatPadding(FloatingActionButton button) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            button.setCompatElevation(0);
        }
    }

    public static void shareGameLink(Context context, IStoredGame storedGame) {
        if (PrefUtils.canSync(context)){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, storedGame.getGameSummary() + "\n" + String.format("%s/view/game/%s", BuildConfig.SERVER_ADDRESS, storedGame.getId()));
            intent.setType("text/plain");

            try {
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
            } catch (ActivityNotFoundException e) {
                Log.e(Tags.UTILS_UI, "Exception while sharing stored game", e);
                UiUtils.makeErrorText(context, context.getString(R.string.share_exception), Toast.LENGTH_LONG).show();
            }

        }
    }

    public static void navigateToHome(Activity activity, @AnimRes int animIn, @AnimRes int animOut) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(animIn, animOut);
    }

    public static void navigateToHome(Activity activity) {
        navigateToHome(activity, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public static void navigateToHomeWithDialog(Activity activity, IGame game) {
        Log.i(Tags.GAME_UI, "Navigate to home");
        if (game.isMatchCompleted()) {
            UiUtils.navigateToHome(activity);
        } else if (GameType.TIME.equals(game.getKind())) {
            ITimeBasedGame timeBasedGameService = (ITimeBasedGame) game;
            if (timeBasedGameService.getRemainingTime() > 0L) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dialog);
                builder.setTitle(activity.getString(R.string.navigate_home)).setMessage(activity.getString(R.string.navigate_home_question));
                builder.setPositiveButton(android.R.string.yes, (dialog, which) -> UiUtils.navigateToHome(activity));
                builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

                AlertDialog alertDialog = builder.show();
                UiUtils.setAlertDialogMessageSize(alertDialog, activity.getResources());
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dialog);
                builder.setTitle(R.string.stop_match_description).setMessage(activity.getString(R.string.confirm_stop_match_question));
                builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Log.i(Tags.GAME_UI, "User accepts to stop");
                    timeBasedGameService.stop();
                    UiUtils.navigateToHome(activity);
                });
                builder.setNegativeButton(android.R.string.no, (dialog, which) -> Log.i(Tags.GAME_UI, "User refuses to stop"));
                AlertDialog alertDialog = builder.show();
                UiUtils.setAlertDialogMessageSize(alertDialog, activity.getResources());
            }
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dialog);
            builder.setTitle(activity.getString(R.string.navigate_home)).setMessage(activity.getString(R.string.navigate_home_question));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> UiUtils.navigateToHome(activity));
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, activity.getResources());
        }
    }

    public static void navigateToUserSignIn(Activity activity) {
        Intent intent = new Intent(activity, UserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        UiUtils.animateForward(activity);
    }

    public static void navigateToStoredGameAfterDelay(Activity activity, long delay) {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(activity, StoredGamesListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            UiUtils.animateForward(activity);
        }, delay);
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

    public static void animateForward(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public static void animateBackward(Activity activity) {
        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public static void animateCreate(Activity activity) {
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void animateNavigationView(FragmentTransaction transaction) {
        transaction.setCustomAnimations(R.anim.push_down_in, R.anim.push_down_out);
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
                title = context.getString(R.string.position_1_title);
                break;
            case POSITION_2:
                title = context.getString(R.string.position_2_title);
                break;
            case POSITION_3:
                title = context.getString(R.string.position_3_title);
                break;
            case POSITION_4:
                title = context.getString(R.string.position_4_title);
                break;
            case POSITION_5:
                title = context.getString(R.string.position_5_title);
                break;
            case POSITION_6:
                title = context.getString(R.string.position_6_title);
                break;
        }

        return title;
    }

    public static void showNotification(Context context, String message) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean interactiveNotification = sharedPreferences.getBoolean(PrefUtils.PREF_INTERACTIVE_NOTIFICATIONS, false);

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

        int backgroundColor = ContextCompat.getColor(context, R.color.colorPrimaryVariant);
        ViewCompat.setBackgroundTintList(textView, ColorStateList.valueOf(backgroundColor));
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

        Toast toast = new Toast(context.getApplicationContext());
        toast.setDuration(duration);
        toast.setView(layout);

        return toast;
    }

    public static Toast makeErrorText(Context context, CharSequence text, int duration) {
        View layout = View.inflate(context, R.layout.custom_toast, null);

        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(text);

        int backgroundColor = ContextCompat.getColor(context, R.color.colorErrorVariant);
        ViewCompat.setBackgroundTintList(textView, ColorStateList.valueOf(backgroundColor));
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorError));

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

    public static void updateToolbarLogo(Toolbar toolbar, GameType gameType, UsageType usageType) {
        ImageView imageView = toolbar.findViewById(R.id.toolbar_logo);
        switch (gameType) {
            case INDOOR:
                if (UsageType.NORMAL.equals(usageType)) {
                    imageView.setImageResource(R.drawable.ic_6x6);
                } else {
                    imageView.setImageResource(R.drawable.ic_volleyball);
                }
                break;
            case INDOOR_4X4:
                imageView.setImageResource(R.drawable.ic_4x4);
                break;
            case BEACH:
                imageView.setImageResource(R.drawable.ic_beach);
                break;
            case SNOW:
                imageView.setImageResource(R.drawable.ic_snow);
                break;
            case TIME:
                imageView.setImageResource(R.drawable.ic_time_based);
                break;
        }
        imageView.setVisibility(View.VISIBLE);
    }

}
