package com.tonkar.volleyballreferee.ui.data;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.data.SavedRulesService;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.rules.RulesSetupFragment;

public class SavedRulesActivity extends AppCompatActivity {

    private SavedRulesService mSavedRulesService;
    private Rules             mRules;
    private MenuItem          mSaveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_rules);

        ServicesProvider.getInstance().restoreSavedRulesService(getApplicationContext());
        mSavedRulesService = ServicesProvider.getInstance().getSavedRulesService();
        mRules = mSavedRulesService.getCurrentRules();

        boolean editable = getIntent().getBooleanExtra("editable", true);

        setTitle("");

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, RulesSetupFragment.newInstance(false, editable));
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_saved_rules, menu);

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
        Log.i("VBR-SavedRulesActivity", "Save rules");
        mSavedRulesService.getCurrentRules().setUserId(PrefUtils.getUserId(this));
        mSavedRulesService.saveCurrentRules();
        Toast.makeText(this, getResources().getString(R.string.saved_rules), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(SavedRulesActivity.this, SavedRulesListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void deleteRules() {
        Log.i("VBR-SavedRulesActivity", "Delete rules");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_rules)).setMessage(getResources().getString(R.string.delete_rules_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mSavedRulesService.deleteSavedRules(mRules.getName());
                Toast.makeText(SavedRulesActivity.this, getResources().getString(R.string.deleted_rules), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(SavedRulesActivity.this, SavedRulesListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    private void cancelRules() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.leave_rules_creation_title)).setMessage(getResources().getString(R.string.leave_rules_creation_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mSavedRulesService.loadSavedRules();
                Intent intent = new Intent(SavedRulesActivity.this, SavedRulesListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

    public void computeSaveItemVisibility() {
        if (mSaveItem != null) {
            if (mRules.getName().length() > 0 && mSavedRulesService.getSavedRules(mRules.getName()) == null) {
                Log.i("VBR-SavedRulesActivity", "Save button is visible");
                mSaveItem.setVisible(true);
            } else {
                Log.i("VBR-SavedRulesActivity", "Save button is invisible");
                mSaveItem.setVisible(false);
            }
        }
    }
}
