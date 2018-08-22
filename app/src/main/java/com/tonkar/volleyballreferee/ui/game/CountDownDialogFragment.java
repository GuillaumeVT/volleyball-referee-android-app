package com.tonkar.volleyballreferee.ui.game;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long duration;

        if (savedInstanceState == null) {
            duration = getArguments().getLong("timeout_duration");
            UiUtils.playNotificationSound(getActivity());
        }
        else {
            duration = savedInstanceState.getLong("saved_timeout_duration");
        }

        mCountDown = new CountDown(duration);

        String title = getArguments().getString("title");

        mCountDownView = new TextView(getActivity());
        mCountDownView.setTextAppearance(getActivity(), androidx.appcompat.R.style.TextAppearance_AppCompat_Headline);
        mCountDownView.setTextSize(72);
        mCountDownView.setGravity(Gravity.CENTER_HORIZONTAL);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(title).setView(mCountDownView).setCancelable(false);
        builder.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(Tags.TIMEOUT, "User cancels the countdown");
                mCountDown.getCountDownTimer().cancel();
            }
        });
        builder.setNeutralButton(R.string.run_background, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(Tags.TIMEOUT, "User runs the countdown in background");
                mCountDown.getCountDownTimer().cancel();
                if (isAdded() && getActivity() instanceof GameActivity) {
                    GameActivity activity = (GameActivity) getActivity();
                    activity.startToolbarCountDown(mCountDown.getDuration());
                }
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
                UiUtils.playNotificationSound(getActivity());
                alertDialog.dismiss();
            }
        });

        mCountDown.getCountDownTimer().start();

        setCancelable(false);

        return alertDialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("saved_timeout_duration", mCountDown.getDuration());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCountDown.getCountDownTimer().cancel();
    }

}
