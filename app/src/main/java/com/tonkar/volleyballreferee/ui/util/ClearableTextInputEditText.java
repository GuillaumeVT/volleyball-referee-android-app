package com.tonkar.volleyballreferee.ui.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;

public class ClearableTextInputEditText extends TextInputEditText {

    public ClearableTextInputEditText(Context context) {
        super(context);
        addTextClearListener();
    }

    public ClearableTextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        addTextClearListener();
    }

    public ClearableTextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addTextClearListener();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void addTextClearListener() {
        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                ClearableTextInputEditText textView = ClearableTextInputEditText.this;
                final int drawableRightIndex = 2;
                final Drawable drawableRight = textView.getCompoundDrawables()[drawableRightIndex];

                if(event.getAction() == MotionEvent.ACTION_UP
                        && drawableRight != null
                        && (event.getRawX() >= (textView.getRight() - drawableRight.getBounds().width()))) {
                    textView.getText().clear();
                    return performClick();
                }
                return false;
            }
        });
    }
}
