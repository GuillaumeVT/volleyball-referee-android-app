package com.tonkar.volleyballreferee.ui.team;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.tonkar.volleyballreferee.R;

public class TeamColorDialogFragment extends DialogFragment {

    private Context                    mContext;
    private AlertDialog                mDialog;
    private TeamColorSelectionListener mTeamColorSelectionListener;

    public static TeamColorDialogFragment newInstance() {
        TeamColorDialogFragment fragment = new TeamColorDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        TeamColorAdapter teamColorAdapter = new TeamColorAdapter();

        final GridView gridView = new GridView(mContext);
        gridView.setNumColumns(4);
        gridView.setGravity(Gravity.CENTER);
        gridView.setPadding(8, 8, 8, 8);
        gridView.setAdapter(teamColorAdapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.select_shirts_color)).setView(gridView);
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        mDialog = builder.create();

        return mDialog;
    }

    void setTeamColorSelectionListener(TeamColorSelectionListener teamColorSelectionListener) {
        mTeamColorSelectionListener = teamColorSelectionListener;
    }

    public interface TeamColorSelectionListener {

        void onTeamColorSelected(int colorId);

    }

    private class TeamColorAdapter extends BaseAdapter {

        private final int[] mColors;

        TeamColorAdapter() {
            mColors = ShirtColors.getShirtColors();
        }

        @Override
        public int getCount() {
            return mColors.length;
        }

        @Override
        public Object getItem(int index) {
            return mColors[index];
        }

        @Override
        public long getItemId(int index) {
            return 0;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup viewGroup) {
            Button button;

            if (convertView == null) {
                button = new Button(mContext);
            } else {
                button = (Button) convertView;
            }

            final int colorId = mColors[index];
            button.getBackground().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(mContext, colorId), PorterDuff.Mode.SRC));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTeamColorSelectionListener.onTeamColorSelected(colorId);

                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
            });
            return button;
        }
    }
}
