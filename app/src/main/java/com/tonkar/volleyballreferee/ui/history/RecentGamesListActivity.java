package com.tonkar.volleyballreferee.ui.history;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.Collections;
import java.util.List;

public class RecentGamesListActivity extends AppCompatActivity {

    private GamesHistoryService    mGamesHistoryService;
    private RecentGamesListAdapter mRecentGamesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-RecentListActivity", "Create recent games list activity");
        setContentView(R.layout.activity_recent_games_list);

        ServicesProvider.getInstance().restoreGamesHistoryService(getApplicationContext());

        setTitle(getResources().getString(R.string.recent_games));

        mGamesHistoryService = ServicesProvider.getInstance().getGamesHistoryService();

        List<RecordedGameService> recordedGameServiceList = mGamesHistoryService.getRecordedGameServiceList();
        // Inverse list to have most recent games on top of the list
        Collections.reverse(recordedGameServiceList);

        final ListView recentGamesList = findViewById(R.id.recent_games_list);
        mRecentGamesListAdapter = new RecentGamesListAdapter(getLayoutInflater(), recordedGameServiceList);
        recentGamesList.setAdapter(mRecentGamesListAdapter);

        recentGamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RecordedGameService recordedGameService = (RecordedGameService) mRecentGamesListAdapter.getItem(i);
                Log.i("VBR-RecentListActivity", String.format("Start activity to display recent game %s", recordedGameService.getGameSummary()));

                final Intent intent;

                if (GameType.INDOOR.equals(recordedGameService.getGameType())
                        && recordedGameService.getPlayers(TeamType.HOME).size() > 0
                        && recordedGameService.getPlayers(TeamType.GUEST).size() > 0) {
                    intent = new Intent(RecentGamesListActivity.this, RecentIndoorGameActivity.class);
                } else {
                    intent = new Intent(RecentGamesListActivity.this, RecentBeachGameActivity.class);
                }

                intent.putExtra("game_date", recordedGameService.getGameDate());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recent_games, menu);

        MenuItem deleteAllGamesItem = menu.findItem(R.id.action_delete_games);
        deleteAllGamesItem.setVisible(mGamesHistoryService.getRecordedGameServiceList().size() > 0);

        MenuItem searchGamesItem = menu.findItem(R.id.action_search_games);
        SearchView searchGamesView = (SearchView) searchGamesItem.getActionView();

        searchGamesView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });

        searchGamesView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                mRecentGamesListAdapter.filter(searchQuery.trim());
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_games:
                return true;
            case R.id.action_delete_games:
                deleteAllGames();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllGames() {
        Log.i("VBR-RecentListActivity", "Delete all games");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_games)).setMessage(getResources().getString(R.string.delete_games_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mGamesHistoryService.deleteAllRecordedGames();
                Toast.makeText(RecentGamesListActivity.this, getResources().getString(R.string.deleted_games), Toast.LENGTH_LONG).show();
                UiUtils.navigateToHome(RecentGamesListActivity.this);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

}
