package com.tonkar.volleyballreferee.ui.data;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.StoredRules;
import com.tonkar.volleyballreferee.business.rules.Rules;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.StoredRulesService;
import com.tonkar.volleyballreferee.ui.interfaces.RulesHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import com.tonkar.volleyballreferee.ui.rules.RulesSetupFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class StoredRulesActivity extends AppCompatActivity {

    private Rules    mRules;
    private MenuItem mSaveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StoredRulesService storedRulesService = new StoredRules(this);
        mRules = new Rules();
        mRules.setAll(storedRulesService.readRules(getIntent().getStringExtra("rules")));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_rules);

        boolean create = getIntent().getBooleanExtra("create", true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, RulesSetupFragment.newInstance(false, create));
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_rules, menu);

        mSaveItem = menu.findItem(R.id.action_save_rules);
        computeSaveItemVisibility();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelRules();
                return true;
            case R.id.action_save_rules:
                saveRules();
                return true;
            case R.id.action_delete_rules:
                deleteRules();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        cancelRules();
    }

    private void saveRules() {
        Log.i(Tags.STORED_RULES, "Save rules");
        StoredRulesService storedRulesService = new StoredRules(this);
        storedRulesService.saveRules(mRules);
        UiUtils.makeText(this, getResources().getString(R.string.saved_rules), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(StoredRulesActivity.this, StoredRulesListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        UiUtils.animateCreate(this);
    }

    private void deleteRules() {
        Log.i(Tags.STORED_RULES, "Delete rules");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_rules)).setMessage(getResources().getString(R.string.delete_rules_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            StoredRulesService storedRulesService = new StoredRules(this);
            storedRulesService.deleteRules(mRules.getId());
            UiUtils.makeText(StoredRulesActivity.this, getResources().getString(R.string.deleted_rules), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(StoredRulesActivity.this, StoredRulesListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void cancelRules() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.leave_rules_creation_title)).setMessage(getResources().getString(R.string.leave_rules_creation_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            Intent intent = new Intent(StoredRulesActivity.this, StoredRulesListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    public void computeSaveItemVisibility() {
        if (mSaveItem != null) {
            if (mRules.getName().length() > 1) {
                Log.i(Tags.STORED_RULES, "Save button is visible");
                mSaveItem.setVisible(true);
            } else {
                Log.i(Tags.STORED_RULES, "Save button is invisible");
                mSaveItem.setVisible(false);
            }
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof RulesHandler) {
            RulesHandler rulesHandler = (RulesHandler) fragment;
            rulesHandler.setRules(mRules);
        }
    }

}
