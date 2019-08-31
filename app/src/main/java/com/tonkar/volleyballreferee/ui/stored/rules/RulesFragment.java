package com.tonkar.volleyballreferee.ui.stored.rules;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.ui.interfaces.RulesHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class RulesFragment extends Fragment implements RulesHandler {

    private Rules mRules;
    
    public RulesFragment() {}

    public static RulesFragment newInstance() {
        return new RulesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.STORED_RULES, "Create rules fragment");
        View view = inflater.inflate(R.layout.fragment_rules, container, false);

        TextView setsPerGame = view.findViewById(R.id.rules_sets_per_game);
        TextView pointsPerSet = view.findViewById(R.id.rules_points_per_set);
        SwitchCompat tieBreakSwitch = view.findViewById(R.id.rules_tie_break);
        TextView pointsInTieBreak = view.findViewById(R.id.rules_points_in_tie_break);
        SwitchCompat twoPointsDifferenceSwitch = view.findViewById(R.id.rules_two_points_difference);
        SwitchCompat sanctionsSwitch = view.findViewById(R.id.rules_sanctions);

        SwitchCompat teamTimeoutsSwitch = view.findViewById(R.id.rules_team_timeouts);
        TextView teamTimeoutsPerSet = view.findViewById(R.id.rules_team_timeouts_per_set);
        TextView teamTimeoutDuration = view.findViewById(R.id.rules_team_timeout_duration);
        SwitchCompat technicalTimeoutsSwitch = view.findViewById(R.id.rules_technical_timeouts);
        TextView technicalTimeoutDuration = view.findViewById(R.id.rules_technical_timeout_duration);
        SwitchCompat gameIntervalsSwitch = view.findViewById(R.id.rules_game_intervals);
        TextView gameIntervalDuration = view.findViewById(R.id.rules_game_intervals_duration);

        TextView substitutionsLimitation = view.findViewById(R.id.rules_substitutions_limitation);
        TextView substitutionsLimitationDescription = view.findViewById(R.id.rules_substitutions_limitation_description);
        UiUtils.setDrawableStart(substitutionsLimitationDescription, R.drawable.ic_info);
        for (Drawable drawable : substitutionsLimitationDescription.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), PorterDuff.Mode.SRC_IN));
            }
        }
        TextView teamSubstitutionsPerSet = view.findViewById(R.id.rules_team_substitutions_per_set);

        SwitchCompat courtSwitchesSwitch = view.findViewById(R.id.rules_court_switches);
        TextView courtSwitchFrequency = view.findViewById(R.id.rules_court_switch_frequency);
        TextView courtSwitchFrequencyTieBreak = view.findViewById(R.id.rules_court_switch_frequency_tie_break);
        TextView consecutiveServes = view.findViewById(R.id.rules_consecutive_serves_per_player);

        setsPerGame.setText(findRuleEntry(mRules.getSetsPerGame(), R.array.sets_per_game_entries, R.array.sets_per_game_values));
        pointsPerSet.setText(findRuleEntry(mRules.getPointsPerSet(), R.array.points_per_set_entries, R.array.points_per_set_values));
        tieBreakSwitch.setChecked(mRules.isTieBreakInLastSet());
        pointsInTieBreak.setText(findRuleEntry(mRules.getPointsInTieBreak(), R.array.points_per_set_entries, R.array.points_per_set_values));
        twoPointsDifferenceSwitch.setChecked(mRules.isTwoPointsDifference());
        sanctionsSwitch.setChecked(mRules.isSanctions());

        teamTimeoutsSwitch.setChecked(mRules.isTeamTimeouts());
        teamTimeoutsPerSet.setText(findRuleEntry(mRules.getTeamTimeoutsPerSet(), R.array.team_timeouts_per_set_entries, R.array.team_timeouts_per_set_values));
        teamTimeoutDuration.setText(findRuleEntry(mRules.getTeamTimeoutDuration(), R.array.timeout_duration_entries, R.array.timeout_duration_values));
        technicalTimeoutsSwitch.setChecked(mRules.isTechnicalTimeouts());
        technicalTimeoutDuration.setText(findRuleEntry(mRules.getTechnicalTimeoutDuration(), R.array.timeout_duration_entries, R.array.timeout_duration_values));
        gameIntervalsSwitch.setChecked(mRules.isGameIntervals());
        gameIntervalDuration.setText(findRuleEntry(mRules.getGameIntervalDuration(), R.array.game_interval_duration_entries, R.array.game_interval_duration_values));

        substitutionsLimitation.setText(findRuleEntry(mRules.getSubstitutionsLimitation(), R.array.substitutions_limitation_entries, R.array.substitutions_limitation_values));
        substitutionsLimitationDescription.setText(findRuleEntry(mRules.getSubstitutionsLimitation(), R.array.substitutions_limitation_description_entries, R.array.substitutions_limitation_values));
        teamSubstitutionsPerSet.setText(findRuleEntry(mRules.getTeamSubstitutionsPerSet(), R.array.team_substitutions_per_set_entries, R.array.team_substitutions_per_set_values));

        courtSwitchesSwitch.setChecked(mRules.isBeachCourtSwitches());
        courtSwitchFrequency.setText(findRuleEntry(mRules.getBeachCourtSwitchFreq(), R.array.court_switch_frequency_entries, R.array.court_switch_frequency_values));
        courtSwitchFrequencyTieBreak.setText(findRuleEntry(mRules.getBeachCourtSwitchFreqTieBreak(), R.array.court_switch_frequency_entries, R.array.court_switch_frequency_values));
        consecutiveServes.setText(findRuleEntry(mRules.getCustomConsecutiveServesPerPlayer(), R.array.consecutive_serves_per_player_entries, R.array.consecutive_serves_per_player_values));

        View indoorSection = view.findViewById(R.id.indoor_rules_section);
        View beachSection = view.findViewById(R.id.beach_rules_section);

        if (GameType.INDOOR.equals(mRules.getKind()) || GameType.INDOOR_4X4.equals(mRules.getKind())) {
            indoorSection.setVisibility(View.VISIBLE);
            beachSection.setVisibility(View.GONE);
        } else if (GameType.BEACH.equals(mRules.getKind())) {
            indoorSection.setVisibility(View.GONE);
            beachSection.setVisibility(View.VISIBLE);
        } else {
            indoorSection.setVisibility(View.GONE);
            beachSection.setVisibility(View.GONE);
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
