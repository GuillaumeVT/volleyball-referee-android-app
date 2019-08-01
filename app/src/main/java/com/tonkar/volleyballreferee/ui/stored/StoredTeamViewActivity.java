package com.tonkar.volleyballreferee.ui.stored;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.chip.Chip;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.stored.StoredTeamsManager;
import com.tonkar.volleyballreferee.engine.stored.StoredTeamsService;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTeam;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class StoredTeamViewActivity extends AppCompatActivity {

    private StoredTeamsService mStoredTeamsService;
    private IBaseTeam          mTeamService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredTeamsService = new StoredTeamsManager(this);
        mTeamService = mStoredTeamsService.copyTeam(mStoredTeamsService.readTeam(getIntent().getStringExtra("team")));

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
        Chip kindItem = findViewById(R.id.team_kind_item);
        Chip genderItem = findViewById(R.id.team_gender_item);

        nameText.setText(mTeamService.getTeamName(null));

        switch (mTeamService.getGender()) {
            case MIXED:
                genderItem.setChipIconResource(R.drawable.ic_mixed);
                genderItem.setChipBackgroundColorResource(R.color.colorMixedLight);
                genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                genderItem.setChipIconResource(R.drawable.ic_ladies);
                genderItem.setChipBackgroundColorResource(R.color.colorLadiesLight);
                genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                genderItem.setChipIconResource(R.drawable.ic_gents);
                genderItem.setChipBackgroundColorResource(R.color.colorGentsLight);
                genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
        }

        switch (mTeamService.getTeamsKind()) {
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

        GridView playersList = findViewById(R.id.players_list);
        PlayersListAdapter playersListAdapter = new PlayersListAdapter(getLayoutInflater(), this, mTeamService, null);
        playersList.setAdapter(playersListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_team_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backToList();
                return true;
            case R.id.action_delete_team:
                deleteTeam();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        backToList();
    }

    public void editTeam(View view) {
        ApiTeam team = mStoredTeamsService.getTeam(mTeamService.getTeamId(null));
        Log.i(Tags.STORED_TEAMS, String.format("Start activity to edit stored team %s", team.getName()));

        final Intent intent = new Intent(this, StoredTeamActivity.class);
        intent.putExtra("team", mStoredTeamsService.writeTeam(team));
        intent.putExtra("kind",mTeamService.getTeamsKind().toString());
        intent.putExtra("create", false);
        startActivity(intent);
        UiUtils.animateForward(this);
    }

    private void backToList() {
        Intent intent = new Intent(this, StoredTeamsListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        UiUtils.animateBackward(this);
    }

    private void deleteTeam() {
        Log.i(Tags.STORED_TEAMS, "Delete team");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_team)).setMessage(getString(R.string.delete_team_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            StoredTeamsService storedTeamsService = new StoredTeamsManager(this);
            storedTeamsService.deleteTeam(mTeamService.getTeamId(null));
            UiUtils.makeText(StoredTeamViewActivity.this, getString(R.string.deleted_team), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(StoredTeamViewActivity.this, StoredTeamsListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }
}
