package com.tonkar.volleyballreferee.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ScrollView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.interfaces.*;
import com.tonkar.volleyballreferee.ui.rules.RulesSetupFragment;
import com.tonkar.volleyballreferee.ui.team.TeamSetupFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class GameSetupActivity extends AppCompatActivity {

    private IGame mGame;

    public GameSetupActivity() {
        super();
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof RulesHandler rulesHandler) {
                rulesHandler.setRules(mGame.getRules());
            }
            if (fragment instanceof BaseTeamServiceHandler baseTeamServiceHandler) {
                baseTeamServiceHandler.setTeamService(mGame);
            }
            if (fragment instanceof GameServiceHandler gameServiceHandler) {
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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cancelSetup();
            }
        });

        final NavigationBarView gameSetupNavigation = findViewById(R.id.game_setup_nav);
        initGameSetupNavigation(gameSetupNavigation, savedInstanceState);

        computeStartGameButton();

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            cancelSetup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void computeStartGameButton() {
        MaterialButton saveButton = findViewById(R.id.start_game_button);
        if (cannotStartGame(mGame)) {
            saveButton.setIconResource(R.drawable.ic_help_menu);
            saveButton.setOnClickListener(view -> showGameSetupStatus(mGame));
        } else {
            saveButton.setOnClickListener(this::startGame);
            saveButton.setIconResource(R.drawable.ic_play);
        }
    }

    public void startGame(View view) {
        Log.i(Tags.SETUP_UI, "Start game");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.new_game_title)).setMessage(getString(R.string.confirm_game_setup_question));
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
        if (Objects.equals(mGame.getCreatedBy(), PrefUtils.getUserId(this))) {
            StoredTeamsService storedTeamsService = new StoredTeamsManager(this);
            GameType gameType = mGame.getTeamsKind();
            storedTeamsService.createAndSaveTeamFrom(gameType, mGame, TeamType.HOME);
            storedTeamsService.createAndSaveTeamFrom(gameType, mGame, TeamType.GUEST);
        }
    }

    private void saveRules() {
        if (Objects.equals(mGame.getCreatedBy(), PrefUtils.getUserId(this))) {
            StoredRulesService storedRulesService = new StoredRulesManager(this);
            storedRulesService.createAndSaveRulesFrom(mGame.getRules());
        }
    }

    private void saveLeague() {
        if (Objects.equals(mGame.getCreatedBy(), PrefUtils.getUserId(this))) {
            StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(this);
            storedLeaguesService.createAndSaveLeagueFrom(mGame.getLeague());
        }
    }

    private void cancelSetup() {
        Log.i(Tags.SETUP_UI, "Cancel setup");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.new_game_title)).setMessage(getString(R.string.leave_game_setup_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            StoredGamesService storedGamesService = new StoredGamesManager(this);
            if (storedGamesService.hasSetupGame()) {
                storedGamesService.deleteSetupGame();
            }
            UiUtils.navigateBackToHome(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void initGameSetupNavigation(final NavigationBarView gameSetupNavigation, Bundle savedInstanceState) {
        gameSetupNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home_team_tab) {
                fragment = TeamSetupFragment.newInstance(mGame.getTeamsKind(), TeamType.HOME, true);
            } else if (itemId == R.id.guest_team_tab) {
                fragment = TeamSetupFragment.newInstance(mGame.getTeamsKind(), TeamType.GUEST, true);
            } else if (itemId == R.id.rules_tab) {
                fragment = RulesSetupFragment.newInstance(true);
            } else if (itemId == R.id.misc_tab) {
                fragment = MiscSetupFragment.newInstance();
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.game_setup_container, fragment).commit();
            }

            return true;
        });

        if (savedInstanceState == null) {
            gameSetupNavigation.setSelectedItemId(R.id.home_team_tab);
        }
    }

    private boolean cannotStartGame(IGame game) {
        return game.getTeamName(TeamType.HOME).length() < IBaseTeam.TEAM_NAME_MIN_LENGTH || game.getNumberOfPlayers(
                TeamType.HOME) < game.getExpectedNumberOfPlayersOnCourt() || game
                .getTeamName(TeamType.GUEST)
                .length() < IBaseTeam.TEAM_NAME_MIN_LENGTH || game.getNumberOfPlayers(
                TeamType.GUEST) < game.getExpectedNumberOfPlayersOnCourt() || game.getCaptain(TeamType.HOME) < 0 || game.getCaptain(
                TeamType.GUEST) < 0 || game.getRules().getName().length() < Rules.RULES_NAME_MIN_LENGTH || (StringUtils.isNotBlank(
                game.getLeague().getName()) && game.getLeague().getDivision().length() < 2);
    }

    private void showGameSetupStatus(IGame game) {
        GameSetupStatusDialog gameSetupStatusDialog = new GameSetupStatusDialog(this, game);
        gameSetupStatusDialog.show(game.getUsage());
    }
}
