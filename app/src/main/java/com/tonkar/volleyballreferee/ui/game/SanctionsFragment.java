package com.tonkar.volleyballreferee.ui.game;

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
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionListener;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionService;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public class SanctionsFragment extends Fragment implements TeamListener, SanctionListener {

    private SanctionService      mSanctionService;
    private TeamService          mTeamService;
    private SanctionsListAdapter mLeftTeamSanctionsListAdapter;
    private SanctionsListAdapter mRightTeamSanctionsListAdapter;

    public SanctionsFragment() {}

    public static SanctionsFragment newInstance() {
        SanctionsFragment fragment = new SanctionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-Card", "Create sanctions fragment");
        View view = inflater.inflate(R.layout.fragment_sanctions, container, false);

        mSanctionService = ServicesProvider.getInstance().getSanctionService();
        mSanctionService.addSanctionListener(this);
        mTeamService = ServicesProvider.getInstance().getTeamService();
        mTeamService.addTeamListener(this);

        ListView leftTeamSanctionsList = view.findViewById(R.id.left_team_sanctions_list);
        mLeftTeamSanctionsListAdapter = new SanctionsListAdapter(getActivity(), inflater, mSanctionService, mTeamService, mTeamService.getTeamOnLeftSide());
        leftTeamSanctionsList.setAdapter(mLeftTeamSanctionsListAdapter);

        ListView rightTeamSanctionsList = view.findViewById(R.id.right_team_sanctions_list);
        mRightTeamSanctionsListAdapter = new SanctionsListAdapter(getActivity(), inflater, mSanctionService, mTeamService, mTeamService.getTeamOnRightSide());
        rightTeamSanctionsList.setAdapter(mRightTeamSanctionsListAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSanctionService.removeSanctionListener(this);
        mTeamService.removeTeamListener(this);
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mLeftTeamSanctionsListAdapter.setTeamType(leftTeamType);
        mRightTeamSanctionsListAdapter.setTeamType(rightTeamType);
        mLeftTeamSanctionsListAdapter.notifyDataSetChanged();
        mRightTeamSanctionsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {}

    @Override
    public void onTeamRotated(TeamType teamType) {}

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        if (teamType.equals(mLeftTeamSanctionsListAdapter.getTeamType())) {
            mLeftTeamSanctionsListAdapter.notifyDataSetChanged();
        } else {
            mRightTeamSanctionsListAdapter.notifyDataSetChanged();
        }
    }
}
