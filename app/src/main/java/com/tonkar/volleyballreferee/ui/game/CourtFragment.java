package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CourtFragment extends Fragment implements TeamClient, TeamListener {

    protected     View                      mView;
    private       TeamService               mTeamService;
    private       TeamType                  mTeamOnLeftSide;
    private       TeamType                  mTeamOnRightSide;
    private final Map<PositionType, Button> mLeftTeamPositions;
    private final Map<PositionType, Button> mRightTeamPositions;

    public CourtFragment() {
        mLeftTeamPositions = new HashMap<>();
        mRightTeamPositions = new HashMap<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-Court", "Create court fragment");

        setTeamService(ServicesProvider.getInstance().getTeamService());

        mTeamOnLeftSide = mTeamService.getTeamOnLeftSide();
        mTeamOnRightSide = mTeamService.getTeamOnRightSide();
        mTeamService.addTeamListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTeamService.removeTeamListener(this);
    }

    protected void initView() {
        onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, null);

        for (Map.Entry<PositionType,Button> entry : mLeftTeamPositions.entrySet()) {
            final PositionType positionType = entry.getKey();
            entry.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final List<Integer> availablePlayers = mTeamService.getPlayersOnBench(mTeamOnLeftSide);
                    if (availablePlayers.size() > 0) {
                        Log.i("VBR-Court", String.format("Substitute %s team player at %s position", mTeamOnLeftSide.toString(), positionType.toString()));
                        final PlayerAdapter playerAdapter = new PlayerAdapter(getContext(), mTeamOnLeftSide, mTeamService.getTeamColor(mTeamOnLeftSide), availablePlayers, positionType);
                        showPlayerSelectionDialog(playerAdapter);
                    }
                }
            });
        }

        for (Map.Entry<PositionType,Button> entry : mRightTeamPositions.entrySet()) {
            final PositionType positionType = entry.getKey();
            entry.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final List<Integer> availablePlayers = mTeamService.getPlayersOnBench(mTeamOnRightSide);
                    if (availablePlayers.size() > 0) {
                        Log.i("VBR-Court", String.format("Substitute %s team player at %s position", mTeamOnRightSide.toString(), positionType.toString()));
                        final PlayerAdapter playerAdapter = new PlayerAdapter(getContext(), mTeamOnRightSide, mTeamService.getTeamColor(mTeamOnRightSide), availablePlayers, positionType);
                        showPlayerSelectionDialog(playerAdapter);
                    }
                }
            });
        }
    }

    protected void addButtonOnLeftSide(final PositionType positionType, final Button button) {
        mLeftTeamPositions.put(positionType, button);
    }

    protected void addButtonOnRightSide(final PositionType positionType, final Button button) {
        mRightTeamPositions.put(positionType, button);
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mTeamOnLeftSide = leftTeamType;
        mTeamOnRightSide = rightTeamType;

        for (final Button button : mLeftTeamPositions.values()) {
            button.setText("?");
            int backgroundColor = ContextCompat.getColor(mView.getContext(), mTeamService.getTeamColor(mTeamOnLeftSide));
            button.getBackground().setColorFilter(new PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.SRC));
            button.setTextColor(UiUtils.getTextColor(mView.getContext(), backgroundColor));
        }

        for (final Button button : mRightTeamPositions.values()) {
            button.setText("?");
            int backgroundColor = ContextCompat.getColor(mView.getContext(), mTeamService.getTeamColor(mTeamOnRightSide));
            button.getBackground().setColorFilter(new PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.SRC));
            button.setTextColor(UiUtils.getTextColor(mView.getContext(), backgroundColor));
        }

        final List<Integer> playersOnLeftSide = mTeamService.getPlayersOnCourt(mTeamOnLeftSide);

        for (Integer number : playersOnLeftSide) {
            final PositionType positionType = mTeamService.getPlayerPosition(mTeamOnLeftSide, number);
            mLeftTeamPositions.get(positionType).setText(String.valueOf(number));
        }

        final List<Integer> playersOnRightSide = mTeamService.getPlayersOnCourt(mTeamOnRightSide);

        for (Integer number : playersOnRightSide) {
            final PositionType positionType = mTeamService.getPlayerPosition(mTeamOnRightSide, number);
            mRightTeamPositions.get(positionType).setText(String.valueOf(number));
        }
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType) {
        if (!PositionType.BENCH.equals(positionType)) {
            final Map<PositionType, Button> teamPositions;

            if (mTeamOnLeftSide.equals(teamType)) {
                teamPositions = mLeftTeamPositions;
            } else {
                teamPositions = mRightTeamPositions;
            }

            teamPositions.get(positionType).setText(String.valueOf(number));
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType) {}

    private void showPlayerSelectionDialog(final PlayerAdapter playerAdapter) {
        final GridView gridView = new GridView(getContext());
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setGravity(Gravity.CENTER);
        gridView.setPadding(8, 8, 8, 8);
        gridView.setAdapter(playerAdapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.select_player_title)).setView(gridView);
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        final AlertDialog alertDialog = builder.create();

        playerAdapter.setDialog(alertDialog);

        alertDialog.show();
    }

    private class PlayerAdapter extends BaseAdapter {

        private final Context       mContext;
        private final TeamType      mTeamType;
        private final int           mColorId;
        private final List<Integer> mAvailablePlayers;
        private final PositionType  mPositionType;
        private       AlertDialog   mDialog;

        private PlayerAdapter(Context context, final TeamType teamType, final int colorId, final List<Integer> availablePlayers, final PositionType positionType) {
            mContext = context;
            mTeamType = teamType;
            mColorId = colorId;
            mAvailablePlayers = availablePlayers;
            mPositionType = positionType;
        }

        @Override
        public int getCount() {
            return mAvailablePlayers.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int playerShirtNumber = mAvailablePlayers.get(position);
            Button button;

            if (convertView == null) {
                button = new Button(mContext);
            } else {
                button = (Button) convertView;
            }

            button.setText(String.valueOf(playerShirtNumber));
            int backgroundColor = ContextCompat.getColor(mContext, mColorId);
            button.setTextColor(UiUtils.getTextColor(mContext, backgroundColor));
            button.getBackground().setColorFilter(new PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.SRC));

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int number = Integer.parseInt(((Button) view).getText().toString());
                    Log.i("VBR-Court", String.format("Substitute %s team player at %s position by #%d player", mTeamType.toString(), mPositionType.toString(), number));
                    mTeamService.substitutePlayer(mTeamType, number, mPositionType);

                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
            });

            return button;
        }

        void setDialog(AlertDialog dialog) {
            mDialog = dialog;
        }
    }

}
