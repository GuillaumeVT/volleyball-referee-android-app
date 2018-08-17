package com.tonkar.volleyballreferee.ui.setup;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.GameDescription;
import com.tonkar.volleyballreferee.business.data.JsonIOUtils;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ScheduledGameActivity extends AppCompatActivity {

    private static final String TAG = "VBR-ScheduledUI";

    private GameDescription      mGameDescription;
    private DateFormat           mDateFormatter;
    private DateFormat           mTimeFormatter;
    private Calendar             mScheduleDate;
    private MaterialButton       mDateInputButton;
    private MaterialButton       mTimeInputButton;
    private FloatingActionButton mGenderButton;
    private Spinner              mHomeTeamSpinner;
    private Spinner              mGuestTeamSpinner;
    private ArrayAdapter<String> mTeamAdapter;
    private MenuItem             mConfirmItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_game);

        ServicesProvider.getInstance().restoreGameService(getApplicationContext());

        String gameDescriptionStr = getIntent().getStringExtra("game");
        mGameDescription = JsonIOUtils.GSON.fromJson(gameDescriptionStr, JsonIOUtils.GAME_DESCRIPTION_TYPE);

        mDateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        mDateFormatter.setTimeZone(TimeZone.getDefault());
        mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        mTimeFormatter.setTimeZone(TimeZone.getDefault());
        mScheduleDate = GregorianCalendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        mScheduleDate.setTimeInMillis(mGameDescription.getGameSchedule());

        mDateInputButton = findViewById(R.id.date_input_button);
        updateDate(mScheduleDate.get(Calendar.YEAR), mScheduleDate.get(Calendar.MONTH), mScheduleDate.get(Calendar.DAY_OF_MONTH));
        mDateInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.setScheduledGameActivity(ScheduledGameActivity.this);
                fragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        mTimeInputButton = findViewById(R.id.time_input_button);
        updateTime(mScheduleDate.get(Calendar.HOUR_OF_DAY), mScheduleDate.get(Calendar.MINUTE));
        mTimeInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment fragment = new TimePickerFragment();
                fragment.setScheduledGameActivity(ScheduledGameActivity.this);
                fragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        final AutoCompleteTextView leagueNameInput = findViewById(R.id.league_name_input_text);
        leagueNameInput.setThreshold(2);
        ArrayAdapter<String> leagueNameAdapter = new ArrayAdapter<>(this, R.layout.autocomplete_list_item, new ArrayList<>(ServicesProvider.getInstance().getRecordedGamesService().getRecordedLeagues()));
        leagueNameInput.setAdapter(leagueNameAdapter);
        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(TAG, "Update league name");
                mGameDescription.setLeagueName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        leagueNameInput.setText(mGameDescription.getLeagueName());

        final AutoCompleteTextView divisionNameInput = findViewById(R.id.division_name_input_text);
        divisionNameInput.setThreshold(2);
        ArrayAdapter<String> divisionNameAdapter = new ArrayAdapter<>(this, R.layout.autocomplete_list_item, new ArrayList<>(ServicesProvider.getInstance().getRecordedGamesService().getRecordedDivisions()));
        divisionNameInput.setAdapter(divisionNameAdapter);
        divisionNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(TAG, "Update division name");
                mGameDescription.setDivisionName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        divisionNameInput.setText(mGameDescription.getDivisionName());

        mHomeTeamSpinner = findViewById(R.id.home_team_name_spinner);
        mHomeTeamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTeam(TeamType.HOME, mTeamAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateTeam(TeamType.HOME, "");
            }
        });

        mGuestTeamSpinner = findViewById(R.id.guest_team_name_spinner);
        mGuestTeamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTeam(TeamType.GUEST, mTeamAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateTeam(TeamType.GUEST, "");
            }
        });

        updateTeamSpinners(false);

        mGenderButton = findViewById(R.id.switch_gender_button);
        updateGender(mGameDescription.getGenderType());
        mGenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.animate(ScheduledGameActivity.this, mGenderButton);
                GenderType genderType = mGameDescription.getGenderType().next();
                updateGender(genderType);
                updateTeamSpinners(true);
                computeConfirmItemVisibility();
            }
        });

        List<String> rulesNames = new ArrayList<>();
        for (Rules rules : ServicesProvider.getInstance().getSavedRulesService().getSavedRules()) {
            rulesNames.add(rules.getName());
        }

        final ArrayAdapter<String> rulesAdapter = new ArrayAdapter<>(this, R.layout.rule_spinner, rulesNames);
        rulesAdapter.setDropDownViewResource(R.layout.rule_entry);

        Spinner rulesSpinner = findViewById(R.id.rules_name_spinner);
        rulesSpinner.setAdapter(rulesAdapter);
        if (rulesAdapter.getCount() > 0 && !mGameDescription.getRulesName().isEmpty()) {
            rulesSpinner.setSelection(rulesAdapter.getPosition(mGameDescription.getRulesName()));
        }
        rulesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRules(rulesAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateRules("");
            }
        });

        updateRules(mGameDescription.getRulesName());

        final TextInputEditText refereeNameInput = findViewById(R.id.referee_name_input_text);
        refereeNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(TAG, "Update referee name");
                mGameDescription.setRefereeName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        refereeNameInput.setText(mGameDescription.getRefereeName());

        computeConfirmItemVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getIntent().putExtra("game", JsonIOUtils.GSON.toJson(mGameDescription, JsonIOUtils.GAME_DESCRIPTION_TYPE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scheduled_game, menu);

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateGender(GenderType genderType) {
        mGameDescription.setGenderType(genderType);
        switch (genderType) {
            case MIXED:
                mGenderButton.setImageResource(R.drawable.ic_mixed);
                mGenderButton.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                mGenderButton.setImageResource(R.drawable.ic_ladies);
                mGenderButton.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                mGenderButton.setImageResource(R.drawable.ic_gents);
                mGenderButton.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorGents), PorterDuff.Mode.SRC_IN));
                break;
        }
    }

    private void updateTeamSpinners(boolean changedGender) {
        List<String> teamNames = ServicesProvider.getInstance().getSavedTeamsService().getSavedTeamNameList(
                mGameDescription.getGameType(), mGameDescription.getGenderType());

        mTeamAdapter = new ArrayAdapter<>(this, R.layout.rule_spinner, teamNames);
        mTeamAdapter.setDropDownViewResource(R.layout.rule_entry);

        mHomeTeamSpinner.setAdapter(mTeamAdapter);
        mGuestTeamSpinner.setAdapter(mTeamAdapter);

        if (mTeamAdapter.getCount() > 0) {
            if (changedGender) {
                mHomeTeamSpinner.setSelection(0);
                mGuestTeamSpinner.setSelection(0);
                updateTeam(TeamType.HOME, mTeamAdapter.getItem(0));
                updateTeam(TeamType.GUEST, mTeamAdapter.getItem(0));
            } else {
                if (!mGameDescription.getHomeTeamName().isEmpty()) {
                    mHomeTeamSpinner.setSelection(mTeamAdapter.getPosition(mGameDescription.getHomeTeamName()));
                }
                if (!mGameDescription.getGuestTeamName().isEmpty()) {
                    mGuestTeamSpinner.setSelection(mTeamAdapter.getPosition(mGameDescription.getGuestTeamName()));
                }
            }
        }
    }

    private void updateTeam(TeamType teamType, String teamName) {
        if (TeamType.HOME.equals(teamType)) {
            mGameDescription.setHomeTeamName(teamName);
        } else {
            mGameDescription.setGuestTeamName(teamName);
        }
        computeConfirmItemVisibility();
    }

    private void updateRules(String rulesName) {
        mGameDescription.setRulesName(rulesName);
        computeConfirmItemVisibility();
    }

    public void updateDate(int year, int month, int day) {
        mScheduleDate.set(Calendar.YEAR, year);
        mScheduleDate.set(Calendar.MONTH, month);
        mScheduleDate.set(Calendar.DAY_OF_MONTH, day);
        mGameDescription.setGameSchedule(mScheduleDate.getTimeInMillis());
        mDateInputButton.setText(mDateFormatter.format(mScheduleDate.getTime()));
    }

    public void updateTime(int hour, int minute) {
        mScheduleDate.set(Calendar.HOUR_OF_DAY, hour);
        mScheduleDate.set(Calendar.MINUTE, minute);
        mGameDescription.setGameSchedule(mScheduleDate.getTimeInMillis());
        mTimeInputButton.setText(mTimeFormatter.format(mScheduleDate.getTime()));
    }

    private void computeConfirmItemVisibility() {
        if (mConfirmItem != null) {
            if (mGameDescription.getHomeTeamName().isEmpty() || mGameDescription.getGuestTeamName().isEmpty() || mGameDescription.getRulesName().isEmpty()) {
                Log.i(TAG, "Confirm button is invisible");
                mConfirmItem.setVisible(false);
            } else {
                Log.i(TAG, "Confirm button is visible");
                mConfirmItem.setVisible(true);
            }
        }
    }

    private void confirmSetup() {
        Log.i(TAG, "Validate schedule");
        ServicesProvider.getInstance().getRecordedGamesService().scheduleUserGameOnline(mGameDescription,
                new DataSynchronizationListener() {
                    @Override
                    public void onSynchronizationSucceeded() {
                        final Intent intent = new Intent(ScheduledGameActivity.this, ScheduledGamesListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onSynchronizationFailed() {
                        Toast.makeText(ScheduledGameActivity.this, getResources().getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public Calendar getScheduleDate() {
        return mScheduleDate;
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private ScheduledGameActivity mActivity;

        public void setScheduledGameActivity(ScheduledGameActivity activity) {
            mActivity = activity;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar scheduleDate = mActivity.getScheduleDate();
            return new DatePickerDialog(mActivity, this,
                    scheduleDate.get(Calendar.YEAR), scheduleDate.get(Calendar.MONTH), scheduleDate.get(Calendar.DAY_OF_MONTH));
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            mActivity.updateDate(year, month, day);
        }
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        private ScheduledGameActivity mActivity;

        public void setScheduledGameActivity(ScheduledGameActivity activity) {
            mActivity = activity;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar scheduleDate = mActivity.getScheduleDate();
            return new TimePickerDialog(mActivity, this, scheduleDate.get(Calendar.HOUR_OF_DAY), scheduleDate.get(Calendar.MINUTE),true);
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            mActivity.updateTime(hour, minute);
        }
    }
}
