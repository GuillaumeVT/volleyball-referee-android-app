package com.tonkar.volleyballreferee.ui.team;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class QuickTeamsSetupActivity extends AppCompatActivity implements TeamClient {

    private TeamService mTeamService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_teams_setup);

        Log.i("VBR-QTSActivity", "Create quick teams setup activity");

        setTeamService(ServicesProvider.getInstance().getTeamService());

        final EditText homeTeamNameInput = findViewById(R.id.home_team_name_input_text);
        homeTeamNameInput.setText(mTeamService.getTeamName(TeamType.HOME));
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
        guestTeamNameInput.setText(mTeamService.getTeamName(TeamType.GUEST));
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

        final Spinner homeTeamColorSpinner = findViewById(R.id.home_team_color_spinner);
        final TeamColorAdapter homeTeamColorAdapter = new TeamColorAdapter(this, getLayoutInflater());
        homeTeamColorSpinner.setAdapter(homeTeamColorAdapter);
        homeTeamColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("VBR-QTSActivity", String.format("Update %s team color", TeamType.HOME.toString()));
                mTeamService.setTeamColor(TeamType.HOME, (int) homeTeamColorAdapter.getItem(homeTeamColorSpinner.getSelectedItemPosition()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        homeTeamColorSpinner.setSelection(homeTeamColorAdapter.getRandomColorIndex());

        final Spinner guestTeamColorSpinner = findViewById(R.id.guest_team_color_spinner);
        final TeamColorAdapter guestTeamColorAdapter = new TeamColorAdapter(this, getLayoutInflater());
        guestTeamColorSpinner.setAdapter(guestTeamColorAdapter);
        guestTeamColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("VBR-QTSActivity", String.format("Update %s team color", TeamType.GUEST.toString()));
                mTeamService.setTeamColor(TeamType.GUEST, (int) guestTeamColorAdapter.getItem(guestTeamColorSpinner.getSelectedItemPosition()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        boolean sameColor = true;
        int guestTeamColorIndex = 0;

        while (sameColor) {
            guestTeamColorIndex = guestTeamColorAdapter.getRandomColorIndex();
            sameColor = (guestTeamColorIndex == homeTeamColorSpinner.getSelectedItemPosition());
        }

        guestTeamColorSpinner.setSelection(guestTeamColorIndex);

        computeNextButtonActivation();
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    private void computeNextButtonActivation() {
        final Button nextButton = findViewById(R.id.next_button);

        if (mTeamService.getTeamName(TeamType.HOME).isEmpty() || mTeamService.getTeamName(TeamType.GUEST).isEmpty()) {
            Log.i("VBR-QTSActivity", "Next button is disabled");
            nextButton.setEnabled(false);
        } else {
            Log.i("VBR-QTSActivity", "Next button is enabled");
            nextButton.setEnabled(true);
        }
    }

    public void validateTeams(View view) {
        Log.i("VBR-QTSActivity", "Validate teams");

        Log.i("VBR-QTSActivity", "Start game activity");
        final Intent gameIntent = new Intent(this, GameActivity.class);
        startActivity(gameIntent);
    }
}
