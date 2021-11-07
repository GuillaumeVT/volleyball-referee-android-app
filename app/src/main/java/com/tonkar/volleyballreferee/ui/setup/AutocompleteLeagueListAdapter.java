package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.ApiLeagueSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class AutocompleteLeagueListAdapter extends ArrayAdapter<ApiLeagueSummary> {

    private final LayoutInflater         mLayoutInflater;
    private final List<ApiLeagueSummary> mStoredLeagueList;
    private final List<ApiLeagueSummary> mFilteredStoredLeagueList;
    private final NameFilter             mNameFilter;

    AutocompleteLeagueListAdapter(Context context, LayoutInflater layoutInflater, List<ApiLeagueSummary> storedLeagueList) {
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
    public ApiLeagueSummary getItem(int index) {
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

        ApiLeagueSummary league = mFilteredStoredLeagueList.get(index);
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

            if (prefix == null || prefix.length() == 0) {
                results.values = mStoredLeagueList;
                results.count = mStoredLeagueList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<ApiLeagueSummary> matchValues = new ArrayList<>();

                for (ApiLeagueSummary league : mStoredLeagueList) {
                    if (lowerCaseText.isEmpty() || league.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                        matchValues.add(league);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredStoredLeagueList.clear();

            if (results.values != null) {
                mFilteredStoredLeagueList.addAll((Collection<? extends ApiLeagueSummary>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
