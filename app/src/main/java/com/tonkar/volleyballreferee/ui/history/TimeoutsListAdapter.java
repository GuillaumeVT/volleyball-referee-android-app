package com.tonkar.volleyballreferee.ui.history;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.BaseTimeoutService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.Timeout;

import java.util.Locale;

public class TimeoutsListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView  scoreText;
        ImageView timeoutImage;
    }

    private final LayoutInflater        mLayoutInflater;
    private final BaseTimeoutService    mTimeoutService;
    private final BaseTeamService       mTeamService;
    private       TeamType              mTeamType;
    private final int                   mSetIndex;

    TimeoutsListAdapter(LayoutInflater layoutInflater, BaseTimeoutService timeoutService, BaseTeamService teamService, TeamType teamType, int setIndex) {
        mLayoutInflater = layoutInflater;
        mTimeoutService = timeoutService;
        mTeamService = teamService;
        mTeamType = teamType;
        mSetIndex = setIndex;
    }

    public void setTeamType(TeamType teamType) {
        mTeamType = teamType;
    }

    public TeamType getTeamType() {
        return mTeamType;
    }

    @Override
    public int getCount() {
        return mTimeoutService.getCalledTimeouts(mTeamType, mSetIndex).size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        View timeoutView = view;
        ViewHolder viewHolder;

        if (timeoutView == null) {
            timeoutView = mLayoutInflater.inflate(R.layout.timeout_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.scoreText = timeoutView.findViewById(R.id.score_text);
            viewHolder.timeoutImage = timeoutView.findViewById(R.id.timeout_image);
            timeoutView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) timeoutView.getTag();
        }

        Timeout timeout = mTimeoutService.getCalledTimeouts(mTeamType, mSetIndex).get(index);

        if (TeamType.HOME.equals(mTeamType)) {
            viewHolder.scoreText.setText(String.format(Locale.getDefault(), "%d-%d", timeout.getHomeTeamPoints(), timeout.getGuestTeamPoints()));
        } else {
            viewHolder.scoreText.setText(String.format(Locale.getDefault(), "%d-%d", timeout.getGuestTeamPoints(), timeout.getHomeTeamPoints()));
        }
        viewHolder.timeoutImage.getDrawable().setColorFilter(new PorterDuffColorFilter(mTeamService.getTeamColor(mTeamType), PorterDuff.Mode.SRC_IN));

        return timeoutView;
    }
}
