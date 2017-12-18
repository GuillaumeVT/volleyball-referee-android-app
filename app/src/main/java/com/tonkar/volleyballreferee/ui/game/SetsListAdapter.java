package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BaseScoreService;
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
        TableRow             homeTeamLadder;
        TableRow             guestTeamLadder;
    }

    private final boolean          mReverseOrder;
    private final LayoutInflater   mLayoutInflater;
    private final BaseScoreService mBaseScoreService;
    private final BaseTeamService  mBaseTeamService;

    public SetsListAdapter(LayoutInflater layoutInflater, BaseScoreService baseScoreService, BaseTeamService baseTeamService, boolean reverseOrder) {
        mLayoutInflater = layoutInflater;
        mBaseScoreService = baseScoreService;
        mBaseTeamService = baseTeamService;
        mReverseOrder = reverseOrder;
    }

    @Override
    public int getCount() {
        return mBaseScoreService.getNumberOfSets();
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
            viewHolder.homeTeamLadder = setView.findViewById(R.id.set_ladder_home_team_row);
            viewHolder.guestTeamLadder = setView.findViewById(R.id.set_ladder_guest_team_row);
            setView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) setView.getTag();
        }

        int actualIndex = mReverseOrder ? (mBaseScoreService.getNumberOfSets() - index - 1) : index;

        viewHolder.setNumberText.setText(String.format(setView.getContext().getResources().getString(R.string.set_number), actualIndex+1));
        viewHolder.setScoreText.setText(String.format(Locale.getDefault(), "%d\t-\t%d", mBaseScoreService.getPoints(TeamType.HOME, actualIndex), mBaseScoreService.getPoints(TeamType.GUEST, actualIndex)));
        viewHolder.setDurationText.setText(String.format(setView.getContext().getResources().getString(R.string.set_duration), mBaseScoreService.getSetDuration(actualIndex) / 60000L));

        fillLadder(viewHolder.homeTeamLadder, viewHolder.guestTeamLadder, mBaseScoreService.getPointsLadder(actualIndex), mBaseTeamService.getTeamColor(TeamType.HOME), mBaseTeamService.getTeamColor(TeamType.GUEST));

        if (mReverseOrder && index == 0) {
            viewHolder.setLadderScroll.post(new Runnable() {
                public void run() {
                    viewHolder.setLadderScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            });
        }

        return setView;
    }

    private void fillLadder(TableRow homeTeamLadder, TableRow guestTeamLadder, List<TeamType> ladder, int homeTeamColor, int guestTeamColor) {
        homeTeamLadder.removeAllViews();
        guestTeamLadder.removeAllViews();

        int homeCount = 0;
        int guestCount = 0;
        int width = measureTextWidth();

        for (int index = 0; index < ladder.size(); index++) {
            final TeamType teamType = ladder.get(index);

            if (TeamType.HOME.equals(teamType)) {
                TextView homeText = (TextView) mLayoutInflater.inflate(R.layout.ladder_item, null);
                homeCount++;
                homeText.setText(String.valueOf(homeCount));
                UiUtils.colorTeamText(homeText.getContext(), homeTeamColor, homeText);
                homeTeamLadder.addView(homeText);
                homeText.setLayoutParams(createTableRowLayoutParams(homeText.getContext(), width, index));
            } else {
                TextView guestText = (TextView) mLayoutInflater.inflate(R.layout.ladder_item, null);
                guestCount++;
                guestText.setText(String.valueOf(guestCount));
                UiUtils.colorTeamText(guestText.getContext(), guestTeamColor, guestText);
                guestTeamLadder.addView(guestText);
                guestText.setLayoutParams(createTableRowLayoutParams(guestText.getContext(), width, index));
            }
        }
    }

    private TableRow.LayoutParams createTableRowLayoutParams(Context context, int width, int columnIndex) {
        TableRow.LayoutParams params = new TableRow.LayoutParams(width, TableRow.LayoutParams.WRAP_CONTENT);
        params.column = columnIndex;

        int margin = getTextMargin(context);
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.topMargin = margin;
        params.bottomMargin = margin;

        return params;
    }

    private int measureTextWidth() {
        TextView text = (TextView) mLayoutInflater.inflate(R.layout.ladder_item, null);
        text.setText("##");

        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        int margin = getTextMargin(text.getContext());
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.topMargin = margin;
        params.bottomMargin = margin;

        text.setLayoutParams(params);
        text.measure(0, 0);
        return text.getMeasuredWidth();
    }

    private int getTextMargin(Context context) {
        return (int) context.getResources().getDimension(R.dimen.tiny_margin_size);
    }
}
