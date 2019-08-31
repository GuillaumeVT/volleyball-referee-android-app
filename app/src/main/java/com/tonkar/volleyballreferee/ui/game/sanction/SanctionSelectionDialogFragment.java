package com.tonkar.volleyballreferee.ui.game.sanction;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.stored.api.ApiSanction;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class SanctionSelectionDialogFragment extends DialogFragment implements GameServiceHandler {

    private IGame                               mGame;
    private TeamType                            mTeamType;
    private AlertDialog                         mAlertDialog;
    private View                                mView;
    private BottomNavigationView                mSanctionTypePager;
    private DelaySanctionSelectionFragment      mDelaySanctionSelectionFragment;
    private MisconductSanctionSelectionFragment mMisconductSanctionSelectionFragment;

    public static SanctionSelectionDialogFragment newInstance(String title, TeamType teamType) {
        SanctionSelectionDialogFragment fragment = new SanctionSelectionDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("teamType", teamType.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public SanctionSelectionDialogFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mView;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        mTeamType = TeamType.valueOf(getArguments().getString("teamType"));
        String title = getArguments().getString("title");

        mView = getActivity().getLayoutInflater().inflate(R.layout.sanction_selection_dialog, null);

        mDelaySanctionSelectionFragment = DelaySanctionSelectionFragment.newInstance(mTeamType);
        mMisconductSanctionSelectionFragment = MisconductSanctionSelectionFragment.newInstance(mTeamType);

        mSanctionTypePager = mView.findViewById(R.id.sanction_nav);

        mSanctionTypePager.setOnNavigationItemSelectedListener(item -> {
                    Fragment fragment = null;

                    switch (item.getItemId()) {
                        case R.id.delay_sanction_tab:
                            fragment = mDelaySanctionSelectionFragment;
                            break;
                        case R.id.misconduct_sanction_tab:
                            fragment = mMisconductSanctionSelectionFragment;
                            break;
                        default:
                            break;
                    }

                    final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    UiUtils.animateNavigationView(transaction);
                    transaction.replace(R.id.sanction_container, fragment).commit();

                    computeOkAvailability(item.getItemId());

                    return true;
                }
        );

        mSanctionTypePager.setSelectedItemId(R.id.delay_sanction_tab);

        mAlertDialog = new AlertDialog
                .Builder(getContext(), R.style.AppTheme_Dialog)
                .setTitle(title).setView(mView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> giveSanction())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                .create();

        mAlertDialog.setOnShowListener(dialog -> computeOkAvailability(R.id.delay_sanction_tab));

        return mAlertDialog;
    }

    private void giveSanction() {
        if (mSanctionTypePager.getSelectedItemId() == R.id.delay_sanction_tab) {
            mGame.giveSanction(mTeamType, mDelaySanctionSelectionFragment.getSelectedDelaySanction(), ApiSanction.TEAM);
        } else {
            mGame.giveSanction(mTeamType, mMisconductSanctionSelectionFragment.getSelectedMisconductSanction(), mMisconductSanctionSelectionFragment.getSelectedMisconductPlayer());
        }
    }

    void computeOkAvailability(@IdRes int selectedTab) {
        if (mAlertDialog != null) {
            boolean enableOk;

            if (selectedTab == R.id.delay_sanction_tab) {
                enableOk = mDelaySanctionSelectionFragment.getSelectedDelaySanction() != null;
            } else {
                enableOk = (mMisconductSanctionSelectionFragment.getSelectedMisconductSanction() != null) && (mMisconductSanctionSelectionFragment.getSelectedMisconductPlayer() >= 0);
            }

            Button okButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            if (okButton != null) {
                okButton.setEnabled(enableOk);
            }
        }
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        if (childFragment instanceof DelaySanctionSelectionFragment) {
            DelaySanctionSelectionFragment fragment = (DelaySanctionSelectionFragment) childFragment;
            fragment.init(this, mGame);
        }
        if (childFragment instanceof MisconductSanctionSelectionFragment) {
            MisconductSanctionSelectionFragment fragment = (MisconductSanctionSelectionFragment) childFragment;
            fragment.init(this, mGame);
        }
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
    }
}
