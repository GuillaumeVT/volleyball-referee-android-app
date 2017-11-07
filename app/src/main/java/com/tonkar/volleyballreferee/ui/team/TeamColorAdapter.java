package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.tonkar.volleyballreferee.R;

import java.util.Random;

public class TeamColorAdapter extends BaseAdapter {

    private final Context        mContext;
    private final LayoutInflater mLayoutInflater;
    private final Random         mRandom;

    private final int[]          mColors = {R.color.colorShirt1, R.color.colorShirt2, R.color.colorShirt3, R.color.colorShirt4,
            R.color.colorShirt5, R.color.colorShirt6, R.color.colorShirt7, R.color.colorShirt8,
            R.color.colorShirt9};

    TeamColorAdapter(final Context context, final LayoutInflater layoutInflater) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mRandom = new Random();
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
    public View getView(int index, View view, ViewGroup viewGroup) {
        final View colorView;

        if (view == null) {
            colorView = mLayoutInflater.inflate(R.layout.color_spinner_row, null);
        }
        else {
            colorView = view;
        }

        ImageView image = colorView.findViewById(R.id.color_circle);
        image.setColorFilter(ContextCompat.getColor(mContext, mColors[index]), PorterDuff.Mode.SRC_IN);
        return colorView;
    }

    int getRandomColorIndex() {
        return mRandom.nextInt(getCount() - 1);
    }
}
