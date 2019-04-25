package com.tonkar.volleyballreferee.ui.data;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.api.ApiTeam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SavedTeamsListAdapter extends ArrayAdapter<ApiTeam> {

    static class ViewHolder {
        TextView  nameText;
        ImageView genderTypeImage;
        ImageView gameTypeImage;
    }

    private final LayoutInflater mLayoutInflater;
    private final List<ApiTeam>  mSavedTeamsList;
    private final List<ApiTeam>  mFilteredSavedTeamsList;
    private final NameFilter     mNameFilter;

    SavedTeamsListAdapter(Context context, LayoutInflater layoutInflater, List<ApiTeam> savedTeamsList) {
        super(context, R.layout.saved_teams_list_item, savedTeamsList);
        mLayoutInflater = layoutInflater;
        mSavedTeamsList = savedTeamsList;
        mFilteredSavedTeamsList = new ArrayList<>();
        mFilteredSavedTeamsList.addAll(mSavedTeamsList);
        mNameFilter = new NameFilter();
    }

    public void updateSavedTeamsList(List<ApiTeam> savedTeamsList) {
        mSavedTeamsList.clear();
        mFilteredSavedTeamsList.clear();
        mSavedTeamsList.addAll(savedTeamsList);
        mFilteredSavedTeamsList.addAll(savedTeamsList);
        notifyDataSetChanged();
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
        View savedTeamView = view;
        ViewHolder viewHolder;

        if (savedTeamView == null) {
            savedTeamView = mLayoutInflater.inflate(R.layout.saved_teams_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.nameText = savedTeamView.findViewById(R.id.saved_team_name);
            viewHolder.genderTypeImage = savedTeamView.findViewById(R.id.saved_team_gender_image);
            viewHolder.gameTypeImage = savedTeamView.findViewById(R.id.saved_team_type_image);
            savedTeamView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) savedTeamView.getTag();
        }

        ApiTeam team = mFilteredSavedTeamsList.get(index);
        viewHolder.nameText.setText(team.getName());

        switch (team.getGenderType()) {
            case MIXED:
                viewHolder.genderTypeImage.setImageResource(R.drawable.ic_mixed);
                viewHolder.genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorMixed), PorterDuff.Mode.SRC_IN));
                break;
            case LADIES:
                viewHolder.genderTypeImage.setImageResource(R.drawable.ic_ladies);
                viewHolder.genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorLadies), PorterDuff.Mode.SRC_IN));
                break;
            case GENTS:
                viewHolder.genderTypeImage.setImageResource(R.drawable.ic_gents);
                viewHolder.genderTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorGents), PorterDuff.Mode.SRC_IN));
                break;
        }

        switch (team.getGameType()) {
            case INDOOR_4X4:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_4x4);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorIndoor), PorterDuff.Mode.SRC_IN));
                break;
            case BEACH:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_sun);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorBeach), PorterDuff.Mode.SRC_IN));
                break;
            case TIME:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_time_based);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorTime), PorterDuff.Mode.SRC_IN));
                break;
            case INDOOR:
            default:
                viewHolder.gameTypeImage.setImageResource(R.drawable.ic_6x6);
                viewHolder.gameTypeImage.getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.colorIndoor), PorterDuff.Mode.SRC_IN));
                break;
        }

        return savedTeamView;
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
