package com.tonkar.volleyballreferee.ui.setup;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.game.GameStatus;
import com.tonkar.volleyballreferee.engine.stored.api.ApiGameSummary;

import java.text.DateFormat;
import java.util.*;

public class ScheduledGamesListAdapter extends ArrayAdapter<ApiGameSummary> {

    static class ViewHolder {
        TextView  summaryText;
        TextView  dateText;
        ImageView genderTypeImage;
        ImageView gameTypeImage;
        ImageView statusImage;
        TextView  leagueText;
    }

    private final LayoutInflater       mLayoutInflater;
    private final List<ApiGameSummary> mGameDescriptionList;
    private final List<ApiGameSummary> mFilteredGameDescriptionList;
    private final DateFormat           mFormatter;
    private final NamesFilter          mNamesFilter;

    ScheduledGamesListAdapter(LayoutInflater layoutInflater) {
        super(layoutInflater.getContext(), R.layout.scheduled_games_list_item);
        mLayoutInflater = layoutInflater;
        mGameDescriptionList = new ArrayList<>();
        mFilteredGameDescriptionList = new ArrayList<>();
        mFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        mFormatter.setTimeZone(TimeZone.getDefault());
        mNamesFilter = new NamesFilter();
    }

    void updateGameDescriptionList(List<ApiGameSummary> gameDescriptionList) {
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
    public ApiGameSummary getItem(int index) {
        return mFilteredGameDescriptionList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
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

        ApiGameSummary gameDescription = mFilteredGameDescriptionList.get(index);
        updateGameDescription(viewHolder, gameDescription);

        return gameView;
    }

    private void updateGameDescription(ViewHolder viewHolder, ApiGameSummary gameDescription) {
        viewHolder.summaryText.setText(String.format(Locale.getDefault(),"%s\t\t - \t\t%s", gameDescription.getHomeTeamName(), gameDescription.getGuestTeamName()));
        viewHolder.dateText.setText(mFormatter.format(new Date(gameDescription.getScheduledAt())));

        switch (gameDescription.getGender()) {
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

        switch (gameDescription.getKind()) {
            case INDOOR_4X4:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_4x4);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorIndoor4x4), PorterDuff.Mode.SRC_IN));
                break;
            case BEACH:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_sun);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorBeach), PorterDuff.Mode.SRC_IN));
                break;
            case TIME:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_time_based);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorTime), PorterDuff.Mode.SRC_IN));
                break;
            case INDOOR:
            default:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_6x6);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorIndoor), PorterDuff.Mode.SRC_IN));
                break;
        }

        if (GameStatus.LIVE.equals(gameDescription.getStatus())) {
            viewHolder.statusImage.setVisibility(View.VISIBLE);
        } else {
            viewHolder.statusImage.setVisibility(View.GONE);
        }

        if (gameDescription.getLeagueName() == null || gameDescription.getDivisionName() == null || gameDescription.getLeagueName().isEmpty() || gameDescription.getDivisionName().isEmpty()) {
            viewHolder.leagueText.setText("");
            viewHolder.leagueText.setVisibility(View.GONE);
        } else {
            viewHolder.leagueText.setText(String.format(Locale.getDefault(), "%s / %s" , gameDescription.getLeagueName(), gameDescription.getDivisionName()));
            viewHolder.leagueText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public @NonNull Filter getFilter() {
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

                List<ApiGameSummary> matchValues = new ArrayList<>();

                for (ApiGameSummary gameDescription : mGameDescriptionList) {
                    if (lowerCaseText.isEmpty()
                            || gameDescription.getHomeTeamName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)
                            || gameDescription.getGuestTeamName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)
                            || (gameDescription.getLeagueName() != null && gameDescription.getLeagueName().toLowerCase(Locale.getDefault()).contains(lowerCaseText))
                            || gameDescription.getRefereeName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
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
                mFilteredGameDescriptionList.addAll((Collection<? extends ApiGameSummary>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
