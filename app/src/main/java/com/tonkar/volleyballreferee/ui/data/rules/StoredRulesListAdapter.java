package com.tonkar.volleyballreferee.ui.data.rules;

import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.RulesSummaryDto;
import com.tonkar.volleyballreferee.ui.data.SelectableArrayAdapter;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class StoredRulesListAdapter extends SelectableArrayAdapter<RulesSummaryDto> {

    static class ViewHolder {
        MaterialCardView listItemCard;
        TextView         nameText;
        ImageView        kindItem;
    }

    private final LayoutInflater        mLayoutInflater;
    private final List<RulesSummaryDto> mStoredRulesList;
    private final List<RulesSummaryDto> mFilteredStoredRulesList;
    private final NameFilter            mNameFilter;

    StoredRulesListAdapter(Context context, LayoutInflater layoutInflater, List<RulesSummaryDto> storedRulesList) {
        super(context, R.layout.stored_rules_list_item, storedRulesList);
        mLayoutInflater = layoutInflater;
        mStoredRulesList = storedRulesList;
        mFilteredStoredRulesList = new ArrayList<>();
        mFilteredStoredRulesList.addAll(mStoredRulesList);
        mNameFilter = new NameFilter();
    }

    void updateStoredRulesList(List<RulesSummaryDto> storedRulesList) {
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
    public RulesSummaryDto getItem(int index) {
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
            rulesView = mLayoutInflater.inflate(R.layout.stored_rules_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.listItemCard = rulesView.findViewById(R.id.list_item_card);
            viewHolder.nameText = rulesView.findViewById(R.id.stored_rules_name);
            viewHolder.kindItem = rulesView.findViewById(R.id.rules_kind_item);
            rulesView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rulesView.getTag();
        }

        RulesSummaryDto rules = mFilteredStoredRulesList.get(index);
        viewHolder.nameText.setText(rules.getName());

        switch (rules.getKind()) {
            case INDOOR_4X4 ->
                    UiUtils.colorChipIcon(getContext(), R.color.colorIndoor4x4Light, R.drawable.ic_4x4_small, viewHolder.kindItem);
            case BEACH -> UiUtils.colorChipIcon(getContext(), R.color.colorBeachLight, R.drawable.ic_beach, viewHolder.kindItem);
            case SNOW -> UiUtils.colorChipIcon(getContext(), R.color.colorSnowLight, R.drawable.ic_snow, viewHolder.kindItem);
            default -> UiUtils.colorChipIcon(getContext(), R.color.colorIndoorLight, R.drawable.ic_6x6_small, viewHolder.kindItem);
        }

        viewHolder.listItemCard.setCardBackgroundColor(
                ContextCompat.getColor(getContext(), isSelectedItem(rules.getId()) ? R.color.colorSelectedItem : R.color.colorSurface));

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

            if (StringUtils.isBlank(prefix)) {
                results.values = mStoredRulesList;
                results.count = mStoredRulesList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<RulesSummaryDto> matchValues = new ArrayList<>();

                for (RulesSummaryDto rules : mStoredRulesList) {
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
                mFilteredStoredRulesList.addAll((Collection<RulesSummaryDto>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
