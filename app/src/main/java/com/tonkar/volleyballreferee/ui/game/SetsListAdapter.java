package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BaseGameService;
import com.tonkar.volleyballreferee.interfaces.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.List;
import java.util.Locale;

public class SetsListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView             setNumberText;
        TextView             setScoreText;
        TextView             setDurationText;
        HorizontalScrollView setLadderScroll;
        GridLayout           setLadderGrid;
    }

    private final boolean         mReverseOrder;
    private final LayoutInflater  mLayoutInflater;
    private final BaseGameService mBaseGameService;
    private final BaseTeamService mBaseTeamService;

    public SetsListAdapter(LayoutInflater layoutInflater, BaseGameService baseGameService, BaseTeamService baseTeamService, boolean reverseOrder) {
        mLayoutInflater = layoutInflater;
        mBaseGameService = baseGameService;
        mBaseTeamService = baseTeamService;
        mReverseOrder = reverseOrder;
    }

    @Override
    public int getCount() {
        return mBaseGameService.getNumberOfSets();
    }

    @Override
    public Object getItem(int index) {
        return null;
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        View setView = view;
        final ViewHolder viewHolder;

        if (setView == null) {
            setView = mLayoutInflater.inflate(R.layout.ladder_with_title_item, null);
            viewHolder = new ViewHolder();
            viewHolder.setNumberText = setView.findViewById(R.id.set_number_text);
            viewHolder.setScoreText = setView.findViewById(R.id.set_score_text);
            viewHolder.setDurationText = setView.findViewById(R.id.set_duration_text);
            viewHolder.setLadderScroll = setView.findViewById(R.id.set_ladder_scroll);
            viewHolder.setLadderGrid = setView.findViewById(R.id.set_ladder_grid);
            setView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) setView.getTag();
        }

        int actualIndex = mReverseOrder ? (mBaseGameService.getNumberOfSets() - index - 1) : index;

        viewHolder.setNumberText.setText(String.format(setView.getContext().getResources().getString(R.string.set_number), actualIndex+1));
        viewHolder.setScoreText.setText(String.format(Locale.getDefault(), "%d\t-\t%d", mBaseGameService.getPoints(TeamType.HOME, actualIndex), mBaseGameService.getPoints(TeamType.GUEST, actualIndex)));
        viewHolder.setDurationText.setText(String.format(setView.getContext().getResources().getString(R.string.set_duration), mBaseGameService.getSetDuration(actualIndex) / 60000L));

        fillLadderGrid(viewHolder.setLadderGrid, mBaseGameService.getPointsLadder(actualIndex), mBaseTeamService.getTeamColor(TeamType.HOME), mBaseTeamService.getTeamColor(TeamType.GUEST));

        if (mReverseOrder && index == 0) {
            viewHolder.setLadderScroll.post(new Runnable() {
                public void run() {
                    viewHolder.setLadderScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            });
        }

        return setView;
    }

    private void fillLadderGrid(GridLayout setLadderGrid, List<TeamType> ladder, int homeTeamColor, int guestTeamColor) {
        setLadderGrid.removeAllViews();
        Context context = setLadderGrid.getContext();
        int homeCount = 0;
        int guestCount = 0;

        for (int index = 0; index < ladder.size(); index++) {
            TextView homeText = new TextView(context);
            applyStyle(homeText, context, homeTeamColor);
            setLadderGrid.addView(homeText, createGridLayoutParams(0, index));

            TextView guestText = new TextView(context);
            applyStyle(guestText, context, guestTeamColor);
            setLadderGrid.addView(guestText, createGridLayoutParams(1, index));

            final TeamType teamType = ladder.get(index);

            if (TeamType.HOME.equals(teamType)) {
                homeCount++;
                homeText.setText(String.valueOf(homeCount));
                guestText.setText("");

                homeText.setVisibility(View.VISIBLE);
                guestText.setVisibility(View.INVISIBLE);
            } else {
                guestCount++;
                homeText.setText("");
                guestText.setText(String.valueOf(guestCount));

                homeText.setVisibility(View.INVISIBLE);
                guestText.setVisibility(View.VISIBLE);
            }
        }
    }

    private void applyStyle(final TextView textView, final Context context, int color) {
        textView.setTextAppearance(context, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Headline);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(UiUtils.getTextColor(context, color));
        textView.setBackgroundColor(color);
        textView.setTextSize(18);
    }

    private GridLayout.LayoutParams createGridLayoutParams(final int rowIndex, final int columnIndex) {
        final GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.columnSpec = GridLayout.spec(columnIndex, GridLayout.FILL);
        params.rowSpec = GridLayout.spec(rowIndex, GridLayout.FILL);
        params.leftMargin = 4;
        params.rightMargin = 4;
        params.topMargin = 4;
        params.bottomMargin = 4;
        params.width = 80;

        return params;
    }
}
