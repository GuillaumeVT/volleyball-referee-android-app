package com.tonkar.volleyballreferee.ui.setup;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.data.*;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.*;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.text.DateFormat;
import java.util.*;

public class ScheduledGameActivity extends AppCompatActivity {

    private ApiGameDescription                     mGameDescription;
    private DateFormat                             mDateFormatter;
    private DateFormat                             mTimeFormatter;
    private Calendar                               mScheduleDate;
    private MaterialButton                         mDateInputButton;
    private MaterialButton                         mTimeInputButton;
    private FloatingActionButton                   mGenderButton;
    private Spinner                                mHomeTeamSpinner;
    private Spinner                                mGuestTeamSpinner;
    private NameSpinnerAdapter<ApiTeamDescription> mTeamAdapter;
    private MenuItem                               mSaveItem;
    private MenuItem                               mDeleteItem;
    private boolean                                mCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String gameDescriptionStr = getIntent().getStringExtra("game");
        mGameDescription = JsonIOUtils.GSON.fromJson(gameDescriptionStr, JsonIOUtils.GAME_DESCRIPTION_TYPE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_game);

        mCreate = getIntent().getBooleanExtra("create", true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.updateToolbarLogo(toolbar, mGameDescription.getKind(), mGameDescription.getUsage());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        StoredLeaguesService storedLeaguesService = new StoredLeagues(this);
        StoredUserService storedUserService = new StoredUser(this);
        StoredRulesService storedRulesService = new StoredRules(this);

        mDateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        mDateFormatter.setTimeZone(TimeZone.getDefault());
        mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        mTimeFormatter.setTimeZone(TimeZone.getDefault());
        mScheduleDate = GregorianCalendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        mScheduleDate.setTimeInMillis(mGameDescription.getScheduledAt());

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
        divisionNameInput.setThreshold(2);
        divisionNameInput.setOnItemClickListener((parent, input, index, id) -> {
            mGameDescription.setDivisionName((String) divisionNameInput.getAdapter().getItem(index));
            computeItemsVisibility();
        });
        divisionNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update division");
                mGameDescription.setDivisionName(s.toString().trim());
                ((TextInputLayout)findViewById(R.id.division_name_input_layout)).setError(count < 2 ? String.format(Locale.getDefault(), getString(R.string.must_provide_at_least_n_characters), 2) : null);
                computeItemsVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        divisionNameInput.setText(mGameDescription.getDivisionName());

        final AutoCompleteTextView leagueNameInput = findViewById(R.id.league_name_input_text);
        leagueNameInput.setThreshold(2);
        leagueNameInput.setAdapter(new AutocompleteLeagueListAdapter(this, getLayoutInflater(), storedLeaguesService.listLeagues(mGameDescription.getKind())));
        leagueNameInput.setOnItemClickListener((parent, input, index, id) -> {
            ApiLeagueDescription leagueDescription = (ApiLeagueDescription) leagueNameInput.getAdapter().getItem(index);
            leagueNameInput.setText(leagueDescription.getName());
            mGameDescription.setLeagueId(leagueDescription.getId());
            mGameDescription.setLeagueName(leagueDescription.getName());
            divisionNameInput.setText("");
            divisionNameInput.setAdapter(new ArrayAdapter<>(this, R.layout.autocomplete_list_item, new ArrayList<>(storedLeaguesService.listDivisionNames(leagueDescription.getId()))));
            computeItemsVisibility();
        });

        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.SETUP_UI, "Update league");
                mGameDescription.setLeagueName(s.toString().trim());
                findViewById(R.id.division_name_input_layout).setVisibility(count == 0 ? View.GONE : View.VISIBLE);
                computeItemsVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        leagueNameInput.setText(mGameDescription.getLeagueName());

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
        updateGender(mGameDescription.getGender());
        mGenderButton.setOnClickListener(button -> {
            UiUtils.animate(ScheduledGameActivity.this, mGenderButton);
            GenderType genderType = mGameDescription.getGender().next();
            updateGender(genderType);
            updateTeamSpinners(true);
            computeItemsVisibility();
        });

        List<ApiRulesDescription> rules = storedRulesService.listRules(mGameDescription.getKind());


        final NameSpinnerAdapter<ApiRulesDescription> rulesAdapter = new NameSpinnerAdapter<ApiRulesDescription>(this, getLayoutInflater(), rules) {
            @Override
            public String getName(ApiRulesDescription rules) {
                return rules.getName();
            }

            @Override
            public String getId(ApiRulesDescription rules) {
                return rules.getId();
            }
        };

