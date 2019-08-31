package com.tonkar.volleyballreferee.ui.setup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tonkar.volleyballreferee.R;

import java.util.List;

public abstract class NameSpinnerAdapter<T> extends ArrayAdapter<T> {

    private final LayoutInflater mLayoutInflater;
    private final List<T>        mItems;

    NameSpinnerAdapter(@NonNull Context context, LayoutInflater layoutInflater, @NonNull List<T> items) {
        super(context, R.layout.vbr_spinner, items);
        mLayoutInflater = layoutInflater;
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public T getItem(int index) {
        return mItems.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    int getPositionFromId(String id) {
        boolean found = false;
        int position = -1;
        int index = 0;

        while (!found && index < mItems.size()) {
            if (id.equals(getId(mItems.get(index)))) {
                found = true;
                position = index;
            }
            index++;
        }

        return position;
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
        TextView textView;

        if (view == null) {
            textView = (TextView) mLayoutInflater.inflate(R.layout.vbr_spinner, null);
        } else {
            textView = (TextView) view;
        }

        textView.setText(getName(mItems.get(index)));

        return textView;
    }

    @Override
    public View getDropDownView(int index, View view, @NonNull ViewGroup parent) {
        TextView textView;

        if (view == null) {
            textView = (TextView) mLayoutInflater.inflate(R.layout.vbr_checked_spinner_entry, null);
        } else {
            textView = (TextView) view;
        }

        textView.setText(getName(mItems.get(index)));

        return textView;
    }

    public abstract String getName(T item);

    public abstract String getId(T item);
}
