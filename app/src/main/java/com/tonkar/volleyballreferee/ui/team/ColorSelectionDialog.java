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
import com.tonkar.volleyballreferee.ui.UiUtils;

public abstract class ColorSelectionDialog {

    private AlertDialog mAlertDialog;

    protected ColorSelectionDialog(LayoutInflater layoutInflater, Context context, String title) {
        final GridView gridView = new GridView(context);
        gridView.setNumColumns(4);
        gridView.setGravity(Gravity.CENTER);
        int pixels = context.getResources().getDimensionPixelSize(R.dimen.default_margin_size);
        gridView.setPadding(pixels, pixels, pixels, pixels);
        ColorSelectionAdapter colorSelectionAdapter = new ColorSelectionAdapter(layoutInflater, context) {
            @Override
            public void onColorSelected(int selectedColor) {
                ColorSelectionDialog.this.onColorSelected(selectedColor);

                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                }
            }
        };
        gridView.setAdapter(colorSelectionAdapter);

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

    public abstract void onColorSelected(int selectedColor);

    private abstract class ColorSelectionAdapter extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private final Context        mContext;
        private final int[]          mColorIds;

        ColorSelectionAdapter(LayoutInflater layoutInflater, Context context) {
            mLayoutInflater = layoutInflater;
            mContext = context;
            mColorIds = ShirtColors.getShirtColorIds();
        }

        @Override
        public int getCount() {
            return mColorIds.length;
        }

        @Override
        public Object getItem(int index) {
            return null;
        }

        @Override
        public long getItemId(int index) {
            return 0;
        }

        @Override
        public View getView(int index, View convertView, ViewGroup viewGroup) {
            Button button;

            if (convertView == null) {
                button = (Button) mLayoutInflater.inflate(R.layout.player_item, null);
            } else {
                button = (Button) convertView;
            }

            int colorId = mColorIds[index];
            final int color = ContextCompat.getColor(mContext, colorId);
            UiUtils.colorTeamButton(mContext, color, button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onColorSelected(color);
                }
            });
            return button;
        }

        public abstract void onColorSelected(int selectedColor);
    }
}
