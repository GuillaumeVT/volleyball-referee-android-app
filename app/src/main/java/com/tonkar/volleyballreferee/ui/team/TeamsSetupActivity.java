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
import android.view.View;
import android.widget.Button;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.ui.UiUtils;

public class TeamsSetupActivity extends AppCompatActivity implements TeamClient {

    private BaseTeamService mTeamService;
    private Button          mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams_setup);

        Log.i("VBR-TSActivity", "Create team setup activity");

        setTeamService(ServicesProvider.getInstance().getTeamService());

        setTitle("");

        mNextButton = findViewById(R.id.next_button);

        final ViewPager teamSetupPager = findViewById(R.id.team_setup_pager);
        teamSetupPager.setAdapter(new TeamSetupFragmentPagerAdapter(this, getSupportFragmentManager()));

        TabLayout teamSetupTabs = findViewById(R.id.team_setup_tabs);
        teamSetupTabs.setupWithViewPager(teamSetupPager);

        computeNextButtonActivation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_teams_setup, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setup_scoreboard_usage:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
                builder.setTitle(getResources().getString(R.string.scoreboard_usage_title)).setMessage(getResources().getString(R.string.scoreboard_usage_message));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mTeamService.setUsageType(UsageType.SCOREBOARD);

                        Log.i("VBR-TSActivity", "Start activity to setup teams quickly");
                        final Intent intent = new Intent(TeamsSetupActivity.this, QuickTeamsSetupActivity.class);
                        intent.putExtra("scoreboard_usage", true);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                });
                AlertDialog alertDialog = builder.show();
                UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
                return true;
            default:
                return true;
        }
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    public void computeNextButtonActivation() {
        if (mTeamService.getTeamName(TeamType.HOME).isEmpty() || mTeamService.getNumberOfPlayers(TeamType.HOME) < 6
                ||mTeamService.getTeamName(TeamType.GUEST).isEmpty() || mTeamService.getNumberOfPlayers(TeamType.GUEST) < 6) {
            Log.i("VBR-TSActivity", "Next button is disabled");
            mNextButton.setEnabled(false);
        } else {
            Log.i("VBR-TSActivity", "Next button is enabled");
            mNextButton.setEnabled(true);
        }
    }

    public void validateTeams(View view) {
        Log.i("VBR-TSActivity", "Validate teams");

        Log.i("VBR-TSActivity", "Start liberos setup activity");
        final Intent gameIntent = new Intent(this, AdditionalSetupActivity.class);
        startActivity(gameIntent);
    }

}
