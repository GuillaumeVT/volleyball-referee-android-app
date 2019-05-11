package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
import com.tonkar.volleyballreferee.business.PrefUtils;

import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void syncAll(Context context) {
        if (PrefUtils.canSync(context)) {
            new StoredUser(context).syncUser();
            new StoredLeagues(context).syncLeagues();
            new StoredRules(context).syncRules();
            new StoredTeams(context).syncTeams();
            new StoredGames(context).syncGames();
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

    static void enqueue() {
        WorkManager.getInstance().enqueueUniquePeriodicWork("vbr.sync", ExistingPeriodicWorkPolicy.KEEP, syncRequest());
    }
}