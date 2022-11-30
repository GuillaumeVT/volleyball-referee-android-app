package com.tonkar.volleyballreferee.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.tonkar.volleyballreferee.ui.interfaces.BaseTeamServiceHandler;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.interfaces.RulesHandler;
import com.tonkar.volleyballreferee.ui.rules.RulesSetupFragment;
import com.tonkar.volleyballreferee.ui.team.TeamSetupFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class GameSetupActivity extends AppCompatActivity {

    private IGame mGame;

    public GameSetupActivity() {
        super();
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof RulesHandler) {
                RulesHandler rulesHandler = (RulesHandler) fragment;
                rulesHandler.setRules(mGame.getRules());
            }
            if (fragment instanceof BaseTeamServiceHandler) {
                BaseTeamServiceHandler baseTeamServiceHandler = (BaseTeamServiceHandler) fragment;
                baseTeamServiceHandler.setTeamService(mGame);
            }
            if (fragment instanceof GameServiceHandler) {
                GameServiceHandler gameServiceHandler = (GameServiceHandler) fragment;
                gameServiceHandler.setGameService(mGame);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StoredGamesService storedGamesService = new StoredGamesManager(this);
        mGame = storedGamesService.loadSetupGame();

        super.onCreate(savedInstanceState);

        Log.i(Tags.SETUP_UI, "Create game setup activity");
        setContentView(R.layout.activity_game_setup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.updateToolbarLogo(toolbar, mGame.getKind(), mGame.getUsage());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final BottomNavigationView gameSetupNavigation = findViewById(R.id.game_setup_nav);
        initGameSetupNavigation(gameSetupNavigation, savedInstanceState);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_help, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_help_menu) {
            showGameSetupStatus();
            return true;
        } else if (itemId == android.R.id.home) {
            cancelSetup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void computeStartLayoutVisibility() {
        View saveLayout = findViewById(R.id.start_game_layout);
        if (mGame.getTeamName(TeamType.HOME).length() < 2 || mGame.getNumberOfPlayers(TeamType.HOME) < mGame.getExpectedNumberOfPlayersOnCourt()
                || mGame.getTeamName(TeamType.GUEST).length() < 2 || mGame.getNumberOfPlayers(TeamType.GUEST) < mGame.getExpectedNumberOfPlayersOnCourt()
                || mGame.getCaptain(TeamType.HOME) < 0 || mGame.getCaptain(TeamType.GUEST) < 0
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.game_setup_title)).setMessage(getString(R.string.confirm_game_setup_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            saveTeams();
            saveRules();
            saveLeague();
            mGame.startMatch();
            StoredGamesService storedGamesService = new StoredGamesManager(this);
            storedGamesService.createCurrentGame(mGame);
            Log.i(Tags.SETUP_UI, "Start game activity");
            final Intent gameIntent = new Intent(GameSetupActivity.this, GameActivity.class);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(gameIntent);
            UiUtils.animateCreate(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
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
            UiUtils.navigateToHome(GameSetupActivity.this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void initGameSetupNavigation(final BottomNavigationView gameSetupNavigation, Bundle savedInstanceState) {
        gameSetupNavigation.setOnItemSelectedListener(item -> {
                    final Fragment fragment;

            int itemId = item.getItemId();
            if (itemId == R.id.home_team_tab) {
                fragment = TeamSetupFragment.newInstance(mGame.getTeamsKind(), TeamType.HOME, true);
            } else if (itemId == R.id.guest_team_tab) {
                fragment = TeamSetupFragment.newInstance(mGame.getTeamsKind(), TeamType.GUEST, true);
            } else if (itemId == R.id.rules_tab) {
                fragment = RulesSetupFragment.newInstance(true);
            } else if (itemId == R.id.misc_tab) {
                fragment = MiscSetupFragment.newInstance();
            } else {
                fragment = null;
            }

                    final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    UiUtils.animateNavigationView(transaction);
                    transaction.replace(R.id.game_setup_container, fragment).commit();

                    return true;
                }
        );

        if (savedInstanceState == null) {
            gameSetupNavigation.setSelectedItemId(R.id.home_team_tab);
        }
    }

    private void showGameSetupStatus() {
        GameSetupStatusDialog gameSetupStatusDialog = new GameSetupStatusDialog(this, mGame);
        gameSetupStatusDialog.show();
    }
}
