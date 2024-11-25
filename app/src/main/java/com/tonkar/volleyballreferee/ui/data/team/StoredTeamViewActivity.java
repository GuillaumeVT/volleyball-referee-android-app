package com.tonkar.volleyballreferee.ui.data.team;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.*;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.TeamDto;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class StoredTeamViewActivity extends AppCompatActivity {

    private StoredTeamsService mStoredTeamsService;
    private IBaseTeam          mTeamService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredTeamsService = new StoredTeamsManager(this);
        mTeamService = mStoredTeamsService.copyTeam(JsonConverters.GSON.fromJson(getIntent().getStringExtra("team"), TeamDto.class));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_team_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView nameText = findViewById(R.id.stored_team_name);
        ImageView kindItem = findViewById(R.id.team_kind_item);
        ImageView genderItem = findViewById(R.id.team_gender_item);

        nameText.setText(mTeamService.getTeamName(null));

        switch (mTeamService.getGender()) {
            case MIXED -> UiUtils.colorChipIcon(this, R.color.colorMixedLight, R.drawable.ic_mixed, genderItem);
            case LADIES -> UiUtils.colorChipIcon(this, R.color.colorLadiesLight, R.drawable.ic_ladies, genderItem);
            case GENTS -> UiUtils.colorChipIcon(this, R.color.colorGentsLight, R.drawable.ic_gents, genderItem);
        }

        switch (mTeamService.getTeamsKind()) {
            case INDOOR_4X4 -> UiUtils.colorChipIcon(this, R.color.colorIndoor4x4Light, R.drawable.ic_4x4_small, kindItem);
            case BEACH -> UiUtils.colorChipIcon(this, R.color.colorBeachLight, R.drawable.ic_beach, kindItem);
            case SNOW -> UiUtils.colorChipIcon(this, R.color.colorSnowLight, R.drawable.ic_snow, kindItem);
            default -> UiUtils.colorChipIcon(this, R.color.colorIndoorLight, R.drawable.ic_6x6_small, kindItem);
        }

        ListView playersList = findViewById(R.id.players_list);
        PlayersListAdapter playersListAdapter = new PlayersListAdapter(getLayoutInflater(), this, mTeamService, null);
        playersList.setAdapter(playersListAdapter);

        ExtendedFloatingActionButton editTeamButton = findViewById(R.id.edit_team_button);
        UiUtils.addExtendShrinkListener(playersList, editTeamButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_team_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (itemId == R.id.action_delete_team) {
            deleteTeam();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void editTeam(View view) {
        TeamDto team = mStoredTeamsService.getTeam(mTeamService.getTeamId(null));

        if (team == null) {
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            Log.i(Tags.STORED_TEAMS, String.format("Start activity to edit stored team %s", team.getName()));

            final Intent intent = new Intent(this, StoredTeamActivity.class);
            intent.putExtra("team", JsonConverters.GSON.toJson(team, TeamDto.class));
            intent.putExtra("kind", mTeamService.getTeamsKind().toString());
            intent.putExtra("create", false);
            startActivity(intent);
            UiUtils.animateForward(this);
        }
    }

    private void deleteTeam() {
        Log.i(Tags.STORED_TEAMS, "Delete team");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_team)).setMessage(getString(R.string.delete_team_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            StoredTeamsService storedTeamsService = new StoredTeamsManager(this);
            storedTeamsService.deleteTeam(mTeamService.getTeamId(null));
            UiUtils.makeText(StoredTeamViewActivity.this, getString(R.string.deleted_team), Toast.LENGTH_LONG).show();
            UiUtils.navigateToMain(this, R.id.stored_teams_list_fragment);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }
}
