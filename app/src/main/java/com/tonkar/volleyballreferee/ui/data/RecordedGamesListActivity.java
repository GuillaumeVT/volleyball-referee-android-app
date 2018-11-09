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
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGamesService;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.ui.NavigationActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class RecordedGamesListActivity extends NavigationActivity implements DataSynchronizationListener {

    private RecordedGamesService     mRecordedGamesService;
    private RecordedGamesListAdapter mRecordedGamesListAdapter;
    private SwipeRefreshLayout       mSyncLayout;

    @Override
    protected String getToolbarTitle() {
        return "";
    }

    @Override
    protected int getCheckedItem() {
        return R.id.action_recorded_games;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(Tags.SAVED_GAMES, "Create recorded games list activity");
        setContentView(R.layout.activity_recorded_games_list);

        initNavigationMenu();

        mSyncLayout = findViewById(R.id.sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateRecordedGamesList);

        mRecordedGamesService = ServicesProvider.getInstance().getRecordedGamesService(getApplicationContext());

        List<RecordedGameService> recordedGameServiceList = mRecordedGamesService.getRecordedGameServiceList();
        // Inverse list to have most recent games on top of the list
        Collections.reverse(recordedGameServiceList);

        final ListView recordedGamesList = findViewById(R.id.recorded_games_list);
        mRecordedGamesListAdapter = new RecordedGamesListAdapter(this, getLayoutInflater(), recordedGameServiceList);
        recordedGamesList.setAdapter(mRecordedGamesListAdapter);

        recordedGamesList.setOnItemClickListener((adapterView, view, i, l) -> {
            RecordedGameService recordedGameService = mRecordedGamesListAdapter.getItem(i);
            Log.i(Tags.SAVED_GAMES, String.format("Start activity to display recorded game %s", recordedGameService.getGameSummary()));

            final Intent intent;

            if ((GameType.INDOOR.equals(recordedGameService.getGameType()) || GameType.INDOOR_4X4.equals(recordedGameService.getGameType())) && UsageType.NORMAL.equals(recordedGameService.getUsageType())) {
                intent = new Intent(RecordedGamesListActivity.this, RecordedIndoorGameActivity.class);
            } else {
                intent = new Intent(RecordedGamesListActivity.this, RecordedBeachGameActivity.class);
            }

            intent.putExtra("game_date", recordedGameService.getGameDate());
            startActivity(intent);
        });

        updateRecordedGamesList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recorded_games, menu);

        MenuItem deleteAllGamesItem = menu.findItem(R.id.action_delete_games);
        deleteAllGamesItem.setVisible(mRecordedGamesService.hasRecordedGames());

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
        syncItem.setVisible(PrefUtils.isSyncOn(this));

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
        Log.i(Tags.SAVED_GAMES, "Delete all games");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_games)).setMessage(getResources().getString(R.string.delete_games_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mRecordedGamesService.deleteAllRecordedGames();
            UiUtils.makeText(RecordedGamesListActivity.this, getResources().getString(R.string.deleted_games), Toast.LENGTH_LONG).show();
            UiUtils.navigateToHome(RecordedGamesListActivity.this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void updateRecordedGamesList() {
        if (PrefUtils.isSyncOn(this)) {
            mSyncLayout.setRefreshing(true);
            mRecordedGamesService.syncGamesOnline(this);
        }
    }

    @Override
    public void onSynchronizationSucceeded() {
        mRecordedGamesListAdapter.updateRecordedGamesList(mRecordedGamesService.getRecordedGameServiceList());
        mSyncLayout.setRefreshing(false);
    }

    @Override
    public void onSynchronizationFailed() {
        UiUtils.makeText(this, getResources().getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
        mSyncLayout.setRefreshing(false);
    }
}
