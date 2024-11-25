package com.tonkar.volleyballreferee.ui.rules;

import android.os.Bundle;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.RulesSummaryDto;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.data.rules.StoredRulesActivity;
import com.tonkar.volleyballreferee.ui.interfaces.RulesHandler;
import com.tonkar.volleyballreferee.ui.setup.*;

import java.util.Locale;

public class RulesSetupFragment extends Fragment implements RulesHandler {

    private Rules mRules;

    private AutoCompleteTextView mRulesNameInput;
    private Spinner              mSetsPerGameSpinner;
    private Spinner              mPointsPerSetSpinner;
    private SwitchCompat         mTieBreakSwitch;
    private Spinner              mPointsInTieBreakSpinner;
    private SwitchCompat         mTwoPointsDifferenceSwitch;
    private SwitchCompat         mSanctionsSwitch;
    private Spinner              mMatchTerminationSpinner;
    private SwitchCompat         mTeamTimeoutsSwitch;
    private Spinner              mTeamTimeoutsPerSetSpinner;
    private Spinner              mTeamTimeoutDurationSpinner;
    private SwitchCompat         mTechnicalTimeoutsSwitch;
    private Spinner              mTechnicalTimeoutDurationSpinner;
    private SwitchCompat         mGameIntervalsSwitch;
    private Spinner              mGameIntervalDurationSpinner;
    private Spinner              mSubstitutionsLimitationSpinner;
    private Spinner              mTeamSubstitutionsPerSetSpinner;
    private SwitchCompat         mCourtSwitchesSwitch;
    private Spinner              mCourtSwitchFrequencySpinner;
    private Spinner              mCourtSwitchFrequencyTieBreakSpinner;
    private Spinner              mConsecutiveServesSpinner;

    private IntegerRuleAdapter mSetsPerGameAdapter;
    private IntegerRuleAdapter mPointsPerSetAdapter;
    private IntegerRuleAdapter mPointsInTieBreakAdapter;
    private IntegerRuleAdapter mMatchTerminationAdapter;
    private IntegerRuleAdapter mTeamTimeoutsPerSetAdapter;
    private IntegerRuleAdapter mTeamTimeoutDurationAdapter;
    private IntegerRuleAdapter mTechnicalTimeoutDurationAdapter;
    private IntegerRuleAdapter mGameIntervalDurationAdapter;
    private IntegerRuleAdapter mSubstitutionsLimitationAdapter;
    private IntegerRuleAdapter mTeamSubstitutionsPerSetAdapter;
    private IntegerRuleAdapter mCourtSwitchFrequencyAdapter;
    private IntegerRuleAdapter mCourtSwitchFrequencyTieBreakAdapter;
    private IntegerRuleAdapter mConsecutiveServesAdapter;

    private TextView mSubstitutionsLimitationDescription;

    public RulesSetupFragment() {}

    public static RulesSetupFragment newInstance() {
        return newInstance(true);
    }

    public static RulesSetupFragment newInstance(boolean isGameContext) {
        RulesSetupFragment fragment = new RulesSetupFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_game", isGameContext);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.RULES, "Create rules setup fragment");
        View view;

        final boolean isGameContext = requireArguments().getBoolean("is_game");

        view = switch (mRules.getKind()) {
            case BEACH -> inflater.inflate(R.layout.fragment_beach_rules_setup, container, false);
            case SNOW -> inflater.inflate(R.layout.fragment_snow_rules_setup, container, false);
            default -> inflater.inflate(R.layout.fragment_indoor_rules_setup, container, false);
        };

        mRulesNameInput = view.findViewById(R.id.rules_name_input_text);

        if (!mRules.getName().isEmpty()) {
            requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        mRulesNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAdded()) {
                    Log.i(Tags.RULES, "Update rules name");
                    mRules.setName(s.toString().trim());
                    ((TextInputLayout) view.findViewById(R.id.rules_name_input_layout)).setError(
                            mRules.getName().length() < Rules.RULES_NAME_MIN_LENGTH ? String.format(Locale.getDefault(), getString(
                                    R.string.must_provide_at_least_n_characters), Rules.RULES_NAME_MIN_LENGTH) : null);
                    computeConfirmItemVisibility();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // General

        mSetsPerGameSpinner = view.findViewById(R.id.rules_sets_per_game);
        mSetsPerGameAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.sets_per_game_entries),
                                                     getResources().getStringArray(R.array.sets_per_game_values));
        mSetsPerGameSpinner.setAdapter(mSetsPerGameAdapter);

