package com.tonkar.volleyballreferee.ui.game.sanction;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class SanctionSelectionDialog extends DialogFragment {

    private IGame                               mGame;
    private TeamType                            mTeamType;
    private String                              mTitle;
    private AlertDialog                         mAlertDialog;
    private View                                mView;
    private BottomNavigationView                mSanctionTypePager;
    private DelaySanctionSelectionFragment      mDelaySanctionSelectionFragment;
    private MisconductSanctionSelectionFragment mMisconductSanctionSelectionFragment;

    public SanctionSelectionDialog() {}

    public SanctionSelectionDialog(String title, IGame game, TeamType teamType) {
        mGame = game;
        mTeamType = teamType;
        mTitle = title;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mView;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        mView = getActivity().getLayoutInflater().inflate(R.layout.sanction_selection_dialog, null);

        mDelaySanctionSelectionFragment = new DelaySanctionSelectionFragment();
        mMisconductSanctionSelectionFragment = new MisconductSanctionSelectionFragment();

        mDelaySanctionSelectionFragment.init(this, mGame, mTeamType);
        mMisconductSanctionSelectionFragment.init(this, mGame, mTeamType);

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
                .setTitle(mTitle).setView(mView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> giveSanction())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                .create();

        return mAlertDialog;
    }

    private void giveSanction() {
        if (mSanctionTypePager.getSelectedItemId() == R.id.delay_sanction_tab) {
            mGame.giveSanction(mTeamType, mDelaySanctionSelectionFragment.getSelectedDelaySanction(), -1);
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

            mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(enableOk);
        }
    }

}
