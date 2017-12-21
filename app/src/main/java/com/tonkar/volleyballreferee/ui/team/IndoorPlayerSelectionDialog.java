package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class IndoorPlayerSelectionDialog {

    private AlertDialog mAlertDialog;

    protected IndoorPlayerSelectionDialog(LayoutInflater layoutInflater, Context context, String title, BaseIndoorTeamService indoorTeamService, TeamType teamType, Set<Integer> players) {
        final GridView gridView = new GridView(context);
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setGravity(Gravity.CENTER);
        int pixels = context.getResources().getDimensionPixelSize(R.dimen.default_margin_size);
        gridView.setPadding(pixels, pixels, pixels, pixels);
        IndoorPlayerSelectionAdapter playerSelectionAdapter = new IndoorPlayerSelectionAdapter(layoutInflater, context, indoorTeamService, teamType, players) {
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

        private final LayoutInflater        mLayoutInflater;
        private final Context               mContext;
        private final BaseIndoorTeamService mIndoorTeamService;
        private final TeamType              mTeamType;
        private final List<Integer>         mPlayers;

        IndoorPlayerSelectionAdapter(LayoutInflater layoutInflater, Context context, BaseIndoorTeamService indoorTeamService, TeamType teamType, Set<Integer> players) {
            mLayoutInflater = layoutInflater;
            mContext = context;
            mIndoorTeamService = indoorTeamService;
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
            UiUtils.styleBaseIndoorTeamButton(mContext, mIndoorTeamService, mTeamType, number, button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int selectedNumber = Integer.parseInt(((Button) view).getText().toString());
                    onPlayerSelected(selectedNumber);
                }
            });

            return button;
        }

        public abstract void onPlayerSelected(int selectedNumber);

    }

}
