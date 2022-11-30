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

import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.ApiGameSummary;
import com.tonkar.volleyballreferee.engine.api.model.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.service.AsyncGameRequestListener;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.service.StoredGamesManager;
import com.tonkar.volleyballreferee.engine.service.StoredGamesService;
import com.tonkar.volleyballreferee.engine.service.StoredLeaguesManager;
import com.tonkar.volleyballreferee.engine.service.StoredLeaguesService;
import com.tonkar.volleyballreferee.ui.NavigationActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class ScheduledGamesListActivity extends NavigationActivity implements AsyncGameRequestListener {

    private StoredGamesService           mStoredGamesService;
    private SwipeRefreshLayout           mSyncLayout;
    private View                         mFabMenu;
    private ScheduledGamesListAdapter    mScheduledGamesListAdapter;
    private boolean                      mIsFabOpen;
    private ExtendedFloatingActionButton mScheduleGameButton;
    private ExtendedFloatingActionButton mScheduleIndoorGameButton;
    private ExtendedFloatingActionButton mScheduleIndoor4x4GameButton;
    private ExtendedFloatingActionButton mScheduleBeachGameButton;
    private ExtendedFloatingActionButton mScheduleSnowGameButton;

    @Override
    protected String getToolbarTitle() {
        return "";
    }

    @Override
    protected int getCheckedItem() {
        return R.id.action_available_games;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredGamesService = new StoredGamesManager(this);

        super.onCreate(savedInstanceState);

        Log.i(Tags.SCHEDULE_UI, "Create scheduled games list activity");
        setContentView(R.layout.activity_scheduled_games_list);

        initNavigationMenu();

        mSyncLayout = findViewById(R.id.scheduled_games_sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateScheduledGamesList);

        mFabMenu = findViewById(R.id.scheduled_games_fab_menu);
        mFabMenu.setVisibility(View.INVISIBLE);

        final ListView scheduledGamesList = findViewById(R.id.scheduled_games_list);
        mScheduledGamesListAdapter = new ScheduledGamesListAdapter(getLayoutInflater());
        scheduledGamesList.setAdapter(mScheduledGamesListAdapter);

        scheduledGamesList.setOnItemClickListener((adapterView, view, i, l) -> {
            ApiGameSummary gameDescription = mScheduledGamesListAdapter.getItem(i);
            if (!GameType.TIME.equals(gameDescription.getKind())) {
                switch (gameDescription.getStatus()) {
                    case SCHEDULED:
                    case LIVE:
                        ScheduleGameListActionMenu scheduleGameListActionMenu = ScheduleGameListActionMenu.newInstance(gameDescription);
                        scheduleGameListActionMenu.show(getSupportFragmentManager(), "schedule_game_list_action_menu");
                        break;
                    default:
                        break;
                }
            }
        });

        updateScheduledGamesList();

        mIsFabOpen = false;
        mScheduleIndoorGameButton = findViewById(R.id.schedule_indoor_game_button);
        mScheduleIndoor4x4GameButton = findViewById(R.id.schedule_indoor_4x4_game_button);
        mScheduleBeachGameButton = findViewById(R.id.schedule_beach_game_button);
        mScheduleSnowGameButton = findViewById(R.id.schedule_snow_game_button);
        mScheduleGameButton = findViewById(R.id.schedule_game_button);
        mScheduleGameButton.setOnClickListener(button -> {
            if(mIsFabOpen){
                closeFABMenu();
            }else{
                showFABMenu();
            }
        });
        closeFABMenu();

        UiUtils.addExtendShrinkListener(scheduledGamesList, mScheduleGameButton);
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
        int itemId = item.getItemId();
        if (itemId == R.id.action_search_games) {
            return true;
        } else if (itemId == R.id.action_sync) {
            updateScheduledGamesList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateScheduledGamesList() {
        if (PrefUtils.canSync(this)) {
            mSyncLayout.setRefreshing(true);
            mStoredGamesService.downloadAvailableGames(this);
            StoredLeaguesService storedLeagues = new StoredLeaguesManager(this);
            storedLeagues.syncLeagues();
        }
    }

    @Override
    public void onGameReceived(IStoredGame storedGame) {}

    @Override
    public void onAvailableGamesReceived(List<ApiGameSummary> gameDescriptionList) {
        runOnUiThread(() -> {
            mScheduledGamesListAdapter.updateGameDescriptionList(gameDescriptionList);
            mSyncLayout.setRefreshing(false);
        });
    }

    @Override
    public void onError(int httpCode) {
        runOnUiThread(() -> {
            mSyncLayout.setRefreshing(false);
            UiUtils.makeErrorText(this, getString(R.string.download_match_error), Toast.LENGTH_LONG).show();
        });
    }

    public void scheduleIndoorGame(View view) {
        scheduleGame(GameType.INDOOR, view);
    }

    public void scheduleIndoor4x4Game(View view) {
        scheduleGame(GameType.INDOOR_4X4, view);
    }

    public void scheduleBeachGame(View view) {
        scheduleGame(GameType.BEACH, view);
    }

    public void scheduleSnowGame(View view) {
        scheduleGame(GameType.SNOW, view);
    }

    private void scheduleGame(GameType kind, View view) {
        long utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
        ApiUserSummary user = PrefUtils.getUser(this);

        ApiGameSummary gameDescription = new ApiGameSummary();
        gameDescription.setId(UUID.randomUUID().toString());
        gameDescription.setCreatedBy(user.getId());
        gameDescription.setCreatedAt(utcTime);
        gameDescription.setUpdatedAt(utcTime);
        gameDescription.setScheduledAt(System.currentTimeMillis());
        gameDescription.setRefereedBy(user.getId());
        gameDescription.setRefereeName(user.getPseudo());
        gameDescription.setKind(kind);

        Log.i(Tags.SCHEDULE_UI, "Start activity to schedule game");
        final Intent intent = new Intent(this, ScheduledGameActivity.class);
        intent.putExtra("game", JsonConverters.GSON.toJson(gameDescription, ApiGameSummary.class));
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "gameKindToToolbar").toBundle());
    }

    private void showFABMenu(){
        mIsFabOpen = true;
        UiUtils.colorCloseIconButton(this, mScheduleGameButton);
        mFabMenu.setVisibility(View.VISIBLE);
        mScheduleIndoorGameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_first));
        mScheduleIndoor4x4GameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_third));
        mScheduleBeachGameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_second));
        mScheduleSnowGameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_fourth));
    }

    private void closeFABMenu(){
        mIsFabOpen = false;
        UiUtils.colorPlusIconButton(this, mScheduleGameButton);
        mScheduleIndoorGameButton.animate().translationY(0);
        mScheduleIndoor4x4GameButton.animate().translationY(0);
        mScheduleBeachGameButton.animate().translationY(0);
        mScheduleSnowGameButton.animate().translationY(0).withEndAction(() -> mFabMenu.setVisibility(View.INVISIBLE));
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
