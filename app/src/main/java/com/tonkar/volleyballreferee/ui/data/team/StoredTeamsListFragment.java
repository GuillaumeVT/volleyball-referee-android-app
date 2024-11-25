package com.tonkar.volleyballreferee.ui.data.team;

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

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

public class StoredTeamsListFragment extends Fragment implements DataSynchronizationListener {

    private StoredTeamsService           mStoredTeamsService;
    private StoredTeamsListAdapter       mStoredTeamsListAdapter;
    private SwipeRefreshLayout           mSyncLayout;
    private View                         mFabMenu;
    private boolean                      mIsFabOpen;
    private ExtendedFloatingActionButton mAddTeamButton;
    private ExtendedFloatingActionButton mAdd6x6TeamButton;
    private ExtendedFloatingActionButton mAdd4x4TeamButton;
    private ExtendedFloatingActionButton mAddBeachTeamButton;
    private ExtendedFloatingActionButton mAddSnowTeamButton;
    private MenuItem                     mDeleteSelectedTeamsItem;
    private OnBackPressedCallback        mBackPressedCallback;

    public StoredTeamsListFragment() {
    }

    public static StoredTeamsListFragment newInstance() {
        return new StoredTeamsListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mStoredTeamsService = new StoredTeamsManager(requireContext());

        List<TeamSummaryDto> teams = mStoredTeamsService.listTeams();

        Log.i(Tags.STORED_TEAMS, "Create teams list fragment");

        View fragmentView = inflater.inflate(R.layout.fragment_stored_teams_list, container, false);

        mSyncLayout = fragmentView.findViewById(R.id.stored_teams_sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateStoredTeamsList);

        mFabMenu = fragmentView.findViewById(R.id.stored_teams_fab_menu);
        mFabMenu.setVisibility(View.INVISIBLE);

        final ListView storedTeamsList = fragmentView.findViewById(R.id.stored_teams_list);
        mStoredTeamsListAdapter = new StoredTeamsListAdapter(requireContext(), getLayoutInflater(), teams);
        storedTeamsList.setAdapter(mStoredTeamsListAdapter);

        storedTeamsList.setOnItemClickListener((adapterView, itemView, position, l) -> {
            TeamSummaryDto teamDescription = mStoredTeamsListAdapter.getItem(position);

            if (mStoredTeamsListAdapter.hasSelectedItems()) {
                onTeamSelected(teamDescription);
            } else {
                computeOnBackPressedCallbackState();
                TeamDto team = mStoredTeamsService.getTeam(teamDescription.getId());

                if (team != null) {
                    Log.i(Tags.STORED_TEAMS, String.format("Start activity to view stored team %s", team.getName()));

                    final Intent intent = new Intent(requireContext(), StoredTeamViewActivity.class);
                    intent.putExtra("team", JsonConverters.GSON.toJson(team, TeamDto.class));
                    startActivity(intent, ActivityOptionsCompat
                            .makeSceneTransitionAnimation(requireActivity(), itemView, "listItemToDetails")
                            .toBundle());
                }
            }
        });

        storedTeamsList.setOnItemLongClickListener((adapterView, itemView, position, id) -> {
            TeamSummaryDto teamDescription = mStoredTeamsListAdapter.getItem(position);
            onTeamSelected(teamDescription);
            return true;
        });

        mIsFabOpen = false;
        mAdd6x6TeamButton = fragmentView.findViewById(R.id.add_6x6_team_button);
        mAdd6x6TeamButton.setOnClickListener(this::addIndoorTeam);

        mAdd4x4TeamButton = fragmentView.findViewById(R.id.add_4x4_team_button);
        mAdd4x4TeamButton.setOnClickListener(this::addIndoor4x4Team);

        mAddBeachTeamButton = fragmentView.findViewById(R.id.add_beach_team_button);
        mAddBeachTeamButton.setOnClickListener(this::addBeachTeam);

        mAddSnowTeamButton = fragmentView.findViewById(R.id.add_snow_team_button);
        mAddSnowTeamButton.setOnClickListener(this::addSnowTeam);

        mAddTeamButton = fragmentView.findViewById(R.id.add_team_button);
        mAddTeamButton.setOnClickListener(view -> {
            if (mIsFabOpen) {
                closeFABMenu();
            } else {
                showFABMenu();
            }
        });

        updateStoredTeamsList();

        UiUtils.addExtendShrinkListener(storedTeamsList, mAddTeamButton);

        mBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (mIsFabOpen) {
                    closeFABMenu();
                } else if (mStoredTeamsListAdapter.hasSelectedItems()) {
                    mStoredTeamsListAdapter.clearSelectedItems();
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
        toolbar.inflateMenu(R.menu.menu_stored_teams);

        Menu menu = toolbar.getMenu();

        mDeleteSelectedTeamsItem = menu.findItem(R.id.action_delete_teams);
        mDeleteSelectedTeamsItem.setVisible(false);

        MenuItem searchTeamsItem = menu.findItem(R.id.action_search_teams);
        SearchView searchTeamsView = (SearchView) searchTeamsItem.getActionView();

        searchTeamsView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {});

        searchTeamsView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                mStoredTeamsListAdapter.getFilter().filter(searchQuery.trim());
                return true;
            }
        });

        MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(PrefUtils.canSync(requireContext()));

        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_search_teams) {
                return true;
            } else if (itemId == R.id.action_sync) {
                updateStoredTeamsList();
                return true;
            } else if (itemId == R.id.action_delete_teams) {
                deleteSelectedTeams();
                return true;
            } else {
                return false;
            }
        });
    }

    private void addIndoorTeam(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new indoor team");
        addTeam(GameType.INDOOR, view);
    }

    private void addIndoor4x4Team(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new indoor 4x4 team");
        addTeam(GameType.INDOOR_4X4, view);
    }

    private void addBeachTeam(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new beach team");
        addTeam(GameType.BEACH, view);
    }

    private void addSnowTeam(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new snow team");
        addTeam(GameType.SNOW, view);
    }

    private void addTeam(GameType gameType, View view) {
        Log.i(Tags.STORED_TEAMS, "Start activity to create new team");
        IBaseTeam teamService = mStoredTeamsService.createTeam(gameType);

        final Intent intent = new Intent(requireContext(), StoredTeamActivity.class);
        intent.putExtra("team", JsonConverters.GSON.toJson(mStoredTeamsService.copyTeam(teamService), TeamDto.class));
        intent.putExtra("kind", gameType.toString());
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, "gameKindToToolbar").toBundle());
    }

    private void computeOnBackPressedCallbackState() {
        mBackPressedCallback.setEnabled(mStoredTeamsListAdapter.hasSelectedItems() || mIsFabOpen);
    }

    private void onTeamSelected(TeamSummaryDto teamDescription) {
        mStoredTeamsListAdapter.toggleItemSelection(teamDescription.getId());
        mDeleteSelectedTeamsItem.setVisible(mStoredTeamsListAdapter.hasSelectedItems());
        computeOnBackPressedCallbackState();
    }

    private void deleteSelectedTeams() {
        Log.i(Tags.STORED_TEAMS, "Delete selected teams");
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_selected_teams)).setMessage(getString(R.string.delete_selected_teams_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredTeamsService.deleteTeams(mStoredTeamsListAdapter.getSelectedItems(), this);
            UiUtils.makeText(requireContext(), getString(R.string.deleted_teams), Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void showFABMenu() {
        mIsFabOpen = true;
        UiUtils.colorCloseIconButton(requireContext(), mAddTeamButton);
        mFabMenu.setVisibility(View.VISIBLE);
        mAdd6x6TeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_first));
        mAdd4x4TeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_third));
        mAddBeachTeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_second));
        mAddSnowTeamButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_fourth));
        computeOnBackPressedCallbackState();
    }

    private void closeFABMenu() {
        mIsFabOpen = false;
        UiUtils.colorPlusIconButton(requireContext(), mAddTeamButton);
        mAdd6x6TeamButton.animate().translationY(0);
        mAdd4x4TeamButton.animate().translationY(0);
        mAddBeachTeamButton.animate().translationY(0);
        mAddSnowTeamButton.animate().translationY(0).withEndAction(() -> mFabMenu.setVisibility(View.INVISIBLE));
        computeOnBackPressedCallbackState();
    }

    private void updateStoredTeamsList() {
        if (PrefUtils.canSync(requireContext())) {
            mSyncLayout.setRefreshing(true);
            mStoredTeamsService.syncTeams(this);
        }
    }

    @Override
    public void onSynchronizationSucceeded() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                mStoredTeamsListAdapter.updateStoredTeamsList(mStoredTeamsService.listTeams());
                if (mDeleteSelectedTeamsItem != null) {
                    mDeleteSelectedTeamsItem.setVisible(mStoredTeamsListAdapter.hasSelectedItems());
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