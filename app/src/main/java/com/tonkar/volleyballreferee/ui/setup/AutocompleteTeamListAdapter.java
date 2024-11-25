package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.TeamSummaryDto;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class AutocompleteTeamListAdapter extends ArrayAdapter<TeamSummaryDto> {

    private final LayoutInflater       mLayoutInflater;
    private final List<TeamSummaryDto> mStoredTeamsList;
    private final List<TeamSummaryDto> mFilteredStoredTeamsList;
    private final NameFilter           mNameFilter;

    public AutocompleteTeamListAdapter(Context context, LayoutInflater layoutInflater, List<TeamSummaryDto> storedTeamsList) {
        super(context, R.layout.autocomplete_list_item, storedTeamsList);
        mLayoutInflater = layoutInflater;
        mStoredTeamsList = storedTeamsList;
        mFilteredStoredTeamsList = new ArrayList<>();
        mFilteredStoredTeamsList.addAll(mStoredTeamsList);
        mNameFilter = new NameFilter();
    }

    @Override
    public int getCount() {
        return mFilteredStoredTeamsList.size();
    }

    @Override
    public TeamSummaryDto getItem(int index) {
        return mFilteredStoredTeamsList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
        TextView teamTextView;

        if (view == null) {
            teamTextView = (TextView) mLayoutInflater.inflate(R.layout.autocomplete_list_item, parent, false);
        } else {
            teamTextView = (TextView) view;
        }

        TeamSummaryDto team = mFilteredStoredTeamsList.get(index);
        teamTextView.setText(team.getName());

        switch (team.getGender()) {
            case MIXED -> teamTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_mixed, 0);
            case LADIES -> teamTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_ladies, 0);
            case GENTS -> teamTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_gents, 0);
        }

        for (Drawable drawable : teamTextView.getCompoundDrawables()) {
            if (drawable != null) {
                switch (team.getGender()) {
                    case MIXED -> drawable
                            .mutate()
                            .setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(teamTextView.getContext(), R.color.colorMixed),
                                                                      PorterDuff.Mode.SRC_IN));
                    case LADIES -> drawable
                            .mutate()
                            .setColorFilter(
                                    new PorterDuffColorFilter(ContextCompat.getColor(teamTextView.getContext(), R.color.colorLadies),
                                                              PorterDuff.Mode.SRC_IN));
                    case GENTS -> drawable
                            .mutate()
                            .setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(teamTextView.getContext(), R.color.colorGents),
                                                                      PorterDuff.Mode.SRC_IN));
                }
            }
        }

        return teamTextView;
    }

    @Override
    public @NonNull Filter getFilter() {
        return mNameFilter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (StringUtils.isBlank(prefix)) {
                results.values = mStoredTeamsList;
                results.count = mStoredTeamsList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());
                boolean perfectMatch = false;
                List<TeamSummaryDto> matchValues = new ArrayList<>();

                for (TeamSummaryDto team : mStoredTeamsList) {
                    if (lowerCaseText.isEmpty() || team.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                        matchValues.add(team);
                    }
                    if (team.getName().toLowerCase(Locale.getDefault()).equals(lowerCaseText)) {
                        perfectMatch = true;
                    }
                }

                // If the input is a team name, show all teams to quickly select another
                if (perfectMatch) {
                    results.values = mStoredTeamsList;
                    results.count = mStoredTeamsList.size();
                } else {
                    results.values = matchValues;
                    results.count = matchValues.size();
                }
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredStoredTeamsList.clear();

            if (results.values != null) {
                mFilteredStoredTeamsList.addAll((Collection<? extends TeamSummaryDto>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
