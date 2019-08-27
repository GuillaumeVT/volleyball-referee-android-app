package com.tonkar.volleyballreferee.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.JsonParseException;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.JsonIOUtils;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesManager;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesService;
import com.tonkar.volleyballreferee.engine.stored.api.ApiCount;
import com.tonkar.volleyballreferee.engine.stored.api.ApiMessage;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUtils;
import com.tonkar.volleyballreferee.ui.billing.PurchasesListActivity;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.game.TimeBasedGameActivity;
import com.tonkar.volleyballreferee.ui.setup.GameSetupActivity;
import com.tonkar.volleyballreferee.ui.setup.QuickGameSetupActivity;
import com.tonkar.volleyballreferee.ui.setup.ScheduledGamesListActivity;
import com.tonkar.volleyballreferee.ui.user.ColleaguesListActivity;
import com.tonkar.volleyballreferee.ui.user.UserActivity;
import com.tonkar.volleyballreferee.ui.util.AlertDialogFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends NavigationActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 1;

    private StoredGamesService mStoredGamesService;

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.app_name);
    }

    @Override
    protected int getCheckedItem() {
        return R.id.action_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStoredGamesService = new StoredGamesManager(this);

        Log.i(Tags.MAIN_UI, "Create main activity");
        setContentView(R.layout.activity_main);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initNavigationMenu();

        if (mStoredGamesService.hasCurrentGame()) {
            View resumeGameCard = findViewById(R.id.resume_game_card);
            resumeGameCard.setVisibility(View.VISIBLE);
        }

        if (PrefUtils.shouldSignIn(this)) {
            View goToSignCard = findViewById(R.id.goto_sign_in_card);
            goToSignCard.setVisibility(View.VISIBLE);
        }

        fetchFriendRequests();
        fetchAvailableGames();

        if (mStoredGamesService.hasCurrentGame()) {
            try {
                mStoredGamesService.loadCurrentGame();
            } catch (JsonParseException e) {
                Log.e(Tags.STORED_GAMES, "Failed to read the recorded game because the JSON format was invalid", e);
                mStoredGamesService.deleteCurrentGame();
            }
        }
        if (mStoredGamesService.hasSetupGame()) {
            mStoredGamesService.deleteSetupGame();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            AlertDialogFragment alertDialogFragment;

            if (savedInstanceState == null) {
                alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.permission_title), getString(R.string.permission_message), getString(android.R.string.ok));
                alertDialogFragment.show(getSupportFragmentManager(), "permission");
            } else {
                alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("permission");
            }

            if (alertDialogFragment != null) {
                alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
                    @Override
                    public void onNegativeButtonClicked() {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_STORAGE);
                    }

                    @Override
                    public void onPositiveButtonClicked() {}

                    @Override
                    public void onNeutralButtonClicked() {}
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem messageItem = menu.findItem(R.id.action_news_menu);
        messageItem.setVisible(ApiUtils.isConnectedToInternet(this));

        final MenuItem purchaseItem = menu.findItem(R.id.action_purchase_menu);
        computePurchaseItemVisibility(purchaseItem);

        final MenuItem accountItem = menu.findItem(R.id.action_account_menu);
        accountItem.setVisible(PrefUtils.canSync(this));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_news_menu:
                Log.i(Tags.WEB, "VBR News");
                fetchAndShowNews();
                return true;
            case R.id.action_purchase_menu:
                Log.i(Tags.BILLING, "Purchase");
                Intent intent = new Intent(this, PurchasesListActivity.class);
                startActivity(intent);
                UiUtils.animateForward(this);
                return true;
            case R.id.action_account_menu:
                Log.i(Tags.USER_UI, "User account");
                intent = new Intent(this, UserActivity.class);
                startActivity(intent);
                UiUtils.animateForward(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void resumeCurrentGame(View view) {
        Log.i(Tags.GAME_UI, "Resume game");
        resumeCurrentGame();
    }

    public void goToSignIn(View view) {
        Log.i(Tags.USER_UI, "User sign in");
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    public void startIndoorGame(View view) {
        Log.i(Tags.GAME_UI, "Start an indoor game");
        ApiUserSummary user = PrefUtils.getUser(this);
        IndoorGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                System.currentTimeMillis(), 0L, Rules.officialIndoorRules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game");
        final Intent intent = new Intent(this, GameSetupActivity.class);
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "gameKindToToolbar").toBundle());
    }

    public void startBeachGame(View view) {
        Log.i(Tags.GAME_UI, "Start a beach game");
        ApiUserSummary user = PrefUtils.getUser(this);
        BeachGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                System.currentTimeMillis(), 0L, Rules.officialBeachRules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "gameKindToToolbar").toBundle());
    }

    public void startSnowGame(View view) {
        Log.i(Tags.GAME_UI, "Start a snow game");
    }

    public void startIndoor4x4Game(View view) {
        Log.i(Tags.GAME_UI, "Start a 4x4 indoor game");
        ApiUserSummary user = PrefUtils.getUser(this);
        Indoor4x4Game game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                System.currentTimeMillis(), 0L, Rules.defaultIndoor4x4Rules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game");
        final Intent intent = new Intent(this, GameSetupActivity.class);
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "gameKindToToolbar").toBundle());
    }

    public void startTimeBasedGame(View view) {
        Log.i(Tags.GAME_UI, "Start a time-based game");
        ApiUserSummary user = PrefUtils.getUser(this);
        TimeBasedGame game = GameFactory.createTimeBasedGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                System.currentTimeMillis(), 0L);
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "gameKindToToolbar").toBundle());
    }

    public void startScoreBasedGame(View view) {
        Log.i(Tags.GAME_UI, "Start a score-based game");
        ApiUserSummary user = PrefUtils.getUser(this);
        IndoorGame game = GameFactory.createPointBasedGame(UUID.randomUUID().toString(), user.getId(), user.getPseudo(),
                System.currentTimeMillis(), 0L, Rules.officialIndoorRules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "gameKindToToolbar").toBundle());
    }

    public void goToAvailableGames(View view) {
        Log.i(Tags.SCHEDULE_UI, "Scheduled games");
        Intent intent = new Intent(this, ScheduledGamesListActivity.class);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    public void goToColleagues(View view) {
        Log.i(Tags.USER_UI, "User colleagues");
        Intent intent = new Intent(this, ColleaguesListActivity.class);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    private void resumeCurrentGame() {
        Log.i(Tags.GAME_UI, "Start game activity and resume current game");
        IGame game = mStoredGamesService.loadCurrentGame();

        if (game == null) {
            UiUtils.makeErrorText(MainActivity.this, getString(R.string.resume_game_error), Toast.LENGTH_LONG).show();
        } else {
            if (GameType.TIME.equals(game.getKind())) {
                final Intent gameIntent = new Intent(MainActivity.this, TimeBasedGameActivity.class);
                startActivity(gameIntent);
                UiUtils.animateCreate(this);
            } else {
                final Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(gameIntent);
                UiUtils.animateCreate(this);
            }
        }
    }

    private void fetchFriendRequests() {
        if (PrefUtils.canSync(this)) {
            Request request = ApiUtils.buildGet(String.format("%s/users/friends/received/count", ApiUtils.BASE_URL), PrefUtils.getAuhentication(this));

            ApiUtils.getInstance().getHttpClient(this).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    initFriendRequestsButton(new ApiCount(0L));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        ApiCount count = JsonIOUtils.GSON.fromJson(response.body().string(), ApiCount.class);
                        initFriendRequestsButton(count);
                    } else {
                        initFriendRequestsButton(new ApiCount(0L));
                    }
                }
            });
        }
    }

    private void initFriendRequestsButton(ApiCount count) {
        runOnUiThread(() -> {
            if (count.getCount() > 0) {
                TextView gotoColleaguesText = findViewById(R.id.goto_colleagues_text);
                gotoColleaguesText.setText(String.format(Locale.getDefault(), "%s: %d", gotoColleaguesText.getText(), count.getCount()));

                View gotoColleaguesCard = findViewById(R.id.goto_colleagues_card);
                gotoColleaguesCard.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchAvailableGames() {
        if (PrefUtils.canSync(this)) {
            Request request = ApiUtils.buildGet(String.format("%s/games/available/count", ApiUtils.BASE_URL), PrefUtils.getAuhentication(this));

            ApiUtils.getInstance().getHttpClient(this).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    initAvailableGamesButton(new ApiCount(0L));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        ApiCount count = JsonIOUtils.GSON.fromJson(response.body().string(), ApiCount.class);
                        initAvailableGamesButton(count);
                    } else {
                        initAvailableGamesButton(new ApiCount(0L));
                    }
                }
            });
        }
    }

    private void initAvailableGamesButton(ApiCount count) {
        runOnUiThread(() -> {
            if (count.getCount() > 0) {
                TextView gotoAvailableGamesText = findViewById(R.id.goto_available_games_text);
                gotoAvailableGamesText.setText(String.format(Locale.getDefault(), "%s: %d", gotoAvailableGamesText.getText(), count.getCount()));

                View gotoAvailableGamesCard = findViewById(R.id.goto_available_games_card);
                gotoAvailableGamesCard.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchAndShowNews() {
        if (ApiUtils.isConnectedToInternet(this)) {
            Request request = ApiUtils.buildGet(String.format("%s/public/messages", ApiUtils.BASE_URL));

            ApiUtils.getInstance().getHttpClient(this).newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    showNews(getString(R.string.no_news));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        ApiMessage message = JsonIOUtils.GSON.fromJson(response.body().string(), ApiMessage.class);
                        showNews(message.getContent());
                    } else {
                        showNews(getString(R.string.no_news));
                    }
                }
            });
        }
    }

    private void showNews(String message) {
        runOnUiThread(() -> {
            if (!isFinishing()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                        .setTitle(R.string.news).setMessage(message)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        });
                AlertDialog alertDialog = builder.show();
                UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
            }
        });
    }

}