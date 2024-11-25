package com.tonkar.volleyballreferee.ui.data.rules;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.JsonConverters;
import com.tonkar.volleyballreferee.engine.api.model.RulesDto;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.interfaces.RulesHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class StoredRulesViewActivity extends AppCompatActivity {

    private StoredRulesService mStoredRulesService;
    private Rules              mRules;

    public StoredRulesViewActivity() {
        super();
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof RulesHandler rulesHandler) {
                rulesHandler.setRules(mRules);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredRulesService = new StoredRulesManager(this);
        mRules = new Rules();
        mRules.setAll(JsonConverters.GSON.fromJson(getIntent().getStringExtra("rules"), RulesDto.class));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_rules_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView nameText = findViewById(R.id.stored_rules_name);
        ImageView kindItem = findViewById(R.id.rules_kind_item);

        nameText.setText(mRules.getName());

        switch (mRules.getKind()) {
            case INDOOR_4X4 -> UiUtils.colorChipIcon(this, R.color.colorIndoor4x4Light, R.drawable.ic_4x4_small, kindItem);
            case BEACH -> UiUtils.colorChipIcon(this, R.color.colorBeachLight, R.drawable.ic_beach, kindItem);
            case SNOW -> UiUtils.colorChipIcon(this, R.color.colorSnowLight, R.drawable.ic_snow, kindItem);
            default -> UiUtils.colorChipIcon(this, R.color.colorIndoorLight, R.drawable.ic_6x6_small, kindItem);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, RulesFragment.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ScrollView scrollView = findViewById(R.id.rules_scroll_view);
        ExtendedFloatingActionButton editRulesButton = findViewById(R.id.edit_rules_button);
        UiUtils.addExtendShrinkListener(scrollView, editRulesButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_stored_rules_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (itemId == R.id.action_delete_rules) {
            deleteRules();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void editRules(View view) {
        RulesDto rules = mStoredRulesService.getRules(mRules.getId());

        if (rules == null) {
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            Log.i(Tags.STORED_RULES, String.format("Start activity to edit stored rules %s", rules.getName()));

            final Intent intent = new Intent(this, StoredRulesActivity.class);
            intent.putExtra("rules", JsonConverters.GSON.toJson(rules, RulesDto.class));
            intent.putExtra("create", false);
            startActivity(intent);
            UiUtils.animateForward(this);
        }
    }

    private void deleteRules() {
        Log.i(Tags.STORED_RULES, "Delete rules");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_rules)).setMessage(getString(R.string.delete_rules_question));
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            StoredRulesService storedRulesService = new StoredRulesManager(this);
            storedRulesService.deleteRules(mRules.getId());
            UiUtils.makeText(this, getString(R.string.deleted_rules), Toast.LENGTH_LONG).show();
            UiUtils.navigateToMain(this, R.id.stored_rules_list_fragment);
            UiUtils.animateBackward(this);
        });
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }
}
