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
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GameHistoryClient;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;

import java.util.Collections;
import java.util.List;

public class RecentGamesListActivity extends AppCompatActivity implements GameHistoryClient {

    private GamesHistoryService    mGamesHistoryService;
    private RecentGamesListAdapter mRecentGamesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_games_list);

        Log.i("VBR-RecentListActivity", "Create recent games list activity");

        setTitle(getResources().getString(R.string.recent_games));

        setGameHistoryService(ServicesProvider.getInstance().getGameHistoryService());

        List<RecordedGameService> recordedGameServiceList = mGamesHistoryService.getRecordedGameServiceList();
        // Inverse list to have most recent games on top of the list
        Collections.reverse(recordedGameServiceList);

        final ListView recentGamesList = findViewById(R.id.recent_games_list);
        mRecentGamesListAdapter = new RecentGamesListAdapter(getLayoutInflater(), recordedGameServiceList);
        recentGamesList.setAdapter(mRecentGamesListAdapter);

        recentGamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("VBR-RecentListActivity", String.format("Start activity to display recent game %s", ((RecordedGameService) mRecentGamesListAdapter.getItem(i)).getGameSummary()));
                final Intent intent = new Intent(RecentGamesListActivity.this, RecentGameActivity.class);
                intent.putExtra("game_date", mRecentGamesListAdapter.getGameDate(i));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recent_games, menu);

        MenuItem searchGamesItem = menu.findItem(R.id.action_search_games);
        SearchView searchGamesView = (SearchView) searchGamesItem.getActionView();

        searchGamesView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {}
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
                Toast.makeText(RecentGamesListActivity.this, getResources().getString(R.string.deleted_game), Toast.LENGTH_LONG).show();
                mRecentGamesListAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.show();
    }

    @Override
    public void setGameHistoryService(GamesHistoryService gamesHistoryService) {
        mGamesHistoryService = gamesHistoryService;
    }

}
