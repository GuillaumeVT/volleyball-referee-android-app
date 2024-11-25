package com.tonkar.volleyballreferee.ui.data.game;

import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.GameSummaryDto;
import com.tonkar.volleyballreferee.ui.data.SelectableArrayAdapter;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.util.*;

public class StoredGamesListAdapter extends SelectableArrayAdapter<GameSummaryDto> {

    static class ViewHolder {
        MaterialCardView listItemCard;
        TextView         summaryText;
        TextView         dateText;
        TextView         scoreText;
        ImageView        kindItem;
        ImageView        genderItem;
        TextView         leagueText;
    }

    private final LayoutInflater       mLayoutInflater;
    private final List<GameSummaryDto> mStoredGamesList;
    private final List<GameSummaryDto> mFilteredStoredGamesList;
    private final DateFormat           mFormatter;
    private final NamesFilter          mNamesFilter;

    StoredGamesListAdapter(Context context, LayoutInflater layoutInflater, List<GameSummaryDto> storedGamesList) {
        super(context, R.layout.stored_games_list_item, storedGamesList);
        mLayoutInflater = layoutInflater;
        mStoredGamesList = storedGamesList;
        mFilteredStoredGamesList = new ArrayList<>();
        mFilteredStoredGamesList.addAll(mStoredGamesList);
        mFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        mFormatter.setTimeZone(TimeZone.getDefault());
        mNamesFilter = new NamesFilter();
    }

    void updateStoredGamesList(List<GameSummaryDto> storedGamesList) {
        mStoredGamesList.clear();
        mFilteredStoredGamesList.clear();
        mStoredGamesList.addAll(storedGamesList);
        mFilteredStoredGamesList.addAll(storedGamesList);
        clearSelectedItems();
    }

    @Override
    public int getCount() {
        return mFilteredStoredGamesList.size();
    }

    @Override
    public GameSummaryDto getItem(int index) {
        return mFilteredStoredGamesList.get(index);
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
            gameView = mLayoutInflater.inflate(R.layout.stored_games_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.listItemCard = gameView.findViewById(R.id.list_item_card);
            viewHolder.summaryText = gameView.findViewById(R.id.stored_game_summary);
            viewHolder.dateText = gameView.findViewById(R.id.stored_game_date);
            viewHolder.scoreText = gameView.findViewById(R.id.stored_game_score);
            viewHolder.kindItem = gameView.findViewById(R.id.game_kind_item);
            viewHolder.genderItem = gameView.findViewById(R.id.game_gender_item);
            viewHolder.leagueText = gameView.findViewById(R.id.stored_game_league);
            gameView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) gameView.getTag();
        }

        GameSummaryDto game = mFilteredStoredGamesList.get(index);
        updateGame(viewHolder, game);

        return gameView;
    }

    private void updateGame(ViewHolder viewHolder, GameSummaryDto game) {
        viewHolder.summaryText.setText(
                String.format(Locale.getDefault(), "%s\t\t%d - %d\t\t%s", game.getHomeTeamName(), game.getHomeSets(), game.getGuestSets(),
                              game.getGuestTeamName()));
        viewHolder.dateText.setText(mFormatter.format(new Date(game.getScheduledAt())));

        viewHolder.scoreText.setText(game.getScore());

        switch (game.getGender()) {
            case MIXED -> UiUtils.colorChipIcon(getContext(), R.color.colorMixedLight, R.drawable.ic_mixed, viewHolder.genderItem);
            case LADIES -> UiUtils.colorChipIcon(getContext(), R.color.colorLadiesLight, R.drawable.ic_ladies, viewHolder.genderItem);
            case GENTS -> UiUtils.colorChipIcon(getContext(), R.color.colorGentsLight, R.drawable.ic_gents, viewHolder.genderItem);
        }

        switch (game.getKind()) {
            case INDOOR_4X4 ->
                    UiUtils.colorChipIcon(getContext(), R.color.colorIndoor4x4Light, R.drawable.ic_4x4_small, viewHolder.kindItem);
            case BEACH -> UiUtils.colorChipIcon(getContext(), R.color.colorBeachLight, R.drawable.ic_beach, viewHolder.kindItem);
            case SNOW -> UiUtils.colorChipIcon(getContext(), R.color.colorSnowLight, R.drawable.ic_snow, viewHolder.kindItem);
            default -> UiUtils.colorChipIcon(getContext(), R.color.colorIndoorLight, R.drawable.ic_6x6_small, viewHolder.kindItem);
        }

        if (game.getLeagueName() == null || game.getDivisionName() == null || game.getLeagueName().isEmpty() || game
                .getDivisionName()
                .isEmpty()) {
            viewHolder.leagueText.setText("");
        } else {
            viewHolder.leagueText.setText(String.format(Locale.getDefault(), "%s / %s", game.getLeagueName(), game.getDivisionName()));
        }
        viewHolder.leagueText.setVisibility(game.getLeagueName() == null || game.getLeagueName().isEmpty() ? View.GONE : View.VISIBLE);

        viewHolder.listItemCard.setCardBackgroundColor(
                ContextCompat.getColor(getContext(), isSelectedItem(game.getId()) ? R.color.colorSelectedItem : R.color.colorSurface));
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
                results.values = mStoredGamesList;
                results.count = mStoredGamesList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<GameSummaryDto> matchValues = new ArrayList<>();

                for (GameSummaryDto game : mStoredGamesList) {
                    if (lowerCaseText.isEmpty() || game.getHomeTeamName().toLowerCase(Locale.getDefault()).contains(lowerCaseText) || game
                            .getGuestTeamName()
                            .toLowerCase(Locale.getDefault())
                            .contains(lowerCaseText) || game
                            .getLeagueName()
                            .toLowerCase(Locale.getDefault())
                            .contains(lowerCaseText) || game.getRefereeName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                        matchValues.add(game);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredStoredGamesList.clear();

            if (results.values != null) {
                mFilteredStoredGamesList.addAll((Collection<? extends GameSummaryDto>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
