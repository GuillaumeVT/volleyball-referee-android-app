package com.tonkar.volleyballreferee.ui.data;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.DataSynchronizationListener;
import com.tonkar.volleyballreferee.interfaces.data.SavedRulesService;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class SavedRulesListActivity extends AppCompatActivity implements DataSynchronizationListener {

    private SavedRulesService     mSavedRulesService;
    private SavedRulesListAdapter mSavedRulesListAdapter;
    private SwipeRefreshLayout    mSyncLayout;
    private boolean               mIsFabOpen;
    private FloatingActionButton  mAddIndoorRulesButton;
    private FloatingActionButton  mAddIndoor4x4RulesButton;
    private FloatingActionButton  mAddBeachRulesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(Tags.SAVED_RULES, "Create rules list activity");
        setContentView(R.layout.activity_saved_rules_list);

        ServicesProvider.getInstance().restoreSavedRulesService(getApplicationContext());

        setTitle(getResources().getString(R.string.saved_rules_list));

        mSyncLayout = findViewById(R.id.sync_layout);
        mSyncLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateSavedRulesList();
                    }
                }
        );

        mSavedRulesService = ServicesProvider.getInstance().getSavedRulesService();

        List<Rules> savedRules = mSavedRulesService.getSavedRules();

        final ListView savedRulesList = findViewById(R.id.saved_rules_list);
        mSavedRulesListAdapter = new SavedRulesListAdapter(this, getLayoutInflater(), savedRules);
        savedRulesList.setAdapter(mSavedRulesListAdapter);

        savedRulesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Rules rules = mSavedRulesListAdapter.getItem(i);
                mSavedRulesService.editRules(rules.getName());
                Log.i(Tags.SAVED_RULES, String.format("Start activity to edit saved rules %s", rules.getName()));

                final Intent intent = new Intent(SavedRulesListActivity.this, SavedRulesActivity.class);
                boolean editable = !PrefUtils.isSignedIn(SavedRulesListActivity.this);
                intent.putExtra("editable", editable);
                startActivity(intent);
            }
        });

        mIsFabOpen = false;
        mAddIndoorRulesButton = findViewById(R.id.add_indoor_rules_button);
        mAddIndoor4x4RulesButton = findViewById(R.id.add_indoor_4x4_rules_button);
        mAddBeachRulesButton = findViewById(R.id.add_beach_rules_button);
        mAddIndoorRulesButton.hide();
        mAddIndoor4x4RulesButton.hide();
        mAddBeachRulesButton.hide();
        FloatingActionButton addRulesButton = findViewById(R.id.add_rules_button);
        addRulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsFabOpen){
                    closeFABMenu();
                }else{
                    showFABMenu();
                }
            }
        });
    }

    public void addIndoorRules(View view) {
        mSavedRulesService.createRules(GameType.INDOOR);
        Log.i(Tags.SAVED_RULES, "Start activity to create new indoor rules");

        final Intent intent = new Intent(this, SavedRulesActivity.class);
        intent.putExtra("editable", true);
        startActivity(intent);
    }

    public void addIndoor4x4Rules(View view) {
        mSavedRulesService.createRules(GameType.INDOOR_4X4);
        Log.i(Tags.SAVED_RULES, "Start activity to create new indoor 4x4 rules");

        final Intent intent = new Intent(this, SavedRulesActivity.class);
        intent.putExtra("editable", true);
        startActivity(intent);
    }

    public void addBeachRules(View view) {
        mSavedRulesService.createRules(GameType.BEACH);
        Log.i(Tags.SAVED_RULES, "Start activity to create new beach rules");

        final Intent intent = new Intent(this, SavedRulesActivity.class);
        intent.putExtra("editable", true);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_saved_rules_list, menu);

        MenuItem deleteAllRulesItem = menu.findItem(R.id.action_delete_rules);
        deleteAllRulesItem.setVisible(mSavedRulesService.getSavedRules().size() > 0);

        MenuItem searchRulesItem = menu.findItem(R.id.action_search_rules);
        SearchView searchRulesView = (SearchView) searchRulesItem.getActionView();

        searchRulesView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });

        searchRulesView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                mSavedRulesListAdapter.getFilter().filter(searchQuery.trim());
                return true;
            }
        });

        MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(PrefUtils.isSyncOn(this));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_rules:
                return true;
            case R.id.action_sync:
                updateSavedRulesList();
                return true;
            case R.id.action_delete_rules:
                deleteAllRules();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllRules() {
        Log.i(Tags.SAVED_RULES, "Delete all rules");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_rules)).setMessage(getResources().getString(R.string.delete_all_rules_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mSavedRulesService.deleteAllSavedRules();
                UiUtils.makeText(SavedRulesListActivity.this, getResources().getString(R.string.deleted_all_rules), Toast.LENGTH_LONG).show();
                UiUtils.navigateToHome(SavedRulesListActivity.this);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
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

    private void updateSavedRulesList() {
        if (PrefUtils.isSyncOn(this)) {
            mSyncLayout.setRefreshing(true);
            mSavedRulesService.syncRulesOnline(this);
        }
    }

    @Override
    public void onSynchronizationSucceeded() {
        mSavedRulesListAdapter.updateSavedRulesList(mSavedRulesService.getSavedRules());
        mSyncLayout.setRefreshing(false);
    }

    @Override
    public void onSynchronizationFailed() {
        UiUtils.makeText(this, getResources().getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
        mSyncLayout.setRefreshing(false);
    }
}
