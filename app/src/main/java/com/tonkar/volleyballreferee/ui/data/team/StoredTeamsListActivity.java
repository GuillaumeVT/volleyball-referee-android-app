package com.tonkar.volleyballreferee.ui.data.team;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.ApiTeam;
import com.tonkar.volleyballreferee.engine.api.model.ApiTeamSummary;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.service.DataSynchronizationListener;
import com.tonkar.volleyballreferee.engine.service.StoredTeamsManager;
import com.tonkar.volleyballreferee.engine.service.StoredTeamsService;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.ui.NavigationActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

public class StoredTeamsListActivity extends NavigationActivity implements DataSynchronizationListener {

    private StoredTeamsService           mStoredTeamsService;
    private StoredTeamsListAdapter       mStoredTeamsListAdapter;
    private SwipeRefreshLayout           mSyncLayout;
    private View                         mFabMenu;
    private boolean                      mIsFabOpen;
    private ExtendedFloatingActionButton mAddTeamButton;
    private ExtendedFloatingActionButton mAdd6x6TeamButton;
    private ExtendedFloatingActionButton mAdd4x4TeamButton;
    private ExtendedFloatingActionButton mAddBeachTeamButton;
    private ExtendedFloatingActionButton mAddSnowTeamButton;
    private MenuItem                     mDeleteSelectedTeamsItem;

    @Override
    protected String getToolbarTitle() {
        return "";
    }

