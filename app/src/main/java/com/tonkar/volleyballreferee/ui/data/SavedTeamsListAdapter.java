package com.tonkar.volleyballreferee.ui.data;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SavedTeamsListAdapter extends ArrayAdapter<BaseIndoorTeamService> {

    private final LayoutInflater              mLayoutInflater;
    private final List<BaseIndoorTeamService> mSavedTeamsServiceList;
    private final List<BaseIndoorTeamService> mFilteredSavedTeamsServiceList;
    private final NameFilter                  mNameFilter;

    public SavedTeamsListAdapter(Context context, LayoutInflater layoutInflater, List<BaseIndoorTeamService> savedTeamsServiceList) {
        super(context, R.layout.autocomplete_list_item, savedTeamsServiceList);
        mLayoutInflater = layoutInflater;
        mSavedTeamsServiceList = savedTeamsServiceList;
        mFilteredSavedTeamsServiceList = new ArrayList<>();
        mFilteredSavedTeamsServiceList.addAll(mSavedTeamsServiceList);
        mNameFilter = new NameFilter();
    }

    @Override
    public int getCount() {
        return mFilteredSavedTeamsServiceList.size();
    }

    @Override
    public BaseIndoorTeamService getItem(int index) {
        return mFilteredSavedTeamsServiceList.get(index);
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

        BaseIndoorTeamService teamService = mFilteredSavedTeamsServiceList.get(index);
        teamTextView.setText(teamService.getTeamName(null));

        switch (teamService.getGenderType()) {
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
                switch (teamService.getGenderType()) {
                    case MIXED:
                        drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(teamTextView.getContext(), R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                        break;
                    case LADIES:
                        drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(teamTextView.getContext(), R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                        break;
                    case GENTS:
                        drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(teamTextView.getContext(), R.color.colorGents), PorterDuff.Mode.SRC_IN));
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
                results.values = mSavedTeamsServiceList;
                results.count = mSavedTeamsServiceList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<BaseIndoorTeamService> matchValues = new ArrayList<>();

                for (BaseIndoorTeamService teamService : mSavedTeamsServiceList) {
                    if (lowerCaseText.isEmpty() || teamService.getTeamName(null).toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                        matchValues.add(teamService);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredSavedTeamsServiceList.clear();

            if (results.values != null) {
                mFilteredSavedTeamsServiceList.clear();
                mFilteredSavedTeamsServiceList.addAll((Collection<? extends BaseIndoorTeamService>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
