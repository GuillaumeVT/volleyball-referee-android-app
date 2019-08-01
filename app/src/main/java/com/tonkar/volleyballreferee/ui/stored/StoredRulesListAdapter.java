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
import com.tonkar.volleyballreferee.engine.stored.api.ApiRulesSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class StoredRulesListAdapter extends ArrayAdapter<ApiRulesSummary> {

    static class ViewHolder {
        TextView nameText;
        Chip     kindItem;
    }

    private final LayoutInflater        mLayoutInflater;
    private final List<ApiRulesSummary> mStoredRulesList;
    private final List<ApiRulesSummary> mFilteredStoredRulesList;
    private final NameFilter            mNameFilter;

    StoredRulesListAdapter(Context context, LayoutInflater layoutInflater, List<ApiRulesSummary> storedRulesList) {
        super(context, R.layout.stored_rules_list_item, storedRulesList);
        mLayoutInflater = layoutInflater;
        mStoredRulesList = storedRulesList;
        mFilteredStoredRulesList = new ArrayList<>();
        mFilteredStoredRulesList.addAll(mStoredRulesList);
        mNameFilter = new NameFilter();
    }

    void updateStoredRulesList(List<ApiRulesSummary> storedRulesList) {
        mStoredRulesList.clear();
        mFilteredStoredRulesList.clear();
        mStoredRulesList.addAll(storedRulesList);
        mFilteredStoredRulesList.addAll(storedRulesList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFilteredStoredRulesList.size();
    }

    @Override
    public ApiRulesSummary getItem(int index) {
        return mFilteredStoredRulesList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
        View storedRulesView = view;
        ViewHolder viewHolder;

        if (storedRulesView == null) {
            storedRulesView = mLayoutInflater.inflate(R.layout.stored_rules_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.nameText = storedRulesView.findViewById(R.id.stored_rules_name);
            viewHolder.kindItem = storedRulesView.findViewById(R.id.rules_kind_item);
            storedRulesView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) storedRulesView.getTag();
        }

        ApiRulesSummary rules = mFilteredStoredRulesList.get(index);
        viewHolder.nameText.setText(rules.getName());

        switch (rules.getKind()) {
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

        return storedRulesView;
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

                List<ApiRulesSummary> matchValues = new ArrayList<>();

                for (ApiRulesSummary rules: mStoredRulesList) {
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
                mFilteredStoredRulesList.addAll((Collection<ApiRulesSummary>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
