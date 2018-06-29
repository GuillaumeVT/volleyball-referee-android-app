package com.tonkar.volleyballreferee.ui.data;

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
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RecordedGamesListAdapter extends ArrayAdapter<RecordedGameService> {

    static class ViewHolder {
        TextView  summaryText;
        TextView  dateText;
        TextView  scoreText;
        ImageView genderTypeImage;
        ImageView gameTypeImage;
        ImageView indexedImage;
        TextView  leagueText;
    }

    private final LayoutInflater            mLayoutInflater;
    private final List<RecordedGameService> mRecordedGameServiceList;
    private final List<RecordedGameService> mFilteredRecordedGameServiceList;
    private final DateFormat                mFormatter;
    private final NamesFilter               mNamesFilter;
    private final boolean                   mIsSyncOn;

    RecordedGamesListAdapter(Context context, LayoutInflater layoutInflater, List<RecordedGameService> recordedGameServiceList) {
        super(context, R.layout.recorded_games_list_item, recordedGameServiceList);
        mLayoutInflater = layoutInflater;
        mRecordedGameServiceList = recordedGameServiceList;
        mFilteredRecordedGameServiceList = new ArrayList<>();
        mFilteredRecordedGameServiceList.addAll(mRecordedGameServiceList);
        mFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        mFormatter.setTimeZone(TimeZone.getDefault());
        mNamesFilter = new NamesFilter();
        mIsSyncOn = PrefUtils.isSyncOn(context);
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
            gameView = mLayoutInflater.inflate(R.layout.recorded_games_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.summaryText = gameView.findViewById(R.id.recorded_game_summary);
            viewHolder.dateText = gameView.findViewById(R.id.recorded_game_date);
            viewHolder.scoreText = gameView.findViewById(R.id.recorded_game_score);
            viewHolder.genderTypeImage = gameView.findViewById(R.id.recorded_game_gender_image);
            viewHolder.gameTypeImage = gameView.findViewById(R.id.recorded_game_type_image);
            viewHolder.indexedImage = gameView.findViewById(R.id.recorded_game_indexed_image);
            viewHolder.leagueText = gameView.findViewById(R.id.recorded_game_league);
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
        viewHolder.summaryText.setText(String.format(Locale.getDefault(),"%s\t\t%d - %d\t\t%s",
                recordedGameService.getTeamName(TeamType.HOME), recordedGameService.getSets(TeamType.HOME), recordedGameService.getSets(TeamType.GUEST), recordedGameService.getTeamName(TeamType.GUEST)));
        viewHolder.dateText.setText(mFormatter.format(new Date(recordedGameService.getGameSchedule())));

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
                viewHolder.genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                viewHolder.genderTypeImage.setImageResource(R.drawable.ic_ladies);
                viewHolder.genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                viewHolder.genderTypeImage.setImageResource(R.drawable.ic_gents);
                viewHolder.genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorGents), PorterDuff.Mode.SRC_IN));
                break;
        }

        switch (recordedGameService.getGameType()) {
            case INDOOR_4X4:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_4x4);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
                break;
            case BEACH:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_sun);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorBeach), PorterDuff.Mode.SRC_IN));
                break;
            case TIME:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_time_based);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimaryText), PorterDuff.Mode.SRC_IN));
                break;
            case INDOOR:
            default:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_6x6);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
                break;
        }

        if (recordedGameService.isIndexed()) {
            viewHolder.indexedImage.setImageResource(R.drawable.ic_public);
            viewHolder.indexedImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), android.R.color.holo_blue_dark), PorterDuff.Mode.SRC_IN));
        } else {
            viewHolder.indexedImage.setImageResource(R.drawable.ic_private);
            viewHolder.indexedImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark), PorterDuff.Mode.SRC_IN));
        }

        viewHolder.indexedImage.setVisibility(mIsSyncOn ? View.VISIBLE : View.GONE);

        if (recordedGameService.getLeagueName().isEmpty() || recordedGameService.getDivisionName().isEmpty()) {
            viewHolder.leagueText.setText(recordedGameService.getLeagueName());
        } else {
            viewHolder.leagueText.setText(String.format(Locale.getDefault(), "%s / %s" , recordedGameService.getLeagueName(), recordedGameService.getDivisionName()));
        }
        viewHolder.leagueText.setVisibility(recordedGameService.getLeagueName().isEmpty() ? View.GONE : View.VISIBLE);
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
