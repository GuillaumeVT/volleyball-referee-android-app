package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.RulesSummaryDto;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class AutocompleteRulesListAdapter extends ArrayAdapter<RulesSummaryDto> {

    private final LayoutInflater        mLayoutInflater;
    private final List<RulesSummaryDto> mStoredRulesList;
    private final List<RulesSummaryDto> mFilteredStoredRulesList;
    private final NameFilter            mNameFilter;

    public AutocompleteRulesListAdapter(Context context, LayoutInflater layoutInflater, List<RulesSummaryDto> storedRulesList) {
        super(context, R.layout.autocomplete_list_item, storedRulesList);
        mLayoutInflater = layoutInflater;
        mStoredRulesList = storedRulesList;
        mFilteredStoredRulesList = new ArrayList<>();
        mFilteredStoredRulesList.addAll(mStoredRulesList);
        mNameFilter = new NameFilter();
    }

    @Override
    public int getCount() {
        return mFilteredStoredRulesList.size();
    }

    @Override
    public RulesSummaryDto getItem(int index) {
        return mFilteredStoredRulesList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
        TextView rulesTextView;

        if (view == null) {
            rulesTextView = (TextView) mLayoutInflater.inflate(R.layout.autocomplete_list_item, parent, false);
        } else {
            rulesTextView = (TextView) view;
        }

        RulesSummaryDto rules = mFilteredStoredRulesList.get(index);
        rulesTextView.setText(rules.getName());

        return rulesTextView;
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
                results.values = mStoredRulesList;
                results.count = mStoredRulesList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());
                boolean perfectMatch = false;
                List<RulesSummaryDto> matchValues = new ArrayList<>();

                for (RulesSummaryDto rules : mStoredRulesList) {
                    if (lowerCaseText.isEmpty() || rules.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseText)) {
                        matchValues.add(rules);
                    }
                    if (rules.getName().toLowerCase(Locale.getDefault()).equals(lowerCaseText)) {
                        perfectMatch = true;
                    }
                }

                // If the input is a rules name, show all rules to quickly select another
                if (perfectMatch) {
                    results.values = mStoredRulesList;
                    results.count = mStoredRulesList.size();
                } else {
                    results.values = matchValues;
                    results.count = matchValues.size();
                }
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredStoredRulesList.clear();

            if (results.values != null) {
                mFilteredStoredRulesList.addAll((Collection<? extends RulesSummaryDto>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
