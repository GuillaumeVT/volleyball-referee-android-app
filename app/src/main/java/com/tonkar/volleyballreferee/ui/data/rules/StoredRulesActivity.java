package com.tonkar.volleyballreferee.ui.data.rules;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.ApiRules;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.StoredRulesManager;
import com.tonkar.volleyballreferee.engine.service.StoredRulesService;
import com.tonkar.volleyballreferee.ui.interfaces.RulesHandler;
import com.tonkar.volleyballreferee.ui.rules.RulesSetupFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class StoredRulesActivity extends AppCompatActivity {

    private Rules   mRules;
    private boolean mCreate;

    public StoredRulesActivity() {
        super();
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof RulesHandler) {
                RulesHandler rulesHandler = (RulesHandler) fragment;
                rulesHandler.setRules(mRules);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mRules = new Rules();
        mRules.setAll(JsonConverters.GSON.fromJson(getIntent().getStringExtra("rules"), ApiRules.class));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_rules);

        mCreate = getIntent().getBooleanExtra("create", true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.updateToolbarLogo(toolbar, mRules.getKind(), UsageType.NORMAL);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, RulesSetupFragment.newInstance(false));
        fragmentTransaction.commit();

        computeSaveLayoutVisibility();

        ScrollView scrollView = findViewById(R.id.rules_scroll_view);
        ExtendedFloatingActionButton saveRulesButton = findViewById(R.id.save_rules_button);
        UiUtils.addExtendShrinkListener(scrollView, saveRulesButton);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getIntent().putExtra("rules", JsonConverters.GSON.toJson(mRules, ApiRules.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_rules, menu);

        MenuItem deleteItem = menu.findItem(R.id.action_delete_rules);
        deleteItem.setVisible(!mCreate);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            cancelRules();
            return true;
        } else if (itemId == R.id.action_delete_rules) {
            deleteRules();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        cancelRules();
    }

    public void saveRules(View view) {
        Log.i(Tags.STORED_RULES, "Save rules");
        StoredRulesService storedRulesService = new StoredRulesManager(this);
        storedRulesService.saveRules(mRules, mCreate);
        UiUtils.makeText(this, getString(R.string.saved_rules), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(StoredRulesActivity.this, StoredRulesListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        UiUtils.animateCreate(this);
    }

    private void deleteRules() {
        Log.i(Tags.STORED_RULES, "Delete rules");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_rules)).setMessage(getString(R.string.delete_rules_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            StoredRulesService storedRulesService = new StoredRulesManager(this);
            storedRulesService.deleteRules(mRules.getId());
            UiUtils.makeText(StoredRulesActivity.this, getString(R.string.deleted_rules), Toast.LENGTH_LONG).show();

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
        builder.setTitle(getString(R.string.leave_rules_creation_title)).setMessage(getString(R.string.leave_rules_creation_question));
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

    public void computeSaveLayoutVisibility() {
        View saveLayout = findViewById(R.id.save_rules_layout);
        if (mRules.getName().length() > 1) {
            Log.i(Tags.STORED_RULES, "Save button is visible");
            saveLayout.setVisibility(View.VISIBLE);
        } else {
            Log.i(Tags.STORED_RULES, "Save button is invisible");
            saveLayout.setVisibility(View.GONE);
        }
    }
}
