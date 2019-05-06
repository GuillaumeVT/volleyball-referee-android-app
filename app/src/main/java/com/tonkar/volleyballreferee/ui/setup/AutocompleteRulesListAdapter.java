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
import com.tonkar.volleyballreferee.api.ApiRulesDescription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class AutocompleteRulesListAdapter extends ArrayAdapter<ApiRulesDescription> {

    private final LayoutInflater            mLayoutInflater;
    private final List<ApiRulesDescription> mStoredRulesList;
    private final List<ApiRulesDescription> mFilteredStoredRulesList;
    private final NameFilter                mNameFilter;

    public AutocompleteRulesListAdapter(Context context, LayoutInflater layoutInflater, List<ApiRulesDescription> storedRulesList) {
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
    public ApiRulesDescription getItem(int index) {
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
            rulesTextView = (TextView) mLayoutInflater.inflate(R.layout.autocomplete_list_item, null);
        } else {
            rulesTextView = (TextView) view;
        }

        ApiRulesDescription rules = mFilteredStoredRulesList.get(index);
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

            if (prefix == null || prefix.length() == 0) {
                results.values = mStoredRulesList;
                results.count = mStoredRulesList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<ApiRulesDescription> matchValues = new ArrayList<>();

                for (ApiRulesDescription rules : mStoredRulesList) {
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
            mFilteredStoredRulesList.clear();

            if (results.values != null) {
                mFilteredStoredRulesList.clear();
                mFilteredStoredRulesList.addAll((Collection<? extends ApiRulesDescription>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
