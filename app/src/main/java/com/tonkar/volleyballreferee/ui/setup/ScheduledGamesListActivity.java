package com.tonkar.volleyballreferee.ui.setup;

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
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.GameDescription;
import com.tonkar.volleyballreferee.business.data.JsonIOUtils;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.AsyncGameRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.ui.util.AlertDialogFragment;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ScheduledGamesListActivity extends AppCompatActivity implements AsyncGameRequestListener {

    private SwipeRefreshLayout        mSyncLayout;
    private ScheduledGamesListAdapter mScheduledGamesListAdapter;
    private boolean                   mIsFabOpen;
    private FloatingActionButton      mScheduleIndoorGameButton;
    private FloatingActionButton      mScheduleIndoor4x4GameButton;
    private FloatingActionButton      mScheduleBeachGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(Tags.SCHEDULE_UI, "Create scheduled games list activity");
        setContentView(R.layout.activity_scheduled_games_list);

        setTitle(getResources().getString(R.string.user_scheduled_games_title));

        ServicesProvider.getInstance().restoreGameService(getApplicationContext());

        mSyncLayout = findViewById(R.id.sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateScheduledGamesList);

        final ListView scheduledGamesList = findViewById(R.id.scheduled_games_list);
        mScheduledGamesListAdapter = new ScheduledGamesListAdapter(getLayoutInflater());
        scheduledGamesList.setAdapter(mScheduledGamesListAdapter);

        scheduledGamesList.setOnItemClickListener((adapterView, view, i, l) -> {
            GameDescription gameDescription = mScheduledGamesListAdapter.getItem(i);
            if (!GameType.TIME.equals(gameDescription.getGameType())) {
                ServicesProvider.getInstance().getRecordedGamesService(getApplicationContext()).getUserGame(gameDescription.getGameDate(), ScheduledGamesListActivity.this);
            }
        });

        updateScheduledGamesList();

        mIsFabOpen = false;
        mScheduleIndoorGameButton = findViewById(R.id.schedule_indoor_game_button);
        mScheduleIndoor4x4GameButton = findViewById(R.id.schedule_indoor_4x4_game_button);
        mScheduleBeachGameButton = findViewById(R.id.schedule_beach_game_button);
        mScheduleIndoorGameButton.hide();
        mScheduleIndoor4x4GameButton.hide();
        mScheduleBeachGameButton.hide();
        FloatingActionButton scheduleGameButton = findViewById(R.id.schedule_game_button);
        scheduleGameButton.setOnClickListener(button -> {
            if(mIsFabOpen){
                closeFABMenu();
            }else{
                showFABMenu();
            }
        });

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

        searchGamesView.setOnQueryTextFocusChangeListener((view, hasFocus) -> {});

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
        if (PrefUtils.isSyncOn(this)) {
            mSyncLayout.setRefreshing(true);
            ServicesProvider.getInstance().getRecordedGamesService(getApplicationContext()).getUserScheduledGames(this);
        }
    }

    @Override
    public void onRecordedGameReceivedFromCode(RecordedGameService recordedGameService) {}

    @Override
    public void onUserGameReceived(RecordedGameService recordedGameService) {
        if (recordedGameService != null) {
            final GameService gameService = GameFactory.createGame(recordedGameService);
            Log.i(Tags.SCHEDULE_UI, "Start game activity after receiving game");

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
        UiUtils.makeText(this, getResources().getString(R.string.download_error_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInternalError() {
        mSyncLayout.setRefreshing(false);
        UiUtils.makeText(this, getResources().getString(R.string.download_error_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError() {
        mSyncLayout.setRefreshing(false);
        UiUtils.makeText(this, getResources().getString(R.string.download_error_message), Toast.LENGTH_LONG).show();
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
                    Log.i(Tags.SCHEDULE_UI, "Start scheduled game immediately");
                    gameService.startMatch();
                    final Intent gameIntent = new Intent(ScheduledGamesListActivity.this, GameActivity.class);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(gameIntent);
                }

                @Override
                public void onPositiveButtonClicked() {
                    Log.i(Tags.SCHEDULE_UI, "Edit scheduled game before starting");
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

    public void scheduleIndoorGame(View view) {
        GameDescription gameDescription = new GameDescription(GameType.INDOOR, PrefUtils.getAuthentication(this).getUserId(), PrefUtils.getPrefRefereeName(this));
        Log.i(Tags.SCHEDULE_UI, "Start activity to schedule new indoor game");

        final Intent intent = new Intent(this, ScheduledGameActivity.class);
        intent.putExtra("game", JsonIOUtils.GSON.toJson(gameDescription, JsonIOUtils.GAME_DESCRIPTION_TYPE));
        startActivity(intent);
    }

    public void scheduleIndoor4x4Game(View view) {
        GameDescription gameDescription = new GameDescription(GameType.INDOOR_4X4, PrefUtils.getAuthentication(this).getUserId(), PrefUtils.getPrefRefereeName(this));
        Log.i(Tags.SCHEDULE_UI, "Start activity to schedule new indoor 4x4 game");

        final Intent intent = new Intent(this, ScheduledGameActivity.class);
        intent.putExtra("game", JsonIOUtils.GSON.toJson(gameDescription, JsonIOUtils.GAME_DESCRIPTION_TYPE));
        startActivity(intent);
    }

    public void scheduleBeachGame(View view) {
        GameDescription gameDescription = new GameDescription(GameType.BEACH, PrefUtils.getAuthentication(this).getUserId(), PrefUtils.getPrefRefereeName(this));
        Log.i(Tags.SCHEDULE_UI, "Start activity to schedule new beach game");

        final Intent intent = new Intent(this, ScheduledGameActivity.class);
        intent.putExtra("game", JsonIOUtils.GSON.toJson(gameDescription, JsonIOUtils.GAME_DESCRIPTION_TYPE));
        startActivity(intent);
    }

    private void showFABMenu(){
        mIsFabOpen = true;
        mScheduleIndoorGameButton.show();
        mScheduleIndoor4x4GameButton.show();
        mScheduleBeachGameButton.show();
        mScheduleIndoorGameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_third));
        mScheduleIndoor4x4GameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_second));
        mScheduleBeachGameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_first));
    }

    private void closeFABMenu(){
        mIsFabOpen = false;
        mScheduleIndoorGameButton.animate().translationY(0);
        mScheduleIndoor4x4GameButton.animate().translationY(0);
        mScheduleBeachGameButton.animate().translationY(0);
        mScheduleIndoorGameButton.hide();
        mScheduleIndoor4x4GameButton.hide();
        mScheduleBeachGameButton.hide();
    }

    @Override
    public void onBackPressed() {
        if(mIsFabOpen){
            closeFABMenu();
        } else {
            super.onBackPressed();
        }
    }
}