    @Override
    protected int getCheckedItem() {
        return R.id.action_stored_teams;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredTeamsService = new StoredTeamsManager(this);

        super.onCreate(savedInstanceState);

        Log.i(Tags.STORED_TEAMS, "Create teams list activity");
        setContentView(R.layout.activity_stored_teams_list);

        initNavigationMenu();

        mSyncLayout = findViewById(R.id.stored_teams_sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateStoredTeamsList);

        mFabMenu = findViewById(R.id.stored_teams_fab_menu);
        mFabMenu.setVisibility(View.INVISIBLE);

        List<ApiTeamSummary> teams = mStoredTeamsService.listTeams();

        final ListView storedTeamsList = findViewById(R.id.stored_teams_list);
        mStoredTeamsListAdapter = new StoredTeamsListAdapter(this, getLayoutInflater(), teams);
        storedTeamsList.setAdapter(mStoredTeamsListAdapter);

        storedTeamsList.setOnItemClickListener((parent, view, position, l) -> {
            ApiTeamSummary teamDescription = mStoredTeamsListAdapter.getItem(position);

            if (mStoredTeamsListAdapter.hasSelectedItems()) {
                mStoredTeamsListAdapter.toggleItemSelection(teamDescription.getId());
                mDeleteSelectedTeamsItem.setVisible(mStoredTeamsListAdapter.hasSelectedItems());
            } else {
                ApiTeam team = mStoredTeamsService.getTeam(teamDescription.getId());
                Log.i(Tags.STORED_TEAMS, String.format("Start activity to view stored team %s", team.getName()));

                final Intent intent = new Intent(StoredTeamsListActivity.this, StoredTeamViewActivity.class);
                intent.putExtra("team", JsonConverters.GSON.toJson(team, ApiTeam.class));
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "listItemToDetails").toBundle());
            }
        });

        storedTeamsList.setOnItemLongClickListener((parent, view, position, id) -> {
            ApiTeamSummary teamDescription = mStoredTeamsListAdapter.getItem(position);
            mStoredTeamsListAdapter.toggleItemSelection(teamDescription.getId());
            mDeleteSelectedTeamsItem.setVisible(mStoredTeamsListAdapter.hasSelectedItems());
            return true;
        });

        mIsFabOpen = false;
        mAdd6x6TeamButton = findViewById(R.id.add_6x6_team_button);
        mAdd4x4TeamButton = findViewById(R.id.add_4x4_team_button);
        mAddBeachTeamButton = findViewById(R.id.add_beach_team_button);
        mAddSnowTeamButton = findViewById(R.id.add_snow_team_button);
        mAddTeamButton = findViewById(R.id.add_team_button);
        mAddTeamButton.setOnClickListener(view -> {
            if(mIsFabOpen){
                closeFABMenu();
            }else{
                showFABMenu();
            }
        });
        closeFABMenu();

        updateStoredTeamsList();

        UiUtils.addExtendShrinkListener(storedTeamsList, mAddTeamButton);
    }

    public void addIndoorTeam(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new indoor team");
        addTeam(GameType.INDOOR, view);
    }

    public void addIndoor4x4Team(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new indoor 4x4 team");
        addTeam(GameType.INDOOR_4X4, view);
    }

    public void addBeachTeam(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new beach team");
        addTeam(GameType.BEACH, view);
    }

    public void addSnowTeam(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new snow team");
        addTeam(GameType.SNOW, view);
    }

    private void addTeam(GameType gameType, View view) {
        Log.i(Tags.STORED_TEAMS, "Start activity to create new team");
        IBaseTeam teamService = mStoredTeamsService.createTeam(gameType);

        final Intent intent = new Intent(this, StoredTeamActivity.class);
        intent.putExtra("team", JsonConverters.GSON.toJson(mStoredTeamsService.copyTeam(teamService), ApiTeam.class));
        intent.putExtra("kind", gameType.toString());
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "gameKindToToolbar").toBundle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_teams, menu);

        mDeleteSelectedTeamsItem = menu.findItem(R.id.action_delete_teams);
        mDeleteSelectedTeamsItem.setVisible(false);

        MenuItem searchTeamsItem = menu.findItem(R.id.action_search_teams);
        SearchView searchTeamsView = (SearchView) searchTeamsItem.getActionView();

        searchTeamsView.setOnQueryTextFocusChangeListener((view, hasFocus) -> {});

        searchTeamsView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                mStoredTeamsListAdapter.getFilter().filter(searchQuery.trim());
                return true;
            }
        });

        MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(PrefUtils.canSync(this));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_search_teams) {
            return true;
        } else if (itemId == R.id.action_sync) {
            updateStoredTeamsList();
            return true;
        } else if (itemId == R.id.action_delete_teams) {
            deleteSelectedTeams();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteSelectedTeams() {
        Log.i(Tags.STORED_TEAMS, "Delete selected teams");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_selected_teams)).setMessage(getString(R.string.delete_selected_teams_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredTeamsService.deleteTeams(mStoredTeamsListAdapter.getSelectedItems(), this);
            UiUtils.makeText(StoredTeamsListActivity.this, getString(R.string.deleted_teams), Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void showFABMenu(){
        mIsFabOpen = true;
        UiUtils.colorCloseIconButton(this, mAddTeamButton);
        mFabMenu.setVisibility(View.VISIBLE);
        mAdd6x6TeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_first));
        mAdd4x4TeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_third));
        mAddBeachTeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_second));
        mAddSnowTeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_fourth));
    }

    private void closeFABMenu(){
        mIsFabOpen = false;
        UiUtils.colorPlusIconButton(this, mAddTeamButton);
        mAdd6x6TeamButton.animate().translationY(0);
        mAdd4x4TeamButton.animate().translationY(0);
        mAddBeachTeamButton.animate().translationY(0);
        mAddSnowTeamButton.animate().translationY(0).withEndAction(() -> mFabMenu.setVisibility(View.INVISIBLE));
    }

    @Override
    public void onBackPressed() {
        if(mIsFabOpen) {
            closeFABMenu();
        } else if(mStoredTeamsListAdapter.hasSelectedItems()){
            mStoredTeamsListAdapter.clearSelectedItems();
        } else {
            super.onBackPressed();
        }
    }

    private void updateStoredTeamsList() {
        if (PrefUtils.canSync(this)) {
            mSyncLayout.setRefreshing(true);
            mStoredTeamsService.syncTeams(this);
        }
    }

    @Override
    public void onSynchronizationSucceeded() {
        runOnUiThread(() -> {
            mStoredTeamsListAdapter.updateStoredTeamsList(mStoredTeamsService.listTeams());
            if (mDeleteSelectedTeamsItem != null) {
                mDeleteSelectedTeamsItem.setVisible(mStoredTeamsListAdapter.hasSelectedItems());
            }
            mSyncLayout.setRefreshing(false);
        });
    }

    @Override
    public void onSynchronizationFailed() {
        runOnUiThread(() -> {
            UiUtils.makeErrorText(this, getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
            mSyncLayout.setRefreshing(false);
        });
    }
}
