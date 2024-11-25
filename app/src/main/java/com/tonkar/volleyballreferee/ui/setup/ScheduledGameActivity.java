package com.tonkar.volleyballreferee.ui.setup;

import android.app.*;
import android.os.Bundle;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.text.DateFormat;
import java.util.*;

public class ScheduledGameActivity extends AppCompatActivity {

    private GameSummaryDto                     mGameSummary;
    private DateFormat                         mDateFormatter;
    private DateFormat                         mTimeFormatter;
    private Calendar                           mScheduleDate;
    private MaterialButton                     mDateInputButton;
    private MaterialButton                     mTimeInputButton;
    private MaterialButton                     mGenderButton;
    private Spinner                            mHomeTeamSpinner;
    private Spinner                            mGuestTeamSpinner;
    private NameSpinnerAdapter<TeamSummaryDto> mTeamAdapter;
    private boolean                            mCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String gameDescriptionStr = getIntent().getStringExtra("game");
        mGameSummary = JsonConverters.GSON.fromJson(gameDescriptionStr, GameSummaryDto.class);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_game);

        mCreate = getIntent().getBooleanExtra("create", true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.updateToolbarLogo(toolbar, mGameSummary.getKind(), mGameSummary.getUsage());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(this);
        StoredUserService storedUserService = new StoredUserManager(this);
        StoredRulesService storedRulesService = new StoredRulesManager(this);

        mDateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        mDateFormatter.setTimeZone(TimeZone.getDefault());
        mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        mTimeFormatter.setTimeZone(TimeZone.getDefault());
        mScheduleDate = GregorianCalendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        mScheduleDate.setTimeInMillis(mGameSummary.getScheduledAt());

        mDateInputButton = findViewById(R.id.date_input_button);
        updateDate(mScheduleDate.get(Calendar.YEAR), mScheduleDate.get(Calendar.MONTH), mScheduleDate.get(Calendar.DAY_OF_MONTH));
        mDateInputButton.setOnClickListener(button -> {
            DatePickerFragment fragment = new DatePickerFragment();
            fragment.setScheduledGameActivity(ScheduledGameActivity.this);
            fragment.show(getSupportFragmentManager(), "datePicker");
        });

        mTimeInputButton = findViewById(R.id.time_input_button);
        updateTime(mScheduleDate.get(Calendar.HOUR_OF_DAY), mScheduleDate.get(Calendar.MINUTE));
        mTimeInputButton.setOnClickListener(button -> {
            TimePickerFragment fragment = new TimePickerFragment();
            fragment.setScheduledGameActivity(ScheduledGameActivity.this);
            fragment.show(getSupportFragmentManager(), "timePicker");
        });

        final AutoCompleteTextView divisionNameInput = findViewById(R.id.division_name_input_text);
        divisionNameInput.setText(mGameSummary.getDivisionName());
        divisionNameInput.setThreshold(1);
        divisionNameInput.setOnItemClickListener((parent, input, index, id) -> {
            mGameSummary.setDivisionName((String) divisionNameInput.getAdapter().getItem(index));
            computeScheduleLayoutVisibility();
        });
        divisionNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update division");
                mGameSummary.setDivisionName(s.toString().trim());
                ((TextInputLayout) findViewById(R.id.division_name_input_layout)).setError(
                        mGameSummary.getDivisionName().length() < 2 ? String.format(Locale.getDefault(),
                                                                                    getString(R.string.must_provide_at_least_n_characters),
                                                                                    2) : null);
                computeScheduleLayoutVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final AutoCompleteTextView leagueNameInput = findViewById(R.id.league_name_input_text);
        leagueNameInput.setText(mGameSummary.getLeagueName());
        leagueNameInput.setThreshold(1);
        leagueNameInput.setAdapter(
                new AutocompleteLeagueListAdapter(this, getLayoutInflater(), storedLeaguesService.listLeagues(mGameSummary.getKind())));
        leagueNameInput.setOnItemClickListener((parent, input, index, id) -> {
            LeagueSummaryDto leagueDescription = (LeagueSummaryDto) leagueNameInput.getAdapter().getItem(index);
            leagueNameInput.setText(leagueDescription.getName());
            mGameSummary.setLeagueId(leagueDescription.getId());
            mGameSummary.setLeagueName(leagueDescription.getName());
            divisionNameInput.setText("");
            divisionNameInput.setAdapter(new ArrayAdapter<>(this, R.layout.autocomplete_list_item, new ArrayList<>(
                    storedLeaguesService.listDivisionNames(leagueDescription.getId()))));
            computeScheduleLayoutVisibility();
        });

        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update league");
                mGameSummary.setLeagueName(s.toString().trim());
                findViewById(R.id.division_name_input_layout).setVisibility(
                        mGameSummary.getLeagueName().length() < 2 ? View.GONE : View.VISIBLE);
                computeScheduleLayoutVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mHomeTeamSpinner = findViewById(R.id.home_team_name_spinner);
        mHomeTeamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTeam(TeamType.HOME, mTeamAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateTeam(TeamType.HOME, null);
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
                updateTeam(TeamType.GUEST, null);
            }
        });

        updateTeamSpinners(false);

        mGenderButton = findViewById(R.id.switch_gender_button);
        updateGender(mGameSummary.getGender());
        mGenderButton.setOnClickListener(button -> {
            UiUtils.animate(ScheduledGameActivity.this, mGenderButton);
            GenderType genderType = mGameSummary.getGender().next();
            updateGender(genderType);
            updateTeamSpinners(true);
            computeScheduleLayoutVisibility();
        });

        List<RulesSummaryDto> rules = storedRulesService.listRules(mGameSummary.getKind());

        final NameSpinnerAdapter<RulesSummaryDto> rulesAdapter = new NameSpinnerAdapter<>(this, getLayoutInflater(), rules) {
            @Override
            public String getName(RulesSummaryDto rules) {
                return rules.getName();
            }

            @Override
            public String getId(RulesSummaryDto rules) {
                return rules.getId();
            }
        };

        Spinner rulesSpinner = findViewById(R.id.rules_name_spinner);
        rulesSpinner.setAdapter(rulesAdapter);
        if (rulesAdapter.getCount() > 0 && mGameSummary.getRulesId() != null) {
            rulesSpinner.setSelection(rulesAdapter.getPositionFromId(mGameSummary.getRulesId()));
        }
        rulesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRules(rulesAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateRules(null);
            }
        });

        List<FriendDto> referees = storedUserService.listReferees();

        Spinner refereeSpinner = findViewById(R.id.referee_spinner);
        NameSpinnerAdapter<FriendDto> refereeAdapter = new NameSpinnerAdapter<>(this, getLayoutInflater(), referees) {
            @Override
            public String getName(FriendDto referee) {
                return referee.getPseudo();
            }

            @Override
            public String getId(FriendDto referee) {
                return referee.getId();
            }
        };
        refereeSpinner.setAdapter(refereeAdapter);
        if (refereeAdapter.getCount() > 0) {
            refereeSpinner.setSelection(refereeAdapter.getPositionFromId(mGameSummary.getRefereedBy()));
        }
        refereeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(Tags.SETUP_UI, "Update referee");
                FriendDto referee = refereeAdapter.getItem(position);
                updateReferee(referee);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateReferee(null);
            }
        });

        computeScheduleLayoutVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getIntent().putExtra("game", JsonConverters.GSON.toJson(mGameSummary, GameSummaryDto.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scheduled_game, menu);

        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        deleteItem.setVisible(!mCreate);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (itemId == R.id.action_delete) {
            cancelGame();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateGender(GenderType genderType) {
        mGameSummary.setGender(genderType);
        switch (genderType) {
            case MIXED -> UiUtils.colorTeamButton(this, getColor(R.color.colorMixed), R.drawable.ic_mixed, mGenderButton);
            case LADIES -> UiUtils.colorTeamButton(this, getColor(R.color.colorLadies), R.drawable.ic_ladies, mGenderButton);
            case GENTS -> UiUtils.colorTeamButton(this, getColor(R.color.colorGents), R.drawable.ic_gents, mGenderButton);
        }
    }

    private void updateTeamSpinners(boolean changedGender) {
        StoredTeamsService storedTeamsService = new StoredTeamsManager(this);
        List<TeamSummaryDto> teams = storedTeamsService.listTeams(mGameSummary.getKind(), mGameSummary.getGender());

        mTeamAdapter = new NameSpinnerAdapter<>(this, getLayoutInflater(), teams) {
            @Override
            public String getName(TeamSummaryDto team) {
                return team.getName();
            }

            @Override
            public String getId(TeamSummaryDto team) {
                return team.getId();
            }
        };

        mHomeTeamSpinner.setAdapter(mTeamAdapter);
        mGuestTeamSpinner.setAdapter(mTeamAdapter);

        if (mTeamAdapter.getCount() > 0) {
            if (changedGender) {
                mHomeTeamSpinner.setSelection(0);
                mGuestTeamSpinner.setSelection(0);
                updateTeam(TeamType.HOME, mTeamAdapter.getItem(0));
                updateTeam(TeamType.GUEST, mTeamAdapter.getItem(0));
            } else {
                if (!mGameSummary.getHomeTeamName().isEmpty()) {
                    mHomeTeamSpinner.setSelection(mTeamAdapter.getPositionFromId(mGameSummary.getHomeTeamId()));
                }
                if (!mGameSummary.getGuestTeamName().isEmpty()) {
                    mGuestTeamSpinner.setSelection(mTeamAdapter.getPositionFromId(mGameSummary.getGuestTeamId()));
                }
            }
        }
    }

    private void updateTeam(TeamType teamType, TeamSummaryDto team) {
        if (TeamType.HOME.equals(teamType)) {
            mGameSummary.setHomeTeamId(team == null ? null : team.getId());
            mGameSummary.setHomeTeamName(team == null ? "" : team.getName());
        } else {
            mGameSummary.setGuestTeamId(team == null ? null : team.getId());
            mGameSummary.setGuestTeamName(team == null ? "" : team.getName());
        }
        computeScheduleLayoutVisibility();
    }

    private void updateRules(RulesSummaryDto rules) {
        mGameSummary.setRulesId(rules == null ? null : rules.getId());
        mGameSummary.setRulesName(rules == null ? "" : rules.getName());
        computeScheduleLayoutVisibility();
    }

    private void updateReferee(FriendDto referee) {
        mGameSummary.setRefereedBy(referee == null ? null : referee.getId());
        mGameSummary.setRefereeName(referee == null ? null : referee.getPseudo());
        computeScheduleLayoutVisibility();
    }

    public void updateDate(int year, int month, int day) {
        mScheduleDate.set(Calendar.YEAR, year);
        mScheduleDate.set(Calendar.MONTH, month);
        mScheduleDate.set(Calendar.DAY_OF_MONTH, day);
        mGameSummary.setScheduledAt(mScheduleDate.getTimeInMillis());
        mDateInputButton.setText(mDateFormatter.format(mScheduleDate.getTime()));
    }

    public void updateTime(int hour, int minute) {
        mScheduleDate.set(Calendar.HOUR_OF_DAY, hour);
        mScheduleDate.set(Calendar.MINUTE, minute);
        mGameSummary.setScheduledAt(mScheduleDate.getTimeInMillis());
        mTimeInputButton.setText(mTimeFormatter.format(mScheduleDate.getTime()));
    }

    private void computeScheduleLayoutVisibility() {
        View saveLayout = findViewById(R.id.schedule_game_layout);
        if (mGameSummary.getHomeTeamId() != null && mGameSummary.getGuestTeamId() != null && mGameSummary.getRulesId() != null && !mGameSummary
                .getHomeTeamName()
                .equals(mGameSummary.getGuestTeamName()) && (mGameSummary.getLeagueName().isEmpty() || (mGameSummary
                .getLeagueName()
                .length() > 1 && mGameSummary.getDivisionName().length() > 1))) {
            Log.i(Tags.SCHEDULE_UI, "Save button is visible");
            saveLayout.setVisibility(View.VISIBLE);
        } else {
            Log.i(Tags.SCHEDULE_UI, "Save button is invisible");
            saveLayout.setVisibility(View.GONE);
        }
    }

    public void scheduleGame(View view) {
        Log.i(Tags.SCHEDULE_UI, "Schedule game");

        if (mGameSummary.getLeagueId() == null && mGameSummary.getLeagueName() != null && !mGameSummary
                .getLeagueName()
                .isEmpty() && mGameSummary.getDivisionName() != null && !mGameSummary.getDivisionName().isEmpty()) {
            mGameSummary.setLeagueId(UUID.randomUUID().toString());
        }

        StoredGamesService storedGamesService = new StoredGamesManager(this);
        storedGamesService.scheduleGame(mGameSummary, mCreate, new DataSynchronizationListener() {
            @Override
            public void onSynchronizationSucceeded() {
                runOnUiThread(() -> {
                    UiUtils.makeText(ScheduledGameActivity.this, getString(R.string.sync_succeeded_message), Toast.LENGTH_LONG).show();
                    UiUtils.navigateToMain(ScheduledGameActivity.this, R.id.scheduled_games_list_fragment);
                    UiUtils.animateForward(ScheduledGameActivity.this);
                });
            }

            @Override
            public void onSynchronizationFailed() {
                runOnUiThread(() -> UiUtils
                        .makeErrorText(ScheduledGameActivity.this, getString(R.string.sync_failed_message), Toast.LENGTH_LONG)
                        .show());
            }
        });
    }

    private void cancelGame() {
        Log.i(Tags.SCHEDULE_UI, "Schedule game");
        StoredGamesService storedGamesService = new StoredGamesManager(this);
        storedGamesService.cancelGame(mGameSummary.getId(), new DataSynchronizationListener() {
            @Override
            public void onSynchronizationSucceeded() {
                runOnUiThread(() -> {
                    UiUtils.navigateToMain(ScheduledGameActivity.this, R.id.scheduled_games_list_fragment);
                    UiUtils.animateBackward(ScheduledGameActivity.this);
                });
            }

            @Override
            public void onSynchronizationFailed() {
                runOnUiThread(() -> UiUtils
                        .makeErrorText(ScheduledGameActivity.this, getString(R.string.sync_failed_message), Toast.LENGTH_LONG)
                        .show());
            }
        });
    }

    public Calendar getScheduleDate() {
        return mScheduleDate;
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private ScheduledGameActivity mActivity;

        void setScheduledGameActivity(ScheduledGameActivity activity) {
            mActivity = activity;
        }

        @Override
        public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar scheduleDate = mActivity.getScheduleDate();
            return new DatePickerDialog(mActivity, this, scheduleDate.get(Calendar.YEAR), scheduleDate.get(Calendar.MONTH),
                                        scheduleDate.get(Calendar.DAY_OF_MONTH));
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            mActivity.updateDate(year, month, day);
        }
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        private ScheduledGameActivity mActivity;

        void setScheduledGameActivity(ScheduledGameActivity activity) {
            mActivity = activity;
        }

        @Override
        public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar scheduleDate = mActivity.getScheduleDate();
            return new TimePickerDialog(mActivity, this, scheduleDate.get(Calendar.HOUR_OF_DAY), scheduleDate.get(Calendar.MINUTE), true);
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            mActivity.updateTime(hour, minute);
        }
    }
}
