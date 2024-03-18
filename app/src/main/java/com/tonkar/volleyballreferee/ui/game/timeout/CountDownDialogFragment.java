package com.tonkar.volleyballreferee.ui.game.timeout;

import android.app.Dialog;
import android.os.*;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class CountDownDialogFragment extends DialogFragment {

    private CountDown mCountDown;
    private TextView  mCountDownView;

    public static CountDownDialogFragment newInstance(int duration, String title) {
        CountDownDialogFragment fragment = new CountDownDialogFragment();
        Bundle args = new Bundle();
        args.putLong("timeout_duration", 1000L * (long) duration);
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        long duration;

        if (savedInstanceState == null) {
            duration = requireArguments().getLong("timeout_duration");
            UiUtils.playNotificationSound(requireActivity());
        } else {
            duration = savedInstanceState.getLong("saved_timeout_duration");
        }

        mCountDown = new CountDown(duration);

        String title = requireArguments().getString("title");

        mCountDownView = new TextView(requireActivity());
        mCountDownView.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Headline);
        mCountDownView.setTextSize(72);
        mCountDownView.setGravity(Gravity.CENTER_HORIZONTAL);

        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(title).setView(mCountDownView).setCancelable(false);
        builder.setNegativeButton(R.string.skip, (dialog, which) -> {
            Log.i(Tags.TIMEOUT, "User cancels the countdown");
            mCountDown.getCountDownTimer().cancel();
        });
        builder.setNeutralButton(R.string.run_background, (dialog, which) -> {
            Log.i(Tags.TIMEOUT, "User runs the countdown in background");
            mCountDown.getCountDownTimer().cancel();
            if (isAdded() && requireActivity() instanceof GameActivity activity) {
                activity.startToolbarCountDown(mCountDown.getDuration());
            }
        });
        final AlertDialog alertDialog = builder.create();

        mCountDown.setCountDownTimer(new CountDownTimer(mCountDown.getDuration(), 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                mCountDown.setDuration(millisUntilFinished);
                mCountDownView.setText(mCountDown.format(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                UiUtils.playNotificationSound(requireActivity());
                alertDialog.dismiss();
            }
        });

        mCountDown.getCountDownTimer().start();

        setCancelable(false);

        return alertDialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("saved_timeout_duration", mCountDown.getDuration());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCountDown.getCountDownTimer().cancel();
    }

}
