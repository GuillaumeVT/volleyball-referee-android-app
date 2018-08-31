package com.tonkar.volleyballreferee.business.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryReceiver extends BroadcastReceiver {

    private boolean mCharging;
    private int     mPercentage;

    public BatteryReceiver() {
        mCharging = false;
        mPercentage = 0;
    }

    public void register(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);
        onReceive(context, batteryStatus);

        context.registerReceiver(this, iFilter);
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent batteryStatus) {
        double scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        double level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        mPercentage = (int) (100.0 * level / scale);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        mCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
    }

    public boolean canPushGameOnline() {
        return mPercentage >= 20 || mCharging;
    }

    public boolean canPushSetOnline() {
        return mPercentage >= 30 || mCharging;
    }
}
