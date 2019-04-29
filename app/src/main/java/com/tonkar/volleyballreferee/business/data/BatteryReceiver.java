package com.tonkar.volleyballreferee.business.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import com.tonkar.volleyballreferee.interfaces.Tags;

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
        try {
            context.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
            Log.e(Tags.BATTERY, e.getMessage(), e);
        }
    }

    @Override
    public void onReceive(Context context, Intent batteryStatus) {
        double scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        double level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        mPercentage = (int) (100.0 * level / scale);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        mCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
    }

    public boolean canPushGameToServer() {
        return mPercentage >= 20 || mCharging;
    }

    public boolean canPushSetToServer() {
        return mPercentage >= 25 || mCharging;
    }
}
