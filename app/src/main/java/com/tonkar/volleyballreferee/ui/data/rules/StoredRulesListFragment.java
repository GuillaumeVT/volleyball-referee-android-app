package com.tonkar.volleyballreferee.ui.data.rules;

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
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

public class StoredRulesListFragment extends Fragment implements DataSynchronizationListener {

    private StoredRulesService           mStoredRulesService;
    private StoredRulesListAdapter       mStoredRulesListAdapter;
    private SwipeRefreshLayout           mSyncLayout;
    private View                         mFabMenu;
    private boolean                      mIsFabOpen;
    private ExtendedFloatingActionButton mAddRulesButton;
    private ExtendedFloatingActionButton mAddIndoorRulesButton;
    private ExtendedFloatingActionButton mAddIndoor4x4RulesButton;
    private ExtendedFloatingActionButton mAddBeachRulesButton;
    private ExtendedFloatingActionButton mAddSnowRulesButton;
    private MenuItem                     mDeleteSelectedRulesItem;
    private OnBackPressedCallback        mBackPressedCallback;

    public StoredRulesListFragment() {}

    public static StoredRulesListFragment newInstance() {
        return new StoredRulesListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mStoredRulesService = new StoredRulesManager(requireContext());

        List<RulesSummaryDto> storedRules = mStoredRulesService.listRules();

        Log.i(Tags.STORED_RULES, "Create rules list fragment");

        View fragmentView = inflater.inflate(R.layout.fragment_stored_rules_list, container, false);

        mSyncLayout = fragmentView.findViewById(R.id.stored_rules_sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateStoredRulesList);

        mFabMenu = fragmentView.findViewById(R.id.stored_rules_fab_menu);
        mFabMenu.setVisibility(View.INVISIBLE);

        final ListView storedRulesList = fragmentView.findViewById(R.id.stored_rules_list);
        mStoredRulesListAdapter = new StoredRulesListAdapter(requireContext(), getLayoutInflater(), storedRules);
        storedRulesList.setAdapter(mStoredRulesListAdapter);

        storedRulesList.setOnItemClickListener((adapterView, itemView, position, l) -> {
            RulesSummaryDto rulesDescription = mStoredRulesListAdapter.getItem(position);

            if (mStoredRulesListAdapter.hasSelectedItems()) {
                onRulesSelected(rulesDescription);
            } else {
                computeOnBackPressedCallbackState();
                RulesDto rules = mStoredRulesService.getRules(rulesDescription.getId());

                if (rules != null) {
                    Log.i(Tags.STORED_RULES, String.format("Start activity to view stored rules %s", rules.getName()));

                    final Intent intent = new Intent(requireContext(), StoredRulesViewActivity.class);
                    intent.putExtra("rules", JsonConverters.GSON.toJson(rules, RulesDto.class));
                    startActivity(intent, ActivityOptionsCompat
                            .makeSceneTransitionAnimation(requireActivity(), itemView, "listItemToDetails")
                            .toBundle());
                }
            }
        });

        storedRulesList.setOnItemLongClickListener((adapterView, itemView, position, id) -> {
            RulesSummaryDto rulesDescription = mStoredRulesListAdapter.getItem(position);
            onRulesSelected(rulesDescription);
            return true;
        });

        mIsFabOpen = false;
        mAddIndoorRulesButton = fragmentView.findViewById(R.id.add_indoor_rules_button);
        mAddIndoorRulesButton.setOnClickListener(this::addIndoorRules);

        mAddIndoor4x4RulesButton = fragmentView.findViewById(R.id.add_indoor_4x4_rules_button);
        mAddIndoor4x4RulesButton.setOnClickListener(this::addIndoor4x4Rules);

        mAddBeachRulesButton = fragmentView.findViewById(R.id.add_beach_rules_button);
        mAddBeachRulesButton.setOnClickListener(this::addBeachRules);

        mAddSnowRulesButton = fragmentView.findViewById(R.id.add_snow_rules_button);
        mAddSnowRulesButton.setOnClickListener(this::addSnowRules);

        mAddRulesButton = fragmentView.findViewById(R.id.add_rules_button);
        mAddRulesButton.setOnClickListener(view -> {
            if (mIsFabOpen) {
                closeFABMenu();
            } else {
                showFABMenu();
            }
        });

        updateStoredRulesList();

        UiUtils.addExtendShrinkListener(storedRulesList, mAddRulesButton);

        mBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (mIsFabOpen) {
                    closeFABMenu();
                } else if (mStoredRulesListAdapter.hasSelectedItems()) {
                    mStoredRulesListAdapter.clearSelectedItems();
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
        toolbar.inflateMenu(R.menu.menu_stored_rules_list);

        Menu menu = toolbar.getMenu();

        mDeleteSelectedRulesItem = menu.findItem(R.id.action_delete_rules);
        mDeleteSelectedRulesItem.setVisible(false);

        MenuItem searchRulesItem = menu.findItem(R.id.action_search_rules);
        SearchView searchRulesView = (SearchView) searchRulesItem.getActionView();

        searchRulesView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {});

        searchRulesView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                mStoredRulesListAdapter.getFilter().filter(searchQuery.trim());
                return true;
            }
        });

        MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(PrefUtils.canSync(requireContext()));

        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_search_rules) {
                return true;
            } else if (itemId == R.id.action_sync) {
                updateStoredRulesList();
                return true;
            } else if (itemId == R.id.action_delete_rules) {
                deleteSelectedRules();
                return true;
            } else {
                return false;
            }
        });
    }

    private void addIndoorRules(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new indoor rules");
        addRules(GameType.INDOOR, view);
    }

    private void addIndoor4x4Rules(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new indoor 4x4 rules");
        addRules(GameType.INDOOR_4X4, view);
    }

    private void addBeachRules(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new beach rules");
        addRules(GameType.BEACH, view);
    }

    private void addSnowRules(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new snow rules");
        addRules(GameType.SNOW, view);
    }

    private void addRules(GameType gameType, View view) {
        final Intent intent = new Intent(requireContext(), StoredRulesActivity.class);
        intent.putExtra("rules", JsonConverters.GSON.toJson(mStoredRulesService.createRules(gameType), RulesDto.class));
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, "gameKindToToolbar").toBundle());
    }

    private void computeOnBackPressedCallbackState() {
        mBackPressedCallback.setEnabled(mStoredRulesListAdapter.hasSelectedItems() || mIsFabOpen);
    }

    private void onRulesSelected(RulesSummaryDto rulesDescription) {
        mStoredRulesListAdapter.toggleItemSelection(rulesDescription.getId());
        mDeleteSelectedRulesItem.setVisible(mStoredRulesListAdapter.hasSelectedItems());
        computeOnBackPressedCallbackState();
    }

    private void deleteSelectedRules() {
        Log.i(Tags.STORED_RULES, "Delete selected rules");
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_rules)).setMessage(getString(R.string.delete_selected_rules_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredRulesService.deleteRules(mStoredRulesListAdapter.getSelectedItems(), this);
            UiUtils.makeText(requireContext(), getString(R.string.deleted_selected_rules), Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void showFABMenu() {
        mIsFabOpen = true;
        UiUtils.colorCloseIconButton(requireContext(), mAddRulesButton);
        mFabMenu.setVisibility(View.VISIBLE);
        mAddIndoorRulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_first));
        mAddIndoor4x4RulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_third));
        mAddBeachRulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_second));
        mAddSnowRulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_fourth));
        computeOnBackPressedCallbackState();
    }

    private void closeFABMenu() {
        mIsFabOpen = false;
        UiUtils.colorPlusIconButton(requireContext(), mAddRulesButton);
        mAddIndoorRulesButton.animate().translationY(0);
        mAddIndoor4x4RulesButton.animate().translationY(0);
        mAddBeachRulesButton.animate().translationY(0);
        mAddSnowRulesButton.animate().translationY(0).withEndAction(() -> mFabMenu.setVisibility(View.INVISIBLE));
        computeOnBackPressedCallbackState();
    }

    private void updateStoredRulesList() {
        if (PrefUtils.canSync(requireContext())) {
            mSyncLayout.setRefreshing(true);
            mStoredRulesService.syncRules(this);
        }
    }

    @Override
    public void onSynchronizationSucceeded() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                mStoredRulesListAdapter.updateStoredRulesList(mStoredRulesService.listRules());
                if (mDeleteSelectedRulesItem != null) {
                    mDeleteSelectedRulesItem.setVisible(mStoredRulesListAdapter.hasSelectedItems());
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