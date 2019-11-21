package com.tonkar.volleyballreferee.ui.stored.rules;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.stored.DataSynchronizationListener;
import com.tonkar.volleyballreferee.engine.stored.StoredRulesManager;
import com.tonkar.volleyballreferee.engine.stored.StoredRulesService;
import com.tonkar.volleyballreferee.engine.stored.api.ApiRules;
import com.tonkar.volleyballreferee.engine.stored.api.ApiRulesSummary;
import com.tonkar.volleyballreferee.ui.NavigationActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

public class StoredRulesListActivity extends NavigationActivity implements DataSynchronizationListener {

    private StoredRulesService     mStoredRulesService;
    private StoredRulesListAdapter mStoredRulesListAdapter;
    private SwipeRefreshLayout     mSyncLayout;
    private boolean                mIsFabOpen;
    private FloatingActionButton   mAddRulesButton;
    private FloatingActionButton   mAddIndoorRulesButton;
    private FloatingActionButton   mAddIndoor4x4RulesButton;
    private FloatingActionButton   mAddBeachRulesButton;
    private FloatingActionButton   mAddSnowRulesButton;
    private MenuItem               mDeleteSelectedRulesItem;

    @Override
    protected String getToolbarTitle() {
        return "";
    }

    @Override
    protected int getCheckedItem() {
        return R.id.action_stored_rules;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredRulesService = new StoredRulesManager(this);

        super.onCreate(savedInstanceState);

        Log.i(Tags.STORED_RULES, "Create rules list activity");
        setContentView(R.layout.activity_stored_rules_list);

        initNavigationMenu();

        mSyncLayout = findViewById(R.id.sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateStoredRulesList);

        List<ApiRulesSummary> storedRules = mStoredRulesService.listRules();

        final ListView storedRulesList = findViewById(R.id.stored_rules_list);
        mStoredRulesListAdapter = new StoredRulesListAdapter(this, getLayoutInflater(), storedRules);
        storedRulesList.setAdapter(mStoredRulesListAdapter);

        storedRulesList.setOnItemClickListener((parent, view, position, l) -> {
            ApiRulesSummary rulesDescription = mStoredRulesListAdapter.getItem(position);

            if (mStoredRulesListAdapter.hasSelectedItems()) {
                mStoredRulesListAdapter.toggleItemSelection(rulesDescription.getId());
                mDeleteSelectedRulesItem.setVisible(mStoredRulesListAdapter.hasSelectedItems());
            } else {
                ApiRules rules = mStoredRulesService.getRules(rulesDescription.getId());
                Log.i(Tags.STORED_RULES, String.format("Start activity to view stored rules %s", rules.getName()));

                final Intent intent = new Intent(StoredRulesListActivity.this, StoredRulesViewActivity.class);
                intent.putExtra("rules", mStoredRulesService.writeRules(rules));
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "listItemToDetails").toBundle());
            }
        });

        storedRulesList.setOnItemLongClickListener((parent, view, position, id) -> {
            ApiRulesSummary rulesDescription = mStoredRulesListAdapter.getItem(position);
            mStoredRulesListAdapter.toggleItemSelection(rulesDescription.getId());
            mDeleteSelectedRulesItem.setVisible(mStoredRulesListAdapter.hasSelectedItems());
            return true;
        });

        mIsFabOpen = false;
        mAddIndoorRulesButton = findViewById(R.id.add_indoor_rules_button);
        mAddIndoor4x4RulesButton = findViewById(R.id.add_indoor_4x4_rules_button);
        mAddBeachRulesButton = findViewById(R.id.add_beach_rules_button);
        mAddSnowRulesButton = findViewById(R.id.add_snow_rules_button);
        mAddIndoorRulesButton.hide();
        mAddIndoor4x4RulesButton.hide();
        mAddBeachRulesButton.hide();
        mAddSnowRulesButton.hide();
        mAddRulesButton = findViewById(R.id.add_rules_button);
        mAddRulesButton.setOnClickListener(view -> {
            if(mIsFabOpen){
                closeFABMenu();
            }else{
                showFABMenu();
            }
        });

        updateStoredRulesList();
    }

    public void addIndoorRules(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new indoor rules");
        addRules(GameType.INDOOR, view);
    }

    public void addIndoor4x4Rules(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new indoor 4x4 rules");
        addRules(GameType.INDOOR_4X4, view);
    }

    public void addBeachRules(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new beach rules");
        addRules(GameType.BEACH, view);
    }

    public void addSnowRules(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new snow rules");
        addRules(GameType.SNOW, view);
    }

    private void addRules(GameType gameType, View view) {
        final Intent intent = new Intent(this, StoredRulesActivity.class);
        intent.putExtra("rules", mStoredRulesService.writeRules(mStoredRulesService.createRules(gameType)));
        intent.putExtra("create", true);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "gameKindToToolbar").toBundle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_rules_list, menu);

        mDeleteSelectedRulesItem = menu.findItem(R.id.action_delete_rules);
        mDeleteSelectedRulesItem.setVisible(false);

        MenuItem searchRulesItem = menu.findItem(R.id.action_search_rules);
        SearchView searchRulesView = (SearchView) searchRulesItem.getActionView();

        searchRulesView.setOnQueryTextFocusChangeListener((view, hasFocus) -> {});

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
        syncItem.setVisible(PrefUtils.canSync(this));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_rules:
                return true;
            case R.id.action_sync:
                updateStoredRulesList();
                return true;
            case R.id.action_delete_rules:
                deleteSelectedRules();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSelectedRules() {
        Log.i(Tags.STORED_RULES, "Delete selected rules");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_rules)).setMessage(getString(R.string.delete_selected_rules_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredRulesService.deleteRules(mStoredRulesListAdapter.getSelectedItems(), this);
            UiUtils.makeText(StoredRulesListActivity.this, getString(R.string.deleted_selected_rules), Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void showFABMenu(){
        mIsFabOpen = true;
        UiUtils.colorCloseIconButton(this, mAddRulesButton);
        mAddIndoorRulesButton.show();
        mAddIndoor4x4RulesButton.show();
        mAddBeachRulesButton.show();
        mAddSnowRulesButton.show();
        mAddIndoorRulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_first));
        mAddIndoor4x4RulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_third));
        mAddBeachRulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_second));
        mAddSnowRulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_fourth));
    }

    private void closeFABMenu(){
        mIsFabOpen = false;
        UiUtils.colorPlusIconButton(this, mAddRulesButton);
        mAddIndoorRulesButton.animate().translationY(0);
        mAddIndoor4x4RulesButton.animate().translationY(0);
        mAddBeachRulesButton.animate().translationY(0);
        mAddSnowRulesButton.animate().translationY(0);
        mAddIndoorRulesButton.hide();
        mAddIndoor4x4RulesButton.hide();
        mAddBeachRulesButton.hide();
        mAddSnowRulesButton.hide();
    }

    @Override
    public void onBackPressed() {
        if(mIsFabOpen) {
            closeFABMenu();
        } else if(mStoredRulesListAdapter.hasSelectedItems()){
            mStoredRulesListAdapter.clearSelectedItems();
        } else {
            super.onBackPressed();
        }
    }

    private void updateStoredRulesList() {
        if (PrefUtils.canSync(this)) {
            mSyncLayout.setRefreshing(true);
            mStoredRulesService.syncRules(this);
        }
    }

    @Override
    public void onSynchronizationSucceeded() {
        runOnUiThread(() -> {
            mStoredRulesListAdapter.updateStoredRulesList(mStoredRulesService.listRules());
            if (mDeleteSelectedRulesItem != null) {
                mDeleteSelectedRulesItem.setVisible(mStoredRulesListAdapter.hasSelectedItems());
            }
            mSyncLayout.setRefreshing(false);
        });
    }

    @Override
    public void onSynchronizationFailed() {
        runOnUiThread(() -> {
            UiUtils.makeErrorText(this, getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
            mSyncLayout.setRefreshing(false);
        });
    }
}
