package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.card.PenaltyCardListener;
import com.tonkar.volleyballreferee.interfaces.card.PenaltyCardService;
import com.tonkar.volleyballreferee.interfaces.card.PenaltyCardType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public class PenaltyCardsFragment extends Fragment implements NamedGameFragment, TeamListener, PenaltyCardListener {

    private PenaltyCardService      mPenaltyCardService;
    private TeamService             mTeamService;
    private PenaltyCardsListAdapter mLeftTeamPenaltyCardsListAdapter;
    private PenaltyCardsListAdapter mRightTeamPenaltyCardsListAdapter;

    public PenaltyCardsFragment() {}

    public static PenaltyCardsFragment newInstance() {
        PenaltyCardsFragment fragment = new PenaltyCardsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getGameFragmentTitle(Context context) {
        return context.getResources().getString(R.string.penalty_cards_tab);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-Card", "Create penalty cards fragment");
        View view = inflater.inflate(R.layout.fragment_penalty_cards, container, false);

        mPenaltyCardService = ServicesProvider.getInstance().getPenaltyCardService();
        mPenaltyCardService.addPenaltyCardListener(this);
        mTeamService = ServicesProvider.getInstance().getTeamService();
        mTeamService.addTeamListener(this);

        ListView leftTeamPenaltyCardsList = view.findViewById(R.id.left_team_penalty_cards_list);
        mLeftTeamPenaltyCardsListAdapter = new PenaltyCardsListAdapter(getActivity(), inflater, mPenaltyCardService, mTeamService, mTeamService.getTeamOnLeftSide());
        leftTeamPenaltyCardsList.setAdapter(mLeftTeamPenaltyCardsListAdapter);

        ListView rightTeamPenaltyCardsList = view.findViewById(R.id.right_team_penalty_cards_list);
        mRightTeamPenaltyCardsListAdapter = new PenaltyCardsListAdapter(getActivity(), inflater, mPenaltyCardService, mTeamService, mTeamService.getTeamOnRightSide());
        rightTeamPenaltyCardsList.setAdapter(mRightTeamPenaltyCardsListAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPenaltyCardService.removePenaltyCardListener(this);
        mTeamService.removeTeamListener(this);
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mLeftTeamPenaltyCardsListAdapter.setTeamType(leftTeamType);
        mRightTeamPenaltyCardsListAdapter.setTeamType(rightTeamType);
        mLeftTeamPenaltyCardsListAdapter.notifyDataSetChanged();
        mRightTeamPenaltyCardsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {}

    @Override
    public void onTeamRotated(TeamType teamType) {}

    @Override
    public void onPenaltyCard(TeamType teamType, PenaltyCardType penaltyCardType, int number) {
        if (teamType.equals(mLeftTeamPenaltyCardsListAdapter.getTeamType())) {
            mLeftTeamPenaltyCardsListAdapter.notifyDataSetChanged();
        } else {
            mRightTeamPenaltyCardsListAdapter.notifyDataSetChanged();
        }
    }
}
