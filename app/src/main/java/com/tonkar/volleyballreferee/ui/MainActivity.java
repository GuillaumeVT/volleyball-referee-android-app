package com.tonkar.volleyballreferee.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonSyntaxException;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.RecordedGames;
import com.tonkar.volleyballreferee.business.data.SavedRules;
import com.tonkar.volleyballreferee.business.data.SavedTeams;
import com.tonkar.volleyballreferee.business.game.*;
import com.tonkar.volleyballreferee.business.web.BooleanRequest;
import com.tonkar.volleyballreferee.business.data.GameDescription;
import com.tonkar.volleyballreferee.business.web.JsonStringRequest;
import com.tonkar.volleyballreferee.business.web.WebUtils;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.*;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.billing.PurchasesListActivity;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.game.TimeBasedGameActivity;
import com.tonkar.volleyballreferee.ui.setup.QuickGameSetupActivity;
import com.tonkar.volleyballreferee.ui.setup.GameSetupActivity;
import com.tonkar.volleyballreferee.ui.util.AlertDialogFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AuthenticationActivity implements AsyncGameRequestListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 1;

    private RecordedGamesService mRecordedGamesService;

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

        mRecordedGamesService = new RecordedGames(this);

        Log.i(Tags.MAIN_UI, "Create main activity");
        setContentView(R.layout.activity_main);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initNavigationMenu();

        initButtonOnClickListeners();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            MaterialButton beachButton = findViewById(R.id.start_beach_game_button);
            beachButton.getIcon().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryText), PorterDuff.Mode.SRC_IN));
        }

        setResumeGameCardVisibility();

        MaterialButton scheduledCodeButton = findViewById(R.id.start_scheduled_code_game_button);
        scheduledCodeButton.setVisibility(PrefUtils.canRequest(this) ? View.VISIBLE : View.GONE);

        Context applicationContext = getApplicationContext();
        SavedTeamsService savedTeamsService = new SavedTeams(applicationContext);
        SavedRulesService savedRulesService = new SavedRules(applicationContext);

        savedRulesService.migrateSavedRules();
        savedTeamsService.migrateSavedTeams();
        mRecordedGamesService.migrateRecordedGames();

        if (mRecordedGamesService.hasCurrentGame()) {
            try {
                mRecordedGamesService.loadCurrentGame();
            } catch (JsonSyntaxException e) {
                Log.e(Tags.SAVED_GAMES, "Failed to read the recorded game because the JSON format was invalid", e);
                mRecordedGamesService.deleteCurrentGame();
            }
        }
        if (mRecordedGamesService.hasSetupGame()) {
            mRecordedGamesService.deleteSetupGame();
        }

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final long currentTime = System.currentTimeMillis();

        if (sharedPreferences.getLong("last_full_sync", 0L) + 3600000L < currentTime) {
            savedRulesService.syncRulesOnline();
            savedTeamsService.syncTeamsOnline();
            mRecordedGamesService.syncGamesOnline();
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

        if (mRecordedGamesService.hasCurrentGame()) {
            resumeCurrentGameWithDialog(savedInstanceState);
        }

        if (savedInstanceState != null) {
            restoreScheduledGameFromCodeDialog();
            restoreEditScheduledGameFromCodeDialog();
        }

        checkAuthentication();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem messageItem = menu.findItem(R.id.action_message_menu);
        initMessageMenuVisibility(messageItem);

        final MenuItem purchaseItem = menu.findItem(R.id.action_purchase_menu);
        computePurchaseItemVisibility(purchaseItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_message_menu:
                Log.i(Tags.WEB, "VBR Message");
                showMessage();
                return true;
            case R.id.action_purchase_menu:
                Log.i(Tags.BILLING, "Purchase");
                Intent intent = new Intent(this, PurchasesListActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initButtonOnClickListeners() {
        findViewById(R.id.resume_game_button).setOnClickListener(button -> resumeCurrentGame(null));
        findViewById(R.id.start_indoor_6x6_game_button).setOnClickListener(button -> startIndoorGame(null));
        findViewById(R.id.start_indoor_4x4_game_button).setOnClickListener(button -> startIndoor4x4Game(null));
        findViewById(R.id.start_score_based_game_button).setOnClickListener(button -> startScoreBasedGame(null));
        findViewById(R.id.start_beach_game_button).setOnClickListener(button -> startBeachGame(null));
        findViewById(R.id.start_time_based_game_button).setOnClickListener(button -> startTimeBasedGame(null));
        findViewById(R.id.start_scheduled_code_game_button).setOnClickListener(button -> startScheduledGameFromCode(null));
    }

    public void resumeCurrentGame(View view) {
        Log.i(Tags.GAME_UI, "Resume game");
        resumeCurrentGameWithDialog(null);
    }

    public void startIndoorGame(View view) {
        Log.i(Tags.GAME_UI, "Start an indoor game");
        IndoorGame game = GameFactory.createIndoorGame(System.currentTimeMillis(), 0L, Rules.officialIndoorRules());
        mRecordedGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game");
        final Intent intent = new Intent(this, GameSetupActivity.class);
        startActivity(intent);
    }

    public void startBeachGame(View view) {
        Log.i(Tags.GAME_UI, "Start a beach game");
        BeachGame game = GameFactory.createBeachGame(System.currentTimeMillis(), 0L, Rules.officialBeachRules());
        mRecordedGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        startActivity(intent);
    }

    public void startIndoor4x4Game(View view) {
        Log.i(Tags.GAME_UI, "Start a 4x4 indoor game");
        Indoor4x4Game game = GameFactory.createIndoor4x4Game(System.currentTimeMillis(), 0L, Rules.defaultIndoor4x4Rules());
        mRecordedGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game");
        final Intent intent = new Intent(this, GameSetupActivity.class);
        startActivity(intent);
    }

    public void startTimeBasedGame(View view) {
        Log.i(Tags.GAME_UI, "Start a time-based game");
        TimeBasedGame game = GameFactory.createTimeBasedGame(System.currentTimeMillis(), 0L);
        mRecordedGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        startActivity(intent);
    }

    public void startScoreBasedGame(View view) {
        Log.i(Tags.GAME_UI, "Start a score-based game");
        IndoorGame game = GameFactory.createPointBasedGame(System.currentTimeMillis(), 0L, Rules.officialIndoorRules());
        mRecordedGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        startActivity(intent);
    }

    public void startScheduledGameFromCode(View view) {
        Log.i(Tags.GAME_UI, "Start a scheduled game from code");

        CodeInputDialogFragment dialogFragment = (CodeInputDialogFragment) getSupportFragmentManager().findFragmentByTag("game_code");

        if (dialogFragment == null) {
            dialogFragment = CodeInputDialogFragment.newInstance(getResources().getString(R.string.new_scheduled_game_from_code),
                    getResources().getString(android.R.string.cancel), getResources().getString(android.R.string.ok));
            dialogFragment.show(getSupportFragmentManager(), "game_code");
        }

        setScheduledGameFromCodeListener(dialogFragment);
    }

    private void resumeCurrentGameWithDialog(Bundle savedInstanceState) {
        boolean showResumeGameDialog = getIntent().getBooleanExtra("show_resume_game", true);
        getIntent().removeExtra("show_resume_game");

        if (mRecordedGamesService.hasCurrentGame() && showResumeGameDialog) {
            AlertDialogFragment alertDialogFragment;

            if (savedInstanceState == null) {
                alertDialogFragment = AlertDialogFragment.newInstance(getResources().getString(R.string.resume_game_title), getResources().getString(R.string.resume_game_question),
                        getResources().getString(R.string.delete), getResources().getString(R.string.resume), getResources().getString(R.string.ignore));
                alertDialogFragment.show(getSupportFragmentManager(), "current_game");
            } else {
                alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("current_game");
            }

            if (alertDialogFragment != null) {
                alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
                    @Override
                    public void onNegativeButtonClicked() {
                        Log.i(Tags.SAVED_GAMES, "Delete current game");
                        mRecordedGamesService.deleteCurrentGame();
                        UiUtils.makeText(MainActivity.this, getResources().getString(R.string.deleted_game), Toast.LENGTH_LONG).show();
                        setResumeGameCardVisibility();
                    }

                    @Override
                    public void onPositiveButtonClicked() {
                        Log.i(Tags.GAME_UI, "Start game activity and resume current game");
                        GameService gameService = mRecordedGamesService.loadCurrentGame();

                        if (gameService == null) {
                            UiUtils.makeText(MainActivity.this, getResources().getString(R.string.resume_game_error), Toast.LENGTH_LONG).show();
                        } else {
                            if (GameType.TIME.equals(gameService.getGameType())) {
                                final Intent gameIntent = new Intent(MainActivity.this, TimeBasedGameActivity.class);
                                startActivity(gameIntent);
                            } else {
                                final Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                                startActivity(gameIntent);
                            }
                        }
                    }

                    @Override
                    public void onNeutralButtonClicked() {}
                });
            }
        }
    }

    private void initMessageMenuVisibility(final MenuItem messageItem) {
        messageItem.setVisible(false);

        if (PrefUtils.canRequest(this)) {
            String url = WebUtils.HAS_MESSAGE_API_URL;
            BooleanRequest booleanRequest = new BooleanRequest(Request.Method.GET, url, messageItem::setVisible, error -> messageItem.setVisible(false)
            );
            WebUtils.getInstance().getRequestQueue(this).add(booleanRequest);
        }
    }

    private void showMessage() {
        if (PrefUtils.canRequest(this)) {
            String url = WebUtils.MESSAGE_API_URL;
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, url, new byte[0],
                    response -> {
                        if (response != null) {
                            Snackbar infoSnackbar = Snackbar.make(mDrawerLayout, response, Snackbar.LENGTH_INDEFINITE);
                            TextView textView = infoSnackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setMaxLines(3);
                            infoSnackbar.setActionTextColor(getResources().getColor(R.color.colorBeach));
                            infoSnackbar.setAction("Close", view -> {});
                            infoSnackbar.show();
                        }
                    }, error -> {}
            );
            WebUtils.getInstance().getRequestQueue(this).add(stringRequest);
        }
    }

    private void restoreScheduledGameFromCodeDialog() {
        CodeInputDialogFragment dialogFragment = (CodeInputDialogFragment) getSupportFragmentManager().findFragmentByTag("game_code");
        setScheduledGameFromCodeListener(dialogFragment);
    }

    private void setScheduledGameFromCodeListener(CodeInputDialogFragment dialogFragment) {
        if (dialogFragment != null) {
            dialogFragment.setAlertDialogListener(new CodeInputDialogFragment.AlertDialogListener() {
                @Override
                public void onNegativeButtonClicked() {}

                @Override
                public void onPositiveButtonClicked(int code) {
                    if (code > 9999999) {
                        Log.i(Tags.GAME_UI, String.format(Locale.getDefault(), "Requesting game from code %d", code));
                        mRecordedGamesService.getGameFromCode(code, MainActivity.this);
                    } else {
                        UiUtils.makeText(MainActivity.this, getResources().getString(R.string.invalid_game_code), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onRecordedGameReceivedFromCode(RecordedGameService recordedGameService) {
        if (recordedGameService != null) {
            final GameService gameService = GameFactory.createGame(recordedGameService);
            Log.i(Tags.GAME_UI, "Start game activity after receiving code");

            switch (recordedGameService.getMatchStatus()) {
                case SCHEDULED:
                    mRecordedGamesService.saveSetupGame(gameService);
                    AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("game_code_edit");
                    if (alertDialogFragment == null) {
                        alertDialogFragment = AlertDialogFragment.newInstance(getResources().getString(R.string.new_scheduled_game_from_code), getResources().getString(R.string.scheduled_game_question),
                                getResources().getString(R.string.no), getResources().getString(R.string.yes));
                        alertDialogFragment.show(getSupportFragmentManager(), "game_code_edit");
                    }

                    setEditScheduledGameFromCodeListener(alertDialogFragment, gameService);
                    break;
                case LIVE:
                    gameService.restoreGame(recordedGameService);
                    mRecordedGamesService.createCurrentGame(gameService);
                    final Intent gameIntent = new Intent(this, GameActivity.class);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(gameIntent);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onUserGameReceived(RecordedGameService recordedGameService) {}

    @Override
    public void onUserGameListReceived(List<GameDescription> gameDescriptionList) {}

    @Override
    public void onNotFound() {
        UiUtils.makeText(MainActivity.this, getResources().getString(R.string.invalid_game_code), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInternalError() {
        UiUtils.makeText(MainActivity.this, getResources().getString(R.string.download_error_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError() {
        onInternalError();
    }

    private void restoreEditScheduledGameFromCodeDialog() {
        GameService gameService = mRecordedGamesService.loadSetupGame();
        AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("game_code_edit");

        if ((mRecordedGamesService.hasCurrentGame() || gameService == null) && alertDialogFragment != null) {
            alertDialogFragment.dismiss();
        } else {
            setEditScheduledGameFromCodeListener(alertDialogFragment, gameService);
        }
    }

    private void setEditScheduledGameFromCodeListener(AlertDialogFragment alertDialogFragment, final GameService gameService) {
        if (alertDialogFragment != null) {
            alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
                @Override
                public void onNegativeButtonClicked() {
                    Log.i(Tags.GAME_UI, "Start game from code immediately");
                    gameService.startMatch();
                    mRecordedGamesService.createCurrentGame(gameService);
                    final Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(gameIntent);
                }

                @Override
                public void onPositiveButtonClicked() {
                    Log.i(Tags.SETUP_UI, "Edit game from code before starting");
                    mRecordedGamesService.saveSetupGame(gameService);
                    final Intent setupIntent;
                    if (gameService.getGameType().equals(GameType.BEACH)) {
                        setupIntent = new Intent(MainActivity.this, QuickGameSetupActivity.class);
                    } else {
                        setupIntent = new Intent(MainActivity.this, GameSetupActivity.class);
                    }
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(setupIntent);
                }

                @Override
                public void onNeutralButtonClicked() {}
            });
        }
    }

    private void setResumeGameCardVisibility() {
        CardView resumeGameCard = findViewById(R.id.resume_game_card);
        resumeGameCard.setVisibility(mRecordedGamesService.hasCurrentGame() ? View.VISIBLE : View.GONE);
    }
}
