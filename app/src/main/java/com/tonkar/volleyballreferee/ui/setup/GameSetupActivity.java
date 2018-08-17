package com.tonkar.volleyballreferee.ui.setup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.data.SavedRulesService;
import com.tonkar.volleyballreferee.interfaces.data.SavedTeamsService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.rules.RulesSetupFragment;
import com.tonkar.volleyballreferee.ui.team.TeamSetupFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class GameSetupActivity extends AppCompatActivity {

    private MenuItem mConfirmItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-GSActivity", "Create game setup activity");
        setContentView(R.layout.activity_game_setup);

        if (ServicesProvider.getInstance().areSetupServicesUnavailable()) {
            ServicesProvider.getInstance().restoreGameServiceForSetup(getApplicationContext());
        }

        setTitle("");

        final BottomNavigationView gameSetupNavigation = findViewById(R.id.game_setup_nav);
        initGameSetupNavigation(gameSetupNavigation, savedInstanceState);
        
        computeConfirmItemVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ServicesProvider.getInstance().getRecordedGamesService().saveSetupGame(ServicesProvider.getInstance().getGameService());
    }

    @Override
    public void onBackPressed() {
        cancelSetup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game_setup, menu);

        mConfirmItem = menu.findItem(R.id.action_confirm);
        computeConfirmItemVisibility();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_confirm:
                confirmSetup();
                return true;
            case android.R.id.home:
                cancelSetup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void computeConfirmItemVisibility() {
        if (mConfirmItem != null) {
            IndoorTeamService indoorTeamService = (IndoorTeamService) ServicesProvider.getInstance().getTeamService();
            Rules rules = ServicesProvider.getInstance().getGeneralService().getRules();

            if (indoorTeamService.getTeamName(TeamType.HOME).isEmpty() || indoorTeamService.getNumberOfPlayers(TeamType.HOME) < indoorTeamService.getExpectedNumberOfPlayersOnCourt()
                    || indoorTeamService.getTeamName(TeamType.GUEST).isEmpty() || indoorTeamService.getNumberOfPlayers(TeamType.GUEST) < indoorTeamService.getExpectedNumberOfPlayersOnCourt()
                    || indoorTeamService.getCaptain(TeamType.HOME) < 1 || indoorTeamService.getCaptain(TeamType.GUEST) < 1
                    || rules.getName().length() == 0) {
                Log.i("VBR-GSActivity", "Confirm button is invisible");
                mConfirmItem.setVisible(false);
            } else {
                Log.i("VBR-GSActivity", "Confirm button is visible");
                mConfirmItem.setVisible(true);
            }
        }
    }

    public void confirmSetup() {
        Log.i("VBR-GSActivity", "Validate setup");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.game_setup_title)).setMessage(getResources().getString(R.string.confirm_game_setup_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ServicesProvider.getInstance().getGameService().startMatch();
                saveTeams();
                saveRules();
                Log.i("VBR-GSActivity", "Start game activity");
                final Intent gameIntent = new Intent(GameSetupActivity.this, GameActivity.class);
                gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(gameIntent);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void saveTeams() {
        SavedTeamsService savedTeamsService = ServicesProvider.getInstance().getSavedTeamsService();
        BaseTeamService teamService = ServicesProvider.getInstance().getTeamService();
        GameType gameType = teamService.getTeamsKind();
        savedTeamsService.createAndSaveTeamFrom(gameType, teamService, TeamType.HOME);
        savedTeamsService.createAndSaveTeamFrom(gameType, teamService, TeamType.GUEST);
    }

    private void saveRules() {
        SavedRulesService savedRulesService = ServicesProvider.getInstance().getSavedRulesService();
        savedRulesService.createAndSaveRulesFrom(ServicesProvider.getInstance().getGeneralService().getRules());
    }

    private void cancelSetup() {
        Log.i("VBR-GSActivity", "Cancel setup");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.game_setup_title)).setMessage(getResources().getString(R.string.leave_game_setup_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (ServicesProvider.getInstance().getRecordedGamesService().hasSetupGame()) {
                    ServicesProvider.getInstance().getRecordedGamesService().deleteSetupGame();
                }
                UiUtils.navigateToHome(GameSetupActivity.this, false);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void initGameSetupNavigation(final BottomNavigationView gameSetupNavigation, Bundle savedInstanceState) {
        gameSetupNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        final Fragment fragment;

                        switch (item.getItemId()) {
                            case R.id.home_team_tab:
                                fragment = TeamSetupFragment.newInstance(TeamType.HOME);
                                break;
                            case R.id.guest_team_tab:
                                fragment = TeamSetupFragment.newInstance(TeamType.GUEST);
                                break;
                            case R.id.rules_tab:
                                fragment = RulesSetupFragment.newInstance();
                                break;
                            case R.id.misc_tab:
                                fragment = MiscSetupFragment.newInstance();
                                break;
                            default:
                                fragment = null;
                                break;
                        }

                        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.game_setup_container, fragment).commit();

                        return true;
                    }
                }
        );

        if (savedInstanceState == null) {
            gameSetupNavigation.setSelectedItemId(R.id.home_team_tab);
        }
    }
}
