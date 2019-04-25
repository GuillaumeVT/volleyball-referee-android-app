package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiTeam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class TeamsListAdapter extends ArrayAdapter<ApiTeam> {

    private final LayoutInflater mLayoutInflater;
    private final List<ApiTeam>  mSavedTeamsList;
    private final List<ApiTeam>  mFilteredSavedTeamsList;
    private final NameFilter     mNameFilter;

    public TeamsListAdapter(Context context, LayoutInflater layoutInflater, List<ApiTeam> savedTeamsList) {
        super(context, R.layout.autocomplete_list_item, savedTeamsList);
        mLayoutInflater = layoutInflater;
        mSavedTeamsList = savedTeamsList;
        mFilteredSavedTeamsList = new ArrayList<>();
        mFilteredSavedTeamsList.addAll(mSavedTeamsList);
        mNameFilter = new NameFilter();
    }

    @Override
    public int getCount() {
        return mFilteredSavedTeamsList.size();
    }

    @Override
    public ApiTeam getItem(int index) {
        return mFilteredSavedTeamsList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        TextView teamTextView;

        if (view == null) {
            teamTextView = (TextView) mLayoutInflater.inflate(R.layout.autocomplete_list_item, null);
        } else {
            teamTextView = (TextView) view;
        }

        ApiTeam team = mFilteredSavedTeamsList.get(index);
        teamTextView.setText(team.getName());

        switch (team.getGenderType()) {
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
                switch (team.getGenderType()) {
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
    public Filter getFilter() {
        return mNameFilter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (prefix == null || prefix.length() == 0) {
                results.values = mSavedTeamsList;
                results.count = mSavedTeamsList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<ApiTeam> matchValues = new ArrayList<>();

                for (ApiTeam team : mSavedTeamsList) {
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
            mFilteredSavedTeamsList.clear();

            if (results.values != null) {
                mFilteredSavedTeamsList.clear();
                mFilteredSavedTeamsList.addAll((Collection<? extends ApiTeam>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
