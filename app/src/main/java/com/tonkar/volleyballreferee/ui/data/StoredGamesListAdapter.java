package com.tonkar.volleyballreferee.ui.data;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiGameDescription;
import com.tonkar.volleyballreferee.business.PrefUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class StoredGamesListAdapter extends ArrayAdapter<ApiGameDescription> {

    static class ViewHolder {
        TextView  summaryText;
        TextView  dateText;
        TextView  scoreText;
        ImageView genderTypeImage;
        ImageView gameTypeImage;
        ImageView indexedImage;
        TextView  leagueText;
    }

    private final LayoutInflater           mLayoutInflater;
    private final List<ApiGameDescription> mStoredGamesList;
    private final List<ApiGameDescription> mFilteredStoredGamesList;
    private final DateFormat               mFormatter;
    private final NamesFilter              mNamesFilter;
    private final boolean                  mIsSyncOn;

    StoredGamesListAdapter(Context context, LayoutInflater layoutInflater, List<ApiGameDescription> storedGamesList) {
        super(context, R.layout.recorded_games_list_item, storedGamesList);
        mLayoutInflater = layoutInflater;
        mStoredGamesList = storedGamesList;
        mFilteredStoredGamesList = new ArrayList<>();
        mFilteredStoredGamesList.addAll(mStoredGamesList);
        mFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        mFormatter.setTimeZone(TimeZone.getDefault());
        mNamesFilter = new NamesFilter();
        mIsSyncOn = PrefUtils.canSync(context);
    }

    void updateStoredGamesList(List<ApiGameDescription> storedGamesList) {
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
    public ApiGameDescription getItem(int index) {
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

        ApiGameDescription game = mFilteredStoredGamesList.get(index);
        updateGame(viewHolder, game);

        return gameView;
    }

    private void updateGame(ViewHolder viewHolder, ApiGameDescription game) {
        viewHolder.summaryText.setText(String.format(Locale.getDefault(),"%s\t\t%d - %d\t\t%s",
                game.getHomeTeamName(), game.getHomeSets(), game.getGuestSets(), game.getGuestTeamName()));
        viewHolder.dateText.setText(mFormatter.format(new Date(game.getScheduledAt())));

        viewHolder.scoreText.setText(game.getScore());

        switch (game.getGender()) {
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

        switch (game.getKind()) {
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

        if (game.isIndexed()) {
            viewHolder.indexedImage.setImageResource(R.drawable.ic_public);
            viewHolder.indexedImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorWeb), PorterDuff.Mode.SRC_IN));
        } else {
            viewHolder.indexedImage.setImageResource(R.drawable.ic_private);
            viewHolder.indexedImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark), PorterDuff.Mode.SRC_IN));
        }

        viewHolder.indexedImage.setVisibility(mIsSyncOn ? View.VISIBLE : View.GONE);

        if (game.getLeagueName().isEmpty() || game.getDivisionName().isEmpty()) {
            viewHolder.leagueText.setText(game.getLeagueName());
        } else {
            viewHolder.leagueText.setText(String.format(Locale.getDefault(), "%s / %s" , game.getLeagueName(), game.getDivisionName()));
        }
        viewHolder.leagueText.setVisibility(game.getLeagueName().isEmpty() ? View.GONE : View.VISIBLE);
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

                List<ApiGameDescription> matchValues = new ArrayList<>();

                for (ApiGameDescription game : mStoredGamesList) {
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
                mFilteredStoredGamesList.addAll((Collection<? extends ApiGameDescription>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
