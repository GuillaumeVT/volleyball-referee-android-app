package com.tonkar.volleyballreferee.ui.rules;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.util.ClearableTextInputAutoCompleteTextView;
import com.tonkar.volleyballreferee.ui.data.SavedRulesActivity;
import com.tonkar.volleyballreferee.ui.data.SavedRulesListAdapter;
import com.tonkar.volleyballreferee.ui.setup.GameSetupActivity;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class RulesSetupFragment extends Fragment {

    private Rules mRules;

    private ScrollView                             mScrollView;
    private ClearableTextInputAutoCompleteTextView mRulesNameInput;
    private Spinner                                mSetsPerGameSpinner;
    private Spinner                                mPointsPerSetSpinner;
    private SwitchCompat                           mTieBreakSwitch;
    private Spinner                                mPointsInTieBreakSpinner;
    private SwitchCompat                           mTwoPointsDifferenceSwitch;
    private SwitchCompat                           mSanctionsSwitch;
    private SwitchCompat                           mTeamTimeoutsSwitch;
    private Spinner                                mTeamTimeoutsPerSetSpinner;
    private Spinner                                mTeamTimeoutDurationSpinner;
    private SwitchCompat                           mTechnicalTimeoutsSwitch;
    private Spinner                                mTechnicalTimeoutDurationSpinner;
    private SwitchCompat                           mGameIntervalsSwitch;
    private Spinner                                mGameIntervalDurationSpinner;
    private Spinner                                mTeamSubstitutionsPerSetSpinner;
    private SwitchCompat                           mCourtSwitchesSwitch;
    private Spinner                                mCourtSwitchFrequencySpinner;
    private Spinner                                mCourtSwitchFrequencyTieBreakSpinner;
    private Spinner                                mConsecutiveServesSpinner;

    private IntegerRuleAdapter mSetsPerGameAdapter;
    private IntegerRuleAdapter mPointsPerSetAdapter;
    private IntegerRuleAdapter mPointsInTieBreakAdapter;
    private IntegerRuleAdapter mTeamTimeoutsPerSetAdapter;
    private IntegerRuleAdapter mTeamTimeoutDurationAdapter;
    private IntegerRuleAdapter mTechnicalTimeoutDurationAdapter;
    private IntegerRuleAdapter mGameIntervalDurationAdapter;
    private IntegerRuleAdapter mTeamSubstitutionsPerSetAdapter;
    private IntegerRuleAdapter mCourtSwitchFrequencyAdapter;
    private IntegerRuleAdapter mCourtSwitchFrequencyTieBreakAdapter;
    private IntegerRuleAdapter mConsecutiveServesAdapter;

    public RulesSetupFragment() {
    }

    public static RulesSetupFragment newInstance() {
        return newInstance(true, true);
    }

    public static RulesSetupFragment newInstance(boolean isGameContext, boolean editable) {
        RulesSetupFragment fragment = new RulesSetupFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_game", isGameContext);
        args.putBoolean("editable", editable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.RULES, "Create rules setup fragment");
        View view = inflater.inflate(R.layout.fragment_rules_setup, container, false);

        final boolean isGameContext = getArguments().getBoolean("is_game");
        final boolean editable = getArguments().getBoolean("editable");

        if (isGameContext) {
            if (ServicesProvider.getInstance().isGameServiceUnavailable()) {
                ServicesProvider.getInstance().restoreGameServiceForSetup(getActivity().getApplicationContext());
            }
            mRules = ServicesProvider.getInstance().getGeneralService().getRules();
        } else {
            mRules = ServicesProvider.getInstance().getSavedRulesService(getActivity().getApplicationContext()).getCurrentRules();
        }

        mScrollView = view.findViewById(R.id.rules_setup_scroll);

        mRulesNameInput = view.findViewById(R.id.rules_name_input_text);
        mRulesNameInput.setEnabled(editable);

        mSetsPerGameSpinner = view.findViewById(R.id.rules_sets_per_game);
        mSetsPerGameAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.sets_per_game_entries), getResources().getStringArray(R.array.sets_per_game_values));
        mSetsPerGameSpinner.setAdapter(mSetsPerGameAdapter);

        mPointsPerSetSpinner = view.findViewById(R.id.rules_points_per_set);
        mPointsPerSetAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.points_per_set_entries), getResources().getStringArray(R.array.points_per_set_values));
        mPointsPerSetSpinner.setAdapter(mPointsPerSetAdapter);

        mTieBreakSwitch = view.findViewById(R.id.rules_tie_break);

        mPointsInTieBreakSpinner = view.findViewById(R.id.rules_points_in_tie_break);
        mPointsInTieBreakAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.points_per_set_entries), getResources().getStringArray(R.array.points_per_set_values));
        mPointsInTieBreakSpinner.setAdapter(mPointsInTieBreakAdapter);

        mTwoPointsDifferenceSwitch = view.findViewById(R.id.rules_two_points_difference);

        mSanctionsSwitch = view.findViewById(R.id.rules_sanctions);

        mTeamTimeoutsSwitch = view.findViewById(R.id.rules_team_timeouts);

        mTeamTimeoutsPerSetSpinner = view.findViewById(R.id.rules_team_timeouts_per_set);
        mTeamTimeoutsPerSetAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.team_timeouts_per_set_entries), getResources().getStringArray(R.array.team_timeouts_per_set_values));
        mTeamTimeoutsPerSetSpinner.setAdapter(mTeamTimeoutsPerSetAdapter);

        mTeamTimeoutDurationSpinner = view.findViewById(R.id.rules_team_timeout_duration);
        mTeamTimeoutDurationAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.timeout_duration_entries), getResources().getStringArray(R.array.timeout_duration_values));
        mTeamTimeoutDurationSpinner.setAdapter(mTeamTimeoutDurationAdapter);

        mTechnicalTimeoutsSwitch = view.findViewById(R.id.rules_technical_timeouts);

        mTechnicalTimeoutDurationSpinner = view.findViewById(R.id.rules_technical_timeout_duration);
        mTechnicalTimeoutDurationAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.timeout_duration_entries), getResources().getStringArray(R.array.timeout_duration_values));
        mTechnicalTimeoutDurationSpinner.setAdapter(mTechnicalTimeoutDurationAdapter);

        mGameIntervalsSwitch = view.findViewById(R.id.rules_game_intervals);

        mGameIntervalDurationSpinner = view.findViewById(R.id.rules_game_intervals_duration);
        mGameIntervalDurationAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.game_interval_duration_entries), getResources().getStringArray(R.array.game_interval_duration_values));
        mGameIntervalDurationSpinner.setAdapter(mGameIntervalDurationAdapter);

        mTeamSubstitutionsPerSetSpinner = view.findViewById(R.id.rules_team_substitutions_per_set);
        mTeamSubstitutionsPerSetAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.team_substitutions_per_set_entries), getResources().getStringArray(R.array.team_substitutions_per_set_values));
        mTeamSubstitutionsPerSetSpinner.setAdapter(mTeamSubstitutionsPerSetAdapter);

        mCourtSwitchesSwitch = view.findViewById(R.id.rules_court_switches);

        mCourtSwitchFrequencySpinner = view.findViewById(R.id.rules_court_switch_frequency);
        mCourtSwitchFrequencyAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.court_switch_frequency_entries), getResources().getStringArray(R.array.court_switch_frequency_values));
        mCourtSwitchFrequencySpinner.setAdapter(mCourtSwitchFrequencyAdapter);

        mCourtSwitchFrequencyTieBreakSpinner = view.findViewById(R.id.rules_court_switch_frequency_tie_break);
        mCourtSwitchFrequencyTieBreakAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.court_switch_frequency_entries), getResources().getStringArray(R.array.court_switch_frequency_values));
        mCourtSwitchFrequencyTieBreakSpinner.setAdapter(mCourtSwitchFrequencyTieBreakAdapter);

        mConsecutiveServesSpinner = view.findViewById(R.id.rules_consecutive_serves_per_player);
        mConsecutiveServesAdapter = new IntegerRuleAdapter(getContext(), inflater, getResources().getStringArray(R.array.consecutive_serves_per_player_entries), getResources().getStringArray(R.array.consecutive_serves_per_player_values));
        mConsecutiveServesSpinner.setAdapter(mConsecutiveServesAdapter);

        initValues();

        if (isGameContext) {
            mRulesNameInput.setThreshold(2);
            mRulesNameInput.setAdapter(new SavedRulesListAdapter(getContext(), getLayoutInflater(), ServicesProvider.getInstance().getSavedRulesService(getActivity().getApplicationContext()).getSavedRules()));
            mRulesNameInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                    Rules rules = (Rules) mRulesNameInput.getAdapter().getItem(index);
                    mRulesNameInput.setText(rules.getName());
                    mRules.setAll(rules);
                    initValues();
                    mScrollView.post(new Runnable() {
                        public void run() {
                            mScrollView.fullScroll(ScrollView.FOCUS_UP);
                        }
                    });
                    computeConfirmItemVisibility();
                }
            });
        }

        if (!mRules.getName().isEmpty()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        mRulesNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(Tags.RULES, "Update rules name");
                mRules.setName(s.toString());
                computeConfirmItemVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mSetsPerGameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setSetsPerGame(mSetsPerGameAdapter.getItem(index));
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

        mTieBreakSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRules.setTieBreakInLastSet(isChecked);
                mPointsInTieBreakSpinner.setEnabled(isChecked);
            }
        });

        mPointsInTieBreakSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setPointsInTieBreak(mPointsInTieBreakAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mTwoPointsDifferenceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRules.setTwoPointsDifference(isChecked);
            }
        });

        mSanctionsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRules.setSanctionsEnabled(isChecked);
            }
        });

        mTeamTimeoutsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRules.setTeamTimeoutsEnabled(isChecked);
                mTeamTimeoutsPerSetSpinner.setEnabled(isChecked);
                mTeamTimeoutDurationSpinner.setEnabled(isChecked);
            }
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

        mTechnicalTimeoutsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRules.setTechnicalTimeoutsEnabled(isChecked);
                mTechnicalTimeoutDurationSpinner.setEnabled(isChecked);
            }
        });

        mTechnicalTimeoutDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setTechnicalTimeoutDuration(mTechnicalTimeoutDurationAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mGameIntervalsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRules.setGameIntervalsEnabled(isChecked);
                mGameIntervalDurationSpinner.setEnabled(isChecked);
            }
        });

        mGameIntervalDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setGameIntervalDuration(mGameIntervalDurationAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mTeamSubstitutionsPerSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setTeamSubstitutionsPerSet(mTeamSubstitutionsPerSetAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mCourtSwitchesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRules.setBeachCourtSwitchesEnabled(isChecked);
                mCourtSwitchFrequencySpinner.setEnabled(isChecked);
                mCourtSwitchFrequencyTieBreakSpinner.setEnabled(isChecked);
            }
        });

        mCourtSwitchFrequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setBeachCourtSwitchFrequency(mCourtSwitchFrequencyAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mCourtSwitchFrequencyTieBreakSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setBeachCourtSwitchFrequencyTieBreak(mCourtSwitchFrequencyTieBreakAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mConsecutiveServesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mRules.setCustomConsecutiveServesPerPlayer(mConsecutiveServesAdapter.getItem(index));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        computeConfirmItemVisibility();

        return view;
    }

    private void initValues() {
        mRulesNameInput.setText(mRules.getName());
        mSetsPerGameSpinner.setSelection(mSetsPerGameAdapter.getPosition(mRules.getSetsPerGame()));
        mPointsPerSetSpinner.setSelection(mPointsPerSetAdapter.getPosition(mRules.getPointsPerSet()));
        mTieBreakSwitch.setChecked(mRules.isTieBreakInLastSet());
        mPointsInTieBreakSpinner.setSelection(mPointsInTieBreakAdapter.getPosition(mRules.getPointsInTieBreak()));
        mPointsInTieBreakSpinner.setEnabled(mRules.isTieBreakInLastSet());
        mTwoPointsDifferenceSwitch.setChecked(mRules.isTwoPointsDifference());
        mSanctionsSwitch.setChecked(mRules.areSanctionsEnabled());
        mTeamTimeoutsSwitch.setChecked(mRules.areTeamTimeoutsEnabled());
        mTeamTimeoutsPerSetSpinner.setSelection(mTeamTimeoutsPerSetAdapter.getPosition(mRules.getTeamTimeoutsPerSet()));
        mTeamTimeoutsPerSetSpinner.setEnabled(mRules.areTeamTimeoutsEnabled());
        mTeamTimeoutDurationSpinner.setSelection(mTeamTimeoutDurationAdapter.getPosition(mRules.getTeamTimeoutDuration()));
        mTeamTimeoutDurationSpinner.setEnabled(mRules.areTeamTimeoutsEnabled());
        mTechnicalTimeoutsSwitch.setChecked(mRules.areTechnicalTimeoutsEnabled());
        mTechnicalTimeoutDurationSpinner.setSelection(mTechnicalTimeoutDurationAdapter.getPosition(mRules.getTechnicalTimeoutDuration()));
        mTechnicalTimeoutDurationSpinner.setEnabled(mRules.areTechnicalTimeoutsEnabled());
        mGameIntervalsSwitch.setChecked(mRules.areGameIntervalsEnabled());
        mGameIntervalDurationSpinner.setSelection(mGameIntervalDurationAdapter.getPosition(mRules.getGameIntervalDuration()));
        mGameIntervalDurationSpinner.setEnabled(mRules.areGameIntervalsEnabled());
        mTeamSubstitutionsPerSetSpinner.setSelection(mTeamSubstitutionsPerSetAdapter.getPosition(mRules.getTeamSubstitutionsPerSet()));
        mCourtSwitchesSwitch.setChecked(mRules.areBeachCourtSwitchesEnabled());
        mCourtSwitchFrequencySpinner.setSelection(mCourtSwitchFrequencyAdapter.getPosition(mRules.getBeachCourtSwitchFrequency()));
        mCourtSwitchFrequencySpinner.setEnabled(mRules.areBeachCourtSwitchesEnabled());
        mCourtSwitchFrequencyTieBreakSpinner.setSelection(mCourtSwitchFrequencyTieBreakAdapter.getPosition(mRules.getBeachCourtSwitchFrequencyTieBreak()));
        mCourtSwitchFrequencyTieBreakSpinner.setEnabled(mRules.areBeachCourtSwitchesEnabled());
        mConsecutiveServesSpinner.setSelection(mConsecutiveServesAdapter.getPosition(mRules.getCustomConsecutiveServesPerPlayer()));
    }

    private void computeConfirmItemVisibility() {
        if (getActivity() instanceof GameSetupActivity) {
            ((GameSetupActivity) getActivity()).computeConfirmItemVisibility();
        } else if (getActivity() instanceof SavedRulesActivity) {
            ((SavedRulesActivity) getActivity()).computeSaveItemVisibility();
        }
    }
}
