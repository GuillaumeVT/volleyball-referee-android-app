package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.util.*;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class PlayerToggleButton extends MaterialButton {

    private int mBackgroundColor;
    private int mTextColor;
    private int mCheckedBackgroundColor;
    private int mCheckedTextColor;

    public PlayerToggleButton(Context context) {
        super(context);
        init(context);
    }

    public PlayerToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayerToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setCheckable(true);
        mBackgroundColor = ContextCompat.getColor(context, R.color.colorDisabledButton);
        mTextColor = ContextCompat.getColor(context, R.color.colorDisabledText);
        mCheckedBackgroundColor = ContextCompat.getColor(context, R.color.colorPrimary);
        mCheckedTextColor = ContextCompat.getColor(context, R.color.colorOnPrimary);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.default_text_size));
        color();

        addOnCheckedChangeListener((button, isChecked) -> color());
    }

    private void color() {
        if (isChecked()) {
            UiUtils.colorTeamButton(getContext(), mCheckedBackgroundColor, this);
            setTextColor(mCheckedTextColor);
        } else {
            UiUtils.colorTeamButton(getContext(), mBackgroundColor, this);
            setTextColor(mTextColor);
        }
    }

    public void setColor(Context context, int backgroundColor) {
        mCheckedBackgroundColor = backgroundColor;
        mCheckedTextColor = UiUtils.getTextColor(context, mCheckedBackgroundColor);
        color();
    }

}
