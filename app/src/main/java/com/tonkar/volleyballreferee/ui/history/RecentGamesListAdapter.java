package com.tonkar.volleyballreferee.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RecentGamesListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView  summaryText;
        TextView  dateText;
        TextView  scoreText;
        ImageView image;
    }

    private final LayoutInflater            mLayoutInflater;
    private final List<RecordedGameService> mRecordedGameServiceList;
    private final List<RecordedGameService> mFilteredRecordedGameServiceList;
    private final DateFormat                mFormatter;

    RecentGamesListAdapter(LayoutInflater layoutInflater, List<RecordedGameService> recordedGameServiceList) {
        mLayoutInflater = layoutInflater;
        mRecordedGameServiceList = recordedGameServiceList;
        mFilteredRecordedGameServiceList = new ArrayList<>();
        mFilteredRecordedGameServiceList.addAll(mRecordedGameServiceList);
        mFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        mFormatter.setTimeZone(TimeZone.getDefault());
    }

    @Override
    public int getCount() {
        return mFilteredRecordedGameServiceList.size();
    }

    @Override
    public Object getItem(int index) {
        return mFilteredRecordedGameServiceList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        View gameView = view;
        ViewHolder viewHolder;

        if (gameView == null) {
            gameView = mLayoutInflater.inflate(R.layout.recent_games_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.summaryText = gameView.findViewById(R.id.recent_game_summary);
            viewHolder.dateText = gameView.findViewById(R.id.recent_game_date);
            viewHolder.scoreText = gameView.findViewById(R.id.recent_game_score);
            viewHolder.image = gameView.findViewById(R.id.recent_game_image);
            gameView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) gameView.getTag();
        }

        RecordedGameService recordedGameService = mFilteredRecordedGameServiceList.get(index);
        updateGame(viewHolder, recordedGameService);

        return gameView;
    }

    private void updateGame(ViewHolder viewHolder, RecordedGameService recordedGameService) {
        viewHolder.summaryText.setText(String.format(Locale.getDefault(),"%s\t\t%d\t-\t%d\t\t%s",
                recordedGameService.getTeamName(TeamType.HOME), recordedGameService.getSets(TeamType.HOME), recordedGameService.getSets(TeamType.GUEST), recordedGameService.getTeamName(TeamType.GUEST)));
        viewHolder.dateText.setText(mFormatter.format(new Date(recordedGameService.getGameDate())));

        StringBuilder builder = new StringBuilder();
        for (int setIndex = 0; setIndex < recordedGameService.getNumberOfSets(); setIndex++) {
            int homePoints = recordedGameService.getPoints(TeamType.HOME, setIndex);
            int guestPoints = recordedGameService.getPoints(TeamType.GUEST, setIndex);
            builder.append(String.valueOf(homePoints)).append('-').append(String.valueOf(guestPoints)).append("\t\t");
        }
        viewHolder.scoreText.setText(builder.toString());

        viewHolder.image.setVisibility(GameType.INDOOR.equals(recordedGameService.getGameType()) ? View.INVISIBLE : View.VISIBLE);
    }

    void filter(String text) {
        String lowerCaseText = text.toLowerCase(Locale.getDefault());

        mFilteredRecordedGameServiceList.clear();

        if (lowerCaseText.isEmpty()) {
            mFilteredRecordedGameServiceList.addAll(mRecordedGameServiceList);

        } else {
            for (RecordedGameService recordedGameService : mRecordedGameServiceList) {
                if (recordedGameService.matchesFilter(lowerCaseText)) {
                    mFilteredRecordedGameServiceList.add(recordedGameService);
                }
            }
        }

        notifyDataSetChanged();
    }

}
