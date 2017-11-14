package com.tonkar.volleyballreferee.ui.team;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class QuickTeamsSetupActivity extends AppCompatActivity implements TeamClient {

    private TeamService mTeamService;
    private Button      mNextButton;
    private Button      mHomeTeamColorButton;
    private Button      mGuestTeamColorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_teams_setup);

        Log.i("VBR-QTSActivity", "Create quick teams setup activity");

        setTeamService(ServicesProvider.getInstance().getTeamService());

        mNextButton = findViewById(R.id.next_button);

        final EditText homeTeamNameInput = findViewById(R.id.home_team_name_input_text);
        homeTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-QTSActivity", String.format("Update %s team name", TeamType.HOME.toString()));
                mTeamService.setTeamName(TeamType.HOME, s.toString());
                computeNextButtonActivation();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final EditText guestTeamNameInput = findViewById(R.id.guest_team_name_input_text);
        guestTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-QTSActivity", String.format("Update %s team name", TeamType.GUEST.toString()));
                mTeamService.setTeamName(TeamType.GUEST, s.toString());
                computeNextButtonActivation();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        mHomeTeamColorButton = findViewById(R.id.home_team_color_button);
        mGuestTeamColorButton = findViewById(R.id.guest_team_color_button);

        if (savedInstanceState == null) {
            int homeTeamColor = ShirtColors.getRandomShirtColor();
            onTeamColorSelected(TeamType.HOME, homeTeamColor);

            boolean sameColor = true;
            int guestTeamColor = 0;

            while (sameColor) {
                guestTeamColor = ShirtColors.getRandomShirtColor();
                sameColor = (guestTeamColor == homeTeamColor);
            }
            onTeamColorSelected(TeamType.GUEST, guestTeamColor);
        } else {
            homeTeamNameInput.setText(mTeamService.getTeamName(TeamType.HOME));
            guestTeamNameInput.setText(mTeamService.getTeamName(TeamType.GUEST));

            onTeamColorSelected(TeamType.HOME, mTeamService.getTeamColor(TeamType.HOME));
            onTeamColorSelected(TeamType.GUEST, mTeamService.getTeamColor(TeamType.GUEST));

            TeamColorDialogFragment teamColorDialogFragment = (TeamColorDialogFragment) getFragmentManager().findFragmentByTag("select_home_team_color");
            if (teamColorDialogFragment != null) {
                initTeamColorSelectionListener(TeamType.HOME, teamColorDialogFragment);
            }

            teamColorDialogFragment = (TeamColorDialogFragment) getFragmentManager().findFragmentByTag("select_guest_team_color");
            if (teamColorDialogFragment != null) {
                initTeamColorSelectionListener(TeamType.GUEST, teamColorDialogFragment);
            }
        }

        computeNextButtonActivation();
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    private void computeNextButtonActivation() {
        if (mTeamService.getTeamName(TeamType.HOME).isEmpty() || mTeamService.getTeamName(TeamType.GUEST).isEmpty()) {
            Log.i("VBR-QTSActivity", "Next button is disabled");
            mNextButton.setEnabled(false);
        } else {
            Log.i("VBR-QTSActivity", "Next button is enabled");
            mNextButton.setEnabled(true);
        }
    }

    public void selectHomeTeamColor(View view) {
        Log.i("VBR-TSActivity", "Select home team color");
        TeamColorDialogFragment teamColorDialogFragment = TeamColorDialogFragment.newInstance();
        initTeamColorSelectionListener(TeamType.HOME, teamColorDialogFragment);
        teamColorDialogFragment.show(getFragmentManager(), "select_home_team_color");
    }

    public void selectGuestTeamColor(View view) {
        Log.i("VBR-TSActivity", "Select guest team color");
        TeamColorDialogFragment teamColorDialogFragment = TeamColorDialogFragment.newInstance();
        initTeamColorSelectionListener(TeamType.GUEST, teamColorDialogFragment);
        teamColorDialogFragment.show(getFragmentManager(), "select_guest_team_color");
    }

    private void initTeamColorSelectionListener(final TeamType teamType, TeamColorDialogFragment teamColorDialogFragment) {
        teamColorDialogFragment.setTeamColorSelectionListener(new TeamColorDialogFragment.TeamColorSelectionListener() {
            @Override
            public void onTeamColorSelected(int colorId) {
                QuickTeamsSetupActivity.this.onTeamColorSelected(teamType, colorId);
            }
        });
    }

    private void onTeamColorSelected(TeamType teamType, int colorId) {
        Log.i("VBR-TSActivity", String.format("Update %s team color", teamType.toString()));
        final Button button;

        if (TeamType.HOME.equals(teamType)) {
            button = mHomeTeamColorButton;
        } else {
            button = mGuestTeamColorButton;
        }

        button.getBackground().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(QuickTeamsSetupActivity.this, colorId), PorterDuff.Mode.SRC));
        mTeamService.setTeamColor(teamType, colorId);
    }

    public void validateTeams(View view) {
        Log.i("VBR-QTSActivity", "Validate teams");

        Log.i("VBR-QTSActivity", "Start game activity");
        final Intent gameIntent = new Intent(this, GameActivity.class);
        startActivity(gameIntent);
    }
}
