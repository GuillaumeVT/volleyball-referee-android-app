package com.tonkar.volleyballreferee.ui.data;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.StoredGames;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredGamesService;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.ui.NavigationActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class RecordedGamesListActivity extends NavigationActivity implements DataSynchronizationListener {

    private StoredGamesService       mStoredGamesService;
    private RecordedGamesListAdapter mRecordedGamesListAdapter;
    private SwipeRefreshLayout       mSyncLayout;

    @Override
    protected String getToolbarTitle() {
        return "";
    }

    @Override
    protected int getCheckedItem() {
        return R.id.action_stored_games;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(Tags.STORED_GAMES, "Create recorded games list activity");
        setContentView(R.layout.activity_recorded_games_list);

        initNavigationMenu();

        mSyncLayout = findViewById(R.id.sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateRecordedGamesList);

        mStoredGamesService = new StoredGames(this);

        List<StoredGameService> storedGameServiceList = mStoredGamesService.listGames();

        final ListView recordedGamesList = findViewById(R.id.recorded_games_list);
        mRecordedGamesListAdapter = new RecordedGamesListAdapter(this, getLayoutInflater(), storedGameServiceList);
        recordedGamesList.setAdapter(mRecordedGamesListAdapter);

        recordedGamesList.setOnItemClickListener((adapterView, view, i, l) -> {
            StoredGameService storedGameService = mRecordedGamesListAdapter.getItem(i);
            Log.i(Tags.STORED_GAMES, String.format("Start activity to display recorded game %s", storedGameService.getGameSummary()));

            final Intent intent;

            if ((GameType.INDOOR.equals(storedGameService.getKind()) || GameType.INDOOR_4X4.equals(storedGameService.getKind())) && UsageType.NORMAL.equals(storedGameService.getUsage())) {
                intent = new Intent(RecordedGamesListActivity.this, RecordedIndoorGameActivity.class);
            } else {
                intent = new Intent(RecordedGamesListActivity.this, RecordedBeachGameActivity.class);
            }

            intent.putExtra("game_date", storedGameService.getGameDate());
            startActivity(intent);
            UiUtils.animateForward(this);
        });

        updateRecordedGamesList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recorded_games, menu);

        MenuItem deleteAllGamesItem = menu.findItem(R.id.action_delete_games);
        deleteAllGamesItem.setVisible(mStoredGamesService.hasGames());

        MenuItem searchGamesItem = menu.findItem(R.id.action_search_games);
        SearchView searchGamesView = (SearchView) searchGamesItem.getActionView();

        searchGamesView.setOnQueryTextFocusChangeListener((view, hasFocus) -> {});

        searchGamesView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                mRecordedGamesListAdapter.getFilter().filter(searchQuery.trim());
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
            case R.id.action_search_games:
                return true;
            case R.id.action_sync:
                updateRecordedGamesList();
                return true;
            case R.id.action_delete_games:
                deleteAllGames();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllGames() {
        Log.i(Tags.STORED_GAMES, "Delete all games");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_games)).setMessage(getResources().getString(R.string.delete_games_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredGamesService.deleteAllGames();
            UiUtils.makeText(RecordedGamesListActivity.this, getResources().getString(R.string.deleted_games), Toast.LENGTH_LONG).show();
            UiUtils.navigateToHome(RecordedGamesListActivity.this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void updateRecordedGamesList() {
        if (PrefUtils.canSync(this)) {
            mSyncLayout.setRefreshing(true);
            mStoredGamesService.syncGames(this);
        }
    }

    @Override
    public void onSynchronizationSucceeded() {
        mRecordedGamesListAdapter.updateRecordedGamesList(mStoredGamesService.listGames());
        mSyncLayout.setRefreshing(false);
    }

    @Override
    public void onSynchronizationFailed() {
        UiUtils.makeText(this, getResources().getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
        mSyncLayout.setRefreshing(false);
    }
}
