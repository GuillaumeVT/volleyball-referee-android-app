package com.tonkar.volleyballreferee.ui.game.timeout;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.TimeoutDto;
import com.tonkar.volleyballreferee.engine.game.timeout.IBaseTimeout;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;

public class TimeoutsListAdapter extends BaseAdapter {

    private final Context        mContext;
    private final LayoutInflater mLayoutInflater;
    private final IBaseTimeout   mTimeoutService;
    private final IBaseTeam      mTeamService;
    private       TeamType       mTeamType;
    private final int            mSetIndex;

    TimeoutsListAdapter(Context context,
                        LayoutInflater layoutInflater,
                        IBaseTimeout timeoutService,
                        IBaseTeam teamService,
                        TeamType teamType) {
        this(context, layoutInflater, timeoutService, teamService, teamType, -1);
    }

    public TimeoutsListAdapter(Context context,
                               LayoutInflater layoutInflater,
                               IBaseTimeout timeoutService,
                               IBaseTeam teamService,
                               TeamType teamType,
                               int setIndex) {
        mContext = context;
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
        int count;

        if (mSetIndex < 0) {
            count = mTimeoutService.getCalledTimeouts(mTeamType).size();
        } else {
            count = mTimeoutService.getCalledTimeouts(mTeamType, mSetIndex).size();
        }

        return count;
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
    public View getView(int index, View view, ViewGroup parent) {
        View timeoutView = view;

        if (timeoutView == null) {
            timeoutView = mLayoutInflater.inflate(R.layout.timeout_list_item, null);
        }

        TextView timeoutText = timeoutView.findViewById(R.id.score_text);

        TimeoutDto timeout;

        if (mSetIndex < 0) {
            timeout = mTimeoutService.getCalledTimeouts(mTeamType).get(index);
        } else {
            timeout = mTimeoutService.getCalledTimeouts(mTeamType, mSetIndex).get(index);
        }

        if (TeamType.HOME.equals(mTeamType)) {
            timeoutText.setText(String.format(Locale.getDefault(), "%d-%d", timeout.getHomePoints(), timeout.getGuestPoints()));
        } else {
            timeoutText.setText(String.format(Locale.getDefault(), "%d-%d", timeout.getGuestPoints(), timeout.getHomePoints()));
        }
        UiUtils.colorTeamText(mContext, mTeamService.getTeamColor(mTeamType), timeoutText);

        return timeoutView;
    }
}
