package com.tonkar.volleyballreferee.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonSyntaxException;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.*;
import com.tonkar.volleyballreferee.business.game.*;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.*;
import com.tonkar.volleyballreferee.business.rules.Rules;
import com.tonkar.volleyballreferee.ui.billing.PurchasesListActivity;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.game.TimeBasedGameActivity;
import com.tonkar.volleyballreferee.ui.setup.QuickGameSetupActivity;
import com.tonkar.volleyballreferee.ui.setup.GameSetupActivity;
import com.tonkar.volleyballreferee.ui.util.AlertDialogFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AuthenticationActivity {

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

        mStoredGamesService = new StoredGames(this);

        Log.i(Tags.MAIN_UI, "Create main activity");
        setContentView(R.layout.activity_main);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initNavigationMenu();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            MaterialButton beachButton = findViewById(R.id.start_beach_game_button);
            beachButton.getIcon().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
        }

        setResumeGameCardVisibility();

        if (mStoredGamesService.hasCurrentGame()) {
            try {
                mStoredGamesService.loadCurrentGame();
            } catch (JsonSyntaxException e) {
                Log.e(Tags.STORED_GAMES, "Failed to read the recorded game because the JSON format was invalid", e);
                mStoredGamesService.deleteCurrentGame();
            }
        }
        if (mStoredGamesService.hasSetupGame()) {
            mStoredGamesService.deleteSetupGame();
        }

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final long currentTime = System.currentTimeMillis();

        if (PrefUtils.canSync(this) && sharedPreferences.getLong("last_full_sync", 0L) + 7200000L < currentTime) {
            Context applicationContext = getApplicationContext();
            new StoredUser(applicationContext).syncUser();
            new StoredLeagues(applicationContext).syncLeagues();
            new StoredRules(applicationContext).syncRules();
            new StoredTeams(applicationContext).syncTeams();
            mStoredGamesService.syncGames();
            sharedPreferences.edit().putLong("last_full_sync", currentTime).apply();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            AlertDialogFragment alertDialogFragment;

            if (savedInstanceState == null) {
                alertDialogFragment = AlertDialogFragment.newInstance(getResources().getString(R.string.permission_title), getResources().getString(R.string.permission_message),
                        getResources().getString(android.R.string.ok));
                alertDialogFragment.show(getSupportFragmentManager(), "permission");
            }
            else {
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

        checkAuthentication();
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
        accountItem.setVisible(PrefUtils.isSignedIn(this));

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
                showAccount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void resumeCurrentGame(View view) {
        Log.i(Tags.GAME_UI, "Resume game");
        resumeCurrentGame();
    }

    public void startIndoorGame(View view) {
        Log.i(Tags.GAME_UI, "Start an indoor game");
        Authentication authentication = PrefUtils.getAuthentication(this);
        IndoorGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(), authentication.getUserId(), authentication.getUserPseudo(),
                System.currentTimeMillis(), 0L, Rules.officialIndoorRules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game");
        final Intent intent = new Intent(this, GameSetupActivity.class);
        intent.putExtra("create", true);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    public void startBeachGame(View view) {
        Log.i(Tags.GAME_UI, "Start a beach game");
        Authentication authentication = PrefUtils.getAuthentication(this);
        BeachGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(), authentication.getUserId(), authentication.getUserPseudo(),
                System.currentTimeMillis(), 0L, Rules.officialBeachRules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        intent.putExtra("create", true);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    public void startIndoor4x4Game(View view) {
        Log.i(Tags.GAME_UI, "Start a 4x4 indoor game");
        Authentication authentication = PrefUtils.getAuthentication(this);
        Indoor4x4Game game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(), authentication.getUserId(), authentication.getUserPseudo(),
                System.currentTimeMillis(), 0L, Rules.defaultIndoor4x4Rules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game");
        final Intent intent = new Intent(this, GameSetupActivity.class);
        intent.putExtra("create", true);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    public void startTimeBasedGame(View view) {
        Log.i(Tags.GAME_UI, "Start a time-based game");
        Authentication authentication = PrefUtils.getAuthentication(this);
        TimeBasedGame game = GameFactory.createTimeBasedGame(UUID.randomUUID().toString(), authentication.getUserId(), authentication.getUserPseudo(),
                System.currentTimeMillis(), 0L);
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        intent.putExtra("create", true);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    public void startScoreBasedGame(View view) {
        Log.i(Tags.GAME_UI, "Start a score-based game");
        Authentication authentication = PrefUtils.getAuthentication(this);
        IndoorGame game = GameFactory.createPointBasedGame(UUID.randomUUID().toString(), authentication.getUserId(), authentication.getUserPseudo(),
                System.currentTimeMillis(), 0L, Rules.officialIndoorRules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        intent.putExtra("create", true);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    private void resumeCurrentGame() {
        Log.i(Tags.GAME_UI, "Start game activity and resume current game");
        GameService gameService = mStoredGamesService.loadCurrentGame();

        if (gameService == null) {
            UiUtils.makeErrorText(MainActivity.this, getResources().getString(R.string.resume_game_error), Toast.LENGTH_LONG).show();
        } else {
            if (GameType.TIME.equals(gameService.getKind())) {
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

    private void fetchAndShowNews() {
        if (ApiUtils.isConnectedToInternet(this)) {
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, ApiUtils.MESSAGES_API_URL, new byte[0],
                    response -> {
                        ApiMessage message = JsonIOUtils.GSON.fromJson(response, JsonIOUtils.MESSAGE_TYPE);
                        showNews(message.getMessage());
                    },
                    error -> showNews(getString(R.string.no_news))
            );
            ApiUtils.getInstance().getRequestQueue(this).add(stringRequest);
        }
    }

    private void showNews(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(R.string.news).setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void showAccount() {
        if (PrefUtils.canSync(this)) {
            Authentication authentication = PrefUtils.getAuthentication(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                    .setMessage(String.format(Locale.getDefault(), getString(R.string.user_signed_in_as_pseudo), authentication.getUserPseudo()) + String.format(Locale.getDefault(), "\n\n(%s)", authentication.getUserId()))
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {})
                    .setNegativeButton(R.string.user_sign_out, (dialog, which) -> signOut());
            AlertDialog alertDialog = builder.show();
            UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
        }
    }

    private void setResumeGameCardVisibility() {
        CardView resumeGameCard = findViewById(R.id.resume_game_card);
        resumeGameCard.setVisibility(mStoredGamesService.hasCurrentGame() ? View.VISIBLE : View.GONE);
    }
}
