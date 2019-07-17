package com.tonkar.volleyballreferee.engine.stored;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
import com.tonkar.volleyballreferee.engine.PrefUtils;

import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void syncAll(Context context) {
        if (PrefUtils.canSync(context)) {
            new StoredUserManager(context).syncUser();
            new StoredLeaguesManager(context).syncLeagues();
            new StoredRulesManager(context).syncRules();
            new StoredTeamsManager(context).syncTeams();
            new StoredGamesManager(context).syncGames();
        }
    }

    @Override
    public @NonNull
    Result doWork() {
        syncAll(getApplicationContext());
        return Result.success();
    }

    private static PeriodicWorkRequest syncRequest() {
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();

        return new PeriodicWorkRequest.Builder(SyncWorker.class, 2, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
    }

    static void enqueue(Context context) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("vbr.sync", ExistingPeriodicWorkPolicy.KEEP, syncRequest());
    }
}