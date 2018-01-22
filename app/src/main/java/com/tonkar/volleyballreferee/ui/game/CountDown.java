package com.tonkar.volleyballreferee.ui.game;

import android.os.CountDownTimer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class CountDown {

    private       long             mDuration;
    private final SimpleDateFormat mTimeoutFormat;
    private       CountDownTimer   mCountDownTimer;

    CountDown(long duration) {
        mTimeoutFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        mDuration = duration;
    }

    long getDuration() {
        return mDuration;
    }

    void setDuration(long duration) {
        mDuration = duration;
    }

    CountDownTimer getCountDownTimer() {
        return mCountDownTimer;
    }

    void setCountDownTimer(CountDownTimer countDownTimer) {
        mCountDownTimer = countDownTimer;
    }

    String format(long millis) {
        return mTimeoutFormat.format(new Date(millis));
    }
}
