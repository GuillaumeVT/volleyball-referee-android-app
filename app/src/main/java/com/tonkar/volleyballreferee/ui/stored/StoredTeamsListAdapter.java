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
import com.tonkar.volleyballreferee.engine.stored.api.ApiTeamSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class StoredTeamsListAdapter extends ArrayAdapter<ApiTeamSummary> {

    static class ViewHolder {
        TextView nameText;
        Chip     kindItem;
        Chip     genderItem;
    }

    private final LayoutInflater       mLayoutInflater;
    private final List<ApiTeamSummary> mStoredTeamsList;
    private final List<ApiTeamSummary> mFilteredStoredTeamsList;
    private final NameFilter           mNameFilter;

    StoredTeamsListAdapter(Context context, LayoutInflater layoutInflater, List<ApiTeamSummary> storedTeamsList) {
        super(context, R.layout.stored_teams_list_item, storedTeamsList);
        mLayoutInflater = layoutInflater;
        mStoredTeamsList = storedTeamsList;
        mFilteredStoredTeamsList = new ArrayList<>();
        mFilteredStoredTeamsList.addAll(mStoredTeamsList);
        mNameFilter = new NameFilter();
    }

    void updateStoredTeamsList(List<ApiTeamSummary> storedTeamsList) {
        mStoredTeamsList.clear();
        mFilteredStoredTeamsList.clear();
        mStoredTeamsList.addAll(storedTeamsList);
        mFilteredStoredTeamsList.addAll(storedTeamsList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFilteredStoredTeamsList.size();
    }

    @Override
    public ApiTeamSummary getItem(int index) {
        return mFilteredStoredTeamsList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
        View storedTeamView = view;
        ViewHolder viewHolder;

        if (storedTeamView == null) {
            storedTeamView = mLayoutInflater.inflate(R.layout.stored_teams_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.nameText = storedTeamView.findViewById(R.id.stored_team_name);
            viewHolder.kindItem = storedTeamView.findViewById(R.id.team_kind_item);
            viewHolder.genderItem = storedTeamView.findViewById(R.id.team_gender_item);
            storedTeamView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) storedTeamView.getTag();
        }

        ApiTeamSummary team = mFilteredStoredTeamsList.get(index);
        viewHolder.nameText.setText(team.getName());

        switch (team.getGender()) {
            case MIXED:
                viewHolder.genderItem.setChipIconResource(R.drawable.ic_mixed);
                viewHolder.genderItem.setChipBackgroundColorResource(R.color.colorMixedLight);
                viewHolder.genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                viewHolder.genderItem.setChipIconResource(R.drawable.ic_ladies);
                viewHolder.genderItem.setChipBackgroundColorResource(R.color.colorLadiesLight);
                viewHolder.genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                viewHolder.genderItem.setChipIconResource(R.drawable.ic_gents);
                viewHolder.genderItem.setChipBackgroundColorResource(R.color.colorGentsLight);
                viewHolder.genderItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
        }

        switch (team.getKind()) {
            case INDOOR_4X4:
                viewHolder.kindItem.setChipIconResource(R.drawable.ic_4x4_small);
                viewHolder.kindItem.setChipBackgroundColorResource(R.color.colorIndoor4x4Light);
                viewHolder.kindItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case BEACH:
                viewHolder.kindItem.setChipIconResource(R.drawable.ic_beach);
                viewHolder.kindItem.setChipBackgroundColorResource(R.color.colorBeachLight);
                viewHolder.kindItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
            case INDOOR:
            default:
                viewHolder.kindItem.setChipIconResource(R.drawable.ic_6x6_small);
                viewHolder.kindItem.setChipBackgroundColorResource(R.color.colorIndoorLight);
                viewHolder.kindItem.getChipIcon().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorOnSurface), PorterDuff.Mode.SRC_IN));
                break;
        }

        return storedTeamView;
    }

    @Override
    public @NonNull Filter getFilter() {
        return mNameFilter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (prefix == null || prefix.length() == 0) {
                results.values = mStoredTeamsList;
                results.count = mStoredTeamsList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<ApiTeamSummary> matchValues = new ArrayList<>();

                for (ApiTeamSummary team : mStoredTeamsList) {
                    if (lowerCaseText.isEmpty() || team.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                        matchValues.add(team);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredStoredTeamsList.clear();

            if (results.values != null) {
                mFilteredStoredTeamsList.clear();
                mFilteredStoredTeamsList.addAll((Collection<? extends ApiTeamSummary>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
