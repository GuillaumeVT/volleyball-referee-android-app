package com.tonkar.volleyballreferee.ui.data;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.data.SavedRulesService;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.List;

public class SavedRulesListActivity extends AppCompatActivity {

    private SavedRulesService     mSavedRulesService;
    private SavedRulesListAdapter mSavedRulesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-RulesListActivity", "Create rules list activity");
        setContentView(R.layout.activity_saved_rules_list);

        ServicesProvider.getInstance().restoreSavedRulesService(getApplicationContext());

        setTitle(getResources().getString(R.string.saved_rules_list));

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
                Log.i("VBR-RulesListActivity", String.format("Start activity to edit saved rules %s", rules.getName()));

                final Intent intent = new Intent(SavedRulesListActivity.this, SavedRulesActivity.class);
                startActivity(intent);
            }
        });
    }

    public void addRules(View view) {
        mSavedRulesService.createRules();
        Log.i("VBR-RulesListActivity", "Start activity to create new rules");

        final Intent intent = new Intent(this, SavedRulesActivity.class);
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_rules:
                return true;
            case R.id.action_delete_rules:
                deleteAllRules();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllRules() {
        Log.i("VBR-RulesListActivity", "Delete all rules");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_rules)).setMessage(getResources().getString(R.string.delete_rules_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mSavedRulesService.deleteAllSavedRules();
                Toast.makeText(SavedRulesListActivity.this, getResources().getString(R.string.deleted_all_rules), Toast.LENGTH_LONG).show();
                UiUtils.navigateToHome(SavedRulesListActivity.this);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

}
