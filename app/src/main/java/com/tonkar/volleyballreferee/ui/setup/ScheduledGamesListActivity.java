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
import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.api.ApiGameDescription;
import com.tonkar.volleyballreferee.business.data.JsonIOUtils;
import com.tonkar.volleyballreferee.business.data.StoredGames;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.AsyncGameRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.interfaces.data.StoredGamesService;
import com.tonkar.volleyballreferee.ui.NavigationActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ScheduledGamesListActivity extends NavigationActivity implements AsyncGameRequestListener {

    private StoredGamesService        mStoredGamesService;
    private SwipeRefreshLayout        mSyncLayout;
    private ScheduledGamesListAdapter mScheduledGamesListAdapter;
    private boolean                   mIsFabOpen;
    private FloatingActionButton      mScheduleIndoorGameButton;
    private FloatingActionButton      mScheduleIndoor4x4GameButton;
    private FloatingActionButton      mScheduleBeachGameButton;

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
        mStoredGamesService = new StoredGames(this);

        super.onCreate(savedInstanceState);

        Log.i(Tags.SCHEDULE_UI, "Create scheduled games list activity");
        setContentView(R.layout.activity_scheduled_games_list);

        initNavigationMenu();

        mSyncLayout = findViewById(R.id.sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateScheduledGamesList);

        final ListView scheduledGamesList = findViewById(R.id.scheduled_games_list);
        mScheduledGamesListAdapter = new ScheduledGamesListAdapter(getLayoutInflater());
        scheduledGamesList.setAdapter(mScheduledGamesListAdapter);

        scheduledGamesList.setOnItemClickListener((adapterView, view, i, l) -> {
            ApiGameDescription gameDescription = mScheduledGamesListAdapter.getItem(i);
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
        if (PrefUtils.canSync(this)) {
            mSyncLayout.setRefreshing(true);
            mStoredGamesService.downloadAvailableGames(this);
        }
    }

    @Override
    public void onGameReceived(StoredGameService storedGameService) {}

    @Override
    public void onAvailableGamesReceived(List<ApiGameDescription> gameDescriptionList) {
        mScheduledGamesListAdapter.updateGameDescriptionList(gameDescriptionList);
        mSyncLayout.setRefreshing(false);
    }

    @Override
    public void onError(int httpCode) {
        mSyncLayout.setRefreshing(false);
        UiUtils.makeErrorText(this, getResources().getString(R.string.download_match_error), Toast.LENGTH_LONG).show();
    }

    public void scheduleIndoorGame(View view) {
        scheduleGame(GameType.INDOOR);
    }

    public void scheduleIndoor4x4Game(View view) {
        scheduleGame(GameType.INDOOR_4X4);
    }

    public void scheduleBeachGame(View view) {
        scheduleGame(GameType.BEACH);
    }

    private void scheduleGame(GameType kind) {
        long utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
        Authentication authentication = PrefUtils.getAuthentication(this);

        ApiGameDescription gameDescription = new ApiGameDescription();
        gameDescription.setId(UUID.randomUUID().toString());
        gameDescription.setCreatedBy(authentication.getUserId());
        gameDescription.setCreatedAt(utcTime);
        gameDescription.setUpdatedAt(utcTime);
        gameDescription.setScheduledAt(System.currentTimeMillis());
        gameDescription.setRefereedBy(authentication.getUserId());
        gameDescription.setRefereeName(authentication.getUserPseudo());
        gameDescription.setKind(kind);

        Log.i(Tags.SCHEDULE_UI, "Start activity to schedule game");
        final Intent intent = new Intent(this, ScheduledGameActivity.class);
        intent.putExtra("game", JsonIOUtils.GSON.toJson(gameDescription, JsonIOUtils.GAME_DESCRIPTION_TYPE));
        intent.putExtra("create", true);
        startActivity(intent);
        UiUtils.animateCreate(this);
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
