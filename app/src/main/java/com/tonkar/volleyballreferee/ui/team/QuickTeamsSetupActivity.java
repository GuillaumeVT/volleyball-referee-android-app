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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class QuickTeamsSetupActivity extends AppCompatActivity {

    private TeamService mTeamService;
    private MenuItem    mConfirmItem;
    private ImageButton mGenderButton;
    private Button      mHomeTeamColorButton;
    private Button      mGuestTeamColorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-QTSActivity", "Create quick teams setup activity");
        setContentView(R.layout.activity_quick_teams_setup);

        if (!ServicesProvider.getInstance().areServicesAvailable()) {
            ServicesProvider.getInstance().restoreGameServiceForSetup(getApplicationContext());
        }

        mTeamService = ServicesProvider.getInstance().getTeamService();

        setTitle("");

        mGenderButton = findViewById(R.id.switch_gender_button);

        updateGender(mTeamService.getGenderType());

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

        homeTeamNameInput.setText(mTeamService.getTeamName(TeamType.HOME));
        guestTeamNameInput.setText(mTeamService.getTeamName(TeamType.GUEST));

        if (savedInstanceState == null) {
            // Coming for the teams setup activity, the color are kept
            if (getIntent().getBooleanExtra("scoreboard_usage", false)) {
                teamColorSelected(TeamType.HOME, mTeamService.getTeamColor(TeamType.HOME));
                teamColorSelected(TeamType.GUEST, mTeamService.getTeamColor(TeamType.GUEST));
            } else {
                int homeTeamColor = ShirtColors.getRandomShirtColor(this);
                teamColorSelected(TeamType.HOME, homeTeamColor);

                boolean sameColor = true;
                int guestTeamColor = 0;

                while (sameColor) {
                    guestTeamColor = ShirtColors.getRandomShirtColor(this);
                    sameColor = (guestTeamColor == homeTeamColor);
                }
                teamColorSelected(TeamType.GUEST, guestTeamColor);
            }
        } else {
            teamColorSelected(TeamType.HOME, mTeamService.getTeamColor(TeamType.HOME));
            teamColorSelected(TeamType.GUEST, mTeamService.getTeamColor(TeamType.GUEST));
        }

        computeConfirmItemVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ServicesProvider.getInstance().getGamesHistoryService().saveSetupGame(ServicesProvider.getInstance().getGameService());
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

        Log.i("VBR-QTSActivity", "Start game activity");
        final Intent gameIntent = new Intent(this, GameActivity.class);
        startActivity(gameIntent);
    }

}