        Spinner rulesSpinner = findViewById(R.id.rules_name_spinner);
        rulesSpinner.setAdapter(rulesAdapter);
        if (rulesAdapter.getCount() > 0 && mGameDescription.getRulesId() != null) {
            rulesSpinner.setSelection(rulesAdapter.getPositionFromId(mGameDescription.getRulesId()));
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

        List<ApiFriend> referees = storedUserService.listReferees();

        Spinner refereeSpinner = findViewById(R.id.referee_spinner);
        NameSpinnerAdapter<ApiFriend> refereeAdapter = new NameSpinnerAdapter<ApiFriend>(this, getLayoutInflater(), referees) {
            @Override
            public String getName(ApiFriend referee) {
                return referee.getPseudo();
            }

            @Override
            public String getId(ApiFriend referee) {
                return referee.getId();
            }
        };
        refereeSpinner.setAdapter(refereeAdapter);
        if (refereeAdapter.getCount() > 0) {
            refereeSpinner.setSelection(refereeAdapter.getPositionFromId(mGameDescription.getRefereedBy()));
        }
        refereeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(Tags.SETUP_UI, "Update referee");
                ApiFriend referee = refereeAdapter.getItem(position);
                updateReferee(referee);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateReferee(null);
            }
        });

        computeItemsVisibility();
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

        mSaveItem = menu.findItem(R.id.action_save);
        mDeleteItem = menu.findItem(R.id.action_delete);
        computeItemsVisibility();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                scheduleGame();
                return true;
            case R.id.action_delete:
                cancelGame();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateGender(GenderType genderType) {
        mGameDescription.setGender(genderType);
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
        StoredTeamsService storedTeamsService = new StoredTeams(this);
        List<ApiTeamDescription> teams = storedTeamsService.listTeams(mGameDescription.getKind(), mGameDescription.getGender());

        mTeamAdapter = new NameSpinnerAdapter<ApiTeamDescription>(this, getLayoutInflater(), teams) {
            @Override
            public String getName(ApiTeamDescription team) {
                return team.getName();
            }

            @Override
            public String getId(ApiTeamDescription team) {
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
                if (!mGameDescription.getHomeTeamName().isEmpty()) {
                    mHomeTeamSpinner.setSelection(mTeamAdapter.getPositionFromId(mGameDescription.getHomeTeamId()));
                }
                if (!mGameDescription.getGuestTeamName().isEmpty()) {
                    mGuestTeamSpinner.setSelection(mTeamAdapter.getPositionFromId(mGameDescription.getGuestTeamId()));
                }
            }
        }
    }

    private void updateTeam(TeamType teamType, ApiTeamDescription team) {
        if (TeamType.HOME.equals(teamType)) {
            mGameDescription.setHomeTeamId(team == null ? null : team.getId());
            mGameDescription.setHomeTeamName(team == null ? "" : team.getName());
        } else {
            mGameDescription.setGuestTeamId(team == null ? null : team.getId());
            mGameDescription.setGuestTeamName(team == null ? "" : team.getName());
        }
        computeItemsVisibility();
    }

    private void updateRules(ApiRulesDescription rules) {
        mGameDescription.setRulesId(rules == null ? null : rules.getId());
        mGameDescription.setRulesName(rules == null ? "" : rules.getName());
        computeItemsVisibility();
    }

    private void updateReferee(ApiFriend referee) {
        mGameDescription.setRefereedBy(referee == null ? Authentication.VBR_USER_ID : referee.getId());
        mGameDescription.setRefereeName(referee == null ? "" : referee.getPseudo());
        computeItemsVisibility();
    }

    public void updateDate(int year, int month, int day) {
        mScheduleDate.set(Calendar.YEAR, year);
        mScheduleDate.set(Calendar.MONTH, month);
        mScheduleDate.set(Calendar.DAY_OF_MONTH, day);
        mGameDescription.setScheduledAt(mScheduleDate.getTimeInMillis());
        mDateInputButton.setText(mDateFormatter.format(mScheduleDate.getTime()));
    }

    public void updateTime(int hour, int minute) {
        mScheduleDate.set(Calendar.HOUR_OF_DAY, hour);
        mScheduleDate.set(Calendar.MINUTE, minute);
        mGameDescription.setScheduledAt(mScheduleDate.getTimeInMillis());
        mTimeInputButton.setText(mTimeFormatter.format(mScheduleDate.getTime()));
    }

    private void computeItemsVisibility() {
        if (mDeleteItem != null && mSaveItem != null) {
            mDeleteItem.setVisible(!mCreate);
            mSaveItem.setVisible(
                    mGameDescription.getHomeTeamId() != null && mGameDescription.getGuestTeamId() != null && mGameDescription.getRulesId() != null
                            && !mGameDescription.getHomeTeamName().equals(mGameDescription.getGuestTeamName())
                            && (mGameDescription.getLeagueName().isEmpty() || (mGameDescription.getLeagueName().length() > 1 && mGameDescription.getDivisionName().length() > 1)));
        }
    }

    private void scheduleGame() {
        Log.i(Tags.SCHEDULE_UI, "Schedule game");
        StoredGamesService storedGamesService = new StoredGames(this);
        storedGamesService.scheduleGame(mGameDescription, mCreate,
                new DataSynchronizationListener() {
                    @Override
                    public void onSynchronizationSucceeded() {
                        UiUtils.makeText(ScheduledGameActivity.this, getString(R.string.sync_succeeded_message), Toast.LENGTH_LONG).show();
                        final Intent intent = new Intent(ScheduledGameActivity.this, ScheduledGamesListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        UiUtils.animateCreate(ScheduledGameActivity.this);
                    }

                    @Override
                    public void onSynchronizationFailed() {
                        UiUtils.makeErrorText(ScheduledGameActivity.this, getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void cancelGame() {
        Log.i(Tags.SCHEDULE_UI, "Schedule game");
        StoredGamesService storedGamesService = new StoredGames(this);
        storedGamesService.cancelGame(mGameDescription.getId(),
                new DataSynchronizationListener() {
                    @Override
                    public void onSynchronizationSucceeded() {
                        final Intent intent = new Intent(ScheduledGameActivity.this, ScheduledGamesListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        UiUtils.animateBackward(ScheduledGameActivity.this);
                    }

                    @Override
                    public void onSynchronizationFailed() {
                        UiUtils.makeErrorText(ScheduledGameActivity.this, getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
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

        void setScheduledGameActivity(ScheduledGameActivity activity) {
            mActivity = activity;
        }

        @Override
        public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar scheduleDate = mActivity.getScheduleDate();
            return new TimePickerDialog(mActivity, this, scheduleDate.get(Calendar.HOUR_OF_DAY), scheduleDate.get(Calendar.MINUTE),true);
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            mActivity.updateTime(hour, minute);
        }
    }
}
