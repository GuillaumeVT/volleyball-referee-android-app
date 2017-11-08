package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class TeamSetupActivity extends AppCompatActivity implements TeamClient {

    private TeamType    mTeamType;
    private TeamService mTeamService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_setup);

        Log.i("VBR-TSActivity", "Create team setup activity");

        Intent intent = getIntent();
        final String teamTypeStr = intent.getStringExtra(TeamType.class.getName());
        mTeamType = TeamType.valueOf(teamTypeStr);

        setTeamService(ServicesProvider.getInstance().getTeamService());

        final EditText teamNameInput = findViewById(R.id.team_name_input_text);
        teamNameInput.setText(mTeamService.getTeamName(mTeamType));

        switch (mTeamType) {
            case HOME:
                setTitle(R.string.home_team);
                teamNameInput.setHint(R.string.home_team_hint);
                break;
            case GUEST:
                setTitle(R.string.guest_team);
                teamNameInput.setHint(R.string.guest_team_hint);
                break;
        }

        teamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-TSActivity", String.format("Update %s team name", mTeamType.toString()));
                mTeamService.setTeamName(mTeamType, s.toString());
                computeNextButtonActivation();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final Spinner teamColorSpinner = findViewById(R.id.team_color_spinner);
        final TeamColorAdapter teamColorAdapter = new TeamColorAdapter(this, getLayoutInflater());
        teamColorSpinner.setAdapter(teamColorAdapter);
        teamColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("VBR-TSActivity", String.format("Update %s team color", mTeamType.toString()));
                mTeamService.setTeamColor(mTeamType, (int) teamColorAdapter.getItem(teamColorSpinner.getSelectedItemPosition()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        teamColorSpinner.setSelection(teamColorAdapter.getRandomColorIndex());

        final GridView teamNumbersGrid = findViewById(R.id.team_member_numbers_grid);
        final PlayerAdapter playerAdapter = new PlayerAdapter(this);
        teamNumbersGrid.setAdapter(playerAdapter);

        computeNextButtonActivation();
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    private void computeNextButtonActivation() {
        final Button nextButton = findViewById(R.id.next_button);

        if (mTeamService.getTeamName(mTeamType).isEmpty() || mTeamService.getNumberOfPlayers(mTeamType) < 6) {
            Log.i("VBR-TSActivity", "Next button is disabled");
            nextButton.setEnabled(false);
        } else {
            Log.i("VBR-TSActivity", "Next button is enabled");
            nextButton.setEnabled(true);
        }
    }

    public void validateTeam(View view) {
        Log.i("VBR-TSActivity", "Validate team");

        switch (mTeamType) {
            case HOME:
                Log.i("VBR-TSActivity", String.format("Start activity to setup %s team", TeamType.GUEST.toString()));
                final Intent setupIntent = new Intent(this, TeamSetupActivity.class);
                setupIntent.putExtra(TeamType.class.getName(), TeamType.GUEST.toString());
                startActivity(setupIntent);
                break;
            case GUEST:
                Log.i("VBR-TSActivity", "Start game activity");
                final Intent gameIntent = new Intent(this, GameActivity.class);
                startActivity(gameIntent);
                break;
        }
    }

    private class PlayerAdapter extends BaseAdapter {

        private final Context mContext;

        private PlayerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return 40;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int playerShirtNumber = position + 1;
            ToggleButton button;

            if (convertView == null) {
                button = new ToggleButton(mContext);
            } else {
                button = (ToggleButton) convertView;
            }

            button.setText(String.valueOf(playerShirtNumber));
            button.setTextOn(String.valueOf(playerShirtNumber));
            button.setTextOff(String.valueOf(playerShirtNumber));
            button.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            button.setChecked(mTeamService.hasPlayer(mTeamType, playerShirtNumber));
            button.setTextSize(16);

            button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final int number = Integer.parseInt(buttonView.getText().toString());
                    if (isChecked) {
                        Log.i("VBR-TSActivity", String.format("Checked #%d player of %s team", number, mTeamType.toString()));
                        mTeamService.addPlayer(mTeamType, number);
                    } else {
                        Log.i("VBR-TSActivity", String.format("Unchecked #%d player of %s team", number, mTeamType.toString()));
                        mTeamService.removePlayer(mTeamType, number);
                    }
                    computeNextButtonActivation();
                }
            });

            return button;
        }
    }

}
