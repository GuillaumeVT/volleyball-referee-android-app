package com.tonkar.volleyballreferee.ui.setup;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.google.android.material.chip.Chip;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.game.GameStatus;
import com.tonkar.volleyballreferee.engine.stored.api.ApiGameSummary;

import java.text.DateFormat;
import java.util.*;

public class ScheduledGamesListAdapter extends ArrayAdapter<ApiGameSummary> {

    static class ViewHolder {
        TextView summaryText;
        TextView dateText;
        Chip     liveItem;
        Chip     beachItem;
        Chip     indoor6x6Item;
        Chip     indoor4x4Item;
        Chip     timeItem;
        Chip     genderItem;
        TextView leagueText;
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
            viewHolder.liveItem = gameView.findViewById(R.id.live_game_item);
            viewHolder.beachItem = gameView.findViewById(R.id.beach_game_item);
            viewHolder.indoor6x6Item = gameView.findViewById(R.id.indoor_6x6_game_item);
            viewHolder.indoor4x4Item = gameView.findViewById(R.id.indoor_4x4_game_item);
            viewHolder.timeItem = gameView.findViewById(R.id.time_game_item);
            viewHolder.genderItem = gameView.findViewById(R.id.gender_game_item);
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
                viewHolder.genderItem.setChipIconResource(R.drawable.ic_mixed);
                viewHolder.genderItem.setChipBackgroundColorResource(R.color.colorMixed);
                viewHolder.genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), android.R.color.white), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                viewHolder.genderItem.setChipIconResource(R.drawable.ic_ladies);
                viewHolder.genderItem.setChipBackgroundColorResource(R.color.colorLadies);
                viewHolder.genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), android.R.color.white), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                viewHolder.genderItem.setChipIconResource(R.drawable.ic_gents);
                viewHolder.genderItem.setChipBackgroundColorResource(R.color.colorGents);
                viewHolder.genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), android.R.color.white), PorterDuff.Mode.SRC_IN));
                break;
        }

        switch (gameDescription.getKind()) {
            case INDOOR_4X4:
                viewHolder.beachItem.setVisibility(View.GONE);
                viewHolder.indoor6x6Item.setVisibility(View.GONE);
                viewHolder.indoor4x4Item.setVisibility(View.VISIBLE);
                viewHolder.timeItem.setVisibility(View.GONE);
                break;
            case BEACH:
                viewHolder.beachItem.setVisibility(View.VISIBLE);
                viewHolder.indoor6x6Item.setVisibility(View.GONE);
                viewHolder.indoor4x4Item.setVisibility(View.GONE);
                viewHolder.timeItem.setVisibility(View.GONE);
                break;
            case TIME:
                viewHolder.beachItem.setVisibility(View.GONE);
                viewHolder.indoor6x6Item.setVisibility(View.GONE);
                viewHolder.indoor4x4Item.setVisibility(View.GONE);
                viewHolder.timeItem.setVisibility(View.VISIBLE);
                break;
            case INDOOR:
            default:
                viewHolder.beachItem.setVisibility(View.GONE);
                viewHolder.indoor6x6Item.setVisibility(View.VISIBLE);
                viewHolder.indoor4x4Item.setVisibility(View.GONE);
                viewHolder.timeItem.setVisibility(View.GONE);
                break;
        }

        viewHolder.liveItem.setVisibility(GameStatus.LIVE.equals(gameDescription.getStatus()) ? View.VISIBLE : View.GONE);

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
