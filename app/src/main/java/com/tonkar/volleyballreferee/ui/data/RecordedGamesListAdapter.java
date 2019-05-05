package com.tonkar.volleyballreferee.ui.data;

import android.content.Context;
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
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RecordedGamesListAdapter extends ArrayAdapter<StoredGameService> {

    static class ViewHolder {
        TextView  summaryText;
        TextView  dateText;
        TextView  scoreText;
        ImageView genderTypeImage;
        ImageView gameTypeImage;
        ImageView indexedImage;
        TextView  leagueText;
    }

    private final LayoutInflater          mLayoutInflater;
    private final List<StoredGameService> mStoredGameServiceList;
    private final List<StoredGameService> mFilteredStoredGameServiceList;
    private final DateFormat              mFormatter;
    private final NamesFilter             mNamesFilter;
    private final boolean                 mIsSyncOn;

    RecordedGamesListAdapter(Context context, LayoutInflater layoutInflater, List<StoredGameService> storedGameServiceList) {
        super(context, R.layout.recorded_games_list_item, storedGameServiceList);
        mLayoutInflater = layoutInflater;
        mStoredGameServiceList = storedGameServiceList;
        mFilteredStoredGameServiceList = new ArrayList<>();
        mFilteredStoredGameServiceList.addAll(mStoredGameServiceList);
        mFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        mFormatter.setTimeZone(TimeZone.getDefault());
        mNamesFilter = new NamesFilter();
        mIsSyncOn = PrefUtils.canSync(context);
    }

    public void updateRecordedGamesList(List<StoredGameService> storedGameServiceList) {
        mStoredGameServiceList.clear();
        mFilteredStoredGameServiceList.clear();
        mStoredGameServiceList.addAll(storedGameServiceList);
        mFilteredStoredGameServiceList.addAll(storedGameServiceList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFilteredStoredGameServiceList.size();
    }

    @Override
    public StoredGameService getItem(int index) {
        return mFilteredStoredGameServiceList.get(index);
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

        StoredGameService storedGameService = mFilteredStoredGameServiceList.get(index);
        updateGame(viewHolder, storedGameService);

        return gameView;
    }

    private void updateGame(ViewHolder viewHolder, StoredGameService storedGameService) {
        viewHolder.summaryText.setText(String.format(Locale.getDefault(),"%s\t\t%d - %d\t\t%s",
                storedGameService.getTeamName(TeamType.HOME), storedGameService.getSets(TeamType.HOME), storedGameService.getSets(TeamType.GUEST), storedGameService.getTeamName(TeamType.GUEST)));
        viewHolder.dateText.setText(mFormatter.format(new Date(storedGameService.getGameSchedule())));

        StringBuilder builder = new StringBuilder();
        for (int setIndex = 0; setIndex < storedGameService.getNumberOfSets(); setIndex++) {
            int homePoints = storedGameService.getPoints(TeamType.HOME, setIndex);
            int guestPoints = storedGameService.getPoints(TeamType.GUEST, setIndex);
            builder.append(UiUtils.formatScoreFromLocale(homePoints, guestPoints, false)).append("\t\t");
        }
        viewHolder.scoreText.setText(builder.toString());

        switch (storedGameService.getGender()) {
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

        switch (storedGameService.getKind()) {
            case INDOOR_4X4:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_4x4);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorIndoor), PorterDuff.Mode.SRC_IN));
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

        if (storedGameService.isIndexed()) {
            viewHolder.indexedImage.setImageResource(R.drawable.ic_public);
            viewHolder.indexedImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorWeb), PorterDuff.Mode.SRC_IN));
        } else {
            viewHolder.indexedImage.setImageResource(R.drawable.ic_private);
            viewHolder.indexedImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark), PorterDuff.Mode.SRC_IN));
        }

        viewHolder.indexedImage.setVisibility(mIsSyncOn ? View.VISIBLE : View.GONE);

        if (storedGameService.getLeagueName().isEmpty() || storedGameService.getDivisionName().isEmpty()) {
            viewHolder.leagueText.setText(storedGameService.getLeagueName());
        } else {
            viewHolder.leagueText.setText(String.format(Locale.getDefault(), "%s / %s" , storedGameService.getLeagueName(), storedGameService.getDivisionName()));
        }
        viewHolder.leagueText.setVisibility(storedGameService.getLeagueName().isEmpty() ? View.GONE : View.VISIBLE);
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
                results.values = mStoredGameServiceList;
                results.count = mStoredGameServiceList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<StoredGameService> matchValues = new ArrayList<>();

                for (StoredGameService storedGameService : mStoredGameServiceList) {
                    if (storedGameService.matchesFilter(lowerCaseText)) {
                        matchValues.add(storedGameService);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredStoredGameServiceList.clear();

            if (results.values != null) {
                mFilteredStoredGameServiceList.clear();
                mFilteredStoredGameServiceList.addAll((Collection<? extends StoredGameService>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
