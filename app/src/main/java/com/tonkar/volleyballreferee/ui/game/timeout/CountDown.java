package com.tonkar.volleyballreferee.ui.game.timeout;

import android.os.CountDownTimer;

import java.text.SimpleDateFormat;
import java.util.*;

public class CountDown {

    private       long             mDuration;
    private final SimpleDateFormat mTimeoutFormat;
    private       CountDownTimer   mCountDownTimer;

    public CountDown(long duration) {
        mTimeoutFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        mDuration = duration;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public CountDownTimer getCountDownTimer() {
        return mCountDownTimer;
    }

    public void setCountDownTimer(CountDownTimer countDownTimer) {
        mCountDownTimer = countDownTimer;
    }

    public String format(long millis) {
        return mTimeoutFormat.format(new Date(millis));
    }
}
