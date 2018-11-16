package com.tonkar.volleyballreferee.ui.game;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGamesService;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Random;

public class GameActionMenu extends BottomSheetDialogFragment {

    private Activity             mActivity;
    private GameService          mGameService;
    private RecordedGamesService mRecordedGamesService;
    private Random               mRandom;

    public static GameActionMenu newInstance() {
        GameActionMenu fragment = new GameActionMenu();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.GAME_UI, "Create game action menu fragment");
        View view = inflater
                .cloneInContext(new ContextThemeWrapper(inflater.getContext(), R.style.AppTheme_Dialog))
                .inflate(R.layout.game_action_menu, container, false);

        Context context = inflater.getContext();

        mActivity = getActivity();
        mGameService = ServicesProvider.getInstance().getGameService();
        mRecordedGamesService = ServicesProvider.getInstance().getRecordedGamesService(context);

        if (mActivity != null && mGameService != null && mRecordedGamesService != null) {
            mRandom = new Random();
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

            TextView navigateHomeText = view.findViewById(R.id.action_navigate_home);
            SwitchCompat indexGameText = view.findViewById(R.id.action_index_game);
            TextView shareGameText = view.findViewById(R.id.action_share_game);
            TextView tossCoinText = view.findViewById(R.id.action_toss_coin);
            TextView resetSetText = view.findViewById(R.id.action_reset_set);
            SwitchCompat keepScreenOnSwitch = view.findViewById(R.id.keep_screen_on);
            SwitchCompat interactiveNotificationSwitch = view.findViewById(R.id.interactive_notification);

            keepScreenOnSwitch.setChecked(sharedPreferences.getBoolean(PrefUtils.PREF_KEEP_SCREEN_ON, false));
            interactiveNotificationSwitch.setChecked(sharedPreferences.getBoolean(PrefUtils.PREF_INTERACTIVE_NOTIFICATIONS, false));

            if (mGameService.isMatchCompleted()) {
                tossCoinText.setVisibility(View.GONE);
                indexGameText.setVisibility(View.GONE);
                resetSetText.setVisibility(View.GONE);
                keepScreenOnSwitch.setVisibility(View.GONE);
                interactiveNotificationSwitch.setVisibility(View.GONE);
            } else if (GameType.TIME.equals(mGameService.getGameType())) {
                resetSetText.setVisibility(View.GONE);
            }

            if (PrefUtils.isSyncOn(context)) {
                indexGameText.setChecked(mGameService.isIndexed());
            } else {
                indexGameText.setVisibility(View.GONE);
            }

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                shareGameText.setVisibility(View.GONE);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                setIcon(navigateHomeText, R.drawable.ic_home);
                setIcon(indexGameText, R.drawable.ic_private);
                setIcon(shareGameText, R.drawable.ic_share_menu);
                setIcon(tossCoinText, R.drawable.ic_coin);
                setIcon(resetSetText, R.drawable.ic_reset_menu);
                setIcon(keepScreenOnSwitch, R.drawable.ic_screen_on);
                setIcon(interactiveNotificationSwitch, R.drawable.ic_notification);
            }

            colorIcon(context, navigateHomeText, R.color.colorPrimary);
            colorIcon(context, indexGameText, R.color.colorPrimary);
            colorIcon(context, shareGameText, R.color.colorPrimary);
            colorIcon(context, tossCoinText, R.color.colorPrimary);
            colorIcon(context, resetSetText, R.color.colorPrimary);

            navigateHomeText.setOnClickListener(textView -> UiUtils.navigateToHomeWithDialog(mActivity, mGameService));
            indexGameText.setOnCheckedChangeListener((button, isChecked) -> toggleIndexed());
            shareGameText.setOnClickListener(textView -> share());
            tossCoinText.setOnClickListener(textView -> tossACoin());
            resetSetText.setOnClickListener(textView -> resetCurrentSetWithDialog());
            keepScreenOnSwitch.setOnCheckedChangeListener((button, isChecked) -> {
                sharedPreferences.edit().putBoolean(PrefUtils.PREF_KEEP_SCREEN_ON, isChecked).apply();
                mActivity.recreate();
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

    private void toggleIndexed() {
        Log.i(Tags.GAME_UI, "Toggle indexed");
        mGameService.setIndexed(!mGameService.isIndexed());
    }

    private void share() {
        Log.i(Tags.GAME_UI, "Share game");
        if (mGameService.isMatchCompleted()) {
            UiUtils.shareRecordedGame(mActivity, mRecordedGamesService.getRecordedGameService(mGameService.getGameDate()));
        } else {
            UiUtils.shareGame(mActivity, mActivity.getWindow(), mGameService);
        }
        dismiss();
    }

    private void tossACoin() {
        Log.i(Tags.GAME_UI, "Toss a coin");
        final String tossResult = mRandom.nextBoolean() ? getResources().getString(R.string.toss_heads) : getResources().getString(R.string.toss_tails);
        UiUtils.makeText(mActivity, tossResult, Toast.LENGTH_LONG).show();
        dismiss();
    }

    private void resetCurrentSetWithDialog() {
        Log.i(Tags.GAME_UI, "Reset current set");
        if (!mGameService.isMatchCompleted()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppTheme_Dialog);
            builder.setTitle(getResources().getString(R.string.reset_set)).setMessage(getResources().getString(R.string.reset_set_question));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                mGameService.resetCurrentSet();
                mActivity.recreate();
            });
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        }
        dismiss();
    }

    private void colorIcon(Context context, TextView textView, @ColorRes int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    private void setIcon(TextView textView, @DrawableRes int drawableId) {
        textView.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
    }
}
