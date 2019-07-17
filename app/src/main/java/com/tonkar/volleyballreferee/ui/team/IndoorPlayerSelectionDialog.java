package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.game.sanction.ISanction;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class IndoorPlayerSelectionDialog {

    private AlertDialog mAlertDialog;

    IndoorPlayerSelectionDialog(LayoutInflater layoutInflater, Context context, String title, IBaseTeam teamService, TeamType teamType, Set<Integer> players) {
        this(layoutInflater, context, title, teamService, null, teamType, players);
    }

    protected IndoorPlayerSelectionDialog(LayoutInflater layoutInflater, Context context, String title, IBaseTeam teamService, ISanction sanction, TeamType teamType, Set<Integer> players) {
        final GridView gridView = new GridView(context);
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setGravity(Gravity.CENTER);
        gridView.setNumColumns(4);
        gridView.setHorizontalSpacing((int)context.getResources().getDimension(R.dimen.small_margin_size));
        gridView.setVerticalSpacing((int)context.getResources().getDimension(R.dimen.small_margin_size));
        int pixels = context.getResources().getDimensionPixelSize(R.dimen.default_margin_size);
        gridView.setPadding(pixels, pixels, pixels, pixels);
        IndoorPlayerSelectionAdapter playerSelectionAdapter = new IndoorPlayerSelectionAdapter(layoutInflater, context, teamService, sanction, teamType, players) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                IndoorPlayerSelectionDialog.this.onPlayerSelected(selectedNumber);

                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        };
        gridView.setAdapter(playerSelectionAdapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        builder.setTitle(title).setView(gridView);
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {});

        mAlertDialog = builder.create();
    }

    public void show() {
        if (mAlertDialog != null) {
            mAlertDialog.show();
        }
    }

    public abstract void onPlayerSelected(int selectedNumber);

    private abstract class IndoorPlayerSelectionAdapter extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private final Context        mContext;
        private final IBaseTeam      mTeamService;
        private final ISanction      mSanction;
        private final TeamType       mTeamType;
        private final List<Integer>  mPlayers;

        IndoorPlayerSelectionAdapter(LayoutInflater layoutInflater, Context context, IBaseTeam teamService, ISanction sanction, TeamType teamType, Set<Integer> players) {
            mLayoutInflater = layoutInflater;
            mContext = context;
            mTeamService = teamService;
            mSanction = sanction;
            mTeamType = teamType;
            mPlayers = new ArrayList<>(players);
        }

        @Override
        public int getCount() {
            return mPlayers.size();
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
            final int number = mPlayers.get(position);
            MaterialButton button;

            if (convertView == null) {
                button = (MaterialButton) mLayoutInflater.inflate(R.layout.player_item, null);
            } else {
                button = (MaterialButton) convertView;
            }

            button.setText(UiUtils.formatNumberFromLocale(number));
            UiUtils.styleTeamButton(mContext, mTeamService, mTeamType, number, button);

            if (isExpulsedOrDisqualified(number)) {
                button.setEnabled(false);
                UiUtils.colorTeamButton(mContext, ContextCompat.getColor(mContext, R.color.colorDisabledButton), button);
                button.setTextColor(ContextCompat.getColor(mContext, R.color.colorRedCard));

            } else {
                button.setEnabled(true);
                button.setOnClickListener(view -> {
                    final int selectedNumber = Integer.parseInt(((MaterialButton) view).getText().toString());
                    onPlayerSelected(selectedNumber);
                });
            }

            return button;
        }

        public abstract void onPlayerSelected(int selectedNumber);

        private boolean isExpulsedOrDisqualified(int number) {
            boolean result = false;

            if (mSanction != null) {
                result = mSanction.getExpulsedOrDisqualifiedPlayersForCurrentSet(mTeamType).contains(number);
            }

            return result;
        }

    }

}
