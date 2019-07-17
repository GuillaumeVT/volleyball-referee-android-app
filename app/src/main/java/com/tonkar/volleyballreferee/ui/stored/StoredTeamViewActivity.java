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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
        ImageView gameTypeImage = findViewById(R.id.stored_team_type_image);
        ImageView genderTypeImage = findViewById(R.id.stored_team_gender_image);

        nameText.setText(mTeamService.getTeamName(null));

        switch (mTeamService.getGender()) {
            case MIXED:
                genderTypeImage.setImageResource(R.drawable.ic_mixed);
                genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                genderTypeImage.setImageResource(R.drawable.ic_ladies);
                genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                genderTypeImage.setImageResource(R.drawable.ic_gents);
                genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorGents), PorterDuff.Mode.SRC_IN));
                break;
        }

        switch (mTeamService.getTeamsKind()) {
            case INDOOR_4X4:
                gameTypeImage.setImageResource(R.drawable.ic_4x4);
                gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorIndoor4x4), PorterDuff.Mode.SRC_IN));
                break;
            case BEACH:
                gameTypeImage.setImageResource(R.drawable.ic_sun);
                gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorBeach), PorterDuff.Mode.SRC_IN));
                break;
            case TIME:
                gameTypeImage.setImageResource(R.drawable.ic_time_based);
                gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorTime), PorterDuff.Mode.SRC_IN));
                break;
            case INDOOR:
            default:
                gameTypeImage.setImageResource(R.drawable.ic_6x6);
                gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorIndoor), PorterDuff.Mode.SRC_IN));
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
