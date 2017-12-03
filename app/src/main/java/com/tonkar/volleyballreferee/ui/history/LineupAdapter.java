package com.tonkar.volleyballreferee.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

public class LineupAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView positionTitle;
        Button   positionButton;
    }

    private final LayoutInflater        mLayoutInflater;
    private final Context               mContext;
    private final BaseIndoorTeamService mIndoorTeamService;
    private final TeamType              mTeamType;
    private final int                   mSetIndex;

    LineupAdapter(LayoutInflater layoutInflater, Context context, BaseIndoorTeamService indoorTeamService, TeamType teamType, int setIndex) {
        mLayoutInflater = layoutInflater;
        mContext = context;
        mIndoorTeamService = indoorTeamService;
        mTeamType = teamType;
        mSetIndex = setIndex;
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        View positionView = view;
        final ViewHolder viewHolder;

        if (positionView == null) {
            positionView = mLayoutInflater.inflate(R.layout.lineup_item, null);
            viewHolder = new ViewHolder();
            viewHolder.positionTitle = positionView.findViewById(R.id.position_title);
            viewHolder.positionButton = positionView.findViewById(R.id.position);
            positionView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) positionView.getTag();
        }

        UiUtils.colorTeamButton(mContext, mIndoorTeamService.getTeamColor(mTeamType), viewHolder.positionButton);

        PositionType positionType = viewIndexToPosition(index);
        int number = mIndoorTeamService.getPlayerAtPositionInStartingLineup(mTeamType, positionType, mSetIndex);
        viewHolder.positionButton.setText(String.valueOf(number));

        switch (positionType) {
            case POSITION_1:
                viewHolder.positionTitle.setText("I");
                break;
            case POSITION_2:
                viewHolder.positionTitle.setText("II");
                break;
            case POSITION_3:
                viewHolder.positionTitle.setText("III");
                break;
            case POSITION_4:
                viewHolder.positionTitle.setText("IV");
                break;
            case POSITION_5:
                viewHolder.positionTitle.setText("V");
                break;
            case POSITION_6:
                viewHolder.positionTitle.setText("VI");
                break;
        }

        return positionView;
    }

    private PositionType viewIndexToPosition(int index) {
        PositionType positionType = null;

        switch (index) {
            case 0:
                positionType = PositionType.POSITION_4;
                break;
            case 1:
                positionType = PositionType.POSITION_3;
                break;
            case 2:
                positionType = PositionType.POSITION_2;
                break;
            case 3:
                positionType = PositionType.POSITION_5;
                break;
            case 4:
                positionType = PositionType.POSITION_6;
                break;
            case 5:
                positionType = PositionType.POSITION_1;
                break;
        }

        return positionType;
    }
}
