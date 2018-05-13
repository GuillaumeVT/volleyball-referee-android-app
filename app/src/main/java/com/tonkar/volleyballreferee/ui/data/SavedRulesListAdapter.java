package com.tonkar.volleyballreferee.ui.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.rules.Rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SavedRulesListAdapter extends ArrayAdapter<Rules> {

    private final LayoutInflater mLayoutInflater;
    private final List<Rules>    mSavedRulesServiceList;
    private final List<Rules>    mFilteredSavedRulesServiceList;
    private final NameFilter     mNameFilter;

    public SavedRulesListAdapter(Context context, LayoutInflater layoutInflater, List<Rules> savedRulesServiceList) {
        super(context, R.layout.autocomplete_list_item, savedRulesServiceList);
        mLayoutInflater = layoutInflater;
        mSavedRulesServiceList = savedRulesServiceList;
        mFilteredSavedRulesServiceList = new ArrayList<>();
        mFilteredSavedRulesServiceList.addAll(mSavedRulesServiceList);
        mNameFilter = new NameFilter();
    }

    @Override
    public int getCount() {
        return mFilteredSavedRulesServiceList.size();
    }

    @Override
    public Rules getItem(int index) {
        return mFilteredSavedRulesServiceList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        TextView rulesTextView;

        if (view == null) {
            rulesTextView = (TextView) mLayoutInflater.inflate(R.layout.autocomplete_list_item, null);
        } else {
            rulesTextView = (TextView) view;
        }

        Rules rules = mFilteredSavedRulesServiceList.get(index);
        rulesTextView.setText(rules.getName());

        return rulesTextView;
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
                results.values = mSavedRulesServiceList;
                results.count = mSavedRulesServiceList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<Rules> matchValues = new ArrayList<>();

                for (Rules rules: mSavedRulesServiceList) {
                    if (lowerCaseText.isEmpty() || rules.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                        matchValues.add(rules);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredSavedRulesServiceList.clear();

            if (results.values != null) {
                mFilteredSavedRulesServiceList.clear();
                mFilteredSavedRulesServiceList.addAll((Collection<Rules>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
