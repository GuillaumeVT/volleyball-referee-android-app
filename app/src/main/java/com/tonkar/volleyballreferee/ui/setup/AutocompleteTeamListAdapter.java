package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.stored.api.ApiTeamSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class AutocompleteTeamListAdapter extends ArrayAdapter<ApiTeamSummary> {

    private final LayoutInflater       mLayoutInflater;
    private final List<ApiTeamSummary> mStoredTeamsList;
    private final List<ApiTeamSummary> mFilteredStoredTeamsList;
    private final NameFilter           mNameFilter;

    public AutocompleteTeamListAdapter(Context context, LayoutInflater layoutInflater, List<ApiTeamSummary> storedTeamsList) {
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
    public ApiTeamSummary getItem(int index) {
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
            teamTextView = (TextView) mLayoutInflater.inflate(R.layout.autocomplete_list_item, null);
        } else {
            teamTextView = (TextView) view;
        }

        ApiTeamSummary team = mFilteredStoredTeamsList.get(index);
        teamTextView.setText(team.getName());

        switch (team.getGender()) {
            case MIXED:
                teamTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_mixed, 0);
                break;
            case LADIES:
                teamTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_ladies, 0);
                break;
            case GENTS:
                teamTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_gents, 0);
                break;
        }

        for (Drawable drawable : teamTextView.getCompoundDrawables()) {
            if (drawable != null) {
                switch (team.getGender()) {
                    case MIXED:
                        drawable.mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(teamTextView.getContext(), R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                        break;
                    case LADIES:
                        drawable.mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(teamTextView.getContext(), R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                        break;
                    case GENTS:
                        drawable.mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(teamTextView.getContext(), R.color.colorGents), PorterDuff.Mode.SRC_IN));
                        break;
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
