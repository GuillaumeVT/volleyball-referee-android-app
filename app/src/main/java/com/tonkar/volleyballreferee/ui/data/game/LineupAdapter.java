package com.tonkar.volleyballreferee.ui.data.game;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class LineupAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView positionTitle;
        TextView positionText;
    }

    private final LayoutInflater mLayoutInflater;
    private final Context        mContext;
    private final IBaseTeam      mTeamService;
    private final TeamType       mTeamType;
    private final int            mSetIndex;

    LineupAdapter(LayoutInflater layoutInflater, Context context, IBaseTeam teamService, TeamType teamType, int setIndex) {
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
            positionView = mLayoutInflater.inflate(R.layout.lineup_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.positionTitle = positionView.findViewById(R.id.position_title);
            viewHolder.positionText = positionView.findViewById(R.id.position);
            positionView.setTag(viewHolder);
        } else {
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
        return switch (index) {
            case 0 -> PositionType.POSITION_4;
            case 1 -> PositionType.POSITION_3;
            case 2 -> PositionType.POSITION_2;
            case 3 -> PositionType.POSITION_5;
            case 4 -> PositionType.POSITION_6;
            case 5 -> PositionType.POSITION_1;
            default -> null;
        };
    }

    private int measureTextWidth() {
        View view = mLayoutInflater.inflate(R.layout.lineup_item, null);
        TextView text = view.findViewById(R.id.position);
        text.setText("###");
        text.measure(0, 0);
        return text.getMeasuredWidth();
    }
}
