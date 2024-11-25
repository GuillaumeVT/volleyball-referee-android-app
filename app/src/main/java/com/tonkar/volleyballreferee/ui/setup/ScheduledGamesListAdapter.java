package com.tonkar.volleyballreferee.ui.setup;

import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.GameSummaryDto;
import com.tonkar.volleyballreferee.engine.game.GameStatus;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.util.*;

public class ScheduledGamesListAdapter extends ArrayAdapter<GameSummaryDto> {

    static class ViewHolder {
        TextView  summaryText;
        TextView  dateText;
        ImageView liveItem;
        ImageView kindItem;
        ImageView genderItem;
        TextView  leagueText;
    }

    private final LayoutInflater       mLayoutInflater;
    private final List<GameSummaryDto> mGameDescriptionList;
    private final List<GameSummaryDto> mFilteredGameDescriptionList;
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

    void updateGameDescriptionList(List<GameSummaryDto> gameDescriptionList) {
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
    public GameSummaryDto getItem(int index) {
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
            gameView = mLayoutInflater.inflate(R.layout.scheduled_games_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.summaryText = gameView.findViewById(R.id.scheduled_game_summary);
            viewHolder.dateText = gameView.findViewById(R.id.scheduled_game_date);
            viewHolder.liveItem = gameView.findViewById(R.id.game_status_item);
            viewHolder.kindItem = gameView.findViewById(R.id.game_kind_item);
            viewHolder.genderItem = gameView.findViewById(R.id.game_gender_item);
            viewHolder.leagueText = gameView.findViewById(R.id.scheduled_game_league);
            gameView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) gameView.getTag();
        }

        GameSummaryDto gameDescription = mFilteredGameDescriptionList.get(index);
        updateGameDescription(viewHolder, gameDescription);

        return gameView;
    }

    private void updateGameDescription(ViewHolder viewHolder, GameSummaryDto gameDescription) {
        viewHolder.summaryText.setText(String.format(Locale.getDefault(), "%s\t\t - \t\t%s", gameDescription.getHomeTeamName(),
                                                     gameDescription.getGuestTeamName()));
        viewHolder.dateText.setText(mFormatter.format(new Date(gameDescription.getScheduledAt())));

        switch (gameDescription.getGender()) {
            case MIXED -> UiUtils.colorChipIcon(getContext(), R.color.colorMixedLight, R.drawable.ic_mixed, viewHolder.genderItem);
            case LADIES -> UiUtils.colorChipIcon(getContext(), R.color.colorLadiesLight, R.drawable.ic_ladies, viewHolder.genderItem);
            case GENTS -> UiUtils.colorChipIcon(getContext(), R.color.colorGentsLight, R.drawable.ic_gents, viewHolder.genderItem);
        }

        switch (gameDescription.getKind()) {
            case INDOOR_4X4 ->
                    UiUtils.colorChipIcon(getContext(), R.color.colorIndoor4x4Light, R.drawable.ic_4x4_small, viewHolder.kindItem);
            case BEACH -> UiUtils.colorChipIcon(getContext(), R.color.colorBeachLight, R.drawable.ic_beach, viewHolder.kindItem);
            case SNOW -> UiUtils.colorChipIcon(getContext(), R.color.colorSnowLight, R.drawable.ic_snow, viewHolder.kindItem);
            default -> UiUtils.colorChipIcon(getContext(), R.color.colorIndoorLight, R.drawable.ic_6x6_small, viewHolder.kindItem);
        }

        viewHolder.liveItem.setVisibility(GameStatus.LIVE.equals(gameDescription.getStatus()) ? View.VISIBLE : View.GONE);

        if (gameDescription.getLeagueName() == null || gameDescription.getDivisionName() == null || gameDescription
                .getLeagueName()
                .isEmpty() || gameDescription.getDivisionName().isEmpty()) {
            viewHolder.leagueText.setText("");
            viewHolder.leagueText.setVisibility(View.GONE);
        } else {
            viewHolder.leagueText.setText(
                    String.format(Locale.getDefault(), "%s / %s", gameDescription.getLeagueName(), gameDescription.getDivisionName()));
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

            if (StringUtils.isBlank(prefix)) {
                results.values = mGameDescriptionList;
                results.count = mGameDescriptionList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<GameSummaryDto> matchValues = new ArrayList<>();

                for (GameSummaryDto gameDescription : mGameDescriptionList) {
                    if (lowerCaseText.isEmpty() || gameDescription
                            .getHomeTeamName()
                            .toLowerCase(Locale.getDefault())
                            .contains(lowerCaseText) || gameDescription
                            .getGuestTeamName()
                            .toLowerCase(Locale.getDefault())
                            .contains(lowerCaseText) || (gameDescription.getLeagueName() != null && gameDescription
                            .getLeagueName()
                            .toLowerCase(Locale.getDefault())
                            .contains(lowerCaseText)) || gameDescription
                            .getRefereeName()
                            .toLowerCase(Locale.getDefault())
                            .contains(lowerCaseText)) {
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
                mFilteredGameDescriptionList.addAll((Collection<? extends GameSummaryDto>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
