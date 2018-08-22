package com.tonkar.volleyballreferee.ui.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class ClearableTextInputAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    public ClearableTextInputAutoCompleteTextView(Context context) {
        super(context);
        addTextClearListener();
    }

    public ClearableTextInputAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addTextClearListener();
    }

    public ClearableTextInputAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addTextClearListener();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        final InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic != null && outAttrs.hintText == null) {
            // If we don't have a hint and our parent is a TextInputLayout, use it's hint for the
            // EditorInfo. This allows us to display a hint in 'extract mode'.
            final ViewParent parent = getParent();
            if (parent instanceof TextInputLayout) {
                outAttrs.hintText = ((TextInputLayout) parent).getHint();
            }
        }
        return ic;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void addTextClearListener() {
        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                ClearableTextInputAutoCompleteTextView textView = ClearableTextInputAutoCompleteTextView.this;
                final int drawableLeftIndex = 0;
                final int drawableRightIndex = 2;
                final Drawable drawableLeft = textView.getCompoundDrawables()[drawableLeftIndex];
                final Drawable drawableRight = textView.getCompoundDrawables()[drawableRightIndex];

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (drawableRight != null && (event.getX() >= (textView.getWidth() - drawableRight.getBounds().width()))) {
                        textView.getText().clear();
                        return performClick();
                    } else if (drawableLeft != null && event.getX() <= drawableLeft.getBounds().width()) {
                        textView.getText().clear();
                        return performClick();
                    }
                }
                return false;
            }
        });
    }
}
