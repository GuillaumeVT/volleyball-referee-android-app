package com.tonkar.volleyballreferee.ui.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class LineupAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView positionTitle;
        TextView positionText;
    }

    private final LayoutInflater  mLayoutInflater;
    private final Context         mContext;
    private final BaseTeamService mTeamService;
    private final TeamType        mTeamType;
    private final int             mSetIndex;

    LineupAdapter(LayoutInflater layoutInflater, Context context, BaseTeamService teamService, TeamType teamType, int setIndex) {
        mLayoutInflater = layoutInflater;
        mContext = context;
        mTeamService = teamService;
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
    public View getView(int index, View view, ViewGroup parent) {
        View positionView = view;
        final ViewHolder viewHolder;

        if (positionView == null) {
            positionView = mLayoutInflater.inflate(R.layout.lineup_item, null);
            viewHolder = new ViewHolder();
            viewHolder.positionTitle = positionView.findViewById(R.id.position_title);
            viewHolder.positionText = positionView.findViewById(R.id.position);
            positionView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) positionView.getTag();
        }

        PositionType positionType = viewIndexToPosition(index);

        if (isVisible(positionType)) {
            int number = mTeamService.getPlayerAtPositionInStartingLineup(mTeamType, positionType, mSetIndex);
            viewHolder.positionText.setWidth(measureTextWidth());
            viewHolder.positionText.setText(UiUtils.formatNumberFromLocale(number));
            UiUtils.styleTeamText(mContext, mTeamService, mTeamType, number, viewHolder.positionText);
            viewHolder.positionTitle.setText(UiUtils.getPositionTitle(mContext, positionType));
            viewHolder.positionText.setVisibility(View.VISIBLE);
            viewHolder.positionTitle.setVisibility(View.VISIBLE);
        } else {
            viewHolder.positionText.setVisibility(View.INVISIBLE);
            viewHolder.positionTitle.setVisibility(View.INVISIBLE);
        }

        return positionView;
    }

    protected boolean isVisible(PositionType positionType) {
        return true;
    }

    protected PositionType viewIndexToPosition(int index) {
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

    private int measureTextWidth() {
        View view = mLayoutInflater.inflate(R.layout.lineup_item, null);
        TextView text = view.findViewById(R.id.position) ;
        text.setText("###");
        text.measure(0, 0);
        return text.getMeasuredWidth();
    }
}
