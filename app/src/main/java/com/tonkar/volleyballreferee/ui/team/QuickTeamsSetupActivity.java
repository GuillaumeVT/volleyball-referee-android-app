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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.ScoreService;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TimeBasedGameService;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.game.TimeBasedGameActivity;

import java.util.ArrayList;

public class QuickTeamsSetupActivity extends AppCompatActivity {

    private TeamService  mTeamService;
    private ScoreService mScoreService;
    private MenuItem     mConfirmItem;
    private ImageButton  mGenderButton;
    private Button       mHomeTeamColorButton;
    private Button       mGuestTeamColorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-QTSActivity", "Create quick teams setup activity");
        setContentView(R.layout.activity_quick_teams_setup);

        if (!ServicesProvider.getInstance().areServicesAvailable()) {
            ServicesProvider.getInstance().restoreGameServiceForSetup(getApplicationContext());
        }

        mTeamService = ServicesProvider.getInstance().getTeamService();
        mScoreService = ServicesProvider.getInstance().getScoreService();

        setTitle("");

        mGenderButton = findViewById(R.id.switch_gender_button);

        updateGender(mTeamService.getGenderType());

        final AutoCompleteTextView leagueNameInput = findViewById(R.id.league_name_input_text);
        leagueNameInput.setThreshold(2);
        ArrayAdapter<String> leagueNameAdapter = new ArrayAdapter<>(this, R.layout.autocomplete_list_item, new ArrayList<>(ServicesProvider.getInstance().getRecordedGamesService().getRecordedLeagues()));
        leagueNameInput.setAdapter(leagueNameAdapter);
        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-QTSActivity", "Update league name");
                mTeamService.setLeagueName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final EditText homeTeamNameInput = findViewById(R.id.home_team_name_input_text);
        homeTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-QTSActivity", String.format("Update %s team name", TeamType.HOME.toString()));
                mTeamService.setTeamName(TeamType.HOME, s.toString());
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final EditText guestTeamNameInput = findViewById(R.id.guest_team_name_input_text);
        guestTeamNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-QTSActivity", String.format("Update %s team name", TeamType.GUEST.toString()));
                mTeamService.setTeamName(TeamType.GUEST, s.toString());
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        mHomeTeamColorButton = findViewById(R.id.home_team_color_button);
        mGuestTeamColorButton = findViewById(R.id.guest_team_color_button);

        leagueNameInput.setText(mTeamService.getLeagueName());
        homeTeamNameInput.setText(mTeamService.getTeamName(TeamType.HOME));
        guestTeamNameInput.setText(mTeamService.getTeamName(TeamType.GUEST));

        if (savedInstanceState == null) {
            int homeTeamColor = ShirtColors.getRandomShirtColor(this);
            teamColorSelected(TeamType.HOME, homeTeamColor);

            boolean sameColor = true;
            int guestTeamColor = 0;

            while (sameColor) {
                guestTeamColor = ShirtColors.getRandomShirtColor(this);
                sameColor = (guestTeamColor == homeTeamColor);
            }
            teamColorSelected(TeamType.GUEST, guestTeamColor);
        } else {
            teamColorSelected(TeamType.HOME, mTeamService.getTeamColor(TeamType.HOME));
            teamColorSelected(TeamType.GUEST, mTeamService.getTeamColor(TeamType.GUEST));
        }

        NumberPicker matchDurationPicker = findViewById(R.id.match_duration_picker);
        TextView matchDurationText = findViewById(R.id.match_duration_text);

        if (UsageType.TIME_SCOREBOARD.equals(mScoreService.getUsageType())) {
            final TimeBasedGameService timeBasedGameService = (TimeBasedGameService) ServicesProvider.getInstance().getGameService();
            matchDurationPicker.setWrapSelectorWheel(false);
            matchDurationPicker.setMinValue(10);
            matchDurationPicker.setMaxValue(40);
            matchDurationPicker.setValue((int) (timeBasedGameService.getDuration() / 60000L));

            matchDurationPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldValue, int newValue){
                    timeBasedGameService.setDuration(newValue * 60000L);
                }
            });
        } else {
            matchDurationPicker.setVisibility(View.GONE);
            matchDurationText.setVisibility(View.GONE);
        }

        computeConfirmItemVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ServicesProvider.getInstance().getRecordedGamesService().saveSetupGame(ServicesProvider.getInstance().getGameService());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_quick_teams_setup, menu);

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void computeConfirmItemVisibility() {
        if (mConfirmItem != null) {
            if (mTeamService.getTeamName(TeamType.HOME).isEmpty() || mTeamService.getTeamName(TeamType.GUEST).isEmpty()) {
                Log.i("VBR-QTSActivity", "Confirm button is invisible");
                mConfirmItem.setVisible(false);
            } else {
                Log.i("VBR-QTSActivity", "Confirm button is visible");
                mConfirmItem.setVisible(true);
            }
        }
    }

    public void selectHomeTeamColor(View view) {
        Log.i("VBR-QTSActivity", "Select home team color");
        UiUtils.animate(this, mHomeTeamColorButton);
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), this, getResources().getString(R.string.select_shirts_color)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(TeamType.HOME, selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    public void selectGuestTeamColor(View view) {
        Log.i("VBR-QTSActivity", "Select guest team color");
        UiUtils.animate(this, mGuestTeamColorButton);
        ColorSelectionDialog colorSelectionDialog = new ColorSelectionDialog(getLayoutInflater(), this, getResources().getString(R.string.select_shirts_color)) {
            @Override
            public void onColorSelected(int selectedColor) {
                teamColorSelected(TeamType.GUEST, selectedColor);
            }
        };
        colorSelectionDialog.show();
    }

    private void teamColorSelected(TeamType teamType, int colorId) {
        Log.i("VBR-QTSActivity", String.format("Update %s team color", teamType.toString()));
        final Button button;

        if (TeamType.HOME.equals(teamType)) {
            button = mHomeTeamColorButton;
        } else {
            button = mGuestTeamColorButton;
        }

        UiUtils.colorTeamButton(this, colorId, button);
        mTeamService.setTeamColor(teamType, colorId);
    }

    public void switchGender(View view) {
        Log.i("VBR-QTSActivity", "Switch gender");
        UiUtils.animate(this, mGenderButton);
        GenderType genderType = mTeamService.getGenderType(TeamType.HOME).next();
        updateGender(genderType);
    }

    private void updateGender(GenderType genderType) {
        mTeamService.setGenderType(genderType);
        switch (genderType) {
            case MIXED:
                mGenderButton.setImageResource(R.drawable.ic_mixed);
                mGenderButton.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                mGenderButton.setImageResource(R.drawable.ic_ladies);
                mGenderButton.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                mGenderButton.setImageResource(R.drawable.ic_gents);
                mGenderButton.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorGents), PorterDuff.Mode.SRC_IN));
                break;
        }
    }

    public void confirmTeams() {
        Log.i("VBR-QTSActivity", "Validate teams");
        mTeamService.initTeams();

        if (UsageType.TIME_SCOREBOARD.equals(mScoreService.getUsageType())) {
            Log.i("VBR-QTSActivity", "Start time-based game activity");
            final Intent gameIntent = new Intent(this, TimeBasedGameActivity.class);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(gameIntent);
        } else {
            Log.i("VBR-QTSActivity", "Start game activity");
            final Intent gameIntent = new Intent(this, GameActivity.class);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(gameIntent);
        }
    }

}
