package com.tonkar.volleyballreferee.ui.data;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiTeamDescription;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.api.ApiTeam;
import com.tonkar.volleyballreferee.business.data.StoredTeams;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredTeamsService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.ui.NavigationActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class StoredTeamsListActivity extends NavigationActivity implements DataSynchronizationListener {

    private StoredTeamsService     mStoredTeamsService;
    private StoredTeamsListAdapter mStoredTeamsListAdapter;
    private SwipeRefreshLayout     mSyncLayout;
    private boolean                mIsFabOpen;
    private FloatingActionButton   mAdd6x6TeamButton;
    private FloatingActionButton   mAdd4x4TeamButton;
    private FloatingActionButton   mAddBeachTeamButton;

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
        mStoredTeamsService = new StoredTeams(this);

        super.onCreate(savedInstanceState);

        Log.i(Tags.STORED_TEAMS, "Create teams list activity");
        setContentView(R.layout.activity_stored_teams_list);

        initNavigationMenu();

        mSyncLayout = findViewById(R.id.sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateStoredTeamsList);

        List<ApiTeamDescription> teams = mStoredTeamsService.getListTeams();

        final ListView storedTeamsList = findViewById(R.id.stored_teams_list);
        mStoredTeamsListAdapter = new StoredTeamsListAdapter(this, getLayoutInflater(), teams);
        storedTeamsList.setAdapter(mStoredTeamsListAdapter);

        storedTeamsList.setOnItemClickListener((adapterView, view, i, l) -> {
            ApiTeamDescription teamDescription = mStoredTeamsListAdapter.getItem(i);
            ApiTeam team = mStoredTeamsService.getTeam(teamDescription.getId());
            Log.i(Tags.STORED_TEAMS, String.format("Start activity to edit stored team %s", team.getName()));

            final Intent intent = new Intent(StoredTeamsListActivity.this, StoredTeamActivity.class);
            intent.putExtra("team", mStoredTeamsService.writeTeam(team));
            intent.putExtra("kind", team.getKind().toString());
            intent.putExtra("create", false);
            startActivity(intent);
            UiUtils.animateForward(this);
        });

        mIsFabOpen = false;
        mAdd6x6TeamButton = findViewById(R.id.add_6x6_team_button);
        mAdd4x4TeamButton = findViewById(R.id.add_4x4_team_button);
        mAddBeachTeamButton = findViewById(R.id.add_beach_team_button);
        mAdd6x6TeamButton.hide();
        mAdd4x4TeamButton.hide();
        mAddBeachTeamButton.hide();
        FloatingActionButton addTeamButton = findViewById(R.id.add_team_button);
        addTeamButton.setOnClickListener(view -> {
            if(mIsFabOpen){
                closeFABMenu();
            }else{
                showFABMenu();
            }
        });

        updateStoredTeamsList();
    }

    public void addIndoorTeam(View view) {
        GameType gameType = GameType.INDOOR;
        addTeam(gameType);
    }

    public void addIndoor4x4Team(View view) {
        GameType gameType = GameType.INDOOR_4X4;
        addTeam(gameType);
    }

    public void addBeachTeam(View view) {
        GameType gameType = GameType.BEACH;
        addTeam(gameType);
    }

    private void addTeam(GameType gameType) {
        Log.i(Tags.STORED_TEAMS, "Start activity to create new team");
        BaseTeamService teamService = mStoredTeamsService.createTeam(gameType);

        final Intent intent = new Intent(this, StoredTeamActivity.class);
        intent.putExtra("team", mStoredTeamsService.writeTeam(mStoredTeamsService.copyTeam(teamService)));
        intent.putExtra("kind", gameType.toString());
        intent.putExtra("create", true);
        startActivity(intent);
        UiUtils.animateCreate(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_teams, menu);

        MenuItem deleteAllTeamsItem = menu.findItem(R.id.action_delete_teams);
        deleteAllTeamsItem.setVisible(mStoredTeamsService.hasTeams());

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
        switch (item.getItemId()) {
            case R.id.action_search_teams:
                return true;
            case R.id.action_sync:
                updateStoredTeamsList();
                return true;
            case R.id.action_delete_teams:
                deleteAllTeams();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllTeams() {
        Log.i(Tags.STORED_TEAMS, "Delete all teams");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_teams)).setMessage(getResources().getString(R.string.delete_teams_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredTeamsService.deleteAllTeams();
            UiUtils.makeText(StoredTeamsListActivity.this, getResources().getString(R.string.deleted_teams), Toast.LENGTH_LONG).show();
            UiUtils.navigateToHome(StoredTeamsListActivity.this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void showFABMenu(){
        mIsFabOpen = true;
        mAdd6x6TeamButton.show();
        mAdd4x4TeamButton.show();
        mAddBeachTeamButton.show();
        mAdd6x6TeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_third));
        mAdd4x4TeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_second));
        mAddBeachTeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_first));
    }

    private void closeFABMenu(){
        mIsFabOpen = false;
        mAdd6x6TeamButton.animate().translationY(0);
        mAdd4x4TeamButton.animate().translationY(0);
        mAddBeachTeamButton.animate().translationY(0);
        mAdd6x6TeamButton.hide();
        mAdd4x4TeamButton.hide();
        mAddBeachTeamButton.hide();
    }

    @Override
    public void onBackPressed() {
        if(mIsFabOpen){
            closeFABMenu();
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
        mStoredTeamsListAdapter.updateStoredTeamsList(mStoredTeamsService.getListTeams());
        mSyncLayout.setRefreshing(false);
    }

    @Override
    public void onSynchronizationFailed() {
        UiUtils.makeText(this, getResources().getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
        mSyncLayout.setRefreshing(false);
    }
}
