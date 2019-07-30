package com.tonkar.volleyballreferee.ui.stored;

import android.content.Context;
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
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.stored.api.ApiGameSummary;

import java.text.DateFormat;
import java.util.*;

public class StoredGamesListAdapter extends ArrayAdapter<ApiGameSummary> {

    static class ViewHolder {
        TextView summaryText;
        TextView dateText;
        TextView scoreText;
        Chip     beachItem;
        Chip     indoor6x6Item;
        Chip     indoor4x4Item;
        Chip     timeItem;
        Chip     genderItem;
        Chip     indexedItem;
        TextView leagueText;
    }

    private final LayoutInflater       mLayoutInflater;
    private final List<ApiGameSummary> mStoredGamesList;
    private final List<ApiGameSummary> mFilteredStoredGamesList;
    private final DateFormat           mFormatter;
    private final NamesFilter          mNamesFilter;
    private final boolean              mIsSyncOn;

    StoredGamesListAdapter(Context context, LayoutInflater layoutInflater, List<ApiGameSummary> storedGamesList) {
        super(context, R.layout.stored_games_list_item, storedGamesList);
        mLayoutInflater = layoutInflater;
        mStoredGamesList = storedGamesList;
        mFilteredStoredGamesList = new ArrayList<>();
        mFilteredStoredGamesList.addAll(mStoredGamesList);
        mFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        mFormatter.setTimeZone(TimeZone.getDefault());
        mNamesFilter = new NamesFilter();
        mIsSyncOn = PrefUtils.canSync(context);
    }

    void updateStoredGamesList(List<ApiGameSummary> storedGamesList) {
        mStoredGamesList.clear();
        mFilteredStoredGamesList.clear();
        mStoredGamesList.addAll(storedGamesList);
        mFilteredStoredGamesList.addAll(storedGamesList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFilteredStoredGamesList.size();
    }

    @Override
    public ApiGameSummary getItem(int index) {
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
            gameView = mLayoutInflater.inflate(R.layout.stored_games_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.summaryText = gameView.findViewById(R.id.stored_game_summary);
            viewHolder.dateText = gameView.findViewById(R.id.stored_game_date);
            viewHolder.scoreText = gameView.findViewById(R.id.stored_game_score);
            viewHolder.beachItem = gameView.findViewById(R.id.beach_game_item);
            viewHolder.indoor6x6Item = gameView.findViewById(R.id.indoor_6x6_game_item);
            viewHolder.indoor4x4Item = gameView.findViewById(R.id.indoor_4x4_game_item);
            viewHolder.timeItem = gameView.findViewById(R.id.time_game_item);
            viewHolder.genderItem = gameView.findViewById(R.id.gender_game_item);
            viewHolder.indexedItem = gameView.findViewById(R.id.indexed_game_item);
            viewHolder.leagueText = gameView.findViewById(R.id.stored_game_league);
            gameView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) gameView.getTag();
        }

        ApiGameSummary game = mFilteredStoredGamesList.get(index);
        updateGame(viewHolder, game);

        return gameView;
    }

    private void updateGame(ViewHolder viewHolder, ApiGameSummary game) {
        viewHolder.summaryText.setText(String.format(Locale.getDefault(),"%s\t\t%d - %d\t\t%s",
                game.getHomeTeamName(), game.getHomeSets(), game.getGuestSets(), game.getGuestTeamName()));
        viewHolder.dateText.setText(mFormatter.format(new Date(game.getScheduledAt())));

        viewHolder.scoreText.setText(game.getScore());

        switch (game.getGender()) {
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

        switch (game.getKind()) {
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

        if (mIsSyncOn) {
            if (game.isIndexed()) {
                viewHolder.indexedItem.setChipIconResource(R.drawable.ic_public);
                viewHolder.indexedItem.setChipBackgroundColorResource(R.color.colorWebPublic);
                viewHolder.indexedItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), android.R.color.white), PorterDuff.Mode.SRC_IN));
            } else {
                viewHolder.indexedItem.setChipIconResource(R.drawable.ic_private);
                viewHolder.indexedItem.setChipBackgroundColorResource(R.color.colorWebPrivate);
                viewHolder.indexedItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), android.R.color.white), PorterDuff.Mode.SRC_IN));
            }
            viewHolder.indexedItem.setVisibility(View.VISIBLE);
        } else {
            viewHolder.indexedItem.setVisibility(View.GONE);
        }

        if (game.getLeagueName() == null || game.getDivisionName() == null || game.getLeagueName().isEmpty() || game.getDivisionName().isEmpty()) {
            viewHolder.leagueText.setText("");
        } else {
            viewHolder.leagueText.setText(String.format(Locale.getDefault(), "%s / %s" , game.getLeagueName(), game.getDivisionName()));
        }
        viewHolder.leagueText.setVisibility(game.getLeagueName() == null || game.getLeagueName().isEmpty() ? View.GONE : View.VISIBLE);
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
                results.values = mStoredGamesList;
                results.count = mStoredGamesList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<ApiGameSummary> matchValues = new ArrayList<>();

                for (ApiGameSummary game : mStoredGamesList) {
                    if (lowerCaseText.isEmpty()
                            || game.getHomeTeamName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)
                            || game.getGuestTeamName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)
                            || game.getLeagueName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)
                            || game.getRefereeName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
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
                mFilteredStoredGamesList.clear();
                mFilteredStoredGamesList.addAll((Collection<? extends ApiGameSummary>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
