package com.tonkar.volleyballreferee.ui.game.sanction;

import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.team.PlayerToggleButton;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class DelaySanctionSelectionFragment extends Fragment {

    private SanctionSelectionDialogFragment mSanctionSelectionDialogFragment;
    private IGame                           mGame;
    private PlayerToggleButton              mDelayWarningButton;
    private PlayerToggleButton              mDelayPenaltyButton;
    private SanctionType                    mSelectedDelaySanction;

    public static DelaySanctionSelectionFragment newInstance(TeamType teamType) {
        DelaySanctionSelectionFragment fragment = new DelaySanctionSelectionFragment();
        Bundle args = new Bundle();
        args.putString("teamType", teamType.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public DelaySanctionSelectionFragment() {}

    void init(SanctionSelectionDialogFragment sanctionSelectionDialogFragment, IGame game) {
        mSanctionSelectionDialogFragment = sanctionSelectionDialogFragment;
        mGame = game;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TeamType teamType = TeamType.valueOf(requireArguments().getString("teamType"));

        View view = inflater.inflate(R.layout.fragment_delay_sanction_selection, container, false);

        if (mGame != null) {
            mDelayWarningButton = view.findViewById(R.id.delay_warning_button);
            mDelayPenaltyButton = view.findViewById(R.id.delay_penalty_button);

            mDelayWarningButton.setColor(getContext(), mGame.getTeamColor(teamType));
            mDelayPenaltyButton.setColor(getContext(), mGame.getTeamColor(teamType));

            mDelayWarningButton.addOnCheckedChangeListener((cButton, isChecked) -> {
                UiUtils.animate(getContext(), cButton);
                if (isChecked) {
                    mSelectedDelaySanction = SanctionType.DELAY_WARNING;
                    mDelayPenaltyButton.setChecked(false);
                    mSanctionSelectionDialogFragment.computeOkAvailability(R.id.delay_sanction_tab);
                }
            });

            mDelayPenaltyButton.addOnCheckedChangeListener((cButton, isChecked) -> {
                UiUtils.animate(getContext(), cButton);
                if (isChecked) {
                    mSelectedDelaySanction = SanctionType.DELAY_PENALTY;
                    mDelayWarningButton.setChecked(false);
                    mSanctionSelectionDialogFragment.computeOkAvailability(R.id.delay_sanction_tab);
                }
            });

            SanctionType possibleDelaySanction = mGame.getPossibleDelaySanction(teamType);

            ViewGroup delayWarningLayout = view.findViewById(R.id.delay_warning_layout);
            ViewGroup delayPenaltyLayout = view.findViewById(R.id.delay_penalty_layout);
            delayWarningLayout.setVisibility(SanctionType.DELAY_WARNING.equals(possibleDelaySanction) ? View.VISIBLE : View.GONE);
            delayPenaltyLayout.setVisibility(SanctionType.DELAY_PENALTY.equals(possibleDelaySanction) ? View.VISIBLE : View.GONE);
        }

        mSelectedDelaySanction = null;
        mSanctionSelectionDialogFragment.computeOkAvailability(R.id.delay_sanction_tab);

        return view;
    }

    SanctionType getSelectedDelaySanction() {
        return mSelectedDelaySanction;
    }
}
