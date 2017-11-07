package com.tonkar.volleyballreferee.ui.game;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CountDownDialogFragment extends DialogFragment {

    // Duration is static in order to continue the countdown after a screen rotation
    private        MediaPlayer      mMediaPlayer;
    private        int              mDuration;
    private        SimpleDateFormat mTimeoutFormat;
    private        CountDownTimer   mCountDownTimer;
    private        TextView         mCountDownView;

    public static CountDownDialogFragment newInstance(int duration, String title) {
        CountDownDialogFragment fragment = new CountDownDialogFragment();
        Bundle args = new Bundle();
        args.putInt("duration", duration);
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mTimeoutFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mMediaPlayer = MediaPlayer.create(getActivity(), notification);

        if (savedInstanceState == null) {
            mDuration = getArguments().getInt("duration");
            mMediaPlayer.start();
        }
        else {
            mDuration = savedInstanceState.getInt("saved_duration");
        }

        String title = getArguments().getString("title");

        mCountDownView = new TextView(getActivity());
        mCountDownView.setTextAppearance(getActivity(), android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Headline);
        mCountDownView.setTextSize(72);
        mCountDownView.setGravity(Gravity.CENTER_HORIZONTAL);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(title).setView(mCountDownView).setCancelable(false);
        builder.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i("VBR-GameActivity", "User cancels the countdown");
                mCountDownTimer.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();

        mCountDownTimer = new CountDownTimer(mDuration * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                mDuration = (int) (millisUntilFinished / 1000);
                mCountDownView.setText(mTimeoutFormat.format(new Date(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                mMediaPlayer.start();
                alertDialog.dismiss();
            }
        };

        mCountDownTimer.start();

        setCancelable(false);

        return alertDialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("saved_duration", mDuration);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCountDownTimer.cancel();
    }
}
