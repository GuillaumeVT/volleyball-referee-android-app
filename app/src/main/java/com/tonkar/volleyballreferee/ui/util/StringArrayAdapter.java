package com.tonkar.volleyballreferee.ui.util;

import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;

import com.tonkar.volleyballreferee.R;

import java.util.*;

public class StringArrayAdapter extends ArrayAdapter<String> {

    private final Context        mContext;
    private final LayoutInflater mLayoutInflater;
    private final List<String>   mDisplayedValues;
    private final List<String>   mActualValues;

    public StringArrayAdapter(Context context, LayoutInflater layoutInflater, String[] displayedValues, String[] actualValues) {
        super(context, R.layout.vbr_spinner);
        mContext = context;
        mLayoutInflater = layoutInflater;
        mDisplayedValues = Arrays.asList(displayedValues);
        mActualValues = Arrays.asList(actualValues);
    }

    @Override
    public int getCount() {
        return mActualValues.size();
    }

    @Override
    public String getItem(int index) {
        return mActualValues.get(index);
    }

    @Override
    public int getPosition(String value) {
        return mActualValues.indexOf(value);
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
        TextView textView;

        if (view == null) {
            textView = (TextView) mLayoutInflater.inflate(R.layout.vbr_spinner, parent, false);
        } else {
            textView = (TextView) view;
        }

        textView.setText(mDisplayedValues.get(index));
        textView.setTextColor(mContext.getColor(R.color.colorOnSurface));

        return textView;
    }

    @Override
    public View getDropDownView(int index, View view, @NonNull ViewGroup parent) {
        TextView textView;

        if (view == null) {
            textView = (TextView) mLayoutInflater.inflate(R.layout.vbr_checked_spinner_entry, parent, false);
        } else {
            textView = (TextView) view;
        }

        textView.setText(mDisplayedValues.get(index));

        return textView;
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }
}
