package com.tonkar.volleyballreferee.ui.history;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RecentGamesListAdapter extends ArrayAdapter<RecordedGameService> {

    static class ViewHolder {
        TextView  summaryText;
        TextView  dateText;
        TextView  scoreText;
        ImageView genderTypeImage;
        ImageView gameTypeImage;
    }

    private final LayoutInflater            mLayoutInflater;
    private final List<RecordedGameService> mRecordedGameServiceList;
    private final List<RecordedGameService> mFilteredRecordedGameServiceList;
    private final DateFormat                mFormatter;
    private final NamesFilter               mNamesFilter;

    RecentGamesListAdapter(Context context, LayoutInflater layoutInflater, List<RecordedGameService> recordedGameServiceList) {
        super(context, R.layout.recent_games_list_item, recordedGameServiceList);
        mLayoutInflater = layoutInflater;
        mRecordedGameServiceList = recordedGameServiceList;
        mFilteredRecordedGameServiceList = new ArrayList<>();
        mFilteredRecordedGameServiceList.addAll(mRecordedGameServiceList);
        mFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        mFormatter.setTimeZone(TimeZone.getDefault());
        mNamesFilter = new NamesFilter();
    }

    @Override
    public int getCount() {
        return mFilteredRecordedGameServiceList.size();
    }

    @Override
    public RecordedGameService getItem(int index) {
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
            viewHolder.genderTypeImage = gameView.findViewById(R.id.recent_game_gender_image);
            viewHolder.gameTypeImage = gameView.findViewById(R.id.recent_game_type_image);
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

        switch (recordedGameService.getGenderType()) {
            case MIXED:
                viewHolder.genderTypeImage.setImageResource(R.drawable.ic_mixed);
                viewHolder.genderTypeImage.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                viewHolder.genderTypeImage.setImageResource(R.drawable.ic_ladies);
                viewHolder.genderTypeImage.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                viewHolder.genderTypeImage.setImageResource(R.drawable.ic_gents);
                viewHolder.genderTypeImage.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorGents), PorterDuff.Mode.SRC_IN));
                break;
        }

        viewHolder.gameTypeImage.setVisibility(GameType.INDOOR.equals(recordedGameService.getGameType()) ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public Filter getFilter() {
        return mNamesFilter;
    }

    private class NamesFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (prefix == null || prefix.length() == 0) {
                results.values = mRecordedGameServiceList;
                results.count = mRecordedGameServiceList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<RecordedGameService> matchValues = new ArrayList<>();

                for (RecordedGameService recordedGameService : mRecordedGameServiceList) {
                    if (recordedGameService.matchesFilter(lowerCaseText)) {
                        matchValues.add(recordedGameService);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredRecordedGameServiceList.clear();

            if (results.values != null) {
                mFilteredRecordedGameServiceList.clear();
                mFilteredRecordedGameServiceList.addAll((Collection<? extends RecordedGameService>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
