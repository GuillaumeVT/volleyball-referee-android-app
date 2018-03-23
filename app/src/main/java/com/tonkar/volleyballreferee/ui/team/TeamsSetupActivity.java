package com.tonkar.volleyballreferee.ui.team;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.team.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.data.SavedTeamsService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.game.GameActivity;

public class TeamsSetupActivity extends AppCompatActivity {

    private BaseIndoorTeamService mIndoorTeamService;
    private MenuItem              mConfirmItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-TSActivity", "Create team setup activity");
        setContentView(R.layout.activity_teams_setup);

        if (!ServicesProvider.getInstance().areServicesAvailable()) {
            ServicesProvider.getInstance().restoreGameServiceForSetup(getApplicationContext());
        }

        mIndoorTeamService = (BaseIndoorTeamService) ServicesProvider.getInstance().getTeamService();

        setTitle("");

        final ViewPager teamSetupPager = findViewById(R.id.team_setup_pager);
        teamSetupPager.setAdapter(new TeamSetupFragmentPagerAdapter(this, getSupportFragmentManager()));

        TabLayout teamSetupTabs = findViewById(R.id.team_setup_tabs);
        teamSetupTabs.setupWithViewPager(teamSetupPager);

        computeConfirmItemVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ServicesProvider.getInstance().getRecordedGamesService().saveSetupGame(ServicesProvider.getInstance().getGameService());
    }

    @Override
    public void onBackPressed() {
        cancelTeams();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_teams_setup, menu);

        mConfirmItem = menu.findItem(R.id.action_confirm);
        computeConfirmItemVisibility();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_confirm:
                confirmTeams();
                return true;
            case android.R.id.home:
                cancelTeams();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void computeConfirmItemVisibility() {
        if (mConfirmItem != null) {
            if (mIndoorTeamService.getTeamName(TeamType.HOME).isEmpty() || mIndoorTeamService.getNumberOfPlayers(TeamType.HOME) < mIndoorTeamService.getExpectedNumberOfPlayersOnCourt()
                    || mIndoorTeamService.getTeamName(TeamType.GUEST).isEmpty() || mIndoorTeamService.getNumberOfPlayers(TeamType.GUEST) < mIndoorTeamService.getExpectedNumberOfPlayersOnCourt()
                    || mIndoorTeamService.getCaptain(TeamType.HOME) < 1 || mIndoorTeamService.getCaptain(TeamType.GUEST) < 1) {
                Log.i("VBR-TSActivity", "Confirm button is invisible");
                mConfirmItem.setVisible(false);
            } else {
                Log.i("VBR-TSActivity", "Confirm button is visible");
                mConfirmItem.setVisible(true);
            }
        }
    }

    public void confirmTeams() {
        Log.i("VBR-TSActivity", "Validate teams");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.teams_setup_title)).setMessage(getResources().getString(R.string.confirm_teams_setup_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mIndoorTeamService.initTeams();
                saveTeams();
                Log.i("VBR-TSActivity", "Start game activity");
                final Intent gameIntent = new Intent(TeamsSetupActivity.this, GameActivity.class);
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
        savedTeamsService.createAndSaveTeamFrom(mIndoorTeamService, TeamType.HOME);
        savedTeamsService.createAndSaveTeamFrom(mIndoorTeamService, TeamType.GUEST);
    }

    private void cancelTeams() {
        Log.i("VBR-TSActivity", "Cancel teams");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.teams_setup_title)).setMessage(getResources().getString(R.string.leave_teams_setup_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                UiUtils.navigateToHome(TeamsSetupActivity.this, false);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

}
