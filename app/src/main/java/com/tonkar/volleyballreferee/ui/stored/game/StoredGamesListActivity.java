package com.tonkar.volleyballreferee.ui.stored.game;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.stored.DataSynchronizationListener;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesManager;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesService;
import com.tonkar.volleyballreferee.engine.stored.api.ApiGameSummary;
import com.tonkar.volleyballreferee.ui.NavigationActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

public class StoredGamesListActivity extends NavigationActivity implements DataSynchronizationListener {

    private StoredGamesService     mStoredGamesService;
    private StoredGamesListAdapter mStoredGamesListAdapter;
    private SwipeRefreshLayout     mSyncLayout;
    private MenuItem               mDeleteSelectedGamesItem;

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

        Log.i(Tags.STORED_GAMES, "Create stored games list activity");
        setContentView(R.layout.activity_stored_games_list);

        initNavigationMenu();

        mSyncLayout = findViewById(R.id.sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateStoredGamesList);

        mStoredGamesService = new StoredGamesManager(this);

        List<ApiGameSummary> games = mStoredGamesService.listGames();

        final ListView storedGamesList = findViewById(R.id.stored_games_list);
        mStoredGamesListAdapter = new StoredGamesListAdapter(this, getLayoutInflater(), games);
        storedGamesList.setAdapter(mStoredGamesListAdapter);

        storedGamesList.setOnItemClickListener((parent, view, position, l) -> {
            ApiGameSummary game = mStoredGamesListAdapter.getItem(position);

            if (mStoredGamesListAdapter.hasSelectedItems()) {
                mStoredGamesListAdapter.toggleItemSelection(game.getId());
                mDeleteSelectedGamesItem.setVisible(mStoredGamesListAdapter.hasSelectedItems());
            } else {
                Log.i(Tags.STORED_GAMES, String.format("Start activity to display stored game %s", game.getId()));

                final Intent intent;

                if (UsageType.POINTS_SCOREBOARD.equals(game.getUsage()) || GameType.TIME.equals(game.getKind())) {
                    intent = new Intent(StoredGamesListActivity.this, StoredBasicGameActivity.class);
                } else {
                    intent = new Intent(StoredGamesListActivity.this, StoredAdvancedGameActivity.class);
                }

                intent.putExtra("game", game.getId());
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "listItemToDetails").toBundle());
            }
        });

        storedGamesList.setOnItemLongClickListener((parent, view, position, id) -> {
            ApiGameSummary game = mStoredGamesListAdapter.getItem(position);
            mStoredGamesListAdapter.toggleItemSelection(game.getId());
            mDeleteSelectedGamesItem.setVisible(mStoredGamesListAdapter.hasSelectedItems());
            return true;
        });

        updateStoredGamesList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_games, menu);

        mDeleteSelectedGamesItem = menu.findItem(R.id.action_delete_games);
        mDeleteSelectedGamesItem.setVisible(false);

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
                mStoredGamesListAdapter.getFilter().filter(searchQuery.trim());
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
                updateStoredGamesList();
                return true;
            case R.id.action_delete_games:
                deleteSelectedGames();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(mStoredGamesListAdapter.hasSelectedItems()){
            mStoredGamesListAdapter.clearSelectedItems();
        } else {
            super.onBackPressed();
        }
    }

    private void deleteSelectedGames() {
        Log.i(Tags.STORED_GAMES, "Delete selected games");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_selected_games)).setMessage(getString(R.string.delete_selected_games_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredGamesService.deleteGames(mStoredGamesListAdapter.getSelectedItems(), this);
            UiUtils.makeText(StoredGamesListActivity.this, getString(R.string.deleted_selected_games), Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void updateStoredGamesList() {
        if (PrefUtils.canSync(this)) {
            mSyncLayout.setRefreshing(true);
            mStoredGamesService.syncGames(this);
        }
    }

    @Override
    public void onSynchronizationSucceeded() {
        runOnUiThread(() -> {
            mStoredGamesListAdapter.updateStoredGamesList(mStoredGamesService.listGames());
            if (mDeleteSelectedGamesItem != null) {
                mDeleteSelectedGamesItem.setVisible(mStoredGamesListAdapter.hasSelectedItems());
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
