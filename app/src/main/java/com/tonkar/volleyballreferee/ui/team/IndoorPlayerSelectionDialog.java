package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class IndoorPlayerSelectionDialog {

    private AlertDialog mAlertDialog;

    IndoorPlayerSelectionDialog(LayoutInflater layoutInflater, Context context, String title, BaseTeamService teamService, TeamType teamType, Set<Integer> players) {
        this(layoutInflater, context, title, teamService, null, teamType, players);
    }

    protected IndoorPlayerSelectionDialog(LayoutInflater layoutInflater, Context context, String title, BaseTeamService teamService, SanctionService sanctionService, TeamType teamType, Set<Integer> players) {
        final GridView gridView = new GridView(context);
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setGravity(Gravity.CENTER);
        gridView.setNumColumns(3);
        int pixels = context.getResources().getDimensionPixelSize(R.dimen.default_margin_size);
        gridView.setPadding(pixels, pixels, pixels, pixels);
        IndoorPlayerSelectionAdapter playerSelectionAdapter = new IndoorPlayerSelectionAdapter(layoutInflater, context, teamService, sanctionService, teamType, players) {
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
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        mAlertDialog = builder.create();
    }

    public void show() {
        if (mAlertDialog != null) {
            mAlertDialog.show();
        }
    }

    public abstract void onPlayerSelected(int selectedNumber);

    private abstract class IndoorPlayerSelectionAdapter extends BaseAdapter {

        private final LayoutInflater  mLayoutInflater;
        private final Context         mContext;
        private final BaseTeamService mTeamService;
        private final SanctionService mSanctionService;
        private final TeamType        mTeamType;
        private final List<Integer>   mPlayers;

        IndoorPlayerSelectionAdapter(LayoutInflater layoutInflater, Context context, BaseTeamService teamService, SanctionService sanctionService, TeamType teamType, Set<Integer> players) {
            mLayoutInflater = layoutInflater;
            mContext = context;
            mTeamService = teamService;
            mSanctionService = sanctionService;
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
            Button button;

            if (convertView == null) {
                button = (Button) mLayoutInflater.inflate(R.layout.player_item, null);
            } else {
                button = (Button) convertView;
            }

            button.setText(String.valueOf(number));
            UiUtils.styleTeamButton(mContext, mTeamService, mTeamType, number, button);

            if (isExpulsedOrDisqualified(number)) {
                button.setEnabled(false);
                UiUtils.colorTeamButton(mContext, ContextCompat.getColor(mContext, R.color.colorDisabledButton), button);
                button.setTextColor(ContextCompat.getColor(mContext, R.color.colorRedCard));

            } else {
                button.setEnabled(true);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int selectedNumber = Integer.parseInt(((Button) view).getText().toString());
                        onPlayerSelected(selectedNumber);
                    }
                });
            }

            return button;
        }

        public abstract void onPlayerSelected(int selectedNumber);

        private boolean isExpulsedOrDisqualified(int number) {
            boolean result = false;

            if (mSanctionService != null) {
                result = mSanctionService.getExpulsedOrDisqualifiedPlayersForCurrentSet(mTeamType).contains(number);
            }

            return result;
        }

    }

}
