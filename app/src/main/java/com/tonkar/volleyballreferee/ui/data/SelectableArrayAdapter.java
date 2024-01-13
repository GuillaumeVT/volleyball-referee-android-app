package com.tonkar.volleyballreferee.ui.data;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.*;
import androidx.collection.ArraySet;

import java.util.*;

public abstract class SelectableArrayAdapter<T> extends ArrayAdapter<T> {

    private final Set<String> mSelectedItems;

    protected SelectableArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<T> objects) {
        super(context, resource, objects);
        mSelectedItems = new ArraySet<>();
    }

    public void toggleItemSelection(String itemId) {
        if (isSelectedItem(itemId)) {
            mSelectedItems.remove(itemId);
        } else {
            mSelectedItems.add(itemId);
        }
        notifyDataSetChanged();
    }

    protected boolean isSelectedItem(String itemId) {
        return mSelectedItems.contains(itemId);
    }

    public boolean hasSelectedItems() {
        return mSelectedItems.size() > 0;
    }

    public void clearSelectedItems() {
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public Set<String> getSelectedItems() {
        return mSelectedItems;
    }
}
