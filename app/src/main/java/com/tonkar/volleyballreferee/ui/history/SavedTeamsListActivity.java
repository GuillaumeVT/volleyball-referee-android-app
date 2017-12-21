package com.tonkar.volleyballreferee.ui.history;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.SavedTeamsService;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.List;

public class SavedTeamsListActivity extends AppCompatActivity {

    private SavedTeamsService     mSavedTeamsService;
    private SavedTeamsListAdapter mSavedTeamsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-TeamsListActivity", "Create teams list activity");
        setContentView(R.layout.activity_saved_teams_list);

        ServicesProvider.getInstance().restoreSavedTeamsService(getApplicationContext());

        setTitle(getResources().getString(R.string.saved_teams));

        mSavedTeamsService = ServicesProvider.getInstance().getSavedTeamsService();

        List<BaseIndoorTeamService> savedTeamsServiceList = mSavedTeamsService.getSavedTeamServiceList();

        final ListView savedTeamsList = findViewById(R.id.saved_teams_list);
        mSavedTeamsListAdapter = new SavedTeamsListAdapter(this, getLayoutInflater(), savedTeamsServiceList);
        savedTeamsList.setAdapter(mSavedTeamsListAdapter);

        savedTeamsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BaseIndoorTeamService indoorTeamService = mSavedTeamsListAdapter.getItem(i);
                mSavedTeamsService.editTeam(indoorTeamService.getTeamName(null), indoorTeamService.getGenderType());
                Log.i("VBR-TeamsListActivity", String.format("Start activity to edit saved team %s", indoorTeamService.getTeamName(null)));

                final Intent intent = new Intent(SavedTeamsListActivity.this, SavedTeamActivity.class);
                startActivity(intent);
            }
        });
    }

    public void addTeam(View view) {
        mSavedTeamsService.createTeam();
        Log.i("VBR-TeamsListActivity", "Start activity to create new team");

        final Intent intent = new Intent(this, SavedTeamActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_saved_teams, menu);

        MenuItem deleteAllTeamsItem = menu.findItem(R.id.action_delete_teams);
        deleteAllTeamsItem.setVisible(mSavedTeamsService.getSavedTeamServiceList().size() > 0);

        MenuItem searchTeamsItem = menu.findItem(R.id.action_search_teams);
        SearchView searchTeamsView = (SearchView) searchTeamsItem.getActionView();

        searchTeamsView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });

        searchTeamsView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                mSavedTeamsListAdapter.getFilter().filter(searchQuery.trim());
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_teams:
                return true;
            case R.id.action_delete_teams:
                deleteAllTeams();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllTeams() {
        Log.i("VBR-TeamsListActivity", "Delete all teams");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.delete_teams)).setMessage(getResources().getString(R.string.delete_teams_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mSavedTeamsService.deleteAllSavedTeams();
                Toast.makeText(SavedTeamsListActivity.this, getResources().getString(R.string.deleted_teams), Toast.LENGTH_LONG).show();
                UiUtils.navigateToHome(SavedTeamsListActivity.this);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }
}
