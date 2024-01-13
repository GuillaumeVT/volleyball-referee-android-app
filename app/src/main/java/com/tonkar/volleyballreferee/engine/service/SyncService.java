package com.tonkar.volleyballreferee.engine.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.tonkar.volleyballreferee.engine.PrefUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SyncService extends Service {
    private Timer mTimer;

    @Override
    public void onCreate() {
        if (mTimer == null) {
            mTimer = new Timer();
        } else {
            mTimer.cancel();
        }
        mTimer.scheduleAtFixedRate(new SyncTask(), 0, 3600000L);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private class SyncTask extends TimerTask {
        @Override
        public void run() {
            Context context = getApplicationContext();
            if (PrefUtils.canSync(context)) {
                new StoredUserManager(context).syncUser();
                new StoredLeaguesManager(context).syncLeagues();
                new StoredRulesManager(context).syncRules();
                new StoredTeamsManager(context).syncTeams();
                new StoredGamesManager(context).syncGames();
            }
        }
    }
}
