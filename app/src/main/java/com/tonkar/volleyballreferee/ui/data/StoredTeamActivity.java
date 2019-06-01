package com.tonkar.volleyballreferee.ui.data;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.StoredTeams;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.data.StoredTeamsService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.BaseTeamServiceHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import com.tonkar.volleyballreferee.ui.team.QuickTeamSetupFragment;
import com.tonkar.volleyballreferee.ui.team.TeamSetupFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class StoredTeamActivity extends AppCompatActivity {

    private StoredTeamsService mStoredTeamsService;
    private BaseTeamService    mTeamService;
    private MenuItem           mSaveItem;
    private boolean            mCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredTeamsService = new StoredTeams(this);
        mTeamService = mStoredTeamsService.copyTeam(mStoredTeamsService.readTeam(getIntent().getStringExtra("team")));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_team);

        String gameTypeStr = getIntent().getStringExtra("kind");
        GameType gameType = GameType.valueOf(gameTypeStr);

        mCreate = getIntent().getBooleanExtra("create", true);

        Fragment fragment = null;

        switch (gameType) {
            case INDOOR:
            case INDOOR_4X4:
                fragment = TeamSetupFragment.newInstance(TeamType.HOME, false, mCreate);
                break;
            case BEACH:
                fragment = QuickTeamSetupFragment.newInstance(TeamType.HOME, mCreate);
                break;
            case TIME:
            default:
                break;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.updateToolbarLogo(toolbar, mTeamService.getTeamsKind(), UsageType.NORMAL);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getIntent().putExtra("team", mStoredTeamsService.writeTeam(mStoredTeamsService.copyTeam(mTeamService)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_team, menu);

        mSaveItem = menu.findItem(R.id.action_save_team);
        computeSaveItemVisibility();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelTeam();
                return true;
            case R.id.action_save_team:
                saveTeam();
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
        cancelTeam();
    }

    private void saveTeam() {
        Log.i(Tags.STORED_TEAMS, "Save team");
        StoredTeamsService storedTeamsService = new StoredTeams(this);
        storedTeamsService.saveTeam(mTeamService, mCreate);
        UiUtils.makeText(StoredTeamActivity.this, getString(R.string.saved_team), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(StoredTeamActivity.this, StoredTeamsListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        UiUtils.animateCreate(this);
    }

    private void deleteTeam() {
        Log.i(Tags.STORED_TEAMS, "Delete team");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_team)).setMessage(getString(R.string.delete_team_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            StoredTeamsService storedTeamsService = new StoredTeams(this);
            storedTeamsService.deleteTeam(mTeamService.getTeamId(null));
            UiUtils.makeText(StoredTeamActivity.this, getString(R.string.deleted_team), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(StoredTeamActivity.this, StoredTeamsListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void cancelTeam() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.leave_team_creation_title)).setMessage(getString(R.string.leave_team_creation_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            Intent intent = new Intent(StoredTeamActivity.this, StoredTeamsListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    public void computeSaveItemVisibility() {
        if (mSaveItem != null) {
            if (mTeamService.getTeamName(null).trim().length() < 1
                    || mTeamService.getNumberOfPlayers(null) < mTeamService.getExpectedNumberOfPlayersOnCourt()
                    || mTeamService.getCaptain(null) < 1) {
                Log.i(Tags.STORED_TEAMS, "Save button is invisible");
                mSaveItem.setVisible(false);
            } else {
                Log.i(Tags.STORED_TEAMS, "Save button is visible");
                mSaveItem.setVisible(true);
            }
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof BaseTeamServiceHandler) {
            BaseTeamServiceHandler baseTeamServiceHandler = (BaseTeamServiceHandler) fragment;
            baseTeamServiceHandler.setTeamService(mTeamService);
        }
    }
}
