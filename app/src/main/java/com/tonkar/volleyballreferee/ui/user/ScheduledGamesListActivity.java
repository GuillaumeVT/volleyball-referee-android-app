package com.tonkar.volleyballreferee.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.GameDescription;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.data.AsyncGameRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.ui.AlertDialogFragment;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.setup.GameSetupActivity;
import com.tonkar.volleyballreferee.ui.setup.QuickGameSetupActivity;

import java.util.List;

public class ScheduledGamesListActivity extends AppCompatActivity implements AsyncGameRequestListener {

    private SwipeRefreshLayout        mSyncLayout;
    private ScheduledGamesListAdapter mScheduledGamesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-SRGamesActivity", "Create scheduled games list activity");
        setContentView(R.layout.activity_scheduled_games_list);

        setTitle(getResources().getString(R.string.user_scheduled_games_title));

        ServicesProvider.getInstance().restoreGameService(getApplicationContext());

        mSyncLayout = findViewById(R.id.sync_layout);
        mSyncLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateScheduledGamesList();
                    }
                }
        );

        final ListView scheduledGamesList = findViewById(R.id.scheduled_games_list);
        mScheduledGamesListAdapter = new ScheduledGamesListAdapter(getLayoutInflater());
        scheduledGamesList.setAdapter(mScheduledGamesListAdapter);

        scheduledGamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GameDescription gameDescription = mScheduledGamesListAdapter.getItem(i);
                if (!GameType.TIME.equals(gameDescription.getGameType())) {
                    ServicesProvider.getInstance().getRecordedGamesService().getUserGame(PrefUtils.getUserId(ScheduledGamesListActivity.this), gameDescription.getGameDate(), ScheduledGamesListActivity.this);
                }
            }
        });

        updateScheduledGamesList();

        if (savedInstanceState != null) {
            restoreEditScheduledGameDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scheduled_games, menu);

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
                mScheduledGamesListAdapter.getFilter().filter(searchQuery.trim());
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
            case R.id.action_sync:
                updateScheduledGamesList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateScheduledGamesList() {
        mSyncLayout.setRefreshing(true);
        ServicesProvider.getInstance().getRecordedGamesService().getUserScheduledGames(PrefUtils.getUserId(this), this);
    }

    @Override
    public void onRecordedGameReceivedFromCode(RecordedGameService recordedGameService) {}

    @Override
    public void onUserGameReceived(RecordedGameService recordedGameService) {
        if (recordedGameService != null) {
            final GameService gameService = GameFactory.createGame(recordedGameService);
            Log.i("VBR-SRGamesActivity", "Start game activity after receiving game");

            switch (recordedGameService.getMatchStatus()) {
                case SCHEDULED:
                    AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("schedule_game_edit");

                    if (alertDialogFragment == null) {
                        alertDialogFragment = AlertDialogFragment.newInstance(getResources().getString(R.string.user_scheduled_games_title), getResources().getString(R.string.scheduled_game_question),
                                getResources().getString(R.string.no), getResources().getString(R.string.yes), getResources().getString(android.R.string.cancel));
                        alertDialogFragment.show(getSupportFragmentManager(), "schedule_game_edit");
                    }

                    setEditScheduledGameListener(alertDialogFragment, gameService);
                    break;
                case LIVE:
                    gameService.restoreGame(recordedGameService);
                    final Intent gameIntent = new Intent(this, GameActivity.class);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(gameIntent);
                    break;
                default:
                    break;
            }
        }
        mSyncLayout.setRefreshing(false);
    }

    @Override
    public void onUserGameListReceived(List<GameDescription> gameDescriptionList) {
        mScheduledGamesListAdapter.updateGameDescriptionList(gameDescriptionList);
        mSyncLayout.setRefreshing(false);
    }

    @Override
    public void onNotFound() {
        mSyncLayout.setRefreshing(false);
        Toast.makeText(this, getResources().getString(R.string.download_error_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInternalError() {
        mSyncLayout.setRefreshing(false);
        Toast.makeText(this, getResources().getString(R.string.download_error_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError() {
        mSyncLayout.setRefreshing(false);
        Toast.makeText(this, getResources().getString(R.string.download_error_message), Toast.LENGTH_LONG).show();
    }

    private void restoreEditScheduledGameDialog() {
        GameService gameService = ServicesProvider.getInstance().getGameService();
        AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag("schedule_game_edit");

        if (gameService == null && alertDialogFragment != null) {
            alertDialogFragment.dismiss();
        } else {
            setEditScheduledGameListener(alertDialogFragment, gameService);
        }
    }

    private void setEditScheduledGameListener(AlertDialogFragment alertDialogFragment, final GameService gameService) {
        if (alertDialogFragment != null) {
            alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
                @Override
                public void onNegativeButtonClicked() {
                    Log.i("VBR-SRGamesActivity", "Start scheduled game immediately");
                    gameService.startMatch();
                    final Intent gameIntent = new Intent(ScheduledGamesListActivity.this, GameActivity.class);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(gameIntent);
                }

                @Override
                public void onPositiveButtonClicked() {
                    Log.i("VBR-SRGamesActivity", "Edit scheduled game before starting");
                    final Intent setupIntent;
                    if (gameService.getGameType().equals(GameType.BEACH)) {
                        setupIntent = new Intent(ScheduledGamesListActivity.this, QuickGameSetupActivity.class);
                    } else {
                        setupIntent = new Intent(ScheduledGamesListActivity.this, GameSetupActivity.class);
                    }
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(setupIntent);
                }

                @Override
                public void onNeutralButtonClicked() {}
            });
        }
    }
}
