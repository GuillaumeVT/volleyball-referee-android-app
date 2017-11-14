package com.tonkar.volleyballreferee.ui.team;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class TeamsSetupActivity extends AppCompatActivity implements TeamClient {

    private IndoorTeamService mTeamService;
    private Button            mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams_setup);

        Log.i("VBR-TSActivity", "Create team setup activity");

        setTeamService(ServicesProvider.getInstance().getTeamService());

        mNextButton = findViewById(R.id.next_button);

        final ViewPager teamSetupPager = findViewById(R.id.team_setup_pager);
        teamSetupPager.setAdapter(new TeamSetupFragmentPagerAdapter(this, getSupportFragmentManager()));

        TabLayout teamSetupTabs = findViewById(R.id.team_setup_tabs);
        teamSetupTabs.setupWithViewPager(teamSetupPager);

        computeNextButtonActivation();
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = (IndoorTeamService) teamService;
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
        final Intent gameIntent = new Intent(this, LiberosSetupActivity.class);
        startActivity(gameIntent);
    }

}
