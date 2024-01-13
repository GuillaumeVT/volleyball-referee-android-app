package com.tonkar.volleyballreferee.ui.data.team;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.ApiTeam;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.service.StoredTeamsManager;
import com.tonkar.volleyballreferee.engine.service.StoredTeamsService;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.BaseTeamServiceHandler;
import com.tonkar.volleyballreferee.ui.team.QuickTeamSetupFragment;
import com.tonkar.volleyballreferee.ui.team.TeamSetupFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class StoredTeamActivity extends AppCompatActivity {

    private StoredTeamsService mStoredTeamsService;
    private IBaseTeam          mTeamService;
    private boolean            mCreate;

    public StoredTeamActivity() {
        super();
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof BaseTeamServiceHandler) {
                BaseTeamServiceHandler baseTeamServiceHandler = (BaseTeamServiceHandler) fragment;
                baseTeamServiceHandler.setTeamService(mTeamService);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredTeamsService = new StoredTeamsManager(this);
        mTeamService = mStoredTeamsService.copyTeam(JsonConverters.GSON.fromJson(getIntent().getStringExtra("team"), ApiTeam.class));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_team);

        String gameTypeStr = getIntent().getStringExtra("kind");
        GameType gameType = GameType.valueOf(gameTypeStr);

        mCreate = getIntent().getBooleanExtra("create", true);

        Fragment fragment = null;

        switch (gameType) {
            case INDOOR:
            case INDOOR_4X4:
            case SNOW:
                fragment = TeamSetupFragment.newInstance(mTeamService.getTeamsKind(), TeamType.HOME, false);
                break;
            case BEACH:
                fragment = QuickTeamSetupFragment.newInstance(TeamType.HOME);
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

        computeSaveLayoutVisibility();

        ScrollView scrollView = findViewById(R.id.team_scroll_view);
        ExtendedFloatingActionButton saveTeamButton = findViewById(R.id.save_team_button);
        UiUtils.addExtendShrinkListener(scrollView, saveTeamButton);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cancelTeam();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getIntent().putExtra("team", JsonConverters.GSON.toJson(mStoredTeamsService.copyTeam(mTeamService), ApiTeam.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_team, menu);

        MenuItem deleteItem = menu.findItem(R.id.action_delete_team);
        deleteItem.setVisible(!mCreate);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            cancelTeam();
            return true;
        } else if (itemId == R.id.action_delete_team) {
            deleteTeam();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveTeam(View view) {
        Log.i(Tags.STORED_TEAMS, "Save team");
        StoredTeamsService storedTeamsService = new StoredTeamsManager(this);
        storedTeamsService.saveTeam(mTeamService, mCreate);
        UiUtils.makeText(StoredTeamActivity.this, getString(R.string.saved_team), Toast.LENGTH_LONG).show();
        UiUtils.navigateToMain(this, R.id.stored_teams_list_fragment);
        UiUtils.animateForward(this);
    }

    private void deleteTeam() {
        Log.i(Tags.STORED_TEAMS, "Delete team");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_team)).setMessage(getString(R.string.delete_team_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            StoredTeamsService storedTeamsService = new StoredTeamsManager(this);
            storedTeamsService.deleteTeam(mTeamService.getTeamId(null));
            UiUtils.makeText(StoredTeamActivity.this, getString(R.string.deleted_team), Toast.LENGTH_LONG).show();
            UiUtils.navigateToMain(this, R.id.stored_teams_list_fragment);
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
            UiUtils.navigateToMain(this, R.id.stored_teams_list_fragment);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    public void computeSaveLayoutVisibility() {
        View saveLayout = findViewById(R.id.save_team_layout);
        if (mTeamService.getTeamName(null).trim().length() < 1
                || mTeamService.getNumberOfPlayers(null) < mTeamService.getExpectedNumberOfPlayersOnCourt()
                || mTeamService.getCaptain(null) < 0) {
            Log.i(Tags.STORED_TEAMS, "Save button is invisible");
            saveLayout.setVisibility(View.GONE);
        } else {
            Log.i(Tags.STORED_TEAMS, "Save button is visible");
            saveLayout.setVisibility(View.VISIBLE);
        }
    }
}
