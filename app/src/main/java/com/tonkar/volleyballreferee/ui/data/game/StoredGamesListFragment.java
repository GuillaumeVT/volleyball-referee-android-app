package com.tonkar.volleyballreferee.ui.data.game;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.api.model.GameSummaryDto;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

public class StoredGamesListFragment extends Fragment implements DataSynchronizationListener {

    private StoredGamesService     mStoredGamesService;
    private StoredGamesListAdapter mStoredGamesListAdapter;
    private SwipeRefreshLayout     mSyncLayout;
    private MenuItem               mDeleteSelectedGamesItem;
    private OnBackPressedCallback  mBackPressedCallback;

    public StoredGamesListFragment() {}

    public static StoredGamesListFragment newInstance() {
        return new StoredGamesListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.STORED_GAMES, "Create stored games list fragment");

        mStoredGamesService = new StoredGamesManager(requireContext());
        List<GameSummaryDto> games = mStoredGamesService.listGames();

        View fragmentView = inflater.inflate(R.layout.fragment_stored_games_list, container, false);

        mSyncLayout = fragmentView.findViewById(R.id.stored_games_sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateStoredGamesList);

        final ListView storedGamesList = fragmentView.findViewById(R.id.stored_games_list);
        mStoredGamesListAdapter = new StoredGamesListAdapter(requireContext(), getLayoutInflater(), games);
        storedGamesList.setAdapter(mStoredGamesListAdapter);

        storedGamesList.setOnItemClickListener((adapterView, itemView, position, l) -> {
            GameSummaryDto game = mStoredGamesListAdapter.getItem(position);

            if (game != null) {
                if (mStoredGamesListAdapter.hasSelectedItems()) {
                    onGameSelected(game);
                } else {
                    computeOnBackPressedCallbackState();
                    Log.i(Tags.STORED_GAMES, String.format("Start activity to display stored game %s", game.getId()));

                    final Intent intent;

                    if (UsageType.POINTS_SCOREBOARD.equals(game.getUsage())) {
                        intent = new Intent(requireContext(), StoredBasicGameActivity.class);
                    } else {
                        intent = new Intent(requireContext(), StoredAdvancedGameActivity.class);
                    }

                    intent.putExtra("game", game.getId());
                    startActivity(intent, ActivityOptionsCompat
                            .makeSceneTransitionAnimation(requireActivity(), itemView, "listItemToDetails")
                            .toBundle());
                }
            }
        });

        storedGamesList.setOnItemLongClickListener((adapterView, itemView, position, id) -> {
            GameSummaryDto game = mStoredGamesListAdapter.getItem(position);
            if (game != null) {
                onGameSelected(game);
            }
            return true;
        });

        updateStoredGamesList();

        mBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (mStoredGamesListAdapter.hasSelectedItems()) {
                    mStoredGamesListAdapter.clearSelectedItems();
                }
                computeOnBackPressedCallbackState();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), mBackPressedCallback);

        initMenu(fragmentView);

        return fragmentView;
    }

    @Override
    public void onStop() {
        super.onStop();
        mBackPressedCallback.setEnabled(false);
    }

    private void initMenu(View fragmentView) {
        Toolbar toolbar = fragmentView.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_stored_games);

        Menu menu = toolbar.getMenu();

        mDeleteSelectedGamesItem = menu.findItem(R.id.action_delete_games);
        mDeleteSelectedGamesItem.setVisible(false);

        MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(PrefUtils.canSync(requireContext()));

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
                mStoredGamesListAdapter.getFilter().filter(searchQuery.trim());
                return true;
            }
        });

        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_search_games) {
                return true;
            } else if (itemId == R.id.action_sync) {
                updateStoredGamesList();
                return true;
            } else if (itemId == R.id.action_delete_games) {
                deleteSelectedGames();
                return true;
            } else {
                return false;
            }
        });
    }

    private void computeOnBackPressedCallbackState() {
        mBackPressedCallback.setEnabled(mStoredGamesListAdapter.hasSelectedItems());
    }

    private void onGameSelected(GameSummaryDto game) {
        mStoredGamesListAdapter.toggleItemSelection(game.getId());
        mDeleteSelectedGamesItem.setVisible(mStoredGamesListAdapter.hasSelectedItems());
        computeOnBackPressedCallbackState();
    }

    private void deleteSelectedGames() {
        Log.i(Tags.STORED_GAMES, "Delete selected games");
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_selected_games)).setMessage(getString(R.string.delete_selected_games_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredGamesService.deleteGames(mStoredGamesListAdapter.getSelectedItems(), this);
            UiUtils.makeText(requireContext(), getString(R.string.deleted_selected_games), Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void updateStoredGamesList() {
        if (PrefUtils.canSync(requireContext())) {
            mSyncLayout.setRefreshing(true);
            mStoredGamesService.syncGames(this);
        }
    }

    @Override
    public void onSynchronizationSucceeded() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                mStoredGamesListAdapter.updateStoredGamesList(mStoredGamesService.listGames());
                if (mDeleteSelectedGamesItem != null) {
                    mDeleteSelectedGamesItem.setVisible(mStoredGamesListAdapter.hasSelectedItems());
                }
                mSyncLayout.setRefreshing(false);
            });
        }
    }

    @Override
    public void onSynchronizationFailed() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                UiUtils.makeErrorText(requireContext(), getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
                mSyncLayout.setRefreshing(false);
            });
        }
    }
}