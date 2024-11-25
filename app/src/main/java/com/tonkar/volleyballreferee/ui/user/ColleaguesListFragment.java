package com.tonkar.volleyballreferee.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class ColleaguesListFragment extends Fragment implements AsyncFriendRequestListener {

    private StoredUserService     mStoredUserService;
    private ColleaguesListAdapter mColleaguesListAdapter;
    private SwipeRefreshLayout    mSyncLayout;

    public ColleaguesListFragment() {}

    public static ColleaguesListFragment newInstance() {
        return new ColleaguesListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mStoredUserService = new StoredUserManager(requireContext());

        Log.i(Tags.USER_UI, "Create colleagues list fragment");

        View fragmentView = inflater.inflate(R.layout.fragment_colleagues_list, container, false);

        mSyncLayout = fragmentView.findViewById(R.id.colleagues_sync_layout);
        mSyncLayout.setOnRefreshListener(this::updateColleaguesList);

        final ListView colleaguesList = fragmentView.findViewById(R.id.colleagues_list);
        mColleaguesListAdapter = new ColleaguesListAdapter(requireContext(), getLayoutInflater(), this);
        colleaguesList.setAdapter(mColleaguesListAdapter);

        updateColleaguesList();

        ExtendedFloatingActionButton addColleagueButton = fragmentView.findViewById(R.id.add_colleague_button);
        addColleagueButton.setOnClickListener(this::addColleague);
        UiUtils.addExtendShrinkListener(colleaguesList, addColleagueButton);

        initMenu(fragmentView);

        return fragmentView;
    }

    private void initMenu(View fragmentView) {
        Toolbar toolbar = fragmentView.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_colleagues_list);

        Menu menu = toolbar.getMenu();

        MenuItem searchColleagueItem = menu.findItem(R.id.action_search_colleague);
        SearchView searchColleagueView = (SearchView) searchColleagueItem.getActionView();

        searchColleagueView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {});

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
        syncItem.setVisible(PrefUtils.canSync(requireContext()));

        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_search_colleague) {
                return true;
            } else if (itemId == R.id.action_sync) {
                updateColleaguesList();
                return true;
            } else {
                return false;
            }
        });
    }

    private void addColleague(View view) {
        Log.i(Tags.USER_UI, "Add colleague");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.add_colleague))
                .setView(getLayoutInflater().inflate(R.layout.pseudo_input_dialog, null))
                .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    EditText editText = alertDialog.findViewById(R.id.pseudo_input_text);
                    String pseudo = editText.getText().toString().trim();
                    if (pseudo.length() > 2) {
                        mStoredUserService.sendFriendRequest(pseudo, this);
                    } else {
                        UiUtils
                                .makeErrorText(requireContext(), String.format(getString(R.string.must_provide_at_least_n_characters), 3),
                                               Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> {});

        builder.create().show();
    }

    private void updateColleaguesList() {
        if (PrefUtils.canSync(requireContext())) {
            mSyncLayout.setRefreshing(true);
            mStoredUserService.downloadFriendsAndRequests(this);
        }
    }

    @Override
    public void onFriendsAndRequestsReceived(FriendsAndRequestsDto friendsAndRequests) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                mColleaguesListAdapter.updateFriendsAndRequests(friendsAndRequests);
                mSyncLayout.setRefreshing(false);
            });
        }
    }

    @Override
    public void onFriendRequestSent(String friendPseudo) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                UiUtils.makeText(requireContext(), getString(R.string.sync_succeeded_message), Toast.LENGTH_LONG).show();
                updateColleaguesList();
            });
        }
    }

    @Override
    public void onFriendRequestAccepted(FriendRequestDto friendRequest) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                UiUtils.makeText(requireContext(), getString(R.string.sync_succeeded_message), Toast.LENGTH_LONG).show();
                updateColleaguesList();
            });
        }
    }

    @Override
    public void onFriendRequestRejected(FriendRequestDto friendRequest) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                UiUtils.makeText(requireContext(), getString(R.string.sync_succeeded_message), Toast.LENGTH_LONG).show();
                updateColleaguesList();
            });
        }
    }

    @Override
    public void onFriendRemoved(FriendDto friend) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                UiUtils.makeText(requireContext(), getString(R.string.sync_succeeded_message), Toast.LENGTH_LONG).show();
                updateColleaguesList();
            });
        }
    }

    @Override
    public void onError(int httpCode) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                UiUtils.makeErrorText(requireContext(), getString(R.string.sync_failed_message), Toast.LENGTH_LONG).show();
                mSyncLayout.setRefreshing(false);
            });
        }
    }
}