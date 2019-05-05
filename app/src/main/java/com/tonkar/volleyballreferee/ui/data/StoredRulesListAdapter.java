package com.tonkar.volleyballreferee.ui.data;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiRulesDescription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class StoredRulesListAdapter extends ArrayAdapter<ApiRulesDescription> {

    static class ViewHolder {
        TextView  nameText;
        ImageView gameTypeImage;
    }

    private final LayoutInflater            mLayoutInflater;
    private final List<ApiRulesDescription> mStoredRulesList;
    private final List<ApiRulesDescription> mFilteredStoredRulesList;
    private final NameFilter                mNameFilter;

    public StoredRulesListAdapter(Context context, LayoutInflater layoutInflater, List<ApiRulesDescription> storedRulesList) {
        super(context, R.layout.stored_rules_list_item, storedRulesList);
        mLayoutInflater = layoutInflater;
        mStoredRulesList = storedRulesList;
        mFilteredStoredRulesList = new ArrayList<>();
        mFilteredStoredRulesList.addAll(mStoredRulesList);
        mNameFilter = new NameFilter();
    }

    public void updateStoredRulesList(List<ApiRulesDescription> storedRulesList) {
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
    public ApiRulesDescription getItem(int index) {
        return mFilteredStoredRulesList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        View storedRulesView = view;
        ViewHolder viewHolder;

        if (storedRulesView == null) {
            storedRulesView = mLayoutInflater.inflate(R.layout.stored_rules_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.nameText = storedRulesView.findViewById(R.id.stored_rules_name);
            viewHolder.gameTypeImage = storedRulesView.findViewById(R.id.stored_rules_kind_image);
            storedRulesView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) storedRulesView.getTag();
        }

        ApiRulesDescription rules = mFilteredStoredRulesList.get(index);
        viewHolder.nameText.setText(rules.getName());

        switch (rules.getKind()) {
            case INDOOR_4X4:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_4x4);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorIndoor), PorterDuff.Mode.SRC_IN));
                break;
            case BEACH:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_sun);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorBeach), PorterDuff.Mode.SRC_IN));
                break;
            case INDOOR:
            default:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_6x6);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorIndoor), PorterDuff.Mode.SRC_IN));
                break;
        }

        return storedRulesView;
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
                results.values = mStoredRulesList;
                results.count = mStoredRulesList.size();
            } else {
                String lowerCaseText = prefix.toString().toLowerCase(Locale.getDefault());

                List<ApiRulesDescription> matchValues = new ArrayList<>();

                for (ApiRulesDescription rules: mStoredRulesList) {
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
                mFilteredStoredRulesList.addAll((Collection<ApiRulesDescription>) results.values);
            }

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
