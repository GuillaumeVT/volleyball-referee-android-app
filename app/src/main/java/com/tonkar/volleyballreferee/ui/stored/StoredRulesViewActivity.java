package com.tonkar.volleyballreferee.ui.stored;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ArrayRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.chip.Chip;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.stored.StoredRulesManager;
import com.tonkar.volleyballreferee.engine.stored.StoredRulesService;
import com.tonkar.volleyballreferee.engine.stored.api.ApiRules;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class StoredRulesViewActivity extends AppCompatActivity {

    private StoredRulesService mStoredRulesService;
    private Rules              mRules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredRulesService = new StoredRulesManager(this);
        mRules = new Rules();
        mRules.setAll(mStoredRulesService.readRules(getIntent().getStringExtra("rules")));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_rules_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayout itemLayout = findViewById(R.id.stored_rules_item_layout);
        TextView nameText = findViewById(R.id.stored_rules_name);
        Chip kindItem = findViewById(R.id.rules_kind_item);

        itemLayout.setPadding(0, 0, 0, 0);
        nameText.setText(mRules.getName());

        switch (mRules.getKind()) {
            case INDOOR_4X4:
                kindItem.setChipIconResource(R.drawable.ic_4x4_small);
                kindItem.setChipBackgroundColorResource(R.color.colorIndoor4x4Light);
                kindItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case BEACH:
                kindItem.setChipIconResource(R.drawable.ic_beach);
                kindItem.setChipBackgroundColorResource(R.color.colorBeachLight);
                kindItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case INDOOR:
            default:
                kindItem.setChipIconResource(R.drawable.ic_6x6_small);
                kindItem.setChipBackgroundColorResource(R.color.colorIndoorLight);
                kindItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
        }

        TextView setsPerGame = findViewById(R.id.rules_sets_per_game);
        TextView pointsPerSet = findViewById(R.id.rules_points_per_set);
        SwitchCompat tieBreakSwitch = findViewById(R.id.rules_tie_break);
        TextView pointsInTieBreak = findViewById(R.id.rules_points_in_tie_break);
        SwitchCompat twoPointsDifferenceSwitch = findViewById(R.id.rules_two_points_difference);
        SwitchCompat sanctionsSwitch = findViewById(R.id.rules_sanctions);

        SwitchCompat teamTimeoutsSwitch = findViewById(R.id.rules_team_timeouts);
        TextView teamTimeoutsPerSet = findViewById(R.id.rules_team_timeouts_per_set);
        TextView teamTimeoutDuration = findViewById(R.id.rules_team_timeout_duration);
        SwitchCompat technicalTimeoutsSwitch = findViewById(R.id.rules_technical_timeouts);
        TextView technicalTimeoutDuration = findViewById(R.id.rules_technical_timeout_duration);
        SwitchCompat gameIntervalsSwitch = findViewById(R.id.rules_game_intervals);
        TextView gameIntervalDuration = findViewById(R.id.rules_game_intervals_duration);

        TextView substitutionsLimitation = findViewById(R.id.rules_substitutions_limitation);
        TextView substitutionsLimitationDescription = findViewById(R.id.rules_substitutions_limitation_description);
        UiUtils.setDrawableStart(substitutionsLimitationDescription, R.drawable.ic_info);
        for (Drawable drawable : substitutionsLimitationDescription.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
            }
        }
        TextView teamSubstitutionsPerSet = findViewById(R.id.rules_team_substitutions_per_set);

        SwitchCompat courtSwitchesSwitch = findViewById(R.id.rules_court_switches);
        TextView courtSwitchFrequency = findViewById(R.id.rules_court_switch_frequency);
        TextView courtSwitchFrequencyTieBreak = findViewById(R.id.rules_court_switch_frequency_tie_break);
        TextView consecutiveServes = findViewById(R.id.rules_consecutive_serves_per_player);

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

        View indoorSection = findViewById(R.id.indoor_rules_section);
        View beachSection = findViewById(R.id.beach_rules_section);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_rules_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backToList();
                return true;
            case R.id.action_delete_rules:
                deleteRules();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        backToList();
    }

    public void editRules(View view) {
        ApiRules rules = mStoredRulesService.getRules(mRules.getId());
        Log.i(Tags.STORED_RULES, String.format("Start activity to edit stored rules %s", rules.getName()));

        final Intent intent = new Intent(this, StoredRulesActivity.class);
        intent.putExtra("rules", mStoredRulesService.writeRules(rules));
        intent.putExtra("create", false);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    private void backToList() {
        Intent intent = new Intent(this, StoredRulesListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        UiUtils.animateBackward(this);
    }

    private void deleteRules() {
        Log.i(Tags.STORED_RULES, "Delete rules");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_rules)).setMessage(getString(R.string.delete_rules_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            StoredRulesService storedRulesService = new StoredRulesManager(this);
            storedRulesService.deleteRules(mRules.getId());
            UiUtils.makeText(this, getString(R.string.deleted_rules), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, StoredRulesListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
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
}
