package com.tonkar.volleyballreferee.ui.game;

import android.app.*;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.*;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.service.StoredGamesService;
import com.tonkar.volleyballreferee.ui.interfaces.*;
import com.tonkar.volleyballreferee.ui.util.*;

import java.util.Random;

public class GameActionMenu extends BottomSheetDialogFragment implements GameServiceHandler, StoredGamesServiceHandler {

    private Activity           mActivity;
    private IGame              mGame;
    private StoredGamesService mStoredGamesService;
    private Random             mRandom;

    public static GameActionMenu newInstance() {
        GameActionMenu fragment = new GameActionMenu();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.GAME_UI, "Create game action menu fragment");
        View view = inflater
                .cloneInContext(new ContextThemeWrapper(inflater.getContext(), R.style.AppTheme_Dialog))
                .inflate(R.layout.game_action_menu, container, false);

        mActivity = requireActivity();

        if (mGame != null && mStoredGamesService != null) {
            mRandom = new Random();
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

            TextView navigateHomeText = view.findViewById(R.id.action_navigate_home);
            TextView tossCoinText = view.findViewById(R.id.action_toss_coin);
            TextView resetSetText = view.findViewById(R.id.action_reset_set);
            SwitchCompat keepScreenOnSwitch = view.findViewById(R.id.keep_screen_on);
            Spinner nightModeSpinner = view.findViewById(R.id.night_mode_spinner);

            keepScreenOnSwitch.setChecked(sharedPreferences.getBoolean(PrefUtils.PREF_KEEP_SCREEN_ON, false));

            if (mGame.isMatchCompleted()) {
                tossCoinText.setVisibility(View.GONE);
                resetSetText.setVisibility(View.GONE);
                keepScreenOnSwitch.setVisibility(View.GONE);
            }

            navigateHomeText.setOnClickListener(textView -> UiUtils.navigateToMainWithDialog(mActivity, mGame));
            tossCoinText.setOnClickListener(textView -> tossACoin());
            resetSetText.setOnClickListener(textView -> resetCurrentSetWithDialog());
            keepScreenOnSwitch.setOnCheckedChangeListener((button, isChecked) -> {
                sharedPreferences.edit().putBoolean(PrefUtils.PREF_KEEP_SCREEN_ON, isChecked).apply();
                mActivity.recreate();
            });
            StringArrayAdapter nightModeSpinnerAdapter = new StringArrayAdapter(getContext(), inflater,
                                                                                getResources().getStringArray(R.array.night_mode_entries),
                                                                                getResources().getStringArray(R.array.night_mode_values));
            nightModeSpinner.setAdapter(nightModeSpinnerAdapter);
            nightModeSpinner.setSelection(nightModeSpinnerAdapter.getPosition(PrefUtils.getNightMode(mActivity)));
            nightModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    PrefUtils.setNightMode(mActivity, nightModeSpinnerAdapter.getItem(index));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }

        return view;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog tmpDialog = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = tmpDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet).setHideable(true);
        });
        return bottomSheetDialog;
    }

    private void tossACoin() {
        Log.i(Tags.GAME_UI, "Toss a coin");
        final String tossResult = mRandom.nextBoolean() ? getString(R.string.toss_heads) : getString(R.string.toss_tails);
        UiUtils.makeText(mActivity, tossResult, Toast.LENGTH_LONG).show();
        dismiss();
    }

    private void resetCurrentSetWithDialog() {
        Log.i(Tags.GAME_UI, "Reset current set");
        if (!mGame.isMatchCompleted()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppTheme_Dialog);
            builder.setTitle(getString(R.string.reset_set)).setMessage(getString(R.string.reset_set_question));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                mGame.resetCurrentSet();
                mActivity.recreate();
            });
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        }
        dismiss();
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
    }

    @Override
    public void setStoredGamesService(StoredGamesService storedGamesService) {
        mStoredGamesService = storedGamesService;
    }

}