        mPointsPerSetSpinner = view.findViewById(R.id.rules_points_per_set);
        mPointsPerSetAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.points_per_set_entries),
                                                      getResources().getStringArray(R.array.points_per_set_values));
        mPointsPerSetSpinner.setAdapter(mPointsPerSetAdapter);

        mTieBreakSwitch = view.findViewById(R.id.rules_tie_break);

        mPointsInTieBreakSpinner = view.findViewById(R.id.rules_points_in_tie_break);
        mPointsInTieBreakAdapter = new IntegerRuleAdapter(getContext(), inflater,
                                                          getResources().getStringArray(R.array.points_per_set_entries),
                                                          getResources().getStringArray(R.array.points_per_set_values));
        mPointsInTieBreakSpinner.setAdapter(mPointsInTieBreakAdapter);

        mTwoPointsDifferenceSwitch = view.findViewById(R.id.rules_two_points_difference);

        mSanctionsSwitch = view.findViewById(R.id.rules_sanctions);

        mMatchTerminationSpinner = view.findViewById(R.id.rules_match_termination);
        mMatchTerminationAdapter = new IntegerRuleAdapter(getContext(), inflater,
                                                          getResources().getStringArray(R.array.match_termination_entries),
                                                          getResources().getStringArray(R.array.match_termination_values));
        mMatchTerminationSpinner.setAdapter(mMatchTerminationAdapter);

        mSetsPerGameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                int setsPerGame = mSetsPerGameAdapter.getItem(index);
                mRules.setSetsPerGame(setsPerGame);
                if (setsPerGame % 2 == 0) {
                    mMatchTerminationSpinner.setSelection(mMatchTerminationAdapter.getPosition(2)); // ALL_SETS_TERMINATION
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mPointsPerSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setPointsPerSet(mPointsPerSetAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mTieBreakSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            mRules.setTieBreakInLastSet(isChecked);
            mPointsInTieBreakSpinner.setEnabled(isChecked);
        });

        mPointsInTieBreakSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setPointsInTieBreak(mPointsInTieBreakAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mTwoPointsDifferenceSwitch.setOnCheckedChangeListener((button, isChecked) -> mRules.setTwoPointsDifference(isChecked));

        mSanctionsSwitch.setOnCheckedChangeListener((button, isChecked) -> mRules.setSanctions(isChecked));

        mMatchTerminationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                if (mRules.getSetsPerGame() % 2 == 0) {
                    mMatchTerminationSpinner.setSelection(mMatchTerminationAdapter.getPosition(2)); // ALL_SETS_TERMINATION
                } else {
                    mRules.setMatchTermination(mMatchTerminationAdapter.getItem(index));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // Timeouts

        mTeamTimeoutsSwitch = view.findViewById(R.id.rules_team_timeouts);

        mTeamTimeoutsPerSetSpinner = view.findViewById(R.id.rules_team_timeouts_per_set);
        mTeamTimeoutsPerSetAdapter = new IntegerRuleAdapter(getContext(), inflater,
                                                            getResources().getStringArray(R.array.team_timeouts_per_set_entries),
                                                            getResources().getStringArray(R.array.team_timeouts_per_set_values));
        mTeamTimeoutsPerSetSpinner.setAdapter(mTeamTimeoutsPerSetAdapter);

        mTeamTimeoutDurationSpinner = view.findViewById(R.id.rules_team_timeout_duration);
        mTeamTimeoutDurationAdapter = new IntegerRuleAdapter(getContext(), inflater,
                                                             getResources().getStringArray(R.array.timeout_duration_entries),
                                                             getResources().getStringArray(R.array.timeout_duration_values));
        mTeamTimeoutDurationSpinner.setAdapter(mTeamTimeoutDurationAdapter);

        mTeamTimeoutsSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            mRules.setTeamTimeouts(isChecked);
            mTeamTimeoutsPerSetSpinner.setEnabled(isChecked);
            mTeamTimeoutDurationSpinner.setEnabled(isChecked);
        });

        mTeamTimeoutsPerSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setTeamTimeoutsPerSet(mTeamTimeoutsPerSetAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mTeamTimeoutDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setTeamTimeoutDuration(mTeamTimeoutDurationAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        if (!GameType.SNOW.equals(mRules.getKind())) {
            mTechnicalTimeoutsSwitch = view.findViewById(R.id.rules_technical_timeouts);

            mTechnicalTimeoutDurationSpinner = view.findViewById(R.id.rules_technical_timeout_duration);
            mTechnicalTimeoutDurationAdapter = new IntegerRuleAdapter(getContext(), inflater,
                                                                      getResources().getStringArray(R.array.timeout_duration_entries),
                                                                      getResources().getStringArray(R.array.timeout_duration_values));
            mTechnicalTimeoutDurationSpinner.setAdapter(mTechnicalTimeoutDurationAdapter);

            mTechnicalTimeoutsSwitch.setOnCheckedChangeListener((button, isChecked) -> {
                mRules.setTechnicalTimeouts(isChecked);
                mTechnicalTimeoutDurationSpinner.setEnabled(isChecked);
            });

            mTechnicalTimeoutDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    mRules.setTechnicalTimeoutDuration(mTechnicalTimeoutDurationAdapter.getItem(index));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }

        mGameIntervalsSwitch = view.findViewById(R.id.rules_game_intervals);

        mGameIntervalDurationSpinner = view.findViewById(R.id.rules_game_intervals_duration);
        mGameIntervalDurationAdapter = new IntegerRuleAdapter(getContext(), inflater,
                                                              getResources().getStringArray(R.array.game_interval_duration_entries),
                                                              getResources().getStringArray(R.array.game_interval_duration_values));
        mGameIntervalDurationSpinner.setAdapter(mGameIntervalDurationAdapter);

        mGameIntervalsSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            mRules.setGameIntervals(isChecked);
            mGameIntervalDurationSpinner.setEnabled(isChecked);
        });

        mGameIntervalDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setGameIntervalDuration(mGameIntervalDurationAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // Substitutions

        if (!GameType.BEACH.equals(mRules.getKind())) {
            mSubstitutionsLimitationSpinner = view.findViewById(R.id.rules_substitutions_limitation);
            mSubstitutionsLimitationAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(
                    R.array.substitutions_limitation_entries), getResources().getStringArray(R.array.substitutions_limitation_values));
            mSubstitutionsLimitationSpinner.setAdapter(mSubstitutionsLimitationAdapter);

            mSubstitutionsLimitationDescription = view.findViewById(R.id.rules_substitutions_limitation_description);

            mTeamSubstitutionsPerSetSpinner = view.findViewById(R.id.rules_team_substitutions_per_set);
            mTeamSubstitutionsPerSetAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(
                    R.array.team_substitutions_per_set_entries), getResources().getStringArray(R.array.team_substitutions_per_set_values));
            mTeamSubstitutionsPerSetSpinner.setAdapter(mTeamSubstitutionsPerSetAdapter);

            mSubstitutionsLimitationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    mRules.setSubstitutionsLimitation(mSubstitutionsLimitationAdapter.getItem(index));
                    mTeamSubstitutionsPerSetSpinner.setSelection(
                            mTeamSubstitutionsPerSetAdapter.getPosition(mRules.getTeamSubstitutionsPerSet()));
                    mSubstitutionsLimitationDescription.setText(
                            getResources().getStringArray(R.array.substitutions_limitation_description_entries)[index]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            mTeamSubstitutionsPerSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    mRules.setTeamSubstitutionsPerSet(mTeamSubstitutionsPerSetAdapter.getItem(index));
                    mTeamSubstitutionsPerSetSpinner.setSelection(
                            mTeamSubstitutionsPerSetAdapter.getPosition(mRules.getTeamSubstitutionsPerSet()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }

        // Switches

        if (GameType.BEACH.equals(mRules.getKind()) || GameType.SNOW.equals(mRules.getKind())) {
            mCourtSwitchesSwitch = view.findViewById(R.id.rules_court_switches);

            mCourtSwitchFrequencySpinner = view.findViewById(R.id.rules_court_switch_frequency);
            mCourtSwitchFrequencyAdapter = new IntegerRuleAdapter(getContext(), inflater,
                                                                  getResources().getStringArray(R.array.court_switch_frequency_entries),
                                                                  getResources().getStringArray(R.array.court_switch_frequency_values));
            mCourtSwitchFrequencySpinner.setAdapter(mCourtSwitchFrequencyAdapter);

            mCourtSwitchFrequencyTieBreakSpinner = view.findViewById(R.id.rules_court_switch_frequency_tie_break);
            mCourtSwitchFrequencyTieBreakAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(
                    R.array.court_switch_frequency_entries), getResources().getStringArray(R.array.court_switch_frequency_values));
            mCourtSwitchFrequencyTieBreakSpinner.setAdapter(mCourtSwitchFrequencyTieBreakAdapter);

            mCourtSwitchesSwitch.setOnCheckedChangeListener((button, isChecked) -> {
                mRules.setBeachCourtSwitches(isChecked);
                mCourtSwitchFrequencySpinner.setEnabled(isChecked);
                mCourtSwitchFrequencyTieBreakSpinner.setEnabled(isChecked);
            });

            mCourtSwitchFrequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    mRules.setBeachCourtSwitchFreq(mCourtSwitchFrequencyAdapter.getItem(index));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            mCourtSwitchFrequencyTieBreakSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    mRules.setBeachCourtSwitchFreqTieBreak(mCourtSwitchFrequencyTieBreakAdapter.getItem(index));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }

        if (GameType.INDOOR.equals(mRules.getKind()) || GameType.INDOOR_4X4.equals(mRules.getKind())) {
            mConsecutiveServesSpinner = view.findViewById(R.id.rules_consecutive_serves_per_player);
            mConsecutiveServesAdapter = new IntegerRuleAdapter(getContext(), inflater,
                                                               getResources().getStringArray(R.array.consecutive_serves_per_player_entries),
                                                               getResources().getStringArray(R.array.consecutive_serves_per_player_values));
            mConsecutiveServesSpinner.setAdapter(mConsecutiveServesAdapter);

            mConsecutiveServesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    mRules.setCustomConsecutiveServesPerPlayer(mConsecutiveServesAdapter.getItem(index));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }

        if (isGameContext) {
            StoredRulesService storedRulesService = new StoredRulesManager(getContext());

            mRulesNameInput.setThreshold(1);
            mRulesNameInput.setAdapter(
                    new AutocompleteRulesListAdapter(getContext(), getLayoutInflater(), storedRulesService.listRules(mRules.getKind())));
            mRulesNameInput.setOnItemClickListener((parent, input, index, id) -> {
                RulesSummaryDto rulesDescription = (RulesSummaryDto) mRulesNameInput.getAdapter().getItem(index);
                mRulesNameInput.setText(rulesDescription.getName());
                mRules.setAll(storedRulesService.getRules(rulesDescription.getId()));
                initValues();
                computeConfirmItemVisibility();
            });
        }

        initValues();

        computeConfirmItemVisibility();

        return view;
    }

    private void initValues() {
        if (!mRules.getName().isEmpty()) {
            mRulesNameInput.setText(mRules.getName());
        }
        mSetsPerGameSpinner.setSelection(mSetsPerGameAdapter.getPosition(mRules.getSetsPerGame()));
        mPointsPerSetSpinner.setSelection(mPointsPerSetAdapter.getPosition(mRules.getPointsPerSet()));
        mTieBreakSwitch.setChecked(mRules.isTieBreakInLastSet());
        mPointsInTieBreakSpinner.setSelection(mPointsInTieBreakAdapter.getPosition(mRules.getPointsInTieBreak()));
        mPointsInTieBreakSpinner.setEnabled(mRules.isTieBreakInLastSet());
        mTwoPointsDifferenceSwitch.setChecked(mRules.isTwoPointsDifference());
        mSanctionsSwitch.setChecked(mRules.isSanctions());
        mMatchTerminationSpinner.setSelection(mMatchTerminationAdapter.getPosition(mRules.getMatchTermination()));
        mTeamTimeoutsSwitch.setChecked(mRules.isTeamTimeouts());
        mTeamTimeoutsPerSetSpinner.setSelection(mTeamTimeoutsPerSetAdapter.getPosition(mRules.getTeamTimeoutsPerSet()));
        mTeamTimeoutsPerSetSpinner.setEnabled(mRules.isTeamTimeouts());
        mTeamTimeoutDurationSpinner.setSelection(mTeamTimeoutDurationAdapter.getPosition(mRules.getTeamTimeoutDuration()));
        mTeamTimeoutDurationSpinner.setEnabled(mRules.isTeamTimeouts());
        if (!GameType.SNOW.equals(mRules.getKind())) {
            mTechnicalTimeoutsSwitch.setChecked(mRules.isTechnicalTimeouts());
            mTechnicalTimeoutDurationSpinner.setSelection(
                    mTechnicalTimeoutDurationAdapter.getPosition(mRules.getTechnicalTimeoutDuration()));
            mTechnicalTimeoutDurationSpinner.setEnabled(mRules.isTechnicalTimeouts());
        }
        mGameIntervalsSwitch.setChecked(mRules.isGameIntervals());
        mGameIntervalDurationSpinner.setSelection(mGameIntervalDurationAdapter.getPosition(mRules.getGameIntervalDuration()));
        mGameIntervalDurationSpinner.setEnabled(mRules.isGameIntervals());
        if (!GameType.BEACH.equals(mRules.getKind())) {
            mSubstitutionsLimitationSpinner.setSelection(mSubstitutionsLimitationAdapter.getPosition(mRules.getSubstitutionsLimitation()));
            mTeamSubstitutionsPerSetSpinner.setSelection(mTeamSubstitutionsPerSetAdapter.getPosition(mRules.getTeamSubstitutionsPerSet()));
        }
        if (GameType.BEACH.equals(mRules.getKind()) || GameType.SNOW.equals(mRules.getKind())) {
            mCourtSwitchesSwitch.setChecked(mRules.isBeachCourtSwitches());
            mCourtSwitchFrequencySpinner.setSelection(mCourtSwitchFrequencyAdapter.getPosition(mRules.getBeachCourtSwitchFreq()));
            mCourtSwitchFrequencySpinner.setEnabled(mRules.isBeachCourtSwitches());
            mCourtSwitchFrequencyTieBreakSpinner.setSelection(
                    mCourtSwitchFrequencyTieBreakAdapter.getPosition(mRules.getBeachCourtSwitchFreqTieBreak()));
            mCourtSwitchFrequencyTieBreakSpinner.setEnabled(mRules.isBeachCourtSwitches());
        }
        if (GameType.INDOOR.equals(mRules.getKind()) || GameType.INDOOR_4X4.equals(mRules.getKind())) {
            mConsecutiveServesSpinner.setSelection(mConsecutiveServesAdapter.getPosition(mRules.getCustomConsecutiveServesPerPlayer()));
        }
    }

    private void computeConfirmItemVisibility() {
        if (requireActivity() instanceof GameSetupActivity) {
            ((GameSetupActivity) requireActivity()).computeStartGameButton();
        } else if (requireActivity() instanceof StoredRulesActivity) {
            ((StoredRulesActivity) requireActivity()).computeSaveLayoutVisibility();
        }
    }

    @Override
    public void setRules(Rules rules) {
        mRules = rules;
    }
}
