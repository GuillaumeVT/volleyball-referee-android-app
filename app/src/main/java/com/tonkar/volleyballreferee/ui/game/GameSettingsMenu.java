package com.tonkar.volleyballreferee.ui.game;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.Tags;

public class GameSettingsMenu extends BottomSheetDialogFragment {

    public static GameSettingsMenu newInstance() {
        GameSettingsMenu fragment = new GameSettingsMenu();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.GAME_UI, "Create game settings menu fragment");
        View view = inflater
                .cloneInContext(new ContextThemeWrapper(inflater.getContext(), R.style.AppTheme_Dialog))
                .inflate(R.layout.game_settings_menu, container, false);

        Context context = inflater.getContext();

        final Activity activity = getActivity();

        if (activity != null) {
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

            SwitchCompat keepScreenOnSwitch = view.findViewById(R.id.keep_screen_on);
            SwitchCompat interactiveNotificationSwitch = view.findViewById(R.id.interactive_notification);

            keepScreenOnSwitch.setChecked(sharedPreferences.getBoolean(PrefUtils.PREF_KEEP_SCREEN_ON, false));
            interactiveNotificationSwitch.setChecked(sharedPreferences.getBoolean(PrefUtils.PREF_INTERACTIVE_NOTIFICATIONS, false));

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                setIcon(keepScreenOnSwitch, R.drawable.ic_keep_screen_on_menu);
                setIcon(interactiveNotificationSwitch, R.drawable.ic_interactive_notification_menu);
            }

            colorIcon(context, keepScreenOnSwitch, R.color.colorPrimary);
            colorIcon(context, interactiveNotificationSwitch, R.color.colorPrimary);

            keepScreenOnSwitch.setOnCheckedChangeListener((button, isChecked) -> {
                sharedPreferences.edit().putBoolean(PrefUtils.PREF_KEEP_SCREEN_ON, isChecked).apply();
                activity.recreate();
            });
            interactiveNotificationSwitch.setOnCheckedChangeListener((button, isChecked) -> {
                sharedPreferences.edit().putBoolean(PrefUtils.PREF_INTERACTIVE_NOTIFICATIONS, isChecked).apply();
            });
        }

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog tmpDialog = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = tmpDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet).setHideable(true);
        });
        return bottomSheetDialog;
    }

    private void colorIcon(Context context, SwitchCompat switchCompat, @ColorRes int color) {
        for (Drawable drawable : switchCompat.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    private void setIcon(SwitchCompat switchCompat, @DrawableRes int drawableId) {
        switchCompat.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
    }

}
