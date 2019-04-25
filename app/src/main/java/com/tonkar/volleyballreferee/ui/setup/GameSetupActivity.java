package com.tonkar.volleyballreferee.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.RecordedGames;
import com.tonkar.volleyballreferee.business.data.SavedRules;
import com.tonkar.volleyballreferee.business.data.SavedTeams;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.data.SavedRulesService;
import com.tonkar.volleyballreferee.interfaces.data.SavedTeamsService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.interfaces.RulesServiceHandler;
import com.tonkar.volleyballreferee.ui.interfaces.BaseTeamServiceHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.rules.RulesSetupFragment;
import com.tonkar.volleyballreferee.ui.team.TeamSetupFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class GameSetupActivity extends AppCompatActivity {

    private GameService mGameService;
    private MenuItem    mStartItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecordedGamesService recordedGamesService = new RecordedGames(this);
        mGameService = recordedGamesService.loadSetupGame();

        super.onCreate(savedInstanceState);

        Log.i(Tags.SETUP_UI, "Create game setup activity");
        setContentView(R.layout.activity_game_setup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.updateToolbarLogo(toolbar, mGameService.getKind(), mGameService.getUsage());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final boolean create = getIntent().getBooleanExtra("create", true);
        final BottomNavigationView gameSetupNavigation = findViewById(R.id.game_setup_nav);
        initGameSetupNavigation(gameSetupNavigation, savedInstanceState, create);
        
        computeStartItemVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        RecordedGamesService recordedGamesService = new RecordedGames(this);
        recordedGamesService.saveSetupGame(mGameService);
    }

    @Override
    public void onBackPressed() {
        cancelSetup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game_setup, menu);

        mStartItem = menu.findItem(R.id.action_start);
        computeStartItemVisibility();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start:
                startGame();
                return true;
            case android.R.id.home:
                cancelSetup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void computeStartItemVisibility() {
        if (mStartItem != null) {
            if (mGameService.getTeamName(TeamType.HOME).isEmpty() || mGameService.getNumberOfPlayers(TeamType.HOME) < mGameService.getExpectedNumberOfPlayersOnCourt()
                    || mGameService.getTeamName(TeamType.GUEST).isEmpty() || mGameService.getNumberOfPlayers(TeamType.GUEST) < mGameService.getExpectedNumberOfPlayersOnCourt()
                    || mGameService.getCaptain(TeamType.HOME) < 1 || mGameService.getCaptain(TeamType.GUEST) < 1
                    || mGameService.getRules().getName().length() == 0) {
                Log.i(Tags.SETUP_UI, "Confirm button is invisible");
                mStartItem.setVisible(false);
            } else {
                Log.i(Tags.SETUP_UI, "Confirm button is visible");
                mStartItem.setVisible(true);
            }
        }
    }

    public void startGame() {
        Log.i(Tags.SETUP_UI, "Start game");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.game_setup_title)).setMessage(getResources().getString(R.string.confirm_game_setup_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            saveTeams();
            saveRules();
            mGameService.startMatch();
            RecordedGamesService recordedGamesService = new RecordedGames(this);
            recordedGamesService.createCurrentGame(mGameService);
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
        SavedTeamsService savedTeamsService = new SavedTeams(this);
        GameType gameType = mGameService.getTeamsKind();
        savedTeamsService.createAndSaveTeamFrom(gameType, mGameService, TeamType.HOME);
        savedTeamsService.createAndSaveTeamFrom(gameType, mGameService, TeamType.GUEST);
    }

    private void saveRules() {
        SavedRulesService rulesService = new SavedRules(this);
        rulesService.createAndSaveRulesFrom(mGameService.getRules());
    }

    private void cancelSetup() {
        Log.i(Tags.SETUP_UI, "Cancel setup");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.game_setup_title)).setMessage(getResources().getString(R.string.leave_game_setup_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            RecordedGamesService recordedGamesService = new RecordedGames(this);
            if (recordedGamesService.hasSetupGame()) {
                recordedGamesService.deleteSetupGame();
            }
            UiUtils.navigateToHome(GameSetupActivity.this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void initGameSetupNavigation(final BottomNavigationView gameSetupNavigation, Bundle savedInstanceState, boolean create) {
        gameSetupNavigation.setOnNavigationItemSelectedListener(item -> {
                    final Fragment fragment;

                    switch (item.getItemId()) {
                        case R.id.home_team_tab:
                            fragment = TeamSetupFragment.newInstance(TeamType.HOME, true, create);
                            break;
                        case R.id.guest_team_tab:
                            fragment = TeamSetupFragment.newInstance(TeamType.GUEST, true, create);
                            break;
                        case R.id.rules_tab:
                            fragment = RulesSetupFragment.newInstance(true, create);
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
                    transaction.replace(R.id.game_setup_container, fragment).commit();

                    return true;
                }
        );

        if (savedInstanceState == null) {
            gameSetupNavigation.setSelectedItemId(R.id.home_team_tab);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof RulesServiceHandler) {
            RulesServiceHandler rulesServiceHandler = (RulesServiceHandler) fragment;
            rulesServiceHandler.setRules(mGameService.getRules());
        }
        if (fragment instanceof BaseTeamServiceHandler) {
            BaseTeamServiceHandler baseTeamServiceHandler = (BaseTeamServiceHandler) fragment;
            baseTeamServiceHandler.setTeamService(mGameService);
        }
        if (fragment instanceof GameServiceHandler) {
            GameServiceHandler gameServiceHandler = (GameServiceHandler) fragment;
            gameServiceHandler.setGameService(mGameService);
        }
    }
}
