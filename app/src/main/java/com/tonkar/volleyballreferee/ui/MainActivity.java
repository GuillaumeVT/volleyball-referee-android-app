package com.tonkar.volleyballreferee.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.BooleanRequest;
import com.tonkar.volleyballreferee.business.data.GameDescription;
import com.tonkar.volleyballreferee.business.data.JsonStringRequest;
import com.tonkar.volleyballreferee.business.data.WebUtils;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.data.AsyncGameRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.data.SavedRulesListActivity;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.game.TimeBasedGameActivity;
import com.tonkar.volleyballreferee.ui.data.RecordedGamesListActivity;
import com.tonkar.volleyballreferee.ui.data.SavedTeamsListActivity;
import com.tonkar.volleyballreferee.ui.setup.QuickGameSetupActivity;
import com.tonkar.volleyballreferee.ui.setup.GameSetupActivity;
import com.tonkar.volleyballreferee.ui.user.ScheduledGamesListActivity;
import com.tonkar.volleyballreferee.ui.user.UserActivity;
import com.tonkar.volleyballreferee.ui.user.UserSignInActivity;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AsyncGameRequestListener {

    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 1;

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-MainActivity", "Create main activity");
        setContentView(R.layout.activity_main);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initNavigationMenu();

        Button indoor6x6Button = findViewById(R.id.start_indoor_6x6_game_button);
        colorButtonDrawable(indoor6x6Button, android.R.color.white);

        Button indoor4x4Button = findViewById(R.id.start_indoor_4x4_game_button);
        colorButtonDrawable(indoor4x4Button, android.R.color.white);

        Button scoreBasedButton = findViewById(R.id.start_score_based_game_button);
        colorButtonDrawable(scoreBasedButton, android.R.color.white);

        Button beachButton = findViewById(R.id.start_beach_game_button);
        colorButtonDrawable(beachButton, android.R.color.white);

        Button timeBasedButton = findViewById(R.id.start_time_based_game_button);
        colorButtonDrawable(timeBasedButton, android.R.color.white);

        Button scheduledListButton = findViewById(R.id.start_scheduled_list_game_button);
        colorButtonDrawable(scheduledListButton, android.R.color.white);
        scheduledListButton.setVisibility(PrefUtils.isPrefOnlineRecordingEnabled(this) && PrefUtils.isSignedIn(this) ? View.VISIBLE : View.GONE);

        Button scheduledCodeButton = findViewById(R.id.start_scheduled_code_game_button);
        colorButtonDrawable(scheduledCodeButton, android.R.color.white);
        scheduledCodeButton.setVisibility(PrefUtils.isPrefOnlineRecordingEnabled(this) ? View.VISIBLE : View.GONE);

        ServicesProvider.getInstance().restoreGameService(getApplicationContext());
        if (ServicesProvider.getInstance().getRecordedGamesService().hasSetupGame()) {
            ServicesProvider.getInstance().getRecordedGamesService().deleteSetupGame();
        }
        if (ServicesProvider.getInstance().isSavedRulesServiceUnavailable()) {
            ServicesProvider.getInstance().restoreSavedRulesService(getApplicationContext());
        } else {
            ServicesProvider.getInstance().getSavedRulesService().loadSavedRules();
        }
        if (ServicesProvider.getInstance().isSavedTeamsServiceUnavailable()) {
            ServicesProvider.getInstance().restoreSavedTeamsService(getApplicationContext());
        } else {
            ServicesProvider.getInstance().getSavedTeamsService().loadSavedTeams();
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

        if (ServicesProvider.getInstance().getRecordedGamesService().hasCurrentGame()) {
            resumeCurrentGameWithDialog(savedInstanceState);
        }

        if (savedInstanceState != null) {
            restoreScheduledGameFromCodeDialog();
            restoreEditScheduledGameFromCodeDialog();
        }
    }

    private void initNavigationMenu() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_saved_rules:
                        Log.i("VBR-MainActivity", "Saved Rules");
                        Intent intent = new Intent(MainActivity.this, SavedRulesListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_settings:
                        Log.i("VBR-MainActivity", "Settings");
                        intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_recorded_games:
                        Log.i("VBR-MainActivity", "Recorded games");
                        intent = new Intent(MainActivity.this, RecordedGamesListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_saved_teams:
                        Log.i("VBR-MainActivity", "Saved teams");
                        intent = new Intent(MainActivity.this, SavedTeamsListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_view_live_games:
                        Log.i("VBR-MainActivity", "Live games");
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebUtils.LIVE_URL));
                        startActivity(intent);
                        break;
                    case R.id.action_search_online_games:
                        Log.i("VBR-MainActivity", "Search online games");
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebUtils.SEARCH_URL));
                        startActivity(intent);
                        break;
                    case R.id.action_view_online_account:
                        Log.i("VBR-MainActivity", "View online account");
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebUtils.USER_URL));
                        startActivity(intent);
                        break;
                    case R.id.action_facebook:
                        Log.i("VBR-MainActivity", "Facebook");
                        Intent browserIntent;
                        try {
                            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/1983857898556706"));
                            startActivity(browserIntent);
                        } catch (ActivityNotFoundException e) {
                            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/VolleyballReferee/"));
                            startActivity(browserIntent);
                        }
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem importantMessageItem = menu.findItem(R.id.action_important_message);
        importantMessageItem.setVisible(ServicesProvider.getInstance().getRecordedGamesService().hasCurrentGame());

        final MenuItem messageItem = menu.findItem(R.id.action_message);
        initMessageMenuVisibility(messageItem);

        final MenuItem userItem = menu.findItem(R.id.action_account);
        userItem.setVisible(PrefUtils.isPrefOnlineRecordingEnabled(this));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i("VBR-MainActivity", "Drawer");
                if (isNavDrawerOpen()) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            case R.id.action_important_message:
                Log.i("VBR-MainActivity", "Resume game");
                resumeCurrentGameWithDialog(null);
                return true;
            case R.id.action_message:
                Log.i("VBR-MainActivity", "VBR Message");
                showMessage();
                return true;
            case R.id.action_account:
                final Intent intent;
                if (PrefUtils.isSignedIn(this)) {
                    Log.i("VBR-UserActivity", "User account");
                    intent = new Intent(this, UserActivity.class);
                } else {
                    Log.i("VBR-UserSignInActivity", "User sign in");
                    intent = new Intent(this, UserSignInActivity.class);
                }
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startIndoorGame(View view) {
        Log.i("VBR-MainActivity", "Start an indoor game");
        GameFactory.createIndoorGame(System.currentTimeMillis(), 0L, Rules.officialIndoorRules());

        Log.i("VBR-MainActivity", "Start activity to setup game");
        final Intent intent = new Intent(this, GameSetupActivity.class);
        startActivity(intent);
    }

    public void startBeachGame(View view) {
        Log.i("VBR-MainActivity", "Start a beach game");
        GameFactory.createBeachGame(System.currentTimeMillis(), 0L, Rules.officialBeachRules());

        Log.i("VBR-MainActivity", "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        startActivity(intent);
    }

    public void startIndoor4x4Game(View view) {
        Log.i("VBR-MainActivity", "Start a 4x4 indoor game");
        GameFactory.createIndoor4x4Game(System.currentTimeMillis(), 0L, Rules.defaultIndoor4x4Rules());

        Log.i("VBR-MainActivity", "Start activity to setup game");
        final Intent intent = new Intent(this, GameSetupActivity.class);
        startActivity(intent);
    }

    public void startTimeBasedGame(View view) {
        Log.i("VBR-MainActivity", "Start a time-based game");
        GameFactory.createTimeBasedGame(System.currentTimeMillis(), 0L);

        Log.i("VBR-MainActivity", "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        startActivity(intent);
    }

    public void startScoreBasedGame(View view) {
        Log.i("VBR-MainActivity", "Start a score-based game");
        GameFactory.createPointBasedGame(System.currentTimeMillis(), 0L, Rules.officialIndoorRules());

        Log.i("VBR-MainActivity", "Start activity to setup game quickly");
        final Intent intent = new Intent(this, QuickGameSetupActivity.class);
        startActivity(intent);
    }

    public void startScheduledGameFromCode(View view) {
        Log.i("VBR-MainActivity", "Start a scheduled game from code");

        CodeInputDialogFragment dialogFragment = (CodeInputDialogFragment) getSupportFragmentManager().findFragmentByTag("game_code");

        if (dialogFragment == null) {
            dialogFragment = CodeInputDialogFragment.newInstance(getResources().getString(R.string.new_scheduled_game_from_code),
                    getResources().getString(android.R.string.cancel), getResources().getString(android.R.string.ok));
            dialogFragment.show(getSupportFragmentManager(), "game_code");
        }

        setScheduledGameFromCodeListener(dialogFragment);
    }

    public void goToScheduledGames(View view) {
        Log.i("VBR-MainActivity", "Go to scheduled games");
        final Intent intent = new Intent(this, ScheduledGamesListActivity.class);
        startActivity(intent);
    }

    private void resumeCurrentGameWithDialog(Bundle savedInstanceState) {
        boolean showResumeGameDialog = getIntent().getBooleanExtra("show_resume_game", true);
        getIntent().removeExtra("show_resume_game");

        if (ServicesProvider.getInstance().getRecordedGamesService().hasCurrentGame() && showResumeGameDialog) {
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
                        Log.i("VBR-MainActivity", "Delete current game");
                        ServicesProvider.getInstance().getRecordedGamesService().deleteCurrentGame();
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.deleted_game), Toast.LENGTH_LONG).show();
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onPositiveButtonClicked() {
                        Log.i("VBR-MainActivity", "Start game activity and resume current game");
                        if (ServicesProvider.getInstance().getGameService() == null) {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.resume_game_error), Toast.LENGTH_LONG).show();
                        } else {
                            if (GameType.TIME.equals(ServicesProvider.getInstance().getGeneralService().getGameType())) {
                                final Intent gameIntent = new Intent(MainActivity.this, TimeBasedGameActivity.class);
                                startActivity(gameIntent);
                            } else {
                                final Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                                startActivity(gameIntent);
                            }
                        }
                    }

                    @Override
                    public void onNeutralButtonClicked() {
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void initMessageMenuVisibility(final MenuItem messageItem) {
        messageItem.setVisible(false);

        if (PrefUtils.isPrefOnlineRecordingEnabled(this)) {
            String url = WebUtils.HAS_MESSAGE_URL;
            BooleanRequest booleanRequest = new BooleanRequest(Request.Method.GET, url,
                    new Response.Listener<Boolean>() {
                        @Override
                        public void onResponse(Boolean response) {
                            messageItem.setVisible(response);
                        }
                    }, new Response.ErrorListener() {
                         @Override
                        public void onErrorResponse(VolleyError error) {
                            messageItem.setVisible(false);
                        }
                    }
            );
            WebUtils.getInstance().getRequestQueue(this).add(booleanRequest);
        }
    }

    private void showMessage() {
        if (PrefUtils.isPrefOnlineRecordingEnabled(this)) {
            String url = WebUtils.MESSAGE_URL;
            JsonStringRequest stringRequest = new JsonStringRequest(Request.Method.GET, url, new byte[0],
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null) {
                                Snackbar infoSnackbar = Snackbar.make(mDrawerLayout, response, Snackbar.LENGTH_INDEFINITE);
                                TextView textView = infoSnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setMaxLines(3);
                                infoSnackbar.setActionTextColor(getResources().getColor(R.color.colorBeach));
                                infoSnackbar.setAction("Close", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                    }
                                });
                                infoSnackbar.show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {}
            }
            );
            WebUtils.getInstance().getRequestQueue(this).add(stringRequest);
        }
    }

    private void colorButtonDrawable(Button button, int colorId) {
        for (Drawable drawable : button.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, colorId), PorterDuff.Mode.SRC_IN));
            }
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
                        Log.i("VBR-MainActivity", String.format(Locale.getDefault(), "Requesting game from code %d", code));
                        ServicesProvider.getInstance().getRecordedGamesService().getGameFromCode(code, MainActivity.this);
                    } else {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.invalid_game_code), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onRecordedGameReceivedFromCode(RecordedGameService recordedGameService) {
        if (recordedGameService != null) {
            final GameService gameService = GameFactory.createGame(recordedGameService);
            Log.i("VBR-MainActivity", "Start game activity after receiving code");

            switch (recordedGameService.getMatchStatus()) {
                case SCHEDULED:
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
        Toast.makeText(MainActivity.this, getResources().getString(R.string.invalid_game_code), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInternalError() {
        Toast.makeText(MainActivity.this, getResources().getString(R.string.download_error_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError() {
        onInternalError();
    }

    private void restoreEditScheduledGameFromCodeDialog() {
        GameService gameService = ServicesProvider.getInstance().getGameService();
        AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("game_code_edit");

        if ((ServicesProvider.getInstance().getRecordedGamesService().hasCurrentGame() || gameService == null) && alertDialogFragment != null) {
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
                    Log.i("VBR-MainActivity", "Start game from code immediately");
                    gameService.startMatch();
                    final Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(gameIntent);
                }

                @Override
                public void onPositiveButtonClicked() {
                    Log.i("VBR-MainActivity", "Edit game from code before starting");
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
}
