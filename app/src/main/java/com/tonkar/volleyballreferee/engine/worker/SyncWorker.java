package com.tonkar.volleyballreferee.engine.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.*;

import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.service.*;

import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(Tags.SYNC_WORKER, "Running sync");
        Context context = getApplicationContext();

        try {
            if (PrefUtils.canSync(context)) {
                new StoredUserManager(context).syncUser();
                new StoredLeaguesManager(context).syncLeagues();
                new StoredRulesManager(context).syncRules();
                new StoredTeamsManager(context).syncTeams();
                new StoredGamesManager(context).syncGames();
                Log.i(Tags.SYNC_WORKER, "Sync completed");
            }
        } catch (Exception e) {
            Log.e(Tags.SYNC_WORKER, String.format("Sync failed with %s", e.getMessage()), e);
            return Result.failure();
        }

        return Result.success();
    }

    public static void enqueue(Context context) {
        if (PrefUtils.canSync(context)) {
            Log.i(Tags.SYNC_WORKER, "Enqueuing sync");

            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build();

            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 1, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build();

            WorkManager workManager = WorkManager.getInstance(context);
            workManager.enqueueUniquePeriodicWork("SYNC_WORKER", ExistingPeriodicWorkPolicy.UPDATE, periodicWorkRequest);
        }
    }
}
