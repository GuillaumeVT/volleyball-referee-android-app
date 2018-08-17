package com.tonkar.volleyballreferee.ui.util;

import android.content.Context;
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

    public void addTextClearListener() {
        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int drawableRight = 2;

                if(event.getAction() == MotionEvent.ACTION_UP
                        && (event.getRawX() >= (ClearableTextInputEditText.this.getRight() - ClearableTextInputEditText.this.getCompoundDrawables()[drawableRight].getBounds().width()))) {
                    ClearableTextInputEditText.this.getText().clear();
                    return performClick();
                }
                return false;
            }
        });
    }
}
