package com.tonkar.volleyballreferee.ui.util;

import android.app.Activity;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.media.*;
import android.os.Handler;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.MainActivity;

import java.util.*;

public class UiUtils {

    private static final Random RANDOM = new Random();

    public static int getRandomShirtColor(Context context) {
        String[] colorsStr = context.getResources().getStringArray(R.array.shirt_colors);
        String randomColorStr = colorsStr[RANDOM.nextInt(colorsStr.length - 1)];
        return Color.parseColor(randomColorStr);
    }

    public static void styleBaseTeamButton(Context context, IBaseTeam teamService, TeamType teamType, MaterialButton button) {
        colorTeamButton(context, teamService.getTeamColor(teamType), button);
        button.setPaintFlags(button.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
    }

    public static void styleIndoorTeamButton(Context context,
                                             IClassicTeam indoorTeam,
                                             TeamType teamType,
                                             int number,
                                             MaterialButton button) {
        button.setPaintFlags(button.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        if (number < 0) {
            colorTeamButton(context, indoorTeam.getTeamColor(teamType), button);
        } else if (indoorTeam.isLibero(teamType, number)) {
            colorTeamButton(context, indoorTeam.getLiberoColor(teamType), button);
        } else {
            colorTeamButton(context, indoorTeam.getTeamColor(teamType), button);
        }

        if (indoorTeam.isGameCaptain(teamType, number)) {
            button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    public static void styleTeamButton(Context context, IBaseTeam teamService, TeamType teamType, int number, MaterialButton button) {
        button.setPaintFlags(button.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        if (number < 0) {
            colorTeamButton(context, teamService.getTeamColor(teamType), button);
        } else if (teamService.isLibero(teamType, number)) {
            colorTeamButton(context, teamService.getLiberoColor(teamType), button);
        } else {
            colorTeamButton(context, teamService.getTeamColor(teamType), button);
        }

        if (teamService.isCaptain(teamType, number)) {
            button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    public static void styleTeamText(Context context, IBaseTeam teamService, TeamType teamType, int number, TextView text) {
        text.setPaintFlags(text.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        if (number < 0) {
            colorTeamText(context, teamService.getTeamColor(teamType), text);
        } else if (teamService.isLibero(teamType, number)) {
            colorTeamText(context, teamService.getLiberoColor(teamType), text);
        } else {
            colorTeamText(context, teamService.getTeamColor(teamType), text);
        }

        if (teamService.isCaptain(teamType, number)) {
            text.setPaintFlags(text.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    public static void colorTeamButton(Context context, @ColorInt int color, MaterialButton button) {
        int textColor = getTextColor(context, color);
        button.setTextColor(textColor);
        button.setBackgroundTintList(ColorStateList.valueOf(color));
        if (button.getIcon() != null) {
            button.setIconTint(ColorStateList.valueOf(textColor));
        }
    }

    public static void colorTeamButton(Context context, @ColorInt int color, @DrawableRes int drawable, MaterialButton button) {
        button.setIconResource(drawable);
        colorTeamButton(context, color, button);
    }

    public static void colorTeamText(Context context, @ColorInt int color, TextView text) {
        ViewCompat.setBackgroundTintList(text, ColorStateList.valueOf(color));
        text.setTextColor(getTextColor(context, color));
    }

    public static int getTextColor(Context context, int backgroundColor) {
        int textColor;

        double a = 1 - (0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(
                backgroundColor)) / 255;

        if (a < 0.5) {
            textColor = ContextCompat.getColor(context, R.color.colorOnLightSurface);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorOnDarkSurface);
        }

        return textColor;
    }

    private static void colorIconButton(Context context, ExtendedFloatingActionButton button, @DrawableRes int drawable) {
        button.setIconResource(drawable);
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorFab)));
        button
                .getIcon()
                .mutate()
                .setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorOnFab), PorterDuff.Mode.SRC_IN));
    }

    public static void colorPlusIconButton(Context context, ExtendedFloatingActionButton button) {
        colorIconButton(context, button, R.drawable.ic_plus);
    }

    public static void colorCloseIconButton(Context context, ExtendedFloatingActionButton button) {
        colorIconButton(context, button, R.drawable.ic_close);
    }

    public static void colorChipIcon(Context context, @ColorRes int color, @DrawableRes int drawable, ImageView imageView) {
        imageView.setImageResource(drawable);
        imageView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, color)));
        imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorOnLightSurface)));
    }

    public static void navigateBackToHome(Activity originActivity) {
        navigateToMain(originActivity, R.id.home_fragment);
        animateBackward(originActivity);
    }

    public static void navigateToMainWithDialog(Activity originActivity, IGame game) {
        Log.i(Tags.GAME_UI, "Navigate to home");
        if (game.isMatchCompleted()) {
            UiUtils.navigateBackToHome(originActivity);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(originActivity, R.style.AppTheme_Dialog);
            builder
                    .setTitle(originActivity.getString(R.string.navigate_home))
                    .setMessage(originActivity.getString(R.string.navigate_home_question));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> UiUtils.navigateBackToHome(originActivity));
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, originActivity.getResources());
        }
    }

    public static void navigateToStoredGameAfterDelay(Activity activity, long delay) {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            navigateToMain(activity, R.id.stored_games_list_fragment);
            UiUtils.animateForward(activity);
        }, delay);
    }

    public static final String INIT_FRAGMENT_ID = "initFragmentId";

    public static void navigateToMain(Activity originActivity, @IdRes int initFragmentId) {
        Intent intent = new Intent(originActivity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(INIT_FRAGMENT_ID, initFragmentId);
        originActivity.startActivity(intent);
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

    public static void playNotificationSound(Context context) {
        final MediaPlayer timeoutPlayer = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (timeoutPlayer != null) {
            timeoutPlayer.setLooping(false);
            timeoutPlayer.setOnCompletionListener(mediaPlayer -> timeoutPlayer.release());
            timeoutPlayer.start();
        }
    }

    public static String getPositionTitle(Context context, PositionType positionType) {
        return switch (positionType) {
            case POSITION_1 -> context.getString(R.string.position_1_title);
            case POSITION_2 -> context.getString(R.string.position_2_title);
            case POSITION_3 -> context.getString(R.string.position_3_title);
            case POSITION_4 -> context.getString(R.string.position_4_title);
            case POSITION_5 -> context.getString(R.string.position_5_title);
            case POSITION_6 -> context.getString(R.string.position_6_title);
            default -> "";
        };
    }

    public static void showNotification(View view, String message) {
        if (view != null) {
            Context context = view.getContext();
            Snackbar
                    .make(view, message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, button -> {
                    })
                    .setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setTextColor(ContextCompat.getColor(context, R.color.colorOnSurface))
                    .setBackgroundTint(ContextCompat.getColor(context, R.color.colorPrimaryContainer))
                    .show();
        }
    }

    public static void setAlertDialogMessageSize(AlertDialog alertDialog, Resources resources) {
        TextView textView = alertDialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.default_text_size));
        }
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        View layout = View.inflate(context, R.layout.custom_toast, null);

        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(text);

        int backgroundColor = ContextCompat.getColor(context, R.color.colorPrimaryContainer);
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

        int backgroundColor = ContextCompat.getColor(context, R.color.colorErrorContainer);
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
        if (View.LAYOUT_DIRECTION_LTR == TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())) {
            return String.format(Locale.getDefault(), format, leftScore, rightScore);
        } else {
            return String.format(Locale.getDefault(), format, rightScore, leftScore);
        }
    }

    public static void setSanctionImage(ImageView imageView, SanctionType sanctionType) {
        switch (sanctionType) {
            case YELLOW -> imageView.setImageResource(R.drawable.yellow_card);
            case RED -> imageView.setImageResource(R.drawable.red_card);
            case RED_EXPULSION -> imageView.setImageResource(R.drawable.expulsion_card);
            case RED_DISQUALIFICATION -> imageView.setImageResource(R.drawable.disqualification_card);
            case DELAY_WARNING -> imageView.setImageResource(R.drawable.delay_warning);
            case DELAY_PENALTY -> imageView.setImageResource(R.drawable.delay_penalty);
        }
    }

    public static void updateToolbarLogo(Toolbar toolbar, GameType gameType, UsageType usageType) {
        ImageView imageView = toolbar.findViewById(R.id.toolbar_logo);
        switch (gameType) {
            case INDOOR -> {
                if (UsageType.NORMAL.equals(usageType)) {
                    imageView.setImageResource(R.drawable.ic_6x6);
                } else {
                    imageView.setImageResource(R.drawable.ic_volleyball);
                }
            }
            case INDOOR_4X4 -> imageView.setImageResource(R.drawable.ic_4x4);
            case BEACH -> imageView.setImageResource(R.drawable.ic_beach);
            case SNOW -> imageView.setImageResource(R.drawable.ic_snow);
        }
        imageView.setVisibility(View.VISIBLE);
    }

    public static void addExtendShrinkListener(View relatedView, ExtendedFloatingActionButton button) {
        relatedView.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> button.extend();
                default -> button.shrink();
            }
            return false;
        });
    }
}
