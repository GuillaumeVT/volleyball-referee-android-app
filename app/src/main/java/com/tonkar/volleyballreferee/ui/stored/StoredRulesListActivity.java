package com.tonkar.volleyballreferee.ui.stored;

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
    private FloatingActionButton   mAddIndoorRulesButton;
    private FloatingActionButton   mAddIndoor4x4RulesButton;
    private FloatingActionButton   mAddBeachRulesButton;

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

        storedRulesList.setOnItemClickListener((adapterView, view, i, l) -> {
            ApiRulesSummary rulesDescription = mStoredRulesListAdapter.getItem(i);
            ApiRules rules = mStoredRulesService.getRules(rulesDescription.getId());
            Log.i(Tags.STORED_RULES, String.format("Start activity to view stored rules %s", rules.getName()));

            final Intent intent = new Intent(StoredRulesListActivity.this, StoredRulesViewActivity.class);
            intent.putExtra("rules", mStoredRulesService.writeRules(rules));
            startActivity(intent);
            UiUtils.animateForward(this);
        });

        mIsFabOpen = false;
        mAddIndoorRulesButton = findViewById(R.id.add_indoor_rules_button);
        mAddIndoor4x4RulesButton = findViewById(R.id.add_indoor_4x4_rules_button);
        mAddBeachRulesButton = findViewById(R.id.add_beach_rules_button);
        mAddIndoorRulesButton.hide();
        mAddIndoor4x4RulesButton.hide();
        mAddBeachRulesButton.hide();
        FloatingActionButton addRulesButton = findViewById(R.id.add_rules_button);
        addRulesButton.setOnClickListener(view -> {
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
        addRules(GameType.INDOOR);
    }

    public void addIndoor4x4Rules(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new indoor 4x4 rules");
        addRules(GameType.INDOOR_4X4);
    }

    public void addBeachRules(View view) {
        Log.i(Tags.STORED_RULES, "Start activity to create new beach rules");
        addRules(GameType.BEACH);
    }

    private void addRules(GameType gameType) {
        final Intent intent = new Intent(this, StoredRulesActivity.class);
        intent.putExtra("rules", mStoredRulesService.writeRules(mStoredRulesService.createRules(gameType)));
        intent.putExtra("create", true);
        startActivity(intent);
        UiUtils.animateCreate(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_rules_list, menu);

        MenuItem deleteAllRulesItem = menu.findItem(R.id.action_delete_rules);
        deleteAllRulesItem.setVisible(mStoredRulesService.hasRules());

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
                deleteAllRules();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllRules() {
        Log.i(Tags.STORED_RULES, "Delete all rules");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_rules)).setMessage(getString(R.string.delete_all_rules_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mStoredRulesService.deleteAllRules();
            UiUtils.makeText(StoredRulesListActivity.this, getString(R.string.deleted_all_rules), Toast.LENGTH_LONG).show();
            UiUtils.navigateToHome(StoredRulesListActivity.this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void showFABMenu(){
        mIsFabOpen = true;
        mAddIndoorRulesButton.show();
        mAddIndoor4x4RulesButton.show();
        mAddBeachRulesButton.show();
        mAddIndoorRulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_third));
        mAddIndoor4x4RulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_second));
        mAddBeachRulesButton.animate().translationY(-getResources().getDimension(R.dimen.fab_shift_first));
    }

    private void closeFABMenu(){
        mIsFabOpen = false;
        mAddIndoorRulesButton.animate().translationY(0);
        mAddIndoor4x4RulesButton.animate().translationY(0);
        mAddBeachRulesButton.animate().translationY(0);
        mAddIndoorRulesButton.hide();
        mAddIndoor4x4RulesButton.hide();
        mAddBeachRulesButton.hide();
    }

    @Override
    public void onBackPressed() {
        if(mIsFabOpen){
            closeFABMenu();
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
