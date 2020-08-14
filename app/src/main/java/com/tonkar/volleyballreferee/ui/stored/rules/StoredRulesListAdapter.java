package com.tonkar.volleyballreferee.ui.stored.rules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.stored.api.ApiRulesSummary;
import com.tonkar.volleyballreferee.ui.stored.SelectableArrayAdapter;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class StoredRulesListAdapter extends SelectableArrayAdapter<ApiRulesSummary> {

    static class ViewHolder {
        MaterialCardView listItemCard;
        TextView         nameText;
        Chip             kindItem;
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
        clearSelectedItems();
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
        View rulesView = view;
        ViewHolder viewHolder;

        if (rulesView == null) {
            rulesView = mLayoutInflater.inflate(R.layout.stored_rules_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.listItemCard = rulesView.findViewById(R.id.list_item_card);
            viewHolder.nameText = rulesView.findViewById(R.id.stored_rules_name);
            viewHolder.kindItem = rulesView.findViewById(R.id.rules_kind_item);
            rulesView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) rulesView.getTag();
        }

        ApiRulesSummary rules = mFilteredStoredRulesList.get(index);
        viewHolder.nameText.setText(rules.getName());

        switch (rules.getKind()) {
            case INDOOR_4X4:
                UiUtils.colorChipIcon(getContext(), R.color.colorIndoor4x4Light, R.drawable.ic_4x4_small, viewHolder.kindItem);
                break;
            case BEACH:
                UiUtils.colorChipIcon(getContext(), R.color.colorBeachLight, R.drawable.ic_beach, viewHolder.kindItem);
                break;
            case SNOW:
                UiUtils.colorChipIcon(getContext(), R.color.colorSnowLight, R.drawable.ic_snow, viewHolder.kindItem);
                break;
            case INDOOR:
            default:
                UiUtils.colorChipIcon(getContext(), R.color.colorIndoorLight, R.drawable.ic_6x6_small, viewHolder.kindItem);
                break;
        }

        viewHolder.listItemCard.setBackgroundColor(ContextCompat.getColor(getContext(), isSelectedItem(rules.getId()) ? R.color.colorSelectedItem : R.color.colorSurface));

        return rulesView;
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
