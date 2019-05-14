package com.tonkar.volleyballreferee.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiFriend;
import com.tonkar.volleyballreferee.api.ApiFriendRequest;
import com.tonkar.volleyballreferee.api.ApiFriendsAndRequests;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.data.StoredUser;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.AsyncFriendRequestListener;
import com.tonkar.volleyballreferee.interfaces.data.StoredUserService;
import com.tonkar.volleyballreferee.ui.NavigationActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class ColleaguesListActivity extends NavigationActivity implements AsyncFriendRequestListener {

    private StoredUserService     mStoredUserService;
    private ColleaguesListAdapter mColleaguesListAdapter;
    private SwipeRefreshLayout    mSyncLayout;

    @Override
    protected String getToolbarTitle() {
        return "";
    }

    @Override
    protected int getCheckedItem() {
        return R.id.action_colleagues;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStoredUserService = new StoredUser(this);

        super.onCreate(savedInstanceState);

        Log.i(Tags.STORED_USER, "Create colleagues list activity");
        setContentView(R.layout.activity_colleagues_list);

        initNavigationMenu();

        mSyncLayout = findViewById(R.id.sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateColleaguesList);

        final ListView colleaguesList = findViewById(R.id.colleagues_list);
        mColleaguesListAdapter = new ColleaguesListAdapter(this, getLayoutInflater(), this);
        colleaguesList.setAdapter(mColleaguesListAdapter);

        updateColleaguesList();
    }

    public void addColleague(View view) {
        Log.i(Tags.STORED_USER, "Add colleague");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.add_colleague))
                .setView(getLayoutInflater().inflate(R.layout.pseudo_input_dialog, null))
                .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    EditText editText = alertDialog.findViewById(R.id.pseudo_input_text);
                    String pseudo = editText.getText().toString().trim();
                    if (pseudo.length() > 2) {
                        mStoredUserService.sendFriendRequest(pseudo, ColleaguesListActivity.this);
                    } else {
                        UiUtils.makeErrorText(ColleaguesListActivity.this, String.format(getString(R.string.must_provide_at_least_n_characters), 3), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> {});

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_colleagues_list, menu);

        MenuItem searchColleagueItem = menu.findItem(R.id.action_search_colleague);
        SearchView searchColleagueView = (SearchView) searchColleagueItem.getActionView();

        searchColleagueView.setOnQueryTextFocusChangeListener((view, hasFocus) -> {});

        searchColleagueView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                mColleaguesListAdapter.getFilter().filter(searchQuery.trim());
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
            case R.id.action_search_colleague:
                return true;
            case R.id.action_sync:
                updateColleaguesList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateColleaguesList() {
        if (PrefUtils.canSync(this)) {
            mSyncLayout.setRefreshing(true);
            mStoredUserService.downloadFriendsAndRequests(this);
        }
    }

    @Override
    public void onFriendsAndRequestsReceived(ApiFriendsAndRequests friendsAndRequests) {
        mColleaguesListAdapter.updateFriendsAndRequests(friendsAndRequests);
        mSyncLayout.setRefreshing(false);
    }

    @Override
    public void onFriendRequestSent(String friendPseudo) {
        UiUtils.makeText(this, getString(R.string.sync_succeeded_message), Toast.LENGTH_LONG).show();
        updateColleaguesList();
    }

    @Override
    public void onFriendRequestAccepted(ApiFriendRequest friendRequest) {
        UiUtils.makeText(this, getString(R.string.sync_succeeded_message), Toast.LENGTH_LONG).show();
        updateColleaguesList();
    }

    @Override
    public void onFriendRequestRejected(ApiFriendRequest friendRequest) {
        UiUtils.makeText(this, getString(R.string.sync_succeeded_message), Toast.LENGTH_LONG).show();
        updateColleaguesList();
    }

    @Override
    public void onFriendRemoved(ApiFriend friend) {
        UiUtils.makeText(this, getString(R.string.sync_succeeded_message), Toast.LENGTH_LONG).show();
        updateColleaguesList();
    }

    @Override
    public void onError(int httpCode) {
        UiUtils.makeErrorText(this, getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
        mSyncLayout.setRefreshing(false);
    }
}
