package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.LeagueSummaryDto;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class AutocompleteLeagueListAdapter extends ArrayAdapter<LeagueSummaryDto> {

    private final LayoutInflater         mLayoutInflater;
    private final List<LeagueSummaryDto> mStoredLeagueList;
    private final List<LeagueSummaryDto> mFilteredStoredLeagueList;
    private final NameFilter             mNameFilter;

    AutocompleteLeagueListAdapter(Context context, LayoutInflater layoutInflater, List<LeagueSummaryDto> storedLeagueList) {
        super(context, R.layout.autocomplete_list_item, storedLeagueList);
        mLayoutInflater = layoutInflater;
        mStoredLeagueList = storedLeagueList;
        mFilteredStoredLeagueList = new ArrayList<>();
        mFilteredStoredLeagueList.addAll(mStoredLeagueList);
        mNameFilter = new NameFilter();
    }

    @Override
    public int getCount() {
        return mFilteredStoredLeagueList.size();
    }

    @Override
    public LeagueSummaryDto getItem(int index) {
        return mFilteredStoredLeagueList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
        TextView leagueTextView;

        if (view == null) {
            leagueTextView = (TextView) mLayoutInflater.inflate(R.layout.autocomplete_list_item, parent, false);
        } else {
            leagueTextView = (TextView) view;
        }

        LeagueSummaryDto league = mFilteredStoredLeagueList.get(index);
        leagueTextView.setText(league.getName());

        return leagueTextView;
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
                results.values = mStoredLeagueList;
                results.count = mStoredLeagueList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());
                boolean perfectMatch = false;
                List<LeagueSummaryDto> matchValues = new ArrayList<>();

                for (LeagueSummaryDto league : mStoredLeagueList) {
                    if (lowerCaseText.isEmpty() || league.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                        matchValues.add(league);
                    }
                    if (league.getName().toLowerCase(Locale.getDefault()).equals(lowerCaseText)) {
                        perfectMatch = true;
                    }
                }

                // If the input is a league name, show all leagues to quickly select another
                if (perfectMatch) {
                    results.values = mStoredLeagueList;
                    results.count = mStoredLeagueList.size();
                } else {
                    results.values = matchValues;
                    results.count = matchValues.size();
                }
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredStoredLeagueList.clear();

            if (results.values != null) {
                mFilteredStoredLeagueList.addAll((Collection<? extends LeagueSummaryDto>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
