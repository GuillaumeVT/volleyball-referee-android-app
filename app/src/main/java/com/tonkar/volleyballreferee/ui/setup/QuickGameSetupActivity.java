package com.tonkar.volleyballreferee.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.service.StoredGamesManager;
import com.tonkar.volleyballreferee.engine.service.StoredGamesService;
import com.tonkar.volleyballreferee.engine.service.StoredLeaguesManager;
import com.tonkar.volleyballreferee.engine.service.StoredLeaguesService;
import com.tonkar.volleyballreferee.engine.service.StoredRulesManager;
import com.tonkar.volleyballreferee.engine.service.StoredRulesService;
import com.tonkar.volleyballreferee.engine.service.StoredTeamsManager;
import com.tonkar.volleyballreferee.engine.service.StoredTeamsService;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.game.TimeBasedGameActivity;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.interfaces.RulesHandler;
import com.tonkar.volleyballreferee.ui.rules.RulesSetupFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class QuickGameSetupActivity extends AppCompatActivity {

    private IGame mGame;

    public QuickGameSetupActivity() {
        super();
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof GameServiceHandler) {
                GameServiceHandler gameServiceHandler = (GameServiceHandler) fragment;
                gameServiceHandler.setGameService(mGame);
            }
            if (fragment instanceof RulesHandler) {
                RulesHandler rulesHandler = (RulesHandler) fragment;
                rulesHandler.setRules(mGame.getRules());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StoredGamesService storedGamesService = new StoredGamesManager(this);
        mGame = storedGamesService.loadSetupGame();

        super.onCreate(savedInstanceState);

        Log.i(Tags.SETUP_UI, "Create quick game setup activity");
        setContentView(R.layout.activity_quick_game_setup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.updateToolbarLogo(toolbar, mGame.getKind(), mGame.getUsage());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final boolean create = getIntent().getBooleanExtra("create", true);
        final BottomNavigationView gameSetupNavigation = findViewById(R.id.quick_game_setup_nav);
        initGameSetupNavigation(gameSetupNavigation, savedInstanceState, create);

        computeStartLayoutVisibility();

        ScrollView scrollView = findViewById(R.id.setup_scroll_view);
        ExtendedFloatingActionButton startGameButton = findViewById(R.id.start_game_button);
        UiUtils.addExtendShrinkListener(scrollView, startGameButton);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StoredGamesService storedGamesService = new StoredGamesManager(this);
        storedGamesService.saveSetupGame(mGame);
    }

    @Override
    public void onBackPressed() {
        cancelSetup();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            cancelSetup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void computeStartLayoutVisibility() {
        View saveLayout = findViewById(R.id.start_game_layout);
        if (mGame.getTeamName(TeamType.HOME).length() < 2 || mGame.getTeamName(TeamType.GUEST).length() < 2
                || mGame.getRules().getName().length() < 2
                || (mGame.getLeague().getName().length() > 0 && mGame.getLeague().getDivision().length() < 2)) {
            Log.i(Tags.SETUP_UI, "Save button is invisible");
            saveLayout.setVisibility(View.GONE);
        } else {
            Log.i(Tags.SETUP_UI, "Save button is visible");
            saveLayout.setVisibility(View.VISIBLE);
        }
    }

    public void startGame(View view) {
        Log.i(Tags.SETUP_UI, "Start game");
        mGame.startMatch();
        StoredGamesService storedGamesService = new StoredGamesManager(this);
        storedGamesService.createCurrentGame(mGame);

        if (GameType.TIME.equals(mGame.getKind())) {
            Log.i(Tags.SETUP_UI, "Start time-based game activity");
            final Intent gameIntent = new Intent(this, TimeBasedGameActivity.class);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(gameIntent);
            UiUtils.animateCreate(this);
        } else {
            if (GameType.BEACH.equals(mGame.getKind())) {
                saveTeams();
                saveLeague();
                saveRules();
            }
            Log.i(Tags.SETUP_UI, "Start game activity");
            final Intent gameIntent = new Intent(this, GameActivity.class);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(gameIntent);
            UiUtils.animateCreate(this);
        }
    }

    private void saveTeams() {
        if (mGame.getCreatedBy().equals(PrefUtils.getUser(this).getId())) {
            StoredTeamsService storedTeamsService = new StoredTeamsManager(this);
            GameType gameType = mGame.getTeamsKind();
            storedTeamsService.createAndSaveTeamFrom(gameType, mGame, TeamType.HOME);
            storedTeamsService.createAndSaveTeamFrom(gameType, mGame, TeamType.GUEST);
        }
    }

    private void saveRules() {
        if (mGame.getCreatedBy().equals(PrefUtils.getUser(this).getId())) {
            StoredRulesService storedRulesService = new StoredRulesManager(this);
            storedRulesService.createAndSaveRulesFrom(mGame.getRules());
        }
    }

    private void saveLeague() {
        if (mGame.getCreatedBy().equals(PrefUtils.getUser(this).getId())) {
            StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(this);
            storedLeaguesService.createAndSaveLeagueFrom(mGame.getLeague());
        }
    }

    private void cancelSetup() {
        Log.i(Tags.SETUP_UI, "Cancel setup");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.game_setup_title)).setMessage(getString(R.string.leave_game_setup_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            StoredGamesService storedGamesService = new StoredGamesManager(this);
            if (storedGamesService.hasSetupGame()) {
                storedGamesService.deleteSetupGame();
            }
            UiUtils.navigateToHome(QuickGameSetupActivity.this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void initGameSetupNavigation(final BottomNavigationView gameSetupNavigation, Bundle savedInstanceState, boolean create) {
        gameSetupNavigation.setOnItemSelectedListener(item -> {
                    final Fragment fragment;

                    switch (item.getItemId()) {
                        case R.id.teams_tab:
                            fragment = QuickGameSetupFragment.newInstance(create);
                            break;
                        case R.id.rules_tab:
                            if (GameType.TIME.equals(mGame.getKind())) {
                                fragment = null;
                            } else {
                                fragment = RulesSetupFragment.newInstance(true);
                            }
                            break;
                        case R.id.misc_tab:
                            fragment = MiscSetupFragment.newInstance();
                            break;
                        default:
                            fragment = null;
                            break;
                    }

                    final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    UiUtils.animateNavigationView(transaction);
                    transaction.replace(R.id.quick_game_setup_container, fragment).commit();

                    return true;
                }
        );

        if (savedInstanceState == null) {
            gameSetupNavigation.setSelectedItemId(R.id.teams_tab);
        }

        if (GameType.TIME.equals(mGame.getKind())) {
            gameSetupNavigation.setVisibility(View.GONE);
        }
    }
}
