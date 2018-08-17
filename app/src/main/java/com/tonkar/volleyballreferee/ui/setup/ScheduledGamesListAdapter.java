package com.tonkar.volleyballreferee.ui.setup;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.GameDescription;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ScheduledGamesListAdapter extends ArrayAdapter<GameDescription> {

    static class ViewHolder {
        TextView  summaryText;
        TextView  dateText;
        ImageView genderTypeImage;
        ImageView gameTypeImage;
        ImageView statusImage;
        TextView  leagueText;
    }

    private final LayoutInflater        mLayoutInflater;
    private final List<GameDescription> mGameDescriptionList;
    private final List<GameDescription> mFilteredGameDescriptionList;
    private final DateFormat            mFormatter;
    private final NamesFilter           mNamesFilter;

    ScheduledGamesListAdapter(LayoutInflater layoutInflater) {
        super(layoutInflater.getContext(), R.layout.scheduled_games_list_item);
        mLayoutInflater = layoutInflater;
        mGameDescriptionList = new ArrayList<>();
        mFilteredGameDescriptionList = new ArrayList<>();
        mFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        mFormatter.setTimeZone(TimeZone.getDefault());
        mNamesFilter = new NamesFilter();
    }

    public void updateGameDescriptionList(List<GameDescription> gameDescriptionList) {
        mGameDescriptionList.clear();
        mFilteredGameDescriptionList.clear();
        mGameDescriptionList.addAll(gameDescriptionList);
        mFilteredGameDescriptionList.addAll(gameDescriptionList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFilteredGameDescriptionList.size();
    }

    @Override
    public GameDescription getItem(int index) {
        return mFilteredGameDescriptionList.get(index);
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
            gameView = mLayoutInflater.inflate(R.layout.scheduled_games_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.summaryText = gameView.findViewById(R.id.scheduled_game_summary);
            viewHolder.dateText = gameView.findViewById(R.id.scheduled_game_date);
            viewHolder.genderTypeImage = gameView.findViewById(R.id.scheduled_game_gender_image);
            viewHolder.gameTypeImage = gameView.findViewById(R.id.scheduled_game_type_image);
            viewHolder.statusImage = gameView.findViewById(R.id.scheduled_game_status_image);
            viewHolder.leagueText = gameView.findViewById(R.id.scheduled_game_league);
            gameView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) gameView.getTag();
        }

        GameDescription gameDescription = mFilteredGameDescriptionList.get(index);
        updateGameDescription(viewHolder, gameDescription);

        return gameView;
    }

    private void updateGameDescription(ViewHolder viewHolder, GameDescription gameDescription) {
        viewHolder.summaryText.setText(String.format(Locale.getDefault(),"%s\t\t - \t\t%s", gameDescription.getHomeTeamName(), gameDescription.getGuestTeamName()));
        viewHolder.dateText.setText(mFormatter.format(new Date(gameDescription.getGameSchedule())));

        switch (gameDescription.getGenderType()) {
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

        switch (gameDescription.getGameType()) {
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

        switch (gameDescription.getMatchStatus()) {
            case LIVE:
                viewHolder.statusImage.setVisibility(View.VISIBLE);
                break;
            default:
                viewHolder.statusImage.setVisibility(View.GONE);
                break;
        }

        if (gameDescription.getLeagueName().isEmpty() || gameDescription.getDivisionName().isEmpty()) {
            viewHolder.leagueText.setText(gameDescription.getLeagueName());
        } else {
            viewHolder.leagueText.setText(String.format(Locale.getDefault(), "%s / %s" , gameDescription.getLeagueName(), gameDescription.getDivisionName()));
        }
        viewHolder.leagueText.setVisibility(gameDescription.getLeagueName().isEmpty() ? View.GONE : View.VISIBLE);
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
                results.values = mGameDescriptionList;
                results.count = mGameDescriptionList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<GameDescription> matchValues = new ArrayList<>();

                for (GameDescription gameDescription : mGameDescriptionList) {
                    if (lowerCaseText.isEmpty()
                            || gameDescription.getHomeTeamName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)
                            || gameDescription.getGuestTeamName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)
                            || gameDescription.getLeagueName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                        matchValues.add(gameDescription);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredGameDescriptionList.clear();

            if (results.values != null) {
                mFilteredGameDescriptionList.addAll((Collection<? extends GameDescription>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
