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
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class SubstitutionsFragment extends Fragment implements NamedGameFragment, TeamListener {

    private IndoorTeamService        mIndoorTeamService;
    private SubstitutionsListAdapter mLeftTeamSubstitutionsListAdapter;
    private SubstitutionsListAdapter mRightTeamSubstitutionsListAdapter;

    public SubstitutionsFragment() {}

    public static SubstitutionsFragment newInstance() {
        SubstitutionsFragment fragment = new SubstitutionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getGameFragmentTitle(Context context) {
        return context.getResources().getString(R.string.substitutions_tab);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-Substitutions", "Create substitutions fragment");
        View view = inflater.inflate(R.layout.fragment_substitutions, container, false);

        mIndoorTeamService = (IndoorTeamService) ServicesProvider.getInstance().getTeamService();
        mIndoorTeamService.addTeamListener(this);

        ListView leftTeamSubstitutionsList = view.findViewById(R.id.left_team_substitutions_list);
        mLeftTeamSubstitutionsListAdapter = new SubstitutionsListAdapter(getActivity(), inflater, mIndoorTeamService, mIndoorTeamService.getTeamOnLeftSide(), mIndoorTeamService.getTeamOnLeftSide());
        leftTeamSubstitutionsList.setAdapter(mLeftTeamSubstitutionsListAdapter);

        ListView rightTeamSubstitutionsList = view.findViewById(R.id.right_team_substitutions_list);
        mRightTeamSubstitutionsListAdapter = new SubstitutionsListAdapter(getActivity(), inflater, mIndoorTeamService, mIndoorTeamService.getTeamOnRightSide(), mIndoorTeamService.getTeamOnLeftSide());
        rightTeamSubstitutionsList.setAdapter(mRightTeamSubstitutionsListAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIndoorTeamService.removeTeamListener(this);
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mLeftTeamSubstitutionsListAdapter.setTeamType(leftTeamType);
        mLeftTeamSubstitutionsListAdapter.setTeamOnLeftSide(leftTeamType);
        mRightTeamSubstitutionsListAdapter.setTeamType(rightTeamType);
        mRightTeamSubstitutionsListAdapter.setTeamOnLeftSide(leftTeamType);
        mLeftTeamSubstitutionsListAdapter.notifyDataSetChanged();
        mRightTeamSubstitutionsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (ActionOriginType.USER.equals(actionOriginType)) {
            if (teamType.equals(mLeftTeamSubstitutionsListAdapter.getTeamType())) {
                mLeftTeamSubstitutionsListAdapter.notifyDataSetChanged();
            } else {
                mRightTeamSubstitutionsListAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType) {}

}
