package com.tonkar.volleyballreferee.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.*;

public class ScheduledGamesListFragment extends Fragment implements AsyncGameRequestListener {

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
    private OnBackPressedCallback        mBackPressedCallback;

    public ScheduledGamesListFragment() {}

    public static ScheduledGamesListFragment newInstance() {
        return new ScheduledGamesListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mStoredGamesService = new StoredGamesManager(requireContext());

        Log.i(Tags.SCHEDULE_UI, "Create scheduled games list fragment");

        View fragmentView = inflater.inflate(R.layout.fragment_scheduled_games_list, container, false);

        mSyncLayout = fragmentView.findViewById(R.id.scheduled_games_sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateScheduledGamesList);

        mFabMenu = fragmentView.findViewById(R.id.scheduled_games_fab_menu);
        mFabMenu.setVisibility(View.INVISIBLE);

        final ListView scheduledGamesList = fragmentView.findViewById(R.id.scheduled_games_list);
        mScheduledGamesListAdapter = new ScheduledGamesListAdapter(getLayoutInflater());
        scheduledGamesList.setAdapter(mScheduledGamesListAdapter);

        scheduledGamesList.setOnItemClickListener((adapterView, itemView, i, l) -> {
            GameSummaryDto gameDescription = mScheduledGamesListAdapter.getItem(i);
            switch (gameDescription.getStatus()) {
                case SCHEDULED, LIVE -> {
                    ScheduleGameListActionMenu scheduleGameListActionMenu = ScheduleGameListActionMenu.newInstance(gameDescription);
                    scheduleGameListActionMenu.show(getChildFragmentManager(), "schedule_game_list_action_menu");
                }
                default -> {
                }
            }
        });

        updateScheduledGamesList();

        mIsFabOpen = false;
        mScheduleIndoorGameButton = fragmentView.findViewById(R.id.schedule_indoor_game_button);
        mScheduleIndoorGameButton.setOnClickListener(this::scheduleIndoorGame);

        mScheduleIndoor4x4GameButton = fragmentView.findViewById(R.id.schedule_indoor_4x4_game_button);
        mScheduleIndoor4x4GameButton.setOnClickListener(this::scheduleIndoor4x4Game);

        mScheduleBeachGameButton = fragmentView.findViewById(R.id.schedule_beach_game_button);
        mScheduleBeachGameButton.setOnClickListener(this::scheduleBeachGame);

        mScheduleSnowGameButton = fragmentView.findViewById(R.id.schedule_snow_game_button);
        mScheduleSnowGameButton.setOnClickListener(this::scheduleSnowGame);

        mScheduleGameButton = fragmentView.findViewById(R.id.schedule_game_button);
        mScheduleGameButton.setOnClickListener(button -> {
            if (mIsFabOpen) {
                closeFABMenu();
            } else {
                showFABMenu();
            }
        });

        UiUtils.addExtendShrinkListener(scheduledGamesList, mScheduleGameButton);

        mBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (mIsFabOpen) {
                    closeFABMenu();
                }
                computeOnBackPressedCallbackState();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), mBackPressedCallback);

        initMenu(fragmentView);

        closeFABMenu();

        return fragmentView;
    }

    @Override
    public void onStop() {
        super.onStop();
        closeFABMenu();
        mBackPressedCallback.setEnabled(false);
    }

    private void initMenu(View fragmentView) {
        Toolbar toolbar = fragmentView.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_scheduled_games);

        Menu menu = toolbar.getMenu();
        MenuItem searchGamesItem = menu.findItem(R.id.action_search_games);
        SearchView searchGamesView = (SearchView) searchGamesItem.getActionView();

        searchGamesView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {});

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

        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_search_games) {
                return true;
            } else if (itemId == R.id.action_sync) {
                updateScheduledGamesList();
                return true;
            } else {
                return false;
            }
        });
    }

    private void computeOnBackPressedCallbackState() {
        mBackPressedCallback.setEnabled(mIsFabOpen);
    }

    private void updateScheduledGamesList() {
        if (PrefUtils.canSync(requireContext())) {
            mSyncLayout.setRefreshing(true);
            mStoredGamesService.downloadAvailableGames(this);
            StoredLeaguesService storedLeagues = new StoredLeaguesManager(requireContext());
            storedLeagues.syncLeagues();
        }
    }

    @Override
    public void onGameReceived(IStoredGame storedGame) {}

    @Override
    public void onAvailableGamesReceived(List<GameSummaryDto> gameDescriptionList) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                mScheduledGamesListAdapter.updateGameDescriptionList(gameDescriptionList);
                mSyncLayout.setRefreshing(false);
            });
        }
    }

    @Override
    public void onError(int httpCode) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                mSyncLayout.setRefreshing(false);
                UiUtils.makeErrorText(requireContext(), getString(R.string.download_match_error), Toast.LENGTH_LONG).show();
            });
        }
    }

    private void scheduleIndoorGame(View view) {
        scheduleGame(GameType.INDOOR, view);
    }

    private void scheduleIndoor4x4Game(View view) {
        scheduleGame(GameType.INDOOR_4X4, view);
    }

    private void scheduleBeachGame(View view) {
        scheduleGame(GameType.BEACH, view);
    }

    private void scheduleSnowGame(View view) {
        scheduleGame(GameType.SNOW, view);
    }

    private void scheduleGame(GameType kind, View view) {
        long utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime();
        UserSummaryDto user = PrefUtils.getUser(requireContext());

        GameSummaryDto gameDescription = new GameSummaryDto();
        gameDescription.setId(UUID.randomUUID().toString());
        gameDescription.setCreatedBy(user.getId());
        gameDescription.setCreatedAt(utcTime);
        gameDescription.setUpdatedAt(utcTime);
        gameDescription.setScheduledAt(System.currentTimeMillis());
        gameDescription.setRefereedBy(user.getId());
        gameDescription.setRefereeName(user.getPseudo());
        gameDescription.setKind(kind);

        Log.i(Tags.SCHEDULE_UI, "Start activity to schedule game");
        final Intent intent = new Intent(requireContext(), ScheduledGameActivity.class);
        intent.putExtra("game", JsonConverters.GSON.toJson(gameDescription, GameSummaryDto.class));
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, "gameKindToToolbar").toBundle());
    }

    private void showFABMenu() {
        mIsFabOpen = true;
        UiUtils.colorCloseIconButton(requireContext(), mScheduleGameButton);
        mFabMenu.setVisibility(View.VISIBLE);
        mScheduleIndoorGameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_first));
        mScheduleIndoor4x4GameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_third));
        mScheduleBeachGameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_second));
        mScheduleSnowGameButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_fourth));
        computeOnBackPressedCallbackState();
    }

    private void closeFABMenu() {
        mIsFabOpen = false;
        UiUtils.colorPlusIconButton(requireContext(), mScheduleGameButton);
        mScheduleIndoorGameButton.animate().translationY(0);
        mScheduleIndoor4x4GameButton.animate().translationY(0);
        mScheduleBeachGameButton.animate().translationY(0);
        mScheduleSnowGameButton.animate().translationY(0).withEndAction(() -> mFabMenu.setVisibility(View.INVISIBLE));
        computeOnBackPressedCallbackState();
    }
}