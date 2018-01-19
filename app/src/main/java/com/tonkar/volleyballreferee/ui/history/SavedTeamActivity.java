package com.tonkar.volleyballreferee.ui.history;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.SavedTeamsService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.team.TeamSetupFragment;

public class SavedTeamActivity extends AppCompatActivity {

    private SavedTeamsService     mSavedTeamsService;
    private BaseIndoorTeamService mIndoorTeamService;
    private MenuItem              mSaveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_team);

        ServicesProvider.getInstance().restoreSavedTeamsService(getApplicationContext());
        mSavedTeamsService = ServicesProvider.getInstance().getSavedTeamsService();
        mIndoorTeamService = mSavedTeamsService.getCurrentTeam();

        setTitle("");

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, TeamSetupFragment.newInstance(TeamType.HOME, false));
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_saved_team, menu);

        mSaveItem = menu.findItem(R.id.action_save_team);
        computeSaveItemVisibility();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelTeam();
                return true;
            case R.id.action_save_team:
                saveTeam();
                return true;
            case R.id.action_delete_team:
                deleteTeam();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        cancelTeam();
    }

    private void saveTeam() {
        Log.i("VBR-SavedTeamActivity", "Save team");
        mSavedTeamsService.saveCurrentTeam();
        Toast.makeText(SavedTeamActivity.this, getResources().getString(R.string.saved_team), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(SavedTeamActivity.this, SavedTeamsListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void deleteTeam() {
        Log.i("VBR-SavedTeamActivity", "Delete team");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_team)).setMessage(getResources().getString(R.string.delete_team_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mSavedTeamsService.deleteSavedTeam(mIndoorTeamService.getTeamName(null), mIndoorTeamService.getGenderType());
                Toast.makeText(SavedTeamActivity.this, getResources().getString(R.string.deleted_team), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(SavedTeamActivity.this, SavedTeamsListActivity.class);
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

    private void cancelTeam() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.leave_team_creation_title)).setMessage(getResources().getString(R.string.leave_team_creation_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mSavedTeamsService.loadSavedTeams();
                Intent intent = new Intent(SavedTeamActivity.this, SavedTeamsListActivity.class);
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
            if (mIndoorTeamService.getTeamName(TeamType.HOME).isEmpty() || mIndoorTeamService.getNumberOfPlayers(TeamType.HOME) < 6
                    || mIndoorTeamService.getTeamName(TeamType.GUEST).isEmpty() || mIndoorTeamService.getNumberOfPlayers(TeamType.GUEST) < 6
                    || mIndoorTeamService.getCaptain(TeamType.HOME) < 1 || mIndoorTeamService.getCaptain(TeamType.GUEST) < 1) {
                Log.i("VBR-SavedTeamActivity", "Save button is invisible");
                mSaveItem.setVisible(false);
            } else {
                Log.i("VBR-SavedTeamActivity", "Save button is visible");
                mSaveItem.setVisible(true);
            }
        }
    }
}