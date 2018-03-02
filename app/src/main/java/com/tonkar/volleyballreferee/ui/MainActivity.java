package com.tonkar.volleyballreferee.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.BooleanRequest;
import com.tonkar.volleyballreferee.business.data.JsonStringRequest;
import com.tonkar.volleyballreferee.business.data.WebUtils;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.game.TimeBasedGameActivity;
import com.tonkar.volleyballreferee.ui.data.RecordedGamesListActivity;
import com.tonkar.volleyballreferee.ui.data.SavedTeamsListActivity;
import com.tonkar.volleyballreferee.ui.rules.RulesActivity;
import com.tonkar.volleyballreferee.ui.team.QuickTeamsSetupActivity;
import com.tonkar.volleyballreferee.ui.team.TeamsSetupActivity;
import com.tonkar.volleyballreferee.ui.web.WebActivity;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 1;

    private RecordedGamesService mRecordedGamesService;
    private DrawerLayout         mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-MainActivity", "Create main activity");
        setContentView(R.layout.activity_main);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PreferenceManager.setDefaultValues(this, R.xml.rules, false);

        initNavigationMenu();

        ServicesProvider.getInstance().restoreRecordedGamesService(getApplicationContext());
        mRecordedGamesService = ServicesProvider.getInstance().getRecordedGamesService();

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
                    public void onPositiveButtonClicked() {
                    }

                    @Override
                    public void onNeutralButtonClicked() {
                    }
                });
            }
        }

        if (mRecordedGamesService.hasCurrentGame()) {
            resumeCurrentGameWithDialog(savedInstanceState);
        }
    }

    private void initNavigationMenu() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_rules:
                        Log.i("VBR-MainActivity", "Rules");
                        Intent intent = new Intent(MainActivity.this, RulesActivity.class);
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
                        intent = new Intent(MainActivity.this, WebActivity.class);
                        intent.putExtra("url", WebUtils.LIVE_URL);
                        startActivity(intent);
                        break;
                    case R.id.action_search_online_games:
                        Log.i("VBR-MainActivity", "Search online games");
                        intent = new Intent(MainActivity.this, WebActivity.class);
                        intent.putExtra("url", WebUtils.SEARCH_URL);
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
                    case R.id.action_start_time_based_game:
                        Log.i("VBR-MainActivity", "Start a time-based game");
                        startTimeBasedGame();
                        break;
                    case R.id.action_start_score_based_game_official:
                        Log.i("VBR-MainActivity", "Start an official score-based game");
                        startScoreBasedGame(false);
                        break;
                    case R.id.action_start_score_based_game_custom:
                        Log.i("VBR-MainActivity", "Start a custom score-based game");
                        startScoreBasedGame(true);
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
        importantMessageItem.setVisible(mRecordedGamesService.hasCurrentGame());

        final MenuItem messageItem = menu.findItem(R.id.action_message);
        initMessageMenuVisibility(messageItem);

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startOfficialIndoorGame(View view) {
        Log.i("VBR-MainActivity", "Start an official indoor game");
        startIndoorGame(false);
    }

    public void startCustomIndoorGame(View view) {
        Log.i("VBR-MainActivity", "Start a custom indoor game");
        startIndoorGame(true);
    }

    public void startOfficialBeachGame(View view) {
        Log.i("VBR-MainActivity", "Start an official beach game");
        startBeachGame(false);
    }

    public void startCustomBeachGame(View view) {
        Log.i("VBR-MainActivity", "Start a custom beach game");
        startBeachGame(true);
    }

    private void startIndoorGame(final boolean custom) {
        if (custom) {
            GameFactory.createIndoorGame(PreferenceManager.getDefaultSharedPreferences(this));
        } else {
            GameFactory.createIndoorGame();
        }

        Log.i("VBR-MainActivity", "Start activity to setup teams");
        final Intent intent = new Intent(this, TeamsSetupActivity.class);
        startActivity(intent);
    }

    private void startBeachGame(final boolean custom) {
        if (custom) {
            GameFactory.createBeachGame(PreferenceManager.getDefaultSharedPreferences(this));
        } else {
            GameFactory.createBeachGame();
        }

        Log.i("VBR-MainActivity", "Start activity to setup teams quickly");
        final Intent intent = new Intent(this, QuickTeamsSetupActivity.class);
        startActivity(intent);
    }

    private void startTimeBasedGame() {
        GameFactory.createTimeBasedGame();

        Log.i("VBR-MainActivity", "Start activity to setup teams quickly");
        final Intent intent = new Intent(this, QuickTeamsSetupActivity.class);
        startActivity(intent);
    }

    private void startScoreBasedGame(final boolean custom) {
        if (custom) {
            GameFactory.createPointBasedGame(PreferenceManager.getDefaultSharedPreferences(this));
        } else {
            GameFactory.createPointBasedGame();
        }

        Log.i("VBR-MainActivity", "Start activity to setup teams quickly");
        final Intent intent = new Intent(this, QuickTeamsSetupActivity.class);
        startActivity(intent);
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
            }
            else {
                alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("current_game");
            }

            if (alertDialogFragment != null) {
                alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
                    @Override
                    public void onNegativeButtonClicked() {
                        Log.i("VBR-MainActivity", "Delete current game");
                        mRecordedGamesService.deleteCurrentGame();
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.deleted_game), Toast.LENGTH_LONG).show();
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onPositiveButtonClicked() {
                        Log.i("VBR-MainActivity", "Start game activity and resume current game");
                        ServicesProvider.getInstance().restoreGameService(getApplicationContext());
                        if (UsageType.TIME_SCOREBOARD.equals(ServicesProvider.getInstance().getScoreService().getUsageType())) {
                            final Intent gameIntent = new Intent(MainActivity.this, TimeBasedGameActivity.class);
                            startActivity(gameIntent);
                        } else {
                            final Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                            startActivity(gameIntent);
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
}
