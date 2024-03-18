package com.tonkar.volleyballreferee.ui.data.rules;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.ui.interfaces.RulesHandler;

public class RulesFragment extends Fragment implements RulesHandler {

    private Rules mRules;

    public RulesFragment() {}

    public static RulesFragment newInstance() {
        return new RulesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.STORED_RULES, "Create rules fragment");
        View view = switch (mRules.getKind()) {
            case BEACH -> inflater.inflate(R.layout.fragment_beach_rules, container, false);
            case SNOW -> inflater.inflate(R.layout.fragment_snow_rules, container, false);
            default -> inflater.inflate(R.layout.fragment_indoor_rules, container, false);
        };

        // General

        TextView setsPerGame = view.findViewById(R.id.rules_sets_per_game);
        setsPerGame.setText(findRuleEntry(mRules.getSetsPerGame(), R.array.sets_per_game_entries, R.array.sets_per_game_values));

        TextView pointsPerSet = view.findViewById(R.id.rules_points_per_set);
        pointsPerSet.setText(findRuleEntry(mRules.getPointsPerSet(), R.array.points_per_set_entries, R.array.points_per_set_values));

        SwitchCompat tieBreakSwitch = view.findViewById(R.id.rules_tie_break);
        tieBreakSwitch.setChecked(mRules.isTieBreakInLastSet());

        TextView pointsInTieBreak = view.findViewById(R.id.rules_points_in_tie_break);
        pointsInTieBreak.setText(
                findRuleEntry(mRules.getPointsInTieBreak(), R.array.points_per_set_entries, R.array.points_per_set_values));

        SwitchCompat twoPointsDifferenceSwitch = view.findViewById(R.id.rules_two_points_difference);
        twoPointsDifferenceSwitch.setChecked(mRules.isTwoPointsDifference());

        SwitchCompat sanctionsSwitch = view.findViewById(R.id.rules_sanctions);
        sanctionsSwitch.setChecked(mRules.isSanctions());

        TextView matchTermination = view.findViewById(R.id.rules_match_termination);
        matchTermination.setText(
                findRuleEntry(mRules.getMatchTermination(), R.array.match_termination_entries, R.array.match_termination_values));

        // Timeouts

        SwitchCompat teamTimeoutsSwitch = view.findViewById(R.id.rules_team_timeouts);
        teamTimeoutsSwitch.setChecked(mRules.isTeamTimeouts());

        TextView teamTimeoutsPerSet = view.findViewById(R.id.rules_team_timeouts_per_set);
        teamTimeoutsPerSet.setText(
                findRuleEntry(mRules.getTeamTimeoutsPerSet(), R.array.team_timeouts_per_set_entries, R.array.team_timeouts_per_set_values));

        TextView teamTimeoutDuration = view.findViewById(R.id.rules_team_timeout_duration);
        teamTimeoutDuration.setText(
                findRuleEntry(mRules.getTeamTimeoutDuration(), R.array.timeout_duration_entries, R.array.timeout_duration_values));

        if (!GameType.SNOW.equals(mRules.getKind())) {
            SwitchCompat technicalTimeoutsSwitch = view.findViewById(R.id.rules_technical_timeouts);
            technicalTimeoutsSwitch.setChecked(mRules.isTechnicalTimeouts());

            TextView technicalTimeoutDuration = view.findViewById(R.id.rules_technical_timeout_duration);
            technicalTimeoutDuration.setText(
                    findRuleEntry(mRules.getTechnicalTimeoutDuration(), R.array.timeout_duration_entries, R.array.timeout_duration_values));
        }

        SwitchCompat gameIntervalsSwitch = view.findViewById(R.id.rules_game_intervals);
        gameIntervalsSwitch.setChecked(mRules.isGameIntervals());

        TextView gameIntervalDuration = view.findViewById(R.id.rules_game_intervals_duration);
        gameIntervalDuration.setText(findRuleEntry(mRules.getGameIntervalDuration(), R.array.game_interval_duration_entries,
                                                   R.array.game_interval_duration_values));

        // Substitutions

        if (!GameType.BEACH.equals(mRules.getKind())) {
            TextView substitutionsLimitation = view.findViewById(R.id.rules_substitutions_limitation);
            substitutionsLimitation.setText(findRuleEntry(mRules.getSubstitutionsLimitation(), R.array.substitutions_limitation_entries,
                                                          R.array.substitutions_limitation_values));

            TextView substitutionsLimitationDescription = view.findViewById(R.id.rules_substitutions_limitation_description);
            substitutionsLimitationDescription.setText(
                    findRuleEntry(mRules.getSubstitutionsLimitation(), R.array.substitutions_limitation_description_entries,
                                  R.array.substitutions_limitation_values));

            TextView teamSubstitutionsPerSet = view.findViewById(R.id.rules_team_substitutions_per_set);
            teamSubstitutionsPerSet.setText(findRuleEntry(mRules.getTeamSubstitutionsPerSet(), R.array.team_substitutions_per_set_entries,
                                                          R.array.team_substitutions_per_set_values));
        }

        // Switches

        if (GameType.BEACH.equals(mRules.getKind()) || GameType.SNOW.equals(mRules.getKind())) {
            SwitchCompat courtSwitchesSwitch = view.findViewById(R.id.rules_court_switches);
            courtSwitchesSwitch.setChecked(mRules.isBeachCourtSwitches());

            TextView courtSwitchFrequency = view.findViewById(R.id.rules_court_switch_frequency);
            courtSwitchFrequency.setText(findRuleEntry(mRules.getBeachCourtSwitchFreq(), R.array.court_switch_frequency_entries,
                                                       R.array.court_switch_frequency_values));

            TextView courtSwitchFrequencyTieBreak = view.findViewById(R.id.rules_court_switch_frequency_tie_break);
            courtSwitchFrequencyTieBreak.setText(
                    findRuleEntry(mRules.getBeachCourtSwitchFreqTieBreak(), R.array.court_switch_frequency_entries,
                                  R.array.court_switch_frequency_values));
        }

        if (GameType.INDOOR.equals(mRules.getKind()) || GameType.INDOOR_4X4.equals(mRules.getKind())) {
            TextView consecutiveServes = view.findViewById(R.id.rules_consecutive_serves_per_player);
            consecutiveServes.setText(
                    findRuleEntry(mRules.getCustomConsecutiveServesPerPlayer(), R.array.consecutive_serves_per_player_entries,
                                  R.array.consecutive_serves_per_player_values));
        }

        return view;
    }

    private String findRuleEntry(int value, @ArrayRes int entriesRes, @ArrayRes int valuesRes) {
        String entry = "";
        String valueStr = Integer.toString(value);
        String[] entries = getResources().getStringArray(entriesRes);
        String[] values = getResources().getStringArray(valuesRes);

        for (int index = 0; index < values.length; index++) {
            if (values[index].equals(valueStr)) {
                entry = entries[index];
            }
        }

        return entry;
    }

    @Override
    public void setRules(Rules rules) {
        mRules = rules;
    }
}
