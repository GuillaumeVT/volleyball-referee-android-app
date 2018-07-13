package com.tonkar.volleyballreferee.ui.rules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntegerRuleAdapter extends ArrayAdapter<Integer> {

    private final LayoutInflater mLayoutInflater;
    private final List<String>   mDisplayedValues;
    private final List<Integer>  mActualValues;

    public IntegerRuleAdapter(Context context, LayoutInflater layoutInflater, String[] displayedValues, String[] actualValues) {
        super(context, R.layout.rule_spinner);
        mLayoutInflater = layoutInflater;
        mDisplayedValues = Arrays.asList(displayedValues);
        mActualValues = new ArrayList<>();
        for (String actualValue : actualValues) {
            mActualValues.add(Integer.valueOf(actualValue));
        }
    }

    @Override
    public int getCount() {
        return mActualValues.size();
    }

    @Override
    public Integer getItem(int index) {
        return mActualValues.get(index);
    }

    @Override
    public int getPosition(Integer value) {
        return mActualValues.indexOf(value);
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        TextView textView;

        if (view == null) {
            textView = (TextView) mLayoutInflater.inflate(R.layout.rule_spinner, null);
        } else {
            textView = (TextView) view;
        }

        textView.setText(mDisplayedValues.get(index));

        return textView;
    }

    @Override
    public View getDropDownView(int index, View view, ViewGroup parent) {
        TextView textView;

        if (view == null) {
            textView = (TextView) mLayoutInflater.inflate(R.layout.rule_entry, null);
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
